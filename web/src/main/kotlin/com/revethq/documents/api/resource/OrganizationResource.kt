package com.revethq.documents.api.resource

import com.revethq.documents.api.mapper.OrganizationDTOMapper
import com.revethq.documents.dto.CreateOrganizationRequest
import com.revethq.documents.dto.OrganizationDTO
import com.revethq.documents.dto.UpdateOrganizationRequest
import com.revethq.documents.permission.Actions
import com.revethq.documents.service.OrganizationService
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
 * REST Resource for Organization endpoints.
 * Maps between DTOs and Domain models, delegating business logic to the service layer.
 */
@Path("/api/v1/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Organizations", description = "Organization management endpoints")
class OrganizationResource
    @Inject
    constructor(
        private val organizationService: com.revethq.documents.service.OrganizationService,
    ) {
        @GET
        @RequiresPermission(action = Actions.Organization.LIST, resource = "urn:revet:documents:{tenantId}:organization/*")
        @Operation(summary = "List all organizations", description = "Retrieve a list of all active organizations")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "List of organizations",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.OrganizationDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun listOrganizations(
            @QueryParam("includeInactive")
            @Parameter(description = "Include inactive organizations")
            includeInactive: Boolean = false,
        ): List<com.revethq.documents.dto.OrganizationDTO> =
            organizationService
                .getAllOrganizations(includeInactive)
                .map {
                    com.revethq.documents.api.mapper.OrganizationDTOMapper
                        .toDTO(it)
                }

        @GET
        @Path("/{uuid}")
        @RequiresPermission(action = Actions.Organization.GET, resource = "urn:revet:documents:{tenantId}:organization/{uuid}")
        @Operation(summary = "Get organization by UUID", description = "Retrieve a single organization by its UUID")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Organization found",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.OrganizationDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Organization not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun getOrganization(
            @PathParam("uuid")
            @Parameter(description = "Organization UUID")
            uuid: UUID,
        ): Response {
            val organization =
                organizationService.getOrganizationByUuid(uuid)
                    ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Organization not found"))
                        .build()

            return Response
                .ok(
                    com.revethq.documents.api.mapper.OrganizationDTOMapper
                        .toDTO(organization),
                ).build()
        }

        @POST
        @RequiresPermission(action = Actions.Organization.CREATE, resource = "urn:revet:documents:{tenantId}:organization/*")
        @Operation(summary = "Create organization", description = "Create a new organization")
        @APIResponses(
            APIResponse(
                responseCode = "201",
                description = "Organization created",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.OrganizationDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "400", description = "Invalid request"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun createOrganization(request: com.revethq.documents.dto.CreateOrganizationRequest): Response =
            try {
                val contactInfo =
                    com.revethq.documents.api.mapper.OrganizationDTOMapper
                        .toContactInfo(request)
                val organization =
                    organizationService.createOrganization(
                        name = request.name,
                        description = request.description,
                        contactInfo = contactInfo,
                        locale = request.locale,
                        timezone = request.timezone,
                        bucketId = request.bucketId,
                    )

                Response
                    .status(Response.Status.CREATED)
                    .entity(
                        com.revethq.documents.api.mapper.OrganizationDTOMapper
                            .toDTO(organization),
                    ).build()
            } catch (e: IllegalArgumentException) {
                Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to e.message))
                    .build()
            }

        @PUT
        @Path("/{uuid}")
        @RequiresPermission(action = Actions.Organization.UPDATE, resource = "urn:revet:documents:{tenantId}:organization/{uuid}")
        @Operation(summary = "Update organization", description = "Update an existing organization")
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Organization updated",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.OrganizationDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "404", description = "Organization not found"),
            APIResponse(responseCode = "400", description = "Invalid request"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun updateOrganization(
            @PathParam("uuid")
            @Parameter(description = "Organization UUID")
            uuid: UUID,
            request: com.revethq.documents.dto.UpdateOrganizationRequest,
        ): Response {
            return try {
                val contactInfo =
                    com.revethq.documents.api.mapper.OrganizationDTOMapper
                        .toContactInfo(request)
                val organization =
                    organizationService.updateOrganizationByUuid(
                        uuid = uuid,
                        name = request.name,
                        description = request.description,
                        contactInfo = contactInfo,
                        locale = request.locale,
                        timezone = request.timezone,
                        bucketId = request.bucketId,
                        isActive = request.isActive,
                    ) ?: return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(mapOf("error" to "Organization not found"))
                        .build()

                Response
                    .ok(
                        com.revethq.documents.api.mapper.OrganizationDTOMapper
                            .toDTO(organization),
                    ).build()
            } catch (e: IllegalArgumentException) {
                Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to e.message))
                    .build()
            }
        }

        @DELETE
        @Path("/{uuid}")
        @RequiresPermission(action = Actions.Organization.DELETE, resource = "urn:revet:documents:{tenantId}:organization/{uuid}")
        @Operation(summary = "Delete organization", description = "Soft delete an organization")
        @APIResponses(
            APIResponse(responseCode = "204", description = "Organization deleted"),
            APIResponse(responseCode = "404", description = "Organization not found"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun deleteOrganization(
            @PathParam("uuid")
            @Parameter(description = "Organization UUID")
            uuid: UUID,
        ): Response {
            val deleted = organizationService.deleteOrganizationByUuid(uuid)
            return if (deleted) {
                Response.noContent().build()
            } else {
                Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Organization not found"))
                    .build()
            }
        }
    }
