package com.revethq.documents.api.resource

import com.revethq.buckets.service.BucketService
import com.revethq.buckets.web.api.resource.BucketResource
import com.revethq.buckets.web.dto.BucketDTO
import com.revethq.buckets.web.dto.CreateBucketRequest
import com.revethq.buckets.web.dto.UpdateBucketRequest
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
 * Local subclass of the library's BucketResource. The library resource is disabled via
 * revet.buckets.resources.disabled=true in application.properties, so this subclass is
 * the only JAX-RS registration for /api/v1/buckets.
 *
 * The list endpoint is overridden to use @Authenticated instead of @RequiresPermission,
 * with permission filtering handled by the service layer via PermissionFilterService.
 *
 * All other endpoints explicitly override and delegate to super so that the OpenAPI spec
 * generator (which only scans locally declared methods) includes them in the output.
 */
@Path("/api/v1/buckets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Buckets", description = "Storage bucket configuration endpoints")
class DocumentsBucketResource
    @Inject
    constructor(
        bucketService: BucketService,
    ) : BucketResource(bucketService) {
        @GET
        @Authenticated
        override fun listBuckets(
            @QueryParam("includeInactive")
            @Parameter(description = "Include inactive buckets")
            includeInactive: Boolean,
        ): List<BucketDTO> = super.listBuckets(includeInactive)

        @GET
        @Path("/{uuid}")
        override fun getBucket(
            @PathParam("uuid")
            @Parameter(description = "Bucket UUID")
            uuid: UUID,
        ): Response = super.getBucket(uuid)

        @POST
        override fun createBucket(request: CreateBucketRequest): Response = super.createBucket(request)

        @PUT
        @Path("/{uuid}")
        override fun updateBucket(
            @PathParam("uuid")
            @Parameter(description = "Bucket UUID")
            uuid: UUID,
            request: UpdateBucketRequest,
        ): Response = super.updateBucket(uuid, request)

        @DELETE
        @Path("/{uuid}")
        override fun deleteBucket(
            @PathParam("uuid")
            @Parameter(description = "Bucket UUID")
            uuid: UUID,
        ): Response = super.deleteBucket(uuid)
    }
