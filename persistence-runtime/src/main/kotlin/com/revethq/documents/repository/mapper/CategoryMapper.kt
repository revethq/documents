package com.revethq.documents.repository.mapper

import com.revethq.documents.domain.Category
import com.revethq.documents.repository.entity.CategoryEntity

/**
 * Maps between Domain Category and CategoryEntity (Panache).
 */
object CategoryMapper {
    fun toDomain(entity: com.revethq.documents.repository.entity.CategoryEntity): com.revethq.documents.domain.Category =
        com.revethq.documents.domain.Category(
            id = entity.id,
            name = entity.name,
            projectId = entity.project?.id ?: throw IllegalStateException("Category must have a project"),
        )

    fun toEntity(domain: com.revethq.documents.domain.Category): com.revethq.documents.repository.entity.CategoryEntity =
        com.revethq.documents.repository.entity.CategoryEntity().apply {
            domain.id?.let { this.id = it }
            this.name = domain.name
        }

    fun updateEntity(
        entity: com.revethq.documents.repository.entity.CategoryEntity,
        domain: com.revethq.documents.domain.Category,
    ): com.revethq.documents.repository.entity.CategoryEntity {
        entity.apply {
            name = domain.name
        }
        return entity
    }
}
