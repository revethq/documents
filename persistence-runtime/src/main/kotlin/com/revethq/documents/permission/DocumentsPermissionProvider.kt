package com.revethq.documents.permission

import com.revethq.iam.permission.discovery.PermissionDeclaration
import com.revethq.iam.permission.discovery.PermissionManifest
import com.revethq.iam.permission.discovery.PermissionProvider
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class DocumentsPermissionProvider : PermissionProvider {
    override fun manifest() =
        PermissionManifest(
            service = Actions.SERVICE,
            permissions =
                listOf(
                    // Document
                    PermissionDeclaration(
                        action = Actions.Document.LIST,
                        description = "List all documents",
                        resourceType = "urn:revet:documents:{tenantId}:document/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Document.GET,
                        description = "Get a document by ID",
                        resourceType = "urn:revet:documents:{tenantId}:document/{documentId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Document.CREATE,
                        description = "Create a new document",
                        resourceType = "urn:revet:documents:{tenantId}:document/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Document.UPDATE,
                        description = "Update an existing document",
                        resourceType = "urn:revet:documents:{tenantId}:document/{documentId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Document.DELETE,
                        description = "Delete a document",
                        resourceType = "urn:revet:documents:{tenantId}:document/{documentId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Document.DOWNLOAD,
                        description = "Download a document file",
                        resourceType = "urn:revet:documents:{tenantId}:document/{documentId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Document.ADD_TAG,
                        description = "Add a tag to a document",
                        resourceType = "urn:revet:documents:{tenantId}:document/{documentId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Document.REMOVE_TAG,
                        description = "Remove a tag from a document",
                        resourceType = "urn:revet:documents:{tenantId}:document/{documentId}",
                    ),
                    // Document Version
                    PermissionDeclaration(
                        action = Actions.DocumentVersion.LIST,
                        description = "List all document versions",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.DocumentVersion.GET,
                        description = "Get a document version by ID",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/{versionId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.DocumentVersion.CREATE,
                        description = "Create a new document version",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.DocumentVersion.UPDATE,
                        description = "Update a document version",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/{versionId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.DocumentVersion.DELETE,
                        description = "Delete a document version",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/{versionId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.DocumentVersion.COMPLETE_UPLOAD,
                        description = "Mark a document version upload as complete",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/{versionId}",
                    ),
                    // Category
                    PermissionDeclaration(
                        action = Actions.Category.LIST,
                        description = "List all categories",
                        resourceType = "urn:revet:documents:{tenantId}:category/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Category.GET,
                        description = "Get a category by ID",
                        resourceType = "urn:revet:documents:{tenantId}:category/{categoryId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Category.CREATE,
                        description = "Create a new category",
                        resourceType = "urn:revet:documents:{tenantId}:category/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Category.UPDATE,
                        description = "Update an existing category",
                        resourceType = "urn:revet:documents:{tenantId}:category/{categoryId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Category.DELETE,
                        description = "Delete a category",
                        resourceType = "urn:revet:documents:{tenantId}:category/{categoryId}",
                    ),
                    // Tag
                    PermissionDeclaration(
                        action = Actions.Tag.LIST,
                        description = "List all tags",
                        resourceType = "urn:revet:documents:{tenantId}:tag/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Tag.GET,
                        description = "Get a tag by ID",
                        resourceType = "urn:revet:documents:{tenantId}:tag/{tagId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Tag.CREATE,
                        description = "Create a new tag",
                        resourceType = "urn:revet:documents:{tenantId}:tag/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.Tag.UPDATE,
                        description = "Update an existing tag",
                        resourceType = "urn:revet:documents:{tenantId}:tag/{tagId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.Tag.DELETE,
                        description = "Delete a tag",
                        resourceType = "urn:revet:documents:{tenantId}:tag/{tagId}",
                    ),
                    // User
                    PermissionDeclaration(
                        action = Actions.User.LIST,
                        description = "List all users",
                        resourceType = "urn:revet:documents:{tenantId}:user/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.GET,
                        description = "Get a user by ID",
                        resourceType = "urn:revet:documents:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.CREATE,
                        description = "Create a new user",
                        resourceType = "urn:revet:documents:{tenantId}:user/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.UPDATE,
                        description = "Update a user",
                        resourceType = "urn:revet:documents:{tenantId}:user/{userId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.User.DELETE,
                        description = "Delete a user",
                        resourceType = "urn:revet:documents:{tenantId}:user/{userId}",
                    ),
                    // File Upload
                    PermissionDeclaration(
                        action = Actions.FileUpload.INITIATE_UPLOAD,
                        description = "Initiate a file upload and get a presigned URL",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/*",
                    ),
                    PermissionDeclaration(
                        action = Actions.FileUpload.GET_DOWNLOAD_URL,
                        description = "Get a presigned download URL for a document version",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/{versionId}",
                    ),
                    PermissionDeclaration(
                        action = Actions.FileUpload.CREATE_VERSION_WITH_URL,
                        description = "Create a document version and get an upload URL in one step",
                        resourceType = "urn:revet:documents:{tenantId}:document-version/*",
                    ),
                    // Search
                    PermissionDeclaration(
                        action = Actions.Search.SEARCH_DOCUMENTS,
                        description = "Search across documents, projects, and organizations",
                        resourceType = "urn:revet:documents:{tenantId}:document/*",
                    ),
                ),
        )
}
