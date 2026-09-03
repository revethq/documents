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
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity_.id
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

/**
 * Service for provisioning users on authentication.
 *
 * When revet.auth.user.provision=auto, any user with a valid JWT is automatically
 * created. The first user in the system is granted admin access; subsequent users
 * are created with no permissions.
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
        @ConfigProperty(name = "revet.auth.user.provision")
        private val provisionMode: Optional<String>,
    ) {
        private val log = Logger.getLogger(UserProvisioningService::class.java)

        private val autoProvision: Boolean
            get() = provisionMode.orElse("").equals("auto", ignoreCase = true)

        /**
         * Provisions a user if they don't exist.
         *
         * When auto-provisioning is enabled, any authenticated user is created on first login.
         * The first user in the system is granted admin access; subsequent users get no permissions.
         *
         * @param externalId The external ID from the identity provider (JWT sub claim)
         * @param username The user's username (from JWT claims)
         * @param email The user's email (from JWT claims)
         * @param tenantId The tenant ID (may be null)
         * @param issuer The JWT issuer (used as IdentityProvider externalId)
         * @return The provisioned or existing user, or null if provisioning is not needed
         */
        @Transactional
        fun provisionUserIfNeeded(
            externalId: String,
            username: String?,
            email: String?,
            tenantId: String?,
            issuer: String?,
        ): User? {
            val identityProvider = findOrCreateIdentityProvider(issuer)

            // Check if user already exists by external ID
            val existingUser = userService.findByExternalId(externalId, identityProvider.id)
            if (existingUser != null) {
                return existingUser
            }

            // User doesn't exist — check if we should create them
            val isFirstUser = userService.count() == 0L

            if (!isFirstUser && !autoProvision) {
                return null
            }

            val user =
                User(
                    id = UUID.randomUUID(),
                    username = username ?: externalId,
                    email = email ?: "",
                    metadata = Metadata(),
                    createdOn = null,
                    updatedOn = null,
                )
            val createdUser = userService.create(user, identityProvider.id, externalId)

            if (isFirstUser) {
                attachGlobalAdminPolicy(createdUser, tenantId)
                log.info("Provisioned first user as admin: ${createdUser.id} (${createdUser.username})")
            } else {
                log.info("Auto-provisioned user: ${createdUser.id} (${createdUser.username})")
            }

            return createdUser
        }

        private fun findOrCreateIdentityProvider(issuer: String?): IdentityProviderEntity {
            if (issuer != null) {
                val byIssuer = identityProviderRepository.findByExternalId(issuer)
                if (byIssuer != null) {
                    return byIssuer
                }
            }

            val idp =
                IdentityProviderEntity().apply {
                    id = UUID.randomUUID()
                    name = issuer?.substringAfterLast("/")?.ifBlank { "default" } ?: "default"
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
            val effectiveTenantId = ""
            val adminPolicy = prebuiltPolicies.globalAdminPolicy()
            val savedPolicy = policyService.create(adminPolicy)
            val principalUrn = urn.userPrincipal(effectiveTenantId, user.id)
            policyAttachmentService.attach(savedPolicy.id, principalUrn, effectiveTenantId)
        }
    }
