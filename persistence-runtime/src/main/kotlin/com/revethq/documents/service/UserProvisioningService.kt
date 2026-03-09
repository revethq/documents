package com.revethq.documents.service

import com.revethq.core.Metadata
import com.revethq.documents.permission.DocumentsUrn
import com.revethq.documents.permission.PrebuiltPolicies
import com.revethq.iam.permission.persistence.service.PolicyService
import com.revethq.iam.permission.service.PolicyAttachmentService
import com.revethq.iam.user.domain.User
import com.revethq.iam.user.persistence.entity.IdentityProviderEntity
import com.revethq.iam.user.persistence.repository.IdentityProviderRepository
import com.revethq.iam.user.persistence.service.UserService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.jboss.logging.Logger
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service for provisioning users on first authentication.
 *
 * When a user authenticates and no users exist in the system,
 * this service creates the user as an admin with full access.
 */
@ApplicationScoped
class UserProvisioningService
    @Inject
    constructor(
        private val userService: UserService,
        private val identityProviderRepository: IdentityProviderRepository,
        private val policyService: PolicyService,
        private val policyAttachmentService: PolicyAttachmentService,
        private val prebuiltPolicies: PrebuiltPolicies,
        private val urn: DocumentsUrn,
    ) {
        private val log = Logger.getLogger(UserProvisioningService::class.java)
        private val provisioningInProgress = AtomicBoolean(false)
        private val provisioningComplete = AtomicBoolean(false)

        /**
         * Provisions a user if they don't exist.
         * If this is the first user in the system, they are granted admin access.
         *
         * @param userUuid The user's UUID from the JWT
         * @param username The user's username (from JWT claims)
         * @param email The user's email (from JWT claims)
         * @param externalId The external ID from the identity provider
         * @param tenantId The tenant ID (may be null)
         * @param issuer The JWT issuer (used as IdentityProvider externalId)
         * @return The provisioned or existing user, or null if provisioning is not needed
         */
        @Transactional
        fun provisionUserIfNeeded(
            userUuid: UUID,
            username: String?,
            email: String?,
            externalId: String,
            tenantId: String?,
            issuer: String?,
        ): User? {
            // Fast path: if provisioning already complete, just check for existing user
            if (provisioningComplete.get()) {
                return userService.findById(userUuid)
            }

            // Check if user already exists
            val existingUser = userService.findById(userUuid)
            if (existingUser != null) {
                provisioningComplete.set(true)
                return existingUser
            }

            // Try to acquire provisioning lock - if another thread is already provisioning, skip
            if (!provisioningInProgress.compareAndSet(false, true)) {
                return null
            }

            // Double-check user doesn't exist
            val existingAfterLock = userService.findById(userUuid)
            if (existingAfterLock != null) {
                provisioningComplete.set(true)
                return existingAfterLock
            }

            // Check if this is the first user
            val userCount = userService.count()
            if (userCount > 0) {
                provisioningComplete.set(true)
                return null
            }

            // This is the first user - create them as admin
            val identityProvider = findOrCreateDefaultIdentityProvider(issuer)

            val user =
                User(
                    id = userUuid,
                    username = username ?: "admin",
                    email = email ?: "admin@localhost",
                    metadata = Metadata(),
                    createdOn = null,
                    updatedOn = null,
                )
            val createdUser = userService.create(user, identityProvider.id, externalId)

            attachGlobalAdminPolicy(createdUser, tenantId)

            log.info("Provisioned first user as admin: ${createdUser.id} (${createdUser.username})")
            provisioningComplete.set(true)
            return createdUser
        }

        private fun findOrCreateDefaultIdentityProvider(issuer: String?): IdentityProviderEntity {
            val existing = identityProviderRepository.findAll().firstResult()
            if (existing != null) {
                return existing
            }

            val idp =
                IdentityProviderEntity().apply {
                    id = UUID.randomUUID()
                    name = "default"
                    externalId = issuer
                    createdOn = OffsetDateTime.now()
                    updatedOn = OffsetDateTime.now()
                }
            identityProviderRepository.persist(idp)
            return idp
        }

        private fun attachGlobalAdminPolicy(
            user: User,
            tenantId: String?,
        ) {
            // Use empty tenant ID since app is not multi-tenant
            val effectiveTenantId = ""
            val adminPolicy = prebuiltPolicies.globalAdminPolicy()
            val savedPolicy = policyService.create(adminPolicy)
            val principalUrn = urn.userPrincipal(effectiveTenantId, user.id)
            policyAttachmentService.attach(savedPolicy.id, principalUrn, effectiveTenantId)
        }
    }
