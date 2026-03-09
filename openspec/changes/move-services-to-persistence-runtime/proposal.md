# Change: Move service implementations to persistence-runtime

## Why

The `persistence-runtime` module is intended to be a self-contained library that other Revet services can depend on to bring the documents system in-process — without pulling in the REST API layer. For this to work, service implementations and their supporting CDI beans must live in `persistence-runtime`, not `web`. Currently they live in `web`, which means any consumer that wants documents business logic must also depend on `web` and all its REST/HTTP/JWT/OpenAPI baggage.

Moving services to `persistence-runtime` also eliminates `web`'s dependency on `quarkus-hibernate-orm-panache-kotlin`, keeping web as a thin REST layer.

## What Changes

- Move 8 service implementation files from `web/service/` to `persistence-runtime/service/`
- Move 2 permission CDI beans (`PrebuiltPolicies`, `DocumentsUrn`) from `web/permission/` to `persistence-runtime/permission/` (they are dependencies of the moved services)
- Add revet-iam, revet-buckets, and CDI dependencies to `persistence-runtime/build.gradle.kts`
- Remove `quarkus-hibernate-orm-panache-kotlin` and persistence-only dependencies from `web/build.gradle.kts`
- Update `web/build.gradle.kts` to depend on `persistence-runtime` (web needs service impls and permission beans at compile time for security filters)

## Impact

- Affected specs: `project-structure` (module responsibilities and dependency graph)
- Affected code:
  - `persistence-runtime/build.gradle.kts` — add dependencies
  - `web/build.gradle.kts` — remove Panache and persistence-only deps, add persistence-runtime dep
  - 10 files moved from `web/` to `persistence-runtime/`
