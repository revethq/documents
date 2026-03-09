# Project Context

## Purpose

Revet Documents is a document management system REST API. It provides endpoints for managing documents, projects, organizations, categories, tags, and file storage (upload/download via presigned URLs). The API supports multi-provider cloud storage (S3, GCS, MinIO) and integrates with Revet IAM for authentication and fine-grained permission-based authorization.

## Tech Stack

- **Language**: Kotlin 2.3.10 (JVM target: Java 25)
- **Framework**: Quarkus 3.31.1
- **Build Tool**: Gradle 9.3.1 (Kotlin DSL)
- **ORM**: Hibernate Panache Kotlin
- **Database**: PostgreSQL
- **REST**: JAX-RS (quarkus-rest) with Jackson serialization
- **Authentication**: SmallRye JWT (OIDC/JWT verification)
- **Authorization**: Revet IAM (revet-user, revet-permission, revet-scim) v0.1.15
- **Storage**: AWS S3, Google Cloud Storage, MinIO (via presigned URLs)
- **API Docs**: SmallRye OpenAPI / Swagger UI
- **Testing**: JUnit 5, REST Assured, MockK
- **Containerization**: Docker (UBI9 OpenJDK 25), Docker Compose

## Project Conventions

### Code Style

**Naming conventions:**

| Element | Pattern | Example |
|---------|---------|---------|
| Domain classes | PascalCase, singular | `Document`, `Organization` |
| Entity classes | PascalCase + `Entity` suffix | `DocumentEntity` |
| Service interfaces | PascalCase + `Service` | `DocumentService` |
| Service implementations | PascalCase + `ServiceImpl` | `DocumentServiceImpl` |
| Repository interfaces | PascalCase + `Repository` | `DocumentRepository` |
| Repository implementations | PascalCase + `RepositoryImpl` | `DocumentRepositoryImpl` |
| REST resources | PascalCase + `Resource` | `DocumentResource` |
| DTOs | PascalCase + `DTO` | `DocumentDTO` |
| Request DTOs | `Create`/`Update` + Entity + `Request` | `CreateDocumentRequest` |
| Domain-layer mappers | PascalCase + `Mapper` (object) | `DocumentMapper` |
| DTO-layer mappers | PascalCase + `DTOMapper` (object) | `DocumentDTOMapper` |
| REST paths | `/api/v1/lowercase-plural` | `/api/v1/documents` |
| Packages | lowercase | `com.revethq.documents.domain` |

**Kotlin idioms used throughout:**
- Immutable `data class` models with `copy()` for updates
- `companion object` factory methods (`create()`)
- `object` singletons for stateless mappers
- `apply {}` scoped functions for entity construction
- Null-safe navigation (`?.`, `?:`, `let {}`)
- Extension functions for framework conversions (e.g., `Sort.toPanacheSort()`)
- Constructor injection via `@Inject constructor(...)`

### Architecture Patterns

Layered architecture with strict separation of concerns:

```
Client → Resource (DTO) → Service (Domain) → Repository (Domain ↔ Entity) → Database
```

| Layer | Package | Responsibility |
|-------|---------|----------------|
| API | `api/resource/` | JAX-RS endpoints, HTTP concerns, OpenAPI annotations |
| DTO Mapper | `api/mapper/` | Domain ↔ DTO conversion (object singletons) |
| Service | `service/` | Business logic, validation, `@Transactional` boundaries |
| Repository | `repository/` | Data access abstraction (interface + impl) |
| Entity Mapper | `repository/mapper/` | Domain ↔ Entity conversion (object singletons) |
| Entity | `repository/entity/` | Panache entities (`@Entity`, `PanacheEntity`) |
| Domain | `domain/` | Core models, framework-agnostic, immutable |
| DTO | `dto/` | API request/response objects |

**Key principles:**
- Domain models have zero framework dependencies
- Dependencies flow inward: API → Service → Repository → Domain
- All layers use interface + `@ApplicationScoped` implementation pairs
- Pagination uses an overfetch pattern (`size + 1`) to determine `hasMore`, avoiding expensive `COUNT` queries
- Errors follow RFC 9457 Problem Details (`application/problem+json`)
- Storage uses a factory pattern to abstract multiple cloud providers behind a common `StorageProviderClient` interface
- Authorization uses Revet IAM's `@RequiresPermission` annotation with action constants and URN-based resource identifiers

### Testing Strategy

Testing infrastructure is configured but tests are not yet written. The intended stack:
- **Unit tests**: JUnit 5 + MockK for service/repository logic
- **Integration tests**: Quarkus `@QuarkusTest` for full-stack validation
- **API tests**: REST Assured for endpoint testing
- Run with: `./gradlew test`

### Git Workflow

- Main branch: `main`
- Feature branches merged via pull requests

## Domain Context

The system manages **documents** within a hierarchy:

- **Organizations** own **Projects** and have contact info, locale, and timezone settings
- **Projects** belong to an Organization and group related Documents; they track client UUIDs
- **Documents** belong to a Project, optionally have a Category, and can be tagged
- **Document Versions** track file revisions for a Document, with storage keys and upload status
- **Categories** provide classification within a Project
- **Tags** are cross-cutting labels applied to documents via a `TaggedItem` join entity
- **Buckets** configure cloud storage (provider type, bucket name, region, encryption settings)

File storage uses **presigned URLs** — the API generates time-limited upload/download URLs and the client interacts directly with the storage provider.

**Permission model**: Actions follow a `service:ActionName` format (e.g., `documents:ListDocuments`). Resources are identified by URNs: `urn:revet:documents:{tenantId}:{resourceType}/{resourceId}`.

## Important Constraints

- Java 25 required (language features and runtime)
- Domain models must remain framework-agnostic (no Jakarta/Quarkus annotations)
- All API errors must use RFC 9457 Problem Details format
- API versioning is path-based (`/api/v1/...`)
- Pagination max page size is 100
- The `allopen` Kotlin compiler plugin is configured for `@Path`, `@ApplicationScoped`, `@Entity`, and `@Provider` annotations

## External Dependencies

- **PostgreSQL**: Primary database (port 5432 in containers, configurable)
- **MinIO / AWS S3**: Object storage for document files (S3-compatible API)
- **Google Cloud Storage**: Alternative storage provider
- **Revet IAM**: Identity and access management (user provisioning, permission evaluation)
- **OIDC Provider**: JWT token verification for authentication (issuer configured via `mp.jwt.verify.issuer`)
- **Next.js webapp**: Frontend client (runs on port 3000, CORS configured)
