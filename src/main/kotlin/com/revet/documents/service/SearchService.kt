package com.revet.documents.service

import com.revet.documents.domain.Document
import com.revet.documents.domain.Organization
import com.revet.documents.domain.Project
import com.revet.documents.repository.entity.DocumentEntity
import com.revet.documents.repository.entity.OrganizationEntity
import com.revet.documents.repository.entity.ProjectEntity
import com.revet.documents.repository.mapper.DocumentMapper
import com.revet.documents.repository.mapper.OrganizationMapper
import com.revet.documents.repository.mapper.ProjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager

/**
 * Service for full-text search operations using PostgreSQL's native full-text search.
 */
interface SearchService {
    fun searchDocuments(query: String, maxResults: Int = 50): List<com.revet.documents.domain.Document>
    fun searchProjects(query: String, maxResults: Int = 50): List<com.revet.documents.domain.Project>
    fun searchOrganizations(query: String, maxResults: Int = 50): List<com.revet.documents.domain.Organization>
    fun searchAll(query: String, maxResults: Int = 20): com.revet.documents.service.SearchResults
}

/**
 * Data class to hold search results across multiple entity types.
 */
data class SearchResults(
    val documents: List<com.revet.documents.domain.Document>,
    val projects: List<com.revet.documents.domain.Project>,
    val organizations: List<com.revet.documents.domain.Organization>
)

/**
 * Implementation of SearchService using PostgreSQL full-text search.
 */
@ApplicationScoped
class SearchServiceImpl @Inject constructor(
    private val entityManager: EntityManager
) : com.revet.documents.service.SearchService {

    override fun searchDocuments(query: String, maxResults: Int): List<com.revet.documents.domain.Document> {
        // PostgreSQL full-text search using to_tsquery and to_tsvector
        // Search across name, tags, and mime fields
        val sql = """
            SELECT d.* FROM documents d
            WHERE
                to_tsvector('english', COALESCE(d.name, '')) @@ plainto_tsquery('english', :query)
                OR to_tsvector('english', COALESCE(d.mime, '')) @@ plainto_tsquery('english', :query)
                OR EXISTS (
                    SELECT 1 FROM document_tags dt
                    WHERE dt.document_id = d.id
                    AND to_tsvector('english', dt.tag) @@ plainto_tsquery('english', :query)
                )
            ORDER BY
                ts_rank(to_tsvector('english', COALESCE(d.name, '')), plainto_tsquery('english', :query)) DESC
            LIMIT :maxResults
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val results = entityManager.createNativeQuery(sql, _root_ide_package_.com.revet.documents.repository.entity.DocumentEntity::class.java)
            .setParameter("query", query)
            .setParameter("maxResults", maxResults)
            .resultList as List<com.revet.documents.repository.entity.DocumentEntity>

        return results.map { _root_ide_package_.com.revet.documents.repository.mapper.DocumentMapper.toDomain(it) }
    }

    override fun searchProjects(query: String, maxResults: Int): List<com.revet.documents.domain.Project> {
        // Search across name, description, and tags
        val sql = """
            SELECT p.* FROM projects p
            WHERE
                to_tsvector('english', COALESCE(p.name, '')) @@ plainto_tsquery('english', :query)
                OR to_tsvector('english', COALESCE(p.description, '')) @@ plainto_tsquery('english', :query)
                OR EXISTS (
                    SELECT 1 FROM project_tags pt
                    WHERE pt.project_id = p.id
                    AND to_tsvector('english', pt.tag) @@ plainto_tsquery('english', :query)
                )
            ORDER BY
                ts_rank(
                    to_tsvector('english', COALESCE(p.name, '') || ' ' || COALESCE(p.description, '')),
                    plainto_tsquery('english', :query)
                ) DESC
            LIMIT :maxResults
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val results = entityManager.createNativeQuery(sql, _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity::class.java)
            .setParameter("query", query)
            .setParameter("maxResults", maxResults)
            .resultList as List<com.revet.documents.repository.entity.ProjectEntity>

        return results.map { _root_ide_package_.com.revet.documents.repository.mapper.ProjectMapper.toDomain(it) }
    }

    override fun searchOrganizations(query: String, maxResults: Int): List<com.revet.documents.domain.Organization> {
        // Search across name and description
        val sql = """
            SELECT o.* FROM kala_companies o
            WHERE
                to_tsvector('english', COALESCE(o.name, '')) @@ plainto_tsquery('english', :query)
                OR to_tsvector('english', COALESCE(o.description, '')) @@ plainto_tsquery('english', :query)
            ORDER BY
                ts_rank(
                    to_tsvector('english', COALESCE(o.name, '') || ' ' || COALESCE(o.description, '')),
                    plainto_tsquery('english', :query)
                ) DESC
            LIMIT :maxResults
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val results = entityManager.createNativeQuery(sql, _root_ide_package_.com.revet.documents.repository.entity.OrganizationEntity::class.java)
            .setParameter("query", query)
            .setParameter("maxResults", maxResults)
            .resultList as List<com.revet.documents.repository.entity.OrganizationEntity>

        return results.map { _root_ide_package_.com.revet.documents.repository.mapper.OrganizationMapper.toDomain(it) }
    }

    override fun searchAll(query: String, maxResults: Int): com.revet.documents.service.SearchResults {
        return _root_ide_package_.com.revet.documents.service.SearchResults(
            documents = searchDocuments(query, maxResults),
            projects = searchProjects(query, maxResults),
            organizations = searchOrganizations(query, maxResults)
        )
    }
}
