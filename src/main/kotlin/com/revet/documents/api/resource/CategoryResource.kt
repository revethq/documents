package com.revet.documents.api.resource

import com.revet.documents.api.mapper.CategoryDTOMapper
import com.revet.documents.dto.CategoryDTO
import com.revet.documents.dto.CreateCategoryRequest
import com.revet.documents.dto.UpdateCategoryRequest
import com.revet.documents.service.CategoryService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag

/**
 * REST Resource for Category endpoints.
 */
@Path("/api/v1/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Categories", description = "Category management endpoints")
class CategoryResource @Inject constructor(
    private val categoryService: com.revet.documents.service.CategoryService
) {

    @GET
    @Operation(summary = "List all categories", description = "Retrieve a list of all categories")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of categories",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.CategoryDTO::class))]
        )
    )
    fun listCategories(
        @QueryParam("projectId")
        @Parameter(description = "Filter by project ID")
        projectId: Long? = null
    ): List<com.revet.documents.dto.CategoryDTO> {
        val categories = if (projectId != null) {
            categoryService.getCategoriesByProjectId(projectId)
        } else {
            categoryService.getAllCategories()
        }
        return categories.map { _root_ide_package_.com.revet.documents.api.mapper.CategoryDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a single category by its ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Category found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.CategoryDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Category not found")
    )
    fun getCategory(
        @PathParam("id")
        @Parameter(description = "Category ID")
        id: Long
    ): Response {
        val category = categoryService.getCategoryById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Category not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.CategoryDTOMapper.toDTO(category)).build()
    }

    @POST
    @Operation(summary = "Create category", description = "Create a new category")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Category created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.CategoryDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createCategory(request: com.revet.documents.dto.CreateCategoryRequest): Response {
        return try {
            val category = categoryService.createCategory(
                name = request.name,
                projectId = request.projectId
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.CategoryDTOMapper.toDTO(category))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Category updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.CategoryDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Category not found"),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun updateCategory(
        @PathParam("id")
        @Parameter(description = "Category ID")
        id: Long,
        request: com.revet.documents.dto.UpdateCategoryRequest
    ): Response {
        return try {
            val category = categoryService.updateCategory(id, request.name)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Category not found"))
                    .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.CategoryDTOMapper.toDTO(category)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Category deleted"),
        APIResponse(responseCode = "404", description = "Category not found")
    )
    fun deleteCategory(
        @PathParam("id")
        @Parameter(description = "Category ID")
        id: Long
    ): Response {
        val deleted = categoryService.deleteCategory(id)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Category not found"))
                .build()
        }
    }
}
