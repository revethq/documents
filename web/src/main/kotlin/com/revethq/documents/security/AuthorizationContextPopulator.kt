package com.revethq.documents.security

import com.revethq.documents.permission.DocumentsUrn
import com.revethq.iam.permission.web.filter.AuthorizationContext
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal
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
 */
@Provider
@Priority(Priorities.AUTHORIZATION - 100) // Run before AuthorizationFilter
class AuthorizationContextPopulator : ContainerRequestFilter {
    private val log = Logger.getLogger(AuthorizationContextPopulator::class.java)

    companion object {
        private const val DEFAULT_TENANT_ID = "default"
    }

    @Inject
    lateinit var authorizationContext: AuthorizationContext

    @Inject
    lateinit var urn: DocumentsUrn

    override fun filter(requestContext: ContainerRequestContext) {
        val principal = requestContext.securityContext?.userPrincipal

        log.debug("AuthorizationContextPopulator: path=${requestContext.uriInfo.path}, principal=${principal?.name}")

        if (principal == null) {
            log.debug("No principal found, skipping context population")
            return
        }

        if (principal is JWTCallerPrincipal) {
            val userUuid =
                try {
                    UUID.fromString(principal.name)
                } catch (e: IllegalArgumentException) {
                    log.warn("Invalid UUID in principal name: ${principal.name}")
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
            authorizationContext.sourceIp = forwardedFor?.split(",")?.firstOrNull()?.trim()
                ?: realIp?.trim()
        } else {
            log.debug("Principal is not a JWTCallerPrincipal: ${principal.javaClass.name}")
        }
    }
}
