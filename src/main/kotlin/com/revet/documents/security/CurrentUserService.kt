package com.revet.documents.security

import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.SecurityContext
import java.util.*

/**
 * Service to get the current authenticated user from the JWT token.
 *
 * The JWT "sub" claim contains the user's UUID.
 */
interface CurrentUserService {
    /**
     * Get the current user's UUID from the JWT "sub" claim.
     * @return User UUID or null if not authenticated
     */
    fun getCurrentUserUuid(): UUID?
}

@RequestScoped
class CurrentUserServiceImpl : CurrentUserService {

    @Context
    lateinit var securityContext: SecurityContext

    override fun getCurrentUserUuid(): UUID? {
        val principal = securityContext.userPrincipal ?: return null
        val sub = principal.name ?: return null

        return try {
            UUID.fromString(sub)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
