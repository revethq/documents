package com.revet.documents.repository.entity

import com.revet.documents.domain.PermissionType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*

/**
 * Panache entity for OrganizationPermission persistence.
 */
@Entity
@Table(
    name = "organization_permissions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["organization_id", "user_id"])]
)
class OrganizationPermissionEntity : PanacheEntity() {
    companion object : PanacheCompanion<OrganizationPermissionEntity>

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: com.revet.documents.repository.entity.OrganizationEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: com.revet.documents.repository.entity.UserEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var permission: com.revet.documents.domain.PermissionType = _root_ide_package_.com.revet.documents.domain.PermissionType.CAN_CREATE
}
