package com.revet.documents.api.mapper

import com.revet.documents.domain.Document
import com.revet.documents.dto.DocumentDTO

/**
 * Maps between Domain Document and DTOs for the API layer.
 */
object DocumentDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.Document): com.revet.documents.dto.DocumentDTO {
        return _root_ide_package_.com.revet.documents.dto.DocumentDTO(
            id = domain.id,
            uuid = domain.uuid,
            name = domain.name,
            projectId = domain.projectId,
            categoryId = domain.categoryId,
            mime = domain.mime,
            date = domain.date,
            tags = domain.tags,
            isActive = domain.isActive
        )
    }
}
