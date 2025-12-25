package com.revet.documents.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Core domain model for Project.
 * Projects belong to Organizations and contain Documents.
 */
data class Project(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val description: String?,
    val organizationId: Long,
    val clientIds: Set<Long>,
    val tags: Set<String>,
    val isActive: Boolean,
    val timestamps: Timestamps
) {
    data class Timestamps(
        val createdAt: LocalDateTime,
        val modifiedAt: LocalDateTime,
        val removedAt: LocalDate?
    )

    companion object {
        fun create(
            name: String,
            organizationId: Long,
            description: String? = null,
            clientIds: Set<Long> = emptySet(),
            tags: Set<String> = emptySet()
        ): Project {
            val now = LocalDateTime.now()
            return Project(
                id = null,
                uuid = UUID.randomUUID(),
                name = name,
                description = description,
                organizationId = organizationId,
                clientIds = clientIds,
                tags = tags,
                isActive = true,
                timestamps = Timestamps(
                    createdAt = now,
                    modifiedAt = now,
                    removedAt = null
                )
            )
        }
    }

    fun update(
        name: String? = null,
        description: String? = null,
        clientIds: Set<Long>? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null
    ): Project {
        return this.copy(
            name = name ?: this.name,
            description = description ?: this.description,
            clientIds = clientIds ?: this.clientIds,
            tags = tags ?: this.tags,
            isActive = isActive ?: this.isActive,
            timestamps = this.timestamps.copy(modifiedAt = LocalDateTime.now())
        )
    }

    fun deactivate(): Project {
        return this.copy(
            isActive = false,
            timestamps = this.timestamps.copy(
                modifiedAt = LocalDateTime.now(),
                removedAt = LocalDate.now()
            )
        )
    }

    fun addClient(clientId: Long): Project {
        return this.copy(
            clientIds = this.clientIds + clientId,
            timestamps = this.timestamps.copy(modifiedAt = LocalDateTime.now())
        )
    }

    fun removeClient(clientId: Long): Project {
        return this.copy(
            clientIds = this.clientIds - clientId,
            timestamps = this.timestamps.copy(modifiedAt = LocalDateTime.now())
        )
    }

    fun addTag(tag: String): Project {
        return this.copy(
            tags = this.tags + tag,
            timestamps = this.timestamps.copy(modifiedAt = LocalDateTime.now())
        )
    }

    fun removeTag(tag: String): Project {
        return this.copy(
            tags = this.tags - tag,
            timestamps = this.timestamps.copy(modifiedAt = LocalDateTime.now())
        )
    }

    fun isNew(): Boolean = id == null
}
