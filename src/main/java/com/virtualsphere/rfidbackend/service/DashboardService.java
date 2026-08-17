package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.dto.ActivityLogResponse;
import com.virtualsphere.rfidbackend.dto.DashboardSummaryResponse;
import com.virtualsphere.rfidbackend.dto.InventoryResponse;
import com.virtualsphere.rfidbackend.model.ActivityLog;
import com.virtualsphere.rfidbackend.model.InventoryItem;
import com.virtualsphere.rfidbackend.model.InventoryStatus;
import com.virtualsphere.rfidbackend.model.Role;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.ActivityLogRepository;
import com.virtualsphere.rfidbackend.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Backs the "Dashboard Modules" from BRD 2.2.2: total assets, real-time status
 * counts, alerts for missing items, and recent activity. Inward/outward movement
 * is derived from the same ActivityLog entries used for the alerts feed.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ActivityLogRepository activityLogRepository;

    public DashboardSummaryResponse getSummary(User requester) {
        List<InventoryItem> items = requester.getRole() == Role.ADMIN
                ? inventoryItemRepository.findAll()
                : inventoryItemRepository.findAllByLocationIgnoreCase(requester.getLocation());

        long total = items.size();
        long inCount = items.stream().filter(i -> i.getStatus() == InventoryStatus.IN).count();
        long outCount = items.stream().filter(i -> i.getStatus() == InventoryStatus.OUT).count();
        long missingCount = items.stream().filter(i -> i.getStatus() == InventoryStatus.MISSING).count();

        List<InventoryResponse> alerts = items.stream()
                .filter(i -> i.getStatus() == InventoryStatus.MISSING)
                .map(InventoryResponse::from)
                .toList();

        List<ActivityLog> recentActivity = requester.getRole() == Role.ADMIN
                ? activityLogRepository.findTop20ByOrderByCreatedAtDesc()
                : activityLogRepository.findTop20ByLocationIgnoreCaseOrderByCreatedAtDesc(requester.getLocation());

        return new DashboardSummaryResponse(total, inCount, outCount, missingCount,
                alerts, recentActivity.stream().map(ActivityLogResponse::from).toList());
    }
}
