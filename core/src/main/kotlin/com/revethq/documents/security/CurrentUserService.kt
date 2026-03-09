package com.revethq.documents.security

import java.util.UUID

interface CurrentUserService {
    fun getCurrentUserUuid(): UUID?
}
