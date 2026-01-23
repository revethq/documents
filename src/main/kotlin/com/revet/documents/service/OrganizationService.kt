package com.revet.documents.service

import com.revet.documents.domain.Organization
import com.revet.documents.permission.DocumentsUrn
import com.revet.documents.permission.PrebuiltPolicies
import com.revet.documents.repository.OrganizationRepository
import com.revet.documents.security.CurrentUserService
import com.revethq.iam.permission.persistence.service.PolicyAttachmentService
import com.revethq.iam.permission.persistence.service.PolicyService
import com.revethq.iam.permission.web.filter.AuthorizationContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

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
        bucketId: Long? = null
    ): Organization
    fun updateOrganization(
        id: Long,
        name: String? = null,
        description: String? = null,
        contactInfo: Organization.ContactInfo? = null,
        locale: String? = null,
        timezone: String? = null,
        bucketId: Long? = null,
        isActive: Boolean? = null
    ): Organization?
    fun updateOrganizationByUuid(
        uuid: UUID,
        name: String? = null,
        description: String? = null,
        contactInfo: Organization.ContactInfo? = null,
        locale: String? = null,
        timezone: String? = null,
        bucketId: Long? = null,
        isActive: Boolean? = null
    ): Organization?
    fun deleteOrganization(id: Long): Boolean
    fun deleteOrganizationByUuid(uuid: UUID): Boolean
}

/**
 * Implementation of OrganizationService with business logic.
 */
@ApplicationScoped
class OrganizationServiceImpl @Inject constructor(
    private val organizationRepository: OrganizationRepository,
    private val policyService: PolicyService,
    private val policyAttachmentService: PolicyAttachmentService,
    private val prebuiltPolicies: PrebuiltPolicies,
    private val urn: DocumentsUrn,
    private val authorizationContext: AuthorizationContext,
    private val currentUserService: CurrentUserService
) : OrganizationService {

    override fun getAllOrganizations(includeInactive: Boolean): List<Organization> {
        return organizationRepository.findAll(includeInactive)
    }

    override fun getOrganizationById(id: Long): Organization? {
        return organizationRepository.findById(id)
    }

    override fun getOrganizationByUuid(uuid: UUID): Organization? {
        return organizationRepository.findByUuid(uuid)
    }

    override fun createOrganization(
        name: String,
        description: String?,
        contactInfo: Organization.ContactInfo,
        locale: String?,
        timezone: String,
        bucketId: Long?
    ): Organization {
        require(name.isNotBlank()) { "Organization name cannot be blank" }

        val organization = Organization.create(
            name = name,
            description = description,
            contactInfo = contactInfo,
            locale = locale,
            timezone = timezone,
            bucketId = bucketId
        )

        val savedOrganization = organizationRepository.save(organization)

        // Create default policies for the organization
        createDefaultPolicies(savedOrganization)

        return savedOrganization
    }

    private fun createDefaultPolicies(organization: Organization) {
        val tenantId = authorizationContext.tenantId ?: return
        val currentUserUuid = currentUserService.getCurrentUserUuid() ?: return

        // Create admin policy and attach to creating user
        val adminPolicy = prebuiltPolicies.organizationAdminPolicy(tenantId, organization.uuid)
        val savedAdminPolicy = policyService.create(adminPolicy)
        policyAttachmentService.attach(
            savedAdminPolicy.id,
            urn.userPrincipal(tenantId, currentUserUuid),
            tenantId
        )

        // Create manager policy (unattached for later assignment)
        val managerPolicy = prebuiltPolicies.organizationManagerPolicyForResource(tenantId, organization.uuid)
        policyService.create(managerPolicy)

        // Create viewer policy (unattached for later assignment)
        val viewerPolicy = prebuiltPolicies.organizationViewerPolicyForResource(tenantId, organization.uuid)
        policyService.create(viewerPolicy)
    }

    override fun updateOrganization(
        id: Long,
        name: String?,
        description: String?,
        contactInfo: Organization.ContactInfo?,
        locale: String?,
        timezone: String?,
        bucketId: Long?,
        isActive: Boolean?
    ): Organization? {
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

    override fun updateOrganizationByUuid(
        uuid: UUID,
        name: String?,
        description: String?,
        contactInfo: Organization.ContactInfo?,
        locale: String?,
        timezone: String?,
        bucketId: Long?,
        isActive: Boolean?
    ): Organization? {
        val organization = organizationRepository.findByUuid(uuid) ?: return null
        return updateOrganization(organization.id!!, name, description, contactInfo, locale, timezone, bucketId, isActive)
    }

    override fun deleteOrganizationByUuid(uuid: UUID): Boolean {
        val organization = organizationRepository.findByUuid(uuid) ?: return false
        return organizationRepository.delete(organization.id!!)
    }
}
