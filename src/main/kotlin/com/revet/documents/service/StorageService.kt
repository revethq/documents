package com.revet.documents.service

import com.revet.documents.repository.BucketRepository
import com.revet.documents.service.storage.StorageClientFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Service for file storage operations using configured buckets.
 */
interface StorageService {
    fun generatePresignedUploadUrl(bucketId: Long, s3Key: String, contentType: String?): com.revet.documents.service.PresignedUrl
    fun generatePresignedDownloadUrl(bucketId: Long, s3Key: String): com.revet.documents.service.PresignedUrl
    fun checkFileExists(bucketId: Long, s3Key: String): Boolean
    fun getFileMetadata(bucketId: Long, s3Key: String): com.revet.documents.service.FileMetadata
    fun deleteFile(bucketId: Long, s3Key: String): Boolean
    fun generateStorageKey(documentId: Long, fileName: String): String
}

/**
 * Data class for file metadata from storage.
 */
data class FileMetadata(
    val size: Long,
    val contentType: String?,
    val lastModified: java.time.Instant
)

/**
 * Data class for presigned URL information.
 */
data class PresignedUrl(
    val url: String,
    val expiresInMinutes: Int,
    val s3Key: String
)

/**
 * Implementation of StorageService using dynamic bucket configuration.
 */
@ApplicationScoped
class StorageServiceImpl @Inject constructor(
    private val bucketRepository: com.revet.documents.repository.BucketRepository,
    private val storageClientFactory: com.revet.documents.service.storage.StorageClientFactory
) : com.revet.documents.service.StorageService {

    override fun generatePresignedUploadUrl(bucketId: Long, s3Key: String, contentType: String?): com.revet.documents.service.PresignedUrl {
        val bucket = bucketRepository.findById(bucketId)
            ?: throw IllegalArgumentException("Bucket with id $bucketId not found")

        val client = storageClientFactory.createClient(bucket)
        return client.generatePresignedUploadUrl(s3Key, contentType)
    }

    override fun generatePresignedDownloadUrl(bucketId: Long, s3Key: String): com.revet.documents.service.PresignedUrl {
        val bucket = bucketRepository.findById(bucketId)
            ?: throw IllegalArgumentException("Bucket with id $bucketId not found")

        val client = storageClientFactory.createClient(bucket)
        return client.generatePresignedDownloadUrl(s3Key)
    }

    override fun checkFileExists(bucketId: Long, s3Key: String): Boolean {
        val bucket = bucketRepository.findById(bucketId)
            ?: throw IllegalArgumentException("Bucket with id $bucketId not found")

        val client = storageClientFactory.createClient(bucket)
        return client.exists(s3Key)
    }

    override fun getFileMetadata(bucketId: Long, s3Key: String): com.revet.documents.service.FileMetadata {
        val bucket = bucketRepository.findById(bucketId)
            ?: throw IllegalArgumentException("Bucket with id $bucketId not found")

        val client = storageClientFactory.createClient(bucket)
        return client.getFileMetadata(s3Key)
    }

    override fun deleteFile(bucketId: Long, s3Key: String): Boolean {
        val bucket = bucketRepository.findById(bucketId)
            ?: throw IllegalArgumentException("Bucket with id $bucketId not found")

        val client = storageClientFactory.createClient(bucket)
        return client.delete(s3Key)
    }

    override fun generateStorageKey(documentId: Long, fileName: String): String {
        val sanitizedFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val uuid = UUID.randomUUID().toString()
        return "documents/$documentId/$uuid-$sanitizedFileName"
    }
}
