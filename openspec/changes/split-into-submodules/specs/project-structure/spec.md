# Project Structure

## MODIFIED Requirements

### Requirement: Multi-module project layout

The project MUST be organized into three Gradle submodules: `core`, `persistence-runtime`, and `web`.

#### Scenario: Module directory structure exists
- GIVEN the project root
- WHEN listing submodules
- THEN `core/`, `persistence-runtime/`, and `web/` directories exist with `build.gradle.kts` files

#### Scenario: settings.gradle.kts includes all modules
- GIVEN `settings.gradle.kts`
- WHEN reading the file
- THEN it contains `include("core")`, `include("persistence-runtime")`, and `include("web")`

### Requirement: Core module contains only domain models and interfaces

The `core` module MUST NOT depend on Quarkus BOM, Panache, JAX-RS, or CDI annotations. It contains domain models, repository interfaces, service interfaces, and framework-agnostic constants.

#### Scenario: Core has no Quarkus dependencies
- GIVEN `core/build.gradle.kts`
- WHEN reading dependencies
- THEN no Quarkus BOM or Quarkus libraries are listed

#### Scenario: Domain models are in core
- GIVEN `core/src/main/kotlin/com/revethq/documents/domain/`
- WHEN listing files
- THEN all 8 domain model files are present

### Requirement: Persistence-runtime MUST contain entities and repository implementations

The `persistence-runtime` module MUST depend on `core` and provide Panache entities, entity mappers, and repository implementations.

#### Scenario: Persistence-runtime depends on core
- GIVEN `persistence-runtime/build.gradle.kts`
- WHEN reading dependencies
- THEN `implementation(project(":core"))` is present

#### Scenario: Repository implementations are in persistence-runtime
- GIVEN `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/`
- WHEN listing files
- THEN all 6 repository implementation files are present

### Requirement: Web module MUST be the main Quarkus application

The `web` module MUST depend on `core` and contain JAX-RS resources, DTOs, service implementations, security, and the Quarkus application configuration.

#### Scenario: Web depends on core only (not persistence-runtime)
- GIVEN `web/build.gradle.kts`
- WHEN reading dependencies
- THEN `implementation(project(":core"))` is present
- AND `persistence-runtime` is NOT in the dependency list

#### Scenario: Application properties are in web
- GIVEN `web/src/main/resources/`
- WHEN listing files
- THEN `application.properties` is present

### Requirement: Interfaces and implementations are in separate files

Each repository and service MUST have its interface in `core` and its implementation in the appropriate module (`persistence-runtime` or `web`).

#### Scenario: Repository interface in core, implementation in persistence-runtime
- GIVEN `OrganizationRepository`
- WHEN locating files
- THEN `core/.../repository/OrganizationRepository.kt` contains only the interface
- AND `persistence-runtime/.../repository/OrganizationRepositoryImpl.kt` contains only the implementation
