package com.revethq.documents.repository

import com.revethq.documents.domain.Organization
import java.util.UUID

interface OrganizationRepository {
    fun findAll(includeInactive: Boolean = false): List<Organization>

    fun findById(id: Long): Organization?

    fun findByUuid(uuid: UUID): Organization?

    fun save(organization: Organization): Organization

    fun delete(id: Long): Boolean
}
