package com.revethq.documents.repository

import com.revethq.documents.domain.Organization
import com.revethq.documents.repository.entity.OrganizationEntity
import com.revethq.documents.repository.mapper.OrganizationMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Panache-based implementation of OrganizationRepository.
 * Converts between domain models and Panache entities.
 */
@ApplicationScoped
class OrganizationRepositoryImpl : OrganizationRepository {
    override fun findAll(includeInactive: Boolean): List<Organization> {
        val entities =
            if (includeInactive) {
                OrganizationEntity.listAll()
            } else {
                OrganizationEntity.list("isActive", true)
            }
        return entities.map { OrganizationMapper.toDomain(it) }
    }

    override fun findById(id: Long): Organization? =
        OrganizationEntity.findById(id)?.let {
            OrganizationMapper.toDomain(it)
        }

    override fun findByUuid(uuid: UUID): Organization? =
        OrganizationEntity
            .find("uuid", uuid)
            .firstResult()
            ?.let { OrganizationMapper.toDomain(it) }

    @Transactional
    override fun save(organization: Organization): Organization {
        val entity =
            if (organization.isNew()) {
                // Create new entity
                OrganizationMapper
                    .toEntity(organization)
                    .also { it.persist() }
            } else {
                // Update existing entity
                val existing =
                    OrganizationEntity
                        .findById(organization.id!!)
                        ?: throw IllegalArgumentException("Organization with id ${organization.id} not found")
                OrganizationMapper.updateEntity(existing, organization)
                existing
            }
        return OrganizationMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        val entity = OrganizationEntity.findById(id) ?: return false
        entity.isActive = false
        entity.removedAt = LocalDateTime.now()
        return true
    }
}
