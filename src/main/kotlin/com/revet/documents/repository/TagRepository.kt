package com.revet.documents.repository

import com.revet.documents.domain.Tag
import com.revet.documents.repository.entity.TagEntity
import com.revet.documents.repository.entity.TaggedItemEntity
import com.revet.documents.repository.mapper.TagMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/**
 * Repository interface for Tag persistence operations.
 */
interface TagRepository {
    fun findAll(): List<com.revet.documents.domain.Tag>
    fun findById(id: Int): com.revet.documents.domain.Tag?
    fun findByName(name: String): com.revet.documents.domain.Tag?
    fun findBySlug(slug: String): com.revet.documents.domain.Tag?
    fun save(tag: com.revet.documents.domain.Tag): com.revet.documents.domain.Tag
    fun delete(id: Int): Boolean

    fun findTagsByDocumentId(documentId: Long): List<com.revet.documents.domain.Tag>
    fun addTagToDocument(tagId: Int, documentId: Long)
    fun removeTagFromDocument(tagId: Int, documentId: Long): Boolean
}

/**
 * Panache-based implementation of TagRepository.
 */
@ApplicationScoped
class TagRepositoryImpl : com.revet.documents.repository.TagRepository {

    override fun findAll(): List<com.revet.documents.domain.Tag> {
        return _root_ide_package_.com.revet.documents.repository.entity.TagEntity.listAll().map { _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.toDomain(it) }
    }

    override fun findById(id: Int): com.revet.documents.domain.Tag? {
        return _root_ide_package_.com.revet.documents.repository.entity.TagEntity.find("id = ?1", id).firstResult()?.let { _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.toDomain(it) }
    }

    override fun findByName(name: String): com.revet.documents.domain.Tag? {
        return _root_ide_package_.com.revet.documents.repository.entity.TagEntity.find("name = ?1", name).firstResult()?.let { _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.toDomain(it) }
    }

    override fun findBySlug(slug: String): com.revet.documents.domain.Tag? {
        return _root_ide_package_.com.revet.documents.repository.entity.TagEntity.find("slug = ?1", slug).firstResult()?.let { _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.toDomain(it) }
    }

    @Transactional
    override fun save(tag: com.revet.documents.domain.Tag): com.revet.documents.domain.Tag {
        val entity = if (tag.isNew()) {
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.toEntity(tag)
            newEntity.persist()
            newEntity
        } else {
            val tagId = tag.id ?: throw IllegalArgumentException("Tag id cannot be null for update")
            val existing = _root_ide_package_.com.revet.documents.repository.entity.TagEntity.find("id = ?1", tagId).firstResult()
                ?: throw IllegalArgumentException("Tag with id $tagId not found")
            _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.updateEntity(existing, tag)
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Int): Boolean {
        // First delete all tagged items referencing this tag
        _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.delete("tag.id = ?1", id)
        return _root_ide_package_.com.revet.documents.repository.entity.TagEntity.delete("id = ?1", id) > 0
    }

    override fun findTagsByDocumentId(documentId: Long): List<com.revet.documents.domain.Tag> {
        val taggedItems = _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.list(
            "contentTypeId = ?1 and objectId = ?2",
            _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
            documentId.toInt()
        )
        return taggedItems.mapNotNull { it.tag?.let { tag -> _root_ide_package_.com.revet.documents.repository.mapper.TagMapper.toDomain(tag) } }
    }

    @Transactional
    override fun addTagToDocument(tagId: Int, documentId: Long) {
        // Check if already tagged
        val existing = _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.find(
            "contentTypeId = ?1 and objectId = ?2 and tag.id = ?3",
            _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
            documentId.toInt(),
            tagId
        ).firstResult()

        if (existing == null) {
            val tagEntity = _root_ide_package_.com.revet.documents.repository.entity.TagEntity.find("id = ?1", tagId).firstResult()
                ?: throw IllegalArgumentException("Tag with id $tagId not found")

            val taggedItem = _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity().apply {
                this.contentTypeId = _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID
                this.objectId = documentId.toInt()
                this.tag = tagEntity
            }
            taggedItem.persist()
        }
    }

    @Transactional
    override fun removeTagFromDocument(tagId: Int, documentId: Long): Boolean {
        val count = _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.delete(
            "contentTypeId = ?1 and objectId = ?2 and tag.id = ?3",
            _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
            documentId.toInt(),
            tagId
        )
        return count > 0
    }
}
