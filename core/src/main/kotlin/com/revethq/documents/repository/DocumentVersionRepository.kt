package com.revethq.documents.repository

import com.revethq.documents.domain.DocumentVersion
import java.util.UUID

interface DocumentVersionRepository {
    fun findAll(): List<DocumentVersion>

    fun findByUuid(uuid: UUID): DocumentVersion?

    fun findByDocumentId(documentId: Long): List<DocumentVersion>

    fun findLatestByDocumentId(documentId: Long): DocumentVersion?

    fun save(documentVersion: DocumentVersion): DocumentVersion

    fun delete(uuid: UUID): Boolean
}
