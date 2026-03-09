## Context

The Documents service (Kala API) has a full local implementation of bucket management, storage providers (S3, GCS), and credential encryption. This functionality has been extracted into the `revet-buckets` library as a reusable set of Quarkus modules. The library provides identical domain models, services, persistence, REST endpoints, and provider implementations. We want to replace the local code with the library dependency.

## Goals / Non-Goals

- Goals:
  - Replace all local bucket/storage code with `revet-buckets` library modules
  - Maintain identical REST API behavior at `/api/v1/buckets`
  - Preserve storage operations (presigned URLs, file metadata, deletion) for documents
  - Migrate existing data from `kala_buckets` to `revet_buckets` table
- Non-Goals:
  - Changing the bucket API contract or adding new features
  - Implementing Azure Blob support (remains a stub in the library)
  - Changing how documents reference buckets (Organization.bucketId stays as-is)

## Decisions

### 1. Import library's web module for REST endpoints

The `revet-buckets-web` module provides a JAX-RS resource at `/api/v1/buckets` with the same endpoints. We delete the local `BucketResource` and let the library register its own.

- Alternative: Keep a local resource that delegates to library services. Rejected because the library resource is functionally identical and adds OpenAPI docs automatically.

### 2. Use library's permission model (buckets: prefix)

The library defines actions as `buckets:ListBuckets` rather than `documents:ListBuckets`. We adopt the library's prefix.

- Trade-off: This is a **breaking change** for existing IAM policies. Any policies using `documents:*Bucket*` actions must be updated to use `buckets:*` actions.
- Mitigation: Document the migration and provide a SQL script to update existing policy action strings.

### 3. Extract generateStorageKey() into DocumentStorageService

The local `StorageServiceImpl.generateStorageKey()` is documents-specific (generates paths like `documents/{id}/{uuid}-{filename}`). The library's `StorageService` does not include this. We create a thin `DocumentStorageService` that wraps the library's `StorageService` and adds `generateStorageKey()`.

- Alternative: Make `generateStorageKey()` a standalone utility function. Rejected because it's only used alongside other storage operations and injection keeps it testable.

### 4. Rename database table via SQL migration

The library expects `revet_buckets` table. We add a Flyway/manual SQL migration: `ALTER TABLE kala_buckets RENAME TO revet_buckets`.

- Alternative: Configure Hibernate to map the library entity to `kala_buckets`. Rejected because it would require customizing the library's entity mapping, which defeats the purpose of using a standard library.

### 5. Map encryption key config property

The library reads from `revet.encryption.key`. We update `application.properties` to use this property name (or alias via `kala.encryption.key` → `revet.encryption.key`).

## Risks / Trade-offs

- **Breaking IAM policies** → Mitigation: Provide SQL migration script for policy action/resource updates
- **Table rename in production** → Mitigation: `ALTER TABLE RENAME` is a metadata-only operation in PostgreSQL, safe and fast
- **Library version coupling** → The Documents service now depends on `revet-buckets:0.1.0`. Version bumps need coordination.

## Migration Plan

1. Add revet-buckets dependencies to `build.gradle.kts`
2. Create SQL migration to rename table and update IAM policies
3. Delete local bucket/storage files
4. Create `DocumentStorageService` wrapper
5. Update imports in consumers
6. Update permission actions and URNs
7. Update `application.properties`
8. Build and verify compilation
9. Run integration tests

Rollback: Revert the commit and run `ALTER TABLE revet_buckets RENAME TO kala_buckets`.

## Open Questions

- None — the library's API is a direct match for the existing local implementation.
