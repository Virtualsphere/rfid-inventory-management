package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.ScanCategory;
import com.virtualsphere.rfidbackend.model.ScanReport;
import com.virtualsphere.rfidbackend.model.ScanReportItem;
import com.virtualsphere.rfidbackend.model.ScanReportType;
import com.virtualsphere.rfidbackend.repository.ScanReportItemRepository;
import com.virtualsphere.rfidbackend.repository.ScanReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Persists and serves scan reports - the backend-truth version of what the
 * handheld app's Bulk Scan and Scan Inventory screens currently compute and
 * hold on-device only. InventoryService does the per-EPC classification (it
 * already owns inventory lookups); this service just stores and queries the
 * result, and is what both the mobile "Scan History" screen and the admin
 * desktop Reports page read from.
 */
@Service
@RequiredArgsConstructor
public class ScanReportService {

    private static final int MAX_LIMIT = 200;

    private final ScanReportRepository scanReportRepository;
    private final ScanReportItemRepository scanReportItemRepository;

    @Transactional
    public ScanReport save(ScanReportType type, String location, String performedBy, LocalDateTime scannedAt,
                            Integer durationSeconds, Integer expectedCount, List<ScanReportItem> items) {
        ScanReport report = new ScanReport();
        report.setType(type);
        report.setLocation(location);
        report.setPerformedBy(performedBy);
        report.setScannedAt(scannedAt != null ? scannedAt : LocalDateTime.now());
        report.setDurationSeconds(durationSeconds);
        report.setExpectedCount(expectedCount);
        report.setTotalDetected(items.size());

        for (ScanReportItem item : items) {
            switch (item.getCategory()) {
                case FOUND -> report.setFoundCount(report.getFoundCount() + 1);
                case MISSING -> report.setMissingCount(report.getMissingCount() + 1);
                case UNEXPECTED -> report.setUnexpectedCount(report.getUnexpectedCount() + 1);
                case UNKNOWN -> report.setUnknownCount(report.getUnknownCount() + 1);
                case UNAVAILABLE -> report.setUnavailableCount(report.getUnavailableCount() + 1);
            }
        }

        ScanReport saved = scanReportRepository.save(report);
        items.forEach(item -> item.setReportId(saved.getId()));
        scanReportItemRepository.saveAll(items);
        return saved;
    }

    public List<ScanReport> query(ScanReportType type, String location, String performedBy,
                                   LocalDateTime from, LocalDateTime to, int limit) {
        int effectiveLimit = limit <= 0 ? 50 : Math.min(limit, MAX_LIMIT);
        return scanReportRepository.findAll(
                ScanReportSpecifications.search(type, location, performedBy, from, to),
                PageRequest.of(0, effectiveLimit, Sort.by(Sort.Direction.DESC, "scannedAt"))
        ).getContent();
    }

    public ScanReport getById(Long id) {
        return scanReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scan report not found: " + id));
    }

    /** Preserves the order/duplicates of the requested ids - important for a multi-id export request. */
    public List<ScanReport> getByIds(List<Long> ids) {
        Map<Long, ScanReport> byId = scanReportRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ScanReport::getId, r -> r));
        return ids.stream()
                .map(id -> {
                    ScanReport report = byId.get(id);
                    if (report == null) {
                        throw new ResourceNotFoundException("Scan report not found: " + id);
                    }
                    return report;
                })
                .toList();
    }

    public List<ScanReportItem> getItems(Long reportId) {
        return scanReportItemRepository.findAllByReportId(reportId);
    }

    public List<ScanReportItem> getItems(List<Long> reportIds) {
        return scanReportItemRepository.findAllByReportIdIn(reportIds);
    }

    public static ScanReportItem item(String epc, ScanCategory category, String productName,
                                       String itemLocation, com.virtualsphere.rfidbackend.model.InventoryStatus previousStatus) {
        ScanReportItem item = new ScanReportItem();
        item.setEpc(epc);
        item.setCategory(category);
        item.setProductName(productName);
        item.setItemLocation(itemLocation);
        item.setPreviousStatus(previousStatus);
        return item;
    }
}
