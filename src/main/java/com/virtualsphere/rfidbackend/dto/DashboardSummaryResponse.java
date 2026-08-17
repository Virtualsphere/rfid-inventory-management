package com.virtualsphere.rfidbackend.dto;

import java.util.List;

public record DashboardSummaryResponse(long totalAssets, long inCount, long outCount, long missingCount,
                                        List<InventoryResponse> alerts, List<ActivityLogResponse> recentActivity) {
}
