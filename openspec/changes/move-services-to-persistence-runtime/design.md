# Design: Move Services to persistence-runtime

## Context

The `persistence-runtime` module is designed to be a reusable library that other Revet services can depend on to embed the documents system in-process — without bringing in the REST API, JWT auth, OpenAPI, or any HTTP concerns. For this to work, `persistence-runtime` must contain the full business logic layer (service implementations) and their supporting CDI beans, not just entities and repositories.

The `split-into-submodules` change placed service implementations in `web/`, which means any consumer wanting documents business logic must depend on the entire `web` module. This defeats the purpose of the split.

## Goals / Non-Goals

- **Goal**: `persistence-runtime` is a self-contained library usable by other Revet services without pulling in `web`
- **Goal**: Web module has no Panache dependency — it is a thin REST/HTTP layer
- **Goal**: Permission CDI beans (`PrebuiltPolicies`, `DocumentsUrn`) move with the services that depend on them
- **Non-Goal**: Creating new interfaces for `UserProvisioningService` or `DocumentStorageService` (they remain concrete classes)
- **Non-Goal**: Changing package names (all modules continue to use `com.revethq.documents.*`)

## Revised Dependency Graph

```
web → core + persistence-runtime
persistence-runtime → core
```

The `web` module depends on `persistence-runtime` at compile time because:
1. `UserProvisioningAugmentor` (web/security) injects `UserProvisioningService`
2. `AuthorizationContextPopulator` (web/security) injects `DocumentsUrn`
3. Quarkus needs the CDI beans on the classpath for build-time augmentation

## File Movement

### From web/service/ to persistence-runtime/service/ (8 files)
- `CategoryServiceImpl.kt`
- `DocumentServiceImpl.kt`
- `DocumentVersionServiceImpl.kt`
- `TagServiceImpl.kt`
- `OrganizationServiceImpl.kt`
- `ProjectServiceImpl.kt`
- `UserProvisioningService.kt`
- `DocumentStorageService.kt`

### From web/permission/ to persistence-runtime/permission/ (2 files)
- `PrebuiltPolicies.kt`
- `DocumentsUrn.kt`

## Dependency Changes

### persistence-runtime/build.gradle.kts — additions

```kotlin
// CDI
implementation(libs.quarkus.arc)
implementation(libs.kotlin.stdlib)

// Revet IAM (for OrganizationServiceImpl, ProjectServiceImpl, UserProvisioningService)
implementation(libs.revet.iam.user)
implementation(libs.revet.iam.user.persistence)
implementation(libs.revet.iam.permission)
implementation(libs.revet.iam.permission.persistence)
implementation(libs.revet.iam.permission.web)

// Revet Buckets (for DocumentStorageService, PrebuiltPolicies)
implementation(libs.revet.buckets.core)
implementation(libs.revet.buckets.persistence)
implementation(libs.revet.buckets.provider.s3)
implementation(libs.revet.buckets.provider.gcs)
```

Also add allOpen annotations for CDI bean proxying:
```kotlin
allOpen {
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.enterprise.context.RequestScoped")
}
```

### web/build.gradle.kts — removals

```kotlin
// REMOVE — no longer needed in web
implementation(libs.quarkus.hibernate.orm.panache.kotlin)
implementation(libs.revet.iam.user.persistence)
implementation(libs.revet.iam.permission.persistence)
implementation(libs.revet.buckets.persistence)
implementation(libs.revet.buckets.provider.s3)
implementation(libs.revet.buckets.provider.gcs)
```

### web/build.gradle.kts — additions

```kotlin
implementation(project(":persistence-runtime"))
```

### web/build.gradle.kts — kept (still needed by web code)

| Dependency | Used by |
|-----------|---------|
| `revet.iam.permission.web` | `RequiresPermission` (all resources), `AuthorizationContext` (AuthorizationContextPopulator) |
| `revet.iam.user.web` | `UserProvisioningAugmentor` base classes |
| `revet.iam.scim` | SCIM REST endpoints |
| `revet.buckets.web` | Bucket REST endpoints |
| `revet.buckets.core` | Transitive dependency for revet.buckets.web types |

## Risks / Trade-offs

- **persistence-runtime grows heavier**: It gains revet-iam and revet-buckets dependencies. This is acceptable because service implementations genuinely need these for business logic.
- **Circular dependency avoided**: Web → persistence-runtime is a one-way compile dependency. There is no persistence-runtime → web dependency.
