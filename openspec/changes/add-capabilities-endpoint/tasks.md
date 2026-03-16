## 1. Dependencies

- [x] 1.1 Add `revet-capabilities` version to `gradle/libs.versions.toml`
- [x] 1.2 Add `revet-capabilities-core`, `revet-capabilities-persistence-runtime`, and `revet-capabilities-web` library entries to `libs.versions.toml`
- [x] 1.3 Add `revet-capabilities-core` dependency to `core/build.gradle.kts`
- [x] 1.4 Add `revet-capabilities-persistence-runtime` dependency to `persistence-runtime/build.gradle.kts`
- [x] 1.5 Add `revet-capabilities-web` dependency to `web/build.gradle.kts`

## 2. Capability Provider

- [x] 2.1 Create `DocumentsCapabilityProvider` in `persistence-runtime` that implements `CapabilityProvider` and returns a `CapabilityManifest` declaring the documents service capabilities
- [x] 2.2 Define documents-native capability declarations: document management, version management, category management, tag management, user management, file upload, and search
- [x] 2.3 Define cross-service capability declarations: organization management (`CoreActions.Organization`), project management (`CoreActions.Project`), bucket management (`BucketActions.Bucket`), and group management (`IamActions.Group`)

## 3. Database Migration

- [x] 3.1 Add Flyway migration script for `revet_tenant_capabilities` table

## 4. Verification

- [x] 4.1 Verify the application compiles with `./gradlew compileKotlin`
- [ ] 4.2 Verify `GET /api/v1/capabilities` endpoint is auto-registered by starting the dev server
