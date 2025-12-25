package com.revet.documents.repository

import com.revet.documents.domain.Project
import com.revet.documents.repository.entity.OrganizationEntity
import com.revet.documents.repository.entity.ProjectEntity
import com.revet.documents.repository.entity.UserEntity
import com.revet.documents.repository.mapper.ProjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

/**
 * Repository interface for Project persistence operations.
 */
interface ProjectRepository {
    fun findAll(includeInactive: Boolean = false): List<com.revet.documents.domain.Project>
    fun findById(id: Long): com.revet.documents.domain.Project?
    fun findByUuid(uuid: UUID): com.revet.documents.domain.Project?
    fun findByOrganizationId(organizationId: Long, includeInactive: Boolean = false): List<com.revet.documents.domain.Project>
    fun save(project: com.revet.documents.domain.Project): com.revet.documents.domain.Project
    fun delete(id: Long): Boolean
}

/**
 * Panache-based implementation of ProjectRepository.
 */
@ApplicationScoped
class ProjectRepositoryImpl : com.revet.documents.repository.ProjectRepository {

    override fun findAll(includeInactive: Boolean): List<com.revet.documents.domain.Project> {
        val entities = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.listAll()
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.list("isActive", true)
        }
        return entities.map { _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.toDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.Project? {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.findById(id)?.let { _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.toDomain(it) }
    }

    override fun findByUuid(uuid: UUID): com.revet.documents.domain.Project? {
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.find("uuid", uuid).firstResult()
            ?.let { _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.toDomain(it) }
    }

    override fun findByOrganizationId(organizationId: Long, includeInactive: Boolean): List<com.revet.documents.domain.Project> {
        val query = if (includeInactive) {
            "organization.id = ?1"
        } else {
            "organization.id = ?1 and isActive = true"
        }
        return _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.list(query, organizationId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.toDomain(it) }
    }

    @Transactional
    override fun save(project: com.revet.documents.domain.Project): com.revet.documents.domain.Project {
        val entity = if (project.isNew()) {
            // Create new entity
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.toEntity(project)

            // Set organization
            val organization = _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity.findById(project.organizationId)
                ?: throw IllegalArgumentException("Organization with id ${project.organizationId} not found")
            newEntity.organization = organization

            // Set clients
            if (project.clientIds.isNotEmpty()) {
                val clients = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.list("id in ?1", project.clientIds).toMutableSet()
                newEntity.clients = clients
            }

            newEntity.persist()
            newEntity
        } else {
            // Update existing entity
            val existing = _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.findById(project.id!!)
                ?: throw IllegalArgumentException("Project with id ${project.id} not found")

            _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.updateEntity(existing, project)

            // Update clients
            existing.clients.clear()
            if (project.clientIds.isNotEmpty()) {
                val clients = _root_ide_package_.com.revet.documents.repository.entity.UserEntity.list("id in ?1", project.clientIds)
                existing.clients.addAll(clients)
            }

            existing
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        val entity = _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.findById(id) ?: return false
        entity.isActive = false
        entity.removedAt = java.time.LocalDate.now()
        return true
    }
}
