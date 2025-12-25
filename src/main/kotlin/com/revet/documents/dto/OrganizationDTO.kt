package com.revet.documents.dto

import java.time.LocalDateTime
import java.util.*

/**
 * Data Transfer Object for Organization.
 * Used for API request/response serialization.
 */
data class OrganizationDTO(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val fax: String? = null,
    val website: String? = null,
    val locale: String? = "en",
    val timezone: String = "UTC",
    val bucketId: Long? = null,
    val isActive: Boolean = true
)

data class CreateOrganizationRequest(
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val fax: String? = null,
    val website: String? = null,
    val locale: String? = "en",
    val timezone: String = "UTC",
    val bucketId: Long? = null
)

data class UpdateOrganizationRequest(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val fax: String? = null,
    val website: String? = null,
    val locale: String? = null,
    val timezone: String? = null,
    val bucketId: Long? = null,
    val isActive: Boolean? = null
)
