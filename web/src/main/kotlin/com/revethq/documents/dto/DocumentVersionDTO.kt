package com.revethq.documents.dto

import com.revethq.documents.domain.UploadStatus
import java.time.OffsetDateTime
import java.util.UUID

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
    val userId: UUID?,
    val uploadStatus: com.revethq.documents.domain.UploadStatus,
    val created: OffsetDateTime,
    val changed: OffsetDateTime,
    val downloadUrl: String? = null,
)

data class CreateDocumentVersionRequest(
    val documentId: Long,
    val name: String,
    val url: String = "",
    val size: Int = 0,
    val file: String? = null,
    val description: String? = null,
    val mime: String? = null,
    val userId: UUID? = null,
)

data class UpdateDocumentVersionRequest(
    val name: String? = null,
    val file: String? = null,
    val url: String? = null,
    val size: Int? = null,
    val description: String? = null,
    val mime: String? = null,
)
