package com.revet.documents.api.mapper

import com.revet.documents.domain.Project
import com.revet.documents.dto.ProjectDTO

/**
 * Maps between Domain Project and DTOs for the API layer.
 */
object ProjectDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.Project): com.revet.documents.dto.ProjectDTO {
        return _root_ide_package_.com.revet.documents.dto.ProjectDTO(
            id = domain.id,
            uuid = domain.uuid,
            name = domain.name,
            description = domain.description,
            organizationId = domain.organizationId,
            clientIds = domain.clientIds,
            tags = domain.tags,
            isActive = domain.isActive,
            createdAt = domain.timestamps.createdAt,
            modifiedAt = domain.timestamps.modifiedAt
        )
    }
}
