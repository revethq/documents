package com.revethq.documents.api.resource

import com.revethq.core.api.mapper.TagDTOMapper
import com.revethq.core.dto.CreateTagRequest
import com.revethq.core.dto.TagDTO
import com.revethq.core.dto.UpdateTagRequest
import com.revethq.documents.dto.ProblemDetail
import com.revethq.documents.permission.Actions
import com.revethq.documents.service.TagService
import com.revethq.iam.permission.web.filter.RequiresPermission
import io.quarkus.security.Authenticated
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
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
class TagResource
    @Inject
    constructor(
        private val tagService: TagService,
    ) {
        @GET
        @Authenticated
        @Operation(summary = "List all tags", description = "Retrieve a list of all tags for an organization")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "List of tags",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = TagDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun listTags(
            @QueryParam("organizationId")
            @Parameter(description = "Organization ID", required = true)
            organizationId: Long,
        ): List<TagDTO> = tagService.getAllTags(organizationId).map { TagDTOMapper.toDTO(it) }

        @GET
        @Path("/{id}")
        @RequiresPermission(action = Actions.Tag.GET, resource = "urn:revet:documents:{tenantId}:tag/{id}")
        @Operation(summary = "Get tag by ID", description = "Retrieve a single tag by its ID")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Tag found",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = TagDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Tag not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun getTag(
            @PathParam("id")
            @Parameter(description = "Tag ID")
            id: Int,
        ): Response {
            val tag =
                tagService.getTagById(id)
                    ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .type("application/problem+json")
                        .entity(
                            ProblemDetail(
                                type = "https://docs.revethq.com/problems/not-found",
                                title = "Tag Not Found",
                                status = 404,
                                detail = "Tag with ID $id was not found",
                            ),
                        ).build()

            return Response.ok(TagDTOMapper.toDTO(tag)).build()
        }

        @GET
        @Path("/slug/{slug}")
        @RequiresPermission(action = Actions.Tag.GET, resource = "urn:revet:documents:{tenantId}:tag/*")
        @Operation(summary = "Get tag by slug", description = "Retrieve a single tag by its slug")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Tag found",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = TagDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Tag not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun getTagBySlug(
            @PathParam("slug")
            @Parameter(description = "Tag slug")
            slug: String,
            @QueryParam("organizationId")
            @Parameter(description = "Organization ID", required = true)
            organizationId: Long,
        ): Response {
            val tag =
                tagService.getTagBySlug(slug, organizationId)
                    ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .type("application/problem+json")
                        .entity(
                            ProblemDetail(
                                type = "https://docs.revethq.com/problems/not-found",
                                title = "Tag Not Found",
                                status = 404,
                                detail = "Tag with slug '$slug' was not found",
                            ),
                        ).build()

            return Response.ok(TagDTOMapper.toDTO(tag)).build()
        }

        @POST
        @RequiresPermission(action = Actions.Tag.CREATE, resource = "urn:revet:documents:{tenantId}:tag/*")
        @Operation(summary = "Create tag", description = "Create a new tag")
        @APIResponses(
            APIResponse(
                responseCode = "201",
                description = "Tag created",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = TagDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "400", description = "Invalid request"),
            APIResponse(responseCode = "409", description = "Tag already exists"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun createTag(
            @QueryParam("organizationId")
            @Parameter(description = "Organization ID", required = true)
            organizationId: Long,
            request: CreateTagRequest,
        ): Response =
            try {
                val tag =
                    tagService.createTag(
                        name = request.name,
                        organizationId = organizationId,
                        slug = request.slug,
                    )

                Response
                    .status(Response.Status.CREATED)
                    .entity(TagDTOMapper.toDTO(tag))
                    .build()
            } catch (e: IllegalArgumentException) {
                val status =
                    if (e.message?.contains("already exists") == true) {
                        Response.Status.CONFLICT
                    } else {
                        Response.Status.BAD_REQUEST
                    }
                Response
                    .status(status)
                    .type("application/problem+json")
                    .entity(
                        ProblemDetail(
                            type =
                                "https://docs.revethq.com/problems/" +
                                    if (status == Response.Status.CONFLICT) "conflict" else "validation-error",
                            title =
                                if (status == Response.Status.CONFLICT) {
                                    "Tag Already Exists"
                                } else {
                                    "Validation Error"
                                },
                            status = status.statusCode,
                            detail = e.message,
                        ),
                    ).build()
            }

        @PUT
        @Path("/{id}")
        @RequiresPermission(action = Actions.Tag.UPDATE, resource = "urn:revet:documents:{tenantId}:tag/{id}")
        @Operation(summary = "Update tag", description = "Update an existing tag")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Tag updated",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = TagDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Tag not found"),
            APIResponse(responseCode = "400", description = "Invalid request"),
            APIResponse(responseCode = "409", description = "Tag name/slug already exists"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun updateTag(
            @PathParam("id")
            @Parameter(description = "Tag ID")
            id: Int,
            request: UpdateTagRequest,
        ): Response {
            return try {
                val tag =
                    tagService.updateTag(
                        id = id,
                        name = request.name,
                        slug = request.slug,
                    ) ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .type("application/problem+json")
                        .entity(
                            ProblemDetail(
                                type = "https://docs.revethq.com/problems/not-found",
                                title = "Tag Not Found",
                                status = 404,
                                detail = "Tag with ID $id was not found",
                            ),
                        ).build()

                Response.ok(TagDTOMapper.toDTO(tag)).build()
            } catch (e: IllegalArgumentException) {
                val status =
                    if (e.message?.contains("already exists") == true) {
                        Response.Status.CONFLICT
                    } else {
                        Response.Status.BAD_REQUEST
                    }
                Response
                    .status(status)
                    .type("application/problem+json")
                    .entity(
                        ProblemDetail(
                            type =
                                "https://docs.revethq.com/problems/" +
                                    if (status == Response.Status.CONFLICT) "conflict" else "validation-error",
                            title =
                                if (status == Response.Status.CONFLICT) {
                                    "Tag Already Exists"
                                } else {
                                    "Validation Error"
                                },
                            status = status.statusCode,
                            detail = e.message,
                        ),
                    ).build()
            }
        }

        @DELETE
        @Path("/{id}")
        @RequiresPermission(action = Actions.Tag.DELETE, resource = "urn:revet:documents:{tenantId}:tag/{id}")
        @Operation(summary = "Delete tag", description = "Delete a tag")
        @APIResponses(
            APIResponse(responseCode = "204", description = "Tag deleted"),
            APIResponse(responseCode = "404", description = "Tag not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun deleteTag(
            @PathParam("id")
            @Parameter(description = "Tag ID")
            id: Int,
        ): Response {
            val deleted = tagService.deleteTag(id)
            return if (deleted) {
                Response.noContent().build()
            } else {
                Response
                    .status(Response.Status.NOT_FOUND)
                    .type("application/problem+json")
                    .entity(
                        ProblemDetail(
                            type = "https://docs.revethq.com/problems/not-found",
                            title = "Tag Not Found",
                            status = 404,
                            detail = "Tag with ID $id was not found",
                        ),
                    ).build()
            }
        }
    }
