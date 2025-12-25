package com.revet.documents.repository

import com.revet.documents.domain.Category
import com.revet.documents.repository.entity.CategoryEntity
import com.revet.documents.repository.entity.ProjectEntity
import com.revet.documents.repository.mapper.CategoryMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/**
 * Repository interface for Category persistence operations.
 */
interface CategoryRepository {
    fun findAll(): List<com.revet.documents.domain.Category>
    fun findById(id: Long): com.revet.documents.domain.Category?
    fun findByProjectId(projectId: Long): List<com.revet.documents.domain.Category>
    fun save(category: com.revet.documents.domain.Category): com.revet.documents.domain.Category
    fun delete(id: Long): Boolean
}

/**
 * Panache-based implementation of CategoryRepository.
 */
@ApplicationScoped
class CategoryRepositoryImpl : com.revet.documents.repository.CategoryRepository {

    override fun findAll(): List<com.revet.documents.domain.Category> {
        return _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity.listAll().map { _root_ide_package_.com.revet.documents.repository.mapper.CategoryMapper.toDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.Category? {
        return _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity.findById(id)?.let { _root_ide_package_.com.revet.documents.repository.mapper.CategoryMapper.toDomain(it) }
    }

    override fun findByProjectId(projectId: Long): List<com.revet.documents.domain.Category> {
        return _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity.list("project.id", projectId)
            .map { _root_ide_package_.com.revet.documents.repository.mapper.CategoryMapper.toDomain(it) }
    }

    @Transactional
    override fun save(category: com.revet.documents.domain.Category): com.revet.documents.domain.Category {
        val entity = if (category.isNew()) {
            val newEntity = _root_ide_package_.com.revet.documents.repository.mapper.CategoryMapper.toEntity(category)

            val project = _root_ide_package_.com.revet.documents.repository.entity.ProjectEntity.findById(category.projectId)
                ?: throw IllegalArgumentException("Project with id ${category.projectId} not found")
            newEntity.project = project

            newEntity.persist()
            newEntity
        } else {
            val existing = _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity.findById(category.id!!)
                ?: throw IllegalArgumentException("Category with id ${category.id} not found")
            _root_ide_package_.com.revet.documents.repository.mapper.CategoryMapper.updateEntity(existing, category)
            existing
        }
        return _root_ide_package_.com.revet.documents.repository.mapper.CategoryMapper.toDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        return _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity.deleteById(id)
    }
}
