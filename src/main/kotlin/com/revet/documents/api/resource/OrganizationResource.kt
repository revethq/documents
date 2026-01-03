package com.revet.documents.api.resource

import com.revet.documents.api.mapper.OrganizationDTOMapper
import com.revet.documents.domain.PermissionType
import com.revet.documents.dto.CreateOrganizationRequest
import com.revet.documents.dto.OrganizationDTO
import com.revet.documents.dto.UpdateOrganizationRequest
import com.revet.documents.security.ResourceType
import com.revet.documents.security.SecuredBy
import com.revet.documents.service.OrganizationService
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
 * REST Resource for Organization endpoints.
 * Maps between DTOs and Domain models, delegating business logic to the service layer.
 */
@Path("/api/v1/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Organizations", description = "Organization management endpoints")
class OrganizationResource @Inject constructor(
    private val organizationService: com.revet.documents.service.OrganizationService
) {

    @GET
    @Operation(summary = "List all organizations", description = "Retrieve a list of all active organizations")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of organizations",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.OrganizationDTO::class))]
        )
    )
    fun listOrganizations(
        @QueryParam("includeInactive")
        @Parameter(description = "Include inactive organizations")
        includeInactive: Boolean = false
    ): List<com.revet.documents.dto.OrganizationDTO> {
        return organizationService.getAllOrganizations(includeInactive)
            .map { _root_ide_package_.com.revet.documents.api.mapper.OrganizationDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.ORGANIZATION, permission = PermissionType.CAN_INVITE)
    @Operation(summary = "Get organization by UUID", description = "Retrieve a single organization by its UUID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Organization found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.OrganizationDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Organization not found"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun getOrganization(
        @PathParam("uuid")
        @Parameter(description = "Organization UUID")
        uuid: UUID
    ): Response {
        val organization = organizationService.getOrganizationByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Organization not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.OrganizationDTOMapper.toDTO(organization)).build()
    }

    @POST
    @Operation(summary = "Create organization", description = "Create a new organization")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Organization created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.OrganizationDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createOrganization(request: com.revet.documents.dto.CreateOrganizationRequest): Response {
        return try {
            val contactInfo = _root_ide_package_.com.revet.documents.api.mapper.OrganizationDTOMapper.toContactInfo(request)
            val organization = organizationService.createOrganization(
                name = request.name,
                description = request.description,
                contactInfo = contactInfo,
                locale = request.locale,
                timezone = request.timezone,
                bucketId = request.bucketId
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.OrganizationDTOMapper.toDTO(organization))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.ORGANIZATION, permission = PermissionType.CAN_CREATE)
    @Operation(summary = "Update organization", description = "Update an existing organization")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Organization updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.OrganizationDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Organization not found"),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun updateOrganization(
        @PathParam("uuid")
        @Parameter(description = "Organization UUID")
        uuid: UUID,
        request: com.revet.documents.dto.UpdateOrganizationRequest
    ): Response {
        return try {
            val contactInfo = _root_ide_package_.com.revet.documents.api.mapper.OrganizationDTOMapper.toContactInfo(request)
            val organization = organizationService.updateOrganizationByUuid(
                uuid = uuid,
                name = request.name,
                description = request.description,
                contactInfo = contactInfo,
                locale = request.locale,
                timezone = request.timezone,
                bucketId = request.bucketId,
                isActive = request.isActive
            ) ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Organization not found"))
                .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.OrganizationDTOMapper.toDTO(organization)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.ORGANIZATION, permission = PermissionType.CAN_MANAGE)
    @Operation(summary = "Delete organization", description = "Soft delete an organization")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Organization deleted"),
        APIResponse(responseCode = "404", description = "Organization not found"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun deleteOrganization(
        @PathParam("uuid")
        @Parameter(description = "Organization UUID")
        uuid: UUID
    ): Response {
        val deleted = organizationService.deleteOrganizationByUuid(uuid)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Organization not found"))
                .build()
        }
    }
}
