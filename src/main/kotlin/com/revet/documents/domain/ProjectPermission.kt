package com.revet.documents.domain

/**
 * Core domain model for ProjectPermission.
 * Links users to projects with specific permissions.
 */
data class ProjectPermission(
    val id: Long?,
    val projectId: Long,
    val userId: Long,
    val permission: com.revet.documents.domain.PermissionType
) {
    companion object {
        fun create(
            projectId: Long,
            userId: Long,
            permission: com.revet.documents.domain.PermissionType
        ): ProjectPermission {
            return ProjectPermission(
                id = null,
                projectId = projectId,
                userId = userId,
                permission = permission
            )
        }
    }

    fun update(permission: com.revet.documents.domain.PermissionType): ProjectPermission {
        return this.copy(permission = permission)
    }

    fun isNew(): Boolean = id == null
}
