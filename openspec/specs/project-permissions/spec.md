# Project Permissions

## Purpose
Define API behaviors for granting, updating, listing, and revoking project permissions.

## Requirements
### Requirement: List project permissions
The system SHALL return project permissions from `GET /api/v1/project-permissions` and MAY filter by `projectId` or `userId`.

#### Scenario: List permissions for a project
- **WHEN** a client requests `GET /api/v1/project-permissions?projectId=77`
- **THEN** the system responds with `200` and only permissions for that project.

### Requirement: Fetch permission by ID
The system SHALL return a permission from `GET /api/v1/project-permissions/{id}`.

#### Scenario: Permission found
- **WHEN** a client requests `GET /api/v1/project-permissions/{id}` for an existing permission
- **THEN** the system responds with `200` and the `ProjectPermissionDTO`.

### Requirement: Grant permission
The system SHALL grant permissions via `POST /api/v1/project-permissions/projects/{projectId}/grant`.

#### Scenario: Grant permission
- **WHEN** a client posts a valid `GrantPermissionRequest` for a project
- **THEN** the system responds with `201` and the created permission.

### Requirement: Update permission
The system SHALL update permissions via `PUT /api/v1/project-permissions/{id}`.

#### Scenario: Update permission
- **WHEN** a client submits `UpdatePermissionRequest` for an existing permission
- **THEN** the system responds with `200` and the updated permission.

### Requirement: Revoke permission
The system SHALL revoke permissions via `DELETE /api/v1/project-permissions/{id}` or
`DELETE /api/v1/project-permissions/projects/{projectId}/users/{userId}`.

#### Scenario: Revoke by project and user
- **WHEN** a client deletes `/api/v1/project-permissions/projects/{projectId}/users/{userId}`
- **THEN** the system responds with `204`.
