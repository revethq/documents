# Change: Integrate Capabilities Endpoint

## Why

The frontend needs to know which features the current user can access so it can show/hide UI elements based on permissions and tenant-level feature flags. The `revet-capabilities` library provides this functionality — it evaluates all declared capabilities against the user's IAM policies and tenant overrides, returning a single response the UI can consume. The documents service needs to integrate this library and declare its capabilities.

## What Changes

- Add `revet-capabilities` library dependencies across all three modules (`core`, `persistence-runtime`, `web`)
- Implement a `DocumentsCapabilityProvider` in `persistence-runtime` that declares the documents service's capability manifest, including cross-service capabilities for organizations, projects, buckets, users, and groups
- The `GET /api/v1/capabilities` endpoint is provided automatically by the `revet-capabilities-web` module via CDI discovery
- Add a Flyway migration for the `revet_tenant_capabilities` table used by the library
- Add version catalog entries for the new library

## Impact

- Affected specs: new `capabilities` spec
- Affected code:
  - `gradle/libs.versions.toml` — new version and library entries
  - `core/build.gradle.kts` — add `revet-capabilities-core` dependency
  - `persistence-runtime/build.gradle.kts` — add `revet-capabilities-persistence-runtime` dependency
  - `web/build.gradle.kts` — add `revet-capabilities-web` dependency
  - `persistence-runtime/.../DocumentsCapabilityProvider.kt` — new file declaring the capability manifest
  - `web/src/main/resources/db/migration/` — new Flyway migration for `revet_tenant_capabilities`
