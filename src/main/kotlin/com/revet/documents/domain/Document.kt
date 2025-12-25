package com.revet.documents.domain

import java.time.LocalDateTime
import java.util.*

/**
 * Core domain model for Document.
 * Documents belong to Projects and can have multiple versions.
 */
data class Document(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val projectId: Long,
    val categoryId: Long?,
    val mime: String?,
    val date: LocalDateTime,
    val tags: Set<String>,
    val isActive: Boolean,
    val removedAt: LocalDateTime?
) {
    companion object {
        fun create(
            name: String,
            projectId: Long,
            categoryId: Long? = null,
            mime: String? = null,
            tags: Set<String> = emptySet()
        ): Document {
            return Document(
                id = null,
                uuid = UUID.randomUUID(),
                name = name,
                projectId = projectId,
                categoryId = categoryId,
                mime = mime,
                date = LocalDateTime.now(),
                tags = tags,
                isActive = true,
                removedAt = null
            )
        }
    }

    fun update(
        name: String? = null,
        categoryId: Long? = null,
        mime: String? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null
    ): Document {
        return this.copy(
            name = name ?: this.name,
            categoryId = categoryId ?: this.categoryId,
            mime = mime ?: this.mime,
            tags = tags ?: this.tags,
            isActive = isActive ?: this.isActive
        )
    }

    fun deactivate(): Document {
        return this.copy(
            isActive = false,
            removedAt = LocalDateTime.now()
        )
    }

    fun addTag(tag: String): Document {
        return this.copy(tags = this.tags + tag)
    }

    fun removeTag(tag: String): Document {
        return this.copy(tags = this.tags - tag)
    }

    fun isNew(): Boolean = id == null
}
