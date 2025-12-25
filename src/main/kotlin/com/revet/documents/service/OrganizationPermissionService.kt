package com.revet.documents.service

import com.revet.documents.domain.OrganizationPermission
import com.revet.documents.domain.PermissionType
import com.revet.documents.repository.OrganizationPermissionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Service interface for OrganizationPermission business logic.
 */
interface OrganizationPermissionService {
    fun getAllPermissions(): List<com.revet.documents.domain.OrganizationPermission>
    fun getPermissionById(id: Long): com.revet.documents.domain.OrganizationPermission?
    fun getPermissionsByOrganizationId(organizationId: Long): List<com.revet.documents.domain.OrganizationPermission>
    fun getPermissionsByUserId(userId: Long): List<com.revet.documents.domain.OrganizationPermission>
    fun getPermissionByOrganizationAndUser(organizationId: Long, userId: Long): com.revet.documents.domain.OrganizationPermission?
    fun grantPermission(organizationId: Long, userId: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.OrganizationPermission
    fun updatePermission(id: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.OrganizationPermission?
    fun revokePermission(id: Long): Boolean
    fun revokePermissionByOrganizationAndUser(organizationId: Long, userId: Long): Boolean
}

/**
 * Implementation of OrganizationPermissionService with business logic.
 */
@ApplicationScoped
class OrganizationPermissionServiceImpl @Inject constructor(
    private val permissionRepository: com.revet.documents.repository.OrganizationPermissionRepository
) : com.revet.documents.service.OrganizationPermissionService {

    override fun getAllPermissions(): List<com.revet.documents.domain.OrganizationPermission> {
        return permissionRepository.findAll()
    }

    override fun getPermissionById(id: Long): com.revet.documents.domain.OrganizationPermission? {
        return permissionRepository.findById(id)
    }

    override fun getPermissionsByOrganizationId(organizationId: Long): List<com.revet.documents.domain.OrganizationPermission> {
        return permissionRepository.findByOrganizationId(organizationId)
    }

    override fun getPermissionsByUserId(userId: Long): List<com.revet.documents.domain.OrganizationPermission> {
        return permissionRepository.findByUserId(userId)
    }

    override fun getPermissionByOrganizationAndUser(organizationId: Long, userId: Long): com.revet.documents.domain.OrganizationPermission? {
        return permissionRepository.findByOrganizationAndUser(organizationId, userId)
    }

    override fun grantPermission(
        organizationId: Long,
        userId: Long,
        permission: com.revet.documents.domain.PermissionType
    ): com.revet.documents.domain.OrganizationPermission {
        // Check if permission already exists
        val existing = permissionRepository.findByOrganizationAndUser(organizationId, userId)
        if (existing != null) {
            // Update existing permission
            val updated = existing.update(permission)
            return permissionRepository.save(updated)
        }

        // Create new permission
        val newPermission = _root_ide_package_.com.revet.documents.domain.OrganizationPermission.create(
            organizationId = organizationId,
            userId = userId,
            permission = permission
        )

        return permissionRepository.save(newPermission)
    }

    override fun updatePermission(id: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.OrganizationPermission? {
        val existing = permissionRepository.findById(id) ?: return null
        val updated = existing.update(permission)
        return permissionRepository.save(updated)
    }

    override fun revokePermission(id: Long): Boolean {
        return permissionRepository.delete(id)
    }

    override fun revokePermissionByOrganizationAndUser(organizationId: Long, userId: Long): Boolean {
        return permissionRepository.deleteByOrganizationAndUser(organizationId, userId)
    }
}
