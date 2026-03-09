package com.revethq.documents.permission

/**
 * Permission actions for the Documents service.
 *
 * Actions follow the format: {service}:{action}
 * Service name: "documents"
 */
object Actions {
    const val SERVICE = "documents"

    // ============================================================
    // Organization Actions
    // ============================================================
    object Organization {
        const val LIST = "$SERVICE:ListOrganizations"
        const val GET = "$SERVICE:GetOrganization"
        const val CREATE = "$SERVICE:CreateOrganization"
        const val UPDATE = "$SERVICE:UpdateOrganization"
        const val DELETE = "$SERVICE:DeleteOrganization"

        val ALL = listOf(LIST, GET, CREATE, UPDATE, DELETE)
        val READ_ONLY = listOf(LIST, GET)
        val WRITE = listOf(CREATE, UPDATE)
    }

    // ============================================================
    // Project Actions
    // ============================================================
    object Project {
        const val LIST = "$SERVICE:ListProjects"
        const val GET = "$SERVICE:GetProject"
        const val CREATE = "$SERVICE:CreateProject"
        const val UPDATE = "$SERVICE:UpdateProject"
        const val DELETE = "$SERVICE:DeleteProject"
        const val ADD_CLIENT = "$SERVICE:AddProjectClient"
        const val REMOVE_CLIENT = "$SERVICE:RemoveProjectClient"
        const val ADD_TAG = "$SERVICE:AddProjectTag"
        const val REMOVE_TAG = "$SERVICE:RemoveProjectTag"

        val ALL = listOf(LIST, GET, CREATE, UPDATE, DELETE, ADD_CLIENT, REMOVE_CLIENT, ADD_TAG, REMOVE_TAG)
        val READ_ONLY = listOf(LIST, GET)
        val WRITE = listOf(CREATE, UPDATE, ADD_TAG, REMOVE_TAG)
        val MANAGE_CLIENTS = listOf(ADD_CLIENT, REMOVE_CLIENT)
    }

    // ============================================================
    // Document Actions
    // ============================================================
    object Document {
        const val LIST = "$SERVICE:ListDocuments"
        const val GET = "$SERVICE:GetDocument"
        const val CREATE = "$SERVICE:CreateDocument"
        const val UPDATE = "$SERVICE:UpdateDocument"
        const val DELETE = "$SERVICE:DeleteDocument"
        const val DOWNLOAD = "$SERVICE:DownloadDocument"
        const val ADD_TAG = "$SERVICE:AddDocumentTag"
        const val REMOVE_TAG = "$SERVICE:RemoveDocumentTag"

        val ALL = listOf(LIST, GET, CREATE, UPDATE, DELETE, DOWNLOAD, ADD_TAG, REMOVE_TAG)
        val READ_ONLY = listOf(LIST, GET, DOWNLOAD)
        val WRITE = listOf(CREATE, UPDATE, ADD_TAG, REMOVE_TAG)
    }

    // ============================================================
    // Document Version Actions
    // ============================================================
    object DocumentVersion {
        const val LIST = "$SERVICE:ListDocumentVersions"
        const val GET = "$SERVICE:GetDocumentVersion"
        const val CREATE = "$SERVICE:CreateDocumentVersion"
        const val UPDATE = "$SERVICE:UpdateDocumentVersion"
        const val DELETE = "$SERVICE:DeleteDocumentVersion"
        const val COMPLETE_UPLOAD = "$SERVICE:CompleteDocumentUpload"

        val ALL = listOf(LIST, GET, CREATE, UPDATE, DELETE, COMPLETE_UPLOAD)
        val READ_ONLY = listOf(LIST, GET)
        val WRITE = listOf(CREATE, UPDATE, COMPLETE_UPLOAD)
    }

    // ============================================================
    // Category Actions
    // ============================================================
    object Category {
        const val LIST = "$SERVICE:ListCategories"
        const val GET = "$SERVICE:GetCategory"
        const val CREATE = "$SERVICE:CreateCategory"
        const val UPDATE = "$SERVICE:UpdateCategory"
        const val DELETE = "$SERVICE:DeleteCategory"

        val ALL = listOf(LIST, GET, CREATE, UPDATE, DELETE)
        val READ_ONLY = listOf(LIST, GET)
        val WRITE = listOf(CREATE, UPDATE)
    }

    // ============================================================
    // Tag Actions
    // ============================================================
    object Tag {
        const val LIST = "$SERVICE:ListTags"
        const val GET = "$SERVICE:GetTag"
        const val CREATE = "$SERVICE:CreateTag"
        const val UPDATE = "$SERVICE:UpdateTag"
        const val DELETE = "$SERVICE:DeleteTag"

        val ALL = listOf(LIST, GET, CREATE, UPDATE, DELETE)
        val READ_ONLY = listOf(LIST, GET)
        val WRITE = listOf(CREATE, UPDATE)
    }

    // ============================================================
    // User Actions
    // ============================================================
    object User {
        const val LIST = "$SERVICE:ListUsers"
        const val GET = "$SERVICE:GetUser"
        const val CREATE = "$SERVICE:CreateUser"
        const val UPDATE = "$SERVICE:UpdateUser"
        const val DELETE = "$SERVICE:DeleteUser"

        val ALL = listOf(LIST, GET, CREATE, UPDATE, DELETE)
        val READ_ONLY = listOf(LIST, GET)
        val WRITE = listOf(CREATE, UPDATE)
    }

    // ============================================================
    // File Upload Actions
    // ============================================================
    object FileUpload {
        const val INITIATE_UPLOAD = "$SERVICE:InitiateUpload"
        const val GET_DOWNLOAD_URL = "$SERVICE:GetDownloadUrl"
        const val CREATE_VERSION_WITH_URL = "$SERVICE:CreateVersionWithUrl"

        val ALL = listOf(INITIATE_UPLOAD, GET_DOWNLOAD_URL, CREATE_VERSION_WITH_URL)
    }

    // ============================================================
    // Search Actions
    // ============================================================
    object Search {
        const val SEARCH_DOCUMENTS = "$SERVICE:SearchDocuments"

        val ALL = listOf(SEARCH_DOCUMENTS)
    }

    /**
     * Wildcard action that matches all actions in the Documents service.
     */
    const val ALL_ACTIONS = "$SERVICE:*"

    /**
     * Global wildcard that matches all actions across all services.
     */
    const val GLOBAL_WILDCARD = "*"
}
