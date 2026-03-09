package com.revethq.documents.api.mapper

import com.revethq.documents.domain.Organization
import com.revethq.documents.dto.CreateOrganizationRequest
import com.revethq.documents.dto.OrganizationDTO
import com.revethq.documents.dto.UpdateOrganizationRequest

/**
 * Maps between Domain Organization and DTOs for the API layer.
 */
object OrganizationDTOMapper {
    fun toDTO(domain: com.revethq.documents.domain.Organization): com.revethq.documents.dto.OrganizationDTO =
        com.revethq.documents.dto.OrganizationDTO(
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
            isActive = domain.isActive,
        )

    fun toContactInfo(request: com.revethq.documents.dto.CreateOrganizationRequest): com.revethq.documents.domain.Organization.ContactInfo =
        com.revethq.documents.domain.Organization.ContactInfo(
            address = request.address,
            city = request.city,
            state = request.state,
            zipCode = request.zipCode,
            country = request.country,
            phone = request.phone,
            fax = request.fax,
            website = request.website,
        )

    fun toContactInfo(
        request: com.revethq.documents.dto.UpdateOrganizationRequest,
    ): com.revethq.documents.domain.Organization.ContactInfo? {
        // Only create ContactInfo if at least one field is provided
        if (listOf(
                request.address,
                request.city,
                request.state,
                request.zipCode,
                request.country,
                request.phone,
                request.fax,
                request.website,
            ).all { it == null }
        ) {
            return null
        }

        return com.revethq.documents.domain.Organization.ContactInfo(
            address = request.address,
            city = request.city,
            state = request.state,
            zipCode = request.zipCode,
            country = request.country,
            phone = request.phone,
            fax = request.fax,
            website = request.website,
        )
    }
}
