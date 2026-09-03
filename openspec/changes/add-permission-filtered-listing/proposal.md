# Change: Add permission-filtered listing for all list endpoints

## Why

List endpoints use `@RequiresPermission` with wildcard resource URNs (e.g., `core:ListProjects` on `urn:revet:core:{tenantId}:project/*`), which requires blanket access to all resources of that type. Users with project-scoped policies (e.g., access to a single project) receive 403 Forbidden on every list endpoint, even though they should see the resources they have access to.

## What Changes

- Remove `@RequiresPermission` from all list endpoints; replace with `@Authenticated`
- Add a `PermissionFilterService` in `persistence-runtime` that filters query results by the authenticated user's policies
- Update every service implementation's list/search methods to filter results through `PermissionFilterService`
- Create local resource subclasses for library list endpoints (projects, organizations, buckets) that use `@Authenticated` instead of `@RequiresPermission`

## Impact

- Affected specs: `org-project-management` (from `replace-org-project-with-library` change — list endpoint authorization approach changes)
- Affected code:
  - **New**: `persistence-runtime/.../service/PermissionFilterService.kt`
  - **Modified**: `DocumentServiceImpl`, `ProjectServiceImpl`, `OrganizationServiceImpl`, `TagServiceImpl`, `SearchServiceImpl`, `CategoryServiceImpl`
  - **Modified**: `DocumentResource`, `CategoryResource`, `TagResource`, `SearchResource` (remove `@RequiresPermission` from list methods)
  - **New**: `DocumentsProjectResource`, `DocumentsOrganizationResource`, `DocumentsBucketResource` (local subclasses overriding list methods)
  - **Modified**: `application.properties` (set `revet.core.resources.disabled=true`, `revet.buckets.resources.disabled=true`, `revet.iam.resources.disabled=true` to disable library resource classes via `@UnlessBuildProperty`)
- Affected libraries (upstream changes):
  - **Modified**: `revet-buckets-web` `BucketResource` — added `@UnlessBuildProperty(name = "revet.buckets.resources.disabled")`
  - **Modified**: `revet-iam` — added `@UnlessBuildProperty(name = "revet.iam.resources.disabled")` to all 14 resource classes across `permission-web`, `user-web`, `service-account-web`, and `scim` modules
