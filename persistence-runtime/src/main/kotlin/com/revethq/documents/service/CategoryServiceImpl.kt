package com.revethq.documents.service

import com.revethq.documents.domain.Category
import com.revethq.documents.permission.Actions
import com.revethq.documents.permission.DocumentsUrn
import com.revethq.documents.repository.CategoryRepository
import com.revethq.iam.permission.web.filter.AuthorizationContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Implementation of CategoryService with business logic.
 */
@ApplicationScoped
class CategoryServiceImpl
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
        private val permissionFilterService: PermissionFilterService,
        private val urn: DocumentsUrn,
        private val authorizationContext: AuthorizationContext,
    ) : CategoryService {
        override fun getAllCategories(): List<Category> {
            val categories = categoryRepository.findAll()
            val tenantId = authorizationContext.tenantId ?: ""
            return permissionFilterService.filter(categories, Actions.Category.GET) { category ->
                urn.category(tenantId, category.id!!)
            }
        }

        override fun getCategoryById(id: Long): Category? = categoryRepository.findById(id)

        override fun getCategoriesByProjectId(projectId: Long): List<Category> {
            val categories = categoryRepository.findByProjectId(projectId)
            val tenantId = authorizationContext.tenantId ?: ""
            return permissionFilterService.filter(categories, Actions.Category.GET) { category ->
                urn.category(tenantId, category.id!!)
            }
        }

        override fun createCategory(
            name: String,
            projectId: Long,
        ): Category {
            require(name.isNotBlank()) { "Category name cannot be blank" }

            val category =
                Category.create(
                    name = name,
                    projectId = projectId,
                )

            return categoryRepository.save(category)
        }

        override fun updateCategory(
            id: Long,
            name: String,
        ): Category? {
            val existing = categoryRepository.findById(id) ?: return null
            require(name.isNotBlank()) { "Category name cannot be blank" }

            val updated = existing.update(name)
            return categoryRepository.save(updated)
        }

        override fun deleteCategory(id: Long): Boolean = categoryRepository.delete(id)
    }
