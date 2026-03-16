## ADDED Requirements

### Requirement: Capabilities Endpoint

The system SHALL expose a `GET /api/v1/capabilities` endpoint that returns all declared capabilities for the current user, evaluated against their IAM policies and tenant-level overrides.

#### Scenario: Authenticated user retrieves capabilities

- **WHEN** an authenticated user sends a GET request to `/api/v1/capabilities`
- **THEN** the system returns a 200 response with a JSON body containing a `capabilities` array
- **AND** each capability includes `id`, `name`, `description`, `category`, `granted`, `enabled`, `available`, and `permissions` fields
- **AND** `granted` is true when the user has at least one permission in the capability
- **AND** `enabled` is true when the capability is not disabled at the tenant level
- **AND** `available` is true only when both `granted` and `enabled` are true

#### Scenario: Unauthenticated request

- **WHEN** an unauthenticated request is sent to `/api/v1/capabilities`
- **THEN** the system returns a 401 response

### Requirement: Documents Service Capability Declarations

The documents service SHALL declare capability manifests for all its functional areas, including both documents-native and cross-service capabilities. Each capability SHALL group related permission actions.

#### Scenario: Document management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-documents` is present in category `documents`
- **AND** it includes permission refs for all document actions (list, get, create, update, delete, download, add tag, remove tag)
- **AND** each permission ref uses resource type `urn:revet:documents:{tenantId}:document/*`

#### Scenario: Document version management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-versions` is present in category `documents`
- **AND** it includes permission refs for all document version actions
- **AND** each permission ref uses resource type `urn:revet:documents:{tenantId}:document-version/*`

#### Scenario: Category management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-categories` is present in category `documents`
- **AND** it includes permission refs for all category actions
- **AND** each permission ref uses resource type `urn:revet:documents:{tenantId}:category/*`

#### Scenario: Tag management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-tags` is present in category `documents`
- **AND** it includes permission refs for all tag actions
- **AND** each permission ref uses resource type `urn:revet:documents:{tenantId}:tag/*`

#### Scenario: User management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-users` is present in category `documents`
- **AND** it includes permission refs for all user actions
- **AND** each permission ref uses resource type `urn:revet:documents:{tenantId}:user/*`

#### Scenario: File upload capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:upload-files` is present in category `documents`
- **AND** it includes permission refs for all file upload actions
- **AND** each permission ref uses resource type `urn:revet:documents:{tenantId}:document-version/*`

#### Scenario: Search capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:search` is present in category `documents`
- **AND** it includes permission refs for the search documents action
- **AND** each permission ref uses resource type `urn:revet:documents:{tenantId}:document/*`

#### Scenario: Organization management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-organizations` is present in category `core`
- **AND** it includes permission refs for all core organization actions (list, get, create, update, delete)
- **AND** each permission ref uses resource type `urn:revet:core:{tenantId}:organization/*`

#### Scenario: Project management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-projects` is present in category `core`
- **AND** it includes permission refs for all core project actions (list, get, create, update, delete)
- **AND** each permission ref uses resource type `urn:revet:core:{tenantId}:project/*`

#### Scenario: Bucket management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-buckets` is present in category `storage`
- **AND** it includes permission refs for all bucket actions (list, get, create, update, delete)
- **AND** each permission ref uses resource type `urn:revet:buckets:{tenantId}:bucket/*`

#### Scenario: Group management capability declared

- **WHEN** the capability registry is queried
- **THEN** a capability with id `documents:manage-groups` is present in category `iam`
- **AND** it includes permission refs for all group actions (list, get, create, update, delete, list members, add member, remove member)
- **AND** each permission ref uses resource type `urn:revet:iam:{tenantId}:group/*`

### Requirement: Tenant Capability Overrides

The system SHALL support tenant-level capability overrides stored in the `revet_tenant_capabilities` database table. When a tenant override disables a capability, it SHALL be reported as `enabled: false` regardless of the user's permissions.

#### Scenario: Tenant disables a capability

- **GIVEN** a tenant override exists with `enabled = false` for capability `documents:manage-tags`
- **WHEN** a user in that tenant retrieves capabilities
- **THEN** the `documents:manage-tags` capability has `enabled: false` and `available: false`
- **AND** the `granted` field still reflects the user's actual permissions

#### Scenario: No tenant override exists

- **GIVEN** no tenant override exists for a capability
- **WHEN** a user retrieves capabilities
- **THEN** the capability has `enabled: true` by default

### Requirement: Permission Resource Scoping

Each capability permission ref SHALL use a tenant-scoped URN template matching the resource type's service namespace: `urn:revet:{service}:{tenantId}:{resourceType}/*`. The `{tenantId}` placeholder SHALL be substituted with the actual tenant ID at evaluation time.

#### Scenario: Tenant ID substitution in documents URN

- **WHEN** capabilities are evaluated for tenant `acme-corp`
- **THEN** documents-native permission checks use URNs like `urn:revet:documents:acme-corp:document/*`

#### Scenario: Tenant ID substitution in cross-service URNs

- **WHEN** capabilities are evaluated for tenant `acme-corp`
- **THEN** organization permission checks use `urn:revet:core:acme-corp:organization/*`
- **AND** project permission checks use `urn:revet:core:acme-corp:project/*`
- **AND** bucket permission checks use `urn:revet:buckets:acme-corp:bucket/*`
- **AND** group permission checks use `urn:revet:iam:acme-corp:group/*`
