package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.ScanReportItem;

public record ScanReportItemResponse(Long id, String epc, String category, String productName,
                                      String itemLocation, String previousStatus) {
    public static ScanReportItemResponse from(ScanReportItem item) {
        return new ScanReportItemResponse(item.getId(), item.getEpc(), item.getCategory().name(),
                item.getProductName(), item.getItemLocation(),
                item.getPreviousStatus() != null ? item.getPreviousStatus().name() : null);
    }
}
