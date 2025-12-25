package com.revet.documents.service

import com.revet.documents.domain.Project
import com.revet.documents.repository.ProjectRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Service interface for Project business logic.
 */
interface ProjectService {
    fun getAllProjects(includeInactive: Boolean = false): List<com.revet.documents.domain.Project>
    fun getProjectById(id: Long): com.revet.documents.domain.Project?
    fun getProjectByUuid(uuid: UUID): com.revet.documents.domain.Project?
    fun getProjectsByOrganizationId(organizationId: Long, includeInactive: Boolean = false): List<com.revet.documents.domain.Project>
    fun createProject(
        name: String,
        organizationId: Long,
        description: String? = null,
        clientIds: Set<Long> = emptySet(),
        tags: Set<String> = emptySet()
    ): com.revet.documents.domain.Project
    fun updateProject(
        id: Long,
        name: String? = null,
        description: String? = null,
        clientIds: Set<Long>? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null
    ): com.revet.documents.domain.Project?
    fun addClientToProject(projectId: Long, clientId: Long): com.revet.documents.domain.Project?
    fun removeClientFromProject(projectId: Long, clientId: Long): com.revet.documents.domain.Project?
    fun addTagToProject(projectId: Long, tag: String): com.revet.documents.domain.Project?
    fun removeTagFromProject(projectId: Long, tag: String): com.revet.documents.domain.Project?
    fun deleteProject(id: Long): Boolean
}

/**
 * Implementation of ProjectService with business logic.
 */
@ApplicationScoped
class ProjectServiceImpl @Inject constructor(
    private val projectRepository: com.revet.documents.repository.ProjectRepository
) : com.revet.documents.service.ProjectService {

    override fun getAllProjects(includeInactive: Boolean): List<com.revet.documents.domain.Project> {
        return projectRepository.findAll(includeInactive)
    }

    override fun getProjectById(id: Long): com.revet.documents.domain.Project? {
        return projectRepository.findById(id)
    }

    override fun getProjectByUuid(uuid: UUID): com.revet.documents.domain.Project? {
        return projectRepository.findByUuid(uuid)
    }

    override fun getProjectsByOrganizationId(organizationId: Long, includeInactive: Boolean): List<com.revet.documents.domain.Project> {
        return projectRepository.findByOrganizationId(organizationId, includeInactive)
    }

    override fun createProject(
        name: String,
        organizationId: Long,
        description: String?,
        clientIds: Set<Long>,
        tags: Set<String>
    ): com.revet.documents.domain.Project {
        require(name.isNotBlank()) { "Project name cannot be blank" }

        val project = _root_ide_package_.com.revet.documents.domain.Project.create(
            name = name,
            organizationId = organizationId,
            description = description,
            clientIds = clientIds,
            tags = tags
        )

        return projectRepository.save(project)
    }

    override fun updateProject(
        id: Long,
        name: String?,
        description: String?,
        clientIds: Set<Long>?,
        tags: Set<String>?,
        isActive: Boolean?
    ): com.revet.documents.domain.Project? {
        val existing = projectRepository.findById(id) ?: return null

        name?.let { require(it.isNotBlank()) { "Project name cannot be blank" } }

        val updated = existing.update(
            name = name,
            description = description,
            clientIds = clientIds,
            tags = tags,
            isActive = isActive
        )

        return projectRepository.save(updated)
    }

    override fun addClientToProject(projectId: Long, clientId: Long): com.revet.documents.domain.Project? {
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.addClient(clientId)
        return projectRepository.save(updated)
    }

    override fun removeClientFromProject(projectId: Long, clientId: Long): com.revet.documents.domain.Project? {
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.removeClient(clientId)
        return projectRepository.save(updated)
    }

    override fun addTagToProject(projectId: Long, tag: String): com.revet.documents.domain.Project? {
        require(tag.isNotBlank()) { "Tag cannot be blank" }
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.addTag(tag)
        return projectRepository.save(updated)
    }

    override fun removeTagFromProject(projectId: Long, tag: String): com.revet.documents.domain.Project? {
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.removeTag(tag)
        return projectRepository.save(updated)
    }

    override fun deleteProject(id: Long): Boolean {
        return projectRepository.delete(id)
    }
}
