package com.revet.documents.domain

import java.time.OffsetDateTime
import java.util.*

/**
 * Core domain model for DocumentVersion.
 * Represents a specific version of a document with file information.
 */
data class DocumentVersion(
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
    val changed: OffsetDateTime
) {
    companion object {
        fun create(
            documentId: Long?,
            name: String,
            url: String = "",
            size: Int = 0,
            file: String? = null,
            description: String? = null,
            mime: String? = null,
            userId: Int? = null,
            uploadStatus: com.revet.documents.domain.UploadStatus = _root_ide_package_.com.revet.documents.domain.UploadStatus.PENDING
        ): DocumentVersion {
            val now = OffsetDateTime.now()
            return DocumentVersion(
                uuid = UUID.randomUUID(),
                documentId = documentId,
                name = name,
                file = file,
                url = url,
                size = size,
                description = description,
                mime = mime,
                userId = userId,
                uploadStatus = uploadStatus,
                created = now,
                changed = now
            )
        }
    }

    fun update(
        name: String? = null,
        file: String? = null,
        url: String? = null,
        size: Int? = null,
        description: String? = null,
        mime: String? = null,
        uploadStatus: com.revet.documents.domain.UploadStatus? = null
    ): DocumentVersion {
        return this.copy(
            name = name ?: this.name,
            file = file ?: this.file,
            url = url ?: this.url,
            size = size ?: this.size,
            description = description ?: this.description,
            mime = mime ?: this.mime,
            uploadStatus = uploadStatus ?: this.uploadStatus,
            changed = OffsetDateTime.now()
        )
    }
}
