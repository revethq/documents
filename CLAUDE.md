# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kala API is a document management system REST API built with Kotlin, Quarkus 3.17.4, and Hibernate Panache. It requires Java 21.

## Common Commands

```bash
# Start development server (hot-reload enabled)
./gradlew quarkusDev

# Run tests
./gradlew test

# Build for production
./gradlew build

# Run production build
java -jar build/quarkus-app/quarkus-run.jar

# Native build (requires GraalVM)
./gradlew build -Dquarkus.package.type=native
```

## Dependencies

- **PostgreSQL**: Run on port 5444 with database/user/password all set to `kala`
- **MinIO/S3**: Run on port 9000 for document file storage (credentials: minioadmin/minioadmin)

## Architecture

This project uses a layered architecture with strict separation of concerns. Data flows:

```
Client → Resource (DTO) → Service (Domain) → Repository (Domain ↔ Entity) → Database
```

### Layer Responsibilities

| Layer | Package | Purpose |
|-------|---------|---------|
| API | `api/resource/` | JAX-RS REST endpoints, HTTP handling |
| DTO Mapper | `api/mapper/` | Converts Domain ↔ DTO |
| Service | `service/` | Business logic, validation, transaction boundaries |
| Repository | `repository/` | Data access abstraction |
| Entity Mapper | `repository/mapper/` | Converts Domain ↔ Entity |
| Entity | `repository/entity/` | Panache entities (persistence) |
| Domain | `domain/` | Core business models (framework-agnostic, immutable) |
| DTO | `dto/` | API request/response objects |

### Key Principle

Domain models have no framework dependencies. Dependencies flow inward: API → Service → Repository → Domain.

## Adding a New Entity

Follow this order when adding a new entity (e.g., `Widget`):

1. `domain/Widget.kt` - Domain model with `create()` companion and `update()` methods
2. `repository/entity/WidgetEntity.kt` - Panache entity with `@Entity`
3. `repository/mapper/WidgetMapper.kt` - Object with `toDomain()` and `toEntity()`
4. `repository/WidgetRepository.kt` - Interface + `@ApplicationScoped` implementation
5. `service/WidgetService.kt` - Interface + implementation with business logic
6. `dto/WidgetDTO.kt` - DTOs for API contracts
7. `api/mapper/WidgetDTOMapper.kt` - Object with `toDTO()` method
8. `api/resource/WidgetResource.kt` - `@Path("/api/v1/widgets")` JAX-RS resource

## Error Handling

All API errors must use **RFC 9457 Problem Details** format with content type `application/problem+json`.

```json
{
  "type": "https://kala.ndptc.com/problems/not-found",
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
