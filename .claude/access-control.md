# Access Control System

## Overview

The Kala API uses a **two-dimensional permission inheritance** model:

1. **Permission Type Hierarchy** - higher permissions imply lower ones
2. **Resource Hierarchy** - permissions on parent resources grant access to children

## Permission Types

```
CAN_MANAGE > CAN_CREATE > CAN_INVITE
```

| Permission | Description | Implies |
|------------|-------------|---------|
| `CAN_INVITE` | Can invite users to the resource | - |
| `CAN_CREATE` | Can create/edit content within the resource | `CAN_INVITE` |
| `CAN_MANAGE` | Full management including permission administration | `CAN_CREATE`, `CAN_INVITE` |

**Example:** A user with `CAN_MANAGE` can perform any action that `CAN_CREATE` or `CAN_INVITE` allows.

## Resource Hierarchy

```
Organization → Project → Document
```

Permissions granted at a higher level automatically apply to all children:

| Permission On | Grants Access To |
|---------------|------------------|
| Organization | All Projects and Documents in that Organization |
| Project | All Documents in that Project |
| Document | Only that specific Document |

**Example:** A user with `CAN_CREATE` on an Organization has `CAN_CREATE` on all Projects and Documents within that Organization.

## Combined Inheritance

The two hierarchies combine. A user's **effective permission** on a resource is the **highest** permission they have from any level.

### Example Scenario

```
Organization: NDPTC
├── Project: Training Materials
│   ├── Document: Safety Guide
│   └── Document: Equipment Manual
└── Project: Reports
    └── Document: Annual Report
```

**User permissions:**
- `CAN_INVITE` on Organization "NDPTC"
- `CAN_CREATE` on Project "Training Materials"

**Effective permissions:**
| Resource | Effective Permission | Source |
|----------|---------------------|--------|
| NDPTC (Org) | `CAN_INVITE` | Direct org permission |
| Training Materials (Project) | `CAN_CREATE` | Direct project permission |
| Reports (Project) | `CAN_INVITE` | Inherited from org |
| Safety Guide (Doc) | `CAN_CREATE` | Inherited from project |
| Equipment Manual (Doc) | `CAN_CREATE` | Inherited from project |
| Annual Report (Doc) | `CAN_INVITE` | Inherited from org |

## Permission Tables

Three tables store explicit permissions:

### organization_permissions
```sql
(id, organization_id, user_id, permission)
UNIQUE(organization_id, user_id)
```

### project_permissions
```sql
(id, project_id, user_id, permission)
UNIQUE(project_id, user_id)
```

### document_permissions
```sql
(id, document_id, user_id, permission)
UNIQUE(document_id, user_id)
```

Each user can have **at most one** permission entry per resource. The entry specifies their highest explicit permission on that resource.

## @SecuredBy Annotation

The recommended way to enforce permissions is the `@SecuredBy` annotation:

```kotlin
@SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_CREATE)
```

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `resource` | `ResourceType` | The type of resource: `DOCUMENT`, `PROJECT`, or `ORGANIZATION` |
| `permission` | `PermissionType` | Minimum required permission: `CAN_INVITE`, `CAN_CREATE`, or `CAN_MANAGE` |

### Usage Example

```kotlin
@Path("/api/v1/documents")
class DocumentResource @Inject constructor(
    private val documentService: DocumentService
) {
    @PUT
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_CREATE)
    fun updateDocument(
        @PathParam("uuid") uuid: UUID,
        request: UpdateDocumentRequest
    ): Response {
        // Only runs if user has CAN_CREATE permission on this document
        // (or inherited from project/organization)
        val document = documentService.updateDocument(uuid, request)
        return Response.ok(document).build()
    }

    @DELETE
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_MANAGE)
    fun deleteDocument(@PathParam("uuid") uuid: UUID): Response {
        // Requires CAN_MANAGE permission
        documentService.deleteDocument(uuid)
        return Response.noContent().build()
    }

    @GET
    @Path("/{uuid}")
    @SecuredBy(resource = ResourceType.DOCUMENT, permission = PermissionType.CAN_INVITE)
    fun getDocument(@PathParam("uuid") uuid: UUID): Response {
        // CAN_INVITE is the minimum - allows read access
        val document = documentService.getDocumentByUuid(uuid)
        return Response.ok(document).build()
    }
}
```

### How It Works

1. The `SecuredByInterceptor` intercepts methods annotated with `@SecuredBy`
2. Extracts the resource UUID from `@PathParam("uuid")`
3. Gets the current user ID from the JWT `sub` claim via `CurrentUserService`
4. Calls `SecurityService.canAccess*ByUuid()` to check permission with inheritance
5. Returns **403 Forbidden** if permission is denied
6. Proceeds with the method if permission is granted

### Requirements

- Method must have a `@PathParam("uuid")` parameter of type `UUID`
- User must be authenticated (JWT with `sub` claim containing user UUID)
- The annotation uses CDI interceptors, so the class must be a CDI bean

## SecurityService API

For programmatic permission checks, use `SecurityService` directly:

```kotlin
interface SecurityService {
    // UUID-based methods (preferred for API layer)
    fun canAccessDocumentByUuid(userId: Long, documentUuid: UUID, requiredPermission: PermissionType): Boolean
    fun canAccessProjectByUuid(userId: Long, projectUuid: UUID, requiredPermission: PermissionType): Boolean
    fun canAccessOrganizationByUuid(userId: Long, organizationUuid: UUID, requiredPermission: PermissionType): Boolean

    // Long ID-based methods (for internal use)
    fun canAccessDocument(userId: Long, documentId: Long, requiredPermission: PermissionType): Boolean
    fun canAccessProject(userId: Long, projectId: Long, requiredPermission: PermissionType): Boolean
    fun canAccessOrganization(userId: Long, organizationId: Long, requiredPermission: PermissionType): Boolean

    // Get effective permission (highest from any level)
    fun getEffectiveDocumentPermission(userId: Long, documentId: Long): PermissionType?
    fun getEffectiveProjectPermission(userId: Long, projectId: Long): PermissionType?
    fun getOrganizationPermission(userId: Long, organizationId: Long): PermissionType?
}
```

## Permission Check Flow

When checking `canAccessDocument(userId, documentId, CAN_CREATE)`:

```
1. Check document_permissions for (documentId, userId)
   → Found CAN_INVITE? Continue checking...
   → Found CAN_CREATE or CAN_MANAGE? Return true

2. Look up document.projectId, check project_permissions
   → Found CAN_CREATE or CAN_MANAGE? Return true
   → Found CAN_INVITE? Continue checking...

3. Look up project.organizationId, check organization_permissions
   → Found CAN_CREATE or CAN_MANAGE? Return true
   → Otherwise return false
```

The check stops as soon as a sufficient permission is found.

## Future Considerations

- **Caching**: Permission lookups could be cached to reduce database queries
- **Batch checks**: For listing resources, batch permission checks would be more efficient
- **Admin role**: A system-wide admin role that bypasses permission checks
- **Public resources**: Support for publicly accessible documents without authentication
