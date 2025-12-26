package com.revet.documents.security

import com.revet.documents.domain.PermissionType
import com.revet.documents.service.DocumentVersionService
import com.revet.documents.service.SecurityService
import jakarta.annotation.Priority
import jakarta.inject.Inject
import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InvocationContext
import jakarta.ws.rs.ForbiddenException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import java.util.UUID

/**
 * CDI Interceptor that enforces permission checks on methods annotated with @SecuredBy.
 *
 * This interceptor:
 * 1. Extracts the resource UUID from the method's @PathParam("uuid") parameter
 * 2. If viaDocumentVersion=true, looks up the DocumentVersion and gets parent Document
 * 3. Gets the current user ID from the JWT token
 * 4. Checks if the user has the required permission on the resource
 * 5. Returns 403 Forbidden if access is denied
 *
 * Usage:
 * ```
 * @PUT
 * @Path("/{uuid}")
 * @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_CREATE)
 * fun updateDocument(@PathParam("uuid") uuid: UUID, request: UpdateRequest): Response
 * ```
 *
 * For child resources like DocumentVersion:
 * ```
 * @GET
 * @Path("/{uuid}")
 * @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_INVITE, viaDocumentVersion = true)
 * fun getVersion(@PathParam("uuid") uuid: UUID): Response
 * ```
 */
@SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_INVITE)
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
class SecuredByInterceptor @Inject constructor(
    private val securityService: SecurityService,
    private val currentUserService: CurrentUserService,
    private val documentVersionService: DocumentVersionService
) {

    @AroundInvoke
    fun enforce(context: InvocationContext): Any? {
        // Get the @SecuredBy annotation from the method
        val securedBy = context.method.getAnnotation(SecuredBy::class.java)
            ?: throw IllegalStateException("@SecuredBy annotation not found on method ${context.method.name}")

        // Get current user ID
        val userId = currentUserService.getCurrentUserId()
            ?: throw ForbiddenException("Not authenticated")

        // Extract UUID from method parameters
        val pathUuid = extractUuidFromParams(context)
            ?: throw IllegalStateException("Could not extract UUID from method parameters. Ensure there is a @PathParam(\"uuid\") parameter of type UUID.")

        // If viaDocumentVersion, look up the version and get parent document ID
        val hasPermission = if (securedBy.viaDocumentVersion) {
            // Look up DocumentVersion to get its parent document
            val version = documentVersionService.getVersionByUuid(pathUuid)
                ?: throw NotFoundException("DocumentVersion not found")

            val documentId = version.documentId
                ?: throw NotFoundException("DocumentVersion not linked to a document")

            // Check permission on the parent document
            securityService.canAccessDocument(userId, documentId, securedBy.permission)
        } else {
            // Direct resource check
            when (securedBy.resource) {
                ResourceType.DOCUMENT -> securityService.canAccessDocumentByUuid(userId, pathUuid, securedBy.permission)
                ResourceType.PROJECT -> securityService.canAccessProjectByUuid(userId, pathUuid, securedBy.permission)
                ResourceType.ORGANIZATION -> securityService.canAccessOrganizationByUuid(userId, pathUuid, securedBy.permission)
            }
        }

        if (!hasPermission) {
            throw ForbiddenException("Insufficient permissions: requires ${securedBy.permission} on ${securedBy.resource}")
        }

        // Permission granted - proceed with the method
        return context.proceed()
    }

    /**
     * Extract the UUID from method parameters.
     * Looks for a parameter annotated with @PathParam("uuid") of type UUID.
     */
    private fun extractUuidFromParams(context: InvocationContext): UUID? {
        val method = context.method
        val parameters = method.parameters
        val arguments = context.parameters

        for (i in parameters.indices) {
            val param = parameters[i]
            val pathParam = param.getAnnotation(PathParam::class.java)

            // Look for @PathParam("uuid") of type UUID
            if (pathParam != null && pathParam.value == "uuid" && param.type == UUID::class.java) {
                return arguments[i] as? UUID
            }
        }

        // Fallback: look for any UUID parameter with @PathParam
        for (i in parameters.indices) {
            val param = parameters[i]
            val pathParam = param.getAnnotation(PathParam::class.java)

            if (pathParam != null && param.type == UUID::class.java) {
                return arguments[i] as? UUID
            }
        }

        return null
    }
}
