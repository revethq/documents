package com.revet.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*

/**
 * Panache entity for Tag persistence.
 * Maps to the existing taggit_tag table.
 */
@Entity
@Table(name = "taggit_tag")
class TagEntity : PanacheEntityBase {
    companion object : PanacheCompanion<TagEntity>

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(nullable = false, unique = true, length = 100)
    var name: String = ""

    @Column(nullable = false, unique = true, length = 100)
    var slug: String = ""
}
