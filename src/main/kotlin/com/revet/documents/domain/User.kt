package com.revet.documents.domain

import java.time.LocalDateTime
import java.util.*

/**
 * Core domain model for User.
 * Represents a user in the Kala system with authentication and profile information.
 */
data class User(
    val id: Long?,
    val uuid: UUID,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val profile: UserProfile,
    val isActive: Boolean,
    val isStaff: Boolean,
    val isSuperuser: Boolean,
    val timestamps: Timestamps
) {
    data class UserProfile(
        val title: String?,
        val timezone: String?,
        val phoneOffice: String?,
        val phoneMobile: String?,
        val phoneFax: String?,
        val phoneExt: String?,
        val avatarUrl: String?,
        val accessNewProjects: Boolean
    )

    data class Timestamps(
        val createdAt: LocalDateTime,
        val modifiedAt: LocalDateTime,
        val lastLogin: LocalDateTime?
    )

    companion object {
        fun create(
            username: String,
            email: String,
            firstName: String? = null,
            lastName: String? = null,
            profile: UserProfile = UserProfile(null, null, null, null, null, null, null, true)
        ): User {
            val now = LocalDateTime.now()
            return User(
                id = null,
                uuid = UUID.randomUUID(),
                username = username,
                email = email,
                firstName = firstName,
                lastName = lastName,
                profile = profile,
                isActive = true,
                isStaff = false,
                isSuperuser = false,
                timestamps = Timestamps(
                    createdAt = now,
                    modifiedAt = now,
                    lastLogin = null
                )
            )
        }
    }

    fun update(
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        profile: UserProfile? = null,
        isActive: Boolean? = null,
        isStaff: Boolean? = null,
        isSuperuser: Boolean? = null
    ): User {
        return this.copy(
            email = email ?: this.email,
            firstName = firstName ?: this.firstName,
            lastName = lastName ?: this.lastName,
            profile = profile ?: this.profile,
            isActive = isActive ?: this.isActive,
            isStaff = isStaff ?: this.isStaff,
            isSuperuser = isSuperuser ?: this.isSuperuser,
            timestamps = this.timestamps.copy(modifiedAt = LocalDateTime.now())
        )
    }

    fun updateLastLogin(): User {
        return this.copy(
            timestamps = this.timestamps.copy(lastLogin = LocalDateTime.now())
        )
    }

    fun deactivate(): User {
        return this.copy(
            isActive = false,
            timestamps = this.timestamps.copy(modifiedAt = LocalDateTime.now())
        )
    }

    fun fullName(): String {
        return when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> username
        }
    }

    fun isNew(): Boolean = id == null
}
