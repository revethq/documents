package com.revethq.documents.permission

import com.revethq.core.Project
import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import com.revethq.iam.permission.evaluation.AuthorizationRequest
import com.revethq.iam.permission.evaluation.DefaultPolicyEvaluator
import com.revethq.iam.permission.evaluation.PolicyCollector
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.revethq.core.permission.Actions as CoreActions

/**
 * Tests verifying that project visibility is controlled by organization-level
 * permissions and direct project-level permission grants.
 *
 * Hierarchy: Organization → Project
 *
 * A user should only see projects in organizations they have access to,
 * unless they have a direct project-level grant.
 */
class ProjectAccessPermissionTest {
    private val policyCollector = mockk<PolicyCollector>()
    private val evaluator = DefaultPolicyEvaluator(policyCollector)

    private val tenantId = "acme-corp"
    private val principalUrn = "urn:revet:iam:$tenantId:user/${UUID.randomUUID()}"

    // Organizations
    private val orgAlphaUuid = UUID.randomUUID()
    private val orgBetaUuid = UUID.randomUUID()

    // Projects (two in each organization)
    private val project1InAlpha = createProject(id = 1L, organizationId = 10L)
    private val project2InAlpha = createProject(id = 2L, organizationId = 10L)
    private val project3InBeta = createProject(id = 3L, organizationId = 20L)
    private val project4InBeta = createProject(id = 4L, organizationId = 20L)

    private val orgUuidByOrgId = mapOf(10L to orgAlphaUuid, 20L to orgBetaUuid)
    private val allProjects = listOf(project1InAlpha, project2InAlpha, project3InBeta, project4InBeta)

    // ============================================================
    // Organization Permission Controls Project Visibility
    // ============================================================

    @Test
    fun `user with organization view permission can access that organization`() {
        val policy = orgViewPolicy(orgAlphaUuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
    }

    @Test
    fun `user without organization view permission is denied access`() {
        val policy = orgViewPolicy(orgAlphaUuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val result = evaluator.evaluate(orgGetRequest(orgBetaUuid))
        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
    }

    @Test
    fun `user with no policies is denied access to all organizations`() {
        every { policyCollector.collectPolicies(principalUrn) } returns emptyList()

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isDenied())
        assertTrue(evaluator.evaluate(orgGetRequest(orgBetaUuid)).isDenied())
    }

    @Test
    fun `user with multiple organization policies can access all granted organizations`() {
        val policies = listOf(orgViewPolicy(orgAlphaUuid), orgViewPolicy(orgBetaUuid))
        every { policyCollector.collectPolicies(principalUrn) } returns policies

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(orgBetaUuid)).isAllowed())
    }

    @Test
    fun `user with wildcard organization permission can access any organization`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "AllOrgsViewPolicy",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            effect = Effect.ALLOW,
                            actions = CoreActions.Organization.READ_ONLY,
                            resources = listOf("urn:revet:core:$tenantId:organization/*"),
                        ),
                    ),
            )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(orgBetaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(UUID.randomUUID())).isAllowed())
    }

    // ============================================================
    // Filtering Projects by Organization Permission
    // ============================================================

    @Test
    fun `only projects from permitted organizations are returned`() {
        val policies = listOf(orgViewPolicy(orgAlphaUuid))

        val accessibleProjects = filterProjectsByOrgAccess(allProjects, policies)

        assertEquals(2, accessibleProjects.size)
        assertTrue(accessibleProjects.all { it.organizationId == 10L })
        assertEquals(
            setOf(project1InAlpha.uuid, project2InAlpha.uuid),
            accessibleProjects.map { it.uuid }.toSet(),
        )
    }

    @Test
    fun `no projects are returned when user has no organization permissions`() {
        val accessibleProjects = filterProjectsByOrgAccess(allProjects, emptyList())

        assertTrue(accessibleProjects.isEmpty())
    }

    @Test
    fun `all projects are returned when user has wildcard organization permission`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "AllOrgsViewPolicy",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Organization.GET),
                            resources = listOf("urn:revet:core:$tenantId:organization/*"),
                        ),
                    ),
            )

        val accessibleProjects = filterProjectsByOrgAccess(allProjects, listOf(policy))

        assertEquals(4, accessibleProjects.size)
    }

    // ============================================================
    // Direct Project Permission Grants
    // ============================================================

    @Test
    fun `user granted permission to a specific project can access it`() {
        val policy = projectViewPolicy(project1InAlpha.uuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(projectGetRequest(project1InAlpha.uuid)).isAllowed())
    }

    @Test
    fun `user granted permission to a specific project cannot access other projects`() {
        val policy = projectViewPolicy(project1InAlpha.uuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(projectGetRequest(project1InAlpha.uuid)).isAllowed())
        assertTrue(evaluator.evaluate(projectGetRequest(project2InAlpha.uuid)).isDenied())
        assertTrue(evaluator.evaluate(projectGetRequest(project3InBeta.uuid)).isDenied())
        assertTrue(evaluator.evaluate(projectGetRequest(project4InBeta.uuid)).isDenied())
    }

    @Test
    fun `user with project permission but no organization permission can still access the project`() {
        val policy = projectViewPolicy(project3InBeta.uuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        // Organization access is denied
        assertTrue(evaluator.evaluate(orgGetRequest(orgBetaUuid)).isDenied())

        // But direct project access is allowed
        assertTrue(evaluator.evaluate(projectGetRequest(project3InBeta.uuid)).isAllowed())
    }

    @Test
    fun `user with multiple project grants can access all granted projects`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "MultiProjectViewPolicy",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Project.GET),
                            resources =
                                listOf(
                                    "urn:revet:core:$tenantId:project/${project1InAlpha.uuid}",
                                    "urn:revet:core:$tenantId:project/${project4InBeta.uuid}",
                                ),
                        ),
                    ),
            )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(projectGetRequest(project1InAlpha.uuid)).isAllowed())
        assertTrue(evaluator.evaluate(projectGetRequest(project4InBeta.uuid)).isAllowed())
        assertTrue(evaluator.evaluate(projectGetRequest(project2InAlpha.uuid)).isDenied())
        assertTrue(evaluator.evaluate(projectGetRequest(project3InBeta.uuid)).isDenied())
    }

    // ============================================================
    // Combined Organization + Project Filtering
    // ============================================================

    @Test
    fun `filtering combines organization access and direct project grants`() {
        val policies =
            listOf(
                orgViewPolicy(orgAlphaUuid),
                projectViewPolicy(project3InBeta.uuid),
            )

        val accessibleProjects = filterProjectsByAccess(allProjects, policies)

        assertEquals(3, accessibleProjects.size)
        val accessibleUuids = accessibleProjects.map { it.uuid }.toSet()
        assertTrue(project1InAlpha.uuid in accessibleUuids, "project1 should be accessible via org Alpha")
        assertTrue(project2InAlpha.uuid in accessibleUuids, "project2 should be accessible via org Alpha")
        assertTrue(project3InBeta.uuid in accessibleUuids, "project3 should be accessible via direct grant")
        assertFalse(project4InBeta.uuid in accessibleUuids, "project4 should not be accessible")
    }

    @Test
    fun `direct project grant does not leak access to other projects in the same organization`() {
        val policies = listOf(projectViewPolicy(project3InBeta.uuid))

        val accessibleProjects = filterProjectsByAccess(allProjects, policies)

        assertEquals(1, accessibleProjects.size)
        assertEquals(project3InBeta.uuid, accessibleProjects[0].uuid)
    }

    @Test
    fun `explicit deny on project overrides organization-level allow`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "OrgAllowWithProjectDeny",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            sid = "AllowOrgAlpha",
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Organization.GET),
                            resources = listOf("urn:revet:core:$tenantId:organization/$orgAlphaUuid"),
                        ),
                        Statement(
                            sid = "AllowAllProjects",
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Project.GET),
                            resources = listOf("urn:revet:core:$tenantId:project/*"),
                        ),
                        Statement(
                            sid = "DenyConfidentialProject",
                            effect = Effect.DENY,
                            actions = listOf(CoreActions.Project.GET),
                            resources = listOf("urn:revet:core:$tenantId:project/${project1InAlpha.uuid}"),
                        ),
                    ),
            )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(projectGetRequest(project2InAlpha.uuid)).isAllowed())

        val result = evaluator.evaluate(projectGetRequest(project1InAlpha.uuid))
        assertTrue(result.isDenied())
        assertTrue(result.isExplicitDeny)
    }

    @Test
    fun `explicit deny on project is excluded from combined filtering`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "OrgAllowWithProjectDeny",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Organization.GET),
                            resources = listOf("urn:revet:core:$tenantId:organization/$orgAlphaUuid"),
                        ),
                        Statement(
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Project.GET),
                            resources = listOf("urn:revet:core:$tenantId:project/*"),
                        ),
                        Statement(
                            effect = Effect.DENY,
                            actions = listOf(CoreActions.Project.GET),
                            resources = listOf("urn:revet:core:$tenantId:project/${project1InAlpha.uuid}"),
                        ),
                    ),
            )
        val policies = listOf(policy)

        val accessibleProjects = filterProjectsByAccess(allProjects, policies)

        val accessibleUuids = accessibleProjects.map { it.uuid }.toSet()
        assertFalse(project1InAlpha.uuid in accessibleUuids, "project1 should be excluded by explicit deny")
        assertTrue(project2InAlpha.uuid in accessibleUuids, "project2 should be accessible via org Alpha")
    }

    // ============================================================
    // Helpers
    // ============================================================

    companion object {
        const val POLICY_VERSION = "2026-01-01"
    }

    private fun filterProjectsByOrgAccess(
        projects: List<Project>,
        policies: List<Policy>,
    ): List<Project> =
        projects.filter { project ->
            val orgUuid = orgUuidByOrgId[project.organizationId]!!
            evaluator.evaluateWithPolicies(orgGetRequest(orgUuid), policies).isAllowed()
        }

    private fun filterProjectsByAccess(
        projects: List<Project>,
        policies: List<Policy>,
    ): List<Project> =
        projects.filter { project ->
            val projectResult =
                evaluator.evaluateWithPolicies(
                    projectGetRequest(project.uuid),
                    policies,
                )

            if (projectResult.isExplicitDeny) return@filter false
            if (projectResult.isAllowed()) return@filter true

            val orgUuid = orgUuidByOrgId[project.organizationId]!!
            evaluator.evaluateWithPolicies(orgGetRequest(orgUuid), policies).isAllowed()
        }

    private fun orgGetRequest(orgUuid: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = CoreActions.Organization.GET,
            resourceUrn = "urn:revet:core:$tenantId:organization/$orgUuid",
        )

    private fun projectGetRequest(projectUuid: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = CoreActions.Project.GET,
            resourceUrn = "urn:revet:core:$tenantId:project/$projectUuid",
        )

    private fun orgViewPolicy(orgUuid: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "OrgViewPolicy-$orgUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        effect = Effect.ALLOW,
                        actions = CoreActions.Organization.READ_ONLY,
                        resources = listOf("urn:revet:core:$tenantId:organization/$orgUuid"),
                    ),
                ),
        )

    private fun projectViewPolicy(projectUuid: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "ProjectViewPolicy-$projectUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements =
                listOf(
                    Statement(
                        effect = Effect.ALLOW,
                        actions = CoreActions.Project.READ_ONLY,
                        resources = listOf("urn:revet:core:$tenantId:project/$projectUuid"),
                    ),
                ),
        )

    private fun createProject(
        id: Long,
        organizationId: Long,
    ): Project {
        val now = LocalDateTime.now()
        return Project(
            id = id,
            uuid = UUID.randomUUID(),
            name = "Project $id",
            description = null,
            organizationId = organizationId,
            clientIds = emptySet(),
            tags = emptySet(),
            isActive = true,
            timestamps =
                Project.Timestamps(
                    createdAt = now,
                    modifiedAt = now,
                    removedAt = null,
                ),
        )
    }
}
