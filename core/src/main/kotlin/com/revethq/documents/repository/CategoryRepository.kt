package com.revethq.documents.repository

import com.revethq.documents.domain.Category

interface CategoryRepository {
    fun findAll(): List<Category>

    fun findById(id: Long): Category?

    fun findByProjectId(projectId: Long): List<Category>

    fun save(category: Category): Category

    fun delete(id: Long): Boolean
}
