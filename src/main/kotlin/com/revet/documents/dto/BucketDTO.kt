package com.revet.documents.dto

import com.revet.documents.domain.StorageProvider
import java.util.*

/**
 * Data Transfer Object for Bucket.
 * Note: secretKey is not included in responses for security.
 */
data class BucketDTO(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val provider: com.revet.documents.domain.StorageProvider,
    val bucketName: String,
    val endpoint: String?,
    val region: String?,
    val presignedUrlDurationMinutes: Int,
    val isActive: Boolean
)

data class CreateBucketRequest(
    val name: String,
    val provider: com.revet.documents.domain.StorageProvider,
    val bucketName: String,
    val accessKey: String,
    val secretKey: String,
    val endpoint: String? = null,
    val region: String? = null,
    val presignedUrlDurationMinutes: Int = 15
)

data class UpdateBucketRequest(
    val name: String? = null,
    val bucketName: String? = null,
    val endpoint: String? = null,
    val region: String? = null,
    val accessKey: String? = null,
    val secretKey: String? = null,
    val presignedUrlDurationMinutes: Int? = null,
    val isActive: Boolean? = null
)
