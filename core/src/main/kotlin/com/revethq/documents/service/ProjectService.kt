package com.revethq.documents.service

import com.revethq.documents.domain.Project
import java.util.UUID

/**
 * Service interface for Project business logic.
 */
interface ProjectService {
    fun getAllProjects(includeInactive: Boolean = false): List<Project>

    fun getProjectById(id: Long): Project?

    fun getProjectByUuid(uuid: UUID): Project?

    fun getProjectsByOrganizationId(
        organizationId: Long,
        includeInactive: Boolean = false,
    ): List<Project>

    fun createProject(
        name: String,
        organizationId: Long,
        description: String? = null,
        clientIds: Set<UUID> = emptySet(),
        tags: Set<String> = emptySet(),
    ): Project

    fun updateProject(
        id: Long,
        name: String? = null,
        description: String? = null,
        clientIds: Set<UUID>? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null,
    ): Project?

    fun updateProjectByUuid(
        uuid: UUID,
        name: String? = null,
        description: String? = null,
        clientIds: Set<UUID>? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null,
    ): Project?

    fun addClientToProject(
        projectId: Long,
        clientId: UUID,
    ): Project?

    fun addClientToProjectByUuid(
        uuid: UUID,
        clientId: UUID,
    ): Project?

    fun removeClientFromProject(
        projectId: Long,
        clientId: UUID,
    ): Project?

    fun removeClientFromProjectByUuid(
        uuid: UUID,
        clientId: UUID,
    ): Project?

    fun addTagToProject(
        projectId: Long,
        tag: String,
    ): Project?

    fun addTagToProjectByUuid(
        uuid: UUID,
        tag: String,
    ): Project?

    fun removeTagFromProject(
        projectId: Long,
        tag: String,
    ): Project?

    fun removeTagFromProjectByUuid(
        uuid: UUID,
        tag: String,
    ): Project?

    fun deleteProject(id: Long): Boolean

    fun deleteProjectByUuid(uuid: UUID): Boolean
}
