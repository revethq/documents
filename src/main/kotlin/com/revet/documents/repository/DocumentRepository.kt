package com.revet.documents.repository

import com.revet.documents.domain.Document
import com.revet.documents.domain.Page
import com.revet.documents.domain.PageRequest
import com.revet.documents.domain.Sort
import com.revet.documents.repository.entity.CategoryEntity
import com.revet.documents.repository.entity.DocumentEntity
import com.revet.documents.repository.entity.ProjectEntity
import com.revet.documents.repository.entity.TaggedItemEntity
import com.revet.documents.repository.mapper.DocumentMapper
import io.quarkus.panache.common.Sort as PanacheSort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import java.util.*

/**
 * Repository interface for Document persistence operations.
 */
interface DocumentRepository {
    fun findAll(includeInactive: Boolean = false): List<com.revet.documents.domain.Document>
    fun findById(id: Long): com.revet.documents.domain.Document?
    fun findByUuid(uuid: UUID): com.revet.documents.domain.Document?
    fun findByProjectId(projectId: Long, includeInactive: Boolean = false): List<com.revet.documents.domain.Document>
    fun findByCategoryId(categoryId: Long, includeInactive: Boolean = false): List<com.revet.documents.domain.Document>
    fun save(document: com.revet.documents.domain.Document): com.revet.documents.domain.Document
    fun delete(id: Long): Boolean

    fun findAllPaginated(pageRequest: com.revet.documents.domain.PageRequest, includeInactive: Boolean = false): com.revet.documents.domain.Page<com.revet.documents.domain.Document>
    fun findByProjectIdPaginated(projectId: Long, pageRequest: com.revet.documents.domain.PageRequest, includeInactive: Boolean = false): com.revet.documents.domain.Page<com.revet.documents.domain.Document>
    fun findByCategoryIdPaginated(categoryId: Long, pageRequest: com.revet.documents.domain.PageRequest, includeInactive: Boolean = false): com.revet.documents.domain.Page<com.revet.documents.domain.Document>
    fun findByTagIdsPaginated(tagIds: List<Int>, pageRequest: com.revet.documents.domain.PageRequest, includeInactive: Boolean = false): com.revet.documents.domain.Page<com.revet.documents.domain.Document>
    fun findByFilters(
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
 * Panache-based implementation of DocumentRepository.
 */
@ApplicationScoped
class DocumentRepositoryImpl : com.revet.documents.repository.DocumentRepository {

    private fun fetchTagsForDocument(documentId: Long): Set<String> {
        val taggedItems = _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.list(
            "contentTypeId = ?1 and objectId = ?2",
            _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
            documentId.toInt()
        )
        return taggedItems.mapNotNull { it.tag?.name }.toSet()
    }

    private fun entityToDomain(entity: com.revet.documents.repository.entity.DocumentEntity): com.revet.documents.domain.Document {
        val tags = entity.id?.let { fetchTagsForDocument(it) } ?: emptySet()
        return _root_ide_package_.com.revet.documents.repository.mapper.DocumentMapper.toDomain(entity, tags)
    }

    override fun findAll(includeInactive: Boolean): List<com.revet.documents.domain.Document> {
        val entities = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.listAll()
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.list("isActive", true)
        }
        return entities.map { entityToDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.Document? {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.findById(id)?.let { entityToDomain(it) }
    }

    override fun findByUuid(uuid: UUID): com.revet.documents.domain.Document? {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("uuid", uuid).firstResult()
            ?.let { entityToDomain(it) }
    }

    override fun findByProjectId(projectId: Long, includeInactive: Boolean): List<com.revet.documents.domain.Document> {
        val query = if (includeInactive) {
            "project.id = ?1"
        } else {
            "project.id = ?1 and isActive = true"
        }
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.list(query, projectId)
            .map { entityToDomain(it) }
    }

    override fun findByCategoryId(categoryId: Long, includeInactive: Boolean): List<com.revet.documents.domain.Document> {
        val query = if (includeInactive) {
            "category.id = ?1"
        } else {
            "category.id = ?1 and isActive = true"
        }
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.list(query, categoryId)
            .map { entityToDomain(it) }
    }

    @Transactional
    override fun save(document: com.revet.documents.domain.Document): com.revet.documents.domain.Document {
        val entity = if (document.isNew()) {
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.DocumentMapper.toEntity(document)

            // Set project
            val project = _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.findById(document.projectId)
                ?: throw IllegalArgumentException("Project with id ${document.projectId} not found")
            newEntity.project = project

            // Set category if provided
            document.categoryId?.let { catId ->
                val category = _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity.findById(catId)
                    ?: throw IllegalArgumentException("Category with id $catId not found")
                newEntity.category = category
            }

            newEntity.persist()
            newEntity
        } else {
            val existing = _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.findById(document.id!!)
                ?: throw IllegalArgumentException("Document with id ${document.id} not found")

            _root_ide_package_.com.revet.documents.repository.mapper.DocumentMapper.updateEntity(existing, document)

            // Update category if changed
            val newCategoryId = document.categoryId
            if (newCategoryId != null && newCategoryId != existing.category?.id) {
                val category = _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity.findById(newCategoryId)
                    ?: throw IllegalArgumentException("Category with id $newCategoryId not found")
                existing.category = category
            } else if (newCategoryId == null) {
                existing.category = null
            }

            existing
        }
        return entityToDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        val entity = _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.findById(id) ?: return false
        entity.isActive = false
        entity.removedAt = java.time.LocalDateTime.now()
        return true
    }

    override fun findAllPaginated(pageRequest: com.revet.documents.domain.PageRequest, includeInactive: Boolean): com.revet.documents.domain.Page<com.revet.documents.domain.Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.findAll(panacheSort)
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("isActive = true", panacheSort)
        }

        // Fetch size+1 to determine if there are more results
        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return _root_ide_package_.com.revet.documents.domain.Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    override fun findByProjectIdPaginated(
        projectId: Long,
        pageRequest: com.revet.documents.domain.PageRequest,
        includeInactive: Boolean
    ): com.revet.documents.domain.Page<com.revet.documents.domain.Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("project.id = ?1", panacheSort, projectId)
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("project.id = ?1 and isActive = true", panacheSort, projectId)
        }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return _root_ide_package_.com.revet.documents.domain.Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    override fun findByCategoryIdPaginated(
        categoryId: Long,
        pageRequest: com.revet.documents.domain.PageRequest,
        includeInactive: Boolean
    ): com.revet.documents.domain.Page<com.revet.documents.domain.Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("category.id = ?1", panacheSort, categoryId)
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("category.id = ?1 and isActive = true", panacheSort, categoryId)
        }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return _root_ide_package_.com.revet.documents.domain.Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    private fun com.revet.documents.domain.Sort.toPanacheSort(): PanacheSort {
        return if (ascending) {
            PanacheSort.by(field).ascending()
        } else {
            PanacheSort.by(field).descending()
        }
    }

    override fun findByTagIdsPaginated(
        tagIds: List<Int>,
        pageRequest: com.revet.documents.domain.PageRequest,
        includeInactive: Boolean
    ): com.revet.documents.domain.Page<com.revet.documents.domain.Document> {
        if (tagIds.isEmpty()) {
            return _root_ide_package_.com.revet.documents.domain.Page.empty(pageRequest.page, pageRequest.size)
        }

        // Find document IDs that have ALL specified tags
        val documentIds = findDocumentIdsWithAllTags(tagIds)
        if (documentIds.isEmpty()) {
            return _root_ide_package_.com.revet.documents.domain.Page.empty(pageRequest.page, pageRequest.size)
        }

        return findByDocumentIds(documentIds, pageRequest, includeInactive)
    }

    override fun findByFilters(
        pageRequest: com.revet.documents.domain.PageRequest,
        includeInactive: Boolean,
        name: String?,
        projectId: Long?,
        categoryId: Long?,
        tagIds: List<Int>?,
        organizationIds: List<Long>?
    ): com.revet.documents.domain.Page<com.revet.documents.domain.Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        // If tagIds are specified, we need to filter by them first
        val documentIdsWithTags: Set<Long>? = if (!tagIds.isNullOrEmpty()) {
            findDocumentIdsWithAllTags(tagIds)
        } else null

        // If tag filter returned no results, return empty page
        if (documentIdsWithTags != null && documentIdsWithTags.isEmpty()) {
            return _root_ide_package_.com.revet.documents.domain.Page.empty(pageRequest.page, pageRequest.size)
        }

        // Build dynamic query
        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()
        var paramIndex = 1

        if (!includeInactive) {
            conditions.add("isActive = true")
        }

        // Fuzzy search on name (case-insensitive contains)
        if (!name.isNullOrBlank()) {
            conditions.add("lower(name) like ?$paramIndex")
            params.add("%${name.lowercase()}%")
            paramIndex++
        }

        if (projectId != null) {
            conditions.add("project.id = ?$paramIndex")
            params.add(projectId)
            paramIndex++
        }

        if (categoryId != null) {
            conditions.add("category.id = ?$paramIndex")
            params.add(categoryId)
            paramIndex++
        }

        if (!organizationIds.isNullOrEmpty()) {
            conditions.add("project.organization.id in ?$paramIndex")
            params.add(organizationIds)
            paramIndex++
        }

        if (documentIdsWithTags != null) {
            conditions.add("id in ?$paramIndex")
            params.add(documentIdsWithTags.toList())
            paramIndex++
        }

        val queryString = if (conditions.isEmpty()) "" else conditions.joinToString(" and ")

        val query = if (queryString.isEmpty()) {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.findAll(panacheSort)
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find(queryString, panacheSort, *params.toTypedArray())
        }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return _root_ide_package_.com.revet.documents.domain.Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    private fun findDocumentIdsWithAllTags(tagIds: List<Int>): Set<Long> {
        // For each tag, find document IDs that have it, then intersect
        val tagIdsList = tagIds.toList()
        var result: Set<Long>? = null

        for (tagId in tagIdsList) {
            val taggedItems = _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.list(
                "contentTypeId = ?1 and tag.id = ?2",
                _root_ide_package_.com.revet.documents.repository.entity.TaggedItemEntity.DOCUMENT_CONTENT_TYPE_ID,
                tagId
            )
            val docIds = taggedItems.map { it.objectId.toLong() }.toSet()

            result = if (result == null) {
                docIds
            } else {
                result.intersect(docIds)
            }

            // Early exit if no documents match
            if (result.isEmpty()) {
                return emptySet()
            }
        }

        return result ?: emptySet()
    }

    private fun findByDocumentIds(
        documentIds: Set<Long>,
        pageRequest: com.revet.documents.domain.PageRequest,
        includeInactive: Boolean
    ): com.revet.documents.domain.Page<com.revet.documents.domain.Document> {
        if (documentIds.isEmpty()) {
            return _root_ide_package_.com.revet.documents.domain.Page.empty(pageRequest.page, pageRequest.size)
        }

        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("id in ?1", panacheSort, documentIds.toList())
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity.find("id in ?1 and isActive = true", panacheSort, documentIds.toList())
        }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return _root_ide_package_.com.revet.documents.domain.Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }
}
