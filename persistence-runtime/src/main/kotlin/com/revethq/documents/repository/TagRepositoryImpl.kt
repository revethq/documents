package com.revethq.documents.repository

import com.revethq.documents.domain.Tag
import com.revethq.documents.repository.entity.TagEntity
import com.revethq.documents.repository.entity.TaggedItemEntity
import com.revethq.documents.repository.mapper.TagMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/**
 * Panache-based implementation of TagRepository.
 */
@ApplicationScoped
class TagRepositoryImpl : TagRepository {
    override fun findAll(): List<Tag> =
        TagEntity.listAll().map {
            TagMapper.toDomain(it)
        }

    override fun findById(id: Int): Tag? =
        TagEntity.find("id = ?1", id).firstResult()?.let {
            TagMapper.toDomain(it)
        }

    override fun findByName(name: String): Tag? =
        TagEntity.find("name = ?1", name).firstResult()?.let {
            TagMapper.toDomain(it)
        }

    override fun findBySlug(slug: String): Tag? =
        TagEntity.find("slug = ?1", slug).firstResult()?.let {
            TagMapper.toDomain(it)
        }

    @Transactional
    override fun save(tag: Tag): Tag {
        val entity =
            if (tag.isNew()) {
                val newEntity = TagMapper.toEntity(tag)
                newEntity.persist()
                newEntity
            } else {
                val tagId = tag.id ?: throw IllegalArgumentException("Tag id cannot be null for update")
                val existing =
                    TagEntity
                        .find("id = ?1", tagId)
                        .firstResult()
                        ?: throw IllegalArgumentException("Tag with id $tagId not found")
                TagMapper.updateEntity(existing, tag)
            }
        return TagMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Int): Boolean {
        // First delete all tagged items referencing this tag
        TaggedItemEntity.delete("tag.id = ?1", id)
        return TagEntity.delete("id = ?1", id) > 0
    }

    override fun findTagsByDocumentId(documentId: Long): List<Tag> {
        val taggedItems =
            TaggedItemEntity.list(
                "contentTypeId = ?1 and objectId = ?2",
                TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
                documentId.toInt(),
            )
        return taggedItems.mapNotNull {
            it.tag?.let { tag ->
                TagMapper.toDomain(tag)
            }
        }
    }

    @Transactional
    override fun addTagToDocument(
        tagId: Int,
        documentId: Long,
    ) {
        // Check if already tagged
        val existing =
            TaggedItemEntity
                .find(
                    "contentTypeId = ?1 and objectId = ?2 and tag.id = ?3",
                    TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
                    documentId.toInt(),
                    tagId,
                ).firstResult()

        if (existing == null) {
            val tagEntity =
                TagEntity
                    .find("id = ?1", tagId)
                    .firstResult()
                    ?: throw IllegalArgumentException("Tag with id $tagId not found")

            val taggedItem =
                TaggedItemEntity().apply {
                    this.contentTypeId = TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID
                    this.objectId = documentId.toInt()
                    this.tag = tagEntity
                }
            taggedItem.persist()
        }
    }

    @Transactional
    override fun removeTagFromDocument(
        tagId: Int,
        documentId: Long,
    ): Boolean {
        val count =
            TaggedItemEntity.delete(
                "contentTypeId = ?1 and objectId = ?2 and tag.id = ?3",
                TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
                documentId.toInt(),
                tagId,
            )
        return count > 0
    }
}
