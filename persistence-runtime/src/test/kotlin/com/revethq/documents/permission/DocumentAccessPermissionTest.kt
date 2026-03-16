package com.revethq.documents.permission

import com.revethq.documents.domain.Document
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
 * Tests verifying that the permission model correctly controls document visibility
 * based on project-level and document-level permission grants.
 *
 * Uses [DefaultPolicyEvaluator] with real action/resource matching to validate
 * that policies are evaluated correctly for document access scenarios.
 */
class DocumentAccessPermissionTest {
    private val policyCollector = mockk<PolicyCollector>()
    private val evaluator = DefaultPolicyEvaluator(policyCollector)

    private val tenantId = "acme-corp"
    private val principalUrn = "urn:revet:iam:$tenantId:user/${UUID.randomUUID()}"

    // Projects
    private val projectAlphaUuid = UUID.randomUUID()
    private val projectBetaUuid = UUID.randomUUID()

    // Documents (two in each project)
    private val doc1InAlpha = createDocument(id = 1L, projectId = 100L)
    private val doc2InAlpha = createDocument(id = 2L, projectId = 100L)
    private val doc3InBeta = createDocument(id = 3L, projectId = 200L)
    private val doc4InBeta = createDocument(id = 4L, projectId = 200L)

    // Maps project internal IDs to their UUIDs (as a service would maintain)
    private val projectUuidByProjectId = mapOf(100L to projectAlphaUuid, 200L to projectBetaUuid)
    private val allDocuments = listOf(doc1InAlpha, doc2InAlpha, doc3InBeta, doc4InBeta)

    // ============================================================
    // Project Permission Controls Document Visibility
    // ============================================================

    @Test
    fun `user with project view permission can access that project`() {
        val policy = projectViewPolicy(projectAlphaUuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val result = evaluator.evaluate(projectGetRequest(projectAlphaUuid))

        assertTrue(result.isAllowed())
    }

    @Test
    fun `user without project view permission is denied access`() {
        val policy = projectViewPolicy(projectAlphaUuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        val result = evaluator.evaluate(projectGetRequest(projectBetaUuid))

        assertTrue(result.isDenied())
        assertFalse(result.isExplicitDeny)
    }

    @Test
    fun `user with no policies is denied access to all projects`() {
        every { policyCollector.collectPolicies(principalUrn) } returns emptyList()

        assertTrue(evaluator.evaluate(projectGetRequest(projectAlphaUuid)).isDenied())
        assertTrue(evaluator.evaluate(projectGetRequest(projectBetaUuid)).isDenied())
    }

    @Test
    fun `user with multiple project policies can access all granted projects`() {
        val policies = listOf(
            projectViewPolicy(projectAlphaUuid),
            projectViewPolicy(projectBetaUuid),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns policies

        assertTrue(evaluator.evaluate(projectGetRequest(projectAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(projectGetRequest(projectBetaUuid)).isAllowed())
    }

    @Test
    fun `user with wildcard project permission can access any project`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllProjectsViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = CoreActions.Project.READ_ONLY,
                    resources = listOf("urn:revet:core:$tenantId:project/*"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(projectGetRequest(projectAlphaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(projectGetRequest(projectBetaUuid)).isAllowed())
        assertTrue(evaluator.evaluate(projectGetRequest(UUID.randomUUID())).isAllowed())
    }

    // ============================================================
    // Filtering Documents by Project Permission
    // ============================================================

    @Test
    fun `only documents from permitted projects are returned`() {
        val policies = listOf(projectViewPolicy(projectAlphaUuid))

        val accessibleDocuments = filterDocumentsByProjectAccess(allDocuments, policies)

        assertEquals(2, accessibleDocuments.size)
        assertTrue(accessibleDocuments.all { it.projectId == 100L })
        assertEquals(
            setOf(doc1InAlpha.uuid, doc2InAlpha.uuid),
            accessibleDocuments.map { it.uuid }.toSet(),
        )
    }

    @Test
    fun `no documents are returned when user has no project permissions`() {
        val accessibleDocuments = filterDocumentsByProjectAccess(allDocuments, emptyList())

        assertTrue(accessibleDocuments.isEmpty())
    }

    @Test
    fun `all documents are returned when user has wildcard project permission`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "AllProjectsViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(CoreActions.Project.GET),
                    resources = listOf("urn:revet:core:$tenantId:project/*"),
                ),
            ),
        )

        val accessibleDocuments = filterDocumentsByProjectAccess(allDocuments, listOf(policy))

        assertEquals(4, accessibleDocuments.size)
    }

    // ============================================================
    // Direct Document Permission Grants
    // ============================================================

    @Test
    fun `user granted permission to a specific document can access it`() {
        val policy = documentViewPolicy(doc1InAlpha.uuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(documentGetRequest(doc1InAlpha.uuid)).isAllowed())
    }

    @Test
    fun `user granted permission to a specific document cannot access other documents`() {
        val policy = documentViewPolicy(doc1InAlpha.uuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(documentGetRequest(doc1InAlpha.uuid)).isAllowed())
        assertTrue(evaluator.evaluate(documentGetRequest(doc2InAlpha.uuid)).isDenied())
        assertTrue(evaluator.evaluate(documentGetRequest(doc3InBeta.uuid)).isDenied())
        assertTrue(evaluator.evaluate(documentGetRequest(doc4InBeta.uuid)).isDenied())
    }

    @Test
    fun `user with document permission but no project permission can still access the document`() {
        val policy = documentViewPolicy(doc3InBeta.uuid)
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        // Project access is denied
        assertTrue(evaluator.evaluate(projectGetRequest(projectBetaUuid)).isDenied())

        // But direct document access is allowed
        assertTrue(evaluator.evaluate(documentGetRequest(doc3InBeta.uuid)).isAllowed())
    }

    @Test
    fun `user with multiple document grants can access all granted documents`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "MultiDocumentViewPolicy",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(Actions.Document.GET),
                    resources = listOf(
                        "urn:revet:documents:$tenantId:document/${doc1InAlpha.uuid}",
                        "urn:revet:documents:$tenantId:document/${doc4InBeta.uuid}",
                    ),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        assertTrue(evaluator.evaluate(documentGetRequest(doc1InAlpha.uuid)).isAllowed())
        assertTrue(evaluator.evaluate(documentGetRequest(doc4InBeta.uuid)).isAllowed())
        assertTrue(evaluator.evaluate(documentGetRequest(doc2InAlpha.uuid)).isDenied())
        assertTrue(evaluator.evaluate(documentGetRequest(doc3InBeta.uuid)).isDenied())
    }

    // ============================================================
    // Combined Project + Document Filtering
    // ============================================================

    @Test
    fun `filtering combines project access and direct document grants`() {
        // User can view project Alpha + has a direct grant on doc3 in project Beta
        val policies = listOf(
            projectViewPolicy(projectAlphaUuid),
            documentViewPolicy(doc3InBeta.uuid),
        )

        val accessibleDocuments = filterDocumentsByAccess(allDocuments, policies)

        assertEquals(3, accessibleDocuments.size)
        val accessibleUuids = accessibleDocuments.map { it.uuid }.toSet()
        assertTrue(doc1InAlpha.uuid in accessibleUuids, "doc1 should be accessible via project Alpha")
        assertTrue(doc2InAlpha.uuid in accessibleUuids, "doc2 should be accessible via project Alpha")
        assertTrue(doc3InBeta.uuid in accessibleUuids, "doc3 should be accessible via direct grant")
        assertFalse(doc4InBeta.uuid in accessibleUuids, "doc4 should not be accessible")
    }

    @Test
    fun `direct document grant does not leak access to other documents in the same project`() {
        val policies = listOf(documentViewPolicy(doc3InBeta.uuid))

        val accessibleDocuments = filterDocumentsByAccess(allDocuments, policies)

        assertEquals(1, accessibleDocuments.size)
        assertEquals(doc3InBeta.uuid, accessibleDocuments[0].uuid)
    }

    @Test
    fun `explicit deny on document overrides project-level allow`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "ProjectAllowWithDocumentDeny",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    sid = "AllowProjectAlpha",
                    effect = Effect.ALLOW,
                    actions = listOf(CoreActions.Project.GET),
                    resources = listOf("urn:revet:core:$tenantId:project/$projectAlphaUuid"),
                ),
                Statement(
                    sid = "AllowAllDocuments",
                    effect = Effect.ALLOW,
                    actions = listOf(Actions.Document.GET),
                    resources = listOf("urn:revet:documents:$tenantId:document/*"),
                ),
                Statement(
                    sid = "DenyConfidentialDocument",
                    effect = Effect.DENY,
                    actions = listOf(Actions.Document.GET),
                    resources = listOf("urn:revet:documents:$tenantId:document/${doc1InAlpha.uuid}"),
                ),
            ),
        )
        every { policyCollector.collectPolicies(principalUrn) } returns listOf(policy)

        // Project access is allowed
        assertTrue(evaluator.evaluate(projectGetRequest(projectAlphaUuid)).isAllowed())

        // doc2 in Alpha is allowed
        assertTrue(evaluator.evaluate(documentGetRequest(doc2InAlpha.uuid)).isAllowed())

        // doc1 in Alpha is explicitly denied
        val result = evaluator.evaluate(documentGetRequest(doc1InAlpha.uuid))
        assertTrue(result.isDenied())
        assertTrue(result.isExplicitDeny)
    }

    @Test
    fun `explicit deny on document is excluded from combined filtering`() {
        val policy = Policy(
            id = UUID.randomUUID(),
            name = "ProjectAllowWithDocumentDeny",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(CoreActions.Project.GET),
                    resources = listOf("urn:revet:core:$tenantId:project/$projectAlphaUuid"),
                ),
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(Actions.Document.GET),
                    resources = listOf("urn:revet:documents:$tenantId:document/*"),
                ),
                Statement(
                    effect = Effect.DENY,
                    actions = listOf(Actions.Document.GET),
                    resources = listOf("urn:revet:documents:$tenantId:document/${doc1InAlpha.uuid}"),
                ),
            ),
        )
        val policies = listOf(policy)

        val accessibleDocuments = filterDocumentsByAccess(allDocuments, policies)

        val accessibleUuids = accessibleDocuments.map { it.uuid }.toSet()
        assertFalse(doc1InAlpha.uuid in accessibleUuids, "doc1 should be excluded by explicit deny")
        assertTrue(doc2InAlpha.uuid in accessibleUuids, "doc2 should be accessible via project Alpha")
    }

    // ============================================================
    // Helpers
    // ============================================================

    companion object {
        const val POLICY_VERSION = "2026-01-01"
    }

    /**
     * Simulates the filtering logic that checks project-level access only.
     * Documents are included if the user can view their parent project.
     */
    private fun filterDocumentsByProjectAccess(
        documents: List<Document>,
        policies: List<Policy>,
    ): List<Document> =
        documents.filter { doc ->
            val projectUuid = projectUuidByProjectId[doc.projectId]!!
            evaluator.evaluateWithPolicies(projectGetRequest(projectUuid), policies).isAllowed()
        }

    /**
     * Simulates the combined filtering logic: a document is visible if the user
     * has project-level access OR a direct document-level grant, and is not
     * explicitly denied at the document level.
     */
    private fun filterDocumentsByAccess(
        documents: List<Document>,
        policies: List<Policy>,
    ): List<Document> =
        documents.filter { doc ->
            val documentResult = evaluator.evaluateWithPolicies(
                documentGetRequest(doc.uuid),
                policies,
            )

            // Explicit deny always wins
            if (documentResult.isExplicitDeny) return@filter false

            // Allow if the user has direct document access
            if (documentResult.isAllowed()) return@filter true

            // Fall back to project-level access
            val projectUuid = projectUuidByProjectId[doc.projectId]!!
            evaluator.evaluateWithPolicies(projectGetRequest(projectUuid), policies).isAllowed()
        }

    private fun projectGetRequest(projectUuid: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = CoreActions.Project.GET,
            resourceUrn = "urn:revet:core:$tenantId:project/$projectUuid",
        )

    private fun documentGetRequest(documentUuid: UUID) =
        AuthorizationRequest(
            principalUrn = principalUrn,
            action = Actions.Document.GET,
            resourceUrn = "urn:revet:documents:$tenantId:document/$documentUuid",
        )

    private fun projectViewPolicy(projectUuid: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "ProjectViewPolicy-$projectUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = CoreActions.Project.READ_ONLY,
                    resources = listOf("urn:revet:core:$tenantId:project/$projectUuid"),
                ),
            ),
        )

    private fun documentViewPolicy(documentUuid: UUID) =
        Policy(
            id = UUID.randomUUID(),
            name = "DocumentViewPolicy-$documentUuid",
            version = POLICY_VERSION,
            tenantId = tenantId,
            statements = listOf(
                Statement(
                    effect = Effect.ALLOW,
                    actions = listOf(Actions.Document.GET, Actions.Document.LIST),
                    resources = listOf("urn:revet:documents:$tenantId:document/$documentUuid"),
                ),
            ),
        )

    private fun createDocument(id: Long, projectId: Long) =
        Document(
            id = id,
            uuid = UUID.randomUUID(),
            name = "Document $id",
            projectId = projectId,
            categoryId = null,
            mime = "application/pdf",
            date = LocalDateTime.now(),
            tags = emptySet(),
            isActive = true,
            removedAt = null,
        )
}
