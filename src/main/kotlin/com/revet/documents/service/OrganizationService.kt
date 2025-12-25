package com.revet.documents.service

import com.revet.documents.domain.Organization
import com.revet.documents.repository.OrganizationRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Service interface for Organization business logic.
 */
interface OrganizationService {
    fun getAllOrganizations(includeInactive: Boolean = false): List<com.revet.documents.domain.Organization>
    fun getOrganizationById(id: Long): com.revet.documents.domain.Organization?
    fun getOrganizationByUuid(uuid: UUID): com.revet.documents.domain.Organization?
    fun createOrganization(
        name: String,
        description: String? = null,
        contactInfo: com.revet.documents.domain.Organization.ContactInfo = _root_ide_package_.com.revet.documents.domain.Organization.ContactInfo(null, null, null, null, null, null, null, null),
        locale: String? = "en",
        timezone: String = "UTC",
        bucketId: Long? = null
    ): com.revet.documents.domain.Organization
    fun updateOrganization(
        id: Long,
        name: String? = null,
        description: String? = null,
        contactInfo: com.revet.documents.domain.Organization.ContactInfo? = null,
        locale: String? = null,
        timezone: String? = null,
        bucketId: Long? = null,
        isActive: Boolean? = null
    ): com.revet.documents.domain.Organization?
    fun deleteOrganization(id: Long): Boolean
}

/**
 * Implementation of OrganizationService with business logic.
 */
@ApplicationScoped
class OrganizationServiceImpl @Inject constructor(
    private val organizationRepository: com.revet.documents.repository.OrganizationRepository
) : com.revet.documents.service.OrganizationService {

    override fun getAllOrganizations(includeInactive: Boolean): List<com.revet.documents.domain.Organization> {
        return organizationRepository.findAll(includeInactive)
    }

    override fun getOrganizationById(id: Long): com.revet.documents.domain.Organization? {
        return organizationRepository.findById(id)
    }

    override fun getOrganizationByUuid(uuid: UUID): com.revet.documents.domain.Organization? {
        return organizationRepository.findByUuid(uuid)
    }

    override fun createOrganization(
        name: String,
        description: String?,
        contactInfo: com.revet.documents.domain.Organization.ContactInfo,
        locale: String?,
        timezone: String,
        bucketId: Long?
    ): com.revet.documents.domain.Organization {
        require(name.isNotBlank()) { "Organization name cannot be blank" }

        val organization = _root_ide_package_.com.revet.documents.domain.Organization.create(
            name = name,
            description = description,
            contactInfo = contactInfo,
            locale = locale,
            timezone = timezone,
            bucketId = bucketId
        )

        return organizationRepository.save(organization)
    }

    override fun updateOrganization(
        id: Long,
        name: String?,
        description: String?,
        contactInfo: com.revet.documents.domain.Organization.ContactInfo?,
        locale: String?,
        timezone: String?,
        bucketId: Long?,
        isActive: Boolean?
    ): com.revet.documents.domain.Organization? {
        val existing = organizationRepository.findById(id) ?: return null

        name?.let { require(it.isNotBlank()) { "Organization name cannot be blank" } }

        val updated = existing.update(
            name = name,
            description = description,
            contactInfo = contactInfo,
            locale = locale,
            timezone = timezone,
            bucketId = bucketId,
            isActive = isActive
        )

        return organizationRepository.save(updated)
    }

    override fun deleteOrganization(id: Long): Boolean {
        return organizationRepository.delete(id)
    }
}
