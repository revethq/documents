package com.revethq.documents.service

import java.util.UUID

interface CurrentUserService {
    fun getCurrentUserUuid(): UUID?
}
