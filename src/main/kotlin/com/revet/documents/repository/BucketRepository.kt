package com.revet.documents.repository

import com.revet.documents.domain.Bucket
import com.revet.documents.repository.entity.BucketEntity
import com.revet.documents.repository.mapper.BucketMapper
import com.revet.documents.service.EncryptionService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.util.*

/**
 * Repository interface for Bucket persistence operations.
 */
interface BucketRepository {
    fun findAll(includeInactive: Boolean = false): List<com.revet.documents.domain.Bucket>
    fun findById(id: Long): com.revet.documents.domain.Bucket?
    fun findByUuid(uuid: UUID): com.revet.documents.domain.Bucket?
    fun save(bucket: com.revet.documents.domain.Bucket): com.revet.documents.domain.Bucket
    fun delete(id: Long): Boolean
}

/**
 * Panache-based implementation of BucketRepository.
 * Handles encryption/decryption of credentials during persistence.
 */
@ApplicationScoped
class BucketRepositoryImpl @Inject constructor(
    private val encryptionService: com.revet.documents.service.EncryptionService
) : com.revet.documents.repository.BucketRepository {

    override fun findAll(includeInactive: Boolean): List<com.revet.documents.domain.Bucket> {
        val entities = if (includeInactive) {
            _root_ide_package_.com.revet.documents.repository.entity.BucketEntity.listAll()
        } else {
            _root_ide_package_.com.revet.documents.repository.entity.BucketEntity.list("isActive", true)
        }
        return entities.map { entityToDomain(it) }
    }

    override fun findById(id: Long): com.revet.documents.domain.Bucket? {
        return _root_ide_package_.com.revet.documents.repository.entity.BucketEntity.findById(id)?.let { entityToDomain(it) }
    }

    override fun findByUuid(uuid: UUID): com.revet.documents.domain.Bucket? {
        return _root_ide_package_.com.revet.documents.repository.entity.BucketEntity.find("uuid", uuid).firstResult()
            ?.let { entityToDomain(it) }
    }

    @Transactional
    override fun save(bucket: com.revet.documents.domain.Bucket): com.revet.documents.domain.Bucket {
        val encryptedAccessKey = encryptionService.encrypt(bucket.accessKey)
        val encryptedSecretKey = encryptionService.encrypt(bucket.secretKey)

        val entity = if (bucket.isNew()) {
            _root_ide_package_.com.revet.documents.repository.mapper.BucketMapper.toEntity(bucket, encryptedAccessKey, encryptedSecretKey).also { it.persist() }
        } else {
            val existing = _root_ide_package_.com.revet.documents.repository.entity.BucketEntity.findById(bucket.id!!)
                ?: throw IllegalArgumentException("Bucket with id ${bucket.id} not found")
            _root_ide_package_.com.revet.documents.repository.mapper.BucketMapper.updateEntity(existing, bucket, encryptedAccessKey, encryptedSecretKey)
            existing
        }
        return entityToDomain(entity)
    }

    @Transactional
    override fun delete(id: Long): Boolean {
        val entity = _root_ide_package_.com.revet.documents.repository.entity.BucketEntity.findById(id) ?: return false
        entity.isActive = false
        entity.removedAt = java.time.LocalDateTime.now()
        return true
    }

    private fun entityToDomain(entity: com.revet.documents.repository.entity.BucketEntity): com.revet.documents.domain.Bucket {
        val decryptedAccessKey = encryptionService.decrypt(entity.accessKey)
        val decryptedSecretKey = encryptionService.decrypt(entity.secretKey)
        return _root_ide_package_.com.revet.documents.repository.mapper.BucketMapper.toDomain(entity, decryptedAccessKey, decryptedSecretKey)
    }
}
