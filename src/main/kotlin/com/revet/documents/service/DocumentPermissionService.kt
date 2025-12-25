package com.revet.documents.service

import com.revet.documents.domain.DocumentPermission
import com.revet.documents.domain.PermissionType
import com.revet.documents.repository.DocumentPermissionRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Service interface for DocumentPermission business logic.
 */
interface DocumentPermissionService {
    fun getAllPermissions(): List<com.revet.documents.domain.DocumentPermission>
    fun getPermissionById(id: Long): com.revet.documents.domain.DocumentPermission?
    fun getPermissionsByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentPermission>
    fun getPermissionsByUserId(userId: Long): List<com.revet.documents.domain.DocumentPermission>
    fun getPermissionByDocumentAndUser(documentId: Long, userId: Long): com.revet.documents.domain.DocumentPermission?
    fun grantPermission(documentId: Long, userId: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.DocumentPermission
    fun updatePermission(id: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.DocumentPermission?
    fun revokePermission(id: Long): Boolean
    fun revokePermissionByDocumentAndUser(documentId: Long, userId: Long): Boolean
}

/**
 * Implementation of DocumentPermissionService with business logic.
 */
@ApplicationScoped
class DocumentPermissionServiceImpl @Inject constructor(
    private val permissionRepository: com.revet.documents.repository.DocumentPermissionRepository
) : com.revet.documents.service.DocumentPermissionService {

    override fun getAllPermissions(): List<com.revet.documents.domain.DocumentPermission> {
        return permissionRepository.findAll()
    }

    override fun getPermissionById(id: Long): com.revet.documents.domain.DocumentPermission? {
        return permissionRepository.findById(id)
    }

    override fun getPermissionsByDocumentId(documentId: Long): List<com.revet.documents.domain.DocumentPermission> {
        return permissionRepository.findByDocumentId(documentId)
    }

    override fun getPermissionsByUserId(userId: Long): List<com.revet.documents.domain.DocumentPermission> {
        return permissionRepository.findByUserId(userId)
    }

    override fun getPermissionByDocumentAndUser(documentId: Long, userId: Long): com.revet.documents.domain.DocumentPermission? {
        return permissionRepository.findByDocumentAndUser(documentId, userId)
    }

    override fun grantPermission(
        documentId: Long,
        userId: Long,
        permission: com.revet.documents.domain.PermissionType
    ): com.revet.documents.domain.DocumentPermission {
        // Check if permission already exists
        val existing = permissionRepository.findByDocumentAndUser(documentId, userId)
        if (existing != null) {
            // Update existing permission
            val updated = existing.update(permission)
            return permissionRepository.save(updated)
        }

        // Create new permission
        val newPermission = _root_ide_package_.com.revet.documents.domain.DocumentPermission.create(
            documentId = documentId,
            userId = userId,
            permission = permission
        )

        return permissionRepository.save(newPermission)
    }

    override fun updatePermission(id: Long, permission: com.revet.documents.domain.PermissionType): com.revet.documents.domain.DocumentPermission? {
        val existing = permissionRepository.findById(id) ?: return null
        val updated = existing.update(permission)
        return permissionRepository.save(updated)
    }

    override fun revokePermission(id: Long): Boolean {
        return permissionRepository.delete(id)
    }

    override fun revokePermissionByDocumentAndUser(documentId: Long, userId: Long): Boolean {
        return permissionRepository.deleteByDocumentAndUser(documentId, userId)
    }
}
