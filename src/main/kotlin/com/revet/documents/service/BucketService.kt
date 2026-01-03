package com.revet.documents.service

import com.revet.documents.domain.Bucket
import com.revet.documents.domain.StorageProvider
import com.revet.documents.repository.BucketRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Service interface for Bucket business logic.
 */
interface BucketService {
    fun getAllBuckets(includeInactive: Boolean = false): List<com.revet.documents.domain.Bucket>
    fun getBucketById(id: Long): com.revet.documents.domain.Bucket?
    fun getBucketByUuid(uuid: UUID): com.revet.documents.domain.Bucket?
    fun createBucket(
        name: String,
        provider: com.revet.documents.domain.StorageProvider,
        bucketName: String,
        accessKey: String,
        secretKey: String,
        endpoint: String? = null,
        region: String? = null,
        presignedUrlDurationMinutes: Int = 15
    ): com.revet.documents.domain.Bucket
    fun updateBucket(
        id: Long,
        name: String? = null,
        bucketName: String? = null,
        endpoint: String? = null,
        region: String? = null,
        accessKey: String? = null,
        secretKey: String? = null,
        presignedUrlDurationMinutes: Int? = null,
        isActive: Boolean? = null
    ): com.revet.documents.domain.Bucket?
    fun updateBucketByUuid(
        uuid: UUID,
        name: String? = null,
        bucketName: String? = null,
        endpoint: String? = null,
        region: String? = null,
        accessKey: String? = null,
        secretKey: String? = null,
        presignedUrlDurationMinutes: Int? = null,
        isActive: Boolean? = null
    ): com.revet.documents.domain.Bucket?
    fun deleteBucket(id: Long): Boolean
    fun deleteBucketByUuid(uuid: UUID): Boolean
}

/**
 * Implementation of BucketService with business logic.
 */
@ApplicationScoped
class BucketServiceImpl @Inject constructor(
    private val bucketRepository: com.revet.documents.repository.BucketRepository
) : com.revet.documents.service.BucketService {

    override fun getAllBuckets(includeInactive: Boolean): List<com.revet.documents.domain.Bucket> {
        return bucketRepository.findAll(includeInactive)
    }

    override fun getBucketById(id: Long): com.revet.documents.domain.Bucket? {
        return bucketRepository.findById(id)
    }

    override fun getBucketByUuid(uuid: UUID): com.revet.documents.domain.Bucket? {
        return bucketRepository.findByUuid(uuid)
    }

    override fun createBucket(
        name: String,
        provider: com.revet.documents.domain.StorageProvider,
        bucketName: String,
        accessKey: String,
        secretKey: String,
        endpoint: String?,
        region: String?,
        presignedUrlDurationMinutes: Int
    ): com.revet.documents.domain.Bucket {
        require(name.isNotBlank()) { "Bucket name cannot be blank" }
        require(bucketName.isNotBlank()) { "Bucket name (S3/GCS bucket) cannot be blank" }
        require(accessKey.isNotBlank()) { "Access key cannot be blank" }
        require(secretKey.isNotBlank()) { "Secret key cannot be blank" }
        require(presignedUrlDurationMinutes > 0) { "Presigned URL duration must be positive" }

        // MinIO and S3-compatible services require an endpoint
        if (provider == _root_ide_package_.com.revet.documents.domain.StorageProvider.MINIO) {
            require(!endpoint.isNullOrBlank()) { "Endpoint is required for MinIO provider" }
        }

        val bucket = _root_ide_package_.com.revet.documents.domain.Bucket.create(
            name = name,
            provider = provider,
            bucketName = bucketName,
            accessKey = accessKey,
            secretKey = secretKey,
            endpoint = endpoint,
            region = region,
            presignedUrlDurationMinutes = presignedUrlDurationMinutes
        )

        return bucketRepository.save(bucket)
    }

    override fun updateBucket(
        id: Long,
        name: String?,
        bucketName: String?,
        endpoint: String?,
        region: String?,
        accessKey: String?,
        secretKey: String?,
        presignedUrlDurationMinutes: Int?,
        isActive: Boolean?
    ): com.revet.documents.domain.Bucket? {
        val existing = bucketRepository.findById(id) ?: return null

        name?.let { require(it.isNotBlank()) { "Bucket name cannot be blank" } }
        bucketName?.let { require(it.isNotBlank()) { "Bucket name (S3/GCS bucket) cannot be blank" } }
        accessKey?.let { require(it.isNotBlank()) { "Access key cannot be blank" } }
        secretKey?.let { require(it.isNotBlank()) { "Secret key cannot be blank" } }
        presignedUrlDurationMinutes?.let { require(it > 0) { "Presigned URL duration must be positive" } }

        val updated = existing.update(
            name = name,
            bucketName = bucketName,
            endpoint = endpoint,
            region = region,
            accessKey = accessKey,
            secretKey = secretKey,
            presignedUrlDurationMinutes = presignedUrlDurationMinutes,
            isActive = isActive
        )

        return bucketRepository.save(updated)
    }

    override fun deleteBucket(id: Long): Boolean {
        return bucketRepository.delete(id)
    }

    override fun updateBucketByUuid(
        uuid: UUID,
        name: String?,
        bucketName: String?,
        endpoint: String?,
        region: String?,
        accessKey: String?,
        secretKey: String?,
        presignedUrlDurationMinutes: Int?,
        isActive: Boolean?
    ): com.revet.documents.domain.Bucket? {
        val bucket = bucketRepository.findByUuid(uuid) ?: return null
        return updateBucket(bucket.id!!, name, bucketName, endpoint, region, accessKey, secretKey, presignedUrlDurationMinutes, isActive)
    }

    override fun deleteBucketByUuid(uuid: UUID): Boolean {
        val bucket = bucketRepository.findByUuid(uuid) ?: return false
        return bucketRepository.delete(bucket.id!!)
    }
}
