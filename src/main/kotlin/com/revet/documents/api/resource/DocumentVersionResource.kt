package com.revet.documents.api.resource

import com.revet.documents.api.mapper.DocumentVersionDTOMapper
import com.revet.documents.domain.DocumentVersion
import com.revet.documents.domain.UploadStatus
import com.revet.documents.dto.CreateDocumentVersionRequest
import com.revet.documents.dto.DocumentVersionDTO
import com.revet.documents.dto.UpdateDocumentVersionRequest
import com.revet.documents.domain.PermissionType
import com.revet.documents.security.ResourceType
import com.revet.documents.security.SecuredBy
import com.revet.documents.service.DocumentService
import com.revet.documents.service.DocumentVersionService
import com.revet.documents.service.OrganizationService
import com.revet.documents.service.ProjectService
import com.revet.documents.service.StorageService
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
 * REST Resource for DocumentVersion endpoints.
 */
@Path("/api/v1/document-versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Document Versions", description = "Document version management endpoints")
class DocumentVersionResource @Inject constructor(
    private val documentVersionService: com.revet.documents.service.DocumentVersionService,
    private val documentService: com.revet.documents.service.DocumentService,
    private val projectService: com.revet.documents.service.ProjectService,
    private val organizationService: com.revet.documents.service.OrganizationService,
    private val storageService: com.revet.documents.service.StorageService
) {

    @GET
    @Operation(summary = "List all document versions", description = "Retrieve a list of all document versions")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of document versions",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO::class))]
        )
    )
    fun listVersions(
        @QueryParam("documentId")
        @Parameter(description = "Filter by document ID")
        documentId: Long? = null
    ): List<com.revet.documents.dto.DocumentVersionDTO> {
        val versions = if (documentId != null) {
            documentVersionService.getVersionsByDocumentId(documentId)
        } else {
            documentVersionService.getAllVersions()
        }
        return versions.map { _root_ide_package_.com.revet.documents.api.mapper.DocumentVersionDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_INVITE, viaDocumentVersion = true)
    @Operation(summary = "Get version by UUID", description = "Retrieve a single document version by its UUID, including a presigned download URL if available")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Version found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Version not found"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun getVersion(
        @PathParam("uuid")
        @Parameter(description = "Version UUID")
        uuid: UUID
    ): Response {
        val version = documentVersionService.getVersionByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Version not found"))
                .build()

        val downloadUrl = generateDownloadUrl(version)

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentVersionDTOMapper.toDTO(version, downloadUrl)).build()
    }

    /**
     * Generate a presigned download URL for a version if it's downloadable.
     * Returns null if version is not completed, has no s3Key, or bucket is not configured.
     */
    private fun generateDownloadUrl(version: com.revet.documents.domain.DocumentVersion): String? {
        // Only generate URL for completed uploads
        if (version.uploadStatus != _root_ide_package_.com.revet.documents.domain.UploadStatus.COMPLETED) return null

        // Need s3Key to generate URL
        val s3Key = version.file ?: return null

        // Need document ID to find bucket
        val documentId = version.documentId ?: return null

        // Resolve document -> project -> organization -> bucket
        val document = documentService.getDocumentById(documentId) ?: return null
        val project = projectService.getProjectById(document.projectId) ?: return null
        val organization = organizationService.getOrganizationById(project.organizationId) ?: return null
        val bucketId = organization.bucketId ?: return null

        return try {
            storageService.generatePresignedDownloadUrl(bucketId, s3Key).url
        } catch (e: Exception) {
            null
        }
    }

    @GET
    @Path("/document/{uuid}/latest")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_INVITE)
    @Operation(summary = "Get latest version", description = "Retrieve the latest version of a document")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Latest version found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "No versions found"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun getLatestVersion(
        @PathParam("uuid")
        @Parameter(description = "Document UUID")
        uuid: UUID
    ): Response {
        val version = documentVersionService.getLatestVersionByDocumentUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "No versions found for this document"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentVersionDTOMapper.toDTO(version)).build()
    }

    @POST
    @Operation(summary = "Create version", description = "Create a new document version")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Version created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createVersion(request: com.revet.documents.dto.CreateDocumentVersionRequest): Response {
        return try {
            val version = documentVersionService.createVersion(
                documentId = request.documentId,
                name = request.name,
                url = request.url,
                size = request.size,
                file = request.file,
                description = request.description,
                mime = request.mime,
                userId = request.userId
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.DocumentVersionDTOMapper.toDTO(version))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_CREATE, viaDocumentVersion = true)
    @Operation(summary = "Update version", description = "Update an existing document version")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Version updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Version not found"),
        APIResponse(responseCode = "400", description = "Invalid request"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun updateVersion(
        @PathParam("uuid")
        @Parameter(description = "Version UUID")
        uuid: UUID,
        request: com.revet.documents.dto.UpdateDocumentVersionRequest
    ): Response {
        return try {
            val version = documentVersionService.updateVersion(
                uuid = uuid,
                name = request.name,
                file = request.file,
                url = request.url,
                size = request.size,
                description = request.description,
                mime = request.mime
            ) ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Version not found"))
                .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentVersionDTOMapper.toDTO(version)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{uuid}/complete-upload")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_CREATE, viaDocumentVersion = true)
    @Operation(summary = "Complete upload", description = "Mark a pending upload as completed after verifying file exists in storage")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Upload completed",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Version not found"),
        APIResponse(responseCode = "400", description = "Invalid state or file not found in storage"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun completeUpload(
        @PathParam("uuid")
        @Parameter(description = "Version UUID")
        uuid: UUID
    ): Response {
        // Get the document version
        val version = documentVersionService.getVersionByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Version not found"))
                .build()

        // Check current status
        if (version.uploadStatus != _root_ide_package_.com.revet.documents.domain.UploadStatus.PENDING) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf(
                    "error" to "Cannot complete upload - version is not in PENDING state",
                    "currentStatus" to version.uploadStatus.name
                ))
                .build()
        }

        // Get the S3 key
        val s3Key = version.file
            ?: return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Version has no storage key"))
                .build()

        // Get document to find organization bucket
        val documentId = version.documentId
            ?: return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Version not linked to a document"))
                .build()

        val document = documentService.getDocumentById(documentId)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()

        val project = projectService.getProjectById(document.projectId)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Project not found"))
                .build()

        val organization = organizationService.getOrganizationById(project.organizationId)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Organization not found"))
                .build()

        val bucketId = organization.bucketId
            ?: return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Storage not configured for this organization"))
                .build()

        // Check if file exists in storage
        if (!storageService.checkFileExists(bucketId, s3Key)) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "File not found in storage", "s3Key" to s3Key))
                .build()
        }

        // Get file metadata to update size
        val metadata = storageService.getFileMetadata(bucketId, s3Key)

        // Update version status to COMPLETED
        val updatedVersion = documentVersionService.completeUpload(
            uuid = uuid,
            fileSize = metadata.size.toInt()
        ) ?: return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapOf("error" to "Failed to update version"))
            .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentVersionDTOMapper.toDTO(updatedVersion)).build()
    }

    @DELETE
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_MANAGE, viaDocumentVersion = true)
    @Operation(summary = "Delete version", description = "Delete a document version")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Version deleted"),
        APIResponse(responseCode = "404", description = "Version not found"),
        APIResponse(responseCode = "403", description = "Insufficient permissions")
    )
    fun deleteVersion(
        @PathParam("uuid")
        @Parameter(description = "Version UUID")
        uuid: UUID
    ): Response {
        val deleted = documentVersionService.deleteVersion(uuid)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Version not found"))
                .build()
        }
    }
}
