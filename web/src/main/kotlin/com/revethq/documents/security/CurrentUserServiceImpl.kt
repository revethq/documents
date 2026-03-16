package com.revethq.documents.security

import com.revethq.documents.service.CurrentUserService
import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import java.util.UUID

@RequestScoped
class CurrentUserServiceImpl
    @Inject
    constructor(
        private val securityIdentity: SecurityIdentity,
    ) : CurrentUserService {
        override fun getCurrentUserUuid(): UUID? =
            securityIdentity.getAttribute<UUID>(UserProvisioningAugmentor.USER_ID_ATTRIBUTE)
    }
