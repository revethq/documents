package com.revet.documents.dto

/**
 * Data Transfer Objects for Search.
 */
data class SearchResultsDTO(
    val documents: List<com.revet.documents.dto.DocumentDTO>,
    val projects: List<com.revet.documents.dto.ProjectDTO>,
    val organizations: List<com.revet.documents.dto.OrganizationDTO>
)

data class SearchQueryRequest(
    val query: String,
    val maxResults: Int? = 20
)
