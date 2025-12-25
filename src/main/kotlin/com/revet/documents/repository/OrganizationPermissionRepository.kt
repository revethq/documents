package com.revet.documents.repository

import com.revet.documents.domain.OrganizationPermission
import com.revet.documents.domain.PermissionType
import com.revet.documents.repository.entity.OrganizationEntity
import com.revet.documents.repository.entity.OrganizationPermissionEntity
import com.revet.documents.repository.entity.UserEntity
import com.revet.documents.repository.mapper.OrganizationPermissionMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/**
 * Repository interface for OrganizationPermission persistence operations.
 */
interface OrganizationPermissionRepository {
    fun findAll(): List<com.revet.documents.domain.OrganizationPermission>
    fun findById(id: Long): com.revet.documents.domain.OrganizationPermission?
    fun findByOrganizationId(organizationId: Long): List<com.revet.documents.domain.OrganizationPermission>
    fun findByUserId(userId: Long): List<com.revet.documents.domain.OrganizationPermission>
    fun findByOrganizationAndUser(organizationId: Long, userId: Long): com.revet.documents.domain.OrganizationPermission?
    fun save(permission: com.revet.documents.domain.OrganizationPermission): com.revet.documents.domain.OrganizationPermission
    fun delete(id: Long): Boolean
    fun deleteByOrganizationAndUser(organizationId: Long, userId: Long): Boolean
}

/**
 * Panache-based implementation of OrganizationPermissionRepository.
 */
@ApplicationScoped
class OrganizationPermissionRepositoryImpl : com.revet.documents.repository.OrganizationPermissionRepository {

    override fun findAll(): List<com.revet.documents.domain.OrganizationPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.listAll()
            .map { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.toDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.OrganizationPermission? {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.findById(id)
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.toDomain(it) }
    }

    override fun findByOrganizationId(organizationId: Long): List<com.revet.documents.domain.OrganizationPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.list("organization.id", organizationId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.toDomain(it) }
    }

    override fun findByUserId(userId: Long): List<com.revet.documents.domain.OrganizationPermission> {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.list("user.id", userId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.toDomain(it) }
    }

    override fun findByOrganizationAndUser(organizationId: Long, userId: Long): com.revet.documents.domain.OrganizationPermission? {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.find("organization.id = ?1 and user.id = ?2", organizationId, userId)
            .firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.toDomain(it) }
    }

    @Transactional
    override fun save(permission: com.revet.documents.domain.OrganizationPermission): com.revet.documents.domain.OrganizationPermission {
        val entity = if (permission.isNew()) {
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.toEntity(permission)

            val organization = _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.findById(permission.organizationId)
                ?: throw IllegalArgumentException("Organization with id ${permission.organizationId} not found")
            newEntity.organization = organization

            val user = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.findById(permission.userId)
                ?: throw IllegalArgumentException("User with id ${permission.userId} not found")
            newEntity.user = user

            newEntity.persist()
            newEntity
        } else {
            val existing = _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.findById(permission.id!!)
                ?: throw IllegalArgumentException("OrganizationPermission with id ${permission.id} not found")
            _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.updateEntity(existing, permission)
            existing
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.OrganizationPermissionMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.deleteById(id)
    }

    @Transactional
    override fun deleteByOrganizationAndUser(organizationId: Long, userId: Long): Boolean {
        val count = _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity.delete(
            "organization.id = ?1 and user.id = ?2",
            organizationId,
            userId
        )
        return count > 0
    }
}
