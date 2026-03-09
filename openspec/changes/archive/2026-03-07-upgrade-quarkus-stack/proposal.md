# Change: Upgrade project to Revet Quarkus project guide standards

## Why

The Kala API uses outdated versions of Quarkus (3.17.4), Kotlin (2.0.21), Gradle (8.11.1), and Java 21. The Revet Quarkus project guide standardizes on Quarkus 3.31.1, Kotlin 2.3.10, Gradle 9.3.1, JVM 25, and ktlint 1.5.0. Aligning with the guide ensures compatibility with other Revet libraries, access to Gradle 9 improvements, and consistent code formatting.

## What Changes

- **Gradle 8.11.1 → 9.3.1** — Update wrapper via `gradle wrapper --gradle-version 9.3.1`
- **Quarkus 3.17.4 → 3.31.1** — Major framework upgrade
- **Kotlin 2.0.21 → 2.3.10** — K2 compiler, Gradle 9 compatible allopen/noarg plugins
- **Java 21 → 25** — JVM target, source/target compatibility, Docker base images
- **Add `gradle/libs.versions.toml`** — Version catalog replaces inline version strings in `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties`
- **Add ktlint 1.5.0** — Code formatting plugin, applied via `org.jlleitschuh.gradle.ktlint`
- **Update `build.gradle.kts`** — Use version catalog aliases, remove `gradle.properties` BOM variables
- **Update `settings.gradle.kts`** — Remove inline plugin versions (use catalog)
- **Update `gradle.properties`** — Remove BOM version properties (moved to catalog)
- **Update `Dockerfile.jvm`** — `openjdk-21-runtime` → `openjdk-25-runtime` (or equivalent JVM 25 base image)
- **Update `docker-compose-dev.yml`** — `eclipse-temurin:21-jdk` → `eclipse-temurin:25-jdk`
- **Update Quarkiverse dependencies** — Bump `quarkus-amazon-services-bom` and `quarkus-google-cloud-storage` to versions compatible with Quarkus 3.31.1

## Impact

- Affected specs: `build-toolchain` (new)
- Affected code:
  - **Modified**: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`
  - **Created**: `gradle/libs.versions.toml`
  - **Modified**: `src/main/docker/Dockerfile.jvm`, `docker-compose-dev.yml`
- **BREAKING**: Requires JVM 25 installed locally for development
- No API or behavioral changes — this is a build/tooling-only upgrade
