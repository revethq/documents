package com.revet.documents.service

import com.revet.documents.domain.PermissionType
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID

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
    // ===== UUID-based methods (preferred for API layer) =====

    /**
     * Check if a user has the required permission on a document by UUID.
     * Checks document -> project -> organization hierarchy.
     */
    fun canAccessDocumentByUuid(userId: Long, documentUuid: UUID, requiredPermission: PermissionType): Boolean

    /**
     * Check if a user has the required permission on a project by UUID.
     * Checks project -> organization hierarchy.
     */
    fun canAccessProjectByUuid(userId: Long, projectUuid: UUID, requiredPermission: PermissionType): Boolean

    /**
     * Check if a user has the required permission on an organization by UUID.
     */
    fun canAccessOrganizationByUuid(userId: Long, organizationUuid: UUID, requiredPermission: PermissionType): Boolean

    // ===== Long ID-based methods (for internal use) =====

    /**
     * Check if a user has the required permission on a document.
     * Checks document -> project -> organization hierarchy.
     */
    fun canAccessDocument(userId: Long, documentId: Long, requiredPermission: PermissionType): Boolean

    /**
     * Check if a user has the required permission on a project.
     * Checks project -> organization hierarchy.
     */
    fun canAccessProject(userId: Long, projectId: Long, requiredPermission: PermissionType): Boolean

    /**
     * Check if a user has the required permission on an organization.
     */
    fun canAccessOrganization(userId: Long, organizationId: Long, requiredPermission: PermissionType): Boolean

    /**
     * Get the effective permission a user has on a document (considering inheritance).
     * Returns the highest permission level from document, project, or organization.
     */
    fun getEffectiveDocumentPermission(userId: Long, documentId: Long): PermissionType?

    /**
     * Get the effective permission a user has on a project (considering inheritance).
     * Returns the highest permission level from project or organization.
     */
    fun getEffectiveProjectPermission(userId: Long, projectId: Long): PermissionType?

    /**
     * Get the permission a user has on an organization.
     */
    fun getOrganizationPermission(userId: Long, organizationId: Long): PermissionType?
}

@ApplicationScoped
class SecurityServiceImpl @Inject constructor(
    private val documentPermissionService: DocumentPermissionService,
    private val projectPermissionService: ProjectPermissionService,
    private val organizationPermissionService: OrganizationPermissionService,
    private val documentService: DocumentService,
    private val projectService: ProjectService,
    private val organizationService: OrganizationService
) : SecurityService {

    // ===== UUID-based methods =====

    override fun canAccessDocumentByUuid(userId: Long, documentUuid: UUID, requiredPermission: PermissionType): Boolean {
        val document = documentService.getDocumentByUuid(documentUuid) ?: return false
        return canAccessDocument(userId, document.id!!, requiredPermission)
    }

    override fun canAccessProjectByUuid(userId: Long, projectUuid: UUID, requiredPermission: PermissionType): Boolean {
        val project = projectService.getProjectByUuid(projectUuid) ?: return false
        return canAccessProject(userId, project.id!!, requiredPermission)
    }

    override fun canAccessOrganizationByUuid(userId: Long, organizationUuid: UUID, requiredPermission: PermissionType): Boolean {
        val organization = organizationService.getOrganizationByUuid(organizationUuid) ?: return false
        return canAccessOrganization(userId, organization.id!!, requiredPermission)
    }

    // ===== Long ID-based methods =====

    override fun canAccessDocument(userId: Long, documentId: Long, requiredPermission: PermissionType): Boolean {
        val effectivePermission = getEffectiveDocumentPermission(userId, documentId)
        return effectivePermission?.implies(requiredPermission) ?: false
    }

    override fun canAccessProject(userId: Long, projectId: Long, requiredPermission: PermissionType): Boolean {
        val effectivePermission = getEffectiveProjectPermission(userId, projectId)
        return effectivePermission?.implies(requiredPermission) ?: false
    }

    override fun canAccessOrganization(userId: Long, organizationId: Long, requiredPermission: PermissionType): Boolean {
        val permission = getOrganizationPermission(userId, organizationId)
        return permission?.implies(requiredPermission) ?: false
    }

    override fun getEffectiveDocumentPermission(userId: Long, documentId: Long): PermissionType? {
        // Check document-level permission
        val docPermission = documentPermissionService.getPermissionByDocumentAndUser(documentId, userId)

        // Get the document to find its project
        val document = documentService.getDocumentById(documentId) ?: return docPermission?.permission

        // Check project-level permission
        val projectPermission = getEffectiveProjectPermission(userId, document.projectId)

        // Return the higher of the two permissions
        return highestPermission(docPermission?.permission, projectPermission)
    }

    override fun getEffectiveProjectPermission(userId: Long, projectId: Long): PermissionType? {
        // Check project-level permission
        val projectPermission = projectPermissionService.getPermissionByProjectAndUser(projectId, userId)

        // Get the project to find its organization
        val project = projectService.getProjectById(projectId) ?: return projectPermission?.permission

        // Check organization-level permission
        val orgPermission = getOrganizationPermission(userId, project.organizationId)

        // Return the higher of the two permissions
        return highestPermission(projectPermission?.permission, orgPermission)
    }

    override fun getOrganizationPermission(userId: Long, organizationId: Long): PermissionType? {
        return organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId)?.permission
    }

    /**
     * Returns the higher of two permission types, or the non-null one if only one exists.
     */
    private fun highestPermission(a: PermissionType?, b: PermissionType?): PermissionType? {
        return when {
            a == null -> b
            b == null -> a
            a.ordinal >= b.ordinal -> a
            else -> b
        }
    }
}
