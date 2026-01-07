# Organizations

## Purpose
Define organization management behaviors for the API.

## Requirements
### Requirement: List organizations
The system SHALL return a list of organizations from `GET /api/v1/organizations` and map domain models to `OrganizationDTO`.

#### Scenario: List active organizations
- **WHEN** a client requests `GET /api/v1/organizations` without `includeInactive`
- **THEN** the system responds with `200` and only active organizations.

### Requirement: Fetch organization by UUID
The system SHALL return a single organization from `GET /api/v1/organizations/{uuid}` when the caller has `CAN_INVITE` for the organization.

#### Scenario: Organization found
- **WHEN** a client requests `GET /api/v1/organizations/{uuid}` for an existing organization
- **THEN** the system responds with `200` and the organization `OrganizationDTO`.

### Requirement: Create organization
The system SHALL create organizations from `POST /api/v1/organizations` and return the created `OrganizationDTO`.

#### Scenario: Create organization
- **WHEN** a client posts a valid `CreateOrganizationRequest`
- **THEN** the system responds with `201` and the created organization.

### Requirement: Update organization
The system SHALL update organizations from `PUT /api/v1/organizations/{uuid}` when the caller has `CAN_CREATE`.

#### Scenario: Update organization
- **WHEN** a client submits `UpdateOrganizationRequest` for an existing organization
- **THEN** the system responds with `200` and the updated organization.

### Requirement: Soft delete organization
The system SHALL soft-delete organizations from `DELETE /api/v1/organizations/{uuid}` when the caller has `CAN_MANAGE`.

#### Scenario: Delete organization
- **WHEN** a client deletes an existing organization
- **THEN** the system responds with `204`.
