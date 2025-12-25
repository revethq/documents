package com.revet.documents.repository.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*

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
    var tag: com.revet.documents.repository.entity.TagEntity? = null
}
