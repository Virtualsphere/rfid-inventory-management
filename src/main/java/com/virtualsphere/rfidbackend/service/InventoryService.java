package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.dto.InventoryRequest;
import com.virtualsphere.rfidbackend.dto.InventoryResponse;
import com.virtualsphere.rfidbackend.dto.ScanVerifyRequest;
import com.virtualsphere.rfidbackend.dto.ScanVerifyResponse;
import com.virtualsphere.rfidbackend.exception.DuplicateResourceException;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.*;
import com.virtualsphere.rfidbackend.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ActivityLogService activityLogService;

    /**
     * Non-admin users are always scoped to their own assigned location, regardless
     * of what location filter they pass in - matches BRD 2.2.3 "User Access and
     * Operations Mapping According to Assigned Locations".
     */
    public List<InventoryItem> search(String freeText, InventoryStatus status, String location,
                                       String rackId, String modelNo, String category, User requester) {
        String effectiveLocation = requester.getRole() == Role.USER ? requester.getLocation() : location;
        return inventoryItemRepository.findAll(
                InventorySpecifications.search(freeText, status, effectiveLocation, rackId, modelNo, category));
    }

    public InventoryItem findByEpc(String epc, User requester) {
        InventoryItem item = inventoryItemRepository.findByEpcIgnoreCase(epc)
                .orElseThrow(() -> new ResourceNotFoundException("No inventory item found for EPC: " + epc));
        assertLocationAccess(item, requester);
        return item;
    }

    @Transactional
    public InventoryItem create(InventoryRequest request, User requester) {
        if (inventoryItemRepository.existsByEpcIgnoreCase(request.getEpc())) {
            throw new DuplicateResourceException("An inventory item with this EPC already exists: " + request.getEpc());
        }

        InventoryItem item = new InventoryItem();
        item.setEpc(request.getEpc().toUpperCase());
        item.setProductName(request.getProductName());
        item.setModelNo(request.getModelNo());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setLocation(request.getLocation() != null ? request.getLocation() : requester.getLocation());
        item.setRackId(request.getRackId());
        item.setStatus(request.getStatus() == null ? InventoryStatus.IN : request.getStatus());
        item.setLastScannedAt(LocalDateTime.now());

        InventoryItem saved = inventoryItemRepository.save(item);
        activityLogService.log(saved.getEpc(), ActivityAction.CREATED, requester.getUsername(),
                saved.getLocation(), "Inventory item registered");
        return saved;
    }

    @Transactional
    public InventoryItem update(Long id, InventoryRequest request, User requester) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + id));
        assertLocationAccess(item, requester);

        InventoryStatus previousStatus = item.getStatus();

        if (request.getProductName() != null) item.setProductName(request.getProductName());
        if (request.getModelNo() != null) item.setModelNo(request.getModelNo());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getLocation() != null) item.setLocation(request.getLocation());
        if (request.getRackId() != null) item.setRackId(request.getRackId());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        item.setLastScannedAt(LocalDateTime.now());

        InventoryItem saved = inventoryItemRepository.save(item);

        ActivityAction action = ActivityAction.UPDATED;
        if (request.getStatus() != null && request.getStatus() != previousStatus) {
            action = switch (request.getStatus()) {
                case IN -> ActivityAction.SCAN_IN;
                case OUT -> ActivityAction.SCAN_OUT;
                case MISSING -> ActivityAction.MISSING_DETECTED;
            };
        }
        activityLogService.log(saved.getEpc(), action, requester.getUsername(), saved.getLocation(), null);
        return saved;
    }

    @Transactional
    public void delete(Long id, String performedBy) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + id));
        inventoryItemRepository.delete(item);
        activityLogService.log(item.getEpc(), ActivityAction.DELETED, performedBy, item.getLocation(), null);
    }

    /**
     * Mobile "bulk scan verification" workflow (BRD 2.1.2): compares every EPC the
     * handheld actually scanned at a location against everything the system expects
     * to be there (status = IN). Anything expected but not scanned is flagged MISSING.
     */
    @Transactional
    public ScanVerifyResponse scanVerify(ScanVerifyRequest request, User requester) {
        if (requester.getRole() == Role.USER
                && requester.getLocation() != null
                && !requester.getLocation().equalsIgnoreCase(request.getLocation())) {
            throw new AccessDeniedException("You can only verify inventory at your assigned location");
        }

        Set<String> scannedEpcs = request.getScannedEpcs().stream()
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        List<InventoryItem> expected =
                inventoryItemRepository.findAllByLocationIgnoreCaseAndStatus(request.getLocation(), InventoryStatus.IN);

        List<InventoryItem> missing = new ArrayList<>();
        int scannedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (InventoryItem item : expected) {
            if (scannedEpcs.contains(item.getEpc().toUpperCase())) {
                item.setLastScannedAt(now);
                scannedCount++;
            } else {
                item.setStatus(InventoryStatus.MISSING);
                missing.add(item);
            }
        }
        inventoryItemRepository.saveAll(expected);

        for (InventoryItem m : missing) {
            activityLogService.log(m.getEpc(), ActivityAction.MISSING_DETECTED, requester.getUsername(),
                    request.getLocation(), "Marked IN but not found during physical scan verification");
        }

        return new ScanVerifyResponse(expected.size(), scannedCount, missing.size(),
                missing.stream().map(InventoryResponse::from).toList());
    }

    private void assertLocationAccess(InventoryItem item, User requester) {
        if (requester.getRole() == Role.USER
                && item.getLocation() != null
                && requester.getLocation() != null
                && !item.getLocation().equalsIgnoreCase(requester.getLocation())) {
            throw new AccessDeniedException("You do not have access to inventory at this location");
        }
    }
}
