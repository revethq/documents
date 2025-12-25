package com.revet.documents.repository.entity

import com.revet.documents.domain.PermissionType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.*

/**
 * Panache entity for ProjectPermission persistence.
 */
@Entity
@Table(
    name = "project_permissions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "user_id"])]
)
class ProjectPermissionEntity : PanacheEntity() {
    companion object : PanacheCompanion<ProjectPermissionEntity>

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: com.revet.documents.repository.entity.ProjectEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: com.revet.documents.repository.entity.UserEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var permission: com.revet.documents.domain.PermissionType = _root_ide_package_.com.revet.documents.domain.PermissionType.CAN_CREATE
}
