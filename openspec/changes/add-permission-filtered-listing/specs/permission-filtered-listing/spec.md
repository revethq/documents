## ADDED Requirements

### Requirement: Permission-filtered list endpoints

All list endpoints SHALL require authentication via `@Authenticated` instead of `@RequiresPermission`. The service layer SHALL filter results so that each user only sees resources their attached policies grant access to. Users with wildcard permissions SHALL see all resources. Unauthenticated requests SHALL receive 401 Unauthorized.

#### Scenario: User with project-scoped permission lists projects
- **WHEN** a user with `core:GetProject` permission on `urn:revet:core::project/{projectId}` calls `GET /api/v1/projects`
- **THEN** the response contains only the project matching `{projectId}`
- **AND** the HTTP status is 200

#### Scenario: User with project-scoped permission lists documents
- **WHEN** a user with `documents:GetDocument` permission on `urn:revet:documents::document/*` scoped to a specific project calls `GET /api/v1/documents`
- **THEN** the response contains only documents the user's policies grant access to
- **AND** the HTTP status is 200

#### Scenario: Admin user with wildcard permission lists all resources
- **WHEN** a user with `documents:*` on `urn:revet:documents:*:*/*` calls any list endpoint
- **THEN** the response contains all resources (no filtering)

#### Scenario: User with no matching permissions lists resources
- **WHEN** an authenticated user with no policies granting access to any resource of that type calls a list endpoint
- **THEN** the response contains an empty list
- **AND** the HTTP status is 200

#### Scenario: Unauthenticated request to list endpoint
- **WHEN** an unauthenticated client calls any list endpoint
- **THEN** the response is 401 Unauthorized

### Requirement: PermissionFilterService

The system SHALL provide a `PermissionFilterService` as a `@RequestScoped` CDI bean in the `persistence-runtime` module. It SHALL collect the authenticated user's policies once per request via `PolicyCollector` and cache them for the request duration. It SHALL provide a generic `filter()` method that accepts a list of items, an action string, and a URN builder function, and returns only items the user's policies permit. It SHALL use `PolicyEvaluator.evaluateWithPolicies()` for in-memory evaluation.

#### Scenario: Policies collected once per request
- **WHEN** multiple service methods call `PermissionFilterService.filter()` during a single HTTP request
- **THEN** policies are fetched from the database exactly once
- **AND** subsequent calls reuse the cached policies

#### Scenario: Filter with Get action
- **WHEN** `PermissionFilterService.filter()` evaluates a document
- **THEN** it checks the `documents:GetDocument` action against `urn:revet:documents::document/{uuid}`
- **AND** only items where the evaluation result is ALLOW are included

#### Scenario: Unauthenticated context
- **WHEN** `PermissionFilterService.filter()` is called without an authenticated principal
- **THEN** it returns an empty list

### Requirement: List endpoint authorization for library resources

The Documents service SHALL create local resource subclasses for `ProjectResource`, `OrganizationResource`, and `BucketResource` from the revet-core and revet-buckets libraries. These subclasses SHALL override only the list method, replacing `@RequiresPermission` with `@Authenticated`. All other methods (get, create, update, delete) SHALL inherit `@RequiresPermission` from the parent. Library resource classes SHALL be disabled via `@UnlessBuildProperty` annotations with corresponding build properties (`revet.core.resources.disabled`, `revet.buckets.resources.disabled`) set to `true` in `application.properties` to prevent duplicate endpoint registration.

#### Scenario: Local project list endpoint
- **WHEN** a client sends `GET /api/v1/projects`
- **THEN** the request is handled by `DocumentsProjectResource` which extends the library's `ProjectResource`
- **AND** the list method uses `@Authenticated` (not `@RequiresPermission`)
- **AND** results are filtered by `PermissionFilterService`

#### Scenario: Library get/create/update/delete endpoints unchanged
- **WHEN** a client sends `GET /api/v1/projects/{uuid}` or `POST /api/v1/projects`
- **THEN** the inherited `@RequiresPermission` annotation from the parent resource is enforced
- **AND** the authorization check uses the original action and resource URN pattern

#### Scenario: Library resource classes disabled via build property
- **WHEN** `revet.core.resources.disabled=true` is set in `application.properties`
- **THEN** the library's `ProjectResource` and `OrganizationResource` are not registered as JAX-RS endpoints
- **AND** only the local subclasses (`DocumentsProjectResource`, `DocumentsOrganizationResource`) are active

### Requirement: IAM library resource classes disableable

All JAX-RS resource classes in the `revet-iam` library SHALL be annotated with `@UnlessBuildProperty(name = "revet.iam.resources.disabled", stringValue = "true", enableIfMissing = true)`. This applies to 14 resource classes across `permission-web` (`PolicyResource`, `WellKnownPermissionsResource`, `GroupPolicyResource`, `UserPolicyResource`), `user-web` (`UserResource`, `GroupResource`), `service-account-web` (`ServiceAccountResource`, `ServiceAccountPolicyResource`, `ServiceAccountProfileResource`), and `scim` (`UserResource`, `GroupResource`, `SchemaResource`, `ResourceTypeResource`, `ServiceProviderConfigResource`). The Documents service SHALL set `revet.iam.resources.disabled=true` to prevent these endpoints from being registered.

#### Scenario: IAM resources disabled in documents app
- **WHEN** `revet.iam.resources.disabled=true` is set in `application.properties`
- **THEN** none of the IAM library resource endpoints (policies, users, groups, service accounts, SCIM) are registered
- **AND** IAM service beans (PolicyService, UserService, etc.) remain available for injection

#### Scenario: IAM resources enabled by default in other apps
- **WHEN** `revet.iam.resources.disabled` is not set in `application.properties`
- **THEN** all IAM library resource endpoints are registered and available (due to `enableIfMissing = true`)

### Requirement: Documents-owned list endpoints use @Authenticated

The `DocumentResource`, `CategoryResource`, `TagResource`, and `SearchResource` list methods SHALL use `@Authenticated` instead of `@RequiresPermission`. All non-list methods SHALL retain their existing `@RequiresPermission` annotations unchanged.

#### Scenario: Document list endpoint authorization change
- **WHEN** a client sends `GET /api/v1/documents`
- **THEN** the endpoint requires authentication but does not check `@RequiresPermission`
- **AND** results are filtered by `PermissionFilterService` in the service layer

#### Scenario: Document get endpoint unchanged
- **WHEN** a client sends `GET /api/v1/documents/{uuid}`
- **THEN** the endpoint still uses `@RequiresPermission(action = "documents:GetDocument", resource = "urn:revet:documents:{tenantId}:document/{uuid}")`
