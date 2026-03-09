package com.revethq.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Panache entity for Project persistence.
 */
@Entity
@Table(name = "revet_projects")
class ProjectEntity : PanacheEntity() {
    companion object : PanacheCompanion<ProjectEntity>

    @Column(unique = true, nullable = false)
    var uuid: UUID = UUID.randomUUID()

    @Column(nullable = false, length = 255)
    var name: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: com.revethq.documents.repository.entity.OrganizationEntity? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_clients", joinColumns = [JoinColumn(name = "project_id")])
    @Column(name = "user_id")
    var clientIds: MutableSet<UUID> = mutableSetOf()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_tags", joinColumns = [JoinColumn(name = "project_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = mutableSetOf()

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "created", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "changed", nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "removed")
    var removedAt: LocalDate? = null

    @PreUpdate
    fun preUpdate() {
        modifiedAt = LocalDateTime.now()
    }
}
