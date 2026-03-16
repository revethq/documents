package com.revethq.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Panache entity for Document persistence.
 */
@Entity
@Table(name = "revet_documents")
class DocumentEntity : PanacheEntity() {
    companion object : PanacheCompanion<DocumentEntity>

    @Column(unique = true, nullable = false)
    var uuid: UUID = UUID.randomUUID()

    @Column(nullable = false, length = 255)
    var name: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: com.revethq.core.repository.entity.ProjectEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: com.revethq.documents.repository.entity.CategoryEntity? = null

    @Column(length = 255)
    var mime: String? = null

    @Column(nullable = false)
    var date: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false, name = "is_active")
    var isActive: Boolean = true

    var removedAt: LocalDateTime? = null
}
