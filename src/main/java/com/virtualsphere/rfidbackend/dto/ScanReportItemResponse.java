package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.InventoryStatus;
import com.virtualsphere.rfidbackend.model.ScanCategory;
import com.virtualsphere.rfidbackend.model.ScanReportItem;

public record ScanReportItemResponse(Long id, String epc, ScanCategory category, String productName,
                                      String itemLocation, InventoryStatus previousStatus) {
    public static ScanReportItemResponse from(ScanReportItem item) {
        return new ScanReportItemResponse(item.getId(), item.getEpc(), item.getCategory(),
                item.getProductName(), item.getItemLocation(), item.getPreviousStatus());
    }
}
