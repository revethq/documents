package com.revethq.documents.dto

import java.time.LocalDateTime
import java.util.UUID

/**
 * Data Transfer Object for Document.
 */
data class DocumentDTO(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val projectId: Long,
    val categoryId: Long?,
    val mime: String?,
    val date: LocalDateTime,
    val tags: Set<String>,
    val isActive: Boolean,
)

data class CreateDocumentRequest(
    val name: String,
    val projectId: Long,
    val categoryId: Long? = null,
    val mime: String? = null,
    val tags: Set<String> = emptySet(),
)

data class UpdateDocumentRequest(
    val name: String? = null,
    val categoryId: Long? = null,
    val mime: String? = null,
    val tags: Set<String>? = null,
    val isActive: Boolean? = null,
)
