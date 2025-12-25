package com.revet.documents.service

import com.revet.documents.domain.PermissionType
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Integration tests for SecurityService with real database.
 * Uses Quarkus Dev Services to automatically start a PostgreSQL container.
 */
@QuarkusTest
@Transactional
class SecurityServiceIntegrationTest {

    @Inject
    lateinit var securityService: SecurityService

    @Inject
    lateinit var organizationService: OrganizationService

    @Inject
    lateinit var projectService: ProjectService

    @Inject
    lateinit var documentService: DocumentService

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var organizationPermissionService: OrganizationPermissionService

    @Inject
    lateinit var projectPermissionService: ProjectPermissionService

    @Inject
    lateinit var documentPermissionService: DocumentPermissionService

    // Test data IDs - populated in setup
    private var orgId: Long = 0
    private var projectId: Long = 0
    private var documentId: Long = 0
    private var userId: Long = 0

    @BeforeEach
    fun setup() {
        // Create test organization
        val org = organizationService.createOrganization(
            name = "Test Organization ${System.nanoTime()}",
            description = "Test org for security tests"
        )
        orgId = org.id!!

        // Create test project
        val project = projectService.createProject(
            name = "Test Project ${System.nanoTime()}",
            organizationId = orgId
        )
        projectId = project.id!!

        // Create test document
        val document = documentService.createDocument(
            name = "Test Document ${System.nanoTime()}",
            projectId = projectId
        )
        documentId = document.id!!

        // Create test user
        val user = userService.createUser(
            username = "testuser_${System.nanoTime()}",
            email = "test_${System.nanoTime()}@example.com"
        )
        userId = user.id!!
    }

    @Nested
    @DisplayName("Organization Permission Inheritance")
    inner class OrganizationPermissionTests {

        @Test
        @DisplayName("Organization CAN_MANAGE grants access to all child resources")
        fun `org manage permission grants access to project and document`() {
            organizationPermissionService.grantPermission(orgId, userId, PermissionType.CAN_MANAGE)

            // Should have access to org
            assertTrue(securityService.canAccessOrganization(userId, orgId, PermissionType.CAN_MANAGE))
            assertTrue(securityService.canAccessOrganization(userId, orgId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessOrganization(userId, orgId, PermissionType.CAN_INVITE))

            // Should inherit to project
            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_MANAGE))
            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_INVITE))

            // Should inherit to document
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
        }

        @Test
        @DisplayName("Organization CAN_CREATE grants limited access to child resources")
        fun `org create permission grants create and invite to children`() {
            organizationPermissionService.grantPermission(orgId, userId, PermissionType.CAN_CREATE)

            // Can CREATE and INVITE, but not MANAGE
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("Organization CAN_INVITE grants minimal access to child resources")
        fun `org invite permission grants only invite to children`() {
            organizationPermissionService.grantPermission(orgId, userId, PermissionType.CAN_INVITE)

            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_CREATE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }
    }

    @Nested
    @DisplayName("Project Permission Inheritance")
    inner class ProjectPermissionTests {

        @Test
        @DisplayName("Project permission inherits to document but not organization")
        fun `project permission inherits to document only`() {
            projectPermissionService.grantPermission(projectId, userId, PermissionType.CAN_MANAGE)

            // Should NOT have access to org
            assertFalse(securityService.canAccessOrganization(userId, orgId, PermissionType.CAN_INVITE))

            // Should have access to project
            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_MANAGE))

            // Should inherit to document
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }
    }

    @Nested
    @DisplayName("Document Permission")
    inner class DocumentPermissionTests {

        @Test
        @DisplayName("Document permission does not inherit upward")
        fun `document permission does not inherit to project or org`() {
            documentPermissionService.grantPermission(documentId, userId, PermissionType.CAN_MANAGE)

            // Should have access to document
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))

            // Should NOT have access to project or org
            assertFalse(securityService.canAccessProject(userId, projectId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessOrganization(userId, orgId, PermissionType.CAN_INVITE))
        }
    }

    @Nested
    @DisplayName("Permission Combination")
    inner class PermissionCombinationTests {

        @Test
        @DisplayName("Higher permission from org overrides lower permission from document")
        fun `highest permission wins across levels`() {
            // Give low permission on document, high permission on org
            documentPermissionService.grantPermission(documentId, userId, PermissionType.CAN_INVITE)
            organizationPermissionService.grantPermission(orgId, userId, PermissionType.CAN_MANAGE)

            // Effective permission should be CAN_MANAGE
            assertEquals(PermissionType.CAN_MANAGE, securityService.getEffectiveDocumentPermission(userId, documentId))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("Higher permission from document used when org has lower")
        fun `document permission higher than org permission wins`() {
            // Give high permission on document, low permission on org
            documentPermissionService.grantPermission(documentId, userId, PermissionType.CAN_MANAGE)
            organizationPermissionService.grantPermission(orgId, userId, PermissionType.CAN_INVITE)

            // Effective permission should be CAN_MANAGE
            assertEquals(PermissionType.CAN_MANAGE, securityService.getEffectiveDocumentPermission(userId, documentId))
        }

        @Test
        @DisplayName("Project permission combines with org permission")
        fun `project permission combines with org for document access`() {
            // Give CAN_CREATE on project, CAN_INVITE on org
            projectPermissionService.grantPermission(projectId, userId, PermissionType.CAN_CREATE)
            organizationPermissionService.grantPermission(orgId, userId, PermissionType.CAN_INVITE)

            // Effective document permission should be CAN_CREATE (higher of the two)
            assertEquals(PermissionType.CAN_CREATE, securityService.getEffectiveDocumentPermission(userId, documentId))
        }
    }

    @Nested
    @DisplayName("No Permission")
    inner class NoPermissionTests {

        @Test
        @DisplayName("User without any permission cannot access resources")
        fun `user without permission cannot access anything`() {
            // No permissions granted

            assertFalse(securityService.canAccessOrganization(userId, orgId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessProject(userId, projectId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
        }

        @Test
        @DisplayName("Effective permission is null when no permission exists")
        fun `effective permission is null without any grants`() {
            assertNull(securityService.getEffectiveDocumentPermission(userId, documentId))
            assertNull(securityService.getEffectiveProjectPermission(userId, projectId))
            assertNull(securityService.getOrganizationPermission(userId, orgId))
        }
    }

    @Nested
    @DisplayName("Permission Revocation")
    inner class PermissionRevocationTests {

        @Test
        @DisplayName("Revoking permission removes access")
        fun `revoking permission removes access`() {
            // Grant and verify
            organizationPermissionService.grantPermission(orgId, userId, PermissionType.CAN_MANAGE)
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))

            // Revoke
            organizationPermissionService.revokePermissionByOrganizationAndUser(orgId, userId)

            // Access should be denied
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
        }
    }
}
