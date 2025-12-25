package com.revet.documents.repository.mapper

import com.revet.documents.domain.Project
import com.revet.documents.repository.entity.ProjectEntity
import com.revet.documents.repository.entity.OrganizationEntity
import com.revet.documents.repository.entity.UserEntity

/**
 * Maps between Domain Project and ProjectEntity (Panache).
 */
object ProjectMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.ProjectEntity): com.revet.documents.domain.Project {
        return _root_ide_package_.com.revet.documents.domain.Project(
            id = entity.id,
            uuid = entity.uuid,
            name = entity.name,
            description = entity.description,
            organizationId = entity.organization?.id
                ?: throw IllegalStateException("Project must have an organization"),
            clientIds = entity.clients.mapNotNull { it.id }.toSet(),
            tags = entity.tags.toSet(),
            isActive = entity.isActive,
            timestamps = _root_ide_package_.com.revet.documents.domain.Project.Timestamps(
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt,
                removedAt = entity.removedAt
            )
        )
    }

    fun toEntity(domain: com.revet.documents.domain.Project): com.revet.documents.repository.entity.ProjectEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity().apply {
            domain.id?.let { this.id = it }
            this.uuid = domain.uuid
            this.name = domain.name
            this.description = domain.description
            this.isActive = domain.isActive
            this.createdAt = domain.timestamps.createdAt
            this.modifiedAt = domain.timestamps.modifiedAt
            this.removedAt = domain.timestamps.removedAt
            this.tags = domain.tags.toMutableSet()
            // Organization and clients need to be set separately after entity is created
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.ProjectEntity, domain: com.revet.documents.domain.Project): com.revet.documents.repository.entity.ProjectEntity {
        entity.apply {
            name = domain.name
            description = domain.description
            isActive = domain.isActive
            modifiedAt = domain.timestamps.modifiedAt
            removedAt = domain.timestamps.removedAt
            tags = domain.tags.toMutableSet()
            // Clients need to be updated separately
        }
        return entity
    }
}
