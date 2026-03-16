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
import com.revethq.iam.permission.discovery.Actions as IamActions

/**
 * Tests verifying that group management access is controlled by IAM-level
 * permission grants, including group member management actions.
 *
 * Groups are tenant-scoped resources in the `iam` service.
 */
class GroupAccessPermissionTest {
    private val policyCollector = mockk<PolicyCollector>()
    private val evaluator = DefaultPolicyEvaluator(policyCollector)

    private val tenantId = "acme-corp"
    private val principalUrn = "urn:revet:iam:$tenantId:user/${UUID.randomUUID()}"

    // Groups
    private val groupAdminsId = UUID.randomUUID()
    private val groupEditorsId = UUID.randomUUID()
    private val groupViewersId = UUID.randomUUID()
    private val groupAuditorsId = UUID.randomUUID()

    private val allGroupIds = listOf(groupAdminsId, groupEditorsId, groupViewersId, groupAuditorsId)

    // ============================================================
    // Basic Group Access Evaluation
    // ============================================================

    @Test
    fun `user with group view permission can access that group`() {
        val policy = groupViewPolicy(groupAdminsId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(groupGetRequest(groupAdminsId)).isAllowed())
    }

    @Test
    fun `user without group view permission is denied access`() {
        val policy = groupViewPolicy(groupAdminsId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val result = evaluator.evaluate(groupGetRequest(groupEditorsId))
        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
    }

    @Test
    fun `user with no policies is denied access to all groups`() {
        every { policyCollector.collectPolicies(principalUrn) } returns emptyList()

        allGroupIds.forEach { groupId ->
            assertTrue(evaluator.evaluate(groupGetRequest(groupId)).isDenied())
        }
    }

    @Test
    fun `user with multiple group policies can access all granted groups`() {
        val policies = listOf(
            groupViewPolicy(groupAdminsId),
            groupViewPolicy(groupViewersId),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns policies

        assertTrue(evaluator.evaluate(groupGetRequest(groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupGetRequest(groupViewersId)).isAllowed())
        assertTrue(evaluator.evaluate(groupGetRequest(groupEditorsId)).isDenied())
        assertTrue(evaluator.evaluate(groupGetRequest(groupAuditorsId)).isDenied())
    }

    @Test
    fun `user with wildcard group permission can access any group`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllGroupsViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = IamActions.Group.READ_ONLY,
                    resources = listOf("urn:revet:iam:$tenantId:group/*"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        allGroupIds.forEach { groupId ->
            assertTrue(evaluator.evaluate(groupGetRequest(groupId)).isAllowed())
        }
        assertTrue(evaluator.evaluate(groupGetRequest(UUID.randomUUID())).isAllowed())
    }

    @Test
    fun `service wildcard grants all group actions`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "IamWildcardPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(IamActions.ALL_ACTIONS),
                    resources = listOf("urn:revet:iam:$tenantId:group/*"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(groupGetRequest(groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupListRequest()).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.UPDATE, groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.DELETE, groupAdminsId)).isAllowed())
    }

    // ============================================================
    // Group Member Management Permissions
    // ============================================================

    @Test
    fun `group view permission grants list members but not add or remove`() {
        val policy = groupViewPolicy(groupAdminsId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(groupGetRequest(groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.LIST_MEMBERS, groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.ADD_MEMBER, groupAdminsId)).isDenied())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.REMOVE_MEMBER, groupAdminsId)).isDenied())
    }

    @Test
    fun `group manager can manage members`() {
        val policy = groupManagerPolicy(groupAdminsId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(groupGetRequest(groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.LIST_MEMBERS, groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.ADD_MEMBER, groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.REMOVE_MEMBER, groupAdminsId)).isAllowed())
    }

    @Test
    fun `group manager permission on one group does not grant member management on another`() {
        val policy = groupManagerPolicy(groupAdminsId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.ADD_MEMBER, groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupRequest(IamActions.Group.ADD_MEMBER, groupEditorsId)).isDenied())
    }

    // ============================================================
    // Filtering Groups
    // ============================================================

    @Test
    fun `only permitted groups are returned when filtering`() {
        val policies = listOf(
            groupViewPolicy(groupAdminsId),
            groupViewPolicy(groupAuditorsId),
        )

        val accessibleGroups = filterGroups(allGroupIds, policies)

        assertEquals(2, accessibleGroups.size)
        assertEquals(setOf(groupAdminsId, groupAuditorsId), accessibleGroups.toSet())
    }

    @Test
    fun `no groups are returned when user has no permissions`() {
        val accessibleGroups = filterGroups(allGroupIds, emptyList())

        assertTrue(accessibleGroups.isEmpty())
    }

    @Test
    fun `all groups are returned when user has wildcard permission`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllGroupsViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(IamActions.Group.GET),
                    resources = listOf("urn:revet:iam:$tenantId:group/*"),
                ),
            ),
        )

        val accessibleGroups = filterGroups(allGroupIds, listOf(policy))

        assertEquals(4, accessibleGroups.size)
    }

    // ============================================================
    // Direct Group Grants
    // ============================================================

    @Test
    fun `user granted permission to specific groups can access only those`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "MultiGroupViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(IamActions.Group.GET),
                    resources = listOf(
                        "urn:revet:iam:$tenantId:group/$groupAdminsId",
                        "urn:revet:iam:$tenantId:group/$groupEditorsId",
                    ),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(groupGetRequest(groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupGetRequest(groupEditorsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupGetRequest(groupViewersId)).isDenied())
        assertTrue(evaluator.evaluate(groupGetRequest(groupAuditorsId)).isDenied())
    }

    // ============================================================
    // Deny Overrides
    // ============================================================

    @Test
    fun `explicit deny on group overrides wildcard allow`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllowAllDenyOnePolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    sid = "AllowAllGroups",
                    effect = Effect.ALLOW,
                    actions = IamActions.Group.READ_ONLY,
                    resources = listOf("urn:revet:iam:$tenantId:group/*"),
                ),
                Statement(
                    sid = "DenyAuditorsGroup",
                    effect = Effect.DENY,
                    actions = listOf(IamActions.Group.GET),
                    resources = listOf("urn:revet:iam:$tenantId:group/$groupAuditorsId"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(groupGetRequest(groupAdminsId)).isAllowed())
        assertTrue(evaluator.evaluate(groupGetRequest(groupEditorsId)).isAllowed())

        val result = evaluator.evaluate(groupGetRequest(groupAuditorsId))
        assertTrue(result.isDenied())
        assertTrue(result.isExplicitDeny)
    }

    @Test
    fun `explicit deny is excluded from filtered results`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllowAllDenyOnePolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(IamActions.Group.GET),
                    resources = listOf("urn:revet:iam:$tenantId:group/*"),
                ),
                Statement(
                    effect = Effect.DENY,
                    actions = listOf(IamActions.Group.GET),
                    resources = listOf("urn:revet:iam:$tenantId:group/$groupAuditorsId"),
                ),
            ),
        )

        val accessibleGroups = filterGroups(allGroupIds, listOf(policy))

        assertEquals(3, accessibleGroups.size)
        assertFalse(groupAuditorsId in accessibleGroups, "auditors group should be excluded by explicit deny")
    }

    // ============================================================
    // Helpers
    // ============================================================

    companion object {
        const val POLICY_VERSION = "2026-01-01"
    }

    private fun filterGroups(
        groupIds: List<UUID>,
        policies: List<Policy>,
    ): List<UUID> =
        groupIds.filter { groupId ->
            evaluator.evaluateWithPolicies(groupGetRequest(groupId), policies).isAllowed()
        }

    private fun groupGetRequest(groupId: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = IamActions.Group.GET,
            resourceUrn = "urn:revet:iam:$tenantId:group/$groupId",
        )

    private fun groupListRequest() =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = IamActions.Group.LIST,
            resourceUrn = "urn:revet:iam:$tenantId:group/*",
        )

    private fun groupRequest(action: String, groupId: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = "urn:revet:iam:$tenantId:group/$groupId",
        )

    private fun groupViewPolicy(groupId: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "GroupViewPolicy-$groupId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = IamActions.Group.READ_ONLY,
                    resources = listOf("urn:revet:iam:$tenantId:group/$groupId"),
                ),
            ),
        )

    private fun groupManagerPolicy(groupId: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "GroupManagerPolicy-$groupId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = IamActions.Group.ALL,
                    resources = listOf("urn:revet:iam:$tenantId:group/$groupId"),
                ),
            ),
        )
}
