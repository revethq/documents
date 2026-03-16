package com.revethq.documents.permission

import com.revethq.buckets.permission.BucketsUrn
import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.UUID
import com.revethq.buckets.permission.Actions as BucketActions
import com.revethq.core.permission.Actions as CoreActions
import com.revethq.iam.permission.discovery.Actions as IamActions

/**
 * Prebuilt policies for common roles in the Documents service.
 *
 * These policies can be used as templates or directly attached to users/groups.
 */
@ApplicationScoped
class PrebuiltPolicies {
    @Inject
    lateinit var urn: DocumentsUrn

    companion object {
        const val POLICY_VERSION = "2026-01-01"
    }

    // ============================================================
    // Global Admin Policies
    // ============================================================

    /**
     * Full administrator access to all resources across all tenants.
     * Use with caution - this grants complete control.
     */
    fun globalAdminPolicy(): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsGlobalAdminPolicy",
            description = "Full administrative access to all Documents service resources across all tenants",
            version = POLICY_VERSION,
            statements =
                listOf(
                    Statement(
                        sid = "AllowAllActions",
                        effect = Effect.ALLOW,
                        actions = listOf(Actions.ALL_ACTIONS),
                        resources = listOf(urn.wildcardAll()),
                    ),
                    Statement(
                        sid = "AllowGroupManagement",
                        effect = Effect.ALLOW,
                        actions = IamActions.Group.ALL,
                        resources = listOf("urn:${urn.namespace}:iam:*:group/*"),
                    ),
                ),
        )

    // ============================================================
    // Tenant-Scoped Admin Policies
    // ============================================================

    /**
     * Full administrator access within a specific tenant.
     */
    fun tenantAdminPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsTenantAdminPolicy",
            description = "Full administrative access to all Documents service resources within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowAllTenantActions",
                        effect = Effect.ALLOW,
                        actions = listOf(Actions.ALL_ACTIONS),
                        resources =
                            listOf(
                                urn.wildcard(tenantId, "*"),
                            ),
                    ),
                    Statement(
                        sid = "AllowGroupManagement",
                        effect = Effect.ALLOW,
                        actions = IamActions.Group.ALL,
                        resources = listOf("urn:${urn.namespace}:iam:$tenantId:group/*"),
                    ),
                ),
        )

    // ============================================================
    // Organization-Level Policies
    // ============================================================

    /**
     * Organization manager - can manage all aspects of organizations within a tenant.
     */
    fun organizationManagerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsOrganizationManagerPolicy",
            description = "Full management access to organizations within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowOrganizationManagement",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Organization.ALL,
                        resources = listOf(urn.organizationWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Organization viewer - read-only access to organizations.
     */
    fun organizationViewerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsOrganizationViewerPolicy",
            description = "Read-only access to organizations within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowOrganizationRead",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Organization.READ_ONLY,
                        resources = listOf(urn.organizationWildcard(tenantId)),
                    ),
                ),
        )

    // ============================================================
    // Project-Level Policies
    // ============================================================

    /**
     * Project manager - can manage all aspects of projects within a tenant.
     */
    fun projectManagerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsProjectManagerPolicy",
            description = "Full management access to projects within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowProjectManagement",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.ALL,
                        resources = listOf(urn.projectWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Project editor - can create and edit projects but not delete or manage permissions.
     */
    fun projectEditorPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsProjectEditorPolicy",
            description = "Create and edit access to projects within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowProjectReadWrite",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.READ_ONLY + CoreActions.Project.WRITE,
                        resources = listOf(urn.projectWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Project viewer - read-only access to projects.
     */
    fun projectViewerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsProjectViewerPolicy",
            description = "Read-only access to projects within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowProjectRead",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.READ_ONLY,
                        resources = listOf(urn.projectWildcard(tenantId)),
                    ),
                ),
        )

    // ============================================================
    // Document-Level Policies
    // ============================================================

    /**
     * Document manager - full control over documents and versions.
     */
    fun documentManagerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsDocumentManagerPolicy",
            description = "Full management access to documents within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowDocumentManagement",
                        effect = Effect.ALLOW,
                        actions = Actions.Document.ALL,
                        resources = listOf(urn.documentWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowDocumentVersionManagement",
                        effect = Effect.ALLOW,
                        actions = Actions.DocumentVersion.ALL,
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowFileOperations",
                        effect = Effect.ALLOW,
                        actions = Actions.FileUpload.ALL,
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Document editor - can create, edit, and upload documents but not delete or manage permissions.
     */
    fun documentEditorPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsDocumentEditorPolicy",
            description = "Create and edit access to documents within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowDocumentReadWrite",
                        effect = Effect.ALLOW,
                        actions = Actions.Document.READ_ONLY + Actions.Document.WRITE,
                        resources = listOf(urn.documentWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowDocumentVersionReadWrite",
                        effect = Effect.ALLOW,
                        actions = Actions.DocumentVersion.READ_ONLY + Actions.DocumentVersion.WRITE,
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowFileUpload",
                        effect = Effect.ALLOW,
                        actions = Actions.FileUpload.ALL,
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Document viewer - read-only access to documents and download capability.
     */
    fun documentViewerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsDocumentViewerPolicy",
            description = "Read-only and download access to documents within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowDocumentRead",
                        effect = Effect.ALLOW,
                        actions = Actions.Document.READ_ONLY,
                        resources = listOf(urn.documentWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowDocumentVersionRead",
                        effect = Effect.ALLOW,
                        actions = Actions.DocumentVersion.READ_ONLY,
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowDownload",
                        effect = Effect.ALLOW,
                        actions = listOf(Actions.FileUpload.GET_DOWNLOAD_URL),
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                ),
        )

    // ============================================================
    // Content Management Policies (Categories & Tags)
    // ============================================================

    /**
     * Content manager - can manage categories and tags.
     */
    fun contentManagerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsContentManagerPolicy",
            description = "Full management access to categories and tags within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowCategoryManagement",
                        effect = Effect.ALLOW,
                        actions = Actions.Category.ALL,
                        resources = listOf(urn.categoryWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowTagManagement",
                        effect = Effect.ALLOW,
                        actions = Actions.Tag.ALL,
                        resources = listOf(urn.tagWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Content viewer - read-only access to categories and tags.
     */
    fun contentViewerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsContentViewerPolicy",
            description = "Read-only access to categories and tags within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowCategoryRead",
                        effect = Effect.ALLOW,
                        actions = Actions.Category.READ_ONLY,
                        resources = listOf(urn.categoryWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowTagRead",
                        effect = Effect.ALLOW,
                        actions = Actions.Tag.READ_ONLY,
                        resources = listOf(urn.tagWildcard(tenantId)),
                    ),
                ),
        )

    // ============================================================
    // Storage/Bucket Policies
    // ============================================================

    /**
     * Storage admin - can manage storage buckets.
     */
    fun storageAdminPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsStorageAdminPolicy",
            description = "Full management access to storage buckets within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowBucketManagement",
                        effect = Effect.ALLOW,
                        actions = BucketActions.Bucket.ALL,
                        resources = listOf(BucketsUrn.bucketWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Storage viewer - read-only access to bucket configurations.
     */
    fun storageViewerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsStorageViewerPolicy",
            description = "Read-only access to storage buckets within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowBucketRead",
                        effect = Effect.ALLOW,
                        actions = BucketActions.Bucket.READ_ONLY,
                        resources = listOf(BucketsUrn.bucketWildcard(tenantId)),
                    ),
                ),
        )

    // ============================================================
    // User Management Policies
    // ============================================================

    /**
     * User admin - can manage users within a tenant.
     */
    fun userAdminPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsUserAdminPolicy",
            description = "Full management access to users within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowUserManagement",
                        effect = Effect.ALLOW,
                        actions = Actions.User.ALL,
                        resources = listOf(urn.userWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * User viewer - read-only access to users.
     */
    fun userViewerPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsUserViewerPolicy",
            description = "Read-only access to users within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowUserRead",
                        effect = Effect.ALLOW,
                        actions = Actions.User.READ_ONLY,
                        resources = listOf(urn.userWildcard(tenantId)),
                    ),
                ),
        )

    // ============================================================
    // Composite Role Policies
    // ============================================================

    /**
     * Standard user - typical user with read access to most resources and edit access to documents.
     */
    fun standardUserPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsStandardUserPolicy",
            description = "Standard user access: view organizations/projects, edit documents within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowOrganizationRead",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Organization.READ_ONLY,
                        resources = listOf(urn.organizationWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowProjectRead",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.READ_ONLY,
                        resources = listOf(urn.projectWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowDocumentReadWrite",
                        effect = Effect.ALLOW,
                        actions = Actions.Document.READ_ONLY + Actions.Document.WRITE,
                        resources = listOf(urn.documentWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowDocumentVersionReadWrite",
                        effect = Effect.ALLOW,
                        actions = Actions.DocumentVersion.READ_ONLY + Actions.DocumentVersion.WRITE,
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowFileOperations",
                        effect = Effect.ALLOW,
                        actions = Actions.FileUpload.ALL,
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                    Statement(
                        sid = "AllowContentRead",
                        effect = Effect.ALLOW,
                        actions = Actions.Category.READ_ONLY + Actions.Tag.READ_ONLY,
                        resources =
                            listOf(
                                urn.categoryWildcard(tenantId),
                                urn.tagWildcard(tenantId),
                            ),
                    ),
                    Statement(
                        sid = "AllowSearch",
                        effect = Effect.ALLOW,
                        actions = Actions.Search.ALL,
                        resources = listOf(urn.documentWildcard(tenantId)),
                    ),
                ),
        )

    /**
     * Read-only user - can only view resources, no edit capabilities.
     */
    fun readOnlyUserPolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsReadOnlyUserPolicy",
            description = "Read-only access to all resources within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowAllReadOperations",
                        effect = Effect.ALLOW,
                        actions =
                            CoreActions.Organization.READ_ONLY +
                                CoreActions.Project.READ_ONLY +
                                Actions.Document.READ_ONLY +
                                Actions.DocumentVersion.READ_ONLY +
                                Actions.Category.READ_ONLY +
                                Actions.Tag.READ_ONLY +
                                Actions.Search.ALL,
                        resources =
                            listOf(
                                urn.organizationWildcard(tenantId),
                                urn.projectWildcard(tenantId),
                                urn.documentWildcard(tenantId),
                                urn.documentVersionWildcard(tenantId),
                                urn.categoryWildcard(tenantId),
                                urn.tagWildcard(tenantId),
                            ),
                    ),
                    Statement(
                        sid = "AllowDownload",
                        effect = Effect.ALLOW,
                        actions = listOf(Actions.FileUpload.GET_DOWNLOAD_URL),
                        resources = listOf(urn.documentVersionWildcard(tenantId)),
                    ),
                ),
        )

    // ============================================================
    // Resource-Specific Organization Policies
    // ============================================================

    /**
     * Admin access to a specific organization.
     */
    fun organizationAdminPolicy(
        tenantId: String,
        organizationUuid: UUID,
    ): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "OrganizationAdminPolicy-$organizationUuid",
            description = "Full administrative access to organization: $organizationUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowOrganizationAdmin",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Organization.ALL,
                        resources = listOf(urn.organization(tenantId, organizationUuid)),
                    ),
                ),
        )

    /**
     * Manager access to a specific organization.
     */
    fun organizationManagerPolicyForResource(
        tenantId: String,
        organizationUuid: UUID,
    ): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "OrganizationManagerPolicy-$organizationUuid",
            description = "Management access to organization: $organizationUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowOrganizationManagement",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Organization.ALL.filter { it != CoreActions.Organization.DELETE },
                        resources = listOf(urn.organization(tenantId, organizationUuid)),
                    ),
                ),
        )

    /**
     * Viewer access to a specific organization.
     */
    fun organizationViewerPolicyForResource(
        tenantId: String,
        organizationUuid: UUID,
    ): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "OrganizationViewerPolicy-$organizationUuid",
            description = "Read-only access to organization: $organizationUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowOrganizationRead",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Organization.READ_ONLY,
                        resources = listOf(urn.organization(tenantId, organizationUuid)),
                    ),
                ),
        )

    // ============================================================
    // Resource-Specific Project Policies
    // ============================================================

    /**
     * Admin access to a specific project.
     */
    fun projectAdminPolicy(
        tenantId: String,
        projectUuid: UUID,
    ): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "ProjectAdminPolicy-$projectUuid",
            description = "Full administrative access to project: $projectUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowProjectAdmin",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.ALL,
                        resources = listOf(urn.project(tenantId, projectUuid)),
                    ),
                ),
        )

    /**
     * Manager access to a specific project.
     */
    fun projectManagerPolicyForResource(
        tenantId: String,
        projectUuid: UUID,
    ): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "ProjectManagerPolicy-$projectUuid",
            description = "Management access to project: $projectUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowProjectManagement",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.ALL.filter { it != CoreActions.Project.DELETE },
                        resources = listOf(urn.project(tenantId, projectUuid)),
                    ),
                ),
        )

    /**
     * Editor access to a specific project.
     */
    fun projectEditorPolicyForResource(
        tenantId: String,
        projectUuid: UUID,
    ): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "ProjectEditorPolicy-$projectUuid",
            description = "Edit access to project: $projectUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowProjectReadWrite",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.READ_ONLY + CoreActions.Project.WRITE,
                        resources = listOf(urn.project(tenantId, projectUuid)),
                    ),
                ),
        )

    /**
     * Viewer access to a specific project.
     */
    fun projectViewerPolicyForResource(
        tenantId: String,
        projectUuid: UUID,
    ): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "ProjectViewerPolicy-$projectUuid",
            description = "Read-only access to project: $projectUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "AllowProjectRead",
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.READ_ONLY,
                        resources = listOf(urn.project(tenantId, projectUuid)),
                    ),
                ),
        )

    // ============================================================
    // Deny Policies
    // ============================================================

    /**
     * Deny delete operations - prevents any delete actions. Attach alongside other policies.
     */
    fun denyDeletePolicy(tenantId: String): Policy =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentsDenyDeletePolicy",
            description = "Explicitly denies all delete operations within tenant: $tenantId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        sid = "DenyAllDeletes",
                        effect = Effect.DENY,
                        actions =
                            listOf(
                                CoreActions.Organization.DELETE,
                                CoreActions.Project.DELETE,
                                Actions.Document.DELETE,
                                Actions.DocumentVersion.DELETE,
                                Actions.Category.DELETE,
                                Actions.Tag.DELETE,
                                BucketActions.Bucket.DELETE,
                                Actions.User.DELETE,
                            ),
                        resources = listOf(urn.wildcardAll()),
                    ),
                ),
        )
}
