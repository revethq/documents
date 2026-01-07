# Documents

## Purpose
Define document management behaviors, including listing, updates, and downloads.

## Requirements
### Requirement: List documents with pagination and filters
The system SHALL return a paginated list of documents from `GET /api/v1/documents` with optional filters (`name`, `projectId`, `categoryId`, `tagIds`, `organizationIds`) and sorting (`sort`, `direction`).

#### Scenario: Paginated list with defaults
- **WHEN** a client requests `GET /api/v1/documents` with no pagination parameters
- **THEN** the system uses `page=0`, `size=20`, and responds with `200` and a `PageDTO` of documents.

### Requirement: Validate sort field
The system SHALL reject invalid sort fields (allowed: `name`, `date`, `mime`, `id`) with a `400` Problem Details response.

#### Scenario: Invalid sort field
- **WHEN** a client requests `GET /api/v1/documents?sort=owner`
- **THEN** the system responds with `400` and `application/problem+json`.

### Requirement: Fetch document by UUID
The system SHALL return a document from `GET /api/v1/documents/{uuid}` when the caller has `CAN_INVITE` for the document.

#### Scenario: Document found
- **WHEN** a client requests `GET /api/v1/documents/{uuid}` for an existing document
- **THEN** the system responds with `200` and the `DocumentDTO`.

### Requirement: Generate download URL for latest version
The system SHALL return a presigned download URL from `GET /api/v1/documents/{uuid}/download` when the latest version exists and storage is configured.

#### Scenario: Download latest version
- **WHEN** a client requests `GET /api/v1/documents/{uuid}/download` for a document with a latest version and storage key
- **THEN** the system responds with `200` and a `PresignedDownloadResponse`.

### Requirement: Create document
The system SHALL create documents from `POST /api/v1/documents` and return the created `DocumentDTO`.

#### Scenario: Create document
- **WHEN** a client posts a valid `CreateDocumentRequest`
- **THEN** the system responds with `201` and the created document.

### Requirement: Update document
The system SHALL update documents from `PUT /api/v1/documents/{uuid}` when the caller has `CAN_CREATE`.

#### Scenario: Update document
- **WHEN** a client submits `UpdateDocumentRequest` for an existing document
- **THEN** the system responds with `200` and the updated document.

### Requirement: Manage document tags
The system SHALL add or remove document tags via
`POST /api/v1/documents/{uuid}/tags` and `DELETE /api/v1/documents/{uuid}/tags/{tag}` when the caller has `CAN_CREATE`.

#### Scenario: Add tag
- **WHEN** a client posts `AddTagRequest` to `/api/v1/documents/{uuid}/tags`
- **THEN** the system responds with `200` and the updated document.

### Requirement: Soft delete document
The system SHALL soft-delete documents from `DELETE /api/v1/documents/{uuid}` when the caller has `CAN_MANAGE`.

#### Scenario: Delete document
- **WHEN** a client deletes an existing document
- **THEN** the system responds with `204`.
