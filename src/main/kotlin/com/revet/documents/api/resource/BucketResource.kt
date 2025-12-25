package com.revet.documents.api.resource

import com.revet.documents.api.mapper.BucketDTOMapper
import com.revet.documents.dto.BucketDTO
import com.revet.documents.dto.CreateBucketRequest
import com.revet.documents.dto.UpdateBucketRequest
import com.revet.documents.service.BucketService
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
 * REST Resource for Bucket endpoints.
 * Manages storage bucket configurations for organizations.
 */
@Path("/api/v1/buckets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Buckets", description = "Storage bucket configuration endpoints")
class BucketResource @Inject constructor(
    private val bucketService: com.revet.documents.service.BucketService
) {

    @GET
    @Operation(summary = "List all buckets", description = "Retrieve a list of all active bucket configurations")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of buckets",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.BucketDTO::class))]
        )
    )
    fun listBuckets(
        @QueryParam("includeInactive")
        @Parameter(description = "Include inactive buckets")
        includeInactive: Boolean = false
    ): List<com.revet.documents.dto.BucketDTO> {
        return bucketService.getAllBuckets(includeInactive)
            .map { _root_ide_package_.com.revet.documents.api.mapper.BucketDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get bucket by ID", description = "Retrieve a single bucket configuration by its ID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Bucket found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.BucketDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Bucket not found")
    )
    fun getBucket(
        @PathParam("id")
        @Parameter(description = "Bucket ID")
        id: Long
    ): Response {
        val bucket = bucketService.getBucketById(id)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Bucket not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.BucketDTOMapper.toDTO(bucket)).build()
    }

    @GET
    @Path("/uuid/{uuid}")
    @Operation(summary = "Get bucket by UUID", description = "Retrieve a single bucket configuration by its UUID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Bucket found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.BucketDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Bucket not found")
    )
    fun getBucketByUuid(
        @PathParam("uuid")
        @Parameter(description = "Bucket UUID")
        uuid: UUID
    ): Response {
        val bucket = bucketService.getBucketByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Bucket not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.BucketDTOMapper.toDTO(bucket)).build()
    }

    @POST
    @Operation(summary = "Create bucket", description = "Create a new bucket configuration")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Bucket created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.BucketDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createBucket(request: com.revet.documents.dto.CreateBucketRequest): Response {
        return try {
            val bucket = bucketService.createBucket(
                name = request.name,
                provider = request.provider,
                bucketName = request.bucketName,
                accessKey = request.accessKey,
                secretKey = request.secretKey,
                endpoint = request.endpoint,
                region = request.region,
                presignedUrlDurationMinutes = request.presignedUrlDurationMinutes
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.BucketDTOMapper.toDTO(bucket))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update bucket", description = "Update an existing bucket configuration")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "Bucket updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.BucketDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "Bucket not found"),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun updateBucket(
        @PathParam("id")
        @Parameter(description = "Bucket ID")
        id: Long,
        request: com.revet.documents.dto.UpdateBucketRequest
    ): Response {
        return try {
            val bucket = bucketService.updateBucket(
                id = id,
                name = request.name,
                bucketName = request.bucketName,
                endpoint = request.endpoint,
                region = request.region,
                accessKey = request.accessKey,
                secretKey = request.secretKey,
                presignedUrlDurationMinutes = request.presignedUrlDurationMinutes,
                isActive = request.isActive
            ) ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Bucket not found"))
                .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.BucketDTOMapper.toDTO(bucket)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete bucket", description = "Soft delete a bucket configuration")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Bucket deleted"),
        APIResponse(responseCode = "404", description = "Bucket not found")
    )
    fun deleteBucket(
        @PathParam("id")
        @Parameter(description = "Bucket ID")
        id: Long
    ): Response {
        val deleted = bucketService.deleteBucket(id)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "Bucket not found"))
                .build()
        }
    }
}
