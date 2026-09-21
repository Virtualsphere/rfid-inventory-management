package com.virtualsphere.rfidbackend.model;

/**
 * Fine-grained capabilities an admin can grant to a USER account, independent
 * of the coarse ADMIN/USER role. ADMIN always has full access regardless of
 * these flags (see SecurityConfig / method-security checks).
 */
public enum Permission {
    /** View/lookup inventory items and log ad hoc scans - no status changes. */
    SCAN_READ,
    /** Change an inventory item's status (manual edit, bulk sync, scan-verify). */
    UPDATE_STATUS
}
