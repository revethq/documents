package com.revet.documents.security

import com.revet.documents.permission.DocumentsUrn
import com.revethq.iam.permission.web.filter.AuthorizationContext
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.vertx.web.RouteFilter
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal
import io.vertx.ext.web.RoutingContext
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.util.*

/**
 * Vert.x route filter that populates the AuthorizationContext after authentication.
 * This runs after the request scope is active, so we can access request-scoped beans.
 */
class AuthorizationContextPopulator {

    private val log = Logger.getLogger(AuthorizationContextPopulator::class.java)

    companion object {
        private const val DEFAULT_TENANT_ID = "default"
    }

    @Inject
    lateinit var securityIdentity: SecurityIdentity

    @Inject
    lateinit var authorizationContext: AuthorizationContext

    @Inject
    lateinit var urn: DocumentsUrn

    @RouteFilter(200) // Run after authentication
    fun populateContext(rc: RoutingContext) {
        log.info("AuthorizationContextPopulator: path=${rc.request().path()}, anonymous=${securityIdentity.isAnonymous}")

        if (!securityIdentity.isAnonymous) {
            val principal = securityIdentity.principal
            log.info("Principal type: ${principal?.javaClass?.name}")

            if (principal is JWTCallerPrincipal) {
                val userUuid = try {
                    UUID.fromString(principal.name)
                } catch (e: IllegalArgumentException) {
                    log.warn("Invalid UUID: ${principal.name}")
                    rc.next()
                    return
                }

                val tenantId = principal.getClaim<String>("tenant_id") ?: DEFAULT_TENANT_ID
                val principalUrn = urn.userPrincipal(tenantId, userUuid)

                log.info("Setting authorizationContext: principalUrn=$principalUrn, tenantId=$tenantId")

                authorizationContext.principalUrn = principalUrn
                authorizationContext.tenantId = tenantId

                // Extract source IP
                val forwardedFor = rc.request().getHeader("X-Forwarded-For")
                val realIp = rc.request().getHeader("X-Real-IP")
                authorizationContext.sourceIp = forwardedFor?.split(",")?.firstOrNull()?.trim()
                    ?: realIp?.trim()
            }
        }
        rc.next()
    }
}
