package com.revethq.documents.service

import com.revethq.documents.domain.Organization
import java.util.UUID

/**
 * Service interface for Organization business logic.
 */
interface OrganizationService {
    fun getAllOrganizations(includeInactive: Boolean = false): List<Organization>

    fun getOrganizationById(id: Long): Organization?

    fun getOrganizationByUuid(uuid: UUID): Organization?

    fun createOrganization(
        name: String,
        description: String? = null,
        contactInfo: Organization.ContactInfo = Organization.ContactInfo(null, null, null, null, null, null, null, null),
        locale: String? = "en",
        timezone: String = "UTC",
        bucketId: Long? = null,
    ): Organization

    fun updateOrganization(
        id: Long,
        name: String? = null,
        description: String? = null,
        contactInfo: Organization.ContactInfo? = null,
        locale: String? = null,
        timezone: String? = null,
        bucketId: Long? = null,
        isActive: Boolean? = null,
    ): Organization?

    fun updateOrganizationByUuid(
        uuid: UUID,
        name: String? = null,
        description: String? = null,
        contactInfo: Organization.ContactInfo? = null,
        locale: String? = null,
        timezone: String? = null,
        bucketId: Long? = null,
        isActive: Boolean? = null,
    ): Organization?

    fun deleteOrganization(id: Long): Boolean

    fun deleteOrganizationByUuid(uuid: UUID): Boolean
}
