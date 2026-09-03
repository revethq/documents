package com.revethq.documents.permission

import com.revethq.iam.permission.domain.Effect
import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.domain.Statement
import com.revethq.iam.permission.evaluation.AuthorizationRequest
import com.revethq.iam.permission.evaluation.DefaultPolicyEvaluator
import com.revethq.iam.permission.evaluation.PolicyCollector
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.revethq.core.permission.Actions as CoreActions

/**
 * Tests verifying that organization access is controlled by organization-level
 * permission grants.
 *
 * Organizations are top-level resources with no parent hierarchy.
 */
class OrganizationAccessPermissionTest {
    private val policyCollector = mockk<PolicyCollector>()
    private val evaluator = DefaultPolicyEvaluator(policyCollector)

    private val tenantId = "acme-corp"
    private val principalUrn = "urn:revet:iam:$tenantId:user/${UUID.randomUUID()}"

    // Organizations
    private val orgAlphaUuid = UUID.randomUUID()
    private val orgBetaUuid = UUID.randomUUID()
    private val orgGammaUuid = UUID.randomUUID()
    private val orgDeltaUuid = UUID.randomUUID()

    private val allOrgUuids = listOf(orgAlphaUuid, orgBetaUuid, orgGammaUuid, orgDeltaUuid)

    // ============================================================
    // Basic Organization Access Evaluation
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

        allOrgUuids.forEach { orgUuid ->
            assertTrue(evaluator.evaluate(orgGetRequest(orgUuid)).isDenied())
        }
    }

    @Test
    fun `user with multiple organization policies can access all granted organizations`() {
        val policies =
            listOf(
                orgViewPolicy(orgAlphaUuid),
                orgViewPolicy(orgGammaUuid),
            )
        every { policyCollector.collectPolicies(principalUrn) } returns policies

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(orgGammaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(orgBetaUuid)).isDenied())
        assertTrue(evaluator.evaluate(orgGetRequest(orgDeltaUuid)).isDenied())
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

        allOrgUuids.forEach { orgUuid ->
            assertTrue(evaluator.evaluate(orgGetRequest(orgUuid)).isAllowed())
        }
        assertTrue(evaluator.evaluate(orgGetRequest(UUID.randomUUID())).isAllowed())
    }

    @Test
    fun `service wildcard grants all organization actions`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "CoreWildcardPolicy",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.ALL_ACTIONS),
                            resources = listOf("urn:revet:core:$tenantId:organization/*"),
                        ),
                    ),
            )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgListRequest()).isAllowed())
        assertTrue(evaluator.evaluate(orgRequest(CoreActions.Organization.UPDATE, orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgRequest(CoreActions.Organization.DELETE, orgAlphaUuid)).isAllowed())
    }

    // ============================================================
    // Filtering Organizations
    // ============================================================

    @Test
    fun `only permitted organizations are returned when filtering`() {
        val policies =
            listOf(
                orgViewPolicy(orgAlphaUuid),
                orgViewPolicy(orgGammaUuid),
            )

        val accessibleOrgs = filterOrganizations(allOrgUuids, policies)

        assertEquals(2, accessibleOrgs.size)
        assertEquals(setOf(orgAlphaUuid, orgGammaUuid), accessibleOrgs.toSet())
    }

    @Test
    fun `no organizations are returned when user has no permissions`() {
        val accessibleOrgs = filterOrganizations(allOrgUuids, emptyList())

        assertTrue(accessibleOrgs.isEmpty())
    }

    @Test
    fun `all organizations are returned when user has wildcard permission`() {
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

        val accessibleOrgs = filterOrganizations(allOrgUuids, listOf(policy))

        assertEquals(4, accessibleOrgs.size)
    }

    // ============================================================
    // Direct Organization Grants
    // ============================================================

    @Test
    fun `user granted permission to specific organizations can access only those`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "MultiOrgViewPolicy",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Organization.GET),
                            resources =
                                listOf(
                                    "urn:revet:core:$tenantId:organization/$orgAlphaUuid",
                                    "urn:revet:core:$tenantId:organization/$orgDeltaUuid",
                                ),
                        ),
                    ),
            )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(orgDeltaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(orgBetaUuid)).isDenied())
        assertTrue(evaluator.evaluate(orgGetRequest(orgGammaUuid)).isDenied())
    }

    // ============================================================
    // Deny Overrides
    // ============================================================

    @Test
    fun `explicit deny on organization overrides wildcard allow`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "AllowAllDenyOnePolicy",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            sid = "AllowAllOrgs",
                            effect = Effect.ALLOW,
                            actions = CoreActions.Organization.READ_ONLY,
                            resources = listOf("urn:revet:core:$tenantId:organization/*"),
                        ),
                        Statement(
                            sid = "DenySpecificOrg",
                            effect = Effect.DENY,
                            actions = listOf(CoreActions.Organization.GET),
                            resources = listOf("urn:revet:core:$tenantId:organization/$orgBetaUuid"),
                        ),
                    ),
            )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(orgGetRequest(orgAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(orgGetRequest(orgGammaUuid)).isAllowed())

        val result = evaluator.evaluate(orgGetRequest(orgBetaUuid))
        assertTrue(result.isDenied())
        assertTrue(result.isExplicitDeny)
    }

    @Test
    fun `explicit deny is excluded from filtered results`() {
        val policy =
            Policy(
                id = UUID.randomUUID(),
                name = "AllowAllDenyOnePolicy",
                version = POLICY_VERSION,
                tenantId = tenantId,
                statements =
                    listOf(
                        Statement(
                            effect = Effect.ALLOW,
                            actions = listOf(CoreActions.Organization.GET),
                            resources = listOf("urn:revet:core:$tenantId:organization/*"),
                        ),
                        Statement(
                            effect = Effect.DENY,
                            actions = listOf(CoreActions.Organization.GET),
                            resources = listOf("urn:revet:core:$tenantId:organization/$orgBetaUuid"),
                        ),
                    ),
            )

        val accessibleOrgs = filterOrganizations(allOrgUuids, listOf(policy))

        assertEquals(3, accessibleOrgs.size)
        assertFalse(orgBetaUuid in accessibleOrgs, "orgBeta should be excluded by explicit deny")
        assertTrue(orgAlphaUuid in accessibleOrgs)
        assertTrue(orgGammaUuid in accessibleOrgs)
        assertTrue(orgDeltaUuid in accessibleOrgs)
    }

    // ============================================================
    // Helpers
    // ============================================================

    companion object {
        const val POLICY_VERSION = "2026-01-01"
    }

    private fun filterOrganizations(
        orgUuids: List<UUID>,
        policies: List<Policy>,
    ): List<UUID> =
        orgUuids.filter { orgUuid ->
            evaluator.evaluateWithPolicies(orgGetRequest(orgUuid), policies).isAllowed()
        }

    private fun orgGetRequest(orgUuid: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = CoreActions.Organization.GET,
            resourceUrn = "urn:revet:core:$tenantId:organization/$orgUuid",
        )

    private fun orgListRequest() =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = CoreActions.Organization.LIST,
            resourceUrn = "urn:revet:core:$tenantId:organization/*",
        )

    private fun orgRequest(
        action: String,
        orgUuid: UUID,
    ) = AuthorizationRequest(
        principalUrn = principalUrn,
        action = action,
        resourceUrn = "urn:revet:core:$tenantId:organization/$orgUuid",
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
}
