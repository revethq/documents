package com.revet.documents.domain

/**
 * Core domain model for DocumentPermission.
 * Links users to documents with specific permissions.
 */
data class DocumentPermission(
    val id: Long?,
    val documentId: Long,
    val userId: Long,
    val permission: com.revet.documents.domain.PermissionType
) {
    companion object {
        fun create(
            documentId: Long,
            userId: Long,
            permission: com.revet.documents.domain.PermissionType
        ): DocumentPermission {
            return DocumentPermission(
                id = null,
                documentId = documentId,
                userId = userId,
                permission = permission
            )
        }
    }

    fun update(permission: com.revet.documents.domain.PermissionType): DocumentPermission {
        return this.copy(permission = permission)
    }

    fun isNew(): Boolean = id == null
}
