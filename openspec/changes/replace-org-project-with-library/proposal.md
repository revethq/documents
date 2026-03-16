# Change: Replace local Organization, Project, and Tag with revet-core library

## Why

The Organization, Project, and Tag domain models, entities, repositories, mappers, DTOs, and DTO mappers in the Documents service are duplicates of what the `revet-core` library (`com.revethq:revet-core-*:0.2.1`) now provides. The library also provides service interfaces, REST resources, and URN-based tagging. Replacing local implementations reduces maintenance burden and ensures consistency across Revet services.

## What Changes

- **Remove ~24 local files** — domain models, entities, repository interfaces/implementations, entity mappers, service interfaces, DTOs, and DTO mappers for Organization, Project, Tag, and TaggedItem
- **Update revet-core dependency** — version `0.1.0` (single module) becomes `0.2.1` (three modules: core, persistence-runtime, web)
- **Add new Gradle dependencies** — `revet-core-persistence-runtime` to `persistence-runtime` module, `revet-core-web` to `web` module
- **Subclass library REST resources** — create `DocumentsOrganizationResource` and `DocumentsProjectResource` that extend the library's resources and add `@RequiresPermission` annotations on every endpoint
- **Exclude library resource beans** — use `quarkus.arc.exclude-types` to prevent the library's un-annotated resources and `GlobalExceptionMapper` from registering
- **Delete local service interfaces** — `OrganizationService` and `ProjectService` interfaces now come from `com.revethq.core.service`
- **Keep local service implementations** — `OrganizationServiceImpl`, `ProjectServiceImpl`, and `TagServiceImpl` remain local (documents-specific IAM policy creation logic)
- **Keep local TagResource** — the library has no TagResource; update it to use library's `TagDTO`/`TagDTOMapper` and org-scoped operations
- **Update TagService interface** — becomes organization-scoped; document-tag operations change from Django content_type pattern to URN-based tagging via `TaggedItemRepository`
- **BREAKING: Tag model gains `organizationId`** — tags become organization-scoped; API responses include `organizationId`
- **BREAKING: TaggedItem changes from `objectId`/`contentTypeId` to `resourceUrn`** — URN-based resource tagging replaces Django's polymorphic pattern
- **BREAKING: Tag table renames** — `taggit_tag` → `revet_tags`, `taggit_taggeditem` → `revet_tagged_items`
- **No Organization/Project database migration** — table names are identical (`revet_organizations`, `revet_projects`)
- **No permission changes** — actions remain `documents:*` prefix, URNs remain `urn:revet:documents:*`

## Impact

- Affected specs: `org-project-tag-management` (new spec)
- Affected code:
  - **Deleted (core)**: `domain/Organization.kt`, `domain/Project.kt`, `domain/Tag.kt`, `repository/OrganizationRepository.kt`, `repository/ProjectRepository.kt`, `repository/TagRepository.kt`, `service/OrganizationService.kt`, `service/ProjectService.kt`
  - **Deleted (persistence-runtime)**: `repository/entity/OrganizationEntity.kt`, `repository/entity/ProjectEntity.kt`, `repository/entity/TagEntity.kt`, `repository/entity/TaggedItemEntity.kt`, `repository/mapper/OrganizationMapper.kt`, `repository/mapper/ProjectMapper.kt`, `repository/mapper/TagMapper.kt`, `repository/OrganizationRepositoryImpl.kt`, `repository/ProjectRepositoryImpl.kt`, `repository/TagRepositoryImpl.kt`
  - **Deleted (web)**: `dto/OrganizationDTO.kt`, `dto/ProjectDTO.kt`, `dto/TagDTO.kt`, `api/mapper/OrganizationDTOMapper.kt`, `api/mapper/ProjectDTOMapper.kt`, `api/mapper/TagDTOMapper.kt`
  - **Replaced (web)**: `api/resource/OrganizationResource.kt` → `DocumentsOrganizationResource.kt` (subclass), `api/resource/ProjectResource.kt` → `DocumentsProjectResource.kt` (subclass)
  - **Modified (core)**: `service/TagService.kt` (org-scoped + URN-based operations)
  - **Modified (persistence-runtime)**: `service/OrganizationServiceImpl.kt`, `service/ProjectServiceImpl.kt`, `service/TagServiceImpl.kt`
  - **Modified (web)**: `api/resource/TagResource.kt`, `api/resource/DocumentResource.kt`
  - **Modified (build)**: `gradle/libs.versions.toml`, `core/build.gradle.kts`, `persistence-runtime/build.gradle.kts`, `web/build.gradle.kts`
  - **Modified (config)**: `application.properties` (add `quarkus.arc.exclude-types`)
  - **Modified (permissions)**: `permission/PrebuiltPolicies.kt`, `permission/DocumentsUrn.kt`
  - **New**: SQL migration script for tag table renames and schema changes
