package com.revet.documents.repository.mapper

import com.revet.documents.domain.User
import com.revet.documents.repository.entity.UserEntity

/**
 * Maps between Domain User and UserEntity (Panache).
 */
object UserMapper {

    fun toDomain(entity: com.revet.documents.repository.entity.UserEntity): com.revet.documents.domain.User {
        return _root_ide_package_.com.revet.documents.domain.User(
            id = entity.id,
            uuid = entity.uuid,
            username = entity.username,
            email = entity.email,
            firstName = entity.firstName,
            lastName = entity.lastName,
            profile = _root_ide_package_.com.revet.documents.domain.User.UserProfile(
                title = entity.title,
                timezone = entity.timezone,
                phoneOffice = entity.phoneOffice,
                phoneMobile = entity.phoneMobile,
                phoneFax = entity.phoneFax,
                phoneExt = entity.phoneExt,
                avatarUrl = entity.avatarUrl,
                accessNewProjects = entity.accessNewProjects
            ),
            isActive = entity.isActive,
            isStaff = entity.isStaff,
            isSuperuser = entity.isSuperuser,
            timestamps = _root_ide_package_.com.revet.documents.domain.User.Timestamps(
                createdAt = entity.createdAt,
                modifiedAt = entity.modifiedAt,
                lastLogin = entity.lastLogin
            )
        )
    }

    fun toEntity(domain: com.revet.documents.domain.User): com.revet.documents.repository.entity.UserEntity {
        return _root_ide_package_.com.revet.documents.repository.entity.UserEntity().apply {
            domain.id?.let { this.id = it }
            this.uuid = domain.uuid
            this.username = domain.username
            this.email = domain.email
            this.firstName = domain.firstName
            this.lastName = domain.lastName
            this.title = domain.profile.title
            this.timezone = domain.profile.timezone
            this.phoneOffice = domain.profile.phoneOffice
            this.phoneMobile = domain.profile.phoneMobile
            this.phoneFax = domain.profile.phoneFax
            this.phoneExt = domain.profile.phoneExt
            this.avatarUrl = domain.profile.avatarUrl
            this.accessNewProjects = domain.profile.accessNewProjects
            this.isActive = domain.isActive
            this.isStaff = domain.isStaff
            this.isSuperuser = domain.isSuperuser
            this.createdAt = domain.timestamps.createdAt
            this.modifiedAt = domain.timestamps.modifiedAt
            this.lastLogin = domain.timestamps.lastLogin
        }
    }

    fun updateEntity(entity: com.revet.documents.repository.entity.UserEntity, domain: com.revet.documents.domain.User): com.revet.documents.repository.entity.UserEntity {
        entity.apply {
            email = domain.email
            firstName = domain.firstName
            lastName = domain.lastName
            title = domain.profile.title
            timezone = domain.profile.timezone
            phoneOffice = domain.profile.phoneOffice
            phoneMobile = domain.profile.phoneMobile
            phoneFax = domain.profile.phoneFax
            phoneExt = domain.profile.phoneExt
            avatarUrl = domain.profile.avatarUrl
            accessNewProjects = domain.profile.accessNewProjects
            isActive = domain.isActive
            isStaff = domain.isStaff
            isSuperuser = domain.isSuperuser
            modifiedAt = domain.timestamps.modifiedAt
            lastLogin = domain.timestamps.lastLogin
        }
        return entity
    }
}
