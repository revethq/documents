package com.revethq.documents.security

import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.SecurityContext
import java.util.UUID

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
