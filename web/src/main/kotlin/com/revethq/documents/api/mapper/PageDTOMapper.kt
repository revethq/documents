package com.revethq.documents.api.mapper

import com.revethq.documents.domain.Page
import com.revethq.documents.dto.PageDTO
import com.revethq.documents.dto.ProblemDetail

/**
 * Generic mapper for converting domain Page to PageDTO.
 */
object PageDTOMapper {
    fun <D, T> toDTO(
        page: com.revethq.documents.domain.Page<D>,
        contentMapper: (D) -> T,
        warnings: List<com.revethq.documents.dto.ProblemDetail> = emptyList(),
    ): com.revethq.documents.dto.PageDTO<T> =
        com.revethq.documents.dto.PageDTO(
            content = page.content.map(contentMapper),
            page = page.page,
            size = page.size,
            hasMore = page.hasMore,
            warnings = warnings,
        )
}
