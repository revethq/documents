package com.revethq.documents.security

import com.revethq.documents.service.UserProvisioningService
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.SecurityIdentityAugmentor
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger

/**
 * Augments the security identity by provisioning users on first authentication.
 *
 * Stores the resolved user UUID as the "userId" attribute on the SecurityIdentity
 * so downstream components can access it without repeating the external ID lookup.
 */
@ApplicationScoped
class UserProvisioningAugmentor : SecurityIdentityAugmentor {
    private val log = Logger.getLogger(UserProvisioningAugmentor::class.java)

    companion object {
        const val USER_ID_ATTRIBUTE = "userId"
        private const val DEFAULT_TENANT_ID = "default"
    }

    @Inject
    lateinit var userProvisioningService: UserProvisioningService

    override fun augment(
        identity: SecurityIdentity,
        context: AuthenticationRequestContext,
    ): Uni<SecurityIdentity> {
        if (identity.isAnonymous) {
            return Uni.createFrom().item(identity)
        }

        val principal = identity.principal
        if (principal !is JWTCallerPrincipal) {
            return Uni.createFrom().item(identity)
        }

        val subject = principal.subject ?: return Uni.createFrom().item(identity)

        // Extract claims directly from the JWT principal
        val username = principal.getClaim<String>("preferred_username") ?: principal.getClaim<String>("name")
        val email = principal.getClaim<String>("email")
        val issuer = principal.issuer
        val tenantId = principal.getClaim<String>("tenant_id") ?: issuer ?: DEFAULT_TENANT_ID

        // Run provisioning on blocking thread via the context
        return context.runBlocking {
            try {
                val user =
                    userProvisioningService.provisionUserIfNeeded(
                        externalId = subject,
                        username = username,
                        email = email,
                        tenantId = tenantId,
                        issuer = issuer,
                    )

                if (user != null) {
                    // Store the resolved user UUID on the identity for downstream use
                    QuarkusSecurityIdentity
                        .builder(identity)
                        .addAttribute(USER_ID_ATTRIBUTE, user.id)
                        .build()
                } else {
                    identity
                }
            } catch (e: Exception) {
                log.error("Error during user provisioning", e)
                identity
            }
        }
    }
}
