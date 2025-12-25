package com.revet.documents.domain

import java.time.LocalDateTime
import java.util.*

/**
 * Core domain model for Organization.
 * This represents the business concept independent of persistence or API concerns.
 */
data class Organization(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val description: String?,
    val contactInfo: ContactInfo,
    val locale: String?,
    val timezone: String,
    val bucketId: Long?,
    val isActive: Boolean,
    val removedAt: LocalDateTime?
) {
    data class ContactInfo(
        val address: String?,
        val city: String?,
        val state: String?,
        val zipCode: String?,
        val country: String?,
        val phone: String?,
        val fax: String?,
        val website: String?
    )

    companion object {
        fun create(
            name: String,
            description: String? = null,
            contactInfo: ContactInfo = ContactInfo(null, null, null, null, null, null, null, null),
            locale: String? = "en",
            timezone: String = "UTC",
            bucketId: Long? = null
        ): Organization {
            return Organization(
                id = null,
                uuid = UUID.randomUUID(),
                name = name,
                description = description,
                contactInfo = contactInfo,
                locale = locale,
                timezone = timezone,
                bucketId = bucketId,
                isActive = true,
                removedAt = null
            )
        }
    }

    fun update(
        name: String? = null,
        description: String? = null,
        contactInfo: ContactInfo? = null,
        locale: String? = null,
        timezone: String? = null,
        bucketId: Long? = null,
        isActive: Boolean? = null
    ): Organization {
        return this.copy(
            name = name ?: this.name,
            description = description ?: this.description,
            contactInfo = contactInfo ?: this.contactInfo,
            locale = locale ?: this.locale,
            timezone = timezone ?: this.timezone,
            bucketId = bucketId ?: this.bucketId,
            isActive = isActive ?: this.isActive
        )
    }

    fun deactivate(): Organization {
        return this.copy(
            isActive = false,
            removedAt = LocalDateTime.now()
        )
    }

    fun isNew(): Boolean = id == null
}
