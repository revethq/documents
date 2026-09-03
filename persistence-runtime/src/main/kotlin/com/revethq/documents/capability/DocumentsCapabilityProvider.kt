package com.revethq.documents.capability

import com.revethq.capabilities.discovery.CapabilityProvider
import com.revethq.capabilities.domain.CapabilityDeclaration
import com.revethq.capabilities.domain.CapabilityManifest
import com.revethq.capabilities.domain.PermissionRef
import com.revethq.documents.permission.Actions
import jakarta.enterprise.context.ApplicationScoped
import com.revethq.buckets.permission.Actions as BucketActions
import com.revethq.core.permission.Actions as CoreActions
import com.revethq.iam.permission.discovery.Actions as IamActions

@ApplicationScoped
class DocumentsCapabilityProvider : CapabilityProvider {
    override fun manifest() =
        CapabilityManifest(
            service = Actions.SERVICE,
            capabilities =
                listOf(
                    // Documents-native capabilities
                    // Note: LIST actions are excluded because list endpoints use @Authenticated
                    // with service-layer permission filtering. Any user with at least one
                    // permission on a resource type can list (and see filtered results).
                    CapabilityDeclaration(
                        id = "documents:manage-documents",
                        name = "Manage Documents",
                        description = "Create, update, delete, and download documents",
                        category = "documents",
                        permissions =
                            listOf(
                                Actions.Document.GET,
                                Actions.Document.CREATE,
                                Actions.Document.UPDATE,
                                Actions.Document.DELETE,
                                Actions.Document.DOWNLOAD,
                                Actions.Document.ADD_TAG,
                                Actions.Document.REMOVE_TAG,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:document/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-versions",
                        name = "Manage Document Versions",
                        description = "Create, update, and delete document versions",
                        category = "documents",
                        permissions =
                            listOf(
                                Actions.DocumentVersion.GET,
                                Actions.DocumentVersion.CREATE,
                                Actions.DocumentVersion.UPDATE,
                                Actions.DocumentVersion.DELETE,
                                Actions.DocumentVersion.COMPLETE_UPLOAD,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:document-version/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-categories",
                        name = "Manage Categories",
                        description = "Create, update, and delete document categories",
                        category = "documents",
                        permissions =
                            listOf(
                                Actions.Category.GET,
                                Actions.Category.CREATE,
                                Actions.Category.UPDATE,
                                Actions.Category.DELETE,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:category/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-tags",
                        name = "Manage Tags",
                        description = "Create, update, and delete tags",
                        category = "documents",
                        permissions =
                            listOf(
                                Actions.Tag.GET,
                                Actions.Tag.CREATE,
                                Actions.Tag.UPDATE,
                                Actions.Tag.DELETE,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:tag/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-users",
                        name = "Manage Users",
                        description = "Create, update, and delete users",
                        category = "documents",
                        permissions =
                            listOf(
                                Actions.User.GET,
                                Actions.User.CREATE,
                                Actions.User.UPDATE,
                                Actions.User.DELETE,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:user/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:upload-files",
                        name = "Upload Files",
                        description = "Initiate uploads, complete uploads, and get download URLs",
                        category = "documents",
                        permissions =
                            Actions.FileUpload.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:document-version/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:search",
                        name = "Search Documents",
                        description = "Search across documents",
                        category = "documents",
                        permissions =
                            Actions.Search.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:document/*")
                            },
                    ),
                    // Cross-service capabilities
                    CapabilityDeclaration(
                        id = "documents:manage-organizations",
                        name = "Manage Organizations",
                        description = "Create, update, and delete organizations",
                        category = "core",
                        permissions =
                            listOf(
                                CoreActions.Organization.GET,
                                CoreActions.Organization.CREATE,
                                CoreActions.Organization.UPDATE,
                                CoreActions.Organization.DELETE,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:core:{tenantId}:organization/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-projects",
                        name = "Manage Projects",
                        description = "Create, update, and delete projects",
                        category = "core",
                        permissions =
                            listOf(
                                CoreActions.Project.GET,
                                CoreActions.Project.CREATE,
                                CoreActions.Project.UPDATE,
                                CoreActions.Project.DELETE,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:core:{tenantId}:project/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-buckets",
                        name = "Manage Buckets",
                        description = "Create, update, and delete storage buckets",
                        category = "storage",
                        permissions =
                            listOf(
                                BucketActions.Bucket.GET,
                                BucketActions.Bucket.CREATE,
                                BucketActions.Bucket.UPDATE,
                                BucketActions.Bucket.DELETE,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:buckets:{tenantId}:bucket/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-groups",
                        name = "Manage Groups",
                        description = "Create, update, and delete groups and manage group membership",
                        category = "iam",
                        permissions =
                            listOf(
                                IamActions.Group.GET,
                                IamActions.Group.CREATE,
                                IamActions.Group.UPDATE,
                                IamActions.Group.DELETE,
                                IamActions.Group.LIST_MEMBERS,
                                IamActions.Group.ADD_MEMBER,
                                IamActions.Group.REMOVE_MEMBER,
                            ).map {
                                PermissionRef(action = it, resourceType = "urn:revet:iam:{tenantId}:group/*")
                            },
                    ),
                ),
        )
}
