package com.revet.documents.dto

import com.revet.documents.domain.UploadStatus
import java.time.OffsetDateTime
import java.util.*

/**
 * Data Transfer Object for DocumentVersion.
 */
data class DocumentVersionDTO(
    val uuid: UUID,
    val documentId: Long?,
    val name: String,
    val file: String?,
    val url: String,
    val size: Int,
    val description: String?,
    val mime: String?,
    val userId: Int?,
    val uploadStatus: com.revet.documents.domain.UploadStatus,
    val created: OffsetDateTime,
    val changed: OffsetDateTime,
    val downloadUrl: String? = null
)

data class CreateDocumentVersionRequest(
    val documentId: Long,
    val name: String,
    val url: String = "",
    val size: Int = 0,
    val file: String? = null,
    val description: String? = null,
    val mime: String? = null,
    val userId: Int? = null
)

data class UpdateDocumentVersionRequest(
    val name: String? = null,
    val file: String? = null,
    val url: String? = null,
    val size: Int? = null,
    val description: String? = null,
    val mime: String? = null
)
