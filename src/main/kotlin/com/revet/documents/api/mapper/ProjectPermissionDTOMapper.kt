package com.revet.documents.api.mapper

import com.revet.documents.domain.ProjectPermission
import com.revet.documents.dto.ProjectPermissionDTO

/**
 * Maps between Domain ProjectPermission and DTOs for the API layer.
 */
object ProjectPermissionDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.ProjectPermission): com.revet.documents.dto.ProjectPermissionDTO {
        return _root_ide_package_.com.revet.documents.dto.ProjectPermissionDTO(
            id = domain.id,
            projectId = domain.projectId,
            userId = domain.userId,
            permission = domain.permission
        )
    }
}
