package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.InventoryStatus;

/**
 * @param found           whether the EPC is registered in inventory at all - unregistered tags
 *                         near the gate are ignored entirely, matching the desktop's own behavior.
 * @param status          the item's status at the moment of this scan (null if not found).
 * @param alertTriggered  true if the item was IN, which is the unauthorized-removal case the
 *                         gate's buzzer/light exist to catch - the desktop client decides what to
 *                         do with its own hardware based on this flag.
 */
public record CheckpointScanResponse(String epc, boolean found, InventoryStatus status, String productName,
                                      boolean alertTriggered) {
}
