package com.revethq.documents.service

import com.revethq.documents.domain.Document
import com.revethq.documents.domain.Page
import com.revethq.documents.domain.PageRequest
import java.util.UUID

/**
 * Service interface for Document business logic.
 */
interface DocumentService {
    fun getAllDocuments(includeInactive: Boolean = false): List<Document>

    fun getDocumentById(id: Long): Document?

    fun getDocumentByUuid(uuid: UUID): Document?

    fun getDocumentsByProjectId(
        projectId: Long,
        includeInactive: Boolean = false,
    ): List<Document>

    fun getDocumentsByCategoryId(
        categoryId: Long,
        includeInactive: Boolean = false,
    ): List<Document>

    fun createDocument(
        name: String,
        projectId: Long,
        categoryId: Long? = null,
        mime: String? = null,
        tags: Set<String> = emptySet(),
    ): Document

    fun updateDocument(
        id: Long,
        name: String? = null,
        categoryId: Long? = null,
        mime: String? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null,
    ): Document?

    fun updateDocumentByUuid(
        uuid: UUID,
        name: String? = null,
        categoryId: Long? = null,
        mime: String? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null,
    ): Document?

    fun addTagToDocument(
        documentId: Long,
        tag: String,
    ): Document?

    fun addTagToDocumentByUuid(
        uuid: UUID,
        tag: String,
    ): Document?

    fun removeTagFromDocument(
        documentId: Long,
        tag: String,
    ): Document?

    fun removeTagFromDocumentByUuid(
        uuid: UUID,
        tag: String,
    ): Document?

    fun deleteDocument(id: Long): Boolean

    fun deleteDocumentByUuid(uuid: UUID): Boolean

    fun getDocumentsPaginated(
        pageRequest: PageRequest,
        includeInactive: Boolean = false,
        name: String? = null,
        projectId: Long? = null,
        categoryId: Long? = null,
        tagIds: List<Int>? = null,
        organizationIds: List<Long>? = null,
    ): Page<Document>
}
