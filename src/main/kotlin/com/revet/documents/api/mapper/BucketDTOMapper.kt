package com.revet.documents.api.mapper

import com.revet.documents.domain.Bucket
import com.revet.documents.dto.BucketDTO

/**
 * Maps between Domain Bucket and DTOs for the API layer.
 * Note: Credentials are intentionally not exposed in responses.
 */
object BucketDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.Bucket): com.revet.documents.dto.BucketDTO {
        return _root_ide_package_.com.revet.documents.dto.BucketDTO(
            id = domain.id,
            uuid = domain.uuid,
            name = domain.name,
            provider = domain.provider,
            bucketName = domain.bucketName,
            endpoint = domain.endpoint,
            region = domain.region,
            presignedUrlDurationMinutes = domain.presignedUrlDurationMinutes,
            isActive = domain.isActive
        )
    }
}
