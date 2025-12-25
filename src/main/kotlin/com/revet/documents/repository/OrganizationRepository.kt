package com.revet.documents.repository

import com.revet.documents.domain.Organization
import com.revet.documents.repository.entity.OrganizationEntity
import com.revet.documents.repository.mapper.OrganizationMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

/**
 * Repository interface for Organization persistence operations.
 */
interface OrganizationRepository {
    fun findAll(includeInactive: Boolean = false): List<com.revet.documents.domain.Organization>
    fun findById(id: Long): com.revet.documents.domain.Organization?
    fun findByUuid(uuid: UUID): com.revet.documents.domain.Organization?
    fun save(organization: com.revet.documents.domain.Organization): com.revet.documents.domain.Organization
    fun delete(id: Long): Boolean
}

/**
 * Panache-based implementation of OrganizationRepository.
 * Converts between domain models and Panache entities.
 */
@ApplicationScoped
class OrganizationRepositoryImpl : com.revet.documents.repository.OrganizationRepository {

    override fun findAll(includeInactive: Boolean): List<com.revet.documents.domain.Organization> {
        val entities = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.listAll()
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.list("isActive", true)
        }
        return entities.map { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationMapper.toDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.Organization? {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.findById(id)?.let { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationMapper.toDomain(it) }
    }

    override fun findByUuid(uuid: UUID): com.revet.documents.domain.Organization? {
        return _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.find("uuid", uuid).firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationMapper.toDomain(it) }
    }

    @Transactional
    override fun save(organization: com.revet.documents.domain.Organization): com.revet.documents.domain.Organization {
        val entity = if (organization.isNew()) {
            // Create new entity
            _root_ide_package_.com.revet.documents.repository.mapper.OrganizationMapper.toEntity(organization).also { it.persist() }
        } else {
            // Update existing entity
            val existing = _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.findById(organization.id!!)
                ?: throw IllegalArgumentException("Organization with id ${organization.id} not found")
            _root_ide_package_.com.revet.documents.repository.mapper.OrganizationMapper.updateEntity(existing, organization)
            existing
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.OrganizationMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        val entity = _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.findById(id) ?: return false
        entity.isActive = false
        entity.removedAt = java.time.LocalDateTime.now()
        return true
    }
}
