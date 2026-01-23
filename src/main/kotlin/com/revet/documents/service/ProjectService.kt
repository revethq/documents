package com.revet.documents.service

import com.revet.documents.domain.Project
import com.revet.documents.permission.DocumentsUrn
import com.revet.documents.permission.PrebuiltPolicies
import com.revet.documents.repository.ProjectRepository
import com.revet.documents.security.CurrentUserService
import com.revethq.iam.permission.persistence.service.PolicyAttachmentService
import com.revethq.iam.permission.persistence.service.PolicyService
import com.revethq.iam.permission.web.filter.AuthorizationContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Service interface for Project business logic.
 */
interface ProjectService {
    fun getAllProjects(includeInactive: Boolean = false): List<Project>
    fun getProjectById(id: Long): Project?
    fun getProjectByUuid(uuid: UUID): Project?
    fun getProjectsByOrganizationId(organizationId: Long, includeInactive: Boolean = false): List<Project>
    fun createProject(
        name: String,
        organizationId: Long,
        description: String? = null,
        clientIds: Set<UUID> = emptySet(),
        tags: Set<String> = emptySet()
    ): Project
    fun updateProject(
        id: Long,
        name: String? = null,
        description: String? = null,
        clientIds: Set<UUID>? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null
    ): Project?
    fun updateProjectByUuid(
        uuid: UUID,
        name: String? = null,
        description: String? = null,
        clientIds: Set<UUID>? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null
    ): Project?
    fun addClientToProject(projectId: Long, clientId: UUID): Project?
    fun addClientToProjectByUuid(uuid: UUID, clientId: UUID): Project?
    fun removeClientFromProject(projectId: Long, clientId: UUID): Project?
    fun removeClientFromProjectByUuid(uuid: UUID, clientId: UUID): Project?
    fun addTagToProject(projectId: Long, tag: String): Project?
    fun addTagToProjectByUuid(uuid: UUID, tag: String): Project?
    fun removeTagFromProject(projectId: Long, tag: String): Project?
    fun removeTagFromProjectByUuid(uuid: UUID, tag: String): Project?
    fun deleteProject(id: Long): Boolean
    fun deleteProjectByUuid(uuid: UUID): Boolean
}

/**
 * Implementation of ProjectService with business logic.
 */
@ApplicationScoped
class ProjectServiceImpl @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val policyService: PolicyService,
    private val policyAttachmentService: PolicyAttachmentService,
    private val prebuiltPolicies: PrebuiltPolicies,
    private val urn: DocumentsUrn,
    private val authorizationContext: AuthorizationContext,
    private val currentUserService: CurrentUserService
) : ProjectService {

    override fun getAllProjects(includeInactive: Boolean): List<Project> {
        return projectRepository.findAll(includeInactive)
    }

    override fun getProjectById(id: Long): Project? {
        return projectRepository.findById(id)
    }

    override fun getProjectByUuid(uuid: UUID): Project? {
        return projectRepository.findByUuid(uuid)
    }

    override fun getProjectsByOrganizationId(organizationId: Long, includeInactive: Boolean): List<Project> {
        return projectRepository.findByOrganizationId(organizationId, includeInactive)
    }

    override fun createProject(
        name: String,
        organizationId: Long,
        description: String?,
        clientIds: Set<UUID>,
        tags: Set<String>
    ): Project {
        require(name.isNotBlank()) { "Project name cannot be blank" }

        val project = Project.create(
            name = name,
            organizationId = organizationId,
            description = description,
            clientIds = clientIds,
            tags = tags
        )

        val savedProject = projectRepository.save(project)

        // Create default policies for the project
        createDefaultPolicies(savedProject)

        return savedProject
    }

    private fun createDefaultPolicies(project: Project) {
        val tenantId = authorizationContext.tenantId ?: return
        val currentUserUuid = currentUserService.getCurrentUserUuid() ?: return

        // Create admin policy and attach to creating user
        val adminPolicy = prebuiltPolicies.projectAdminPolicy(tenantId, project.uuid)
        val savedAdminPolicy = policyService.create(adminPolicy)
        policyAttachmentService.attach(
            savedAdminPolicy.id,
            urn.userPrincipal(tenantId, currentUserUuid),
            tenantId
        )

        // Create manager policy (unattached for later assignment)
        val managerPolicy = prebuiltPolicies.projectManagerPolicyForResource(tenantId, project.uuid)
        policyService.create(managerPolicy)

        // Create editor policy (unattached for later assignment)
        val editorPolicy = prebuiltPolicies.projectEditorPolicyForResource(tenantId, project.uuid)
        policyService.create(editorPolicy)

        // Create viewer policy (unattached for later assignment)
        val viewerPolicy = prebuiltPolicies.projectViewerPolicyForResource(tenantId, project.uuid)
        policyService.create(viewerPolicy)
    }

    override fun updateProject(
        id: Long,
        name: String?,
        description: String?,
        clientIds: Set<UUID>?,
        tags: Set<String>?,
        isActive: Boolean?
    ): Project? {
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

    override fun addClientToProject(projectId: Long, clientId: UUID): Project? {
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.addClient(clientId)
        return projectRepository.save(updated)
    }

    override fun removeClientFromProject(projectId: Long, clientId: UUID): Project? {
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.removeClient(clientId)
        return projectRepository.save(updated)
    }

    override fun addTagToProject(projectId: Long, tag: String): Project? {
        require(tag.isNotBlank()) { "Tag cannot be blank" }
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.addTag(tag)
        return projectRepository.save(updated)
    }

    override fun removeTagFromProject(projectId: Long, tag: String): Project? {
        val project = projectRepository.findById(projectId) ?: return null
        val updated = project.removeTag(tag)
        return projectRepository.save(updated)
    }

    override fun deleteProject(id: Long): Boolean {
        return projectRepository.delete(id)
    }

    override fun updateProjectByUuid(
        uuid: UUID,
        name: String?,
        description: String?,
        clientIds: Set<UUID>?,
        tags: Set<String>?,
        isActive: Boolean?
    ): Project? {
        val project = projectRepository.findByUuid(uuid) ?: return null
        return updateProject(project.id!!, name, description, clientIds, tags, isActive)
    }

    override fun addClientToProjectByUuid(uuid: UUID, clientId: UUID): Project? {
        val project = projectRepository.findByUuid(uuid) ?: return null
        return addClientToProject(project.id!!, clientId)
    }

    override fun removeClientFromProjectByUuid(uuid: UUID, clientId: UUID): Project? {
        val project = projectRepository.findByUuid(uuid) ?: return null
        return removeClientFromProject(project.id!!, clientId)
    }

    override fun addTagToProjectByUuid(uuid: UUID, tag: String): Project? {
        val project = projectRepository.findByUuid(uuid) ?: return null
        return addTagToProject(project.id!!, tag)
    }

    override fun removeTagFromProjectByUuid(uuid: UUID, tag: String): Project? {
        val project = projectRepository.findByUuid(uuid) ?: return null
        return removeTagFromProject(project.id!!, tag)
    }

    override fun deleteProjectByUuid(uuid: UUID): Boolean {
        val project = projectRepository.findByUuid(uuid) ?: return false
        return projectRepository.delete(project.id!!)
    }
}
