package com.revethq.documents.repository

import com.revethq.documents.domain.DocumentVersion
import com.revethq.documents.repository.entity.DocumentEntity
import com.revethq.documents.repository.entity.DocumentVersionEntity
import com.revethq.documents.repository.mapper.DocumentVersionMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.UUID

/**
 * Panache-based implementation of DocumentVersionRepository.
 */
@ApplicationScoped
class DocumentVersionRepositoryImpl : DocumentVersionRepository {
    override fun findAll(): List<DocumentVersion> =
        DocumentVersionEntity.listAll().map {
            DocumentVersionMapper.toDomain(it)
        }

    override fun findByUuid(uuid: UUID): DocumentVersion? =
        DocumentVersionEntity
            .find("uuid = ?1", uuid)
            .firstResult()
            ?.let { DocumentVersionMapper.toDomain(it) }

    override fun findByDocumentId(documentId: Long): List<DocumentVersion> =
        DocumentVersionEntity
            .list("document.id = ?1", documentId)
            .map {
                DocumentVersionMapper.toDomain(it)
            }.sortedByDescending { it.created }

    override fun findLatestByDocumentId(documentId: Long): DocumentVersion? =
        DocumentVersionEntity
            .list(
                "document.id = ?1 order by created desc",
                documentId,
            ).firstOrNull()
            ?.let { DocumentVersionMapper.toDomain(it) }

    @Transactional
    override fun save(documentVersion: DocumentVersion): DocumentVersion {
        val existing =
            DocumentVersionEntity
                .find(
                    "uuid = ?1",
                    documentVersion.uuid,
                ).firstResult()

        val entity =
            if (existing == null) {
                val newEntity = DocumentVersionMapper.toEntity(documentVersion)

                // Set document if documentId is provided
                documentVersion.documentId?.let { docId ->
                    val document =
                        DocumentEntity
                            .findById(docId)
                            ?: throw IllegalArgumentException("Document with id $docId not found")
                    newEntity.document = document
                }

                newEntity.persist()
                newEntity
            } else {
                DocumentVersionMapper.updateEntity(existing, documentVersion)
                existing
            }

        return DocumentVersionMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(uuid: UUID): Boolean = DocumentVersionEntity.delete("uuid = ?1", uuid) > 0
}
