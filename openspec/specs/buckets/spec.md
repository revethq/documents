# Buckets

## Purpose
Define storage bucket configuration behaviors used by organizations and file storage.

## Requirements
### Requirement: List buckets
The system SHALL return bucket configurations from `GET /api/v1/buckets`, defaulting to active buckets unless `includeInactive=true`.

#### Scenario: List active buckets
- **WHEN** a client requests `GET /api/v1/buckets` without `includeInactive`
- **THEN** the system responds with `200` and only active buckets.

### Requirement: Fetch bucket by UUID
The system SHALL return a bucket from `GET /api/v1/buckets/{uuid}`.

#### Scenario: Bucket found
- **WHEN** a client requests `GET /api/v1/buckets/{uuid}` for an existing bucket
- **THEN** the system responds with `200` and the `BucketDTO`.

### Requirement: Create bucket
The system SHALL create bucket configurations from `POST /api/v1/buckets` and return the created `BucketDTO`.

#### Scenario: Create bucket
- **WHEN** a client posts a valid `CreateBucketRequest`
- **THEN** the system responds with `201` and the created bucket.

### Requirement: Update bucket
The system SHALL update buckets from `PUT /api/v1/buckets/{uuid}`.

#### Scenario: Update bucket
- **WHEN** a client submits `UpdateBucketRequest` for an existing bucket
- **THEN** the system responds with `200` and the updated bucket.

### Requirement: Soft delete bucket
The system SHALL soft-delete buckets from `DELETE /api/v1/buckets/{uuid}`.

#### Scenario: Delete bucket
- **WHEN** a client deletes an existing bucket
- **THEN** the system responds with `204`.
