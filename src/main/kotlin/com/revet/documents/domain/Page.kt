package com.revet.documents.domain

/**
 * Framework-agnostic page result for domain layer.
 * Uses hasMore instead of totalElements to avoid COUNT queries.
 */
data class Page<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasMore: Boolean
) {
    val first: Boolean = page == 0
    val hasPrevious: Boolean = page > 0

    fun <R> map(mapper: (T) -> R): Page<R> {
        return Page(
            content = content.map(mapper),
            page = page,
            size = size,
            hasMore = hasMore
        )
    }

    companion object {
        fun <T> empty(page: Int = 0, size: Int = 20): Page<T> {
            return Page(emptyList(), page, size, hasMore = false)
        }

        /**
         * Create a page from results fetched with size+1.
         * If we got more than size results, there are more pages.
         */
        fun <T> fromOverfetch(results: List<T>, page: Int, size: Int): Page<T> {
            val hasMore = results.size > size
            val content = if (hasMore) results.take(size) else results
            return Page(content, page, size, hasMore)
        }
    }
}

/**
 * Framework-agnostic sort specification for domain layer.
 */
data class Sort(
    val field: String,
    val ascending: Boolean = true
) {
    companion object {
        fun asc(field: String) = Sort(field, true)
        fun desc(field: String) = Sort(field, false)
    }
}

/**
 * Pagination request parameters for domain layer.
 */
data class PageRequest(
    val page: Int,
    val size: Int,
    val sort: com.revet.documents.domain.Sort? = null
) {
    init {
        require(page >= 0) { "Page index must be non-negative" }
        require(size > 0) { "Page size must be positive" }
    }

    companion object {
        fun of(page: Int, size: Int, sort: com.revet.documents.domain.Sort? = null) = PageRequest(page, size, sort)
    }
}
