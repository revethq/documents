# Change: Align project with Revet Quarkus Project Guide

## Why

The project was recently upgraded to Quarkus 3.31.1, Kotlin 2.3.10, Gradle 9.3.1, and JVM 25, but several conformance items from the Revet Quarkus Project Guide remain unaddressed. Additionally, the project still carries the legacy "kala" identity and uses `com.revet.documents` source packages instead of the standard `com.revethq.documents` convention used by all other Revet libraries.

## What Changes

### Build Configuration (from guide conformance checklist)
- Add `kotlin-noarg` plugin with JPA annotations (`@Entity`, `@MappedSuperclass`, `@Embeddable`)
- Update `allOpen` to include `@RequestScoped`, `@MappedSuperclass`, `@Embeddable`
- Add `kotlin-noarg` plugin entry to `gradle/libs.versions.toml`

### Package Rename
- **BREAKING**: Rename all source packages from `com.revet.documents` to `com.revethq.documents` (68 files, 735 occurrences)
- Move source directory tree from `src/main/kotlin/com/revet/documents/` to `src/main/kotlin/com/revethq/documents/`
- Update `application.properties` log category reference

### Project Identity Cleanup
- Rename root project from `kala-api` to `revet-documents` in `settings.gradle.kts`
- Update group from `com.ndptc.kala` to `com.revethq.documents` in `build.gradle.kts`
- Update `quarkus.application.name` from `kala-api` to `revet-documents` in `application.properties`
- **BREAKING**: Rename database tables from `kala_*` to `revet_*` via SQL migration
- Update raw SQL queries referencing old table names
- Update RFC 9457 problem-type URLs from `kala.ndptc.com` to `docs.revethq.com`

### Documentation Updates
- Update `openspec/project.md` to reflect current versions and `com.revethq.documents` packages
- Update `CLAUDE.md` to reflect current versions
- Update `ARCHITECTURE.md` to reflect `com.revethq.documents` packages and new problem-type URLs

## Impact
- Affected code: All 68 Kotlin source files (package/import rename), build files, `application.properties`, entity `@Table` annotations, raw SQL queries, problem-type URLs
- Affected database: Table renames require SQL migration (safe `ALTER TABLE RENAME` — metadata-only in PostgreSQL)
- No API contract changes (endpoint paths, request/response shapes remain the same)
