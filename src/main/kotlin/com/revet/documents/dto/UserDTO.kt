package com.revet.documents.dto

import java.time.LocalDateTime
import java.util.*

/**
 * Data Transfer Object for User.
 */
data class UserDTO(
    val id: Long?,
    val uuid: UUID,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val fullName: String,
    val title: String?,
    val timezone: String?,
    val phoneOffice: String?,
    val phoneMobile: String?,
    val phoneFax: String?,
    val phoneExt: String?,
    val avatarUrl: String?,
    val accessNewProjects: Boolean,
    val isActive: Boolean,
    val isStaff: Boolean,
    val isSuperuser: Boolean,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val lastLogin: LocalDateTime?
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val title: String? = null,
    val timezone: String? = null,
    val phoneOffice: String? = null,
    val phoneMobile: String? = null,
    val phoneFax: String? = null,
    val phoneExt: String? = null,
    val accessNewProjects: Boolean = true
)

data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val title: String? = null,
    val timezone: String? = null,
    val phoneOffice: String? = null,
    val phoneMobile: String? = null,
    val phoneFax: String? = null,
    val phoneExt: String? = null,
    val avatarUrl: String? = null,
    val accessNewProjects: Boolean? = null,
    val isActive: Boolean? = null,
    val isStaff: Boolean? = null,
    val isSuperuser: Boolean? = null
)
