package com.revet.documents.service

import com.revet.documents.domain.ProjectPermission
import com.revet.documents.domain.PermissionType
import com.revet.documents.repository.ProjectPermissionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Service interface for ProjectPermission business logic.
 */
interface ProjectPermissionService {
    fun getAllPermissions(): List<com.revet.documents.domain.ProjectPermission>
    fun getPermissionById(id: Long): com.revet.documents.domain.ProjectPermission?
    fun getPermissionsByProjectId(projectId: Long): List<com.revet.documents.domain.ProjectPermission>
    fun getPermissionsByUserId(userId: Long): List<com.revet.documents.domain.ProjectPermission>
    fun getPermissionByProjectAndUser(projectId: Long, userId: Long): com.revet.documents.domain.ProjectPermission?
    fun grantPermission(projectId: Long, userId: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.ProjectPermission
    fun updatePermission(id: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.ProjectPermission?
    fun revokePermission(id: Long): Boolean
    fun revokePermissionByProjectAndUser(projectId: Long, userId: Long): Boolean
}

/**
 * Implementation of ProjectPermissionService with business logic.
 */
@ApplicationScoped
class ProjectPermissionServiceImpl @Inject constructor(
    private val permissionRepository: com.revet.documents.repository.ProjectPermissionRepository
) : com.revet.documents.service.ProjectPermissionService {

    override fun getAllPermissions(): List<com.revet.documents.domain.ProjectPermission> {
        return permissionRepository.findAll()
    }

    override fun getPermissionById(id: Long): com.revet.documents.domain.ProjectPermission? {
        return permissionRepository.findById(id)
    }

    override fun getPermissionsByProjectId(projectId: Long): List<com.revet.documents.domain.ProjectPermission> {
        return permissionRepository.findByProjectId(projectId)
    }

    override fun getPermissionsByUserId(userId: Long): List<com.revet.documents.domain.ProjectPermission> {
        return permissionRepository.findByUserId(userId)
    }

    override fun getPermissionByProjectAndUser(projectId: Long, userId: Long): com.revet.documents.domain.ProjectPermission? {
        return permissionRepository.findByProjectAndUser(projectId, userId)
    }

    override fun grantPermission(
        projectId: Long,
        userId: Long,
        permission: com.revet.documents.domain.PermissionType
    ): com.revet.documents.domain.ProjectPermission {
        // Check if permission already exists
        val existing = permissionRepository.findByProjectAndUser(projectId, userId)
        if (existing != null) {
            // Update existing permission
            val updated = existing.update(permission)
            return permissionRepository.save(updated)
        }

        // Create new permission
        val newPermission = _root_ide_package_.com.revet.documents.domain.ProjectPermission.create(
            projectId = projectId,
            userId = userId,
            permission = permission
        )

        return permissionRepository.save(newPermission)
    }

    override fun updatePermission(id: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.ProjectPermission? {
        val existing = permissionRepository.findById(id) ?: return null
        val updated = existing.update(permission)
        return permissionRepository.save(updated)
    }

    override fun revokePermission(id: Long): Boolean {
        return permissionRepository.delete(id)
    }

    override fun revokePermissionByProjectAndUser(projectId: Long, userId: Long): Boolean {
        return permissionRepository.deleteByProjectAndUser(projectId, userId)
    }
}
