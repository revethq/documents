package com.revet.documents.dto

/**
 * Data Transfer Object for Category.
 */
data class CategoryDTO(
    val id: Long?,
    val name: String,
    val projectId: Long
)

data class CreateCategoryRequest(
    val name: String,
    val projectId: Long
)

data class UpdateCategoryRequest(
    val name: String
)
