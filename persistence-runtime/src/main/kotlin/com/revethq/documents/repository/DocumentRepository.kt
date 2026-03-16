package com.revethq.documents.repository

import com.revethq.documents.domain.Document
import com.revethq.documents.domain.Page
import com.revethq.documents.domain.PageRequest
import java.util.UUID

interface DocumentRepository {
    fun findAll(includeInactive: Boolean = false): List<Document>

    fun findById(id: Long): Document?

    fun findByUuid(uuid: UUID): Document?

    fun findByProjectId(
        projectId: Long,
        includeInactive: Boolean = false,
    ): List<Document>

    fun findByCategoryId(
        categoryId: Long,
        includeInactive: Boolean = false,
    ): List<Document>

    fun save(document: Document): Document

    fun delete(id: Long): Boolean

    fun findAllPaginated(
        pageRequest: PageRequest,
        includeInactive: Boolean = false,
    ): Page<Document>

    fun findByProjectIdPaginated(
        projectId: Long,
        pageRequest: PageRequest,
        includeInactive: Boolean = false,
    ): Page<Document>

    fun findByCategoryIdPaginated(
        categoryId: Long,
        pageRequest: PageRequest,
        includeInactive: Boolean = false,
    ): Page<Document>

    fun findByTagIdsPaginated(
        tagIds: List<Int>,
        pageRequest: PageRequest,
        includeInactive: Boolean = false,
    ): Page<Document>

    fun findByFilters(
        pageRequest: PageRequest,
        includeInactive: Boolean = false,
        name: String? = null,
        projectId: Long? = null,
        categoryId: Long? = null,
        tagIds: List<Int>? = null,
        organizationIds: List<Long>? = null,
    ): Page<Document>
}
