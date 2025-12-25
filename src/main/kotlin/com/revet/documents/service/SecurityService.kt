package com.revet.documents.service

import com.revet.documents.domain.PermissionType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Service for checking user permissions on resources.
 *
 * Implements two-dimensional permission inheritance:
 *
 * 1. Permission Type Hierarchy: CAN_MANAGE > CAN_CREATE > CAN_INVITE
 *    - CAN_MANAGE implies CAN_CREATE and CAN_INVITE
 *    - CAN_CREATE implies CAN_INVITE
 *
 * 2. Resource Hierarchy: Organization > Project > Document
 *    - Permission on Organization grants same permission on all its Projects
 *    - Permission on Project grants same permission on all its Documents
 */
interface SecurityService {
    /**
     * Check if a user has the required permission on a document.
     * Checks document -> project -> organization hierarchy.
     */
    fun canAccessDocument(userId: Long, documentId: Long, requiredPermission: com.revet.documents.domain.PermissionType): Boolean

    /**
     * Check if a user has the required permission on a project.
     * Checks project -> organization hierarchy.
     */
    fun canAccessProject(userId: Long, projectId: Long, requiredPermission: com.revet.documents.domain.PermissionType): Boolean

    /**
     * Check if a user has the required permission on an organization.
     */
    fun canAccessOrganization(userId: Long, organizationId: Long, requiredPermission: com.revet.documents.domain.PermissionType): Boolean

    /**
     * Get the effective permission a user has on a document (considering inheritance).
     * Returns the highest permission level from document, project, or organization.
     */
    fun getEffectiveDocumentPermission(userId: Long, documentId: Long): com.revet.documents.domain.PermissionType?

    /**
     * Get the effective permission a user has on a project (considering inheritance).
     * Returns the highest permission level from project or organization.
     */
    fun getEffectiveProjectPermission(userId: Long, projectId: Long): com.revet.documents.domain.PermissionType?

    /**
     * Get the permission a user has on an organization.
     */
    fun getOrganizationPermission(userId: Long, organizationId: Long): com.revet.documents.domain.PermissionType?
}

@ApplicationScoped
class SecurityServiceImpl @Inject constructor(
    private val documentPermissionService: com.revet.documents.service.DocumentPermissionService,
    private val projectPermissionService: com.revet.documents.service.ProjectPermissionService,
    private val organizationPermissionService: com.revet.documents.service.OrganizationPermissionService,
    private val documentService: com.revet.documents.service.DocumentService,
    private val projectService: com.revet.documents.service.ProjectService
) : com.revet.documents.service.SecurityService {

    override fun canAccessDocument(userId: Long, documentId: Long, requiredPermission: com.revet.documents.domain.PermissionType): Boolean {
        val effectivePermission = getEffectiveDocumentPermission(userId, documentId)
        return effectivePermission?.implies(requiredPermission) ?: false
    }

    override fun canAccessProject(userId: Long, projectId: Long, requiredPermission: com.revet.documents.domain.PermissionType): Boolean {
        val effectivePermission = getEffectiveProjectPermission(userId, projectId)
        return effectivePermission?.implies(requiredPermission) ?: false
    }

    override fun canAccessOrganization(userId: Long, organizationId: Long, requiredPermission: com.revet.documents.domain.PermissionType): Boolean {
        val permission = getOrganizationPermission(userId, organizationId)
        return permission?.implies(requiredPermission) ?: false
    }

    override fun getEffectiveDocumentPermission(userId: Long, documentId: Long): com.revet.documents.domain.PermissionType? {
        // Check document-level permission
        val docPermission = documentPermissionService.getPermissionByDocumentAndUser(documentId, userId)

        // Get the document to find its project
        val document = documentService.getDocumentById(documentId) ?: return docPermission?.permission

        // Check project-level permission
        val projectPermission = getEffectiveProjectPermission(userId, document.projectId)

        // Return the higher of the two permissions
        return highestPermission(docPermission?.permission, projectPermission)
    }

    override fun getEffectiveProjectPermission(userId: Long, projectId: Long): com.revet.documents.domain.PermissionType? {
        // Check project-level permission
        val projectPermission = projectPermissionService.getPermissionByProjectAndUser(projectId, userId)

        // Get the project to find its organization
        val project = projectService.getProjectById(projectId) ?: return projectPermission?.permission

        // Check organization-level permission
        val orgPermission = getOrganizationPermission(userId, project.organizationId)

        // Return the higher of the two permissions
        return highestPermission(projectPermission?.permission, orgPermission)
    }

    override fun getOrganizationPermission(userId: Long, organizationId: Long): com.revet.documents.domain.PermissionType? {
        return organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId)?.permission
    }

    /**
     * Returns the higher of two permission types, or the non-null one if only one exists.
     */
    private fun highestPermission(a: com.revet.documents.domain.PermissionType?, b: com.revet.documents.domain.PermissionType?): com.revet.documents.domain.PermissionType? {
        return when {
            a == null -> b
            b == null -> a
            a.ordinal >= b.ordinal -> a
            else -> b
        }
    }
}
