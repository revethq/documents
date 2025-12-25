package com.revet.documents.api.mapper

import com.revet.documents.domain.Category
import com.revet.documents.dto.CategoryDTO

/**
 * Maps between Domain Category and DTOs for the API layer.
 */
object CategoryDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.Category): com.revet.documents.dto.CategoryDTO {
        return _root_ide_package_.com.revet.documents.dto.CategoryDTO(
            id = domain.id,
            name = domain.name,
            projectId = domain.projectId
        )
    }
}
