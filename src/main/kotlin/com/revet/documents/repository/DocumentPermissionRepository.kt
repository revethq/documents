package com.revet.documents.repository

import com.revet.documents.domain.DocumentPermission
import com.revet.documents.domain.PermissionType
import com.revet.documents.repository.entity.DocumentEntity
import com.revet.documents.repository.entity.DocumentPermissionEntity
import com.revet.documents.repository.entity.UserEntity
import com.revet.documents.repository.mapper.DocumentPermissionMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/**
 * Repository interface for DocumentPermission persistence operations.
 */
interface DocumentPermissionRepository {
    fun findAll(): List<com.revet.documents.domain.DocumentPermission>
    fun findById(id: Long): com.revet.documents.domain.DocumentPermission?
    fun findByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentPermission>
    fun findByUserId(userId: Long): List<com.revet.documents.domain.DocumentPermission>
    fun findByDocumentAndUser(documentId: Long, userId: Long): com.revet.documents.domain.DocumentPermission?
    fun save(permission: com.revet.documents.domain.DocumentPermission): com.revet.documents.domain.DocumentPermission
    fun delete(id: Long): Boolean
    fun deleteByDocumentAndUser(documentId: Long, userId: Long): Boolean
}

/**
 * Panache-based implementation of DocumentPermissionRepository.
 */
@ApplicationScoped
class DocumentPermissionRepositoryImpl : com.revet.documents.repository.DocumentPermissionRepository {

    override fun findAll(): List<com.revet.documents.domain.DocumentPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.listAll()
            .map { _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.toDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.DocumentPermission? {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.findById(id)
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.toDomain(it) }
    }

    override fun findByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.list("document.id", documentId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.toDomain(it) }
    }

    override fun findByUserId(userId: Long): List<com.revet.documents.domain.DocumentPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.list("user.id", userId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.toDomain(it) }
    }

    override fun findByDocumentAndUser(documentId: Long, userId: Long): com.revet.documents.domain.DocumentPermission? {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.find("document.id = ?1 and user.id = ?2", documentId, userId)
            .firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.toDomain(it) }
    }

    @Transactional
    override fun save(permission: com.revet.documents.domain.DocumentPermission): com.revet.documents.domain.DocumentPermission {
        val entity = if (permission.isNew()) {
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.toEntity(permission)

            val document = _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.findById(permission.documentId)
                ?: throw IllegalArgumentException("Document with id ${permission.documentId} not found")
            newEntity.document = document

            val user = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.findById(permission.userId)
                ?: throw IllegalArgumentException("User with id ${permission.userId} not found")
            newEntity.user = user

            newEntity.persist()
            newEntity
        } else {
            val existing = _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.findById(permission.id!!)
                ?: throw IllegalArgumentException("DocumentPermission with id ${permission.id} not found")
            _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.updateEntity(existing, permission)
            existing
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.DocumentPermissionMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.deleteById(id)
    }

    @Transactional
    override fun deleteByDocumentAndUser(documentId: Long, userId: Long): Boolean {
        val count = _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity.delete(
            "document.id = ?1 and user.id = ?2",
            documentId,
            userId
        )
        return count > 0
    }
}
