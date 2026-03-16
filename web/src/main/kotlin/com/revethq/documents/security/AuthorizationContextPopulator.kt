package com.revethq.documents.security

import com.revethq.documents.permission.DocumentsUrn
import com.revethq.iam.permission.web.filter.AuthorizationContext
import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger
import java.util.UUID

/**
 * JAX-RS filter that populates the AuthorizationContext after authentication.
 * Runs before AUTHORIZATION (2000) to ensure context is available for permission checks.
 *
 * Reads the user UUID from the SecurityIdentity attribute set by UserProvisioningAugmentor,
 * avoiding any assumption about the format of the JWT sub claim.
 */
@Provider
@Priority(Priorities.AUTHORIZATION - 100) // Run before AuthorizationFilter
class AuthorizationContextPopulator : ContainerRequestFilter {
    private val log = Logger.getLogger(AuthorizationContextPopulator::class.java)

    @Inject
    lateinit var authorizationContext: AuthorizationContext

    @Inject
    lateinit var urn: DocumentsUrn

    @Inject
    lateinit var securityIdentity: SecurityIdentity

    override fun filter(requestContext: ContainerRequestContext) {
        val principal = requestContext.securityContext?.userPrincipal

        log.debug("AuthorizationContextPopulator: path=${requestContext.uriInfo.path}, principal=${principal?.name}")

        if (principal == null) {
            log.debug("No principal found, skipping context population")
            return
        }

        val userUuid = securityIdentity.getAttribute<UUID>(UserProvisioningAugmentor.USER_ID_ATTRIBUTE)
        if (userUuid == null) {
            log.warn("No userId attribute on SecurityIdentity, user may not be provisioned")
            return
        }

        // For now, use empty tenant ID since app is not multi-tenant
        val tenantId = ""
        val principalUrn = urn.userPrincipal(tenantId, userUuid)

        log.info("Setting authorizationContext: principalUrn=$principalUrn, tenantId=$tenantId")

        authorizationContext.principalUrn = principalUrn
        authorizationContext.tenantId = tenantId

        // Extract source IP from proxy headers
        val forwardedFor = requestContext.getHeaderString("X-Forwarded-For")
        val realIp = requestContext.getHeaderString("X-Real-IP")
        authorizationContext.sourceIp =
            forwardedFor?.split(",")?.firstOrNull()?.trim()
                ?: realIp?.trim()
    }
}
