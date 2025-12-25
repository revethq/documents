package com.revet.documents.repository.mapper

import com.revet.documents.domain.Category
import com.revet.documents.repository.entity.CategoryEntity

/**
 * Maps between Domain Category and CategoryEntity (Panache).
 */
object CategoryMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.CategoryEntity): com.revet.documents.domain.Category {
        return _root_ide_package_.com.revet.documents.domain.Category(
            id = entity.id,
            name = entity.name,
            projectId = entity.project?.id ?: throw IllegalStateException("Category must have a project")
        )
    }

    fun toEntity(domain: com.revet.documents.domain.Category): com.revet.documents.repository.entity.CategoryEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.CategoryEntity().apply {
            domain.id?.let { this.id = it }
            this.name = domain.name
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.CategoryEntity, domain: com.revet.documents.domain.Category): com.revet.documents.repository.entity.CategoryEntity {
        entity.apply {
            name = domain.name
        }
        return entity
    }
}
