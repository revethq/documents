package com.revet.documents.repository.mapper

import com.revet.documents.domain.DocumentPermission
import com.revet.documents.repository.entity.DocumentPermissionEntity

/**
 * Maps between Domain DocumentPermission and DocumentPermissionEntity (Panache).
 */
object DocumentPermissionMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.DocumentPermissionEntity): com.revet.documents.domain.DocumentPermission {
        return _root_ide_package_.com.revet.documents.domain.DocumentPermission(
            id = entity.id,
            documentId = entity.document?.id
                ?: throw IllegalStateException("DocumentPermission must have a document"),
            userId = entity.user?.id
                ?: throw IllegalStateException("DocumentPermission must have a user"),
            permission = entity.permission
        )
    }

    fun toEntity(domain: com.revet.documents.domain.DocumentPermission): com.revet.documents.repository.entity.DocumentPermissionEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentPermissionEntity().apply {
            domain.id?.let { this.id = it }
            this.permission = domain.permission
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.DocumentPermissionEntity, domain: com.revet.documents.domain.DocumentPermission): com.revet.documents.repository.entity.DocumentPermissionEntity {
        entity.apply {
            permission = domain.permission
        }
        return entity
    }
}
