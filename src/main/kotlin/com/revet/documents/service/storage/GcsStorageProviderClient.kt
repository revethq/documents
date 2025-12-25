package com.revet.documents.service.storage

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.revet.documents.domain.Bucket
import com.revet.documents.service.FileMetadata
import com.revet.documents.service.PresignedUrl
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

/**
 * Google Cloud Storage implementation of StorageProviderClient.
 *
 * Credentials configuration:
 * - accessKey: (optional) can be used for project ID override
 * - secretKey: the service account JSON (entire JSON content)
 */
class GcsStorageProviderClient(
    private val bucket: com.revet.documents.domain.Bucket
) : com.revet.documents.service.storage.StorageProviderClient {

    private val credentials: ServiceAccountCredentials by lazy {
        val jsonStream = ByteArrayInputStream(bucket.secretKey.toByteArray(Charsets.UTF_8))
        ServiceAccountCredentials.fromStream(jsonStream)
    }

    private val storage: Storage by lazy {
        val builder = StorageOptions.newBuilder()
            .setCredentials(credentials)

        // Use accessKey as project ID if provided
        if (bucket.accessKey.isNotBlank()) {
            builder.setProjectId(bucket.accessKey)
        }

        builder.build().service
    }

    override fun generatePresignedUploadUrl(key: String, contentType: String?): com.revet.documents.service.PresignedUrl {
        val blobInfo = BlobInfo.newBuilder(bucket.bucketName, key)
            .apply {
                contentType?.let { setContentType(it) }
            }
            .build()

        val signedUrl = storage.signUrl(
            blobInfo,
            bucket.presignedUrlDurationMinutes.toLong(),
            TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withContentType()
        )

        return PresignedUrl(
            url = signedUrl.toString(),
            expiresInMinutes = bucket.presignedUrlDurationMinutes,
            s3Key = key
        )
    }

    override fun generatePresignedDownloadUrl(key: String): PresignedUrl {
        val blobInfo = BlobInfo.newBuilder(bucket.bucketName, key).build()

        val signedUrl = storage.signUrl(
            blobInfo,
            bucket.presignedUrlDurationMinutes.toLong(),
            TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.GET)
        )

        return PresignedUrl(
            url = signedUrl.toString(),
            expiresInMinutes = bucket.presignedUrlDurationMinutes,
            s3Key = key
        )
    }

    override fun exists(key: String): Boolean {
        val blobId = BlobId.of(bucket.bucketName, key)
        val blob = storage.get(blobId)
        return blob != null && blob.exists()
    }

    override fun getFileMetadata(key: String): FileMetadata {
        val blobId = BlobId.of(bucket.bucketName, key)
        val blob = storage.get(blobId)
            ?: throw IllegalArgumentException("Object not found: $key")

        return FileMetadata(
            size = blob.size,
            contentType = blob.contentType,
            lastModified = java.time.Instant.ofEpochMilli(blob.updateTimeOffsetDateTime.toInstant().toEpochMilli())
        )
    }

    override fun delete(key: String): Boolean {
        val blobId = BlobId.of(bucket.bucketName, key)
        return storage.delete(blobId)
    }
}
