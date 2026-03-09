package com.revethq.documents.api.mapper

import com.revethq.documents.domain.Tag
import com.revethq.documents.dto.TagDTO

/**
 * Maps between Domain Tag and DTOs for the API layer.
 */
object TagDTOMapper {
    fun toDTO(domain: com.revethq.documents.domain.Tag): com.revethq.documents.dto.TagDTO =
        com.revethq.documents.dto.TagDTO(
            id = domain.id,
            name = domain.name,
            slug = domain.slug,
        )
}
