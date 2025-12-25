package com.revet.documents.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

/**
 * Unit tests for PermissionType hierarchy.
 * Tests the implies() method which determines if one permission level
 * grants access for actions requiring another permission level.
 *
 * Hierarchy: CAN_MANAGE > CAN_CREATE > CAN_INVITE
 */
class PermissionTypeTest {

    @Test
    @DisplayName("CAN_MANAGE implies all permission levels")
    fun `CAN_MANAGE implies all permissions`() {
        assertTrue(PermissionType.CAN_MANAGE.implies(PermissionType.CAN_MANAGE))
        assertTrue(PermissionType.CAN_MANAGE.implies(PermissionType.CAN_CREATE))
        assertTrue(PermissionType.CAN_MANAGE.implies(PermissionType.CAN_INVITE))
    }

    @Test
    @DisplayName("CAN_CREATE implies CREATE and INVITE but not MANAGE")
    fun `CAN_CREATE implies CREATE and INVITE but not MANAGE`() {
        assertFalse(PermissionType.CAN_CREATE.implies(PermissionType.CAN_MANAGE))
        assertTrue(PermissionType.CAN_CREATE.implies(PermissionType.CAN_CREATE))
        assertTrue(PermissionType.CAN_CREATE.implies(PermissionType.CAN_INVITE))
    }

    @Test
    @DisplayName("CAN_INVITE only implies itself")
    fun `CAN_INVITE only implies itself`() {
        assertFalse(PermissionType.CAN_INVITE.implies(PermissionType.CAN_MANAGE))
        assertFalse(PermissionType.CAN_INVITE.implies(PermissionType.CAN_CREATE))
        assertTrue(PermissionType.CAN_INVITE.implies(PermissionType.CAN_INVITE))
    }

    @Test
    @DisplayName("Permission ordinals are in correct order")
    fun `ordinals reflect hierarchy order`() {
        assertTrue(PermissionType.CAN_MANAGE.ordinal > PermissionType.CAN_CREATE.ordinal)
        assertTrue(PermissionType.CAN_CREATE.ordinal > PermissionType.CAN_INVITE.ordinal)
    }
}
