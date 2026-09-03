package com.revethq.documents.api.resource

import com.revethq.core.api.mapper.OrganizationDTOMapper
import com.revethq.core.api.mapper.ProjectDTOMapper
import com.revethq.core.dto.OrganizationDTO
import com.revethq.core.dto.ProjectDTO
import com.revethq.documents.api.mapper.DocumentDTOMapper
import com.revethq.documents.dto.DocumentDTO
import com.revethq.documents.dto.SearchResultsDTO
import com.revethq.documents.service.SearchService
import io.quarkus.security.Authenticated
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag

/**
 * REST Resource for full-text search endpoints.
 */
@Path("/api/v1/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Search", description = "Full-text search endpoints")
class SearchResource
    @Inject
    constructor(
        private val searchService: com.revethq.documents.service.SearchService,
    ) {
        @GET
        @Authenticated
        @Operation(
            summary = "Search across all entities",
            description = "Perform full-text search across documents, projects, and organizations",
        )
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Search results",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.SearchResultsDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "400", description = "Invalid query parameter"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun searchAll(
            @QueryParam("q")
            @Parameter(description = "Search query", required = true)
            query: String?,
            @QueryParam("maxResults")
            @Parameter(description = "Maximum number of results per entity type (default: 20)")
            maxResults: Int? = 20,
        ): com.revethq.documents.dto.SearchResultsDTO {
            if (query.isNullOrBlank()) {
                throw BadRequestException("Query parameter 'q' is required")
            }

            val results = searchService.searchAll(query, maxResults ?: 20)

            return com.revethq.documents.dto.SearchResultsDTO(
                documents =
                    results.documents.map {
                        com.revethq.documents.api.mapper.DocumentDTOMapper.toDTO(
                            it,
                        )
                    },
                projects =
                    results.projects.map {
                        com.revethq.core.api.mapper.ProjectDTOMapper.toDTO(
                            it,
                        )
                    },
                organizations =
                    results.organizations.map {
                        com.revethq.core.api.mapper.OrganizationDTOMapper.toDTO(
                            it,
                        )
                    },
            )
        }

        @GET
        @Path("/documents")
        @Authenticated
        @Operation(
            summary = "Search documents",
            description = "Perform full-text search on documents (name, tags, MIME type)",
        )
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Document search results",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.documents.dto.DocumentDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "400", description = "Invalid query parameter"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun searchDocuments(
            @QueryParam("q")
            @Parameter(description = "Search query", required = true)
            query: String?,
            @QueryParam("maxResults")
            @Parameter(description = "Maximum number of results (default: 50)")
            maxResults: Int? = 50,
        ): List<com.revethq.documents.dto.DocumentDTO> {
            if (query.isNullOrBlank()) {
                throw BadRequestException("Query parameter 'q' is required")
            }

            val documents = searchService.searchDocuments(query, maxResults ?: 50)
            return documents.map {
                com.revethq.documents.api.mapper.DocumentDTOMapper
                    .toDTO(it)
            }
        }

        @GET
        @Path("/projects")
        @Authenticated
        @Operation(
            summary = "Search projects",
            description = "Perform full-text search on projects (name, description, tags)",
        )
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Project search results",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.core.dto.ProjectDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "400", description = "Invalid query parameter"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun searchProjects(
            @QueryParam("q")
            @Parameter(description = "Search query", required = true)
            query: String?,
            @QueryParam("maxResults")
            @Parameter(description = "Maximum number of results (default: 50)")
            maxResults: Int? = 50,
        ): List<com.revethq.core.dto.ProjectDTO> {
            if (query.isNullOrBlank()) {
                throw BadRequestException("Query parameter 'q' is required")
            }

            val projects = searchService.searchProjects(query, maxResults ?: 50)
            return projects.map {
                com.revethq.core.api.mapper.ProjectDTOMapper
                    .toDTO(it)
            }
        }

        @GET
        @Path("/organizations")
        @Authenticated
        @Operation(
            summary = "Search organizations",
            description = "Perform full-text search on organizations (name, description)",
        )
        @APIResponses(
            APIResponse(
                responseCode = "200",
                description = "Organization search results",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = Schema(implementation = com.revethq.core.dto.OrganizationDTO::class),
                    ),
                ],
            ),
            APIResponse(responseCode = "400", description = "Invalid query parameter"),
            APIResponse(responseCode = "403", description = "Insufficient permissions"),
        )
        fun searchOrganizations(
            @QueryParam("q")
            @Parameter(description = "Search query", required = true)
            query: String?,
            @QueryParam("maxResults")
            @Parameter(description = "Maximum number of results (default: 50)")
            maxResults: Int? = 50,
        ): List<com.revethq.core.dto.OrganizationDTO> {
            if (query.isNullOrBlank()) {
                throw BadRequestException("Query parameter 'q' is required")
            }

            val organizations = searchService.searchOrganizations(query, maxResults ?: 50)
            return organizations.map {
                com.revethq.core.api.mapper.OrganizationDTOMapper
                    .toDTO(it)
            }
        }
    }
