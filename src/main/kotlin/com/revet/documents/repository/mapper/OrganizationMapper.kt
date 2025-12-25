package com.revet.documents.repository.mapper

import com.revet.documents.domain.Organization
import com.revet.documents.repository.entity.OrganizationEntity

/**
 * Maps between Domain Organization and OrganizationEntity (Panache).
 */
object OrganizationMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.OrganizationEntity): com.revet.documents.domain.Organization {
        return _root_ide_package_.com.revet.documents.domain.Organization(
            id = entity.id,
            uuid = entity.uuid,
            name = entity.name,
            description = entity.description,
            contactInfo = _root_ide_package_.com.revet.documents.domain.Organization.ContactInfo(
                address = entity.address,
                city = entity.city,
                state = entity.state,
                zipCode = entity.zipCode,
                country = entity.country,
                phone = entity.phone,
                fax = entity.fax,
                website = entity.website
            ),
            locale = entity.locale,
            timezone = entity.timezone,
            bucketId = entity.bucketId,
            isActive = entity.isActive,
            removedAt = entity.removedAt
        )
    }

    fun toEntity(domain: com.revet.documents.domain.Organization): com.revet.documents.repository.entity.OrganizationEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity().apply {
            // Only set id if it exists (for updates)
            domain.id?.let { this.id = it }
            this.uuid = domain.uuid
            this.name = domain.name
            this.description = domain.description
            this.address = domain.contactInfo.address
            this.city = domain.contactInfo.city
            this.state = domain.contactInfo.state
            this.zipCode = domain.contactInfo.zipCode
            this.country = domain.contactInfo.country
            this.phone = domain.contactInfo.phone
            this.fax = domain.contactInfo.fax
            this.website = domain.contactInfo.website
            this.locale = domain.locale
            this.timezone = domain.timezone
            this.bucketId = domain.bucketId
            this.isActive = domain.isActive
            this.removedAt = domain.removedAt
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.OrganizationEntity, domain: com.revet.documents.domain.Organization): com.revet.documents.repository.entity.OrganizationEntity {
        entity.apply {
            name = domain.name
            description = domain.description
            address = domain.contactInfo.address
            city = domain.contactInfo.city
            state = domain.contactInfo.state
            zipCode = domain.contactInfo.zipCode
            country = domain.contactInfo.country
            phone = domain.contactInfo.phone
            fax = domain.contactInfo.fax
            website = domain.contactInfo.website
            locale = domain.locale
            timezone = domain.timezone
            bucketId = domain.bucketId
            isActive = domain.isActive
            removedAt = domain.removedAt
        }
        return entity
    }
}
