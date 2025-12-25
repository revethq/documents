package com.revet.documents.dto

/**
 * RFC 9457 Problem Details for HTTP APIs.
 * Used for warnings and error responses.
 */
data class ProblemDetail(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: String? = null
)

/**
 * Generic paginated response wrapper.
 * Uses hasMore instead of totalElements to avoid expensive COUNT queries.
 */
data class PageDTO<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasMore: Boolean,
    val warnings: List<com.revet.documents.dto.ProblemDetail> = emptyList()
)
