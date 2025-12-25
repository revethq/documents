package com.revet.documents.domain

/**
 * Core domain model for OrganizationPermission.
 * Links users to organizations with specific permissions.
 */
data class OrganizationPermission(
    val id: Long?,
    val organizationId: Long,
    val userId: Long,
    val permission: com.revet.documents.domain.PermissionType
) {
    companion object {
        fun create(
            organizationId: Long,
            userId: Long,
            permission: com.revet.documents.domain.PermissionType
        ): OrganizationPermission {
            return OrganizationPermission(
                id = null,
                organizationId = organizationId,
                userId = userId,
                permission = permission
            )
        }
    }

    fun update(permission: com.revet.documents.domain.PermissionType): OrganizationPermission {
        return this.copy(permission = permission)
    }

    fun isNew(): Boolean = id == null
}
