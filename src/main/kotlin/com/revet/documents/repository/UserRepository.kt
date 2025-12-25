package com.revet.documents.repository

import com.revet.documents.domain.User
import com.revet.documents.repository.entity.UserEntity
import com.revet.documents.repository.mapper.UserMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

/**
 * Repository interface for User persistence operations.
 */
interface UserRepository {
    fun findAll(includeInactive: Boolean = false): List<com.revet.documents.domain.User>
    fun findById(id: Long): com.revet.documents.domain.User?
    fun findByUuid(uuid: UUID): com.revet.documents.domain.User?
    fun findByUsername(username: String): com.revet.documents.domain.User?
    fun findByEmail(email: String): com.revet.documents.domain.User?
    fun save(user: com.revet.documents.domain.User): com.revet.documents.domain.User
    fun delete(id: Long): Boolean
}

/**
 * Panache-based implementation of UserRepository.
 */
@ApplicationScoped
class UserRepositoryImpl : com.revet.documents.repository.UserRepository {

    override fun findAll(includeInactive: Boolean): List<com.revet.documents.domain.User> {
        val entities = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.UserEntity.listAll()
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.UserEntity.list("isActive", true)
        }
        return entities.map { _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.toDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.User? {
        return _root_ide_package_.com.revet.documents.repository.entity.UserEntity.findById(id)?.let { _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.toDomain(it) }
    }

    override fun findByUuid(uuid: UUID): com.revet.documents.domain.User? {
        return _root_ide_package_.com.revet.documents.repository.entity.UserEntity.find("uuid", uuid).firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.toDomain(it) }
    }

    override fun findByUsername(username: String): com.revet.documents.domain.User? {
        return _root_ide_package_.com.revet.documents.repository.entity.UserEntity.find("username", username).firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.toDomain(it) }
    }

    override fun findByEmail(email: String): com.revet.documents.domain.User? {
        return _root_ide_package_.com.revet.documents.repository.entity.UserEntity.find("email", email).firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.toDomain(it) }
    }

    @Transactional
    override fun save(user: com.revet.documents.domain.User): com.revet.documents.domain.User {
        val entity = if (user.isNew()) {
            _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.toEntity(user).also { it.persist() }
        } else {
            val existing = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.findById(user.id!!)
                ?: throw IllegalArgumentException("User with id ${user.id} not found")
            _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.updateEntity(existing, user)
            existing
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.UserMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        val entity = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.findById(id) ?: return false
        entity.isActive = false
        return true
    }
}
