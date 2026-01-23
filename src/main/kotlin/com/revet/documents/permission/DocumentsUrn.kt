package com.revet.documents.permission

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.UUID

/**
 * URN builder for Documents service resources.
 *
 * URN format: urn:{namespace}:{service}:{tenant}:{resourceType}/{resourceId}
 * Example: urn:revet:documents:acme-corp:document/550e8400-e29b-41d4-a716-446655440000
 */
@ApplicationScoped
class DocumentsUrn {

    @Inject
    @ConfigProperty(name = "revet.urn.namespace", defaultValue = "revet")
    lateinit var namespace: String

    companion object {
        const val SERVICE = "documents"
    }

    object ResourceType {
        const val ORGANIZATION = "organization"
        const val PROJECT = "project"
        const val DOCUMENT = "document"
        const val DOCUMENT_VERSION = "document-version"
        const val CATEGORY = "category"
        const val TAG = "tag"
        const val BUCKET = "bucket"
        const val USER = "user"
    }

    fun build(tenant: String, resourceType: String, resourceId: String): String {
        return "urn:$namespace:$SERVICE:$tenant:$resourceType/$resourceId"
    }

    fun build(tenant: String, resourceType: String, resourceId: UUID): String {
        return build(tenant, resourceType, resourceId.toString())
    }

    fun wildcard(tenant: String, resourceType: String): String {
        return "urn:$namespace:$SERVICE:$tenant:$resourceType/*"
    }

    fun wildcardAllTenants(resourceType: String): String {
        return "urn:$namespace:$SERVICE:*:$resourceType/*"
    }

    fun wildcardAll(): String {
        return "urn:$namespace:$SERVICE:*:*/*"
    }

    fun organization(tenant: String, id: UUID) = build(tenant, ResourceType.ORGANIZATION, id)
    fun organization(tenant: String, id: String) = build(tenant, ResourceType.ORGANIZATION, id)
    fun organizationWildcard(tenant: String) = wildcard(tenant, ResourceType.ORGANIZATION)

    fun project(tenant: String, id: UUID) = build(tenant, ResourceType.PROJECT, id)
    fun project(tenant: String, id: String) = build(tenant, ResourceType.PROJECT, id)
    fun projectWildcard(tenant: String) = wildcard(tenant, ResourceType.PROJECT)

    fun document(tenant: String, id: UUID) = build(tenant, ResourceType.DOCUMENT, id)
    fun document(tenant: String, id: String) = build(tenant, ResourceType.DOCUMENT, id)
    fun documentWildcard(tenant: String) = wildcard(tenant, ResourceType.DOCUMENT)

    fun documentVersion(tenant: String, id: UUID) = build(tenant, ResourceType.DOCUMENT_VERSION, id)
    fun documentVersion(tenant: String, id: String) = build(tenant, ResourceType.DOCUMENT_VERSION, id)
    fun documentVersionWildcard(tenant: String) = wildcard(tenant, ResourceType.DOCUMENT_VERSION)

    fun category(tenant: String, id: Long) = build(tenant, ResourceType.CATEGORY, id.toString())
    fun category(tenant: String, id: String) = build(tenant, ResourceType.CATEGORY, id)
    fun categoryWildcard(tenant: String) = wildcard(tenant, ResourceType.CATEGORY)

    fun tag(tenant: String, id: Long) = build(tenant, ResourceType.TAG, id.toString())
    fun tag(tenant: String, id: String) = build(tenant, ResourceType.TAG, id)
    fun tagWildcard(tenant: String) = wildcard(tenant, ResourceType.TAG)

    fun bucket(tenant: String, id: UUID) = build(tenant, ResourceType.BUCKET, id)
    fun bucket(tenant: String, id: String) = build(tenant, ResourceType.BUCKET, id)
    fun bucketWildcard(tenant: String) = wildcard(tenant, ResourceType.BUCKET)

    fun user(tenant: String, id: UUID) = build(tenant, ResourceType.USER, id)
    fun user(tenant: String, id: String) = build(tenant, ResourceType.USER, id)
    fun userWildcard(tenant: String) = wildcard(tenant, ResourceType.USER)

    fun userPrincipal(tenant: String, userId: UUID): String {
        return "urn:$namespace:iam:$tenant:user/$userId"
    }

    fun userPrincipal(tenant: String, userId: String): String {
        return "urn:$namespace:iam:$tenant:user/$userId"
    }

    fun groupPrincipal(tenant: String, groupId: UUID): String {
        return "urn:$namespace:iam:$tenant:group/$groupId"
    }

    fun groupPrincipal(tenant: String, groupId: String): String {
        return "urn:$namespace:iam:$tenant:group/$groupId"
    }
}
