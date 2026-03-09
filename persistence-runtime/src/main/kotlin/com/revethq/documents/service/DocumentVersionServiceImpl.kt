package com.revethq.documents.service

import com.revethq.documents.domain.DocumentVersion
import com.revethq.documents.domain.UploadStatus
import com.revethq.documents.repository.DocumentRepository
import com.revethq.documents.repository.DocumentVersionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

/**
 * Implementation of DocumentVersionService with business logic.
 */
@ApplicationScoped
class DocumentVersionServiceImpl
    @Inject
    constructor(
        private val documentVersionRepository: DocumentVersionRepository,
        private val documentRepository: DocumentRepository,
    ) : DocumentVersionService {
        override fun getAllVersions(): List<DocumentVersion> = documentVersionRepository.findAll()

        override fun getVersionByUuid(uuid: UUID): DocumentVersion? = documentVersionRepository.findByUuid(uuid)

        override fun getVersionsByDocumentId(documentId: Long): List<DocumentVersion> =
            documentVersionRepository.findByDocumentId(documentId)

        override fun getLatestVersionByDocumentId(documentId: Long): DocumentVersion? =
            documentVersionRepository.findLatestByDocumentId(documentId)

        override fun getLatestVersionByDocumentUuid(uuid: UUID): DocumentVersion? {
            val document = documentRepository.findByUuid(uuid) ?: return null
            return documentVersionRepository.findLatestByDocumentId(document.id!!)
        }

        override fun createVersion(
            documentId: Long,
            name: String,
            url: String,
            size: Int,
            file: String?,
            description: String?,
            mime: String?,
            userId: UUID?,
        ): DocumentVersion {
            require(name.isNotBlank()) { "Version name cannot be blank" }

            val version =
                DocumentVersion.create(
                    documentId = documentId,
                    name = name,
                    url = url,
                    size = size,
                    file = file,
                    description = description,
                    mime = mime,
                    userId = userId,
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
            mime: String?,
        ): DocumentVersion? {
            val existing = documentVersionRepository.findByUuid(uuid) ?: return null

            val updated =
                existing.update(
                    name = name,
                    file = file,
                    url = url,
                    size = size,
                    description = description,
                    mime = mime,
                )

            return documentVersionRepository.save(updated)
        }

        override fun completeUpload(
            uuid: UUID,
            fileSize: Int,
        ): DocumentVersion? {
            val existing = documentVersionRepository.findByUuid(uuid) ?: return null

            val updated =
                existing.update(
                    size = fileSize,
                    uploadStatus = UploadStatus.COMPLETED,
                )

            return documentVersionRepository.save(updated)
        }

        override fun deleteVersion(uuid: UUID): Boolean = documentVersionRepository.delete(uuid)
    }
