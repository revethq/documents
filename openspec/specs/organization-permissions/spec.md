# Organization Permissions

## Purpose
Define API behaviors for granting, updating, listing, and revoking organization permissions.

## Requirements
### Requirement: List organization permissions
The system SHALL return organization permissions from `GET /api/v1/organization-permissions` and MAY filter by `organizationId` or `userId`.

#### Scenario: List permissions for a user
- **WHEN** a client requests `GET /api/v1/organization-permissions?userId=10`
- **THEN** the system responds with `200` and only permissions for that user.

### Requirement: Fetch permission by ID
The system SHALL return a permission from `GET /api/v1/organization-permissions/{id}`.

#### Scenario: Permission found
- **WHEN** a client requests `GET /api/v1/organization-permissions/{id}` for an existing permission
- **THEN** the system responds with `200` and the `OrganizationPermissionDTO`.

### Requirement: Grant permission
The system SHALL grant permissions via `POST /api/v1/organization-permissions/organizations/{organizationId}/grant`.

#### Scenario: Grant permission
- **WHEN** a client posts a valid `GrantPermissionRequest` for an organization
- **THEN** the system responds with `201` and the created permission.

### Requirement: Update permission
The system SHALL update permissions via `PUT /api/v1/organization-permissions/{id}`.

#### Scenario: Update permission
- **WHEN** a client submits `UpdatePermissionRequest` for an existing permission
- **THEN** the system responds with `200` and the updated permission.

### Requirement: Revoke permission
The system SHALL revoke permissions via `DELETE /api/v1/organization-permissions/{id}` or
`DELETE /api/v1/organization-permissions/organizations/{organizationId}/users/{userId}`.

#### Scenario: Revoke by organization and user
- **WHEN** a client deletes `/api/v1/organization-permissions/organizations/{organizationId}/users/{userId}`
- **THEN** the system responds with `204`.
