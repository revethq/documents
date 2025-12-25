package com.revet.documents.service

import com.revet.documents.domain.Document
import com.revet.documents.domain.Page
import com.revet.documents.domain.PageRequest
import com.revet.documents.repository.DocumentRepository
import com.revet.documents.repository.TagRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.*

/**
 * Service interface for Document business logic.
 */
interface DocumentService {
    fun getAllDocuments(includeInactive: Boolean = false): List<com.revet.documents.domain.Document>
    fun getDocumentById(id: Long): com.revet.documents.domain.Document?
    fun getDocumentByUuid(uuid: UUID): com.revet.documents.domain.Document?
    fun getDocumentsByProjectId(projectId: Long, includeInactive: Boolean = false): List<com.revet.documents.domain.Document>
    fun getDocumentsByCategoryId(categoryId: Long, includeInactive: Boolean = false): List<com.revet.documents.domain.Document>
    fun createDocument(
        name: String,
        projectId: Long,
        categoryId: Long? = null,
        mime: String? = null,
        tags: Set<String> = emptySet()
    ): com.revet.documents.domain.Document
    fun updateDocument(
        id: Long,
        name: String? = null,
        categoryId: Long? = null,
        mime: String? = null,
        tags: Set<String>? = null,
        isActive: Boolean? = null
    ): com.revet.documents.domain.Document?
    fun addTagToDocument(documentId: Long, tag: String): com.revet.documents.domain.Document?
    fun removeTagFromDocument(documentId: Long, tag: String): com.revet.documents.domain.Document?
    fun deleteDocument(id: Long): Boolean

    fun getDocumentsPaginated(
        pageRequest: com.revet.documents.domain.PageRequest,
        includeInactive: Boolean = false,
        name: String? = null,
        projectId: Long? = null,
        categoryId: Long? = null,
        tagIds: List<Int>? = null,
        organizationIds: List<Long>? = null
    ): com.revet.documents.domain.Page<com.revet.documents.domain.Document>
}

/**
 * Implementation of DocumentService with business logic.
 */
@ApplicationScoped
class DocumentServiceImpl @Inject constructor(
    private val documentRepository: com.revet.documents.repository.DocumentRepository,
    private val tagRepository: com.revet.documents.repository.TagRepository
) : com.revet.documents.service.DocumentService {

    override fun getAllDocuments(includeInactive: Boolean): List<com.revet.documents.domain.Document> {
        return documentRepository.findAll(includeInactive)
    }

    override fun getDocumentById(id: Long): com.revet.documents.domain.Document? {
        return documentRepository.findById(id)
    }

    override fun getDocumentByUuid(uuid: UUID): com.revet.documents.domain.Document? {
        return documentRepository.findByUuid(uuid)
    }

    override fun getDocumentsByProjectId(projectId: Long, includeInactive: Boolean): List<com.revet.documents.domain.Document> {
        return documentRepository.findByProjectId(projectId, includeInactive)
    }

    override fun getDocumentsByCategoryId(categoryId: Long, includeInactive: Boolean): List<com.revet.documents.domain.Document> {
        return documentRepository.findByCategoryId(categoryId, includeInactive)
    }

    @Transactional
    override fun createDocument(
        name: String,
        projectId: Long,
        categoryId: Long?,
        mime: String?,
        tags: Set<String>
    ): com.revet.documents.domain.Document {
        require(name.isNotBlank()) { "Document name cannot be blank" }

        val document = _root_ide_package_.com.revet.documents.domain.Document.create(
            name = name,
            projectId = projectId,
            categoryId = categoryId,
            mime = mime,
            tags = emptySet() // Tags will be added after save
        )

        val saved = documentRepository.save(document)

        // Add tags via taggit tables
        tags.forEach { tagName ->
            // Generate slug to look up by unique constraint
            val slug = tagName.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            val tag = tagRepository.findBySlug(slug)
                ?: tagRepository.save(_root_ide_package_.com.revet.documents.domain.Tag.create(tagName, slug))
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
        isActive: Boolean?
    ): com.revet.documents.domain.Document? {
        val existing = documentRepository.findById(id) ?: return null

        name?.let { require(it.isNotBlank()) { "Document name cannot be blank" } }

        val updated = existing.update(
            name = name,
            categoryId = categoryId,
            mime = mime,
            tags = null, // Tags managed separately
            isActive = isActive
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
                val tag = tagRepository.findBySlug(slug)
                    ?: tagRepository.save(_root_ide_package_.com.revet.documents.domain.Tag.create(tagName, slug))
                tagRepository.addTagToDocument(tag.id!!, id)
            }
        }

        // Return document with updated tags
        return documentRepository.findById(id)
    }

    @Transactional
    override fun addTagToDocument(documentId: Long, tag: String): com.revet.documents.domain.Document? {
        require(tag.isNotBlank()) { "Tag cannot be blank" }
        val document = documentRepository.findById(documentId) ?: return null

        val slug = tag.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        val tagEntity = tagRepository.findBySlug(slug)
            ?: tagRepository.save(_root_ide_package_.com.revet.documents.domain.Tag.create(tag, slug))
        tagRepository.addTagToDocument(tagEntity.id!!, documentId)

        return documentRepository.findById(documentId)
    }

    @Transactional
    override fun removeTagFromDocument(documentId: Long, tag: String): com.revet.documents.domain.Document? {
        val document = documentRepository.findById(documentId) ?: return null

        val tagEntity = tagRepository.findByName(tag) ?: return document
        tagRepository.removeTagFromDocument(tagEntity.id!!, documentId)

        return documentRepository.findById(documentId)
    }

    override fun deleteDocument(id: Long): Boolean {
        return documentRepository.delete(id)
    }

    override fun getDocumentsPaginated(
        pageRequest: com.revet.documents.domain.PageRequest,
        includeInactive: Boolean,
        name: String?,
        projectId: Long?,
        categoryId: Long?,
        tagIds: List<Int>?,
        organizationIds: List<Long>?
    ): com.revet.documents.domain.Page<com.revet.documents.domain.Document> {
        return documentRepository.findByFilters(
            pageRequest = pageRequest,
            includeInactive = includeInactive,
            name = name,
            projectId = projectId,
            categoryId = categoryId,
            tagIds = tagIds,
            organizationIds = organizationIds
        )
    }
}
