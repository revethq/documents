package com.revethq.documents.domain

/**
 * Core domain model for Category.
 * Categories are used to organize documents within a project.
 */
data class Category(
    val id: Long?,
    val name: String,
    val projectId: Long,
) {
    companion object {
        fun create(
            name: String,
            projectId: Long,
        ): Category =
            Category(
                id = null,
                name = name,
                projectId = projectId,
            )
    }

    fun update(name: String): Category = this.copy(name = name)

    fun isNew(): Boolean = id == null
}
