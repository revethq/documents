## 1. PermissionFilterService

- [x] 1.1 Create `persistence-runtime/src/main/kotlin/com/revethq/documents/service/PermissionFilterService.kt` — `@RequestScoped` bean injecting `PolicyEvaluator`, `PolicyCollector`, `AuthorizationContext`. Caches policies per request. Provides `filter(items, action, urnBuilder)` method using `evaluateWithPolicies()`.

## 2. Service layer filtering

- [x] 2.1 Update `DocumentServiceImpl` — inject `PermissionFilterService`, filter results in `getDocumentsPaginated()` and `getAllDocuments()` using action `documents:GetDocument` and URN `DocumentsUrn.document(tenant, uuid)`
- [x] 2.2 Update `ProjectServiceImpl` — filter results in `getAllProjects()` and `getProjectsByOrganizationId()` using action `core:GetProject` and URN `DocumentsUrn.project(tenant, uuid)`
- [x] 2.3 Update `OrganizationServiceImpl` — filter results in `getAllOrganizations()` using action `core:GetOrganization` and URN `DocumentsUrn.organization(tenant, uuid)`
- [x] 2.4 Update `TagServiceImpl` — filter results in `getAllTags()` using action `documents:GetTag` and URN `DocumentsUrn.tag(tenant, id)`
- [x] 2.5 Update `CategoryServiceImpl` — filter results in `getAllCategories()` and `getCategoriesByProjectId()` using action `documents:GetCategory` and URN `DocumentsUrn.category(tenant, id)`
- [x] 2.6 Update `SearchServiceImpl` — filter results in `searchDocuments()`, `searchProjects()`, `searchOrganizations()`, and `searchAll()` using appropriate actions and URNs

## 3. Remove @RequiresPermission from documents-owned list endpoints

- [x] 3.1 `DocumentResource.listDocuments()` — replace `@RequiresPermission` with `@Authenticated`
- [x] 3.2 `CategoryResource.listCategories()` — replace `@RequiresPermission` with `@Authenticated`
- [x] 3.3 `TagResource.listTags()` — replace `@RequiresPermission` with `@Authenticated`
- [x] 3.4 `SearchResource` — replace `@RequiresPermission` with `@Authenticated` on `searchAll()`, `searchDocuments()`, `searchProjects()`, `searchOrganizations()`

## 4. Local resource subclasses for library endpoints

- [x] 4.1 Create `web/.../api/resource/DocumentsProjectResource.kt` — extends `com.revethq.core.api.resource.ProjectResource`, overrides `listProjects()` with `@Authenticated` instead of `@RequiresPermission`, delegates to `super`
- [x] 4.2 Create `web/.../api/resource/DocumentsOrganizationResource.kt` — extends `com.revethq.core.api.resource.OrganizationResource`, overrides `listOrganizations()` with `@Authenticated`, delegates to `super`
- [x] 4.3 Create `web/.../api/resource/DocumentsBucketResource.kt` — extends `com.revethq.buckets.web.api.resource.BucketResource`, overrides `listBuckets()` with `@Authenticated`, delegates to `super`
- [x] 4.4 Update `application.properties` — set `revet.core.resources.disabled=true`, `revet.buckets.resources.disabled=true`, and `revet.iam.resources.disabled=true` to disable library resource classes via `@UnlessBuildProperty`

## 4b. Add @UnlessBuildProperty to library resource classes

- [x] 4b.1 Add `@UnlessBuildProperty(name = "revet.buckets.resources.disabled", stringValue = "true", enableIfMissing = true)` to `BucketResource` in `revet-buckets-web`
- [x] 4b.2 Add `@UnlessBuildProperty(name = "revet.iam.resources.disabled", stringValue = "true", enableIfMissing = true)` to all 14 IAM resource classes:
  - `permission-web`: `PolicyResource`, `WellKnownPermissionsResource`, `GroupPolicyResource`, `UserPolicyResource`
  - `user-web`: `UserResource`, `GroupResource`
  - `service-account-web`: `ServiceAccountResource`, `ServiceAccountPolicyResource`, `ServiceAccountProfileResource`
  - `scim`: `UserResource`, `GroupResource`, `SchemaResource`, `ResourceTypeResource`, `ServiceProviderConfigResource`
- [x] 4b.3 Add `quarkus-arc` dependency to IAM modules that lack it (`permission-web`, `user-web` need BOM + quarkus-arc; `service-account-web`, `scim` need quarkus-arc added)

## 5. Verification

- [ ] 5.1 Start dev server (`./gradlew :web:quarkusDev`), authenticate as a user with only a project-scoped policy, verify `GET /api/v1/projects` returns only the permitted project
- [ ] 5.2 Verify `GET /api/v1/documents` returns only documents the user has access to
- [ ] 5.3 Verify global admin user still sees all resources
- [ ] 5.4 Verify unauthenticated request returns 401
