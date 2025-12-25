package com.revet.documents.domain

/**
 * Permission types for resources in the Kala system.
 *
 * Hierarchy (higher implies lower): CAN_MANAGE > CAN_CREATE > CAN_INVITE
 *
 * - CAN_INVITE: Can invite users to the resource
 * - CAN_CREATE: Can create/edit content (implies CAN_INVITE)
 * - CAN_MANAGE: Full management including permissions (implies CAN_CREATE, CAN_INVITE)
 */
enum class PermissionType {
    CAN_INVITE,  // Can invite users (lowest level)
    CAN_CREATE,  // Can create/edit resources (implies CAN_INVITE)
    CAN_MANAGE;  // Full management including permissions (highest level)

    /**
     * Check if this permission level implies (is >= to) the required permission.
     * CAN_MANAGE implies CAN_CREATE implies CAN_INVITE.
     */
    fun implies(required: PermissionType): Boolean {
        return this.ordinal >= required.ordinal
    }
}
