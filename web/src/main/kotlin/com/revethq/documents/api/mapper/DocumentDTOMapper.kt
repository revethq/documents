package com.revethq.documents.api.mapper

import com.revethq.documents.domain.Document
import com.revethq.documents.dto.DocumentDTO

/**
 * Maps between Domain Document and DTOs for the API layer.
 */
object DocumentDTOMapper {
    fun toDTO(domain: com.revethq.documents.domain.Document): com.revethq.documents.dto.DocumentDTO =
        com.revethq.documents.dto.DocumentDTO(
            id = domain.id,
            uuid = domain.uuid,
            name = domain.name,
            projectId = domain.projectId,
            categoryId = domain.categoryId,
            mime = domain.mime,
            date = domain.date,
            tags = domain.tags,
            isActive = domain.isActive,
        )
}
