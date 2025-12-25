package com.revet.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * Panache entity for Organization persistence.
 * This is a repository concern and should not leak into the domain layer.
 */
@Entity
@Table(name = "kala_companies")
class OrganizationEntity : PanacheEntity() {
    companion object : PanacheCompanion<OrganizationEntity>

    @Column(unique = true, nullable = false)
    var uuid: UUID = UUID.randomUUID()

    @Column(nullable = false, length = 255)
    var name: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(length = 255)
    var address: String? = null

    @Column(length = 255)
    var city: String? = null

    @Column(length = 100)
    var state: String? = null

    @Column(name = "zipcode", length = 20)
    var zipCode: String? = null

    @Column(length = 100)
    var country: String? = null

    @Column(length = 50)
    var phone: String? = null

    @Column(length = 50)
    var fax: String? = null

    @Column(length = 255)
    var website: String? = null

    @Column(length = 2)
    var locale: String? = "en"

    @Column(nullable = false)
    var timezone: String = "UTC"

    @Column(name = "bucket_id")
    var bucketId: Long? = null

    @Column(name ="is_active", nullable = false)
    var isActive: Boolean = true

    @Column(name = "removedat")
    var removedAt: LocalDateTime? = null
}
