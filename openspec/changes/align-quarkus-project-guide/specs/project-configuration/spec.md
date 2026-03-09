## ADDED Requirements

### Requirement: Kotlin NoArg Plugin for JPA Entities
The build MUST apply the `kotlin-noarg` plugin configured to generate no-arg constructors for classes annotated with `@Entity`, `@MappedSuperclass`, and `@Embeddable`.

#### Scenario: Entity classes compile with synthetic no-arg constructors
- **WHEN** the project is built
- **THEN** all `@Entity` classes have compiler-generated no-arg constructors
- **AND** Hibernate can instantiate entities without explicit no-arg constructors

### Requirement: AllOpen Plugin Coverage
The `allOpen` configuration MUST include `@RequestScoped`, `@MappedSuperclass`, and `@Embeddable` in addition to the existing `@Path`, `@ApplicationScoped`, and `@Entity` annotations.

#### Scenario: RequestScoped beans are proxyable
- **WHEN** a CDI bean is annotated with `@RequestScoped`
- **THEN** the Kotlin class is made open by the compiler plugin
- **AND** Quarkus ArC can create a client proxy for the bean

### Requirement: Revethq Package Convention
All source packages MUST use the `com.revethq.documents` base package, matching the Maven coordinate convention used by other Revet libraries (`com.revethq.buckets`, `com.revethq.iam`).

#### Scenario: Source files use revethq package
- **WHEN** the project is built
- **THEN** all source files declare `package com.revethq.documents.*`
- **AND** no references to `com.revet.documents` remain

### Requirement: Project Identity
The project MUST use the `revet-documents` identity: root project name, Gradle group, Quarkus application name, and database table prefixes SHALL use `revet` naming.

#### Scenario: Project artifacts use revet identity
- **WHEN** the project is built
- **THEN** the root project name is `revet-documents`
- **AND** the group is `com.revethq.documents`
- **AND** the Quarkus application name is `revet-documents`

#### Scenario: Database tables use revet prefix
- **WHEN** the application starts
- **THEN** entity tables are named `revet_documents`, `revet_projects`, `revet_organizations`, `revet_document_versions`

### Requirement: RFC 9457 Problem Type URLs
All RFC 9457 problem-type URIs MUST use the `https://docs.revethq.com/problems/` base URL.

#### Scenario: Error responses use revet problem-type URLs
- **WHEN** an API error response is returned
- **THEN** the `type` field uses a URI under `https://docs.revethq.com/problems/`
