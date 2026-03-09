package com.revethq.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * Panache entity for Category persistence.
 */
@Entity
@Table(name = "projects_category")
class CategoryEntity : PanacheEntity() {
    companion object : PanacheCompanion<CategoryEntity>

    @Column(nullable = false, length = 255)
    var name: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: com.revethq.documents.repository.entity.ProjectEntity? = null
}
