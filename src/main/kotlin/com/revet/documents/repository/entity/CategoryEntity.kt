package com.revet.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*

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
    var project: com.revet.documents.repository.entity.ProjectEntity? = null
}
