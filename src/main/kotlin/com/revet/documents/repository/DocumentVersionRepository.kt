package com.revet.documents.repository

import com.revet.documents.domain.DocumentVersion
import com.revet.documents.repository.entity.DocumentEntity
import com.revet.documents.repository.entity.DocumentVersionEntity
import com.revet.documents.repository.entity.UserEntity
import com.revet.documents.repository.mapper.DocumentVersionMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

/**
 * Repository interface for DocumentVersion persistence operations.
 */
interface DocumentVersionRepository {
    fun findAll(): List<com.revet.documents.domain.DocumentVersion>
    fun findByUuid(uuid: UUID): com.revet.documents.domain.DocumentVersion?
    fun findByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentVersion>
    fun findLatestByDocumentId(documentId: Long): com.revet.documents.domain.DocumentVersion?
    fun save(documentVersion: com.revet.documents.domain.DocumentVersion): com.revet.documents.domain.DocumentVersion
    fun delete(uuid: UUID): Boolean
}

/**
 * Panache-based implementation of DocumentVersionRepository.
 */
@ApplicationScoped
class DocumentVersionRepositoryImpl : com.revet.documents.repository.DocumentVersionRepository {

    override fun findAll(): List<com.revet.documents.domain.DocumentVersion> {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentVersionEntity.listAll().map { _root_ide_package_.com.revet.documents.repository.mapper.DocumentVersionMapper.toDomain(it) }
    }

    override fun findByUuid(uuid: UUID): com.revet.documents.domain.DocumentVersion? {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentVersionEntity.find("uuid = ?1", uuid).firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.DocumentVersionMapper.toDomain(it) }
    }

    override fun findByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentVersion> {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentVersionEntity.list("document.id = ?1", documentId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.DocumentVersionMapper.toDomain(it) }
            .sortedByDescending { it.created }
    }

    override fun findLatestByDocumentId(documentId: Long): com.revet.documents.domain.DocumentVersion? {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentVersionEntity.list("document.id = ?1 order by created desc", documentId)
            .firstOrNull()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.DocumentVersionMapper.toDomain(it) }
    }

    @Transactional
    override fun save(documentVersion: com.revet.documents.domain.DocumentVersion): com.revet.documents.domain.DocumentVersion {
        val existing = _root_ide_package_.com.revet.documents.repository.entity.DocumentVersionEntity.find("uuid = ?1", documentVersion.uuid).firstResult()

        val entity = if (existing == null) {
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.DocumentVersionMapper.toEntity(documentVersion)

            // Set document if documentId is provided
            documentVersion.documentId?.let { docId ->
                val document = _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.findById(docId)
                    ?: throw IllegalArgumentException("Document with id $docId not found")
                newEntity.document = document
            }

            // Set user if provided
            documentVersion.userId?.let { userId ->
                val user = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.findById(userId.toLong())
                    ?: throw IllegalArgumentException("User with id $userId not found")
                newEntity.user = user
            }

            newEntity.persist()
            newEntity
        } else {
            _root_ide_package_.com.revet.documents.repository.mapper.DocumentVersionMapper.updateEntity(existing, documentVersion)
            existing
        }

        return _root_ide_package_.com.revet.documents.repository.mapper.DocumentVersionMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(uuid: UUID): Boolean {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentVersionEntity.delete("uuid = ?1", uuid) > 0
    }
}
