## Context

The Kala API is a single-module Quarkus application (not a multi-module library). The Revet Quarkus project guide covers both multi-module libraries and single-module applications. We apply the relevant parts: version catalog, version bumps, allOpen config, and ktlint.

## Goals / Non-Goals

- Goals:
  - Align all tool versions with the Revet Quarkus project guide
  - Introduce `gradle/libs.versions.toml` version catalog
  - Add ktlint for consistent code formatting
  - Ensure the project builds and runs on JVM 25
- Non-Goals:
  - Splitting into multi-module structure (this is a single-module application, not a library)
  - Fixing existing ktlint violations in this proposal (run `./gradlew ktlintCheck` to assess, fix separately)
  - Changing any application behavior or APIs

## Decisions

### 1. Version catalog for all dependency versions

Move all version strings into `gradle/libs.versions.toml`. This centralizes version management and aligns with Revet conventions. The `gradle.properties` BOM variables (`quarkusPlatformGroupId`, etc.) are removed since the catalog handles this.

### 2. Gradle 9 upgrade path

Gradle 9 removes the deprecated Convention API. Kotlin 2.3.10 and Quarkus 3.31.1 are confirmed compatible. The `allOpen` block syntax is unchanged — only the internal plugin implementation moved from `convention` to `extensions` API, which is handled by the updated Kotlin plugin.

### 3. Quarkiverse BOM alignment

The `quarkus-amazon-services-bom` must match the Quarkus version. Updating to 3.31.1 ensures the S3/MinIO Quarkiverse extensions are compatible. The GCS extension version should be updated to the latest compatible release.

### 4. JVM 25 Docker images

- `Dockerfile.jvm`: Switch to an OpenJDK 25 base image. The Red Hat UBI9 `openjdk-25-runtime` image may not yet exist; fall back to `eclipse-temurin:25-jre` if needed.
- `docker-compose-dev.yml`: Update `eclipse-temurin:21-jdk` → `eclipse-temurin:25-jdk`.
- `Dockerfile.native-distroless`: No change needed (native builds don't depend on JVM version at runtime).

### 5. ktlint integration

Add the `org.jlleitschuh.gradle.ktlint` plugin at version 1.5.0. Do not auto-fix formatting in this proposal — just add the plugin so `./gradlew ktlintCheck` works. Formatting fixes can be done in a follow-up.

## Risks / Trade-offs

- **JVM 25 availability** → Risk: Team members may not have JVM 25 installed. Mitigation: Document in CLAUDE.md; JVM 25 is an LTS-track release.
- **Quarkiverse version compatibility** → Risk: Quarkiverse extensions may not have a release for Quarkus 3.31.1 BOM. Mitigation: Check Maven Central during implementation and use latest compatible version.
- **ktlint violations** → Adding ktlint may surface many formatting violations. Mitigation: Don't fail the build on violations in this proposal; address separately.

## Open Questions

- None.
