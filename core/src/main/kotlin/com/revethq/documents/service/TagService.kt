package com.revethq.documents.service

import com.revethq.core.Tag

/**
 * Service interface for Tag business logic.
 */
interface TagService {
    fun getAllTags(organizationId: Long): List<Tag>

    fun getTagById(id: Int): Tag?

    fun getTagByName(
        name: String,
        organizationId: Long,
    ): Tag?

    fun getTagBySlug(
        slug: String,
        organizationId: Long,
    ): Tag?

    fun createTag(
        name: String,
        organizationId: Long,
        slug: String? = null,
    ): Tag

    fun updateTag(
        id: Int,
        name: String? = null,
        slug: String? = null,
    ): Tag?

    fun deleteTag(id: Int): Boolean

    fun getTagsForResource(resourceUrn: String): List<Tag>

    fun addTagToResource(
        tagId: Int,
        resourceUrn: String,
    ): Boolean

    fun removeTagFromResource(
        tagId: Int,
        resourceUrn: String,
    ): Boolean

    fun getOrCreateTag(
        name: String,
        organizationId: Long,
    ): Tag
}
