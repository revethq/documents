package com.revet.documents.service

import com.revet.documents.domain.Tag
import com.revet.documents.repository.TagRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Service interface for Tag business logic.
 */
interface TagService {
    fun getAllTags(): List<com.revet.documents.domain.Tag>
    fun getTagById(id: Int): com.revet.documents.domain.Tag?
    fun getTagByName(name: String): com.revet.documents.domain.Tag?
    fun getTagBySlug(slug: String): com.revet.documents.domain.Tag?
    fun createTag(name: String, slug: String? = null): com.revet.documents.domain.Tag
    fun updateTag(id: Int, name: String? = null, slug: String? = null): com.revet.documents.domain.Tag?
    fun deleteTag(id: Int): Boolean

    fun getTagsForDocument(documentId: Long): List<com.revet.documents.domain.Tag>
    fun addTagToDocument(tagId: Int, documentId: Long): Boolean
    fun removeTagFromDocument(tagId: Int, documentId: Long): Boolean
    fun getOrCreateTag(name: String): com.revet.documents.domain.Tag
}

/**
 * Implementation of TagService with business logic.
 */
@ApplicationScoped
class TagServiceImpl @Inject constructor(
    private val tagRepository: com.revet.documents.repository.TagRepository
) : com.revet.documents.service.TagService {

    override fun getAllTags(): List<com.revet.documents.domain.Tag> {
        return tagRepository.findAll()
    }

    override fun getTagById(id: Int): com.revet.documents.domain.Tag? {
        return tagRepository.findById(id)
    }

    override fun getTagByName(name: String): com.revet.documents.domain.Tag? {
        return tagRepository.findByName(name)
    }

    override fun getTagBySlug(slug: String): com.revet.documents.domain.Tag? {
        return tagRepository.findBySlug(slug)
    }

    override fun createTag(name: String, slug: String?): com.revet.documents.domain.Tag {
        require(name.isNotBlank()) { "Tag name cannot be blank" }
        require(name.length <= 100) { "Tag name cannot exceed 100 characters" }

        // Check for duplicate name
        tagRepository.findByName(name)?.let {
            throw IllegalArgumentException("Tag with name '$name' already exists")
        }

        val tag = _root_ide_package_.com.revet.documents.domain.Tag.create(name, slug)

        // Check for duplicate slug
        tagRepository.findBySlug(tag.slug)?.let {
            throw IllegalArgumentException("Tag with slug '${tag.slug}' already exists")
        }

        return tagRepository.save(tag)
    }

    override fun updateTag(id: Int, name: String?, slug: String?): com.revet.documents.domain.Tag? {
        val existing = tagRepository.findById(id) ?: return null

        name?.let {
            require(it.isNotBlank()) { "Tag name cannot be blank" }
            require(it.length <= 100) { "Tag name cannot exceed 100 characters" }

            // Check for duplicate name (excluding current tag)
            tagRepository.findByName(it)?.let { found ->
                if (found.id != id) {
                    throw IllegalArgumentException("Tag with name '$it' already exists")
                }
            }
        }

        slug?.let {
            // Check for duplicate slug (excluding current tag)
            tagRepository.findBySlug(it)?.let { found ->
                if (found.id != id) {
                    throw IllegalArgumentException("Tag with slug '$it' already exists")
                }
            }
        }

        val updated = existing.update(name = name, slug = slug)
        return tagRepository.save(updated)
    }

    override fun deleteTag(id: Int): Boolean {
        return tagRepository.delete(id)
    }

    override fun getTagsForDocument(documentId: Long): List<com.revet.documents.domain.Tag> {
        return tagRepository.findTagsByDocumentId(documentId)
    }

    override fun addTagToDocument(tagId: Int, documentId: Long): Boolean {
        val tag = tagRepository.findById(tagId) ?: return false
        tagRepository.addTagToDocument(tagId, documentId)
        return true
    }

    override fun removeTagFromDocument(tagId: Int, documentId: Long): Boolean {
        return tagRepository.removeTagFromDocument(tagId, documentId)
    }

    override fun getOrCreateTag(name: String): com.revet.documents.domain.Tag {
        return tagRepository.findByName(name) ?: createTag(name)
    }
}
