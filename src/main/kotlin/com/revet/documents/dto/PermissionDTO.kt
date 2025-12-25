package com.revet.documents.dto

import com.revet.documents.domain.PermissionType

/**
 * Data Transfer Objects for Permissions.
 */
data class OrganizationPermissionDTO(
    val id: Long?,
    val organizationId: Long,
    val userId: Long,
    val permission: com.revet.documents.domain.PermissionType
)

data class ProjectPermissionDTO(
    val id: Long?,
    val projectId: Long,
    val userId: Long,
    val permission: com.revet.documents.domain.PermissionType
)

data class DocumentPermissionDTO(
    val id: Long?,
    val documentId: Long,
    val userId: Long,
    val permission: com.revet.documents.domain.PermissionType
)

data class GrantPermissionRequest(
    val userId: Long,
    val permission: com.revet.documents.domain.PermissionType
)

data class UpdatePermissionRequest(
    val permission: com.revet.documents.domain.PermissionType
)
