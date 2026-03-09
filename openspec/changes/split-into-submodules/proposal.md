# Split Into Submodules

## Summary

Split the single-module revet-documents project into three Gradle submodules (`core`, `web`, `persistence-runtime`) following the Revet Quarkus Project Guide multi-module layout.

## Motivation

The Revet Quarkus Project Guide defines a standard three-module structure where domain models and interfaces live in `core`, Panache entities and repository implementations live in `persistence-runtime`, and JAX-RS resources/services live in `web`. This enforces compile-time separation of concerns — domain logic cannot accidentally depend on persistence or HTTP frameworks.

Currently all 76 source files live in a single module with all dependencies mixed together.

## Scope

### In Scope

- Create `core`, `web`, and `persistence-runtime` Gradle submodules with appropriate `build.gradle.kts` files
- Split combined interface+implementation files (all 6 repository files and 7 service files contain both)
- Move files to their target modules (see design.md for full mapping)
- Update `settings.gradle.kts` with `include()` for each submodule
- Convert root `build.gradle.kts` to apply common config across subprojects
- Move `application.properties` and Flyway migrations to `web`

### Out of Scope

- Refactoring SearchService away from direct EntityManager usage (tracked as future work)
- Changing package names (all modules share `com.revethq.documents.*`)
- Database or API changes
