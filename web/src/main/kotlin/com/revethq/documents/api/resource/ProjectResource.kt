package com.revethq.documents.api.resource

import com.revethq.documents.api.mapper.ProjectDTOMapper
import com.revethq.documents.dto.AddClientRequest
import com.revethq.documents.dto.AddTagRequest
import com.revethq.documents.dto.CreateProjectRequest
import com.revethq.documents.dto.ProjectDTO
import com.revethq.documents.dto.UpdateProjectRequest
import com.revethq.documents.permission.Actions
import com.revethq.documents.service.ProjectService
import com.revethq.iam.permission.web.filter.RequiresPermission
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
import java.util.UUID

/**
 * REST Resource for Project endpoints.
 */
@Path("/api/v1/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Projects", description = "Project management endpoints")
class ProjectResource
    @Inject
    constructor(
        private val projectService: com.revethq.documents.service.ProjectService,
    ) {
        @GET
        @RequiresPermission(action = Actions.Project.LIST, resource = "urn:revet:documents:{tenantId}:project/*")
        @Operation(summary = "List all projects", description = "Retrieve a list of all active projects")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "List of projects",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun listProjects(
            @QueryParam("includeInactive")
            @Parameter(description = "Include inactive projects")
            includeInactive: Boolean = false,
            @QueryParam("organizationId")
            @Parameter(description = "Filter by organization ID")
            organizationId: Long? = null,
        ): List<com.revethq.documents.dto.ProjectDTO> {
            val projects =
                if (organizationId != null) {
                    projectService.getProjectsByOrganizationId(organizationId, includeInactive)
                } else {
                    projectService.getAllProjects(includeInactive)
                }
            return projects.map {
                com.revethq.documents.api.mapper.ProjectDTOMapper
                    .toDTO(it)
            }
        }

        @GET
        @Path("/{uuid}")
        @RequiresPermission(action = Actions.Project.GET, resource = "urn:revet:documents:{tenantId}:project/{uuid}")
        @Operation(summary = "Get project by UUID", description = "Retrieve a single project by its UUID")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Project found",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Project not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun getProject(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
        ): Response {
            val project =
                projectService.getProjectByUuid(uuid)
                    ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Project not found"))
                        .build()

            return Response.ok(ProjectDTOMapper.toDTO(project)).build()
        }

        @POST
        @RequiresPermission(action = Actions.Project.CREATE, resource = "urn:revet:documents:{tenantId}:project/*")
        @Operation(summary = "Create project", description = "Create a new project")
        @APIResponses(
            APIResponse(
                responseCode = "201",
                description = "Project created",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "400", description = "Invalid request"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun createProject(request: com.revethq.documents.dto.CreateProjectRequest): Response =
            try {
                val project =
                    projectService.createProject(
                        name = request.name,
                        organizationId = request.organizationId,
                        description = request.description,
                        clientIds = request.clientIds,
                        tags = request.tags,
                    )

                Response
                    .status(Response.Status.CREATED)
                    .entity(
                        com.revethq.documents.api.mapper.ProjectDTOMapper
                            .toDTO(project),
                    ).build()
            } catch (e: IllegalArgumentException) {
                Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to e.message))
                    .build()
            }

        @PUT
        @Path("/{uuid}")
        @RequiresPermission(action = Actions.Project.UPDATE, resource = "urn:revet:documents:{tenantId}:project/{uuid}")
        @Operation(summary = "Update project", description = "Update an existing project")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Project updated",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Project not found"),
            APIResponse(responseCode = "400", description = "Invalid request"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun updateProject(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            request: com.revethq.documents.dto.UpdateProjectRequest,
        ): Response {
            return try {
                val project =
                    projectService.updateProjectByUuid(
                        uuid = uuid,
                        name = request.name,
                        description = request.description,
                        clientIds = request.clientIds,
                        tags = request.tags,
                        isActive = request.isActive,
                    ) ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Project not found"))
                        .build()

                Response
                    .ok(
                        com.revethq.documents.api.mapper.ProjectDTOMapper
                            .toDTO(project),
                    ).build()
            } catch (e: IllegalArgumentException) {
                Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to e.message))
                    .build()
            }
        }

        @POST
        @Path("/{uuid}/clients")
        @RequiresPermission(action = Actions.Project.ADD_CLIENT, resource = "urn:revet:documents:{tenantId}:project/{uuid}")
        @Operation(summary = "Add client to project", description = "Add a client user to the project")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Client added",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Project not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun addClient(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            request: com.revethq.documents.dto.AddClientRequest,
        ): Response {
            val project =
                projectService.addClientToProjectByUuid(uuid, request.clientId)
                    ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Project not found"))
                        .build()

            return Response
                .ok(
                    com.revethq.documents.api.mapper.ProjectDTOMapper
                        .toDTO(project),
                ).build()
        }

        @DELETE
        @Path("/{uuid}/clients/{clientId}")
        @RequiresPermission(action = Actions.Project.REMOVE_CLIENT, resource = "urn:revet:documents:{tenantId}:project/{uuid}")
        @Operation(summary = "Remove client from project", description = "Remove a client user from the project")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Client removed",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Project not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun removeClient(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            @PathParam("clientId")
            @Parameter(description = "Client ID")
            clientId: UUID,
        ): Response {
            val project =
                projectService.removeClientFromProjectByUuid(uuid, clientId)
                    ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Project not found"))
                        .build()

            return Response
                .ok(
                    com.revethq.documents.api.mapper.ProjectDTOMapper
                        .toDTO(project),
                ).build()
        }

        @POST
        @Path("/{uuid}/tags")
        @RequiresPermission(action = Actions.Project.ADD_TAG, resource = "urn:revet:documents:{tenantId}:project/{uuid}")
        @Operation(summary = "Add tag to project", description = "Add a tag to the project")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Tag added",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Project not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun addTag(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            request: com.revethq.documents.dto.AddTagRequest,
        ): Response {
            return try {
                val project =
                    projectService.addTagToProjectByUuid(uuid, request.tag)
                        ?: return Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(mapOf("error" to "Project not found"))
                            .build()

                Response
                    .ok(
                        com.revethq.documents.api.mapper.ProjectDTOMapper
                            .toDTO(project),
                    ).build()
            } catch (e: IllegalArgumentException) {
                Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to e.message))
                    .build()
            }
        }

        @DELETE
        @Path("/{uuid}/tags/{tag}")
        @RequiresPermission(action = Actions.Project.REMOVE_TAG, resource = "urn:revet:documents:{tenantId}:project/{uuid}")
        @Operation(summary = "Remove tag from project", description = "Remove a tag from the project")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Tag removed",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Project not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun removeTag(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            @PathParam("tag")
            @Parameter(description = "Tag to remove")
            tag: String,
        ): Response {
            val project =
                projectService.removeTagFromProjectByUuid(uuid, tag)
                    ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Project not found"))
                        .build()

            return Response
                .ok(
                    com.revethq.documents.api.mapper.ProjectDTOMapper
                        .toDTO(project),
                ).build()
        }

        @DELETE
        @Path("/{uuid}")
        @RequiresPermission(action = Actions.Project.DELETE, resource = "urn:revet:documents:{tenantId}:project/{uuid}")
        @Operation(summary = "Delete project", description = "Soft delete a project")
        @APIResponses(
            APIResponse(responseCode = "204", description = "Project deleted"),
            APIResponse(responseCode = "404", description = "Project not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun deleteProject(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
        ): Response {
            val deleted = projectService.deleteProjectByUuid(uuid)
            return if (deleted) {
                Response.noContent().build()
            } else {
                Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Project not found"))
                    .build()
            }
        }
    }
