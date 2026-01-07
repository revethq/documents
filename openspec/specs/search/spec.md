# Search

## Purpose
Define full-text search behaviors across documents, projects, and organizations.

## Requirements
### Requirement: Search across all entities
The system SHALL perform full-text search from `GET /api/v1/search` using a required `q` parameter.

#### Scenario: Search all entities
- **WHEN** a client requests `GET /api/v1/search?q=fire` with a query
- **THEN** the system responds with `200` and a `SearchResultsDTO`.

### Requirement: Search documents
The system SHALL search documents from `GET /api/v1/search/documents` using a required `q` parameter.

#### Scenario: Search documents
- **WHEN** a client requests `GET /api/v1/search/documents?q=plan`
- **THEN** the system responds with `200` and a list of `DocumentDTO`.

### Requirement: Search projects
The system SHALL search projects from `GET /api/v1/search/projects` using a required `q` parameter.

#### Scenario: Search projects
- **WHEN** a client requests `GET /api/v1/search/projects?q=training`
- **THEN** the system responds with `200` and a list of `ProjectDTO`.

### Requirement: Search organizations
The system SHALL search organizations from `GET /api/v1/search/organizations` using a required `q` parameter.

#### Scenario: Search organizations
- **WHEN** a client requests `GET /api/v1/search/organizations?q=ndptc`
- **THEN** the system responds with `200` and a list of `OrganizationDTO`.
