package com.revethq.documents.api.mapper

import com.revethq.documents.domain.Category
import com.revethq.documents.dto.CategoryDTO

/**
 * Maps between Domain Category and DTOs for the API layer.
 */
object CategoryDTOMapper {
    fun toDTO(domain: com.revethq.documents.domain.Category): com.revethq.documents.dto.CategoryDTO =
        com.revethq.documents.dto.CategoryDTO(
            id = domain.id,
            name = domain.name,
            projectId = domain.projectId,
        )
}
