package com.revet.documents.api.mapper

import com.revet.documents.domain.OrganizationPermission
import com.revet.documents.dto.OrganizationPermissionDTO

/**
 * Maps between Domain OrganizationPermission and DTOs for the API layer.
 */
object OrganizationPermissionDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.OrganizationPermission): com.revet.documents.dto.OrganizationPermissionDTO {
        return _root_ide_package_.com.revet.documents.dto.OrganizationPermissionDTO(
            id = domain.id,
            organizationId = domain.organizationId,
            userId = domain.userId,
            permission = domain.permission
        )
    }
}
