package com.revethq.documents.repository

import com.revethq.documents.domain.Project
import com.revethq.documents.repository.entity.OrganizationEntity
import com.revethq.documents.repository.entity.ProjectEntity
import com.revethq.documents.repository.mapper.ProjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Panache-based implementation of ProjectRepository.
 */
@ApplicationScoped
class ProjectRepositoryImpl : ProjectRepository {
    override fun findAll(includeInactive: Boolean): List<Project> {
        val entities =
            if (includeInactive) {
                ProjectEntity.listAll()
            } else {
                ProjectEntity.list("isActive", true)
            }
        return entities.map { ProjectMapper.toDomain(it) }
    }

    override fun findById(id: Long): Project? =
        ProjectEntity.findById(id)?.let {
            ProjectMapper.toDomain(it)
        }

    override fun findByUuid(uuid: UUID): Project? =
        ProjectEntity
            .find("uuid", uuid)
            .firstResult()
            ?.let { ProjectMapper.toDomain(it) }

    override fun findByOrganizationId(
        organizationId: Long,
        includeInactive: Boolean,
    ): List<Project> {
        val query =
            if (includeInactive) {
                "organization.id = ?1"
            } else {
                "organization.id = ?1 and isActive = true"
            }
        return ProjectEntity
            .list(query, organizationId)
            .map { ProjectMapper.toDomain(it) }
    }

    @Transactional
    override fun save(project: Project): Project {
        val entity =
            if (project.isNew()) {
                // Create new entity
                val newEntity = ProjectMapper.toEntity(project)

                // Set organization
                val organization =
                    OrganizationEntity
                        .findById(project.organizationId)
                        ?: throw IllegalArgumentException("Organization with id ${project.organizationId} not found")
                newEntity.organization = organization

                // Set clients
                newEntity.clientIds = project.clientIds.toMutableSet()

                newEntity.persist()
                newEntity
            } else {
                // Update existing entity
                val existing =
                    ProjectEntity
                        .findById(project.id!!)
                        ?: throw IllegalArgumentException("Project with id ${project.id} not found")

                ProjectMapper.updateEntity(existing, project)

                // Update clients
                existing.clientIds = project.clientIds.toMutableSet()

                existing
            }
        return ProjectMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        val entity = ProjectEntity.findById(id) ?: return false
        entity.isActive = false
        entity.removedAt = LocalDate.now()
        return true
    }
}
