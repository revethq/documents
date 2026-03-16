package com.revethq.documents.repository

import com.revethq.core.repository.entity.ProjectEntity
import com.revethq.core.repository.entity.TaggedItemEntity
import com.revethq.documents.domain.Document
import com.revethq.documents.domain.Page
import com.revethq.documents.domain.PageRequest
import com.revethq.documents.domain.Sort
import com.revethq.documents.repository.entity.CategoryEntity
import com.revethq.documents.repository.entity.DocumentEntity
import com.revethq.documents.repository.mapper.DocumentMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import java.util.UUID
import io.quarkus.panache.common.Sort as PanacheSort

/**
 * Panache-based implementation of DocumentRepository.
 */
@ApplicationScoped
class DocumentRepositoryImpl : DocumentRepository {
    companion object {
        const val DOCUMENT_URN_PREFIX = "urn:revet:documents::document/"
    }

    @Inject
    lateinit var entityManager: EntityManager

    private fun fetchTagsForDocument(documentUuid: UUID): Set<String> {
        val urn = "$DOCUMENT_URN_PREFIX$documentUuid"
        val taggedItems = TaggedItemEntity.list("resourceUrn = ?1", urn)
        return taggedItems.mapNotNull { it.tag?.name }.toSet()
    }

    private fun entityToDomain(entity: DocumentEntity): Document {
        val tags = fetchTagsForDocument(entity.uuid)
        return DocumentMapper.toDomain(entity, tags)
    }

    override fun findAll(includeInactive: Boolean): List<Document> {
        val entities =
            if (includeInactive) {
                DocumentEntity.listAll()
            } else {
                DocumentEntity.list("isActive", true)
            }
        return entities.map { entityToDomain(it) }
    }

    override fun findById(id: Long): Document? =
        DocumentEntity.findById(id)?.let {
            entityToDomain(it)
        }

    override fun findByUuid(uuid: UUID): Document? =
        DocumentEntity
            .find("uuid", uuid)
            .firstResult()
            ?.let { entityToDomain(it) }

    override fun findByProjectId(
        projectId: Long,
        includeInactive: Boolean,
    ): List<Document> {
        val query =
            if (includeInactive) {
                "project.id = ?1"
            } else {
                "project.id = ?1 and isActive = true"
            }
        return DocumentEntity
            .list(query, projectId)
            .map { entityToDomain(it) }
    }

    override fun findByCategoryId(
        categoryId: Long,
        includeInactive: Boolean,
    ): List<Document> {
        val query =
            if (includeInactive) {
                "category.id = ?1"
            } else {
                "category.id = ?1 and isActive = true"
            }
        return DocumentEntity
            .list(query, categoryId)
            .map { entityToDomain(it) }
    }

    @Transactional
    override fun save(document: Document): Document {
        val entity =
            if (document.isNew()) {
                val newEntity = DocumentMapper.toEntity(document)

                // Set project
                val project =
                    ProjectEntity
                        .findById(document.projectId)
                        ?: throw IllegalArgumentException("Project with id ${document.projectId} not found")
                newEntity.project = project

                // Set category if provided
                document.categoryId?.let { catId ->
                    val category =
                        CategoryEntity
                            .findById(catId)
                            ?: throw IllegalArgumentException("Category with id $catId not found")
                    newEntity.category = category
                }

                newEntity.persist()
                newEntity
            } else {
                val existing =
                    DocumentEntity
                        .findById(document.id!!)
                        ?: throw IllegalArgumentException("Document with id ${document.id} not found")

                DocumentMapper.updateEntity(existing, document)

                // Update category if changed
                val newCategoryId = document.categoryId
                if (newCategoryId != null && newCategoryId != existing.category?.id) {
                    val category =
                        CategoryEntity
                            .findById(newCategoryId)
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
        val entity = DocumentEntity.findById(id) ?: return false
        entity.isActive = false
        entity.removedAt = LocalDateTime.now()
        return true
    }

    override fun findAllPaginated(
        pageRequest: PageRequest,
        includeInactive: Boolean,
    ): Page<Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query =
            if (includeInactive) {
                DocumentEntity.findAll(panacheSort)
            } else {
                DocumentEntity.find("isActive = true", panacheSort)
            }

        // Fetch size+1 to determine if there are more results
        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    override fun findByProjectIdPaginated(
        projectId: Long,
        pageRequest: PageRequest,
        includeInactive: Boolean,
    ): Page<Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query =
            if (includeInactive) {
                DocumentEntity.find("project.id = ?1", panacheSort, projectId)
            } else {
                DocumentEntity.find(
                    "project.id = ?1 and isActive = true",
                    panacheSort,
                    projectId,
                )
            }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    override fun findByCategoryIdPaginated(
        categoryId: Long,
        pageRequest: PageRequest,
        includeInactive: Boolean,
    ): Page<Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query =
            if (includeInactive) {
                DocumentEntity.find("category.id = ?1", panacheSort, categoryId)
            } else {
                DocumentEntity.find(
                    "category.id = ?1 and isActive = true",
                    panacheSort,
                    categoryId,
                )
            }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    private fun Sort.toPanacheSort(): PanacheSort =
        if (ascending) {
            PanacheSort.by(field).ascending()
        } else {
            PanacheSort.by(field).descending()
        }

    override fun findByTagIdsPaginated(
        tagIds: List<Int>,
        pageRequest: PageRequest,
        includeInactive: Boolean,
    ): Page<Document> {
        if (tagIds.isEmpty()) {
            return Page.empty(pageRequest.page, pageRequest.size)
        }

        // Find document IDs that have ALL specified tags
        val documentIds = findDocumentIdsWithAllTags(tagIds)
        if (documentIds.isEmpty()) {
            return Page.empty(pageRequest.page, pageRequest.size)
        }

        return findByDocumentIds(documentIds, pageRequest, includeInactive)
    }

    override fun findByFilters(
        pageRequest: PageRequest,
        includeInactive: Boolean,
        name: String?,
        projectId: Long?,
        categoryId: Long?,
        tagIds: List<Int>?,
        organizationIds: List<Long>?,
    ): Page<Document> {
        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        // If tagIds are specified, we need to filter by them first
        val documentIdsWithTags: Set<Long>? =
            if (!tagIds.isNullOrEmpty()) {
                findDocumentIdsWithAllTags(tagIds)
            } else {
                null
            }

        // If tag filter returned no results, return empty page
        if (documentIdsWithTags != null && documentIdsWithTags.isEmpty()) {
            return Page.empty(pageRequest.page, pageRequest.size)
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

        val query =
            if (queryString.isEmpty()) {
                DocumentEntity.findAll(panacheSort)
            } else {
                DocumentEntity.find(queryString, panacheSort, *params.toTypedArray())
            }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findDocumentIdsWithAllTags(tagIds: List<Int>): Set<Long> {
        if (tagIds.isEmpty()) return emptySet()

        // Use native SQL to efficiently find documents that have ALL specified tags
        val sql =
            """
            SELECT d.id FROM revet_documents d
            WHERE (
                SELECT COUNT(DISTINCT ti.tag_id) FROM revet_tagged_items ti
                WHERE ti.resource_urn = '$DOCUMENT_URN_PREFIX' || CAST(d.uuid AS TEXT)
                AND ti.tag_id IN (:tagIds)
            ) = :tagCount
            """.trimIndent()

        val results =
            entityManager
                .createNativeQuery(sql)
                .setParameter("tagIds", tagIds)
                .setParameter("tagCount", tagIds.size.toLong())
                .resultList as List<Number>

        return results.map { it.toLong() }.toSet()
    }

    private fun findByDocumentIds(
        documentIds: Set<Long>,
        pageRequest: PageRequest,
        includeInactive: Boolean,
    ): Page<Document> {
        if (documentIds.isEmpty()) {
            return Page.empty(pageRequest.page, pageRequest.size)
        }

        val panacheSort = pageRequest.sort?.toPanacheSort() ?: PanacheSort.by("date").descending()

        val query =
            if (includeInactive) {
                DocumentEntity.find("id in ?1", panacheSort, documentIds.toList())
            } else {
                DocumentEntity.find(
                    "id in ?1 and isActive = true",
                    panacheSort,
                    documentIds.toList(),
                )
            }

        val entities = query.page(pageRequest.page, pageRequest.size + 1).list()
        val documents = entities.map { entityToDomain(it) }

        return Page.fromOverfetch(documents, pageRequest.page, pageRequest.size)
    }
}
