package com.revethq.documents.service

import com.revethq.documents.domain.Category

/**
 * Service interface for Category business logic.
 */
interface CategoryService {
    fun getAllCategories(): List<Category>

    fun getCategoryById(id: Long): Category?

    fun getCategoriesByProjectId(projectId: Long): List<Category>

    fun createCategory(
        name: String,
        projectId: Long,
    ): Category

    fun updateCategory(
        id: Long,
        name: String,
    ): Category?

    fun deleteCategory(id: Long): Boolean
}
