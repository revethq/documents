package com.revet.documents.domain

/**
 * Upload status for document versions.
 */
enum class UploadStatus {
    PENDING,    // Pre-signed URL generated, waiting for upload
    COMPLETED,  // File successfully uploaded and verified in S3
    FAILED      // Upload failed or verification failed
}
