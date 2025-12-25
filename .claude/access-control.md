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

## SecurityService API

The `SecurityService` provides permission checking with inheritance:

```kotlin
interface SecurityService {
    // Check if user has required permission (with inheritance)
    fun canAccessDocument(userId: Long, documentId: Long, requiredPermission: PermissionType): Boolean
    fun canAccessProject(userId: Long, projectId: Long, requiredPermission: PermissionType): Boolean
    fun canAccessOrganization(userId: Long, organizationId: Long, requiredPermission: PermissionType): Boolean

    // Get effective permission (highest from any level)
    fun getEffectiveDocumentPermission(userId: Long, documentId: Long): PermissionType?
    fun getEffectiveProjectPermission(userId: Long, projectId: Long): PermissionType?
    fun getOrganizationPermission(userId: Long, organizationId: Long): PermissionType?
}
```

### Usage Example

```kotlin
@Path("/api/v1/documents")
class DocumentResource @Inject constructor(
    private val securityService: SecurityService,
    private val documentService: DocumentService
) {
    @PUT
    @Path("/{id}")
    fun updateDocument(
        @PathParam("id") id: Long,
        request: UpdateDocumentRequest,
        @Context securityIdentity: SecurityIdentity
    ): Response {
        val userId = securityIdentity.principal.name.toLong()

        if (!securityService.canAccessDocument(userId, id, PermissionType.CAN_CREATE)) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(mapOf("error" to "Insufficient permissions"))
                .build()
        }

        // Proceed with update...
    }
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
