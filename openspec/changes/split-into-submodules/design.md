# Design: Split Into Submodules

## Module Structure

```
revet-documents/
├── core/                          # Domain models, interfaces
│   └── src/main/kotlin/com/revethq/documents/
├── persistence-runtime/           # Panache entities, repository impls
│   └── src/main/kotlin/com/revethq/documents/
├── web/                           # JAX-RS resources, service impls, DTOs
│   └── src/main/kotlin/com/revethq/documents/
│   └── src/main/resources/        # application.properties, migrations
├── build.gradle.kts               # Root: common config
├── settings.gradle.kts            # includes core, web, persistence-runtime
└── gradle/libs.versions.toml      # Shared version catalog
```

## Dependency Graph

```
web → core
persistence-runtime → core
```

`web` and `persistence-runtime` do NOT depend on each other at compile time. CDI wires repository implementations (persistence-runtime) to service implementations (web) at runtime. The Quarkus application runs from the `web` module.

## File Placement

### core (19 files)

Pure Kotlin library — no Quarkus BOM, no CDI.

| Package | Files | Notes |
|---------|-------|-------|
| `domain/` | Category, Document, DocumentVersion, Organization, Page, Project, Tag, UploadStatus (8) | Unchanged — already framework-agnostic |
| `permission/` | Actions (1) | Pure constants, no CDI |
| `repository/` | 6 interface-only files | Extracted from combined interface+impl files |
| `service/` | 7 interface-only files (incl. SearchService + SearchResults) | Extracted from combined interface+impl files |
| `security/` | CurrentUserService interface (1) | Extracted from combined file |

### persistence-runtime (20 files)

Quarkus module with Panache — depends on `core`.

| Package | Files | Notes |
|---------|-------|-------|
| `repository/entity/` | CategoryEntity, DocumentEntity, DocumentVersionEntity, OrganizationEntity, ProjectEntity, TagEntity, TaggedItemEntity (7) | Unchanged |
| `repository/mapper/` | CategoryMapper, DocumentMapper, DocumentVersionMapper, OrganizationMapper, ProjectMapper, TagMapper (6) | Unchanged |
| `repository/` | 6 implementation files | Extracted from combined files, renamed to `*RepositoryImpl.kt` where needed |
| `service/` | SearchServiceImpl (1) | Uses EntityManager + entity classes directly |

### web (37 files)

Main Quarkus application — depends on `core`.

| Package | Files | Notes |
|---------|-------|-------|
| `api/exception/` | GlobalExceptionMapper (1) | Unchanged |
| `api/mapper/` | 7 DTO mapper files | Unchanged |
| `api/resource/` | 8 resource files | Unchanged |
| `dto/` | 9 DTO files | Unchanged |
| `permission/` | DocumentsUrn, PrebuiltPolicies (2) | CDI beans — depend on revet-iam, revet-buckets |
| `security/` | AuthorizationContextPopulator, CurrentUserServiceImpl, UserProvisioningAugmentor (3) | CurrentUserServiceImpl extracted from combined file |
| `service/` | 6 service impl files + DocumentStorageService + UserProvisioningService (8) | Extracted from combined files |
| `graalvm/` | GrpcNettySubstitutions (1) | GraalVM native image substitutions |

## Interface/Implementation Splitting Strategy

Currently all 6 repository files and 6 service files (excl. SearchService which also has the SearchResults data class, DocumentStorageService, and UserProvisioningService which have no interfaces) contain both the interface and implementation in a single file.

**Approach:** Split each into two files:
- Interface file stays at the original path in the target module (core)
- Implementation file uses the same class name in the target module (persistence-runtime or web)

Example for `OrganizationRepository.kt`:
- `core/.../repository/OrganizationRepository.kt` — contains only the `OrganizationRepository` interface
- `persistence-runtime/.../repository/OrganizationRepositoryImpl.kt` — contains only `OrganizationRepositoryImpl`

## Special Cases

### SearchService

`SearchServiceImpl` uses `EntityManager` and directly references entity classes (`DocumentEntity`, `ProjectEntity`, `OrganizationEntity`) and mappers. It belongs in `persistence-runtime` since it has persistence dependencies. The `SearchService` interface and `SearchResults` data class go in `core`.

### DocumentStorageService

Has no interface — it's a concrete `@ApplicationScoped` class that delegates to `revet-buckets` `StorageService`. Goes in `web`.

### UserProvisioningService

Has no interface — it's a concrete `@ApplicationScoped` class that depends on revet-iam persistence types (`IdentityProviderEntity`, `UserService`). Goes in `web` since those are external library dependencies.

### permission/DocumentsUrn and PrebuiltPolicies

Both are `@ApplicationScoped` CDI beans. `PrebuiltPolicies` depends on revet-iam and revet-buckets domain types. Both go in `web`.

### permission/Actions

Pure constants object with no dependencies. Goes in `core`.

## Build Configuration

### Root build.gradle.kts

Applies common settings (Kotlin version, ktlint, Java target) to all subprojects.

### core/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    `java-library`
}

dependencies {
    api(libs.revet.core)  // for Metadata used in domain
}
```

### persistence-runtime/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.quarkus)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.noarg)
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(project(":core"))
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.kotlin)
    implementation(libs.quarkus.hibernate.orm.panache.kotlin)
    implementation(libs.quarkus.jdbc.postgresql)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
```

### web/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.quarkus)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.ktlint)
}

dependencies {
    implementation(project(":core"))
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.quarkus.kotlin)
    implementation(libs.kotlin.stdlib)
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.rest)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.smallrye.openapi)
    implementation(libs.quarkus.smallrye.health)
    implementation(libs.quarkus.smallrye.jwt)
    implementation(libs.quarkus.reactive.routes)
    implementation(libs.quarkus.container.image.docker)

    // Revet libraries
    implementation(libs.revet.core)
    implementation(libs.revet.iam.user)
    implementation(libs.revet.iam.user.persistence)
    implementation(libs.revet.iam.user.web)
    implementation(libs.revet.iam.permission)
    implementation(libs.revet.iam.permission.persistence)
    implementation(libs.revet.iam.permission.web)
    implementation(libs.revet.iam.scim)
    implementation(libs.revet.buckets.core)
    implementation(libs.revet.buckets.web)
    implementation(libs.revet.buckets.persistence)
    implementation(libs.revet.buckets.provider.s3)
    implementation(libs.revet.buckets.provider.gcs)

    // GraalVM
    compileOnly("org.graalvm.nativeimage:svm:25.0.2")

    // Testing
    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.quarkus.junit5.mockito)
    testImplementation(libs.rest.assured)
    testImplementation(libs.mockk)
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.enterprise.context.RequestScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("jakarta.ws.rs.ext.Provider")
}
```
