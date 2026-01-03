package com.revet.documents.security

import com.revet.documents.domain.PermissionType
import jakarta.enterprise.util.Nonbinding
import jakarta.interceptor.InterceptorBinding

/**
 * Annotation to declaratively secure endpoints with permission checks.
 *
 * Usage:
 * ```
 * @PUT
 * @Path("/{uuid}")
 * @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_CREATE)
 * fun updateDocument(@PathParam("uuid") uuid: UUID, request: UpdateRequest): Response
 * ```
 *
 * For child resources (e.g., DocumentVersion), use viaDocumentVersion to check permission
 * on the parent Document:
 * ```
 * @GET
 * @Path("/{uuid}")
 * @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_INVITE, viaDocumentVersion = true)
 * fun getVersion(@PathParam("uuid") uuid: UUID): Response
 * ```
 *
 * The interceptor will:
 * 1. Extract the resource UUID from the path parameter named "uuid"
 * 2. If viaDocumentVersion=true, look up the DocumentVersion and get its parent Document UUID
 * 3. Get the current user ID from the JWT "sub" claim
 * 4. Check if the user has the required permission on the resource
 * 5. Return 403 Forbidden if the user lacks permission
 *
 * @param resource The type of resource being accessed (DOCUMENT, PROJECT, ORGANIZATION)
 * @param permission The minimum permission level required (CAN_INVITE, CAN_CREATE, CAN_MANAGE)
 * @param viaDocumentVersion If true, the UUID is a DocumentVersion UUID and permission is checked on parent Document
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@InterceptorBinding
@MustBeDocumented
annotation class SecuredBy(
    @get:Nonbinding val resource: ResourceType,
    @get:Nonbinding val permission: PermissionType,
    @get:Nonbinding val viaDocumentVersion: Boolean = false
)
