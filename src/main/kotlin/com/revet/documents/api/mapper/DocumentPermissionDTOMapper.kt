package com.revet.documents.api.mapper

import com.revet.documents.domain.DocumentPermission
import com.revet.documents.dto.DocumentPermissionDTO

/**
 * Maps between Domain DocumentPermission and DTOs for the API layer.
 */
object DocumentPermissionDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.DocumentPermission): com.revet.documents.dto.DocumentPermissionDTO {
        return _root_ide_package_.com.revet.documents.dto.DocumentPermissionDTO(
            id = domain.id,
            documentId = domain.documentId,
            userId = domain.userId,
            permission = domain.permission
        )
    }
}
