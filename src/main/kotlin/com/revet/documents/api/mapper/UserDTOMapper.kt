package com.revet.documents.api.mapper

import com.revet.documents.domain.User
import com.revet.documents.dto.CreateUserRequest
import com.revet.documents.dto.UpdateUserRequest
import com.revet.documents.dto.UserDTO

/**
 * Maps between Domain User and DTOs for the API layer.
 */
object UserDTOMapper {

    fun toDTO(domain: com.revet.documents.domain.User): com.revet.documents.dto.UserDTO {
        return _root_ide_package_.com.revet.documents.dto.UserDTO(
            id = domain.id,
            uuid = domain.uuid,
            username = domain.username,
            email = domain.email,
            firstName = domain.firstName,
            lastName = domain.lastName,
            fullName = domain.fullName(),
            title = domain.profile.title,
            timezone = domain.profile.timezone,
            phoneOffice = domain.profile.phoneOffice,
            phoneMobile = domain.profile.phoneMobile,
            phoneFax = domain.profile.phoneFax,
            phoneExt = domain.profile.phoneExt,
            avatarUrl = domain.profile.avatarUrl,
            accessNewProjects = domain.profile.accessNewProjects,
            isActive = domain.isActive,
            isStaff = domain.isStaff,
            isSuperuser = domain.isSuperuser,
            createdAt = domain.timestamps.createdAt,
            modifiedAt = domain.timestamps.modifiedAt,
            lastLogin = domain.timestamps.lastLogin
        )
    }

    fun toUserProfile(request: com.revet.documents.dto.CreateUserRequest): com.revet.documents.domain.User.UserProfile {
        return _root_ide_package_.com.revet.documents.domain.User.UserProfile(
            title = request.title,
            timezone = request.timezone,
            phoneOffice = request.phoneOffice,
            phoneMobile = request.phoneMobile,
            phoneFax = request.phoneFax,
            phoneExt = request.phoneExt,
            avatarUrl = null,
            accessNewProjects = request.accessNewProjects
        )
    }

    fun toUserProfile(request: com.revet.documents.dto.UpdateUserRequest, existing: com.revet.documents.domain.User.UserProfile): com.revet.documents.domain.User.UserProfile? {
        // Only create new UserProfile if at least one profile field is provided
        if (listOf(
                request.title,
                request.timezone,
                request.phoneOffice,
                request.phoneMobile,
                request.phoneFax,
                request.phoneExt,
                request.avatarUrl,
                request.accessNewProjects
            ).all { it == null }
        ) {
            return null
        }

        return _root_ide_package_.com.revet.documents.domain.User.UserProfile(
            title = request.title ?: existing.title,
            timezone = request.timezone ?: existing.timezone,
            phoneOffice = request.phoneOffice ?: existing.phoneOffice,
            phoneMobile = request.phoneMobile ?: existing.phoneMobile,
            phoneFax = request.phoneFax ?: existing.phoneFax,
            phoneExt = request.phoneExt ?: existing.phoneExt,
            avatarUrl = request.avatarUrl ?: existing.avatarUrl,
            accessNewProjects = request.accessNewProjects ?: existing.accessNewProjects
        )
    }
}
