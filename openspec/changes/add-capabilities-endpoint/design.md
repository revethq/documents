## Context

The `revet-capabilities` library (`com.revethq.capabilities`) is a published library with three modules that mirror the documents service architecture:

- **`revet-capabilities-core`** — Domain models (`CapabilityDeclaration`, `CapabilityManifest`, `ResolvedCapability`, etc.), `CapabilityProvider` interface, `CapabilityRegistry`, `CapabilityEvaluator`, and `TenantCapabilityStore` interface
- **`revet-capabilities-persistence-runtime`** — Panache entity, repository, and `PanacheTenantCapabilityStore` implementation for the `revet_tenant_capabilities` table
- **`revet-capabilities-web`** — `CapabilityResource` JAX-RS endpoint (`GET /api/v1/capabilities`) and DTOs

The library uses CDI discovery — any bean implementing `CapabilityProvider` is automatically picked up by the `CapabilityRegistry`, and the `CapabilityResource` endpoint is auto-registered by Quarkus.

## Goals / Non-Goals

- **Goal**: Expose a `GET /api/v1/capabilities` endpoint that returns the current user's resolved capabilities for the documents service
- **Goal**: Declare all documents service permission actions as structured capabilities, including cross-service capabilities for organizations, projects, buckets, and groups
- **Non-Goal**: Add admin endpoints for managing tenant capability overrides (future work)
- **Non-Goal**: Modify existing permission checks on other endpoints

## Decisions

### Capability Grouping

Capabilities group related permission actions by functional area. The manifest includes both documents-native actions and cross-service actions from `revet-core`, `revet-buckets`, and `revet-iam`:

**Documents-native capabilities** (actions from `Actions.kt`):

| Capability ID | Name | Category | Actions | Resource URN |
|---|---|---|---|---|
| `documents:manage-documents` | Manage Documents | documents | All `Actions.Document` actions | `urn:revet:documents:{tenantId}:document/*` |
| `documents:manage-versions` | Manage Document Versions | documents | All `Actions.DocumentVersion` actions | `urn:revet:documents:{tenantId}:document-version/*` |
| `documents:manage-categories` | Manage Categories | documents | All `Actions.Category` actions | `urn:revet:documents:{tenantId}:category/*` |
| `documents:manage-tags` | Manage Tags | documents | All `Actions.Tag` actions | `urn:revet:documents:{tenantId}:tag/*` |
| `documents:manage-users` | Manage Users | documents | All `Actions.User` actions | `urn:revet:documents:{tenantId}:user/*` |
| `documents:upload-files` | Upload Files | documents | All `Actions.FileUpload` actions | `urn:revet:documents:{tenantId}:document-version/*` |
| `documents:search` | Search Documents | documents | All `Actions.Search` actions | `urn:revet:documents:{tenantId}:document/*` |

**Cross-service capabilities** (actions from external libraries):

| Capability ID | Name | Category | Actions Source | Resource URN |
|---|---|---|---|---|
| `documents:manage-organizations` | Manage Organizations | core | `CoreActions.Organization` (LIST, GET, CREATE, UPDATE, DELETE) | `urn:revet:core:{tenantId}:organization/*` |
| `documents:manage-projects` | Manage Projects | core | `CoreActions.Project` (LIST, GET, CREATE, UPDATE, DELETE) | `urn:revet:core:{tenantId}:project/*` |
| `documents:manage-buckets` | Manage Buckets | storage | `BucketActions.Bucket` (LIST, GET, CREATE, UPDATE, DELETE) | `urn:revet:buckets:{tenantId}:bucket/*` |
| `documents:manage-groups` | Manage Groups | iam | `IamActions.Group` (LIST, GET, CREATE, UPDATE, DELETE, LIST_MEMBERS, ADD_MEMBER, REMOVE_MEMBER) | `urn:revet:iam:{tenantId}:group/*` |

Each capability uses a resource-type-specific wildcard URN scoped to the tenant, matching the URN patterns already used in `DocumentsPermissionProvider` and `PrebuiltPolicies`.

### Provider Location

`DocumentsCapabilityProvider` lives in `persistence-runtime` alongside other CDI beans (permission provider, service implementations). This keeps `core` framework-agnostic and ensures the provider is available whether the service runs as a standalone library or with the web layer.

### Database Migration

The `revet_tenant_capabilities` table migration goes in the documents service's Flyway migrations since the persistence-runtime module manages the entity but doesn't ship migrations. The table schema matches the `TenantCapabilityEntity` in the library.

## Risks / Trade-offs

- **Risk**: Library version compatibility — the `revet-capabilities` library depends on the same `revet-iam` version. Mitigation: use the same version already in the project.
- **Trade-off**: All capabilities use resource-type-specific wildcard URNs (e.g., `urn:revet:documents:{tenantId}:document/*`). This evaluates at the tenant level rather than per-resource. This is appropriate for UI feature toggling but won't tell you about per-document permissions.
- **Trade-off**: Cross-service capabilities (organizations, projects, buckets, groups) reference actions and URNs from other services. This means the capability provider depends on `revet-core`, `revet-buckets`, and `revet-iam` action constants. These are already dependencies of the project.

## Open Questions

- What version of `revet-capabilities` to use? Assuming `0.1.0` as the initial release.
