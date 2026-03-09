package com.revethq.documents.repository.entity

import com.revethq.documents.domain.UploadStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Panache entity for DocumentVersion persistence.
 * Maps to revet_document_versions table with UUID as primary key.
 */
@Entity
@Table(name = "revet_document_versions")
class DocumentVersionEntity : PanacheEntityBase {
    companion object : PanacheCompanion<DocumentVersionEntity>

    @Id
    var uuid: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    var document: com.revethq.documents.repository.entity.DocumentEntity? = null

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

    @Column(name = "user_id")
    var userId: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    var uploadStatus: com.revethq.documents.domain.UploadStatus = com.revethq.documents.domain.UploadStatus.PENDING

    @Column(nullable = false)
    var created: OffsetDateTime = OffsetDateTime.now()

    @Column(nullable = false)
    var changed: OffsetDateTime = OffsetDateTime.now()

    @PreUpdate
    fun preUpdate() {
        changed = OffsetDateTime.now()
    }
}
