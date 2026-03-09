# Change: Replace local bucket implementation with revet-buckets library

## Why

The bucket management, storage provider, and encryption code in the Documents service is a general-purpose concern that has been extracted into the `revet-buckets` library (`com.revethq.buckets:0.1.0`). Replacing the local implementation with the library reduces maintenance burden, ensures consistency across Revet services, and gives us CDI-based provider discovery for free.

## What Changes

- **Remove 14 local files** — domain model, entity, repository, service, mappers, DTOs, REST resource, storage provider clients, encryption service, and storage client factory
- **Add revet-buckets Gradle dependencies** — core, web, persistence-runtime, provider-s3, provider-gcs
- **Remove direct S3/GCS SDK dependencies** — these are now transitive via provider modules
- **Update imports** in `DocumentResource`, `FileUploadResource`, `DocumentVersionResource` to use library's `StorageService`, `PresignedUrl`, and `FileMetadata` types
- **Extract `generateStorageKey()`** from `StorageServiceImpl` into a documents-specific utility (this method is not part of the library)
- **Update `Actions.kt`** — remove `Bucket` object (library provides `com.revethq.buckets.permission.Actions.Bucket` with `buckets:` prefix)
- **Update `DocumentsUrn.kt`** — remove bucket-related methods (library provides `BucketsUrn`)
- **Update `PrebuiltPolicies.kt`** — bucket policies reference library actions and URNs
- **BREAKING: Permission action prefix change** — `documents:ListBuckets` becomes `buckets:ListBuckets` (affects existing IAM policies)
- **BREAKING: URN namespace change** — `urn:revet:documents:{tenantId}:bucket/*` becomes `urn:revet:buckets:{tenantId}:bucket/*`
- **BREAKING: Database table rename** — `kala_buckets` becomes `revet_buckets` (requires SQL migration)
- **Config property change** — `kala.encryption.key` becomes `revet.encryption.key`

## Impact

- Affected specs: `bucket-management` (new spec capturing library integration)
- Affected code:
  - **Deleted**: `domain/Bucket.kt`, `domain/StorageProvider.kt`, `repository/entity/BucketEntity.kt`, `repository/mapper/BucketMapper.kt`, `repository/BucketRepository.kt`, `service/BucketService.kt`, `service/StorageService.kt`, `service/EncryptionService.kt`, `service/storage/StorageProviderClient.kt`, `service/storage/StorageClientFactory.kt`, `service/storage/S3StorageProviderClient.kt`, `service/storage/GcsStorageProviderClient.kt`, `dto/BucketDTO.kt`, `api/mapper/BucketDTOMapper.kt`, `api/resource/BucketResource.kt`
  - **Modified**: `build.gradle.kts`, `application.properties`, `permission/Actions.kt`, `permission/DocumentsUrn.kt`, `permission/PrebuiltPolicies.kt`, `api/resource/DocumentResource.kt`, `api/resource/FileUploadResource.kt`, `api/resource/DocumentVersionResource.kt`, `domain/Organization.kt` (bucketId type stays `Long?`)
