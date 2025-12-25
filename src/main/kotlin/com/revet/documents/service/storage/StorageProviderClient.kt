package com.revet.documents.service.storage

import com.revet.documents.service.FileMetadata
import com.revet.documents.service.PresignedUrl

/**
 * Interface for cloud storage provider operations.
 * Implementations handle provider-specific logic for S3, GCS, Azure Blob, MinIO, etc.
 */
interface StorageProviderClient {
    /**
     * Generates a presigned URL for uploading a file.
     */
    fun generatePresignedUploadUrl(key: String, contentType: String?): PresignedUrl

    /**
     * Generates a presigned URL for downloading a file.
     */
    fun generatePresignedDownloadUrl(key: String): PresignedUrl

    /**
     * Checks if a file exists in the bucket.
     */
    fun exists(key: String): Boolean

    /**
     * Gets file metadata from the bucket.
     */
    fun getFileMetadata(key: String): FileMetadata

    /**
     * Deletes a file from the bucket.
     */
    fun delete(key: String): Boolean
}
