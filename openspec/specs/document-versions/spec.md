# Document Versions

## Purpose
Define document version behaviors, including upload lifecycle and retrieval.

## Requirements
### Requirement: List document versions
The system SHALL return a list of document versions from `GET /api/v1/document-versions` and MAY filter by `documentId`.

#### Scenario: List versions for a document
- **WHEN** a client requests `GET /api/v1/document-versions?documentId=123`
- **THEN** the system responds with `200` and only versions for that document.

### Requirement: Fetch version by UUID
The system SHALL return a version from `GET /api/v1/document-versions/{uuid}` when the caller has `CAN_INVITE` via the document, and MAY include a presigned download URL when the version is downloadable.

#### Scenario: Version found
- **WHEN** a client requests `GET /api/v1/document-versions/{uuid}` for an existing version
- **THEN** the system responds with `200` and the `DocumentVersionDTO`.

### Requirement: Fetch latest version by document UUID
The system SHALL return the latest version from `GET /api/v1/document-versions/document/{uuid}/latest` when the caller has `CAN_INVITE`.

#### Scenario: Latest version found
- **WHEN** a client requests `GET /api/v1/document-versions/document/{uuid}/latest` for a document with versions
- **THEN** the system responds with `200` and the latest `DocumentVersionDTO`.

### Requirement: Create document version
The system SHALL create versions from `POST /api/v1/document-versions` and return the created `DocumentVersionDTO`.

#### Scenario: Create version
- **WHEN** a client posts a valid `CreateDocumentVersionRequest`
- **THEN** the system responds with `201` and the created version.

### Requirement: Update document version
The system SHALL update versions from `PUT /api/v1/document-versions/{uuid}` when the caller has `CAN_CREATE` via the document.

#### Scenario: Update version
- **WHEN** a client submits `UpdateDocumentVersionRequest` for an existing version
- **THEN** the system responds with `200` and the updated version.

### Requirement: Complete upload
The system SHALL complete a pending upload via `PUT /api/v1/document-versions/{uuid}/complete-upload` when the caller has `CAN_CREATE` via the document, and only if the file exists in storage.

#### Scenario: Complete upload
- **WHEN** a client requests completion for a `PENDING` version and the file exists in storage
- **THEN** the system responds with `200` and the version marked `COMPLETED`.

### Requirement: Delete document version
The system SHALL delete versions from `DELETE /api/v1/document-versions/{uuid}` when the caller has `CAN_MANAGE` via the document.

#### Scenario: Delete version
- **WHEN** a client deletes an existing version
- **THEN** the system responds with `204`.
