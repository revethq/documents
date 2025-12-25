package com.revet.documents.repository.mapper

import com.revet.documents.domain.Tag
import com.revet.documents.repository.entity.TagEntity

/**
 * Maps between Domain Tag and TagEntity (Panache).
 */
object TagMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.TagEntity): com.revet.documents.domain.Tag {
        return _root_ide_package_.com.revet.documents.domain.Tag(
            id = entity.id,
            name = entity.name,
            slug = entity.slug
        )
    }

    fun toEntity(domain: com.revet.documents.domain.Tag): com.revet.documents.repository.entity.TagEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.TagEntity().apply {
            domain.id?.let { this.id = it }
            this.name = domain.name
            this.slug = domain.slug
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.TagEntity, domain: com.revet.documents.domain.Tag): com.revet.documents.repository.entity.TagEntity {
        entity.apply {
            name = domain.name
            slug = domain.slug
        }
        return entity
    }
}
