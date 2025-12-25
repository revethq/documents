package com.revet.documents.domain

/**
 * Core domain model for Category.
 * Categories are used to organize documents within a project.
 */
data class Category(
    val id: Long?,
    val name: String,
    val projectId: Long
) {
    companion object {
        fun create(
            name: String,
            projectId: Long
        ): Category {
            return Category(
                id = null,
                name = name,
                projectId = projectId
            )
        }
    }

    fun update(name: String): Category {
        return this.copy(name = name)
    }

    fun isNew(): Boolean = id == null
}
