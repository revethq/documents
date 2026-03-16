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
                    CapabilityDeclaration(
                        id = "documents:manage-documents",
                        name = "Manage Documents",
                        description = "Create, update, delete, and download documents",
                        category = "documents",
                        permissions =
                            Actions.Document.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:document/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-versions",
                        name = "Manage Document Versions",
                        description = "Create, update, and delete document versions",
                        category = "documents",
                        permissions =
                            Actions.DocumentVersion.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:document-version/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-categories",
                        name = "Manage Categories",
                        description = "Create, update, and delete document categories",
                        category = "documents",
                        permissions =
                            Actions.Category.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:category/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-tags",
                        name = "Manage Tags",
                        description = "Create, update, and delete tags",
                        category = "documents",
                        permissions =
                            Actions.Tag.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:documents:{tenantId}:tag/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-users",
                        name = "Manage Users",
                        description = "Create, update, and delete users",
                        category = "documents",
                        permissions =
                            Actions.User.ALL.map {
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
                            CoreActions.Organization.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:core:{tenantId}:organization/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-projects",
                        name = "Manage Projects",
                        description = "Create, update, and delete projects",
                        category = "core",
                        permissions =
                            CoreActions.Project.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:core:{tenantId}:project/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-buckets",
                        name = "Manage Buckets",
                        description = "Create, update, and delete storage buckets",
                        category = "storage",
                        permissions =
                            BucketActions.Bucket.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:buckets:{tenantId}:bucket/*")
                            },
                    ),
                    CapabilityDeclaration(
                        id = "documents:manage-groups",
                        name = "Manage Groups",
                        description = "Create, update, and delete groups and manage group membership",
                        category = "iam",
                        permissions =
                            IamActions.Group.ALL.map {
                                PermissionRef(action = it, resourceType = "urn:revet:iam:{tenantId}:group/*")
                            },
                    ),
                ),
        )
}
