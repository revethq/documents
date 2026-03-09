package com.revethq.documents.repository.mapper

import com.revethq.documents.domain.Tag
import com.revethq.documents.repository.entity.TagEntity

/**
 * Maps between Domain Tag and TagEntity (Panache).
 */
object TagMapper {
    fun toDomain(entity: com.revethq.documents.repository.entity.TagEntity): com.revethq.documents.domain.Tag =
        com.revethq.documents.domain.Tag(
            id = entity.id,
            name = entity.name,
            slug = entity.slug,
        )

    fun toEntity(domain: com.revethq.documents.domain.Tag): com.revethq.documents.repository.entity.TagEntity =
        com.revethq.documents.repository.entity.TagEntity().apply {
            domain.id?.let { this.id = it }
            this.name = domain.name
            this.slug = domain.slug
        }

    fun updateEntity(
        entity: com.revethq.documents.repository.entity.TagEntity,
        domain: com.revethq.documents.domain.Tag,
    ): com.revethq.documents.repository.entity.TagEntity {
        entity.apply {
            name = domain.name
            slug = domain.slug
        }
        return entity
    }
}
