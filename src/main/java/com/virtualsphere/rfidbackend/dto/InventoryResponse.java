package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.InventoryItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryResponse(Long id, String epc, String productName, String modelNo, BigDecimal price,
                                 String category, String location, String rackId, String status,
                                 LocalDateTime lastScannedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static InventoryResponse from(InventoryItem item) {
        return new InventoryResponse(item.getId(), item.getEpc(), item.getProductName(), item.getModelNo(),
                item.getPrice(), item.getCategory(), item.getLocation(), item.getRackId(),
                item.getStatus().name(), item.getLastScannedAt(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
