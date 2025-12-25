package com.revet.documents.repository.entity

import com.revet.documents.domain.StorageProvider
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * Panache entity for Bucket persistence.
 * Credentials (accessKey, secretKey) are stored encrypted.
 */
@Entity
@Table(name = "kala_buckets")
class BucketEntity : PanacheEntity() {
    companion object : PanacheCompanion<BucketEntity>

    @Column(unique = true, nullable = false)
    var uuid: UUID = UUID.randomUUID()

    @Column(nullable = false, length = 255)
    var name: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var provider: com.revet.documents.domain.StorageProvider = _root_ide_package_.com.revet.documents.domain.StorageProvider.S3

    @Column(name = "bucket_name", nullable = false, length = 255)
    var bucketName: String = ""

    @Column(length = 512)
    var endpoint: String? = null

    @Column(length = 100)
    var region: String? = null

    @Column(name = "access_key", nullable = false, columnDefinition = "TEXT")
    var accessKey: String = ""

    @Column(name = "secret_key", nullable = false, columnDefinition = "TEXT")
    var secretKey: String = ""

    @Column(name = "presigned_url_duration_minutes", nullable = false)
    var presignedUrlDurationMinutes: Int = 15

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "removed_at")
    var removedAt: LocalDateTime? = null
}
