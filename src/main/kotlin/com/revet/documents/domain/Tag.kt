package com.revet.documents.domain

/**
 * Core domain model for Tag.
 */
data class Tag(
    val id: Int?,
    val name: String,
    val slug: String
) {
    companion object {
        fun create(name: String, slug: String? = null): Tag {
            return Tag(
                id = null,
                name = name,
                slug = slug ?: name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            )
        }
    }

    fun update(name: String? = null, slug: String? = null): Tag {
        return this.copy(
            name = name ?: this.name,
            slug = slug ?: this.slug
        )
    }

    fun isNew(): Boolean = id == null
}
