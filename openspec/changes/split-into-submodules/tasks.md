## 1. Create Module Directories and Build Files

- [x] 1.1 Create directory structure: `core/src/main/kotlin/com/revethq/documents/`, `persistence-runtime/src/main/kotlin/com/revethq/documents/`, `web/src/main/kotlin/com/revethq/documents/`
- [x] 1.2 Create `core/build.gradle.kts` — Kotlin library with `java-library` plugin, `revet-core` dependency
- [x] 1.3 Create `persistence-runtime/build.gradle.kts` — Quarkus module with Panache, allOpen/noArg for JPA, depends on `:core`
- [x] 1.4 Create `web/build.gradle.kts` — Main Quarkus app with REST, JWT, revet-iam, revet-buckets, depends on `:core` and `:persistence-runtime`
- [x] 1.5 Update `settings.gradle.kts` — add `include("core")`, `include("persistence-runtime")`, `include("web")`
- [x] 1.6 Convert root `build.gradle.kts` to apply common config (Java target, test config) to subprojects

## 2. Split Interfaces from Implementations

- [x] 2.1 Split 6 repository files: extract interface into `core/repository/`, leave impl in `persistence-runtime/repository/` — OrganizationRepository, ProjectRepository, DocumentRepository, DocumentVersionRepository, CategoryRepository, TagRepository
- [x] 2.2 Split 6 service files: extract interface into `core/service/`, leave impl in `web/service/` — OrganizationService, ProjectService, DocumentService, DocumentVersionService, CategoryService, TagService
- [x] 2.3 Split SearchService: interface + SearchResults data class → `core/service/`, SearchServiceImpl → `persistence-runtime/service/`
- [x] 2.4 Split CurrentUserService: interface → `core/security/`, CurrentUserServiceImpl → `web/security/`

## 3. Move Files to Target Modules

- [x] 3.1 Move domain files (8) to `core/` — domain/Category, Document, DocumentVersion, Organization, Page, Project, Tag, UploadStatus
- [x] 3.2 Move `permission/Actions.kt` to `core/`
- [x] 3.3 Move entity files (7) to `persistence-runtime/` — repository/entity/*
- [x] 3.4 Move mapper files (6) to `persistence-runtime/` — repository/mapper/*
- [x] 3.5 Move DTO files (9) to `web/` — dto/*
- [x] 3.6 Move API mapper files (7) to `web/` — api/mapper/*
- [x] 3.7 Move API resource files (8) to `web/` — api/resource/*
- [x] 3.8 Move API exception file to `web/` — api/exception/GlobalExceptionMapper
- [x] 3.9 Move permission CDI beans (2) to `web/` — permission/DocumentsUrn, PrebuiltPolicies
- [x] 3.10 Move security files (2) to `web/` — AuthorizationContextPopulator, UserProvisioningAugmentor
- [x] 3.11 Move remaining service files (2) to `web/` — DocumentStorageService, UserProvisioningService
- [x] 3.12 Move GraalVM substitutions to `web/` — graalvm/GrpcNettySubstitutions
- [x] 3.13 Move `application.properties` and `db/migration/` to `web/src/main/resources/`
- [x] 3.14 Remove old `src/main/kotlin/` directory tree

## 4. Build and Verify

- [x] 4.1 Run `./gradlew build` and fix compilation errors (added beans.xml for CDI discovery, Panache dep to web)
- [x] 4.2 Run `./gradlew ktlintFormat` across all modules
- [x] 4.3 Verify final `./gradlew build` passes clean

## 5. Update Documentation

- [x] 5.1 Update `CLAUDE.md` — project structure, common commands, adding-a-new-entity guidance
- [x] 5.2 Update `ARCHITECTURE.md` — package structure section
- [x] 5.3 Update `README.md` — project structure section
