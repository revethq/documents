package com.revet.documents.api.resource

import com.revet.documents.api.mapper.DocumentDTOMapper
import com.revet.documents.api.mapper.PageDTOMapper
import com.revet.documents.domain.PageRequest
import com.revet.documents.domain.Sort
import com.revet.documents.dto.*
import com.revet.documents.service.CategoryService
import com.revet.documents.service.DocumentService
import com.revet.documents.service.DocumentVersionService
import com.revet.documents.service.OrganizationService
import com.revet.documents.service.ProjectService
import com.revet.documents.service.StorageService
import com.revet.documents.service.TagService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.core.Context
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import java.util.*

/**
 * REST Resource for Document endpoints.
 */
@Path("/api/v1/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Documents", description = "Document management endpoints")
class DocumentResource @Inject constructor(
    private val documentService: com.revet.documents.service.DocumentService,
    private val documentVersionService: com.revet.documents.service.DocumentVersionService,
    private val organizationService: com.revet.documents.service.OrganizationService,
    private val storageService: com.revet.documents.service.StorageService,
    private val projectService: com.revet.documents.service.ProjectService,
    private val categoryService: com.revet.documents.service.CategoryService,
    private val tagService: com.revet.documents.service.TagService
) {

    companion object {
        const val DEFAULT_PAGE = 0
        const val DEFAULT_SIZE = 20
        const val MAX_SIZE = 100
        val ALLOWED_SORT_FIELDS = setOf("name", "date", "mime", "id")
    }

    @GET
    @Operation(summary = "List documents with pagination", description = "Retrieve a paginated list of documents with optional filtering and sorting")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Paginated list of documents",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.PageDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid pagination parameters")
    )
    fun listDocuments(
        @QueryParam("page")
        @Parameter(description = "Page number (0-indexed)")
        page: Int? = null,
        @QueryParam("size")
        @Parameter(description = "Page size (max 100)")
        size: Int? = null,
        @QueryParam("sort")
        @Parameter(description = "Sort field (name, date, mime, id)")
        sortField: String? = null,
        @QueryParam("direction")
        @Parameter(description = "Sort direction (ASC, DESC)")
        sortDirection: String? = null,
        @QueryParam("name")
        @Parameter(description = "Fuzzy search by document name (case-insensitive)")
        name: String? = null,
        @QueryParam("includeInactive")
        @Parameter(description = "Include inactive documents")
        includeInactive: Boolean = false,
        @QueryParam("projectId")
        @Parameter(description = "Filter by project ID")
        projectId: Long? = null,
        @QueryParam("categoryId")
        @Parameter(description = "Filter by category ID")
        categoryId: Long? = null,
        @QueryParam("tagId")
        @Parameter(description = "Filter by tag IDs (documents must have ALL specified tags)")
        tagIds: List<Int>? = null,
        @QueryParam("organizationId")
        @Parameter(description = "Filter by organization UUIDs (documents in ANY of the specified organizations)")
        organizationIds: List<UUID>? = null,
        @Context uriInfo: UriInfo
    ): Response {
        val warnings = mutableListOf<com.revet.documents.dto.ProblemDetail>()
        val requestPath = uriInfo.requestUri.path + "?" + (uriInfo.requestUri.rawQuery ?: "")

        // Validate sort field
        if (sortField != null && sortField !in ALLOWED_SORT_FIELDS) {
            return Response.status(Response.Status.BAD_REQUEST)
                .type("application/problem+json")
                .entity(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/validation-error",
                        title = "Invalid Sort Field",
                        status = 400,
                        detail = "Invalid sort field '$sortField'. Allowed fields: $ALLOWED_SORT_FIELDS",
                        instance = requestPath
                    )
                )
                .build()
        }

        // Validate projectId exists and collect warning if not found
        if (projectId != null && projectService.getProjectById(projectId) == null) {
            warnings.add(
                _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                    type = "https://kala.ndptc.com/problems/not-found",
                    title = "Project Not Found",
                    status = 404,
                    detail = "Project with ID $projectId was not found",
                    instance = requestPath
                )
            )
        }

        // Validate categoryId exists and collect warning if not found
        if (categoryId != null && categoryService.getCategoryById(categoryId) == null) {
            warnings.add(
                _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                    type = "https://kala.ndptc.com/problems/not-found",
                    title = "Category Not Found",
                    status = 404,
                    detail = "Category with ID $categoryId was not found",
                    instance = requestPath
                )
            )
        }

        // Validate tagIds exist and collect warnings for not found
        tagIds?.forEach { tagId ->
            if (tagService.getTagById(tagId) == null) {
                warnings.add(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/not-found",
                        title = "Tag Not Found",
                        status = 404,
                        detail = "Tag with ID $tagId was not found",
                        instance = requestPath
                    )
                )
            }
        }

        // Validate organizationIds exist and collect their Long IDs
        val resolvedOrganizationIds = mutableListOf<Long>()
        organizationIds?.forEach { orgUuid ->
            val org = organizationService.getOrganizationByUuid(orgUuid)
            if (org != null) {
                org.id?.let { resolvedOrganizationIds.add(it) }
            } else {
                warnings.add(
                    _root_ide_package_.com.revet.documents.dto.ProblemDetail(
                        type = "https://kala.ndptc.com/problems/not-found",
                        title = "Organization Not Found",
                        status = 404,
                        detail = "Organization with UUID $orgUuid was not found",
                        instance = requestPath
                    )
                )
            }
        }

        // Build pagination request
        val validatedPage = (page ?: DEFAULT_PAGE).coerceAtLeast(0)
        val validatedSize = (size ?: DEFAULT_SIZE).coerceIn(1, MAX_SIZE)
        val sort = sortField?.let { field ->
            val ascending = sortDirection?.uppercase() != "DESC"
            _root_ide_package_.com.revet.documents.domain.Sort(field, ascending)
        }
        val pageRequest = _root_ide_package_.com.revet.documents.domain.PageRequest(validatedPage, validatedSize, sort)

        val documentPage = documentService.getDocumentsPaginated(
            pageRequest = pageRequest,
            includeInactive = includeInactive,
            name = name?.takeIf { it.isNotBlank() },
            projectId = projectId,
            categoryId = categoryId,
            tagIds = tagIds?.takeIf { it.isNotEmpty() },
            organizationIds = resolvedOrganizationIds.takeIf { it.isNotEmpty() }
        )

        val pageDTO = _root_ide_package_.com.revet.documents.api.mapper.PageDTOMapper.toDTO(documentPage, _root_ide_package_.com.revet.documents.api.mapper.DocumentDTOMapper::toDTO, warnings)

        return Response.ok(pageDTO).build()
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieve a single document by its ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Document found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Document not found")
    )
    fun getDocument(
        @PathParam("id")
        @Parameter(description = "Document ID")
        id: Long
    ): Response {
        val document = documentService.getDocumentById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentDTOMapper.toDTO(document)).build()
    }

    @GET
    @Path("/uuid/{uuid}")
    @Operation(summary = "Get document by UUID", description = "Retrieve a single document by its UUID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Document found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Document not found")
    )
    fun getDocumentByUuid(
        @PathParam("uuid")
        @Parameter(description = "Document UUID")
        uuid: UUID
    ): Response {
        val document = documentService.getDocumentByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentDTOMapper.toDTO(document)).build()
    }

    @GET
    @Path("/{uuid}/download")
    @Operation(summary = "Get download URL for latest document version", description = "Generate a presigned download URL for the latest version of a document")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Presigned download URL generated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.PresignedDownloadResponse::class))]
        ),
        APIResponse(responseCode = "404", description = "Document or version not found"),
        APIResponse(responseCode = "400", description = "File not available for download")
    )
    fun downloadLatestVersion(
        @PathParam("uuid")
        @Parameter(description = "Document UUID")
        uuid: UUID
    ): Response {
        val document = documentService.getDocumentByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()

        val documentId = document.id
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document has no ID"))
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

        val latestVersion = documentVersionService.getLatestVersionByDocumentId(documentId)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "No versions found for this document"))
                .build()

        val s3Key = latestVersion.file
            ?: return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "No storage key associated with this document version"))
                .build()

        val presignedUrl = storageService.generatePresignedDownloadUrl(bucketId, s3Key)

        return Response.ok(
            _root_ide_package_.com.revet.documents.dto.PresignedDownloadResponse(
                downloadUrl = presignedUrl.url,
                expiresInMinutes = presignedUrl.expiresInMinutes,
                fileName = latestVersion.name
            )
        ).build()
    }

    @POST
    @Operation(summary = "Create document", description = "Create a new document")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Document created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createDocument(request: com.revet.documents.dto.CreateDocumentRequest): Response {
        return try {
            val document = documentService.createDocument(
                name = request.name,
                projectId = request.projectId,
                categoryId = request.categoryId,
                mime = request.mime,
                tags = request.tags
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.DocumentDTOMapper.toDTO(document))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update document", description = "Update an existing document")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Document updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Document not found"),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun updateDocument(
        @PathParam("id")
        @Parameter(description = "Document ID")
        id: Long,
        request: com.revet.documents.dto.UpdateDocumentRequest
    ): Response {
        return try {
            val document = documentService.updateDocument(
                id = id,
                name = request.name,
                categoryId = request.categoryId,
                mime = request.mime,
                tags = request.tags,
                isActive = request.isActive
            ) ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentDTOMapper.toDTO(document)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @POST
    @Path("/{id}/tags")
    @Operation(summary = "Add tag to document", description = "Add a tag to the document")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Tag added",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Document not found")
    )
    fun addTag(
        @PathParam("id")
        @Parameter(description = "Document ID")
        id: Long,
        request: com.revet.documents.dto.AddTagRequest
    ): Response {
        return try {
            val document = documentService.addTagToDocument(id, request.tag)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "Document not found"))
                    .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentDTOMapper.toDTO(document)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{id}/tags/{tag}")
    @Operation(summary = "Remove tag from document", description = "Remove a tag from the document")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Tag removed",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.DocumentDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Document not found")
    )
    fun removeTag(
        @PathParam("id")
        @Parameter(description = "Document ID")
        id: Long,
        @PathParam("tag")
        @Parameter(description = "Tag to remove")
        tag: String
    ): Response {
        val document = documentService.removeTagFromDocument(id, tag)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.DocumentDTOMapper.toDTO(document)).build()
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete document", description = "Soft delete a document")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Document deleted"),
        APIResponse(responseCode = "404", description = "Document not found")
    )
    fun deleteDocument(
        @PathParam("id")
        @Parameter(description = "Document ID")
        id: Long
    ): Response {
        val deleted = documentService.deleteDocument(id)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Document not found"))
                .build()
        }
    }
}
