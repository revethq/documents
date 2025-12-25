package com.revet.documents.repository.mapper

import com.revet.documents.domain.DocumentVersion
import com.revet.documents.repository.entity.DocumentVersionEntity

/**
 * Maps between Domain DocumentVersion and DocumentVersionEntity (Panache).
 */
object DocumentVersionMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.DocumentVersionEntity): com.revet.documents.domain.DocumentVersion {
        return _root_ide_package_.com.revet.documents.domain.DocumentVersion(
            uuid = entity.uuid,
            documentId = entity.document?.id,
            name = entity.name,
            file = entity.file,
            url = entity.url,
            size = entity.size,
            description = entity.description,
            mime = entity.mime,
            userId = entity.user?.id?.toInt(),
            uploadStatus = entity.uploadStatus,
            created = entity.created,
            changed = entity.changed
        )
    }

    fun toEntity(domain: com.revet.documents.domain.DocumentVersion): com.revet.documents.repository.entity.DocumentVersionEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.DocumentVersionEntity().apply {
            this.uuid = domain.uuid
            this.name = domain.name
            this.file = domain.file
            this.url = domain.url
            this.size = domain.size
            this.description = domain.description
            this.mime = domain.mime
            this.uploadStatus = domain.uploadStatus
            this.created = domain.created
            this.changed = domain.changed
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.DocumentVersionEntity, domain: com.revet.documents.domain.DocumentVersion): com.revet.documents.repository.entity.DocumentVersionEntity {
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
