package com.revet.documents.repository.mapper

import com.revet.documents.domain.Bucket
import com.revet.documents.repository.entity.BucketEntity

/**
 * Maps between Bucket domain model and BucketEntity.
 * Note: accessKey and secretKey are expected to be encrypted when stored in entity
 * and decrypted when converted to domain.
 */
object BucketMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.BucketEntity, decryptedAccessKey: String, decryptedSecretKey: String): com.revet.documents.domain.Bucket {
        return _root_ide_package_.com.revet.documents.domain.Bucket(
            id = entity.id,
            uuid = entity.uuid,
            name = entity.name,
            provider = entity.provider,
            bucketName = entity.bucketName,
            endpoint = entity.endpoint,
            region = entity.region,
            accessKey = decryptedAccessKey,
            secretKey = decryptedSecretKey,
            presignedUrlDurationMinutes = entity.presignedUrlDurationMinutes,
            isActive = entity.isActive,
            removedAt = entity.removedAt
        )
    }

    fun toEntity(domain: com.revet.documents.domain.Bucket, encryptedAccessKey: String, encryptedSecretKey: String): com.revet.documents.repository.entity.BucketEntity {
        val entity = _root_ide_package_.com.revet.documents.repository.entity.BucketEntity()
        if (domain.id != null) {
            entity.id = domain.id
        }
        entity.uuid = domain.uuid
        entity.name = domain.name
        entity.provider = domain.provider
        entity.bucketName = domain.bucketName
        entity.endpoint = domain.endpoint
        entity.region = domain.region
        entity.accessKey = encryptedAccessKey
        entity.secretKey = encryptedSecretKey
        entity.presignedUrlDurationMinutes = domain.presignedUrlDurationMinutes
        entity.isActive = domain.isActive
        entity.removedAt = domain.removedAt
        return entity
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.BucketEntity, domain: com.revet.documents.domain.Bucket, encryptedAccessKey: String, encryptedSecretKey: String) {
        entity.name = domain.name
        entity.bucketName = domain.bucketName
        entity.endpoint = domain.endpoint
        entity.region = domain.region
        entity.accessKey = encryptedAccessKey
        entity.secretKey = encryptedSecretKey
        entity.presignedUrlDurationMinutes = domain.presignedUrlDurationMinutes
        entity.isActive = domain.isActive
        entity.removedAt = domain.removedAt
    }
}
