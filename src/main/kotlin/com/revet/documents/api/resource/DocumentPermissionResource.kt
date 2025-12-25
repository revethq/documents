package com.revet.documents.api.resource

import com.revet.documents.api.mapper.DocumentPermissionDTOMapper
import com.revet.documents.dto.GrantPermissionRequest
import com.revet.documents.dto.DocumentPermissionDTO
import com.revet.documents.dto.UpdatePermissionRequest
import com.revet.documents.service.DocumentPermissionService
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
 * REST Resource for DocumentPermission endpoints.
 */
@Path("/api/v1/document-permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Document Permissions", description = "Document permission management endpoints")
class DocumentPermissionResource @Inject constructor(
    private val permissionService: com.revet.documents.service.DocumentPermissionService
) {

    @GET
    @Operation(summary = "List all permissions", description = "Retrieve a list of all document permissions")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of permissions",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentPermissionDTO::class))]
        )
    )
    fun listPermissions(
        @QueryParam("documentId")
        @Parameter(description = "Filter by document ID")
        documentId: Long? = null,
        @QueryParam("userId")
        @Parameter(description = "Filter by user ID")
        userId: Long? = null
    ): List<com.revet.documents.dto.DocumentPermissionDTO> {
        val permissions = when {
            documentId != null -> permissionService.getPermissionsByDocumentId(documentId)
            userId != null -> permissionService.getPermissionsByUserId(userId)
            else -> permissionService.getAllPermissions()
        }
        return permissions.map { _root_ide_package_.com.revet.documents.api.mapper.DocumentPermissionDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get permission by ID", description = "Retrieve a single permission by its ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Permission found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentPermissionDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Permission not found")
    )
    fun getPermission(
        @PathParam("id")
        @Parameter(description = "Permission ID")
        id: Long
    ): Response {
        val permission = permissionService.getPermissionById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Permission not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentPermissionDTOMapper.toDTO(permission)).build()
    }

    @POST
    @Path("/documents/{documentId}/grant")
    @Operation(summary = "Grant permission", description = "Grant a permission to a user for a document")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Permission granted",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentPermissionDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun grantPermission(
        @PathParam("documentId")
        @Parameter(description = "Document ID")
        documentId: Long,
        request: com.revet.documents.dto.GrantPermissionRequest
    ): Response {
        val permission = permissionService.grantPermission(
            documentId = documentId,
            userId = request.userId,
            permission = request.permission
        )

        return Response.status(Response.Status.CREATED)
            .entity(_root_ide_package_.com.revet.documents.api.mapper.DocumentPermissionDTOMapper.toDTO(permission))
            .build()
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update permission", description = "Update an existing permission")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Permission updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentPermissionDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Permission not found")
    )
    fun updatePermission(
        @PathParam("id")
        @Parameter(description = "Permission ID")
        id: Long,
        request: com.revet.documents.dto.UpdatePermissionRequest
    ): Response {
        val permission = permissionService.updatePermission(id, request.permission)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Permission not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentPermissionDTOMapper.toDTO(permission)).build()
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Revoke permission", description = "Revoke a permission")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Permission revoked"),
        APIResponse(responseCode = "404", description = "Permission not found")
    )
    fun revokePermission(
        @PathParam("id")
        @Parameter(description = "Permission ID")
        id: Long
    ): Response {
        val deleted = permissionService.revokePermission(id)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Permission not found"))
                .build()
        }
    }

    @DELETE
    @Path("/documents/{documentId}/users/{userId}")
    @Operation(summary = "Revoke permission by document and user", description = "Revoke a user's permission for a document")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Permission revoked"),
        APIResponse(responseCode = "404", description = "Permission not found")
    )
    fun revokePermissionByDocumentAndUser(
        @PathParam("documentId")
        @Parameter(description = "Document ID")
        documentId: Long,
        @PathParam("userId")
        @Parameter(description = "User ID")
        userId: Long
    ): Response {
        val deleted = permissionService.revokePermissionByDocumentAndUser(documentId, userId)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Permission not found"))
                .build()
        }
    }
}
