package com.revethq.documents.repository

import com.revethq.documents.domain.Tag

interface TagRepository {
    fun findAll(): List<Tag>

    fun findById(id: Int): Tag?

    fun findByName(name: String): Tag?

    fun findBySlug(slug: String): Tag?

    fun save(tag: Tag): Tag

    fun delete(id: Int): Boolean

    fun findTagsByDocumentId(documentId: Long): List<Tag>

    fun addTagToDocument(
        tagId: Int,
        documentId: Long,
    )

    fun removeTagFromDocument(
        tagId: Int,
        documentId: Long,
    ): Boolean
}
