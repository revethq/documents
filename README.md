# Revet Documents

Modern REST API for the Revet Document Management System, built with Kotlin, Quarkus, and Panache.

## Features

- **Kotlin + Quarkus**: Modern, reactive framework with native compilation support
- **Panache ORM**: Simplified Hibernate ORM with active record pattern
- **JAX-RS**: RESTful web services with reactive support
- **OpenAPI**: Automatic API documentation generation
- **PostgreSQL**: Robust relational database
- **CORS**: Configured for local development with frontend at localhost:3001

## Technology Stack

- **Kotlin**: 2.3.10
- **Quarkus**: 3.31.1
- **Gradle**: 9.3.1
- **Java**: 25
- **PostgreSQL**: Latest
- **Hibernate Panache**: Kotlin variant

## Prerequisites

- Java 25 or higher
- Docker and Docker Compose (for database)
- Gradle 9.3.1 or higher (wrapper included)

## Getting Started

### 1. Start the Database

```bash
docker run -d \
  --name revet-postgres \
  -e POSTGRES_DB=revet \
  -e POSTGRES_USER=revet \
  -e POSTGRES_PASSWORD=revet \
  -p 5432:5432 \
  postgres:16
```

### 2. Run the Application in Dev Mode

```bash
./gradlew :web:quarkusDev
```

The application will start on `http://localhost:5051`

### 3. Access the API Documentation

- **Swagger UI**: http://localhost:5051/swagger-ui
- **OpenAPI Spec**: http://localhost:5051/openapi

## Available Endpoints

### Organizations API

- `GET /api/v1/organizations` - List all organizations
- `GET /api/v1/organizations/{id}` - Get organization by ID
- `GET /api/v1/organizations/uuid/{uuid}` - Get organization by UUID
- `POST /api/v1/organizations` - Create new organization
- `PUT /api/v1/organizations/{id}` - Update organization
- `DELETE /api/v1/organizations/{id}` - Soft delete organization

## Project Structure

The project is a multi-module Gradle build with three submodules:

- **`core`** - Domain models, repository interfaces, and service interfaces (no framework dependencies)
- **`persistence-runtime`** - Panache entities, entity mappers, repository implementations, service implementations, and permission CDI beans (depends on `core`). Usable as a standalone library by other Revet services.
- **`web`** - REST resources, DTOs, DTO mappers, security filters, and application configuration (depends on `core` and `persistence-runtime`)

```
revet-documents/
├── core/                                # Domain + interfaces
│   └── src/main/kotlin/com/revethq/documents/
│       ├── domain/                      # Core business models (framework-agnostic)
│       ├── repository/                  # Repository interfaces
│       └── service/                     # Service interfaces
├── persistence-runtime/                 # Business logic + data access
│   └── src/main/kotlin/com/revethq/documents/
│       ├── repository/
│       │   ├── entity/                  # Panache entities
│       │   └── mapper/                  # Entity ↔ Domain mappers
│       ├── service/                     # Service implementations
│       └── permission/                  # Permission CDI beans
├── web/                                 # REST API layer
│   └── src/main/kotlin/com/revethq/documents/
│       ├── api/
│       │   ├── resource/               # JAX-RS resources (REST endpoints)
│       │   └── mapper/                 # Domain ↔ DTO mappers
│       └── dto/                        # Data transfer objects
│   └── src/main/resources/
│       └── application.properties
├── build.gradle.kts                     # Root build config
├── settings.gradle.kts
├── gradle.properties
├── ARCHITECTURE.md                      # Detailed architecture documentation
└── README.md
```

## Architecture

This project follows a **layered architecture** with clear separation of concerns:

- **API Layer** (`api/resource`): REST endpoints, maps DTOs ↔ Domain
- **Service Layer** (`service`): Business logic and validation
- **Repository Layer** (`repository`): Data access abstraction, maps Domain ↔ Entity
- **Domain Layer** (`domain`): Core business models (framework-agnostic)

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture documentation.

### Data Flow

```
Client → Resource (DTO) → Service (Domain) → Repository (Domain ↔ Entity) → Database
```

Key benefits:
- Framework-independent domain models
- Easy to test (interface-based layers)
- Clear separation of concerns
- Maintainable and extensible

## Development

### Running Tests

```bash
./gradlew test
```

### Building for Production

```bash
./gradlew build
```

### Running in Production Mode

```bash
java -jar web/build/quarkus-app/quarkus-run.jar
```

### Native Build (GraalVM)

```bash
./gradlew build -Dquarkus.package.type=native
```

## Configuration

Key configuration properties in `web/src/main/resources/application.properties`:

- **HTTP Port**: `quarkus.http.port=5051`
- **CORS Origins**: `quarkus.http.cors.origins=http://localhost:3001`
- **Database URL**: `quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/revet`
- **OpenAPI Path**: `quarkus.smallrye-openapi.path=/openapi`
- **Swagger UI Path**: `quarkus.swagger-ui.path=/swagger-ui`

## CORS Configuration

The API is configured to allow CORS requests from `http://localhost:3001` with the following settings:

- Methods: GET, PUT, POST, DELETE, OPTIONS, PATCH
- Headers: accept, authorization, content-type, x-requested-with
- Credentials: Allowed
- Max Age: 24 hours

## Database Schema

The application uses Flyway for database migrations. The database schema is managed through migration scripts in `web/src/main/resources/db/migration/`.

### Organizations Table

```sql
CREATE TABLE revet_organizations (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(50),
    fax VARCHAR(50),
    website VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    removed_at TIMESTAMP
);
```

## API Examples

### Create Organization

```bash
curl -X POST http://localhost:5051/api/v1/organizations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Revet",
    "description": "Revet Document Management",
    "city": "Anniston",
    "state": "Alabama",
    "country": "USA"
  }'
```

### List Organizations

```bash
curl http://localhost:5051/api/v1/organizations
```

### Get Organization by ID

```bash
curl http://localhost:5051/api/v1/organizations/1
```

### Update Organization

```bash
curl -X PUT http://localhost:5051/api/v1/organizations/1 \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated description"
  }'
```

### Delete Organization

```bash
curl -X DELETE http://localhost:5051/api/v1/organizations/1
```

## License

Apache 2.0
