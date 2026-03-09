package com.revethq.documents.repository.mapper

import com.revethq.documents.domain.DocumentVersion
import com.revethq.documents.repository.entity.DocumentVersionEntity

/**
 * Maps between Domain DocumentVersion and DocumentVersionEntity (Panache).
 */
object DocumentVersionMapper {
    fun toDomain(entity: com.revethq.documents.repository.entity.DocumentVersionEntity): com.revethq.documents.domain.DocumentVersion =
        com.revethq.documents.domain.DocumentVersion(
            uuid = entity.uuid,
            documentId = entity.document?.id,
            name = entity.name,
            file = entity.file,
            url = entity.url,
            size = entity.size,
            description = entity.description,
            mime = entity.mime,
            userId = entity.userId,
            uploadStatus = entity.uploadStatus,
            created = entity.created,
            changed = entity.changed,
        )

    fun toEntity(domain: com.revethq.documents.domain.DocumentVersion): com.revethq.documents.repository.entity.DocumentVersionEntity =
        com.revethq.documents.repository.entity.DocumentVersionEntity().apply {
            this.uuid = domain.uuid
            this.name = domain.name
            this.file = domain.file
            this.url = domain.url
            this.size = domain.size
            this.description = domain.description
            this.mime = domain.mime
            this.userId = domain.userId
            this.uploadStatus = domain.uploadStatus
            this.created = domain.created
            this.changed = domain.changed
        }

    fun updateEntity(
        entity: com.revethq.documents.repository.entity.DocumentVersionEntity,
        domain: com.revethq.documents.domain.DocumentVersion,
    ): com.revethq.documents.repository.entity.DocumentVersionEntity {
        entity.apply {
            name = domain.name
            file = domain.file
            url = domain.url
            size = domain.size
            description = domain.description
            mime = domain.mime
            uploadStatus = domain.uploadStatus
        }
        return entity
    }
}
