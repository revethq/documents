package com.revet.documents.dto

/**
 * Data Transfer Object for Tag.
 */
data class TagDTO(
    val id: Int?,
    val name: String,
    val slug: String
)

data class CreateTagRequest(
    val name: String,
    val slug: String? = null
)

data class UpdateTagRequest(
    val name: String? = null,
    val slug: String? = null
)
