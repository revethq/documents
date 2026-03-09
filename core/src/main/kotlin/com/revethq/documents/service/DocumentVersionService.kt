package com.revethq.documents.service

import com.revethq.documents.domain.DocumentVersion
import java.util.UUID

/**
 * Service interface for DocumentVersion business logic.
 */
interface DocumentVersionService {
    fun getAllVersions(): List<DocumentVersion>

    fun getVersionByUuid(uuid: UUID): DocumentVersion?

    fun getVersionsByDocumentId(documentId: Long): List<DocumentVersion>

    fun getLatestVersionByDocumentId(documentId: Long): DocumentVersion?

    fun getLatestVersionByDocumentUuid(uuid: UUID): DocumentVersion?

    fun createVersion(
        documentId: Long,
        name: String,
        url: String = "",
        size: Int = 0,
        file: String? = null,
        description: String? = null,
        mime: String? = null,
        userId: UUID? = null,
    ): DocumentVersion

    fun updateVersion(
        uuid: UUID,
        name: String? = null,
        file: String? = null,
        url: String? = null,
        size: Int? = null,
        description: String? = null,
        mime: String? = null,
    ): DocumentVersion?

    fun completeUpload(
        uuid: UUID,
        fileSize: Int,
    ): DocumentVersion?

    fun deleteVersion(uuid: UUID): Boolean
}
