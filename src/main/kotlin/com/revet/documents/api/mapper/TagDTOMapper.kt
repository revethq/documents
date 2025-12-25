package com.revet.documents.api.mapper

import com.revet.documents.domain.Tag
import com.revet.documents.dto.TagDTO

/**
 * Maps between Domain Tag and DTOs for the API layer.
 */
object TagDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.Tag): com.revet.documents.dto.TagDTO {
        return _root_ide_package_.com.revet.documents.dto.TagDTO(
            id = domain.id,
            name = domain.name,
            slug = domain.slug
        )
    }
}
