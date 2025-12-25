package com.revet.documents.service

import com.revet.documents.domain.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * Unit tests for SecurityService permission checking with inheritance.
 * Uses MockK to mock all dependencies.
 */
class SecurityServiceTest {

    private lateinit var documentPermissionService: DocumentPermissionService
    private lateinit var projectPermissionService: ProjectPermissionService
    private lateinit var organizationPermissionService: OrganizationPermissionService
    private lateinit var documentService: DocumentService
    private lateinit var projectService: ProjectService
    private lateinit var securityService: SecurityService

    // Test data
    private val userId = 100L
    private val documentId = 1L
    private val projectId = 10L
    private val organizationId = 50L

    private val testDocument = Document(
        id = documentId,
        uuid = UUID.randomUUID(),
        name = "Test Document",
        projectId = projectId,
        categoryId = null,
        mime = "application/pdf",
        date = LocalDateTime.now(),
        tags = emptySet(),
        isActive = true,
        removedAt = null
    )

    private val testProject = Project(
        id = projectId,
        uuid = UUID.randomUUID(),
        name = "Test Project",
        description = null,
        organizationId = organizationId,
        clientIds = emptySet(),
        tags = emptySet(),
        isActive = true,
        timestamps = Project.Timestamps(
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now(),
            removedAt = null
        )
    )

    @BeforeEach
    fun setup() {
        documentPermissionService = mockk(relaxed = true)
        projectPermissionService = mockk(relaxed = true)
        organizationPermissionService = mockk(relaxed = true)
        documentService = mockk(relaxed = true)
        projectService = mockk(relaxed = true)

        securityService = SecurityServiceImpl(
            documentPermissionService,
            projectPermissionService,
            organizationPermissionService,
            documentService,
            projectService
        )

        // Default: document and project exist
        every { documentService.getDocumentById(documentId) } returns testDocument
        every { projectService.getProjectById(projectId) } returns testProject
    }

    @Nested
    @DisplayName("Document Access")
    inner class DocumentAccessTests {

        @Test
        @DisplayName("Direct document permission grants access")
        fun `direct document permission grants access`() {
            every { documentPermissionService.getPermissionByDocumentAndUser(documentId, userId) } returns
                DocumentPermission(1, documentId, userId, PermissionType.CAN_CREATE)

            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("Project permission inherits to document")
        fun `project permission inherits to document`() {
            every { documentPermissionService.getPermissionByDocumentAndUser(documentId, userId) } returns null
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns
                ProjectPermission(1, projectId, userId, PermissionType.CAN_MANAGE)

            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
        }

        @Test
        @DisplayName("Organization permission inherits through project to document")
        fun `organization permission inherits through project to document`() {
            every { documentPermissionService.getPermissionByDocumentAndUser(documentId, userId) } returns null
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns null
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns
                OrganizationPermission(1, organizationId, userId, PermissionType.CAN_CREATE)

            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("No permission at any level denies access")
        fun `no permission at any level denies access`() {
            every { documentPermissionService.getPermissionByDocumentAndUser(documentId, userId) } returns null
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns null
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns null

            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_CREATE))
            assertFalse(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("Higher permission from org overrides lower permission from document")
        fun `highest permission wins when multiple levels have permissions`() {
            // User has CAN_INVITE on document, but CAN_MANAGE on org
            every { documentPermissionService.getPermissionByDocumentAndUser(documentId, userId) } returns
                DocumentPermission(1, documentId, userId, PermissionType.CAN_INVITE)
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns null
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns
                OrganizationPermission(1, organizationId, userId, PermissionType.CAN_MANAGE)

            assertEquals(PermissionType.CAN_MANAGE, securityService.getEffectiveDocumentPermission(userId, documentId))
            assertTrue(securityService.canAccessDocument(userId, documentId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("Higher permission from project used over lower from org")
        fun `project permission higher than org permission wins`() {
            every { documentPermissionService.getPermissionByDocumentAndUser(documentId, userId) } returns null
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns
                ProjectPermission(1, projectId, userId, PermissionType.CAN_MANAGE)
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns
                OrganizationPermission(1, organizationId, userId, PermissionType.CAN_INVITE)

            assertEquals(PermissionType.CAN_MANAGE, securityService.getEffectiveDocumentPermission(userId, documentId))
        }
    }

    @Nested
    @DisplayName("Project Access")
    inner class ProjectAccessTests {

        @Test
        @DisplayName("Direct project permission grants access")
        fun `direct project permission grants access`() {
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns
                ProjectPermission(1, projectId, userId, PermissionType.CAN_CREATE)

            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessProject(userId, projectId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("Organization permission inherits to project")
        fun `organization permission inherits to project`() {
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns null
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns
                OrganizationPermission(1, organizationId, userId, PermissionType.CAN_MANAGE)

            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_MANAGE))
            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessProject(userId, projectId, PermissionType.CAN_INVITE))
        }

        @Test
        @DisplayName("No permission denies access")
        fun `no permission denies access`() {
            every { projectPermissionService.getPermissionByProjectAndUser(projectId, userId) } returns null
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns null

            assertFalse(securityService.canAccessProject(userId, projectId, PermissionType.CAN_INVITE))
        }
    }

    @Nested
    @DisplayName("Organization Access")
    inner class OrganizationAccessTests {

        @Test
        @DisplayName("Organization permission grants access")
        fun `organization permission grants access`() {
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns
                OrganizationPermission(1, organizationId, userId, PermissionType.CAN_CREATE)

            assertTrue(securityService.canAccessOrganization(userId, organizationId, PermissionType.CAN_CREATE))
            assertTrue(securityService.canAccessOrganization(userId, organizationId, PermissionType.CAN_INVITE))
            assertFalse(securityService.canAccessOrganization(userId, organizationId, PermissionType.CAN_MANAGE))
        }

        @Test
        @DisplayName("No permission denies access")
        fun `no permission denies access`() {
            every { organizationPermissionService.getPermissionByOrganizationAndUser(organizationId, userId) } returns null

            assertFalse(securityService.canAccessOrganization(userId, organizationId, PermissionType.CAN_INVITE))
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("Non-existent document returns null effective permission")
        fun `non-existent document returns null effective permission`() {
            every { documentPermissionService.getPermissionByDocumentAndUser(999, userId) } returns null
            every { documentService.getDocumentById(999) } returns null

            assertNull(securityService.getEffectiveDocumentPermission(userId, 999))
            assertFalse(securityService.canAccessDocument(userId, 999, PermissionType.CAN_INVITE))
        }

        @Test
        @DisplayName("Non-existent project returns null effective permission")
        fun `non-existent project returns null effective permission`() {
            every { projectPermissionService.getPermissionByProjectAndUser(999, userId) } returns null
            every { projectService.getProjectById(999) } returns null

            assertNull(securityService.getEffectiveProjectPermission(userId, 999))
            assertFalse(securityService.canAccessProject(userId, 999, PermissionType.CAN_INVITE))
        }

        @Test
        @DisplayName("Document permission returned when document lookup fails")
        fun `document permission returned even when project lookup would fail`() {
            // Has document permission but document lookup returns null
            every { documentPermissionService.getPermissionByDocumentAndUser(documentId, userId) } returns
                DocumentPermission(1, documentId, userId, PermissionType.CAN_CREATE)
            every { documentService.getDocumentById(documentId) } returns null

            // Should still get the document-level permission
            assertEquals(PermissionType.CAN_CREATE, securityService.getEffectiveDocumentPermission(userId, documentId))
        }
    }
}
