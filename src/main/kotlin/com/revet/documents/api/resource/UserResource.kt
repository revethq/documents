package com.revet.documents.api.resource

import com.revet.documents.api.mapper.UserDTOMapper
import com.revet.documents.dto.CreateUserRequest
import com.revet.documents.dto.UpdateUserRequest
import com.revet.documents.dto.UserDTO
import com.revet.documents.service.UserService
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
 * REST Resource for User endpoints.
 */
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management endpoints")
class UserResource @Inject constructor(
    private val userService: com.revet.documents.service.UserService
) {

    @GET
    @Operation(summary = "List all users", description = "Retrieve a list of all active users")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "List of users",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.UserDTO::class))]
        )
    )
    fun listUsers(
        @QueryParam("includeInactive")
        @Parameter(description = "Include inactive users")
        includeInactive: Boolean = false
    ): List<com.revet.documents.dto.UserDTO> {
        return userService.getAllUsers(includeInactive)
            .map { _root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toDTO(it) }
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "Get user by UUID", description = "Retrieve a single user by their UUID")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "User found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.UserDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun getUser(
        @PathParam("uuid")
        @Parameter(description = "User UUID")
        uuid: UUID
    ): Response {
        val user = userService.getUserByUuid(uuid)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "User not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toDTO(user)).build()
    }

    @GET
    @Path("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve a single user by their username")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "User found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.UserDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun getUserByUsername(
        @PathParam("username")
        @Parameter(description = "Username")
        username: String
    ): Response {
        val user = userService.getUserByUsername(username)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "User not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toDTO(user)).build()
    }

    @GET
    @Path("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve a single user by their email")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "User found",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.UserDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun getUserByEmail(
        @PathParam("email")
        @Parameter(description = "Email address")
        email: String
    ): Response {
        val user = userService.getUserByEmail(email)
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "User not found"))
                .build()

        return Response.ok(_root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toDTO(user)).build()
    }

    @POST
    @Operation(summary = "Create user", description = "Create a new user")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "User created",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.UserDTO::class))]
        ),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun createUser(request: com.revet.documents.dto.CreateUserRequest): Response {
        return try {
            val profile = _root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toUserProfile(request)
            val user = userService.createUser(
                username = request.username,
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                profile = profile
            )

            Response.status(Response.Status.CREATED)
                .entity(_root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toDTO(user))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{uuid}")
    @Operation(summary = "Update user", description = "Update an existing user")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "User updated",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = _root_ide_package_.com.revet.documents.dto.UserDTO::class))]
        ),
        APIResponse(responseCode = "404", description = "User not found"),
        APIResponse(responseCode = "400", description = "Invalid request")
    )
    fun updateUser(
        @PathParam("uuid")
        @Parameter(description = "User UUID")
        uuid: UUID,
        request: com.revet.documents.dto.UpdateUserRequest
    ): Response {
        return try {
            val existing = userService.getUserByUuid(uuid)
                ?: return Response.status(Response.Status.NOT_FOUND)
                    .entity(mapOf("error" to "User not found"))
                    .build()

            val profile = _root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toUserProfile(request, existing.profile)
            val user = userService.updateUserByUuid(
                uuid = uuid,
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                profile = profile,
                isActive = request.isActive,
                isStaff = request.isStaff,
                isSuperuser = request.isSuperuser
            ) ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "User not found"))
                .build()

            Response.ok(_root_ide_package_.com.revet.documents.api.mapper.UserDTOMapper.toDTO(user)).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Delete user", description = "Soft delete a user")
    @APIResponses(
        APIResponse(responseCode = "204", description = "User deleted"),
        APIResponse(responseCode = "404", description = "User not found")
    )
    fun deleteUser(
        @PathParam("uuid")
        @Parameter(description = "User UUID")
        uuid: UUID
    ): Response {
        val deleted = userService.deleteUserByUuid(uuid)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "User not found"))
                .build()
        }
    }
}
