## MODIFIED Requirements

### Requirement: Persistence-runtime MUST contain entities and repository implementations

The `persistence-runtime` module MUST depend on `core` and provide Panache entities, entity mappers, repository implementations, service implementations, and permission CDI beans. It MUST be usable as a standalone library by other Revet services that need documents business logic in-process without depending on the `web` module.

#### Scenario: Persistence-runtime depends on core
- GIVEN `persistence-runtime/build.gradle.kts`
- WHEN reading dependencies
- THEN `implementation(project(":core"))` is present

#### Scenario: Repository implementations are in persistence-runtime
- GIVEN `persistence-runtime/src/main/kotlin/com/revethq/documents/repository/`
- WHEN listing files
- THEN all 6 repository implementation files are present

#### Scenario: Service implementations are in persistence-runtime
- GIVEN `persistence-runtime/src/main/kotlin/com/revethq/documents/service/`
- WHEN listing files
- THEN all service implementation files are present (CategoryServiceImpl, DocumentServiceImpl, DocumentVersionServiceImpl, TagServiceImpl, OrganizationServiceImpl, ProjectServiceImpl, UserProvisioningService, DocumentStorageService, SearchServiceImpl)

#### Scenario: Permission CDI beans are in persistence-runtime
- GIVEN `persistence-runtime/src/main/kotlin/com/revethq/documents/permission/`
- WHEN listing files
- THEN `PrebuiltPolicies.kt` and `DocumentsUrn.kt` are present

### Requirement: Web module MUST be the main Quarkus application

The `web` module MUST depend on `core` and `persistence-runtime`, and contain JAX-RS resources, DTOs, DTO mappers, security filters, and the Quarkus application configuration. The `web` module MUST NOT depend on `quarkus-hibernate-orm-panache-kotlin`.

#### Scenario: Web depends on core and persistence-runtime
- GIVEN `web/build.gradle.kts`
- WHEN reading dependencies
- THEN `implementation(project(":core"))` is present
- AND `implementation(project(":persistence-runtime"))` is present

#### Scenario: Web has no Panache dependency
- GIVEN `web/build.gradle.kts`
- WHEN reading dependencies
- THEN `quarkus-hibernate-orm-panache-kotlin` is NOT listed

#### Scenario: Application properties are in web
- GIVEN `web/src/main/resources/`
- WHEN listing files
- THEN `application.properties` is present

### Requirement: Interfaces and implementations are in separate files

Each repository and service MUST have its interface in `core` and its implementation in `persistence-runtime`.

#### Scenario: Repository interface in core, implementation in persistence-runtime
- GIVEN `OrganizationRepository`
- WHEN locating files
- THEN `core/.../repository/OrganizationRepository.kt` contains only the interface
- AND `persistence-runtime/.../repository/OrganizationRepositoryImpl.kt` contains only the implementation

#### Scenario: Service interface in core, implementation in persistence-runtime
- GIVEN `OrganizationService`
- WHEN locating files
- THEN `core/.../service/OrganizationService.kt` contains only the interface
- AND `persistence-runtime/.../service/OrganizationServiceImpl.kt` contains only the implementation
