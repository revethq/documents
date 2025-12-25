package com.revet.documents.repository.mapper

import com.revet.documents.domain.OrganizationPermission
import com.revet.documents.repository.entity.OrganizationPermissionEntity

/**
 * Maps between Domain OrganizationPermission and OrganizationPermissionEntity (Panache).
 */
object OrganizationPermissionMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.OrganizationPermissionEntity): com.revet.documents.domain.OrganizationPermission {
        return _root_ide_package_.com.revet.documents.domain.OrganizationPermission(
            id = entity.id,
            organizationId = entity.organization?.id
                ?: throw IllegalStateException("OrganizationPermission must have an organization"),
            userId = entity.user?.id
                ?: throw IllegalStateException("OrganizationPermission must have a user"),
            permission = entity.permission
        )
    }

    fun toEntity(domain: com.revet.documents.domain.OrganizationPermission): com.revet.documents.repository.entity.OrganizationPermissionEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationPermissionEntity().apply {
            domain.id?.let { this.id = it }
            this.permission = domain.permission
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.OrganizationPermissionEntity, domain: com.revet.documents.domain.OrganizationPermission): com.revet.documents.repository.entity.OrganizationPermissionEntity {
        entity.apply {
            permission = domain.permission
        }
        return entity
    }
}
