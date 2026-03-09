package com.revethq.documents.service

import com.revethq.documents.domain.Document
import com.revethq.documents.domain.Organization
import com.revethq.documents.domain.Project

/**
 * Service for full-text search operations.
 */
interface SearchService {
    fun searchDocuments(
        query: String,
        maxResults: Int = 50,
    ): List<Document>

    fun searchProjects(
        query: String,
        maxResults: Int = 50,
    ): List<Project>

    fun searchOrganizations(
        query: String,
        maxResults: Int = 50,
    ): List<Organization>

    fun searchAll(
        query: String,
        maxResults: Int = 20,
    ): SearchResults
}

/**
 * Data class to hold search results across multiple entity types.
 */
data class SearchResults(
    val documents: List<Document>,
    val projects: List<Project>,
    val organizations: List<Organization>,
)
