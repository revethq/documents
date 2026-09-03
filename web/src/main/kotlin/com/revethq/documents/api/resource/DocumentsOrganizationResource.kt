package com.revethq.documents.api.resource

import com.revethq.core.api.resource.OrganizationResource
import com.revethq.core.dto.CreateOrganizationRequest
import com.revethq.core.dto.OrganizationDTO
import com.revethq.core.dto.UpdateOrganizationRequest
import com.revethq.core.service.OrganizationService
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
 * Local subclass of the library's OrganizationResource. The library resource is disabled via
 * revet.core.resources.disabled=true in application.properties, so this subclass is
 * the only JAX-RS registration for /api/v1/organizations.
 *
 * The list endpoint is overridden to use @Authenticated instead of @RequiresPermission,
 * with permission filtering handled by the service layer via PermissionFilterService.
 *
 * All other endpoints explicitly override and delegate to super so that the OpenAPI spec
 * generator (which only scans locally declared methods) includes them in the output.
 */
@Path("/api/v1/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Organizations", description = "Organization management endpoints")
class DocumentsOrganizationResource
    @Inject
    constructor(
        organizationService: OrganizationService,
    ) : OrganizationResource(organizationService) {
        @GET
        @Authenticated
        override fun listOrganizations(
            @QueryParam("includeInactive")
            @Parameter(description = "Include inactive organizations")
            includeInactive: Boolean,
        ): List<OrganizationDTO> = super.listOrganizations(includeInactive)

        @GET
        @Path("/{uuid}")
        override fun getOrganization(
            @PathParam("uuid")
            @Parameter(description = "Organization UUID")
            uuid: UUID,
        ): Response = super.getOrganization(uuid)

        @POST
        override fun createOrganization(request: CreateOrganizationRequest): Response = super.createOrganization(request)

        @PUT
        @Path("/{uuid}")
        override fun updateOrganization(
            @PathParam("uuid")
            @Parameter(description = "Organization UUID")
            uuid: UUID,
            request: UpdateOrganizationRequest,
        ): Response = super.updateOrganization(uuid, request)

        @DELETE
        @Path("/{uuid}")
        override fun deleteOrganization(
            @PathParam("uuid")
            @Parameter(description = "Organization UUID")
            uuid: UUID,
        ): Response = super.deleteOrganization(uuid)
    }
