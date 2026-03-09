package com.revethq.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * Panache entity for TaggedItem persistence.
 * Maps to the existing taggit_taggeditem table (Django's polymorphic tagging).
 */
@Entity
@Table(name = "taggit_taggeditem")
class TaggedItemEntity : PanacheEntityBase {
    companion object : PanacheCompanion<TaggedItemEntity> {
        const val DOCUMENT_CONTENT_TYPE_ID = 1
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "object_id", nullable = false)
    var objectId: Int = 0

    @Column(name = "content_type_id", nullable = false)
    var contentTypeId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: com.revethq.documents.repository.entity.TagEntity? = null
}
