package com.virtualsphere.rfidbackend.dto;

public record SyncEventResult(String epc, boolean success, String message, InventoryResponse item) {
}
