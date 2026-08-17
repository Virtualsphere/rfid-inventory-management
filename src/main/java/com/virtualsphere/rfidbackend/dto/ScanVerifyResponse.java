package com.virtualsphere.rfidbackend.dto;

import java.util.List;

public record ScanVerifyResponse(int totalExpected, int totalScanned, int totalMissing,
                                  List<InventoryResponse> missingItems) {
}
