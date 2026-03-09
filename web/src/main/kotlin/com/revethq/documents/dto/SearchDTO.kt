package com.revethq.documents.dto

/**
 * Data Transfer Objects for Search.
 */
data class SearchResultsDTO(
    val documents: List<com.revethq.documents.dto.DocumentDTO>,
    val projects: List<com.revethq.documents.dto.ProjectDTO>,
    val organizations: List<com.revethq.documents.dto.OrganizationDTO>,
)

data class SearchQueryRequest(
    val query: String,
    val maxResults: Int? = 20,
)
