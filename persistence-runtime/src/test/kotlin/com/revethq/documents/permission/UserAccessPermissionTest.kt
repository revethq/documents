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
 * Tests verifying that user management access is controlled by IAM-level
 * permission grants.
 *
 * Users are tenant-scoped resources in the `iam` service.
 */
class UserAccessPermissionTest {
    private val policyCollector = mockk<PolicyCollector>()
    private val evaluator = DefaultPolicyEvaluator(policyCollector)

    private val tenantId = "acme-corp"
    private val principalUrn = "urn:revet:iam:$tenantId:user/${UUID.randomUUID()}"

    // Target users (the users being accessed, not the principal)
    private val userAliceId = UUID.randomUUID()
    private val userBobId = UUID.randomUUID()
    private val userCharlieId = UUID.randomUUID()
    private val userDianaId = UUID.randomUUID()

    private val allUserIds = listOf(userAliceId, userBobId, userCharlieId, userDianaId)

    // ============================================================
    // Basic User Access Evaluation
    // ============================================================

    @Test
    fun `user with user view permission can access that user`() {
        val policy = userViewPolicy(userAliceId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(userGetRequest(userAliceId)).isAllowed())
    }

    @Test
    fun `user without user view permission is denied access`() {
        val policy = userViewPolicy(userAliceId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val result = evaluator.evaluate(userGetRequest(userBobId))
        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
    }

    @Test
    fun `user with no policies is denied access to all users`() {
        every { policyCollector.collectPolicies(principalUrn) } returns emptyList()

        allUserIds.forEach { userId ->
            assertTrue(evaluator.evaluate(userGetRequest(userId)).isDenied())
        }
    }

    @Test
    fun `user with multiple user policies can access all granted users`() {
        val policies = listOf(
            userViewPolicy(userAliceId),
            userViewPolicy(userCharlieId),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns policies

        assertTrue(evaluator.evaluate(userGetRequest(userAliceId)).isAllowed())
        assertTrue(evaluator.evaluate(userGetRequest(userCharlieId)).isAllowed())
        assertTrue(evaluator.evaluate(userGetRequest(userBobId)).isDenied())
        assertTrue(evaluator.evaluate(userGetRequest(userDianaId)).isDenied())
    }

    @Test
    fun `user with wildcard user permission can access any user`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllUsersViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = IamActions.User.READ_ONLY,
                    resources = listOf("urn:revet:iam:$tenantId:user/*"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        allUserIds.forEach { userId ->
            assertTrue(evaluator.evaluate(userGetRequest(userId)).isAllowed())
        }
        assertTrue(evaluator.evaluate(userGetRequest(UUID.randomUUID())).isAllowed())
    }

    @Test
    fun `service wildcard grants all user actions`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "IamWildcardPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(IamActions.ALL_ACTIONS),
                    resources = listOf("urn:revet:iam:$tenantId:user/*"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(userGetRequest(userAliceId)).isAllowed())
        assertTrue(evaluator.evaluate(userListRequest()).isAllowed())
        assertTrue(evaluator.evaluate(userRequest(IamActions.User.UPDATE, userAliceId)).isAllowed())
        assertTrue(evaluator.evaluate(userRequest(IamActions.User.DELETE, userAliceId)).isAllowed())
    }

    @Test
    fun `read-only user permission does not grant write actions`() {
        val policy = userViewPolicy(userAliceId)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(userGetRequest(userAliceId)).isAllowed())
        assertTrue(evaluator.evaluate(userRequest(IamActions.User.UPDATE, userAliceId)).isDenied())
        assertTrue(evaluator.evaluate(userRequest(IamActions.User.DELETE, userAliceId)).isDenied())
        assertTrue(evaluator.evaluate(userRequest(IamActions.User.CREATE, userAliceId)).isDenied())
    }

    // ============================================================
    // Filtering Users
    // ============================================================

    @Test
    fun `only permitted users are returned when filtering`() {
        val policies = listOf(
            userViewPolicy(userAliceId),
            userViewPolicy(userDianaId),
        )

        val accessibleUsers = filterUsers(allUserIds, policies)

        assertEquals(2, accessibleUsers.size)
        assertEquals(setOf(userAliceId, userDianaId), accessibleUsers.toSet())
    }

    @Test
    fun `no users are returned when user has no permissions`() {
        val accessibleUsers = filterUsers(allUserIds, emptyList())

        assertTrue(accessibleUsers.isEmpty())
    }

    @Test
    fun `all users are returned when user has wildcard permission`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllUsersViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(IamActions.User.GET),
                    resources = listOf("urn:revet:iam:$tenantId:user/*"),
                ),
            ),
        )

        val accessibleUsers = filterUsers(allUserIds, listOf(policy))

        assertEquals(4, accessibleUsers.size)
    }

    // ============================================================
    // Direct User Grants
    // ============================================================

    @Test
    fun `user granted permission to specific users can access only those`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "MultiUserViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(IamActions.User.GET),
                    resources = listOf(
                        "urn:revet:iam:$tenantId:user/$userAliceId",
                        "urn:revet:iam:$tenantId:user/$userBobId",
                    ),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(userGetRequest(userAliceId)).isAllowed())
        assertTrue(evaluator.evaluate(userGetRequest(userBobId)).isAllowed())
        assertTrue(evaluator.evaluate(userGetRequest(userCharlieId)).isDenied())
        assertTrue(evaluator.evaluate(userGetRequest(userDianaId)).isDenied())
    }

    // ============================================================
    // Deny Overrides
    // ============================================================

    @Test
    fun `explicit deny on user overrides wildcard allow`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllowAllDenyOnePolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    sid = "AllowAllUsers",
                    effect = Effect.ALLOW,
                    actions = IamActions.User.READ_ONLY,
                    resources = listOf("urn:revet:iam:$tenantId:user/*"),
                ),
                Statement(
                    sid = "DenySpecificUser",
                    effect = Effect.DENY,
                    actions = listOf(IamActions.User.GET),
                    resources = listOf("urn:revet:iam:$tenantId:user/$userCharlieId"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(userGetRequest(userAliceId)).isAllowed())
        assertTrue(evaluator.evaluate(userGetRequest(userBobId)).isAllowed())

        val result = evaluator.evaluate(userGetRequest(userCharlieId))
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
                    actions = listOf(IamActions.User.GET),
                    resources = listOf("urn:revet:iam:$tenantId:user/*"),
                ),
                Statement(
                    effect = Effect.DENY,
                    actions = listOf(IamActions.User.GET),
                    resources = listOf("urn:revet:iam:$tenantId:user/$userCharlieId"),
                ),
            ),
        )

        val accessibleUsers = filterUsers(allUserIds, listOf(policy))

        assertEquals(3, accessibleUsers.size)
        assertFalse(userCharlieId in accessibleUsers, "charlie should be excluded by explicit deny")
    }

    // ============================================================
    // Helpers
    // ============================================================

    companion object {
        const val POLICY_VERSION = "2026-01-01"
    }

    private fun filterUsers(
        userIds: List<UUID>,
        policies: List<Policy>,
    ): List<UUID> =
        userIds.filter { userId ->
            evaluator.evaluateWithPolicies(userGetRequest(userId), policies).isAllowed()
        }

    private fun userGetRequest(userId: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = IamActions.User.GET,
            resourceUrn = "urn:revet:iam:$tenantId:user/$userId",
        )

    private fun userListRequest() =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = IamActions.User.LIST,
            resourceUrn = "urn:revet:iam:$tenantId:user/*",
        )

    private fun userRequest(action: String, userId: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = "urn:revet:iam:$tenantId:user/$userId",
        )

    private fun userViewPolicy(userId: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "UserViewPolicy-$userId",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = IamActions.User.READ_ONLY,
                    resources = listOf("urn:revet:iam:$tenantId:user/$userId"),
                ),
            ),
        )
}
