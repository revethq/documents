# Projects

## Purpose
Define the API behaviors for managing projects, clients, and project tags.

## Requirements
### Requirement: List projects
The system SHALL return a list of projects from `GET /api/v1/projects`, defaulting to active projects unless `includeInactive=true`, and MAY filter by `organizationId`.

#### Scenario: List projects for an organization
- **WHEN** a client requests `GET /api/v1/projects?organizationId=123`
- **THEN** the system responds with `200` and only projects for that organization.

### Requirement: Fetch project by UUID
The system SHALL return a project from `GET /api/v1/projects/{uuid}` when the caller has `CAN_INVITE` for the project.

#### Scenario: Project found
- **WHEN** a client requests `GET /api/v1/projects/{uuid}` for an existing project
- **THEN** the system responds with `200` and the `ProjectDTO`.

### Requirement: Create project
The system SHALL create projects from `POST /api/v1/projects` and return the created `ProjectDTO`.

#### Scenario: Create project
- **WHEN** a client posts a valid `CreateProjectRequest`
- **THEN** the system responds with `201` and the created project.

### Requirement: Update project
The system SHALL update projects from `PUT /api/v1/projects/{uuid}` when the caller has `CAN_CREATE`.

#### Scenario: Update project
- **WHEN** a client submits `UpdateProjectRequest` for an existing project
- **THEN** the system responds with `200` and the updated project.

### Requirement: Manage project clients
The system SHALL add or remove project clients via
`POST /api/v1/projects/{uuid}/clients` and `DELETE /api/v1/projects/{uuid}/clients/{clientId}` when the caller has `CAN_MANAGE`.

#### Scenario: Add client
- **WHEN** a client posts `AddClientRequest` to `/api/v1/projects/{uuid}/clients`
- **THEN** the system responds with `200` and the updated project.

### Requirement: Manage project tags
The system SHALL add or remove project tags via
`POST /api/v1/projects/{uuid}/tags` and `DELETE /api/v1/projects/{uuid}/tags/{tag}` when the caller has `CAN_CREATE`.

#### Scenario: Remove tag
- **WHEN** a client deletes `/api/v1/projects/{uuid}/tags/{tag}`
- **THEN** the system responds with `200` and the updated project.

### Requirement: Soft delete project
The system SHALL soft-delete projects from `DELETE /api/v1/projects/{uuid}` when the caller has `CAN_MANAGE`.

#### Scenario: Delete project
- **WHEN** a client deletes an existing project
- **THEN** the system responds with `204`.
