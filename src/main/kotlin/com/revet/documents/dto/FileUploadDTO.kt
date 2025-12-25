package com.revet.documents.dto

import java.util.UUID

/**
 * Response containing download URL for a document version.
 */
data class DownloadResponse(
    val downloadUrl: String,
    val fileName: String
)

/**
 * Response containing presigned URL for download.
 */
data class PresignedDownloadResponse(
    val downloadUrl: String,
    val expiresInMinutes: Int,
    val fileName: String
)

/**
 * Request to initiate a file upload.
 */
data class InitiateUploadRequest(
    val documentUuid: UUID,
    val fileName: String,
    val contentType: String? = null
)

/**
 * Response containing presigned upload URL and version info.
 */
data class InitiateUploadResponse(
    val uploadUrl: String,
    val s3Key: String,
    val documentVersionUuid: UUID,
    val expiresInMinutes: Int
)
