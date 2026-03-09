## ADDED Requirements

### Requirement: Library-Based Bucket Management

The Documents service SHALL use the `revet-buckets` library (`com.revethq.buckets:0.1.0`) for all bucket management, storage operations, and credential encryption instead of local implementations.

#### Scenario: Bucket CRUD via library REST endpoints

- **WHEN** a client sends requests to `/api/v1/buckets`
- **THEN** the requests are handled by the `revet-buckets-web` module's JAX-RS resource
- **AND** bucket data is persisted to the `revet_buckets` table via the library's persistence module

#### Scenario: Storage operations use library providers

- **WHEN** the Documents service generates presigned URLs, checks file existence, or retrieves file metadata
- **THEN** it delegates to the library's `StorageService` which uses CDI-discovered `StorageProviderClientFactory` implementations
- **AND** provider-specific clients (S3, GCS, MinIO) are provided by the library's provider modules

#### Scenario: Credential encryption uses library service

- **WHEN** bucket credentials are stored or retrieved
- **THEN** the library's `AesEncryptionService` encrypts/decrypts using the `revet.encryption.key` configuration property

### Requirement: Documents-Specific Storage Key Generation

The Documents service SHALL provide a `DocumentStorageService` that wraps the library's `StorageService` and adds a `generateStorageKey(documentId, fileName)` method for generating document-specific storage paths.

#### Scenario: Generate storage key for document

- **WHEN** `generateStorageKey(documentId=42, fileName="report.pdf")` is called
- **THEN** it returns a key in the format `documents/{documentId}/{uuid}-{sanitizedFileName}`
- **AND** the file name is sanitized to contain only alphanumeric characters, dots, hyphens, and underscores

### Requirement: Library Permission Model

The Documents service SHALL use the `revet-buckets` library's permission actions (`buckets:ListBuckets`, `buckets:GetBucket`, etc.) and URN format (`urn:revet:buckets:{tenantId}:bucket/{uuid}`) for bucket authorization. The local `Actions.Bucket` object and `DocumentsUrn` bucket methods SHALL be removed.

#### Scenario: Bucket permissions use library actions

- **WHEN** a bucket endpoint is accessed
- **THEN** the `@RequiresPermission` annotation references `com.revethq.buckets.permission.Actions.Bucket` constants
- **AND** resource URNs use the `urn:revet:buckets:` namespace

#### Scenario: Prebuilt policies reference library actions

- **WHEN** storage admin or viewer policies are constructed via `PrebuiltPolicies`
- **THEN** they use `com.revethq.buckets.permission.Actions.Bucket` for actions
- **AND** they use `BucketsUrn` for resource URN generation

## REMOVED Requirements

### Requirement: Local Bucket Domain Model
**Reason**: Replaced by `com.revethq.buckets.domain.Bucket` and `com.revethq.buckets.domain.StorageProvider` from the library.
**Migration**: Update all imports from `com.revet.documents.domain.Bucket` to `com.revethq.buckets.domain.Bucket`.

### Requirement: Local Storage Provider Implementation
**Reason**: Replaced by `revet-buckets-provider-s3` and `revet-buckets-provider-gcs` library modules with CDI-based factory discovery.
**Migration**: Delete local `StorageProviderClient`, `StorageClientFactory`, `S3StorageProviderClient`, `GcsStorageProviderClient`. Add provider module dependencies.

### Requirement: Local Encryption Service
**Reason**: Replaced by `com.revethq.buckets.service.EncryptionService` / `AesEncryptionService` from the library's persistence-runtime module.
**Migration**: Delete local `EncryptionService.kt`. Rename config property from `kala.encryption.key` to `revet.encryption.key`.

### Requirement: Local Bucket REST Resource
**Reason**: Replaced by the JAX-RS resource provided by `revet-buckets-web` module.
**Migration**: Delete local `BucketResource.kt`, `BucketDTOMapper.kt`, and `BucketDTO.kt`. The library registers its own resource at the same path.

### Requirement: Local Bucket Persistence
**Reason**: Replaced by `revet-buckets-persistence-runtime` module (entity, repository, mapper).
**Migration**: Delete local `BucketEntity.kt`, `BucketMapper.kt`, `BucketRepository.kt`. Rename database table from `kala_buckets` to `revet_buckets`.
