## ADDED Requirements

### Requirement: Library-Based Organization and Project Models

The Documents service SHALL use the `revet-core` library (`com.revethq:revet-core-*:0.2.1`) for Organization and Project domain models, service interfaces, entities, repository interfaces, repository implementations, entity mappers, DTOs, and DTO mappers instead of local implementations.

#### Scenario: Organization domain model from library

- **WHEN** any service or resource references the `Organization` domain model
- **THEN** it uses `com.revethq.core.Organization` from the `revet-core-core` module

#### Scenario: Project domain model from library

- **WHEN** any service or resource references the `Project` domain model
- **THEN** it uses `com.revethq.core.Project` from the `revet-core-core` module

#### Scenario: Service interfaces from library

- **WHEN** `OrganizationServiceImpl` or `ProjectServiceImpl` declares which interface it implements
- **THEN** it implements `com.revethq.core.service.OrganizationService` or `com.revethq.core.service.ProjectService` from the library

#### Scenario: Persistence from library

- **WHEN** an `OrganizationRepository` or `ProjectRepository` is injected via CDI
- **THEN** the implementation is provided by `revet-core-persistence-runtime`
- **AND** data is persisted to `revet_organizations`, `revet_projects`, `project_clients`, and `project_tags` tables with no schema changes

#### Scenario: DTOs and DTO mappers from library

- **WHEN** REST resources convert between domain models and API responses
- **THEN** they use `OrganizationDTO`, `ProjectDTO`, request DTOs, `OrganizationDTOMapper`, and `ProjectDTOMapper` from `revet-core-web`

### Requirement: Subclassed REST Resources with Permission Annotations

The Documents service SHALL subclass the library's `OrganizationResource` and `ProjectResource` to add `@RequiresPermission` annotations with `documents:*` action prefixes. The library's un-annotated resource beans and `GlobalExceptionMapper` SHALL be excluded from CDI via `quarkus.arc.exclude-types`.

#### Scenario: Organization REST endpoints use subclassed resource

- **WHEN** a client sends requests to `/api/v1/organizations`
- **THEN** the requests are handled by `DocumentsOrganizationResource` which extends the library's `OrganizationResource`
- **AND** each endpoint has `@RequiresPermission` with actions like `documents:ListOrganizations`, `documents:GetOrganization`, etc.
- **AND** method bodies delegate to `super` for the actual logic

#### Scenario: Project REST endpoints use subclassed resource

- **WHEN** a client sends requests to `/api/v1/projects`
- **THEN** the requests are handled by `DocumentsProjectResource` which extends the library's `ProjectResource`
- **AND** each endpoint has `@RequiresPermission` with actions like `documents:ListProjects`, `documents:GetProject`, etc.
- **AND** method bodies delegate to `super` for the actual logic

#### Scenario: Library resource beans excluded from CDI

- **WHEN** the Quarkus application starts
- **THEN** `com.revethq.core.api.resource.OrganizationResource`, `com.revethq.core.api.resource.ProjectResource`, and `com.revethq.core.api.resource.GlobalExceptionMapper` are excluded via `quarkus.arc.exclude-types`
- **AND** only the documents subclass resources are registered as JAX-RS endpoints

### Requirement: Library-Based Tag and TaggedItem Models

The Documents service SHALL use the `revet-core` library for Tag and TaggedItem domain models, entities, repository interfaces, repository implementations, entity mappers, DTOs, and DTO mappers. Tags SHALL be organization-scoped and resource tagging SHALL use URN-based identification.

#### Scenario: Tag domain model from library

- **WHEN** any service or resource references the `Tag` domain model
- **THEN** it uses `com.revethq.core.Tag` which includes an `organizationId` field

#### Scenario: TaggedItem uses URN-based resource identification

- **WHEN** a tag is applied to a document
- **THEN** it uses `TaggedItemRepository.addTagToResource(tagId, resourceUrn)` with a URN like `urn:revet:documents:{tenantId}:document/{uuid}`

#### Scenario: Tag persistence from library

- **WHEN** a `TagRepository` or `TaggedItemRepository` is injected via CDI
- **THEN** the implementations are provided by `revet-core-persistence-runtime`
- **AND** data is persisted to `revet_tags` and `revet_tagged_items` tables

#### Scenario: Tag DTOs from library

- **WHEN** the TagResource converts between Tag domain models and API responses
- **THEN** it uses `TagDTO` (which includes `organizationId`), `CreateTagRequest`, `UpdateTagRequest`, and `TagDTOMapper` from `revet-core-web`

### Requirement: Documents-Specific Service Implementations

The Documents service SHALL maintain local service implementations (`OrganizationServiceImpl`, `ProjectServiceImpl`, `TagServiceImpl`) that implement library or local service interfaces and provide documents-specific business logic including IAM policy creation.

#### Scenario: Organization creation creates default IAM policies

- **WHEN** a new Organization is created via `OrganizationServiceImpl`
- **THEN** admin, manager, and viewer policies are created via `PrebuiltPolicies`
- **AND** the admin policy is attached to the creating user

#### Scenario: Project creation creates default IAM policies

- **WHEN** a new Project is created via `ProjectServiceImpl`
- **THEN** admin, manager, editor, and viewer policies are created via `PrebuiltPolicies`
- **AND** the admin policy is attached to the creating user

#### Scenario: TagService uses org-scoped and URN-based operations

- **WHEN** `TagServiceImpl` creates or looks up a tag
- **THEN** it uses the library's `TagRepository` with `organizationId` scoping
- **AND** document-tag associations use `TaggedItemRepository.addTagToResource()` with document URNs

### Requirement: Documents-Specific TagResource

The Documents service SHALL maintain a local `TagResource` since the library does not provide one. The TagResource SHALL use library DTOs and DTO mappers, org-scoped operations, and `@RequiresPermission` annotations with `documents:*` action prefixes.

#### Scenario: Tag REST endpoints use local resource

- **WHEN** a client sends requests to `/api/v1/tags`
- **THEN** the requests are handled by the local `TagResource`
- **AND** permission checks use `documents:ListTags`, `documents:GetTag`, etc.

## REMOVED Requirements

### Requirement: Local Organization Domain Model, Service Interface, and Persistence
**Reason**: Replaced by `com.revethq.core.Organization`, `com.revethq.core.service.OrganizationService`, and `revet-core-persistence-runtime`.
**Migration**: Delete 8 local files. Update all imports.

### Requirement: Local Project Domain Model, Service Interface, and Persistence
**Reason**: Replaced by `com.revethq.core.Project`, `com.revethq.core.service.ProjectService`, and `revet-core-persistence-runtime`.
**Migration**: Delete 8 local files. Update all imports.

### Requirement: Local Tag Domain Model and Persistence
**Reason**: Replaced by `com.revethq.core.Tag`, `com.revethq.core.TaggedItem`, and their corresponding entities, mappers, and repositories. Tags gain `organizationId`.
**Migration**: Delete 8 local files. Rename database tables. Add `organization_id` column. Migrate tagged item data to URN format.

### Requirement: Local Organization and Project REST Resources
**Reason**: Replaced by subclasses of the library's REST resources that add `@RequiresPermission`.
**Migration**: Delete `OrganizationResource.kt` and `ProjectResource.kt`. Create `DocumentsOrganizationResource.kt` and `DocumentsProjectResource.kt`.

### Requirement: Local Organization, Project, and Tag DTOs and DTO Mappers
**Reason**: Replaced by `revet-core-web` which provides all DTOs, request objects, and DTO mappers.
**Migration**: Delete 6 local DTO/mapper files. Update imports in resources and any other consumers.

### Requirement: Local TaggedItem with Django Content Type Pattern
**Reason**: Replaced by URN-based `TaggedItem`. Django's `objectId`/`contentTypeId` replaced by `resourceUrn`.
**Migration**: Update `TagServiceImpl` to use `TaggedItemRepository`. Update `DocumentServiceImpl` to construct document URNs. SQL migration to convert existing data.
