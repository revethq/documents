<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Revet Documents is a document management system REST API built with Kotlin 2.3.10, Quarkus 3.31.1, and Hibernate Panache. It requires Java 25.

The project is organized as a multi-module Gradle build with three submodules:

- **`core`** - Domain models, repository interfaces, and service interfaces (no framework dependencies)
- **`persistence-runtime`** - Panache entities, entity mappers, repository implementations, service implementations, and permission CDI beans (depends on `core`). Usable as a standalone library by other Revet services without pulling in the REST API.
- **`web`** - REST resources, DTOs, DTO mappers, security filters, and application configuration (depends on `core` and `persistence-runtime`)

## Common Commands

```bash
# Start development server (hot-reload enabled)
./gradlew :web:quarkusDev

# Run tests (all modules)
./gradlew test

# Build for production
./gradlew build

# Run production build
java -jar web/build/quarkus-app/quarkus-run.jar

# Native build (requires GraalVM)
./gradlew build -Dquarkus.package.type=native
```

## Dependencies

- **PostgreSQL**: Run on port 5432 with database/user/password all set to `revet`
- **MinIO/S3**: Run on port 9000 for document file storage (credentials: minioadmin/minioadmin)

## Architecture

This project uses a layered architecture with strict separation of concerns. Data flows:

```
Client → Resource (DTO) → Service (Domain) → Repository (Domain ↔ Entity) → Database
```

### Layer Responsibilities

| Layer | Module | Package | Purpose |
|-------|--------|---------|---------|
| API | `web` | `api/resource/` | JAX-RS REST endpoints, HTTP handling |
| DTO Mapper | `web` | `api/mapper/` | Converts Domain ↔ DTO |
| Service Interface | `core` | `service/` | Service contracts |
| Service Impl | `persistence-runtime` | `service/` | Business logic, validation, transaction boundaries |
| Repository Interface | `core` | `repository/` | Data access contracts |
| Repository Impl | `persistence-runtime` | `repository/` | Data access implementation |
| Entity Mapper | `persistence-runtime` | `repository/mapper/` | Converts Domain ↔ Entity |
| Entity | `persistence-runtime` | `repository/entity/` | Panache entities (persistence) |
| Domain | `core` | `domain/` | Core business models (framework-agnostic, immutable) |
| DTO | `web` | `dto/` | API request/response objects |

### Key Principle

Domain models have no framework dependencies. Dependencies flow inward: API → Service → Repository → Domain.

## Adding a New Entity

Follow this order when adding a new entity (e.g., `Widget`):

**`core` module:**
1. `domain/Widget.kt` - Domain model with `create()` companion and `update()` methods
2. `repository/WidgetRepository.kt` - Repository interface
3. `service/WidgetService.kt` - Service interface

**`persistence-runtime` module:**
4. `repository/entity/WidgetEntity.kt` - Panache entity with `@Entity`
5. `repository/mapper/WidgetMapper.kt` - Object with `toDomain()` and `toEntity()`
6. `repository/WidgetRepositoryImpl.kt` - `@ApplicationScoped` repository implementation
7. `service/WidgetServiceImpl.kt` - `@ApplicationScoped` service implementation with business logic

**`web` module:**
8. `dto/WidgetDTO.kt` - DTOs for API contracts
9. `api/mapper/WidgetDTOMapper.kt` - Object with `toDTO()` method
10. `api/resource/WidgetResource.kt` - `@Path("/api/v1/widgets")` JAX-RS resource

## Error Handling

All API errors must use **RFC 9457 Problem Details** format with content type `application/problem+json`.

```json
{
  "type": "https://docs.revethq.com/problems/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Document with ID 123 was not found",
  "instance": "/api/v1/documents/123"
}
```

Required fields: `type`, `title`, `status`. Include `detail` for specific error context and `instance` for the request path.

## API Documentation

- Swagger UI: http://localhost:5051/swagger-ui
- OpenAPI spec: http://localhost:5051/openapi
