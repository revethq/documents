## 1. Dependencies & Configuration

- [x] 1.1 Update `gradle/libs.versions.toml` — bump `revet-core` version from `0.1.0` to `0.2.1` and add `revet-core-core`, `revet-core-persistence-runtime`, and `revet-core-web` library entries
- [x] 1.2 Update `core/build.gradle.kts` — change `api(libs.revet.core)` to `api(libs.revet.core.core)`
- [x] 1.3 Update `persistence-runtime/build.gradle.kts` — add `implementation(libs.revet.core.persistence)`
- [x] 1.4 Update `web/build.gradle.kts` — replace `implementation(libs.revet.core)` with `implementation(libs.revet.core.web)`
- [x] 1.5 ~~Add `quarkus.arc.exclude-types`~~ Removed revet-core-web from Jandex index to avoid duplicate JAX-RS endpoint registration (subclass resources handle endpoints locally)

## 2. Database Migration

- [x] 2.1 Create SQL migration to rename `taggit_tag` → `revet_tags`
- [x] 2.2 Create SQL migration to add `organization_id` column to `revet_tags` (NOT NULL, populate from document→project→organization chain for existing rows)
- [x] 2.3 Create SQL migration to rename `taggit_taggeditem` → `revet_tagged_items`, drop `object_id` and `content_type_id` columns, add `resource_urn` column (VARCHAR 512, NOT NULL), and migrate existing data by converting `object_id`/`content_type_id` to URN format

## 3. Delete Local Organization Implementation

- [x] 3.1 Delete `core/src/main/kotlin/com/revethq/documents/domain/Organization.kt`
- [x] 3.2 Delete `core/src/main/kotlin/com/revethq/documents/repository/OrganizationRepository.kt`
- [x] 3.3 Delete `core/src/main/kotlin/com/revethq/documents/service/OrganizationService.kt` (replaced by `com.revethq.core.service.OrganizationService`)
- [x] 3.4 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/entity/OrganizationEntity.kt`
- [x] 3.5 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/mapper/OrganizationMapper.kt`
- [x] 3.6 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/OrganizationRepositoryImpl.kt`
- [x] 3.7 Delete `web/src/main/kotlin/com/revethq/documents/dto/OrganizationDTO.kt`
- [x] 3.8 Delete `web/src/main/kotlin/com/revethq/documents/api/mapper/OrganizationDTOMapper.kt`

## 4. Delete Local Project Implementation

- [x] 4.1 Delete `core/src/main/kotlin/com/revethq/documents/domain/Project.kt`
- [x] 4.2 Delete `core/src/main/kotlin/com/revethq/documents/repository/ProjectRepository.kt`
- [x] 4.3 Delete `core/src/main/kotlin/com/revethq/documents/service/ProjectService.kt` (replaced by `com.revethq.core.service.ProjectService`)
- [x] 4.4 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/entity/ProjectEntity.kt`
- [x] 4.5 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/mapper/ProjectMapper.kt`
- [x] 4.6 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/ProjectRepositoryImpl.kt`
- [x] 4.7 Delete `web/src/main/kotlin/com/revethq/documents/dto/ProjectDTO.kt`
- [x] 4.8 Delete `web/src/main/kotlin/com/revethq/documents/api/mapper/ProjectDTOMapper.kt`

## 5. Delete Local Tag/TaggedItem Implementation

- [x] 5.1 Delete `core/src/main/kotlin/com/revethq/documents/domain/Tag.kt`
- [x] 5.2 Delete `core/src/main/kotlin/com/revethq/documents/repository/TagRepository.kt`
- [x] 5.3 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/entity/TagEntity.kt`
- [x] 5.4 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/entity/TaggedItemEntity.kt`
- [x] 5.5 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/mapper/TagMapper.kt`
- [x] 5.6 Delete `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/TagRepositoryImpl.kt`
- [x] 5.7 Delete `web/src/main/kotlin/com/revethq/documents/dto/TagDTO.kt`
- [x] 5.8 Delete `web/src/main/kotlin/com/revethq/documents/api/mapper/TagDTOMapper.kt`

## 6. Create Subclass REST Resources

- [x] 6.1 Create `web/src/main/kotlin/com/revethq/documents/api/resource/DocumentsOrganizationResource.kt` — extends library's `OrganizationResource`, overrides all 5 methods adding `@RequiresPermission` with `documents:*Organization*` actions and `urn:revet:documents:{tenantId}:organization/*` URNs, delegates to `super`
- [x] 6.2 Create `web/src/main/kotlin/com/revethq/documents/api/resource/DocumentsProjectResource.kt` — extends library's `ProjectResource`, overrides all 9 methods adding `@RequiresPermission` with `documents:*Project*` actions and `urn:revet:documents:{tenantId}:project/*` URNs, delegates to `super`
- [x] 6.3 Delete `web/src/main/kotlin/com/revethq/documents/api/resource/OrganizationResource.kt` (replaced by subclass)
- [x] 6.4 Delete `web/src/main/kotlin/com/revethq/documents/api/resource/ProjectResource.kt` (replaced by subclass)

## 7. Update TagService Interface & Implementation

- [x] 7.1 Update `core/service/TagService.kt` — add `organizationId` parameter to `createTag`, `getTagByName`, `getTagBySlug`, `getOrCreateTag`, `getAllTags`; replace `getTagsForDocument`/`addTagToDocument`/`removeTagFromDocument` with URN-based `getTagsForResource`/`addTagToResource`/`removeTagFromResource`
- [x] 7.2 Update `persistence-runtime/service/TagServiceImpl.kt` — inject library's `TagRepository` and `TaggedItemRepository`; implement updated interface using org-scoped queries and URN-based tagging

## 8. Update Service Implementations

- [x] 8.1 Update `persistence-runtime/service/OrganizationServiceImpl.kt` — implement `com.revethq.core.service.OrganizationService`, inject `com.revethq.core.repository.OrganizationRepository`
- [x] 8.2 Update `persistence-runtime/service/ProjectServiceImpl.kt` — implement `com.revethq.core.service.ProjectService`, inject `com.revethq.core.repository.ProjectRepository`

## 9. Update Remaining Resources & Imports

- [x] 9.1 Update `web/api/resource/TagResource.kt` — update Tag domain, DTO, and DTO mapper imports to library types; update endpoint logic for org-scoped operations
- [x] 9.2 Update `web/api/resource/DocumentResource.kt` — update tag-related operations to use URN-based methods
- [x] 9.3 `persistence-runtime/permission/PrebuiltPolicies.kt` — no changes needed (no references to deleted types)
- [x] 9.4 `persistence-runtime/permission/DocumentsUrn.kt` — no changes needed (no references to deleted types)
- [x] 9.5 Updated all remaining references: SearchResource.kt, SearchDTO.kt, DocumentRepositoryImpl.kt, CategoryRepositoryImpl.kt, DocumentEntity.kt, CategoryEntity.kt, DocumentVersionResource.kt, FileUploadResource.kt

## 10. Verify

- [x] 10.1 Build project (`./gradlew build`) — BUILD SUCCESSFUL (cosmetic Jandex warnings for library DTOs are expected since revet-core-web is intentionally not indexed)
- [ ] 10.2 Verify Swagger UI shows organization, project, and tag endpoints unchanged
- [ ] 10.3 Verify SQL migration applies cleanly
