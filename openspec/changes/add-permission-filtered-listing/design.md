## Context

The current authorization model uses `@RequiresPermission` as a binary gate on every endpoint, including list endpoints. The annotation checks whether the user has a specific action on a wildcard resource URN (e.g., `urn:revet:core::project/*`). This all-or-nothing approach blocks users who have resource-scoped policies (e.g., permission on a single project) from accessing list endpoints.

**Constraint**: The `ProjectResource`, `OrganizationResource`, and `BucketResource` endpoints live in external libraries (`revet-core-web`, `revet-buckets-web`). IAM resource endpoints live in `revet-iam` (`permission-web`, `user-web`, `service-account-web`, `scim`). These libraries require coordinated changes to support disabling their resource classes.

## Goals / Non-Goals

- **Goal**: Users with resource-scoped permissions can call list endpoints and see only the resources they have access to
- **Goal**: Users with wildcard permissions (admins) continue to see all resources
- **Goal**: Unauthenticated requests to list endpoints return 401
- **Non-goal**: Query-level permission filtering (SQL WHERE clause integration with policies) — post-query filtering is acceptable for v1
- **Non-goal**: Pagination accuracy — filtered pages may return fewer items than requested

## Decisions

### PermissionFilterService in persistence-runtime

**Decision**: Create a `@RequestScoped` `PermissionFilterService` that collects the user's policies once per request and evaluates each result item in-memory.

**Rationale**: Policies are already loaded by `PanachePolicyCollector` via a single DB query. Using `PolicyEvaluator.evaluateWithPolicies()` avoids per-item database calls. Request-scoped caching ensures one policy fetch per request regardless of how many list methods are called.

**Alternatives considered**:
- **Query-level filtering**: More efficient but requires mapping policy resources to SQL WHERE clauses. Complex to implement, deferred to a future optimization.
- **Filtering in the resource layer**: Would violate the layered architecture (API layer shouldn't contain business logic).

### @Authenticated replaces @RequiresPermission on list endpoints

**Decision**: Replace `@RequiresPermission` with Quarkus `@Authenticated` on list methods. Keep `@RequiresPermission` on all other methods (get, create, update, delete).

**Rationale**: `@Authenticated` ensures the user is logged in (401 for anonymous), while the service layer handles fine-grained filtering. Non-list endpoints continue to use `@RequiresPermission` for resource-specific access control.

### Local resource subclasses for library endpoints

**Decision**: Create `DocumentsProjectResource`, `DocumentsOrganizationResource`, and `DocumentsBucketResource` in the documents app that extend the library resources and override only the list method with `@Authenticated`.

**Rationale**: JAX-RS does not inherit method annotations to overriding methods. Overriding just the list method removes `@RequiresPermission` from it while all other methods inherit the parent's annotations.

### @UnlessBuildProperty to disable library resource classes

**Decision**: Add `@UnlessBuildProperty` annotations to library resource classes (`revet-core-web`, `revet-buckets-web`, `revet-iam`) so consuming applications can disable them via build properties. The documents app sets `revet.core.resources.disabled=true`, `revet.buckets.resources.disabled=true`, and `revet.iam.resources.disabled=true` in `application.properties`.

**Rationale**: When a subclass and parent both have `@Path`, Quarkus RESTEasy Reactive registers all methods from both as separate endpoints, causing duplicate endpoint errors. `quarkus.arc.exclude-types` only affects CDI bean discovery — it does NOT prevent RESTEasy Reactive from registering JAX-RS resources discovered via Jandex. `@UnlessBuildProperty` (from `io.quarkus.arc.properties`) is respected by both CDI and RESTEasy Reactive, making it the correct mechanism for conditionally disabling library resource classes. The `enableIfMissing = true` parameter ensures resources remain active by default in applications that don't set the property.

**Alternatives considered**:
- **`quarkus.arc.exclude-types`**: Only affects CDI, does not prevent JAX-RS resource registration. Tried and failed during implementation.
- **Removing Jandex indexing for library web modules**: Prevents CDI discovery of critical service beans (e.g., `BucketServiceImpl`, `StorageServiceImpl`) that live in the same module as the resource classes.

### Filtering action uses "Get" not "List"

**Decision**: When filtering list results, check the `Get` action (e.g., `documents:GetDocument`) rather than the `List` action.

**Rationale**: If a user can view a resource, it should appear in lists. The `List` action is now only used for endpoint-level gating (which we're removing). The `Get` action reflects actual access to the resource.

## Risks / Trade-offs

- **Pagination inaccuracy** → Pages may have fewer items than `size` parameter. Total counts reflect filtered results. Acceptable for v1; query-level filtering can be added later.
- **Performance on large datasets** → Filtering N items in-memory after query. Mitigated by: policies collected once per request, in-memory evaluation is fast. For very large datasets, query-level filtering would be needed.
- **Policy configuration gap** → Project-scoped users won't see categories/tags unless their policies explicitly grant those permissions. This is a policy configuration concern, not a code defect.
