package com.revethq.documents.service

import com.revethq.documents.domain.Tag

/**
 * Service interface for Tag business logic.
 */
interface TagService {
    fun getAllTags(): List<Tag>

    fun getTagById(id: Int): Tag?

    fun getTagByName(name: String): Tag?

    fun getTagBySlug(slug: String): Tag?

    fun createTag(
        name: String,
        slug: String? = null,
    ): Tag

    fun updateTag(
        id: Int,
        name: String? = null,
        slug: String? = null,
    ): Tag?

    fun deleteTag(id: Int): Boolean

    fun getTagsForDocument(documentId: Long): List<Tag>

    fun addTagToDocument(
        tagId: Int,
        documentId: Long,
    ): Boolean

    fun removeTagFromDocument(
        tagId: Int,
        documentId: Long,
    ): Boolean

    fun getOrCreateTag(name: String): Tag
}
