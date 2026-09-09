package com.virtualsphere.rfidbackend.dto;

public record ScanLogResponse(Long reportId, String epc, String category, InventoryResponse item) {
}
