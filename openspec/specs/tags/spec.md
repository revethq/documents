# Tags

## Purpose
Define API behaviors for managing tags, including lookup by ID or slug.

## Requirements
### Requirement: List tags
The system SHALL return tags from `GET /api/v1/tags`.

#### Scenario: List tags
- **WHEN** a client requests `GET /api/v1/tags`
- **THEN** the system responds with `200` and a list of `TagDTO`.

### Requirement: Fetch tags by ID or slug
The system SHALL return tags from `GET /api/v1/tags/{id}` and `GET /api/v1/tags/slug/{slug}`.

#### Scenario: Tag found by slug
- **WHEN** a client requests `GET /api/v1/tags/slug/{slug}` for an existing tag
- **THEN** the system responds with `200` and the `TagDTO`.

### Requirement: Create tag
The system SHALL create tags from `POST /api/v1/tags` and return the created `TagDTO`.

#### Scenario: Create tag
- **WHEN** a client posts a valid `CreateTagRequest`
- **THEN** the system responds with `201` and the created tag.

### Requirement: Update tag
The system SHALL update tags from `PUT /api/v1/tags/{id}`.

#### Scenario: Update tag
- **WHEN** a client submits `UpdateTagRequest` for an existing tag
- **THEN** the system responds with `200` and the updated tag.

### Requirement: Delete tag
The system SHALL delete tags from `DELETE /api/v1/tags/{id}`.

#### Scenario: Delete tag
- **WHEN** a client deletes an existing tag
- **THEN** the system responds with `204`.
