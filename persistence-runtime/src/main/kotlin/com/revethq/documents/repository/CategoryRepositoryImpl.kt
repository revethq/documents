package com.revethq.documents.repository

import com.revethq.documents.domain.Category
import com.revethq.documents.repository.entity.CategoryEntity
import com.revethq.documents.repository.entity.ProjectEntity
import com.revethq.documents.repository.mapper.CategoryMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/**
 * Panache-based implementation of CategoryRepository.
 */
@ApplicationScoped
class CategoryRepositoryImpl : CategoryRepository {
    override fun findAll(): List<Category> =
        CategoryEntity.listAll().map {
            CategoryMapper.toDomain(it)
        }

    override fun findById(id: Long): Category? =
        CategoryEntity.findById(id)?.let {
            CategoryMapper.toDomain(it)
        }

    override fun findByProjectId(projectId: Long): List<Category> =
        CategoryEntity
            .list("project.id", projectId)
            .map { CategoryMapper.toDomain(it) }

    @Transactional
    override fun save(category: Category): Category {
        val entity =
            if (category.isNew()) {
                val newEntity = CategoryMapper.toEntity(category)

                val project =
                    ProjectEntity
                        .findById(category.projectId)
                        ?: throw IllegalArgumentException("Project with id ${category.projectId} not found")
                newEntity.project = project

                newEntity.persist()
                newEntity
            } else {
                val existing =
                    CategoryEntity
                        .findById(category.id!!)
                        ?: throw IllegalArgumentException("Category with id ${category.id} not found")
                CategoryMapper.updateEntity(existing, category)
                existing
            }
        return CategoryMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean = CategoryEntity.deleteById(id)
}
