package com.revethq.documents.api.mapper

import com.revethq.documents.domain.Project
import com.revethq.documents.dto.ProjectDTO

/**
 * Maps between Domain Project and DTOs for the API layer.
 */
object ProjectDTOMapper {
    fun toDTO(domain: com.revethq.documents.domain.Project): com.revethq.documents.dto.ProjectDTO =
        com.revethq.documents.dto.ProjectDTO(
            id = domain.id,
            uuid = domain.uuid,
            name = domain.name,
            description = domain.description,
            organizationId = domain.organizationId,
            clientIds = domain.clientIds,
            tags = domain.tags,
            isActive = domain.isActive,
            createdAt = domain.timestamps.createdAt,
            modifiedAt = domain.timestamps.modifiedAt,
        )
}
