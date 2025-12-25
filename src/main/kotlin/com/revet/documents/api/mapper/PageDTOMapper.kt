package com.revet.documents.api.mapper

import com.revet.documents.domain.Page
import com.revet.documents.dto.PageDTO
import com.revet.documents.dto.ProblemDetail

/**
 * Generic mapper for converting domain Page to PageDTO.
 */
object PageDTOMapper {

    fun <D, T> toDTO(
        page: com.revet.documents.domain.Page<D>,
        contentMapper: (D) -> T,
        warnings: List<com.revet.documents.dto.ProblemDetail> = emptyList()
    ): com.revet.documents.dto.PageDTO<T> {
        return _root_ide_package_.com.revet.documents.dto.PageDTO(
            content = page.content.map(contentMapper),
            page = page.page,
            size = page.size,
            hasMore = page.hasMore,
            warnings = warnings
        )
    }
}
