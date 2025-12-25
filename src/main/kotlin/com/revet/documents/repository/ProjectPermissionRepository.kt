package com.revet.documents.repository

import com.revet.documents.domain.ProjectPermission
import com.revet.documents.domain.PermissionType
import com.revet.documents.repository.entity.ProjectEntity
import com.revet.documents.repository.entity.ProjectPermissionEntity
import com.revet.documents.repository.entity.UserEntity
import com.revet.documents.repository.mapper.ProjectPermissionMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/**
 * Repository interface for ProjectPermission persistence operations.
 */
interface ProjectPermissionRepository {
    fun findAll(): List<com.revet.documents.domain.ProjectPermission>
    fun findById(id: Long): com.revet.documents.domain.ProjectPermission?
    fun findByProjectId(projectId: Long): List<com.revet.documents.domain.ProjectPermission>
    fun findByUserId(userId: Long): List<com.revet.documents.domain.ProjectPermission>
    fun findByProjectAndUser(projectId: Long, userId: Long): com.revet.documents.domain.ProjectPermission?
    fun save(permission: com.revet.documents.domain.ProjectPermission): com.revet.documents.domain.ProjectPermission
    fun delete(id: Long): Boolean
    fun deleteByProjectAndUser(projectId: Long, userId: Long): Boolean
}

/**
 * Panache-based implementation of ProjectPermissionRepository.
 */
@ApplicationScoped
class ProjectPermissionRepositoryImpl : com.revet.documents.repository.ProjectPermissionRepository {

    override fun findAll(): List<com.revet.documents.domain.ProjectPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.listAll()
            .map { _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.toDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.ProjectPermission? {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.findById(id)
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.toDomain(it) }
    }

    override fun findByProjectId(projectId: Long): List<com.revet.documents.domain.ProjectPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.list("project.id", projectId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.toDomain(it) }
    }

    override fun findByUserId(userId: Long): List<com.revet.documents.domain.ProjectPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.list("user.id", userId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.toDomain(it) }
    }

    override fun findByProjectAndUser(projectId: Long, userId: Long): com.revet.documents.domain.ProjectPermission? {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.find("project.id = ?1 and user.id = ?2", projectId, userId)
            .firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.toDomain(it) }
    }

    @Transactional
    override fun save(permission: com.revet.documents.domain.ProjectPermission): com.revet.documents.domain.ProjectPermission {
        val entity = if (permission.isNew()) {
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.toEntity(permission)

            val project = _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.findById(permission.projectId)
                ?: throw IllegalArgumentException("Project with id ${permission.projectId} not found")
            newEntity.project = project

            val user = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.findById(permission.userId)
                ?: throw IllegalArgumentException("User with id ${permission.userId} not found")
            newEntity.user = user

            newEntity.persist()
            newEntity
        } else {
            val existing = _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.findById(permission.id!!)
                ?: throw IllegalArgumentException("ProjectPermission with id ${permission.id} not found")
            _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.updateEntity(existing, permission)
            existing
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.ProjectPermissionMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.deleteById(id)
    }

    @Transactional
    override fun deleteByProjectAndUser(projectId: Long, userId: Long): Boolean {
        val count = _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity.delete(
            "project.id = ?1 and user.id = ?2",
            projectId,
            userId
        )
        return count > 0
    }
}
