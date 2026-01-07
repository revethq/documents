# Categories

## Purpose
Define API behaviors for managing document categories and filtering by project.

## Requirements
### Requirement: List categories
The system SHALL return categories from `GET /api/v1/categories` and MAY filter by `projectId`.

#### Scenario: List categories for a project
- **WHEN** a client requests `GET /api/v1/categories?projectId=42`
- **THEN** the system responds with `200` and only categories for that project.

### Requirement: Fetch category by ID
The system SHALL return a category from `GET /api/v1/categories/{id}`.

#### Scenario: Category found
- **WHEN** a client requests `GET /api/v1/categories/{id}` for an existing category
- **THEN** the system responds with `200` and the `CategoryDTO`.

### Requirement: Create category
The system SHALL create categories from `POST /api/v1/categories` and return the created `CategoryDTO`.

#### Scenario: Create category
- **WHEN** a client posts a valid `CreateCategoryRequest`
- **THEN** the system responds with `201` and the created category.

### Requirement: Update category
The system SHALL update categories from `PUT /api/v1/categories/{id}`.

#### Scenario: Update category
- **WHEN** a client submits `UpdateCategoryRequest` for an existing category
- **THEN** the system responds with `200` and the updated category.

### Requirement: Delete category
The system SHALL delete categories from `DELETE /api/v1/categories/{id}`.

#### Scenario: Delete category
- **WHEN** a client deletes an existing category
- **THEN** the system responds with `204`.
