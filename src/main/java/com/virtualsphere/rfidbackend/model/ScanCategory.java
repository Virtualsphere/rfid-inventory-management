package com.virtualsphere.rfidbackend.model;

/**
 * Per-EPC outcome of a scan (single or bulk), backing the mobile app's report
 * tiles (Found / Missing / Unexpected / Unknown / Unavailable).
 */
public enum ScanCategory {
    /** Registered at this location and currently IN - what a scan is supposed to find. */
    FOUND,
    /** Registered as IN at this location but not present in the scan pass. Bulk-only. */
    MISSING,
    /** Registered, but at a different location than the one being scanned. */
    UNEXPECTED,
    /** Not registered in inventory at all - an unrecognized tag. */
    UNKNOWN,
    /** Registered at this location, but its status wasn't IN before this scan (e.g. already OUT). */
    UNAVAILABLE
}
