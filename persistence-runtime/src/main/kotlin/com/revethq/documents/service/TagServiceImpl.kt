package com.revethq.documents.service

import com.revethq.documents.domain.Tag
import com.revethq.documents.repository.TagRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Implementation of TagService with business logic.
 */
@ApplicationScoped
class TagServiceImpl
    @Inject
    constructor(
        private val tagRepository: TagRepository,
    ) : TagService {
        override fun getAllTags(): List<Tag> = tagRepository.findAll()

        override fun getTagById(id: Int): Tag? = tagRepository.findById(id)

        override fun getTagByName(name: String): Tag? = tagRepository.findByName(name)

        override fun getTagBySlug(slug: String): Tag? = tagRepository.findBySlug(slug)

        override fun createTag(
            name: String,
            slug: String?,
        ): Tag {
            require(name.isNotBlank()) { "Tag name cannot be blank" }
            require(name.length <= 100) { "Tag name cannot exceed 100 characters" }

            // Check for duplicate name
            tagRepository.findByName(name)?.let {
                throw IllegalArgumentException("Tag with name '$name' already exists")
            }

            val tag = Tag.create(name, slug)

            // Check for duplicate slug
            tagRepository.findBySlug(tag.slug)?.let {
                throw IllegalArgumentException("Tag with slug '${tag.slug}' already exists")
            }

            return tagRepository.save(tag)
        }

        override fun updateTag(
            id: Int,
            name: String?,
            slug: String?,
        ): Tag? {
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

        override fun deleteTag(id: Int): Boolean = tagRepository.delete(id)

        override fun getTagsForDocument(documentId: Long): List<Tag> = tagRepository.findTagsByDocumentId(documentId)

        override fun addTagToDocument(
            tagId: Int,
            documentId: Long,
        ): Boolean {
            val tag = tagRepository.findById(tagId) ?: return false
            tagRepository.addTagToDocument(tagId, documentId)
            return true
        }

        override fun removeTagFromDocument(
            tagId: Int,
            documentId: Long,
        ): Boolean = tagRepository.removeTagFromDocument(tagId, documentId)

        override fun getOrCreateTag(name: String): Tag = tagRepository.findByName(name) ?: createTag(name)
    }
