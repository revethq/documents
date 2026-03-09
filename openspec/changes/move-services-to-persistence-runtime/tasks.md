## 1. Update Build Files

- [x] 1.1 Update `persistence-runtime/build.gradle.kts` — add revet-iam, revet-buckets, CDI dependencies and allOpen annotations for `@ApplicationScoped`/`@RequestScoped`; remove `quarkus` plugin (library module, not application)
- [x] 1.2 Update `web/build.gradle.kts` — remove `quarkus-hibernate-orm-panache-kotlin` and persistence-only deps (`revet.iam.user.persistence`, `revet.iam.permission.persistence`, `revet.buckets.persistence`, `revet.buckets.provider.s3`, `revet.buckets.provider.gcs`); `implementation(project(":persistence-runtime"))` was already present

## 2. Move Files

- [x] 2.1 Move 8 service files from `web/service/` to `persistence-runtime/service/` — CategoryServiceImpl, DocumentServiceImpl, DocumentVersionServiceImpl, TagServiceImpl, OrganizationServiceImpl, ProjectServiceImpl, UserProvisioningService, DocumentStorageService
- [x] 2.2 Move 2 permission files from `web/permission/` to `persistence-runtime/permission/` — PrebuiltPolicies, DocumentsUrn
- [x] 2.3 Remove the moved files from `web/` and clean up empty directories

## 3. Build and Verify

- [x] 3.1 Run `./gradlew build` and fix any compilation or CDI errors (removed `quarkus` plugin from persistence-runtime to avoid standalone CDI validation)
- [x] 3.2 Run `./gradlew ktlintFormat` across all modules
- [x] 3.3 Verify final `./gradlew clean build` passes clean

## 4. Update Documentation

- [x] 4.1 Update `CLAUDE.md` — module responsibilities, layer table, adding-a-new-entity guidance
- [x] 4.2 Update `ARCHITECTURE.md` — package structure diagram and extension guidelines
- [x] 4.3 Update `README.md` — project structure description and diagram
