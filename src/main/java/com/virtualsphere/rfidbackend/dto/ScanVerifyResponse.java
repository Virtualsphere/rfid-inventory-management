package com.virtualsphere.rfidbackend.dto;

import java.util.List;

/**
 * Full result of a bulk scan-verify pass, backing the mobile app's Bulk Scan
 * report tiles directly - the app no longer needs to compute
 * found/unexpected/unknown/unavailable on-device; every count and list here
 * is server-truth and already persisted as a ScanReport (see reportId).
 */
public record ScanVerifyResponse(
        Long reportId,
        int totalExpected,
        int totalScanned,
        int totalMissing,
        int totalFound,
        int totalUnexpected,
        int totalUnknown,
        int totalUnavailable,
        List<InventoryResponse> missingItems,
        List<InventoryResponse> unexpectedItems,
        List<String> unknownEpcs,
        List<InventoryResponse> unavailableItems
) {
}
