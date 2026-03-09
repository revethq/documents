package com.revethq.documents.service

import com.revethq.documents.domain.Document
import com.revethq.documents.domain.Page
import com.revethq.documents.domain.PageRequest
import com.revethq.documents.domain.Tag
import com.revethq.documents.repository.DocumentRepository
import com.revethq.documents.repository.TagRepository
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
    ) : DocumentService {
        override fun getAllDocuments(includeInactive: Boolean): List<Document> = documentRepository.findAll(includeInactive)

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

            // Add tags via taggit tables
            tags.forEach { tagName ->
                // Generate slug to look up by unique constraint
                val slug = tagName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                val tag =
                    tagRepository.findBySlug(slug)
                        ?: tagRepository.save(
                            Tag
                                .create(tagName, slug),
                        )
                tagRepository.addTagToDocument(tag.id!!, saved.id!!)
            }

            // Return document with tags
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
                val currentTags = tagRepository.findTagsByDocumentId(id)
                val currentTagNames = currentTags.map { it.name }.toSet()

                // Remove tags that are no longer in the list
                currentTags.filter { it.name !in tags }.forEach { tag ->
                    tagRepository.removeTagFromDocument(tag.id!!, id)
                }

                // Add new tags
                tags.filter { it !in currentTagNames }.forEach { tagName ->
                    val slug = tagName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                    val tag =
                        tagRepository.findBySlug(slug)
                            ?: tagRepository.save(
                                Tag
                                    .create(tagName, slug),
                            )
                    tagRepository.addTagToDocument(tag.id!!, id)
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

            val slug = tag.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            val tagEntity =
                tagRepository.findBySlug(slug)
                    ?: tagRepository.save(
                        Tag
                            .create(tag, slug),
                    )
            tagRepository.addTagToDocument(tagEntity.id!!, documentId)

            return documentRepository.findById(documentId)
        }

        @Transactional
        override fun removeTagFromDocument(
            documentId: Long,
            tag: String,
        ): Document? {
            val document = documentRepository.findById(documentId) ?: return null

            val tagEntity = tagRepository.findByName(tag) ?: return document
            tagRepository.removeTagFromDocument(tagEntity.id!!, documentId)

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
        ): Page<Document> =
            documentRepository.findByFilters(
                pageRequest = pageRequest,
                includeInactive = includeInactive,
                name = name,
                projectId = projectId,
                categoryId = categoryId,
                tagIds = tagIds,
                organizationIds = organizationIds,
            )
    }
