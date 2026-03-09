package com.revethq.documents.repository.mapper

import com.revethq.documents.domain.Document
import com.revethq.documents.repository.entity.DocumentEntity

/**
 * Maps between Domain Document and DocumentEntity (Panache).
 * Note: Tags are managed separately via TagRepository and taggit_taggeditem table.
 */
object DocumentMapper {
    fun toDomain(
        entity: com.revethq.documents.repository.entity.DocumentEntity,
        tagNames: Set<String> = emptySet(),
    ): com.revethq.documents.domain.Document =
        com.revethq.documents.domain.Document(
            id = entity.id,
            uuid = entity.uuid,
            name = entity.name,
            projectId = entity.project?.id ?: throw IllegalStateException("Document must have a project"),
            categoryId = entity.category?.id,
            mime = entity.mime,
            date = entity.date,
            tags = tagNames,
            isActive = entity.isActive,
            removedAt = entity.removedAt,
        )

    fun toEntity(domain: com.revethq.documents.domain.Document): com.revethq.documents.repository.entity.DocumentEntity =
        com.revethq.documents.repository.entity.DocumentEntity().apply {
            // Only set ID for existing entities, leave null for new ones to let DB generate
            this.id = domain.id
            this.uuid = domain.uuid
            this.name = domain.name
            this.mime = domain.mime
            this.date = domain.date
            this.isActive = domain.isActive
            this.removedAt = domain.removedAt
        }

    fun updateEntity(
        entity: com.revethq.documents.repository.entity.DocumentEntity,
        domain: com.revethq.documents.domain.Document,
    ): com.revethq.documents.repository.entity.DocumentEntity {
        entity.apply {
            name = domain.name
            mime = domain.mime
            isActive = domain.isActive
            removedAt = domain.removedAt
        }
        return entity
    }
}
