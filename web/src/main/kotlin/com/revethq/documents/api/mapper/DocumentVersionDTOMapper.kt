package com.revethq.documents.api.mapper

import com.revethq.documents.domain.DocumentVersion
import com.revethq.documents.dto.DocumentVersionDTO

/**
 * Maps between Domain DocumentVersion and DTOs for the API layer.
 */
object DocumentVersionDTOMapper {
    fun toDTO(
        domain: com.revethq.documents.domain.DocumentVersion,
        downloadUrl: String? = null,
    ): com.revethq.documents.dto.DocumentVersionDTO =
        com.revethq.documents.dto.DocumentVersionDTO(
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
            downloadUrl = downloadUrl,
        )
}
