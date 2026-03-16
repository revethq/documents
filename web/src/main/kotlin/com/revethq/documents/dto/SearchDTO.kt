package com.revethq.documents.dto

/**
 * Data Transfer Objects for Search.
 */
data class SearchResultsDTO(
    val documents: List<com.revethq.documents.dto.DocumentDTO>,
    val projects: List<com.revethq.core.dto.ProjectDTO>,
    val organizations: List<com.revethq.core.dto.OrganizationDTO>,
)

data class SearchQueryRequest(
    val query: String,
    val maxResults: Int? = 20,
)
