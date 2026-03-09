package com.revethq.documents.repository

import com.revethq.documents.domain.Project
import java.util.UUID

interface ProjectRepository {
    fun findAll(includeInactive: Boolean = false): List<Project>

    fun findById(id: Long): Project?

    fun findByUuid(uuid: UUID): Project?

    fun findByOrganizationId(
        organizationId: Long,
        includeInactive: Boolean = false,
    ): List<Project>

    fun save(project: Project): Project

    fun delete(id: Long): Boolean
}
