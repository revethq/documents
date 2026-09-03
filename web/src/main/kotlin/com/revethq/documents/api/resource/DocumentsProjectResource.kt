package com.revethq.documents.api.resource

import com.revethq.core.api.resource.ProjectResource
import com.revethq.core.dto.AddClientRequest
import com.revethq.core.dto.AddTagRequest
import com.revethq.core.dto.CreateProjectRequest
import com.revethq.core.dto.ProjectDTO
import com.revethq.core.dto.UpdateProjectRequest
import com.revethq.core.service.ProjectService
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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.UUID

/**
 * Local subclass of the library's ProjectResource. The library resource is disabled via
 * revet.core.resources.disabled=true in application.properties, so this subclass is
 * the only JAX-RS registration for /api/v1/projects.
 *
 * The list endpoint is overridden to use @Authenticated instead of @RequiresPermission,
 * with permission filtering handled by the service layer via PermissionFilterService.
 *
 * All other endpoints explicitly override and delegate to super so that the OpenAPI spec
 * generator (which only scans locally declared methods) includes them in the output.
 */
@Path("/api/v1/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Projects", description = "Project management endpoints")
class DocumentsProjectResource
    @Inject
    constructor(
        projectService: ProjectService,
    ) : ProjectResource(projectService) {
        @GET
        @Authenticated
        override fun listProjects(
            @QueryParam("includeInactive")
            @Parameter(description = "Include inactive projects")
            includeInactive: Boolean,
            @QueryParam("organizationId")
            @Parameter(description = "Filter by organization ID", required = false)
            organizationId: Long?,
        ): List<ProjectDTO> = super.listProjects(includeInactive, organizationId)

        @GET
        @Path("/{uuid}")
        override fun getProject(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
        ): Response = super.getProject(uuid)

        @POST
        override fun createProject(request: CreateProjectRequest): Response = super.createProject(request)

        @PUT
        @Path("/{uuid}")
        override fun updateProject(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            request: UpdateProjectRequest,
        ): Response = super.updateProject(uuid, request)

        @POST
        @Path("/{uuid}/clients")
        override fun addClient(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            request: AddClientRequest,
        ): Response = super.addClient(uuid, request)

        @DELETE
        @Path("/{uuid}/clients/{clientId}")
        override fun removeClient(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            @PathParam("clientId")
            @Parameter(description = "Client ID")
            clientId: UUID,
        ): Response = super.removeClient(uuid, clientId)

        @POST
        @Path("/{uuid}/tags")
        override fun addTag(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            request: AddTagRequest,
        ): Response = super.addTag(uuid, request)

        @DELETE
        @Path("/{uuid}/tags/{tag}")
        override fun removeTag(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
            @PathParam("tag")
            @Parameter(description = "Tag to remove")
            tag: String,
        ): Response = super.removeTag(uuid, tag)

        @DELETE
        @Path("/{uuid}")
        override fun deleteProject(
            @PathParam("uuid")
            @Parameter(description = "Project UUID")
            uuid: UUID,
        ): Response = super.deleteProject(uuid)
    }
