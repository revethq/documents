package com.revet.documents.api.resource

import com.revet.documents.api.mapper.ProjectDTOMapper
import com.revet.documents.dto.*
import com.revet.documents.service.ProjectService
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
import java.util.*

/**
 * REST Resource for Project endpoints.
 */
@Path("/api/v1/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Projects", description = "Project management endpoints")
class ProjectResource @Inject constructor(
    private val projectService: com.revet.documents.service.ProjectService
) {

    @GET
    @Operation(summary = "List all projects", description = "Retrieve a list of all active projects")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of projects",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        )
    )
    fun listProjects(
        @QueryParam("includeInactive")
        @Parameter(description = "Include inactive projects")
        includeInactive: Boolean = false,
        @QueryParam("organizationId")
        @Parameter(description = "Filter by organization ID")
        organizationId: Long? = null
    ): List<com.revet.documents.dto.ProjectDTO> {
        val projects = if (organizationId != null) {
            projectService.getProjectsByOrganizationId(organizationId, includeInactive)
        } else {
            projectService.getAllProjects(includeInactive)
        }
        return projects.map { _root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a single project by its ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Project found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Project not found")
    )
    fun getProject(
        @PathParam("id")
        @Parameter(description = "Project ID")
        id: Long
    ): Response {
        val project = projectService.getProjectById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project)).build()
    }

    @GET
    @Path("/uuid/{uuid}")
    @Operation(summary = "Get project by UUID", description = "Retrieve a single project by its UUID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Project found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Project not found")
    )
    fun getProjectByUuid(
        @PathParam("uuid")
        @Parameter(description = "Project UUID")
        uuid: UUID
    ): Response {
        val project = projectService.getProjectByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project)).build()
    }

    @POST
    @Operation(summary = "Create project", description = "Create a new project")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Project created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createProject(request: com.revet.documents.dto.CreateProjectRequest): Response {
        return try {
            val project = projectService.createProject(
                name = request.name,
                organizationId = request.organizationId,
                description = request.description,
                clientIds = request.clientIds,
                tags = request.tags
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update project", description = "Update an existing project")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Project updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Project not found"),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun updateProject(
        @PathParam("id")
        @Parameter(description = "Project ID")
        id: Long,
        request: com.revet.documents.dto.UpdateProjectRequest
    ): Response {
        return try {
            val project = projectService.updateProject(
                id = id,
                name = request.name,
                description = request.description,
                clientIds = request.clientIds,
                tags = request.tags,
                isActive = request.isActive
            ) ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @POST
    @Path("/{id}/clients")
    @Operation(summary = "Add client to project", description = "Add a client user to the project")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Client added",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Project not found")
    )
    fun addClient(
        @PathParam("id")
        @Parameter(description = "Project ID")
        id: Long,
        request: com.revet.documents.dto.AddClientRequest
    ): Response {
        val project = projectService.addClientToProject(id, request.clientId)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project)).build()
    }

    @DELETE
    @Path("/{id}/clients/{clientId}")
    @Operation(summary = "Remove client from project", description = "Remove a client user from the project")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Client removed",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Project not found")
    )
    fun removeClient(
        @PathParam("id")
        @Parameter(description = "Project ID")
        id: Long,
        @PathParam("clientId")
        @Parameter(description = "Client ID")
        clientId: Long
    ): Response {
        val project = projectService.removeClientFromProject(id, clientId)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project)).build()
    }

    @POST
    @Path("/{id}/tags")
    @Operation(summary = "Add tag to project", description = "Add a tag to the project")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Tag added",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Project not found")
    )
    fun addTag(
        @PathParam("id")
        @Parameter(description = "Project ID")
        id: Long,
        request: com.revet.documents.dto.AddTagRequest
    ): Response {
        return try {
            val project = projectService.addTagToProject(id, request.tag)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Project not found"))
                    .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{id}/tags/{tag}")
    @Operation(summary = "Remove tag from project", description = "Remove a tag from the project")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Tag removed",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.ProjectDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Project not found")
    )
    fun removeTag(
        @PathParam("id")
        @Parameter(description = "Project ID")
        id: Long,
        @PathParam("tag")
        @Parameter(description = "Tag to remove")
        tag: String
    ): Response {
        val project = projectService.removeTagFromProject(id, tag)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.ProjectDTOMapper.toDTO(project)).build()
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete project", description = "Soft delete a project")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Project deleted"),
        APIResponse(responseCode = "404", description = "Project not found")
    )
    fun deleteProject(
        @PathParam("id")
        @Parameter(description = "Project ID")
        id: Long
    ): Response {
        val deleted = projectService.deleteProject(id)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()
        }
    }
}
