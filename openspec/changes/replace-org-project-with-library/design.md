## Context

The Documents service has local copies of Organization, Project, Tag, and TaggedItem across all layers (domain, entity, repository, mapper, DTO, resource). The `revet-core` library v0.2.1 provides these models in a three-module structure (`core`, `persistence-runtime`, `web`) plus service interfaces and REST resources. This is the same extraction pattern previously applied to buckets via `revet-buckets`.

## Goals / Non-Goals

- Goals:
  - Replace local Organization/Project/Tag domain models, service interfaces, entities, repositories, mappers, DTOs, and DTO mappers with `revet-core` library modules
  - Subclass library REST resources to add `@RequiresPermission` annotations
  - Adopt URN-based tagging replacing Django's polymorphic pattern
  - Make tags organization-scoped
  - Maintain identical REST API behavior
  - Preserve IAM policy creation on organization/project creation
- Non-Goals:
  - Moving service implementations to the library
  - Changing permission actions or URN formats
  - Adding a TagResource to the library

## Decisions

### 1. Subclass library REST resources for Organization and Project

The library provides `OrganizationResource` and `ProjectResource` with identical method bodies and error handling to the local resources. The only difference is that local resources have `@RequiresPermission` annotations. Instead of duplicating the full resource code, create thin subclasses:

```kotlin
@Path("/api/v1/organizations")
class DocumentsOrganizationResource
    @Inject constructor(organizationService: OrganizationService)
    : OrganizationResource(organizationService) {

    @GET
    @RequiresPermission(action = Actions.Organization.LIST, resource = "urn:revet:documents:{tenantId}:organization/*")
    override fun listOrganizations(includeInactive: Boolean) = super.listOrganizations(includeInactive)
    // ... override other methods similarly
}
```

Exclude the library's resource beans so only the subclasses register:
```properties
quarkus.arc.exclude-types=com.revethq.core.api.resource.OrganizationResource,com.revethq.core.api.resource.ProjectResource,com.revethq.core.api.resource.GlobalExceptionMapper
```

- Alternative: Keep full local resources. Rejected because it duplicates ~580 lines of identical code that must stay in sync with the library.
- Alternative: Use the library's resources as-is without permission checks. Rejected because all endpoints require authorization.

### 2. Use library's web module for DTOs and DTO mappers

Including `revet-core-web` gives us `OrganizationDTO`, `ProjectDTO`, `TagDTO`, all request DTOs, and all DTO mappers. Delete local copies.

The library's `TagDTO` includes `organizationId` (the local one does not). This is correct since tags are now organization-scoped. The API response changes to include this field.

### 3. Use library service interfaces, keep local implementations

The library provides `OrganizationService` and `ProjectService` interfaces in its `core` module (identical to local interfaces). Delete local copies. Service implementations implement `com.revethq.core.service.OrganizationService` / `ProjectService`.

### 4. Keep TagResource and TagService local

The library has no TagResource or TagService. The local TagResource stays, updated to use library's `TagDTO` and `TagDTOMapper`. The local `TagService` interface is updated for org-scoped and URN-based operations.

### 5. URN-based tagging replaces Django content_type pattern

The local `TaggedItemEntity` uses `objectId: Int` + `contentTypeId: Int`. The library uses `resourceUrn: String`. Document tag operations change to use URN strings like `urn:revet:documents:{tenantId}:document/{uuid}`.

### 6. SQL migration for tag tables

- Rename `taggit_tag` → `revet_tags`
- Add `organization_id` column to `revet_tags` (NOT NULL, populate from document→project→organization chain)
- Drop `taggit_taggeditem`, create `revet_tagged_items` with `tag_id` + `resource_urn`
- Migrate existing tagged item data by converting `objectId`/`contentTypeId` to URN format

### 7. No Organization/Project database migration

Table names, column names, and collection tables are identical between local and library.

## Risks / Trade-offs

- **Tag data migration** → Existing tagged items must be converted to URN strings. Mitigation: SQL migration with explicit mapping per content type.
- **Organization ID for existing tags** → Existing tags have no `organization_id`. Mitigation: Populate from document→project→organization relationship.
- **Library version coupling** → Documents service depends on `revet-core:0.2.1` across three modules.
- **Subclass fragility** → If the library adds new methods to its resources, subclasses need updating. Mitigated by compilation errors when overrides are incomplete.
- **Excluded library beans** → Must remember to exclude new library resources if added in future versions.

## Migration Plan

1. Update `libs.versions.toml` — version bump and new module entries
2. Update `build.gradle.kts` files
3. Add `quarkus.arc.exclude-types` to `application.properties`
4. Create SQL migration for tag tables
5. Delete local domain models, service interfaces, entities, repos, mappers, DTOs, DTO mappers (~24 files)
6. Create subclass resources (`DocumentsOrganizationResource`, `DocumentsProjectResource`)
7. Update TagService interface for org-scoped and URN-based operations
8. Update TagServiceImpl, service implementations, and remaining resources
9. Build and verify

Rollback: Revert the commit and run reverse SQL migration.

## Open Questions

- What default `organization_id` to assign to existing tags during migration? (Likely derive from document→project→organization relationship)
