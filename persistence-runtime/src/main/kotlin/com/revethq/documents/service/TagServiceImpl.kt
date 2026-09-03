package com.revethq.documents.service

import com.revethq.core.Tag
import com.revethq.core.repository.TagRepository
import com.revethq.core.repository.TaggedItemRepository
import com.revethq.documents.permission.Actions
import com.revethq.documents.permission.DocumentsUrn
import com.revethq.iam.permission.web.filter.AuthorizationContext
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
        private val taggedItemRepository: TaggedItemRepository,
        private val permissionFilterService: PermissionFilterService,
        private val urn: DocumentsUrn,
        private val authorizationContext: AuthorizationContext,
    ) : TagService {
        override fun getAllTags(organizationId: Long): List<Tag> {
            val tags = tagRepository.findAllByOrganizationId(organizationId)
            val tenantId = authorizationContext.tenantId ?: ""
            return permissionFilterService.filter(tags, Actions.Tag.GET) { tag ->
                urn.tag(tenantId, tag.id!!.toLong())
            }
        }

        override fun getTagById(id: Int): Tag? = tagRepository.findById(id)

        override fun getTagByName(
            name: String,
            organizationId: Long,
        ): Tag? = tagRepository.findByName(name, organizationId)

        override fun getTagBySlug(
            slug: String,
            organizationId: Long,
        ): Tag? = tagRepository.findBySlug(slug, organizationId)

        override fun createTag(
            name: String,
            organizationId: Long,
            slug: String?,
        ): Tag {
            require(name.isNotBlank()) { "Tag name cannot be blank" }
            require(name.length <= 100) { "Tag name cannot exceed 100 characters" }

            // Check for duplicate name
            tagRepository.findByName(name, organizationId)?.let {
                throw IllegalArgumentException("Tag with name '$name' already exists")
            }

            val tag = Tag.create(name, organizationId, slug)

            // Check for duplicate slug
            tagRepository.findBySlug(tag.slug, organizationId)?.let {
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
                tagRepository.findByName(it, existing.organizationId)?.let { found ->
                    if (found.id != id) {
                        throw IllegalArgumentException("Tag with name '$it' already exists")
                    }
                }
            }

            slug?.let {
                // Check for duplicate slug (excluding current tag)
                tagRepository.findBySlug(it, existing.organizationId)?.let { found ->
                    if (found.id != id) {
                        throw IllegalArgumentException("Tag with slug '$it' already exists")
                    }
                }
            }

            val updated = existing.update(name = name, slug = slug)
            return tagRepository.save(updated)
        }

        override fun deleteTag(id: Int): Boolean {
            taggedItemRepository.deleteByTagId(id)
            return tagRepository.delete(id)
        }

        override fun getTagsForResource(resourceUrn: String): List<Tag> = taggedItemRepository.findTagsByResource(resourceUrn)

        override fun addTagToResource(
            tagId: Int,
            resourceUrn: String,
        ): Boolean {
            tagRepository.findById(tagId) ?: return false
            taggedItemRepository.addTagToResource(tagId, resourceUrn)
            return true
        }

        override fun removeTagFromResource(
            tagId: Int,
            resourceUrn: String,
        ): Boolean = taggedItemRepository.removeTagFromResource(tagId, resourceUrn)

        override fun getOrCreateTag(
            name: String,
            organizationId: Long,
        ): Tag = tagRepository.findByName(name, organizationId) ?: createTag(name, organizationId)
    }
