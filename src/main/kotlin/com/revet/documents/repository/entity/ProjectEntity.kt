package com.revet.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Panache entity for Project persistence.
 */
@Entity
@Table(name = "kala_projects")
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
    var organization: com.revet.documents.repository.entity.OrganizationEntity? = null

    @ManyToMany
    @JoinTable(
        name = "project_clients",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var clients: MutableSet<com.revet.documents.repository.entity.UserEntity> = mutableSetOf()

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
