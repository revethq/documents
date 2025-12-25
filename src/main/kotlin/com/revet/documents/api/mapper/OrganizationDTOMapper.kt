package com.revet.documents.api.mapper

import com.revet.documents.domain.Organization
import com.revet.documents.dto.OrganizationDTO
import com.revet.documents.dto.CreateOrganizationRequest
import com.revet.documents.dto.UpdateOrganizationRequest

/**
 * Maps between Domain Organization and DTOs for the API layer.
 */
object OrganizationDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.Organization): com.revet.documents.dto.OrganizationDTO {
        return _root_ide_package_.com.revet.documents.dto.OrganizationDTO(
            id = domain.id,
            uuid = domain.uuid,
            name = domain.name,
            description = domain.description,
            address = domain.contactInfo.address,
            city = domain.contactInfo.city,
            state = domain.contactInfo.state,
            zipCode = domain.contactInfo.zipCode,
            country = domain.contactInfo.country,
            phone = domain.contactInfo.phone,
            fax = domain.contactInfo.fax,
            website = domain.contactInfo.website,
            locale = domain.locale,
            timezone = domain.timezone,
            bucketId = domain.bucketId,
            isActive = domain.isActive
        )
    }

    fun toContactInfo(request: com.revet.documents.dto.CreateOrganizationRequest): com.revet.documents.domain.Organization.ContactInfo {
        return _root_ide_package_.com.revet.documents.domain.Organization.ContactInfo(
            address = request.address,
            city = request.city,
            state = request.state,
            zipCode = request.zipCode,
            country = request.country,
            phone = request.phone,
            fax = request.fax,
            website = request.website
        )
    }

    fun toContactInfo(request: com.revet.documents.dto.UpdateOrganizationRequest): com.revet.documents.domain.Organization.ContactInfo? {
        // Only create ContactInfo if at least one field is provided
        if (listOf(
                request.address,
                request.city,
                request.state,
                request.zipCode,
                request.country,
                request.phone,
                request.fax,
                request.website
            ).all { it == null }
        ) {
            return null
        }

        return _root_ide_package_.com.revet.documents.domain.Organization.ContactInfo(
            address = request.address,
            city = request.city,
            state = request.state,
            zipCode = request.zipCode,
            country = request.country,
            phone = request.phone,
            fax = request.fax,
            website = request.website
        )
    }
}
