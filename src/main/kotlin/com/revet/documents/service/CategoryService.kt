package com.revet.documents.service

import com.revet.documents.domain.Category
import com.revet.documents.repository.CategoryRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Service interface for Category business logic.
 */
interface CategoryService {
    fun getAllCategories(): List<com.revet.documents.domain.Category>
    fun getCategoryById(id: Long): com.revet.documents.domain.Category?
    fun getCategoriesByProjectId(projectId: Long): List<com.revet.documents.domain.Category>
    fun createCategory(name: String, projectId: Long): com.revet.documents.domain.Category
    fun updateCategory(id: Long, name: String): com.revet.documents.domain.Category?
    fun deleteCategory(id: Long): Boolean
}

/**
 * Implementation of CategoryService with business logic.
 */
@ApplicationScoped
class CategoryServiceImpl @Inject constructor(
    private val categoryRepository: com.revet.documents.repository.CategoryRepository
) : com.revet.documents.service.CategoryService {

    override fun getAllCategories(): List<com.revet.documents.domain.Category> {
        return categoryRepository.findAll()
    }

    override fun getCategoryById(id: Long): com.revet.documents.domain.Category? {
        return categoryRepository.findById(id)
    }

    override fun getCategoriesByProjectId(projectId: Long): List<com.revet.documents.domain.Category> {
        return categoryRepository.findByProjectId(projectId)
    }

    override fun createCategory(name: String, projectId: Long): com.revet.documents.domain.Category {
        require(name.isNotBlank()) { "Category name cannot be blank" }

        val category = _root_ide_package_.com.revet.documents.domain.Category.create(
            name = name,
            projectId = projectId
        )

        return categoryRepository.save(category)
    }

    override fun updateCategory(id: Long, name: String): com.revet.documents.domain.Category? {
        val existing = categoryRepository.findById(id) ?: return null
        require(name.isNotBlank()) { "Category name cannot be blank" }

        val updated = existing.update(name)
        return categoryRepository.save(updated)
    }

    override fun deleteCategory(id: Long): Boolean {
        return categoryRepository.delete(id)
    }
}
