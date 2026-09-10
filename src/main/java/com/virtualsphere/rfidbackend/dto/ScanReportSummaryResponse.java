package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.ScanReport;
import com.virtualsphere.rfidbackend.model.ScanReportType;

import java.time.LocalDateTime;

/** One row in the admin Reports list (either tab) - no item-level detail, just the header/counts. */
public record ScanReportSummaryResponse(Long id, ScanReportType type, String location, String performedBy,
                                         LocalDateTime scannedAt, Integer durationSeconds,
                                         Integer expectedCount, int foundCount, int missingCount,
                                         int unexpectedCount, int unknownCount, int unavailableCount,
                                         int totalDetected, LocalDateTime createdAt) {
    public static ScanReportSummaryResponse from(ScanReport r) {
        return new ScanReportSummaryResponse(r.getId(), r.getType(), r.getLocation(), r.getPerformedBy(),
                r.getScannedAt(), r.getDurationSeconds(), r.getExpectedCount(), r.getFoundCount(),
                r.getMissingCount(), r.getUnexpectedCount(), r.getUnknownCount(), r.getUnavailableCount(),
                r.getTotalDetected(), r.getCreatedAt());
    }
}
