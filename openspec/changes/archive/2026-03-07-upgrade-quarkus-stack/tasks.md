## 1. Gradle Wrapper Upgrade

- [x] 1.1 Update Gradle wrapper to 9.3.1 (`gradle-wrapper.properties`)

## 2. Version Catalog

- [x] 2.1 Create `gradle/libs.versions.toml` with all version declarations, library aliases, and plugin aliases
- [x] 2.2 Update `settings.gradle.kts` to remove inline plugin versions and use catalog
- [x] 2.3 Update `build.gradle.kts` to use catalog aliases (`libs.plugins.*`, `libs.quarkus.*`, etc.)
- [x] 2.4 Clean up `gradle.properties` — remove `quarkusPlatformGroupId`, `quarkusPlatformArtifactId`, `quarkusPlatformVersion`

## 3. Version Bumps

- [x] 3.1 Bump Quarkus to 3.31.1 in version catalog
- [x] 3.2 Bump Kotlin to 2.3.10 in version catalog
- [x] 3.3 Update JVM target from 21 to 25 in `build.gradle.kts` (`sourceCompatibility`, `targetCompatibility`, `jvmTarget`)
- [x] 3.4 Update Quarkiverse `quarkus-amazon-services-bom` to 3.31.1
- [x] 3.5 Update Quarkiverse `quarkus-google-cloud-storage` to 2.20.1

## 4. ktlint

- [x] 4.1 Add `org.jlleitschuh.gradle.ktlint` plugin (version 14.0.1) to version catalog and `build.gradle.kts`
- [x] 4.2 Verify `./gradlew ktlintCheck` runs (formatting violations exist but are not fixed in this change)

## 5. Docker Images

- [x] 5.1 Update `src/main/docker/Dockerfile.jvm` base image to `eclipse-temurin:25-jre`
- [x] 5.2 Update `docker-compose-dev.yml` JDK image from `eclipse-temurin:21-jdk` to `eclipse-temurin:25-jdk`

## 6. Configuration Fixes

- [x] 6.1 Fix `quarkus.http.cors` → `quarkus.http.cors.enabled` (deprecated in Quarkus 3.31.1)
- [x] 6.2 Remove obsolete `quarkus.google.cloud.storage.devservices.enabled` config key

## 7. Documentation

- [x] 7.1 Update `CLAUDE.md` to reflect new version requirements (Java 25, Quarkus 3.31.1, Kotlin 2.3.10)

## 8. Verify

- [x] 8.1 `./gradlew compileKotlin` — compilation passes
- [x] 8.2 `./gradlew build -x test -x ktlintCheck` — full build passes with no warnings
