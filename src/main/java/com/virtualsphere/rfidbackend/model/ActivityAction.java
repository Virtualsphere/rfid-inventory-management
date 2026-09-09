package com.virtualsphere.rfidbackend.model;

/**
 * Shared with the admin desktop app's activity_logs table - keep in sync with
 * com.virtualsphere.adminrfid.model.ActivityAction there. SECURITY_ALERT is
 * written by the desktop app's security-gate reader; it must exist here too
 * or reading any activity_logs row it wrote (EnumType.STRING) throws.
 */
public enum ActivityAction {
    CREATED,
    UPDATED,
    DELETED,
    SCAN_IN,
    SCAN_OUT,
    MISSING_DETECTED,
    SECURITY_ALERT
}
