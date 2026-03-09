## 1. Dependencies & Configuration

- [x] 1.1 Add revet-buckets dependencies to `build.gradle.kts` (core, web, persistence-runtime, provider-s3, provider-gcs)
- [x] 1.2 Remove direct S3 SDK and GCS SDK dependencies from `build.gradle.kts` (now transitive via provider modules)
- [x] 1.3 Update `application.properties`: rename `kala.encryption.key` to `revet.encryption.key`

## 2. Database Migration

- [x] 2.1 Create SQL migration script to rename `kala_buckets` table to `revet_buckets`
- [x] 2.2 Create SQL migration script to update IAM policy actions from `documents:*Bucket*` to `buckets:*Bucket*` and resource URNs from `urn:revet:documents:*:bucket/*` to `urn:revet:buckets:*:bucket/*`

## 3. Delete Local Bucket Implementation

- [x] 3.1 Delete `domain/Bucket.kt` and `domain/StorageProvider.kt`
- [x] 3.2 Delete `repository/entity/BucketEntity.kt` and `repository/mapper/BucketMapper.kt`
- [x] 3.3 Delete `repository/BucketRepository.kt`
- [x] 3.4 Delete `service/BucketService.kt` and `service/EncryptionService.kt`
- [x] 3.5 Delete `service/storage/StorageProviderClient.kt`, `service/storage/StorageClientFactory.kt`, `service/storage/S3StorageProviderClient.kt`, `service/storage/GcsStorageProviderClient.kt`
- [x] 3.6 Delete `dto/BucketDTO.kt`, `api/mapper/BucketDTOMapper.kt`, `api/resource/BucketResource.kt`

## 4. Create DocumentStorageService

- [x] 4.1 Create `service/DocumentStorageService.kt` — wraps library's `StorageService` and adds `generateStorageKey()` method
- [x] 4.2 Delete `service/StorageService.kt` (local interface and implementation)

## 5. Update Consumers

- [x] 5.1 Update `api/resource/DocumentResource.kt` — import `DocumentStorageService` instead of local `StorageService`
- [x] 5.2 Update `api/resource/FileUploadResource.kt` — same import update
- [x] 5.3 Update `api/resource/DocumentVersionResource.kt` — same import update
- [x] 5.4 Update any remaining references to local `PresignedUrl` and `FileMetadata` types to use `com.revethq.buckets.domain.PresignedUrl` and `com.revethq.buckets.domain.FileMetadata`

## 6. Update Permissions

- [x] 6.1 Remove `Bucket` object from `permission/Actions.kt`
- [x] 6.2 Remove bucket-related methods from `permission/DocumentsUrn.kt` (bucket, bucketWildcard, ResourceType.BUCKET)
- [x] 6.3 Update `permission/PrebuiltPolicies.kt` — storage policies use `com.revethq.buckets.permission.Actions.Bucket` and `BucketsUrn` for actions and resources

## 7. Verify

- [x] 7.1 Build project (`./gradlew build`) and fix any compilation errors
- [x] 7.2 Verify Swagger UI shows bucket endpoints from library
