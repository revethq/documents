package com.revet.documents.service

import com.revet.documents.domain.DocumentVersion
import com.revet.documents.domain.UploadStatus
import com.revet.documents.repository.DocumentVersionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Service interface for DocumentVersion business logic.
 */
interface DocumentVersionService {
    fun getAllVersions(): List<com.revet.documents.domain.DocumentVersion>
    fun getVersionByUuid(uuid: UUID): com.revet.documents.domain.DocumentVersion?
    fun getVersionsByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentVersion>
    fun getLatestVersionByDocumentId(documentId: Long): com.revet.documents.domain.DocumentVersion?
    fun createVersion(
        documentId: Long,
        name: String,
        url: String = "",
        size: Int = 0,
        file: String? = null,
        description: String? = null,
        mime: String? = null,
        userId: Int? = null
    ): com.revet.documents.domain.DocumentVersion
    fun updateVersion(
        uuid: UUID,
        name: String? = null,
        file: String? = null,
        url: String? = null,
        size: Int? = null,
        description: String? = null,
        mime: String? = null
    ): com.revet.documents.domain.DocumentVersion?
    fun completeUpload(uuid: UUID, fileSize: Int): com.revet.documents.domain.DocumentVersion?
    fun deleteVersion(uuid: UUID): Boolean
}

/**
 * Implementation of DocumentVersionService with business logic.
 */
@ApplicationScoped
class DocumentVersionServiceImpl @Inject constructor(
    private val documentVersionRepository: com.revet.documents.repository.DocumentVersionRepository
) : com.revet.documents.service.DocumentVersionService {

    override fun getAllVersions(): List<com.revet.documents.domain.DocumentVersion> {
        return documentVersionRepository.findAll()
    }

    override fun getVersionByUuid(uuid: UUID): com.revet.documents.domain.DocumentVersion? {
        return documentVersionRepository.findByUuid(uuid)
    }

    override fun getVersionsByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentVersion> {
        return documentVersionRepository.findByDocumentId(documentId)
    }

    override fun getLatestVersionByDocumentId(documentId: Long): com.revet.documents.domain.DocumentVersion? {
        return documentVersionRepository.findLatestByDocumentId(documentId)
    }

    override fun createVersion(
        documentId: Long,
        name: String,
        url: String,
        size: Int,
        file: String?,
        description: String?,
        mime: String?,
        userId: Int?
    ): com.revet.documents.domain.DocumentVersion {
        require(name.isNotBlank()) { "Version name cannot be blank" }

        val version = _root_ide_package_.com.revet.documents.domain.DocumentVersion.create(
            documentId = documentId,
            name = name,
            url = url,
            size = size,
            file = file,
            description = description,
            mime = mime,
            userId = userId
        )

        return documentVersionRepository.save(version)
    }

    override fun updateVersion(
        uuid: UUID,
        name: String?,
        file: String?,
        url: String?,
        size: Int?,
        description: String?,
        mime: String?
    ): com.revet.documents.domain.DocumentVersion? {
        val existing = documentVersionRepository.findByUuid(uuid) ?: return null

        val updated = existing.update(
            name = name,
            file = file,
            url = url,
            size = size,
            description = description,
            mime = mime
        )

        return documentVersionRepository.save(updated)
    }

    override fun completeUpload(uuid: UUID, fileSize: Int): com.revet.documents.domain.DocumentVersion? {
        val existing = documentVersionRepository.findByUuid(uuid) ?: return null

        val updated = existing.update(
            size = fileSize,
            uploadStatus = _root_ide_package_.com.revet.documents.domain.UploadStatus.COMPLETED
        )

        return documentVersionRepository.save(updated)
    }

    override fun deleteVersion(uuid: UUID): Boolean {
        return documentVersionRepository.delete(uuid)
    }
}
