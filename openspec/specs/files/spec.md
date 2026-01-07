# Files

## Purpose
Define file upload and download behaviors tied to document versions and storage.

## Requirements
### Requirement: Fetch download URL
The system SHALL return a download URL from `GET /api/v1/files/download/{documentVersionUuid}` when the document version exists and has a URL.

#### Scenario: Download URL available
- **WHEN** a client requests a valid document version UUID with a stored URL
- **THEN** the system responds with `200` and a `DownloadResponse`.

### Requirement: Initiate upload
The system SHALL create a pending document version and return a presigned upload URL from `POST /api/v1/files/initiate-upload`.

#### Scenario: Initiate upload
- **WHEN** a client posts a valid `InitiateUploadRequest` for an existing document with storage configured
- **THEN** the system responds with `201` and an `InitiateUploadResponse` containing an upload URL.

### Requirement: Create version with URL
The system SHALL create document versions with a provided URL via `POST /api/v1/files/create-version`.

#### Scenario: Create version with URL
- **WHEN** a client posts a valid `CreateDocumentVersionRequest`
- **THEN** the system responds with `201` and the created `DocumentVersionDTO`.
