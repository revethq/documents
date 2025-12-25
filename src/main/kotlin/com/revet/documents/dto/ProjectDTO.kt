package com.revet.documents.dto

import java.time.LocalDateTime
import java.util.*

/**
 * Data Transfer Object for Project.
 */
data class ProjectDTO(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val description: String?,
    val organizationId: Long,
    val clientIds: Set<Long>,
    val tags: Set<String>,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
)

data class CreateProjectRequest(
    val name: String,
    val organizationId: Long,
    val description: String? = null,
    val clientIds: Set<Long> = emptySet(),
    val tags: Set<String> = emptySet()
)

data class UpdateProjectRequest(
    val name: String? = null,
    val description: String? = null,
    val clientIds: Set<Long>? = null,
    val tags: Set<String>? = null,
    val isActive: Boolean? = null
)

data class AddClientRequest(
    val clientId: Long
)

data class RemoveClientRequest(
    val clientId: Long
)

data class AddTagRequest(
    val tag: String
)

data class RemoveTagRequest(
    val tag: String
)
