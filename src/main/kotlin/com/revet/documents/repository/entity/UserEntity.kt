package com.revet.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * Panache entity for User persistence.
 */
@Entity
@Table(name = "users")
class UserEntity : PanacheEntity() {
    companion object : PanacheCompanion<UserEntity>

    @Column(unique = true, nullable = false)
    var uuid: UUID = UUID.randomUUID()

    @Column(unique = true, nullable = false, length = 150)
    var username: String = ""

    @Column(unique = true, nullable = false, length = 255)
    var email: String = ""

    @Column(length = 150)
    var firstName: String? = null

    @Column(length = 150)
    var lastName: String? = null

    @Column(length = 255)
    var title: String? = null

    @Column(length = 100)
    var timezone: String? = null

    @Column(length = 50)
    var phoneOffice: String? = null

    @Column(length = 50)
    var phoneMobile: String? = null

    @Column(length = 50)
    var phoneFax: String? = null

    @Column(length = 10)
    var phoneExt: String? = null

    @Column(length = 500)
    var avatarUrl: String? = null

    @Column(nullable = false)
    var accessNewProjects: Boolean = true

    @Column(nullable = false)
    var isActive: Boolean = true

    @Column(nullable = false)
    var isStaff: Boolean = false

    @Column(nullable = false)
    var isSuperuser: Boolean = false

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    var lastLogin: LocalDateTime? = null

    @ManyToMany
    @JoinTable(
        name = "user_organizations",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "organization_id")]
    )
    var organizations: MutableSet<com.revet.documents.repository.entity.OrganizationEntity> = mutableSetOf()

    @PreUpdate
    fun preUpdate() {
        modifiedAt = LocalDateTime.now()
    }
}
