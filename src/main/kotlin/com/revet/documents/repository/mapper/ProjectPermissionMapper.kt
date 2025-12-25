package com.revet.documents.repository.mapper

import com.revet.documents.domain.ProjectPermission
import com.revet.documents.repository.entity.ProjectPermissionEntity

/**
 * Maps between Domain ProjectPermission and ProjectPermissionEntity (Panache).
 */
object ProjectPermissionMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.ProjectPermissionEntity): com.revet.documents.domain.ProjectPermission {
        return _root_ide_package_.com.revet.documents.domain.ProjectPermission(
            id = entity.id,
            projectId = entity.project?.id
                ?: throw IllegalStateException("ProjectPermission must have a project"),
            userId = entity.user?.id
                ?: throw IllegalStateException("ProjectPermission must have a user"),
            permission = entity.permission
        )
    }

    fun toEntity(domain: com.revet.documents.domain.ProjectPermission): com.revet.documents.repository.entity.ProjectPermissionEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectPermissionEntity().apply {
            domain.id?.let { this.id = it }
            this.permission = domain.permission
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.ProjectPermissionEntity, domain: com.revet.documents.domain.ProjectPermission): com.revet.documents.repository.entity.ProjectPermissionEntity {
        entity.apply {
            permission = domain.permission
        }
        return entity
    }
}
