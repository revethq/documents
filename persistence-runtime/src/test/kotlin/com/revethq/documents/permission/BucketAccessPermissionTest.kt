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
import com.revethq.buckets.permission.Actions as BucketActions

/**
 * Tests verifying that bucket access is controlled by bucket-level
 * permission grants.
 *
 * Buckets are top-level tenant-scoped resources in the `buckets` service.
 */
class BucketAccessPermissionTest {
    private val policyCollector = mockk<PolicyCollector>()
    private val evaluator = DefaultPolicyEvaluator(policyCollector)

    private val tenantId = "acme-corp"
    private val principalUrn = "urn:revet:iam:$tenantId:user/${UUID.randomUUID()}"

    // Buckets
    private val bucketPrimaryUuid = UUID.randomUUID()
    private val bucketArchiveUuid = UUID.randomUUID()
    private val bucketStagingUuid = UUID.randomUUID()
    private val bucketBackupUuid = UUID.randomUUID()

    private val allBucketUuids = listOf(bucketPrimaryUuid, bucketArchiveUuid, bucketStagingUuid, bucketBackupUuid)

    // ============================================================
    // Basic Bucket Access Evaluation
    // ============================================================

    @Test
    fun `user with bucket view permission can access that bucket`() {
        val policy = bucketViewPolicy(bucketPrimaryUuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(bucketGetRequest(bucketPrimaryUuid)).isAllowed())
    }

    @Test
    fun `user without bucket view permission is denied access`() {
        val policy = bucketViewPolicy(bucketPrimaryUuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val result = evaluator.evaluate(bucketGetRequest(bucketArchiveUuid))
        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
    }

    @Test
    fun `user with no policies is denied access to all buckets`() {
        every { policyCollector.collectPolicies(principalUrn) } returns emptyList()

        allBucketUuids.forEach { bucketUuid ->
            assertTrue(evaluator.evaluate(bucketGetRequest(bucketUuid)).isDenied())
        }
    }

    @Test
    fun `user with multiple bucket policies can access all granted buckets`() {
        val policies = listOf(
            bucketViewPolicy(bucketPrimaryUuid),
            bucketViewPolicy(bucketStagingUuid),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns policies

        assertTrue(evaluator.evaluate(bucketGetRequest(bucketPrimaryUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketGetRequest(bucketStagingUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketGetRequest(bucketArchiveUuid)).isDenied())
        assertTrue(evaluator.evaluate(bucketGetRequest(bucketBackupUuid)).isDenied())
    }

    @Test
    fun `user with wildcard bucket permission can access any bucket`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllBucketsViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = BucketActions.Bucket.READ_ONLY,
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/*"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        allBucketUuids.forEach { bucketUuid ->
            assertTrue(evaluator.evaluate(bucketGetRequest(bucketUuid)).isAllowed())
        }
        assertTrue(evaluator.evaluate(bucketGetRequest(UUID.randomUUID())).isAllowed())
    }

    @Test
    fun `service wildcard grants all bucket actions`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "BucketsWildcardPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(BucketActions.ALL_ACTIONS),
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/*"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(bucketGetRequest(bucketPrimaryUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketListRequest()).isAllowed())
        assertTrue(evaluator.evaluate(bucketRequest(BucketActions.Bucket.UPDATE, bucketPrimaryUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketRequest(BucketActions.Bucket.DELETE, bucketPrimaryUuid)).isAllowed())
    }

    @Test
    fun `read-only bucket permission does not grant write actions`() {
        val policy = bucketViewPolicy(bucketPrimaryUuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(bucketGetRequest(bucketPrimaryUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketRequest(BucketActions.Bucket.UPDATE, bucketPrimaryUuid)).isDenied())
        assertTrue(evaluator.evaluate(bucketRequest(BucketActions.Bucket.DELETE, bucketPrimaryUuid)).isDenied())
        assertTrue(evaluator.evaluate(bucketRequest(BucketActions.Bucket.CREATE, bucketPrimaryUuid)).isDenied())
    }

    // ============================================================
    // Filtering Buckets
    // ============================================================

    @Test
    fun `only permitted buckets are returned when filtering`() {
        val policies = listOf(
            bucketViewPolicy(bucketPrimaryUuid),
            bucketViewPolicy(bucketArchiveUuid),
        )

        val accessibleBuckets = filterBuckets(allBucketUuids, policies)

        assertEquals(2, accessibleBuckets.size)
        assertEquals(setOf(bucketPrimaryUuid, bucketArchiveUuid), accessibleBuckets.toSet())
    }

    @Test
    fun `no buckets are returned when user has no permissions`() {
        val accessibleBuckets = filterBuckets(allBucketUuids, emptyList())

        assertTrue(accessibleBuckets.isEmpty())
    }

    @Test
    fun `all buckets are returned when user has wildcard permission`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllBucketsViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(BucketActions.Bucket.GET),
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/*"),
                ),
            ),
        )

        val accessibleBuckets = filterBuckets(allBucketUuids, listOf(policy))

        assertEquals(4, accessibleBuckets.size)
    }

    // ============================================================
    // Direct Bucket Grants
    // ============================================================

    @Test
    fun `user granted permission to specific buckets can access only those`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "MultiBucketViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(BucketActions.Bucket.GET),
                    resources = listOf(
                        "urn:revet:buckets:$tenantId:bucket/$bucketPrimaryUuid",
                        "urn:revet:buckets:$tenantId:bucket/$bucketBackupUuid",
                    ),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(bucketGetRequest(bucketPrimaryUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketGetRequest(bucketBackupUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketGetRequest(bucketArchiveUuid)).isDenied())
        assertTrue(evaluator.evaluate(bucketGetRequest(bucketStagingUuid)).isDenied())
    }

    // ============================================================
    // Deny Overrides
    // ============================================================

    @Test
    fun `explicit deny on bucket overrides wildcard allow`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllowAllDenyOnePolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    sid = "AllowAllBuckets",
                    effect = Effect.ALLOW,
                    actions = BucketActions.Bucket.READ_ONLY,
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/*"),
                ),
                Statement(
                    sid = "DenyBackupBucket",
                    effect = Effect.DENY,
                    actions = listOf(BucketActions.Bucket.GET),
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/$bucketBackupUuid"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(bucketGetRequest(bucketPrimaryUuid)).isAllowed())
        assertTrue(evaluator.evaluate(bucketGetRequest(bucketArchiveUuid)).isAllowed())

        val result = evaluator.evaluate(bucketGetRequest(bucketBackupUuid))
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
                    actions = listOf(BucketActions.Bucket.GET),
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/*"),
                ),
                Statement(
                    effect = Effect.DENY,
                    actions = listOf(BucketActions.Bucket.GET),
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/$bucketBackupUuid"),
                ),
            ),
        )

        val accessibleBuckets = filterBuckets(allBucketUuids, listOf(policy))

        assertEquals(3, accessibleBuckets.size)
        assertFalse(bucketBackupUuid in accessibleBuckets, "backup bucket should be excluded by explicit deny")
    }

    // ============================================================
    // Helpers
    // ============================================================

    companion object {
        const val POLICY_VERSION = "2026-01-01"
    }

    private fun filterBuckets(
        bucketUuids: List<UUID>,
        policies: List<Policy>,
    ): List<UUID> =
        bucketUuids.filter { bucketUuid ->
            evaluator.evaluateWithPolicies(bucketGetRequest(bucketUuid), policies).isAllowed()
        }

    private fun bucketGetRequest(bucketUuid: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = BucketActions.Bucket.GET,
            resourceUrn = "urn:revet:buckets:$tenantId:bucket/$bucketUuid",
        )

    private fun bucketListRequest() =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = BucketActions.Bucket.LIST,
            resourceUrn = "urn:revet:buckets:$tenantId:bucket/*",
        )

    private fun bucketRequest(action: String, bucketUuid: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = action,
            resourceUrn = "urn:revet:buckets:$tenantId:bucket/$bucketUuid",
        )

    private fun bucketViewPolicy(bucketUuid: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "BucketViewPolicy-$bucketUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = BucketActions.Bucket.READ_ONLY,
                    resources = listOf("urn:revet:buckets:$tenantId:bucket/$bucketUuid"),
                ),
            ),
        )
}
