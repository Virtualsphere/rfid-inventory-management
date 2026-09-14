package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.dto.CheckpointScanResponse;
import com.virtualsphere.rfidbackend.model.ActivityAction;
import com.virtualsphere.rfidbackend.model.InventoryItem;
import com.virtualsphere.rfidbackend.model.InventoryStatus;
import com.virtualsphere.rfidbackend.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Backs the admin desktop's Security Reader / checkpoint gate page - kept as its own
 * service (not folded into InventoryService) because this is admin-only checkpoint
 * logic, deliberately separate from the mobile app's own scan endpoints
 * (scan-log/scan-verify/sync) even though both ultimately touch InventoryItem.
 *
 * Mirrors SecurityReaderPageController.processScannedTag() from the desktop app
 * exactly: an EPC not registered in inventory is ignored entirely, and an item
 * already OUT/MISSING is reported back for display but not logged - only an IN
 * item passing the gate is the unauthorized-removal case worth alerting and
 * recording. Status is deliberately left as IN (not flipped to OUT) per the
 * desktop's own "testing mode" behavior, so the same tag keeps re-triggering the
 * alert on repeat test scans instead of going quiet after the first one.
 */
@Service
@RequiredArgsConstructor
public class SecurityCheckpointService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public CheckpointScanResponse checkpointScan(String epc, String performedBy) {
        Optional<InventoryItem> found = inventoryItemRepository.findByEpcIgnoreCase(epc);
        if (found.isEmpty()) {
            return new CheckpointScanResponse(epc, false, null, null, false);
        }

        InventoryItem item = found.get();
        if (item.getStatus() != InventoryStatus.IN) {
            return new CheckpointScanResponse(epc, true, item.getStatus(), item.getProductName(), false);
        }

        item.setLastScannedAt(LocalDateTime.now());
        inventoryItemRepository.save(item);
        activityLogService.log(item.getEpc(), ActivityAction.SECURITY_ALERT, performedBy, item.getLocation(),
                "Security checkpoint: tag was IN, buzzer triggered");

        return new CheckpointScanResponse(epc, true, item.getStatus(), item.getProductName(), true);
    }
}
