package com.revet.documents.security

import com.revet.documents.service.UserProvisioningService
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.SecurityIdentityAugmentor
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.util.*

/**
 * Augments the security identity by provisioning users on first authentication.
 */
@ApplicationScoped
class UserProvisioningAugmentor : SecurityIdentityAugmentor {

    private val log = Logger.getLogger(UserProvisioningAugmentor::class.java)

    companion object {
        private const val DEFAULT_TENANT_ID = "default"
    }

    @Inject
    lateinit var userProvisioningService: UserProvisioningService

    override fun augment(identity: SecurityIdentity, context: AuthenticationRequestContext): Uni<SecurityIdentity> {
        if (identity.isAnonymous) {
            return Uni.createFrom().item(identity)
        }

        val principal = identity.principal
        if (principal !is JWTCallerPrincipal) {
            return Uni.createFrom().item(identity)
        }

        val principalName = principal.name ?: return Uni.createFrom().item(identity)

        // Parse user UUID
        val userUuid = try {
            UUID.fromString(principalName)
        } catch (e: IllegalArgumentException) {
            log.warn("Principal name is not a valid UUID: $principalName")
            return Uni.createFrom().item(identity)
        }

        // Extract claims directly from the JWT principal
        val username = principal.getClaim<String>("preferred_username") ?: principal.getClaim<String>("name")
        val email = principal.getClaim<String>("email")
        val tenantId = principal.getClaim<String>("tenant_id") ?: DEFAULT_TENANT_ID
        val subject = principal.subject ?: principalName
        val issuer = principal.issuer

        // Run provisioning on blocking thread via the context
        return context.runBlocking {
            try {
                userProvisioningService.provisionUserIfNeeded(
                    userUuid = userUuid,
                    username = username,
                    email = email,
                    externalId = subject,
                    tenantId = tenantId,
                    issuer = issuer
                )
            } catch (e: Exception) {
                log.error("Error during user provisioning", e)
            }
            identity
        }
    }
}
