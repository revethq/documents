package com.revet.documents.api.resource

import com.revet.documents.api.mapper.TagDTOMapper
import com.revet.documents.dto.*
import com.revet.documents.service.TagService
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
 * REST Resource for Tag endpoints.
 */
@Path("/api/v1/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Tags", description = "Tag management endpoints")
class TagResource @Inject constructor(
    private val tagService: com.revet.documents.service.TagService
) {

    @GET
    @Operation(summary = "List all tags", description = "Retrieve a list of all tags")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of tags",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.TagDTO::class))]
        )
    )
    fun listTags(): List<com.revet.documents.dto.TagDTO> {
        return tagService.getAllTags().map { _root_ide_package_.com.revet.documents.api.mapper.TagDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get tag by ID", description = "Retrieve a single tag by its ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Tag found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.TagDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Tag not found")
    )
    fun getTag(
        @PathParam("id")
        @Parameter(description = "Tag ID")
        id: Int
    ): Response {
        val tag = tagService.getTagById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .type("application/problem+json")
                .entity(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/not-found",
                        title = "Tag Not Found",
                        status = 404,
                        detail = "Tag with ID $id was not found"
                    )
                )
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.TagDTOMapper.toDTO(tag)).build()
    }

    @GET
    @Path("/slug/{slug}")
    @Operation(summary = "Get tag by slug", description = "Retrieve a single tag by its slug")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Tag found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.TagDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Tag not found")
    )
    fun getTagBySlug(
        @PathParam("slug")
        @Parameter(description = "Tag slug")
        slug: String
    ): Response {
        val tag = tagService.getTagBySlug(slug)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .type("application/problem+json")
                .entity(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/not-found",
                        title = "Tag Not Found",
                        status = 404,
                        detail = "Tag with slug '$slug' was not found"
                    )
                )
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.TagDTOMapper.toDTO(tag)).build()
    }

    @POST
    @Operation(summary = "Create tag", description = "Create a new tag")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Tag created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.TagDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "409", description = "Tag already exists")
    )
    fun createTag(request: com.revet.documents.dto.CreateTagRequest): Response {
        return try {
            val tag = tagService.createTag(
                name = request.name,
                slug = request.slug
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.TagDTOMapper.toDTO(tag))
                .build()
        } catch (e: IllegalArgumentException) {
            val status = if (e.message?.contains("already exists") == true) {
                Response.Status.CONFLICT
            } else {
                Response.Status.BAD_REQUEST
            }
            Response.status(status)
                .type("application/problem+json")
                .entity(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/${if (status == Response.Status.CONFLICT) "conflict" else "validation-error"}",
                        title = if (status == Response.Status.CONFLICT) "Tag Already Exists" else "Validation Error",
                        status = status.statusCode,
                        detail = e.message
                    )
                )
                .build()
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update tag", description = "Update an existing tag")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Tag updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.TagDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Tag not found"),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "409", description = "Tag name/slug already exists")
    )
    fun updateTag(
        @PathParam("id")
        @Parameter(description = "Tag ID")
        id: Int,
        request: com.revet.documents.dto.UpdateTagRequest
    ): Response {
        return try {
            val tag = tagService.updateTag(
                id = id,
                name = request.name,
                slug = request.slug
            ) ?: return Response.status(Response.Status.NOT_FOUND)
                .type("application/problem+json")
                .entity(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/not-found",
                        title = "Tag Not Found",
                        status = 404,
                        detail = "Tag with ID $id was not found"
                    )
                )
                .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.TagDTOMapper.toDTO(tag)).build()
        } catch (e: IllegalArgumentException) {
            val status = if (e.message?.contains("already exists") == true) {
                Response.Status.CONFLICT
            } else {
                Response.Status.BAD_REQUEST
            }
            Response.status(status)
                .type("application/problem+json")
                .entity(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/${if (status == Response.Status.CONFLICT) "conflict" else "validation-error"}",
                        title = if (status == Response.Status.CONFLICT) "Tag Already Exists" else "Validation Error",
                        status = status.statusCode,
                        detail = e.message
                    )
                )
                .build()
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete tag", description = "Delete a tag")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Tag deleted"),
        APIResponse(responseCode = "404", description = "Tag not found")
    )
    fun deleteTag(
        @PathParam("id")
        @Parameter(description = "Tag ID")
        id: Int
    ): Response {
        val deleted = tagService.deleteTag(id)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .type("application/problem+json")
                .entity(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/not-found",
                        title = "Tag Not Found",
                        status = 404,
                        detail = "Tag with ID $id was not found"
                    )
                )
                .build()
        }
    }
}
