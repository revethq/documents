## 1. Build Configuration (Guide Conformance)

- [x] 1.1 Add `kotlin-noarg` plugin to `gradle/libs.versions.toml` (`kotlin-noarg = { id = "org.jetbrains.kotlin.plugin.noarg", version.ref = "kotlin" }`)
- [x] 1.2 Apply `kotlin-noarg` plugin in `build.gradle.kts` and configure `noArg` block for `@Entity`, `@MappedSuperclass`, `@Embeddable`
- [x] 1.3 Update `allOpen` block in `build.gradle.kts`: add `@RequestScoped`, `@MappedSuperclass`, `@Embeddable`
- [x] 1.4 Verify build passes (`./gradlew build -x test`)

## 2. Package Rename (`com.revet.documents` → `com.revethq.documents`)

- [x] 2.1 Move source directory `src/main/kotlin/com/revet/documents/` → `src/main/kotlin/com/revethq/documents/`
- [x] 2.2 Find-and-replace all `com.revet.documents` → `com.revethq.documents` in package declarations and imports across all 68 source files
- [x] 2.3 Update `application.properties` log category from `com.revet.documents` to `com.revethq.documents`
- [x] 2.4 Remove empty `src/main/kotlin/com/revet/` directory tree
- [x] 2.5 Verify build passes (`./gradlew build -x test`)

## 3. Project Identity

- [x] 3.1 Update `settings.gradle.kts`: rename `rootProject.name` from `kala-api` to `revet-documents`
- [x] 3.2 Update `build.gradle.kts`: change `group` from `com.ndptc.kala` to `com.revethq.documents`
- [x] 3.3 Update `application.properties`: change `quarkus.application.name` from `kala-api` to `revet-documents`

## 4. Database Table Renames

- [x] 4.1 Create SQL migration `V3__rename_kala_tables_to_revet.sql` to rename: `kala_documents` → `revet_documents`, `kala_projects` → `revet_projects`, `kala_companies` → `revet_organizations`, `kala_document_version` → `revet_document_versions`
- [x] 4.2 Update entity `@Table` annotations to match new table names: `DocumentEntity`, `ProjectEntity`, `OrganizationEntity`, `DocumentVersionEntity`
- [x] 4.3 Update raw SQL in `SearchService.kt` to use new table name `revet_organizations`

## 5. Problem-Type URL Cleanup

- [x] 5.1 Update problem-type URLs in `DocumentResource.kt` from `https://kala.ndptc.com/problems/` to `https://docs.revethq.com/problems/`
- [x] 5.2 Update problem-type URLs in `TagResource.kt` from `https://kala.ndptc.com/problems/` to `https://docs.revethq.com/problems/`

## 6. Documentation Updates

- [x] 6.1 Update `openspec/project.md` — fix tech stack versions (Kotlin 2.3.10, JVM 25, Quarkus 3.31.1, Gradle 9.3.1, revet-iam 0.1.15), update package references to `com.revethq.documents`
- [x] 6.2 Update `CLAUDE.md` — fix version references (Quarkus 3.31.1, Java 25), update package references
- [x] 6.3 Update `ARCHITECTURE.md` — replace `com.ndptc.kala` with `com.revethq.documents`, update problem-type URLs, rename title from "Kala API" to "Revet Documents"

## 7. Verify

- [x] 7.1 Build project (`./gradlew build`) and fix any compilation or ktlint errors
- [x] 7.2 Verify no remaining `kala`, `ndptc`, or `com.revet.documents` references in source code
