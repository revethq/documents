package com.revet.documents.api.resource

import com.revet.documents.api.mapper.DocumentVersionDTOMapper
import com.revet.documents.dto.*
import com.revet.documents.repository.DocumentVersionRepository
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
 * REST Resource for file operations.
 */
@Path("/api/v1/files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Files", description = "File operations")
class FileUploadResource @Inject constructor(
    private val documentVersionRepository: com.revet.documents.repository.DocumentVersionRepository,
    private val documentVersionService: com.revet.documents.service.DocumentVersionService,
    private val documentService: com.revet.documents.service.DocumentService,
    private val projectService: com.revet.documents.service.ProjectService,
    private val organizationService: com.revet.documents.service.OrganizationService,
    private val storageService: com.revet.documents.service.StorageService
) {

    @GET
    @Path("/download/{documentVersionUuid}")
    @Operation(
        summary = "Get download URL",
        description = "Get the download URL for a document version"
    )
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Download URL retrieved",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DownloadResponse::class))]
        ),
        APIResponse(responseCode = "404", description = "DocumentVersion not found"),
        APIResponse(responseCode = "400", description = "File not available for download")
    )
    fun getDownloadUrl(
        @PathParam("documentVersionUuid")
        @Parameter(description = "DocumentVersion UUID")
        documentVersionUuid: String
    ): Response {
        val uuid = try {
            UUID.fromString(documentVersionUuid)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format"))
                .build()
        }

        val documentVersion = documentVersionRepository.findByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "DocumentVersion not found"))
                .build()

        if (documentVersion.url.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "No download URL available for this document version"))
                .build()
        }

        return Response.ok(
            _root_ide_package_.com.revet.documents.dto.DownloadResponse(
                downloadUrl = documentVersion.url,
                fileName = documentVersion.name
            )
        ).build()
    }

    @POST
    @Path("/initiate-upload")
    @Operation(
        summary = "Initiate file upload",
        description = "Create a document version in pending state and get a presigned upload URL"
    )
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Upload initiated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.InitiateUploadResponse::class))]
        ),
        APIResponse(responseCode = "404", description = "Document not found"),
        APIResponse(responseCode = "400", description = "Storage not configured")
    )
    fun initiateUpload(request: com.revet.documents.dto.InitiateUploadRequest): Response {
        // Look up document by UUID
        val document = documentService.getDocumentByUuid(request.documentUuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()

        val documentId = document.id
            ?: return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Document has no ID"))
                .build()

        // Get project and organization to find bucket
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

        // Generate S3 key
        val s3Key = storageService.generateStorageKey(documentId, request.fileName)

        // Create DocumentVersion in PENDING state
        val version = documentVersionService.createVersion(
            documentId = documentId,
            name = request.fileName,
            url = "",
            size = 0,
            file = s3Key,
            description = null,
            mime = request.contentType,
            userId = null
        )

        // Generate presigned upload URL
        val presignedUrl = storageService.generatePresignedUploadUrl(bucketId, s3Key, request.contentType)

        return Response.status(Response.Status.CREATED)
            .entity(
                _root_ide_package_.com.revet.documents.dto.InitiateUploadResponse(
                    uploadUrl = presignedUrl.url,
                    s3Key = s3Key,
                    documentVersionUuid = version.uuid,
                    expiresInMinutes = presignedUrl.expiresInMinutes
                )
            )
            .build()
    }

    @POST
    @Path("/create-version")
    @Operation(
        summary = "Create document version with URL",
        description = "Create a new document version with a file URL"
    )
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Document version created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentVersionDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createVersionWithUrl(request: com.revet.documents.dto.CreateDocumentVersionRequest): Response {
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
}
