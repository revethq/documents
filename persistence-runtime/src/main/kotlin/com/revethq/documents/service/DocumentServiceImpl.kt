package com.revethq.documents.service

import com.revethq.core.Tag
import com.revethq.core.repository.ProjectRepository
import com.revethq.core.repository.TagRepository
import com.revethq.core.repository.TaggedItemRepository
import com.revethq.documents.domain.Document
import com.revethq.documents.domain.Page
import com.revethq.documents.domain.PageRequest
import com.revethq.documents.permission.Actions
import com.revethq.documents.permission.DocumentsUrn
import com.revethq.documents.repository.DocumentRepository
import com.revethq.iam.permission.web.filter.AuthorizationContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.UUID

/**
 * Implementation of DocumentService with business logic.
 */
@ApplicationScoped
class DocumentServiceImpl
    @Inject
    constructor(
        private val documentRepository: DocumentRepository,
        private val tagRepository: TagRepository,
        private val taggedItemRepository: TaggedItemRepository,
        private val projectRepository: ProjectRepository,
        private val permissionFilterService: PermissionFilterService,
        private val urn: DocumentsUrn,
        private val authorizationContext: AuthorizationContext,
    ) : DocumentService {
        companion object {
            fun documentUrn(uuid: UUID): String = "urn:revet:documents::document/$uuid"
        }

        private fun resolveOrganizationId(projectId: Long): Long {
            val project =
                projectRepository.findById(projectId)
                    ?: throw IllegalArgumentException("Project with id $projectId not found")
            return project.organizationId
        }

        override fun getAllDocuments(includeInactive: Boolean): List<Document> {
            val documents = documentRepository.findAll(includeInactive)
            val tenantId = authorizationContext.tenantId ?: ""
            return permissionFilterService.filter(documents, Actions.Document.GET) { doc ->
                urn.document(tenantId, doc.uuid)
            }
        }

        override fun getDocumentById(id: Long): Document? = documentRepository.findById(id)

        override fun getDocumentByUuid(uuid: UUID): Document? = documentRepository.findByUuid(uuid)

        override fun getDocumentsByProjectId(
            projectId: Long,
            includeInactive: Boolean,
        ): List<Document> = documentRepository.findByProjectId(projectId, includeInactive)

        override fun getDocumentsByCategoryId(
            categoryId: Long,
            includeInactive: Boolean,
        ): List<Document> = documentRepository.findByCategoryId(categoryId, includeInactive)

        @Transactional
        override fun createDocument(
            name: String,
            projectId: Long,
            categoryId: Long?,
            mime: String?,
            tags: Set<String>,
        ): Document {
            require(name.isNotBlank()) { "Document name cannot be blank" }

            val document =
                Document.create(
                    name = name,
                    projectId = projectId,
                    categoryId = categoryId,
                    mime = mime,
                    tags = emptySet(), // Tags will be added after save
                )

            val saved = documentRepository.save(document)
            val organizationId = resolveOrganizationId(projectId)
            val urn = documentUrn(saved.uuid)

            // Add tags via revet_tagged_items
            tags.forEach { tagName ->
                val slug = tagName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                val tag =
                    tagRepository.findBySlug(slug, organizationId)
                        ?: tagRepository.save(Tag.create(tagName, organizationId, slug))
                taggedItemRepository.addTagToResource(tag.id!!, urn)
            }

            return saved
        }

        @Transactional
        override fun updateDocument(
            id: Long,
            name: String?,
            categoryId: Long?,
            mime: String?,
            tags: Set<String>?,
            isActive: Boolean?,
        ): Document? {
            val existing = documentRepository.findById(id) ?: return null

            name?.let { require(it.isNotBlank()) { "Document name cannot be blank" } }

            val updated =
                existing.update(
                    name = name,
                    categoryId = categoryId,
                    mime = mime,
                    tags = null, // Tags managed separately
                    isActive = isActive,
                )

            val saved = documentRepository.save(updated)

            // Sync tags if provided
            if (tags != null) {
                val urn = documentUrn(existing.uuid)
                val organizationId = resolveOrganizationId(existing.projectId)
                val currentTags = taggedItemRepository.findTagsByResource(urn)
                val currentTagNames = currentTags.map { it.name }.toSet()

                // Remove tags that are no longer in the list
                currentTags.filter { it.name !in tags }.forEach { tag ->
                    taggedItemRepository.removeTagFromResource(tag.id!!, urn)
                }

                // Add new tags
                tags.filter { it !in currentTagNames }.forEach { tagName ->
                    val slug = tagName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                    val tag =
                        tagRepository.findBySlug(slug, organizationId)
                            ?: tagRepository.save(Tag.create(tagName, organizationId, slug))
                    taggedItemRepository.addTagToResource(tag.id!!, urn)
                }
            }

            // Return document with updated tags
            return documentRepository.findById(id)
        }

        @Transactional
        override fun addTagToDocument(
            documentId: Long,
            tag: String,
        ): Document? {
            require(tag.isNotBlank()) { "Tag cannot be blank" }
            val document = documentRepository.findById(documentId) ?: return null

            val organizationId = resolveOrganizationId(document.projectId)
            val slug = tag.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            val tagEntity =
                tagRepository.findBySlug(slug, organizationId)
                    ?: tagRepository.save(Tag.create(tag, organizationId, slug))
            taggedItemRepository.addTagToResource(tagEntity.id!!, documentUrn(document.uuid))

            return documentRepository.findById(documentId)
        }

        @Transactional
        override fun removeTagFromDocument(
            documentId: Long,
            tag: String,
        ): Document? {
            val document = documentRepository.findById(documentId) ?: return null

            val organizationId = resolveOrganizationId(document.projectId)
            val tagEntity = tagRepository.findByName(tag, organizationId) ?: return document
            taggedItemRepository.removeTagFromResource(tagEntity.id!!, documentUrn(document.uuid))

            return documentRepository.findById(documentId)
        }

        override fun deleteDocument(id: Long): Boolean = documentRepository.delete(id)

        @Transactional
        override fun updateDocumentByUuid(
            uuid: UUID,
            name: String?,
            categoryId: Long?,
            mime: String?,
            tags: Set<String>?,
            isActive: Boolean?,
        ): Document? {
            val document = documentRepository.findByUuid(uuid) ?: return null
            return updateDocument(document.id!!, name, categoryId, mime, tags, isActive)
        }

        @Transactional
        override fun addTagToDocumentByUuid(
            uuid: UUID,
            tag: String,
        ): Document? {
            val document = documentRepository.findByUuid(uuid) ?: return null
            return addTagToDocument(document.id!!, tag)
        }

        @Transactional
        override fun removeTagFromDocumentByUuid(
            uuid: UUID,
            tag: String,
        ): Document? {
            val document = documentRepository.findByUuid(uuid) ?: return null
            return removeTagFromDocument(document.id!!, tag)
        }

        @Transactional
        override fun deleteDocumentByUuid(uuid: UUID): Boolean {
            val document = documentRepository.findByUuid(uuid) ?: return false
            return documentRepository.delete(document.id!!)
        }

        override fun getDocumentsPaginated(
            pageRequest: PageRequest,
            includeInactive: Boolean,
            name: String?,
            projectId: Long?,
            categoryId: Long?,
            tagIds: List<Int>?,
            organizationIds: List<Long>?,
        ): Page<Document> {
            val page =
                documentRepository.findByFilters(
                    pageRequest = pageRequest,
                    includeInactive = includeInactive,
                    name = name,
                    projectId = projectId,
                    categoryId = categoryId,
                    tagIds = tagIds,
                    organizationIds = organizationIds,
                )
            val tenantId = authorizationContext.tenantId ?: ""
            val filtered =
                permissionFilterService.filter(page.content, Actions.Document.GET) { doc ->
                    urn.document(tenantId, doc.uuid)
                }
            return Page(
                content = filtered,
                page = page.page,
                size = page.size,
                hasMore = page.hasMore,
            )
        }
    }
