# Project Context

## Purpose
Kala API is a document management system REST API for the Kala platform.

## Tech Stack
- Kotlin
- Quarkus 3.17.4
- Hibernate Panache
- Gradle
- Java 21
- PostgreSQL

## Project Conventions

### Code Style
Follow existing Kotlin conventions and package layout. Prefer clear, explicit names that match the domain.

### Architecture Patterns
Layered architecture with strict separation of concerns:
Client -> Resource (DTO) -> Service (Domain) -> Repository (Domain <-> Entity) -> Database.
Dependencies flow inward: API -> Service -> Repository -> Domain.
Domain models are framework-agnostic and immutable.

### Testing Strategy
Run tests with `./gradlew test`.

### Git Workflow
Not specified.

## Domain Context
Document management system with REST APIs. Use DTOs for API contracts and domain models for business logic.

## Important Constraints
- Java 21 is required.
- API errors must use RFC 9457 Problem Details with content type `application/problem+json`.
- Required error fields: `type`, `title`, `status` (include `detail` and `instance` when possible).

## External Dependencies
- PostgreSQL on port 5444 (db/user/password: `kala`).
- OpenAPI spec served at `http://localhost:5051/openapi` and Swagger UI at `http://localhost:5051/swagger-ui`.
