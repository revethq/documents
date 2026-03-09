## ADDED Requirements

### Requirement: Revet Quarkus Project Guide Conformance

The project SHALL use the versions and conventions specified by the Revet Quarkus project guide for all build tooling.

#### Scenario: Version requirements met

- **WHEN** the project build configuration is inspected
- **THEN** Quarkus version is 3.31.1
- **AND** Kotlin version is 2.3.10
- **AND** Gradle version is 9.3.1
- **AND** JVM target is 25

### Requirement: Gradle Version Catalog

The project SHALL use a `gradle/libs.versions.toml` version catalog to centralize all dependency versions, library coordinates, and plugin declarations. Inline version strings in `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties` SHALL be replaced with catalog references.

#### Scenario: Dependencies reference catalog aliases

- **WHEN** `build.gradle.kts` declares a dependency
- **THEN** it uses a catalog alias (e.g., `libs.quarkus.rest`) instead of an inline version string

#### Scenario: Plugins reference catalog aliases

- **WHEN** `build.gradle.kts` or `settings.gradle.kts` applies a plugin
- **THEN** it uses a catalog alias (e.g., `alias(libs.plugins.quarkus)`) instead of an inline version string

### Requirement: ktlint Code Formatting

The project SHALL include the `org.jlleitschuh.gradle.ktlint` plugin (version 1.5.0) for Kotlin code formatting checks. The `./gradlew ktlintCheck` task SHALL be available.

#### Scenario: ktlint check is runnable

- **WHEN** `./gradlew ktlintCheck` is executed
- **THEN** the task runs and reports formatting results

### Requirement: JVM 25 Docker Images

Docker images for the project SHALL use JVM 25 base images. The JVM Dockerfile SHALL use an OpenJDK 25 runtime image and the dev compose file SHALL use a JDK 25 image.

#### Scenario: JVM Dockerfile uses JDK 25 runtime

- **WHEN** the `Dockerfile.jvm` is inspected
- **THEN** the `FROM` image uses a JVM 25 runtime

#### Scenario: Dev compose uses JDK 25

- **WHEN** `docker-compose-dev.yml` is inspected
- **THEN** the API service uses a JDK 25 image
