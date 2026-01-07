# Document Permissions

## Purpose
Define API behaviors for granting, updating, listing, and revoking document permissions.

## Requirements
### Requirement: List document permissions
The system SHALL return document permissions from `GET /api/v1/document-permissions` and MAY filter by `documentId` or `userId`.

#### Scenario: List permissions for a document
- **WHEN** a client requests `GET /api/v1/document-permissions?documentId=55`
- **THEN** the system responds with `200` and only permissions for that document.

### Requirement: Fetch permission by ID
The system SHALL return a permission from `GET /api/v1/document-permissions/{id}`.

#### Scenario: Permission found
- **WHEN** a client requests `GET /api/v1/document-permissions/{id}` for an existing permission
- **THEN** the system responds with `200` and the `DocumentPermissionDTO`.

### Requirement: Grant permission
The system SHALL grant permissions via `POST /api/v1/document-permissions/documents/{documentId}/grant`.

#### Scenario: Grant permission
- **WHEN** a client posts a valid `GrantPermissionRequest` for a document
- **THEN** the system responds with `201` and the created permission.

### Requirement: Update permission
The system SHALL update permissions via `PUT /api/v1/document-permissions/{id}`.

#### Scenario: Update permission
- **WHEN** a client submits `UpdatePermissionRequest` for an existing permission
- **THEN** the system responds with `200` and the updated permission.

### Requirement: Revoke permission
The system SHALL revoke permissions via `DELETE /api/v1/document-permissions/{id}` or
`DELETE /api/v1/document-permissions/documents/{documentId}/users/{userId}`.

#### Scenario: Revoke by document and user
- **WHEN** a client deletes `/api/v1/document-permissions/documents/{documentId}/users/{userId}`
- **THEN** the system responds with `204`.
