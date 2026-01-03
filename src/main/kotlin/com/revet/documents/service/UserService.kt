package com.revet.documents.service

import com.revet.documents.domain.User
import com.revet.documents.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Service interface for User business logic.
 */
interface UserService {
    fun getAllUsers(includeInactive: Boolean = false): List<com.revet.documents.domain.User>
    fun getUserById(id: Long): com.revet.documents.domain.User?
    fun getUserByUuid(uuid: UUID): com.revet.documents.domain.User?
    fun getUserByUsername(username: String): com.revet.documents.domain.User?
    fun getUserByEmail(email: String): com.revet.documents.domain.User?
    fun createUser(
        username: String,
        email: String,
        firstName: String? = null,
        lastName: String? = null,
        profile: com.revet.documents.domain.User.UserProfile = _root_ide_package_.com.revet.documents.domain.User.UserProfile(null, null, null, null, null, null, null, true)
    ): com.revet.documents.domain.User
    fun updateUser(
        id: Long,
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        profile: com.revet.documents.domain.User.UserProfile? = null,
        isActive: Boolean? = null,
        isStaff: Boolean? = null,
        isSuperuser: Boolean? = null
    ): com.revet.documents.domain.User?
    fun updateUserByUuid(
        uuid: UUID,
        email: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        profile: com.revet.documents.domain.User.UserProfile? = null,
        isActive: Boolean? = null,
        isStaff: Boolean? = null,
        isSuperuser: Boolean? = null
    ): com.revet.documents.domain.User?
    fun updateLastLogin(id: Long): com.revet.documents.domain.User?
    fun deleteUser(id: Long): Boolean
    fun deleteUserByUuid(uuid: UUID): Boolean
}

/**
 * Implementation of UserService with business logic.
 */
@ApplicationScoped
class UserServiceImpl @Inject constructor(
    private val userRepository: com.revet.documents.repository.UserRepository
) : com.revet.documents.service.UserService {

    override fun getAllUsers(includeInactive: Boolean): List<com.revet.documents.domain.User> {
        return userRepository.findAll(includeInactive)
    }

    override fun getUserById(id: Long): com.revet.documents.domain.User? {
        return userRepository.findById(id)
    }

    override fun getUserByUuid(uuid: UUID): com.revet.documents.domain.User? {
        return userRepository.findByUuid(uuid)
    }

    override fun getUserByUsername(username: String): com.revet.documents.domain.User? {
        return userRepository.findByUsername(username)
    }

    override fun getUserByEmail(email: String): com.revet.documents.domain.User? {
        return userRepository.findByEmail(email)
    }

    override fun createUser(
        username: String,
        email: String,
        firstName: String?,
        lastName: String?,
        profile: com.revet.documents.domain.User.UserProfile
    ): com.revet.documents.domain.User {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(isValidEmail(email)) { "Email must be valid" }

        // Check for existing username
        userRepository.findByUsername(username)?.let {
            throw IllegalArgumentException("Username already exists")
        }

        // Check for existing email
        userRepository.findByEmail(email)?.let {
            throw IllegalArgumentException("Email already exists")
        }

        val user = _root_ide_package_.com.revet.documents.domain.User.create(
            username = username,
            email = email,
            firstName = firstName,
            lastName = lastName,
            profile = profile
        )

        return userRepository.save(user)
    }

    override fun updateUser(
        id: Long,
        email: String?,
        firstName: String?,
        lastName: String?,
        profile: com.revet.documents.domain.User.UserProfile?,
        isActive: Boolean?,
        isStaff: Boolean?,
        isSuperuser: Boolean?
    ): com.revet.documents.domain.User? {
        val existing = userRepository.findById(id) ?: return null

        email?.let {
            require(it.isNotBlank()) { "Email cannot be blank" }
            require(isValidEmail(it)) { "Email must be valid" }

            // Check if email is already taken by another user
            userRepository.findByEmail(it)?.let { existingUser ->
                if (existingUser.id != id) {
                    throw IllegalArgumentException("Email already exists")
                }
            }
        }

        val updated = existing.update(
            email = email,
            firstName = firstName,
            lastName = lastName,
            profile = profile,
            isActive = isActive,
            isStaff = isStaff,
            isSuperuser = isSuperuser
        )

        return userRepository.save(updated)
    }

    override fun updateLastLogin(id: Long): com.revet.documents.domain.User? {
        val user = userRepository.findById(id) ?: return null
        val updated = user.updateLastLogin()
        return userRepository.save(updated)
    }

    override fun deleteUser(id: Long): Boolean {
        return userRepository.delete(id)
    }

    override fun updateUserByUuid(
        uuid: UUID,
        email: String?,
        firstName: String?,
        lastName: String?,
        profile: com.revet.documents.domain.User.UserProfile?,
        isActive: Boolean?,
        isStaff: Boolean?,
        isSuperuser: Boolean?
    ): com.revet.documents.domain.User? {
        val user = userRepository.findByUuid(uuid) ?: return null
        return updateUser(user.id!!, email, firstName, lastName, profile, isActive, isStaff, isSuperuser)
    }

    override fun deleteUserByUuid(uuid: UUID): Boolean {
        val user = userRepository.findByUuid(uuid) ?: return false
        return userRepository.delete(user.id!!)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"))
    }
}
