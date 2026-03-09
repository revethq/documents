package com.revethq.documents.service

import com.revethq.documents.domain.Document
import com.revethq.documents.domain.Organization
import com.revethq.documents.domain.Project
import com.revethq.documents.repository.entity.DocumentEntity
import com.revethq.documents.repository.entity.OrganizationEntity
import com.revethq.documents.repository.entity.ProjectEntity
import com.revethq.documents.repository.mapper.DocumentMapper
import com.revethq.documents.repository.mapper.OrganizationMapper
import com.revethq.documents.repository.mapper.ProjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager

/**
 * Implementation of SearchService using PostgreSQL full-text search.
 */
@ApplicationScoped
class SearchServiceImpl
    @Inject
    constructor(
        private val entityManager: EntityManager,
    ) : SearchService {
        override fun searchDocuments(
            query: String,
            maxResults: Int,
        ): List<Document> {
            // PostgreSQL full-text search using to_tsquery and to_tsvector
            // Search across name, tags, and mime fields
            val sql =
                """
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
            val results =
                entityManager
                    .createNativeQuery(sql, DocumentEntity::class.java)
                    .setParameter("query", query)
                    .setParameter("maxResults", maxResults)
                    .resultList as List<DocumentEntity>

            return results.map { DocumentMapper.toDomain(it) }
        }

        override fun searchProjects(
            query: String,
            maxResults: Int,
        ): List<Project> {
            // Search across name, description, and tags
            val sql =
                """
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
            val results =
                entityManager
                    .createNativeQuery(sql, ProjectEntity::class.java)
                    .setParameter("query", query)
                    .setParameter("maxResults", maxResults)
                    .resultList as List<ProjectEntity>

            return results.map { ProjectMapper.toDomain(it) }
        }

        override fun searchOrganizations(
            query: String,
            maxResults: Int,
        ): List<Organization> {
            // Search across name and description
            val sql =
                """
                SELECT o.* FROM revet_organizations o
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
            val results =
                entityManager
                    .createNativeQuery(sql, OrganizationEntity::class.java)
                    .setParameter("query", query)
                    .setParameter("maxResults", maxResults)
                    .resultList as List<OrganizationEntity>

            return results.map { OrganizationMapper.toDomain(it) }
        }

        override fun searchAll(
            query: String,
            maxResults: Int,
        ): SearchResults =
            SearchResults(
                documents = searchDocuments(query, maxResults),
                projects = searchProjects(query, maxResults),
                organizations = searchOrganizations(query, maxResults),
            )
    }
