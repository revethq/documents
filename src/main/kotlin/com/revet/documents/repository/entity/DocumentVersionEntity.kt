package com.revet.documents.repository.entity

import com.revet.documents.domain.UploadStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Panache entity for DocumentVersion persistence.
 * Maps to kala_document_version table with UUID as primary key.
 */
@Entity
@Table(name = "kala_document_version")
class DocumentVersionEntity : PanacheEntityBase {
    companion object : PanacheCompanion<DocumentVersionEntity>

    @Id
    var uuid: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    var document: com.revet.documents.repository.entity.DocumentEntity? = null

    @Column(nullable = false, length = 255)
    var name: String = ""

    @Column(length = 255)
    var file: String? = null

    @Column(nullable = false, length = 3000)
    var url: String = ""

    @Column(nullable = false)
    var size: Int = 0

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(length = 255)
    var mime: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: com.revet.documents.repository.entity.UserEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    var uploadStatus: com.revet.documents.domain.UploadStatus = _root_ide_package_.com.revet.documents.domain.UploadStatus.PENDING

    @Column(nullable = false)
    var created: OffsetDateTime = OffsetDateTime.now()

    @Column(nullable = false)
    var changed: OffsetDateTime = OffsetDateTime.now()

    @PreUpdate
    fun preUpdate() {
        changed = OffsetDateTime.now()
    }
}
