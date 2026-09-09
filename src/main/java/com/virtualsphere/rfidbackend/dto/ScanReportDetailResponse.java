package com.virtualsphere.rfidbackend.dto;

import java.util.List;

/** Full detail behind one report row - the header plus every EPC's outcome, for the admin drill-down view. */
public record ScanReportDetailResponse(ScanReportSummaryResponse report, List<ScanReportItemResponse> items) {
}
