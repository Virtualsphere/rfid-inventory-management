package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.InventoryStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Used for both create and update. On create, epc is required; on update, only
 * the fields you want to change need to be set (null = unchanged).
 */
@Data
public class InventoryRequest {
    @NotBlank
    private String epc;

    private String productName;
    private String modelNo;
    private BigDecimal price;
    private String category;
    private String location;
    private String rackId;
    private InventoryStatus status;
}
