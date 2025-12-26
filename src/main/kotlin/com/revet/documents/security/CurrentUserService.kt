package com.revet.documents.security

import com.revet.documents.service.UserService
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.SecurityContext
import java.util.*

/**
 * Service to get the current authenticated user from the JWT token.
 *
 * The JWT "sub" claim contains the user's UUID, which is used to look up
 * the user and get their internal Long ID for permission checks.
 */
interface CurrentUserService {
    /**
     * Get the current user's internal Long ID.
     * @return User ID or null if not authenticated
     */
    fun getCurrentUserId(): Long?

    /**
     * Get the current user's UUID from the JWT "sub" claim.
     * @return User UUID or null if not authenticated
     */
    fun getCurrentUserUuid(): UUID?
}

@RequestScoped
class CurrentUserServiceImpl @Inject constructor(
    private val userService: UserService
) : CurrentUserService {

    @Context
    lateinit var securityContext: SecurityContext

    override fun getCurrentUserId(): Long? {
        val uuid = getCurrentUserUuid() ?: return null
        val user = userService.getUserByUuid(uuid) ?: return null
        return user.id
    }

    override fun getCurrentUserUuid(): UUID? {
        // Get the "sub" claim from JWT via SecurityContext
        // The principal name is the "sub" claim when using JWT
        val principal = securityContext.userPrincipal ?: return null
        val sub = principal.name ?: return null

        return try {
            UUID.fromString(sub)
        } catch (e: IllegalArgumentException) {
            // Sub is not a valid UUID
            null
        }
    }
}
