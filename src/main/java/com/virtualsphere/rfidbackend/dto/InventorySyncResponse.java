package com.virtualsphere.rfidbackend.dto;

import java.util.List;

public record InventorySyncResponse(int totalEvents, int succeeded, int failed, List<SyncEventResult> results) {
}
