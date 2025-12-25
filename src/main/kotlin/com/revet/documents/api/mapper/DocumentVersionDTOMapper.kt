package com.revet.documents.api.mapper

import com.revet.documents.domain.DocumentVersion
import com.revet.documents.dto.DocumentVersionDTO

/**
 * Maps between Domain DocumentVersion and DTOs for the API layer.
 */
object DocumentVersionDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.DocumentVersion, downloadUrl: String? = null): com.revet.documents.dto.DocumentVersionDTO {
        return _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO(
            uuid = domain.uuid,
            documentId = domain.documentId,
            name = domain.name,
            file = domain.file,
            url = domain.url,
            size = domain.size,
            description = domain.description,
            mime = domain.mime,
            userId = domain.userId,
            uploadStatus = domain.uploadStatus,
            created = domain.created,
            changed = domain.changed,
            downloadUrl = downloadUrl
        )
    }
}
