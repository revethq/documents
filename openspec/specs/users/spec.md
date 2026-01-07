# Users

## Purpose
Define the API behaviors for creating, retrieving, updating, and deleting users.

## Requirements
### Requirement: List users
The system SHALL return a list of users from `GET /api/v1/users`, defaulting to active users unless `includeInactive=true`.

#### Scenario: List active users
- **WHEN** a client requests `GET /api/v1/users` without `includeInactive`
- **THEN** the system responds with `200` and only active users.

### Requirement: Fetch users by identifiers
The system SHALL fetch users by UUID, username, or email from:
`GET /api/v1/users/{uuid}`, `GET /api/v1/users/username/{username}`, and `GET /api/v1/users/email/{email}`.

#### Scenario: User found by username
- **WHEN** a client requests `GET /api/v1/users/username/{username}` for an existing user
- **THEN** the system responds with `200` and the user `UserDTO`.

### Requirement: Create user
The system SHALL create users from `POST /api/v1/users` and return the created `UserDTO`.

#### Scenario: Create user
- **WHEN** a client posts a valid `CreateUserRequest`
- **THEN** the system responds with `201` and the created user.

### Requirement: Update user
The system SHALL update users from `PUT /api/v1/users/{uuid}` and return the updated `UserDTO`.

#### Scenario: Update user
- **WHEN** a client submits `UpdateUserRequest` for an existing user
- **THEN** the system responds with `200` and the updated user.

### Requirement: Soft delete user
The system SHALL soft-delete users from `DELETE /api/v1/users/{uuid}`.

#### Scenario: Delete user
- **WHEN** a client deletes an existing user
- **THEN** the system responds with `204`.
