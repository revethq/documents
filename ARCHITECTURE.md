# Kala API Architecture

## Overview

The Kala API is built using a **layered architecture** with clear separation of concerns. The architecture follows Domain-Driven Design (DDD) principles with distinct layers for API, Domain, Service, and Repository.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    API/Resource Layer                    │
│              (JAX-RS REST Endpoints)                     │
│         - OrganizationResource                          │
│         - Maps DTOs ↔ Domain                            │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ↓ Uses
┌─────────────────────────────────────────────────────────┐
│                    Service Layer                         │
│              (Business Logic)                            │
│         - OrganizationService                           │
│         - Validates business rules                      │
│         - Coordinates operations                        │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ↓ Uses
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│           (Data Access Abstraction)                      │
│         - OrganizationRepository                        │
│         - Maps Domain ↔ Entity                          │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ↓ Uses
┌─────────────────────────────────────────────────────────┐
│                  Persistence Layer                       │
│              (Panache Entities)                          │
│         - OrganizationEntity                            │
│         - Hibernate ORM                                 │
└─────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Domain Layer (`com.ndptc.kala.domain`)

**Purpose**: Contains pure business logic and domain models, independent of frameworks and infrastructure.

**Characteristics**:
- No dependencies on Quarkus, Panache, or JAX-RS
- Immutable data models (Kotlin data classes)
- Business logic encapsulated in domain methods
- Framework-agnostic

**Example**:
```kotlin
// Domain model
data class Organization(
    val id: Long?,
    val uuid: UUID,
    val name: String,
    val contactInfo: ContactInfo,
    val isActive: Boolean,
    val timestamps: Timestamps
) {
    fun update(...): Organization { ... }
    fun deactivate(): Organization { ... }
}
```

**Files**:
- `domain/Organization.kt` - Core domain model with business methods

### 2. Repository Layer (`com.ndptc.kala.repository`)

**Purpose**: Abstracts data access and persistence, converting between Domain models and Persistence entities.

**Characteristics**:
- Interface-based design for testability
- Panache-based implementation for database operations
- Converts Entity ↔ Domain models
- Handles transactional operations

**Example**:
```kotlin
// Repository interface
interface OrganizationRepository {
    fun findAll(includeInactive: Boolean = false): List<Organization>
    fun save(organization: Organization): Organization
}

// Panache-based implementation
@ApplicationScoped
class OrganizationRepositoryImpl : OrganizationRepository {
    override fun findAll(includeInactive: Boolean): List<Organization> {
        val entities = OrganizationEntity.list(...)
        return entities.map { OrganizationMapper.toDomain(it) }
    }
}
```

**Files**:
- `repository/OrganizationRepository.kt` - Repository interface and implementation
- `repository/entity/OrganizationEntity.kt` - Panache entity for persistence
- `repository/mapper/OrganizationMapper.kt` - Maps Entity ↔ Domain

### 3. Service Layer (`com.ndptc.kala.service`)

**Purpose**: Contains business logic, orchestrates operations, and enforces business rules.

**Characteristics**:
- Interface-based for testability
- Validates business rules
- Coordinates repository operations
- Transaction boundaries defined here
- Works exclusively with Domain models

**Example**:
```kotlin
interface OrganizationService {
    fun createOrganization(...): Organization
    fun updateOrganization(...): Organization?
}

@ApplicationScoped
class OrganizationServiceImpl @Inject constructor(
    private val organizationRepository: OrganizationRepository
) : OrganizationService {
    override fun createOrganization(...): Organization {
        require(name.isNotBlank()) { "Name cannot be blank" }
        val organization = Organization.create(...)
        return organizationRepository.save(organization)
    }
}
```

**Files**:
- `service/OrganizationService.kt` - Service interface and implementation

### 4. API/Resource Layer (`com.ndptc.kala.api`)

**Purpose**: Exposes REST endpoints, handles HTTP concerns, and maps between DTOs and Domain models.

**Characteristics**:
- JAX-RS annotated resources
- OpenAPI documentation annotations
- Maps DTO ↔ Domain models
- HTTP response handling
- No business logic (delegates to Service layer)

**Example**:
```kotlin
@Path("/api/v1/organizations")
class OrganizationResource @Inject constructor(
    private val organizationService: OrganizationService
) {
    @POST
    fun createOrganization(request: CreateOrganizationRequest): Response {
        val contactInfo = OrganizationDTOMapper.toContactInfo(request)
        val organization = organizationService.createOrganization(...)
        return Response.status(201)
            .entity(OrganizationDTOMapper.toDTO(organization))
            .build()
    }
}
```

**Files**:
- `api/resource/OrganizationResource.kt` - REST endpoints
- `api/mapper/OrganizationDTOMapper.kt` - Maps DTO ↔ Domain
- `dto/OrganizationDTO.kt` - Data Transfer Objects for API

## Data Flow

### Create Organization Request Flow

```
1. Client → POST /api/v1/organizations (CreateOrganizationRequest)
              ↓
2. OrganizationResource receives CreateOrganizationRequest DTO
              ↓
3. OrganizationDTOMapper converts DTO → ContactInfo (Domain concept)
              ↓
4. OrganizationResource calls OrganizationService.createOrganization()
              ↓
5. OrganizationService validates business rules
              ↓
6. OrganizationService creates Domain Organization
              ↓
7. OrganizationService calls OrganizationRepository.save()
              ↓
8. OrganizationRepository converts Domain → OrganizationEntity
              ↓
9. OrganizationEntity.persist() saves to database
              ↓
10. OrganizationRepository converts OrganizationEntity → Domain
              ↓
11. Service returns Domain Organization
              ↓
12. Resource converts Domain → OrganizationDTO
              ↓
13. Client ← HTTP 201 (OrganizationDTO)
```

## Mapping Strategy

### Three Types of Mappers

1. **Entity ↔ Domain** (`repository/mapper/OrganizationMapper.kt`)
   - Converts Panache entities to/from Domain models
   - Located in repository layer
   - Used by Repository implementations

2. **Domain ↔ DTO** (`api/mapper/OrganizationDTOMapper.kt`)
   - Converts Domain models to/from API DTOs
   - Located in API layer
   - Used by Resource classes

3. **Request DTO → Domain Concepts**
   - Converts request DTOs to Domain value objects (e.g., ContactInfo)
   - Part of the DTO mapper
   - Enables rich domain modeling

## Dependency Direction

```
API Layer
   ↓ depends on
Service Layer
   ↓ depends on
Repository Layer
   ↓ depends on
Domain Layer (depends on nothing)
```

**Key Principle**: Dependencies flow inward. The Domain layer has no external dependencies.

## Benefits of This Architecture

### 1. **Separation of Concerns**
- Each layer has a single, well-defined responsibility
- Business logic lives in the Domain and Service layers
- Infrastructure concerns (persistence, HTTP) are isolated

### 2. **Testability**
- Domain logic can be tested without frameworks
- Service layer can be tested with mocked repositories
- Interface-based design enables easy mocking

### 3. **Flexibility**
- Can swap Panache for another ORM without touching Domain/Service
- Can change API framework without touching business logic
- Domain models remain stable

### 4. **Maintainability**
- Clear structure makes code easy to navigate
- Changes are localized to specific layers
- New developers can understand the flow easily

### 5. **Framework Independence**
- Domain models don't depend on Quarkus, Panache, or JAX-RS
- Business logic can be reused across different frameworks
- Easier migration paths

## Package Structure

```
src/main/kotlin/com/ndptc/kala/
├── domain/                    # Core business models
│   └── Organization.kt
├── repository/                # Data access layer
│   ├── OrganizationRepository.kt
│   ├── entity/
│   │   └── OrganizationEntity.kt
│   └── mapper/
│       └── OrganizationMapper.kt
├── service/                   # Business logic layer
│   └── OrganizationService.kt
├── api/                       # REST API layer
│   ├── resource/
│   │   └── OrganizationResource.kt
│   └── mapper/
│       └── OrganizationDTOMapper.kt
└── dto/                       # API request/response objects
    └── OrganizationDTO.kt
```

## Design Patterns Used

### 1. **Repository Pattern**
- `OrganizationRepository` interface abstracts data access
- Implementation uses Panache for persistence
- Isolates domain from persistence concerns

### 2. **Service Layer Pattern**
- `OrganizationService` encapsulates business logic
- Coordinates between repository and domain operations
- Transaction boundaries

### 3. **Data Transfer Object (DTO) Pattern**
- Separate DTOs for API contracts
- Prevents internal models from leaking to API consumers
- API versioning flexibility

### 4. **Mapper Pattern**
- Dedicated mappers for layer transitions
- Single responsibility: convert between representations
- Reusable conversion logic

### 5. **Domain-Driven Design (DDD)**
- Rich domain models with behavior
- Domain models are framework-agnostic
- Business logic encapsulated in domain

## Testing Strategy

### Unit Testing Layers

1. **Domain Layer**
   ```kotlin
   @Test
   fun `should deactivate organization`() {
       val org = Organization.create("Test")
       val deactivated = org.deactivate()
       assertFalse(deactivated.isActive)
       assertNotNull(deactivated.timestamps.removedAt)
   }
   ```

2. **Service Layer** (with mocked repository)
   ```kotlin
   @Test
   fun `should create organization`() {
       val mockRepo = mock<OrganizationRepository>()
       val service = OrganizationServiceImpl(mockRepo)
       // Test business logic
   }
   ```

3. **Repository Layer** (with test database)
   ```kotlin
   @Test
   @Transactional
   fun `should save organization`() {
       val org = Organization.create("Test")
       val saved = repository.save(org)
       assertNotNull(saved.id)
   }
   ```

4. **API Layer** (with REST Assured)
   ```kotlin
   @Test
   fun `should create organization via API`() {
       given()
           .contentType(ContentType.JSON)
           .body(request)
       .when()
           .post("/api/v1/organizations")
       .then()
           .statusCode(201)
   }
   ```

## Extension Guidelines

### Adding a New Entity (e.g., Project)

1. **Create Domain Model** (`domain/Project.kt`)
   ```kotlin
   data class Project(...) {
       companion object {
           fun create(...): Project
       }
       fun update(...): Project
   }
   ```

2. **Create Persistence Entity** (`repository/entity/ProjectEntity.kt`)
   ```kotlin
   @Entity
   class ProjectEntity : PanacheEntity() { ... }
   ```

3. **Create Repository** (`repository/ProjectRepository.kt`)
   ```kotlin
   interface ProjectRepository { ... }
   @ApplicationScoped
   class ProjectRepositoryImpl : ProjectRepository { ... }
   ```

4. **Create Mapper** (`repository/mapper/ProjectMapper.kt`)
   ```kotlin
   object ProjectMapper {
       fun toDomain(entity: ProjectEntity): Project
       fun toEntity(domain: Project): ProjectEntity
   }
   ```

5. **Create Service** (`service/ProjectService.kt`)
   ```kotlin
   interface ProjectService { ... }
   @ApplicationScoped
   class ProjectServiceImpl : ProjectService { ... }
   ```

6. **Create DTOs** (`dto/ProjectDTO.kt`)
   ```kotlin
   data class ProjectDTO(...)
   data class CreateProjectRequest(...)
   ```

7. **Create API Mapper** (`api/mapper/ProjectDTOMapper.kt`)
   ```kotlin
   object ProjectDTOMapper {
       fun toDTO(domain: Project): ProjectDTO
   }
   ```

8. **Create Resource** (`api/resource/ProjectResource.kt`)
   ```kotlin
   @Path("/api/v1/projects")
   class ProjectResource @Inject constructor(
       private val projectService: ProjectService
   ) { ... }
   ```

## Common Operations

### Creating a New Resource

```kotlin
// 1. Domain creation
val organization = Organization.create(name, description, contactInfo)

// 2. Service validation and save
val saved = organizationService.createOrganization(...)

// 3. Repository persistence
val persisted = organizationRepository.save(organization)

// 4. Convert to DTO for API response
val dto = OrganizationDTOMapper.toDTO(persisted)
```

### Updating an Existing Resource

```kotlin
// 1. Service retrieves domain model
val existing = organizationService.getOrganizationById(id)

// 2. Domain update (immutable)
val updated = existing.update(name, description, contactInfo)

// 3. Service validates and saves
val saved = organizationService.updateOrganization(id, ...)

// 4. Repository persists changes
val persisted = organizationRepository.save(updated)
```

## Key Principles

1. **Domain Independence**: Domain models have no framework dependencies
2. **Single Responsibility**: Each layer has one clear purpose
3. **Dependency Inversion**: Depend on interfaces, not implementations
4. **Immutability**: Domain models are immutable (Kotlin data classes)
5. **Explicit Mapping**: Clear boundaries with dedicated mappers
6. **Business Logic in Domain**: Rich domain models, not anemic POJOs
7. **Service Coordination**: Services orchestrate, don't duplicate domain logic

## Error Handling (RFC 9457)

All API errors use **RFC 9457 Problem Details** format for consistent, machine-readable error responses.

### Problem Details Structure

```json
{
  "type": "https://kala.ndptc.com/problems/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "The 'name' field cannot be empty",
  "instance": "/api/v1/documents"
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `type` | Yes | URI identifying the problem type |
| `title` | Yes | Short human-readable summary |
| `status` | Yes | HTTP status code |
| `detail` | No | Specific explanation for this occurrence |
| `instance` | No | URI reference to the specific request |

### Problem Types

Define problem types as URIs under `https://kala.ndptc.com/problems/`:

| Type | Status | Usage |
|------|--------|-------|
| `not-found` | 404 | Resource does not exist |
| `validation-error` | 400 | Request validation failed |
| `conflict` | 409 | Resource state conflict (e.g., duplicate) |
| `forbidden` | 403 | Insufficient permissions |
| `internal-error` | 500 | Unexpected server error |

### Implementation by Layer

**Service Layer**: Throw domain-specific exceptions
```kotlin
class DocumentNotFoundException(val documentId: UUID) : RuntimeException()
class ValidationException(val field: String, val reason: String) : RuntimeException()
```

**API Layer**: Use exception mappers to convert to Problem Details
```kotlin
@Provider
class DocumentNotFoundExceptionMapper : ExceptionMapper<DocumentNotFoundException> {
    override fun toResponse(e: DocumentNotFoundException): Response {
        return Response.status(404)
            .type("application/problem+json")
            .entity(ProblemDetail(
                type = "https://kala.ndptc.com/problems/not-found",
                title = "Document Not Found",
                status = 404,
                detail = "Document with ID ${e.documentId} was not found"
            ))
            .build()
    }
}
```

### Content Type

Always return `application/problem+json` for error responses:
```kotlin
Response.status(status)
    .type("application/problem+json")
    .entity(problemDetail)
    .build()
```

## Future Considerations

- **Event Sourcing**: Domain events for audit trail
- **CQRS**: Separate read/write models for complex queries
- **API Versioning**: Support multiple API versions via separate DTOs
- **Validation**: Bean Validation in DTOs, business validation in Domain/Service
- **Caching**: Add caching at Service or Repository level
- **Multi-tenancy**: Add tenant context to Domain models
