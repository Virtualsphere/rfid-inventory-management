package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.ScanCategory;

public record ScanLogResponse(Long reportId, String epc, ScanCategory category, InventoryResponse item) {
}
