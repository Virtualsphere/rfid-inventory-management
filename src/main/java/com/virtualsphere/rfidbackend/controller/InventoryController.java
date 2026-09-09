package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.InventoryRequest;
import com.virtualsphere.rfidbackend.dto.InventoryResponse;
import com.virtualsphere.rfidbackend.dto.InventorySyncRequest;
import com.virtualsphere.rfidbackend.dto.InventorySyncResponse;
import com.virtualsphere.rfidbackend.dto.ScanVerifyRequest;
import com.virtualsphere.rfidbackend.dto.ScanVerifyResponse;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.InventoryItem;
import com.virtualsphere.rfidbackend.model.InventoryStatus;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import com.virtualsphere.rfidbackend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shared by both the admin web panel and the mobile handheld app - the "Asset
 * Management" CRUD from BRD 2.2.3 plus the mobile "Inventory scanning /
 * Exception handling" workflow from BRD 2.2.4 (scan-verify below).
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping
    public List<InventoryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String rackId,
            @RequestParam(required = false) String modelNo,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserDetails principal) {
        User requester = currentUser(principal);
        return inventoryService.search(q, status, location, rackId, modelNo, category, requester).stream()
                .map(InventoryResponse::from)
                .toList();
    }

    @GetMapping("/{epc}")
    public InventoryResponse getByEpc(@PathVariable String epc, @AuthenticationPrincipal UserDetails principal) {
        return InventoryResponse.from(inventoryService.findByEpc(epc, currentUser(principal)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(@Valid @RequestBody InventoryRequest request,
                                     @AuthenticationPrincipal UserDetails principal) {
        return InventoryResponse.from(inventoryService.create(request, currentUser(principal)));
    }

    @PutMapping("/{id}")
    public InventoryResponse update(@PathVariable Long id, @Valid @RequestBody InventoryRequest request,
                                     @AuthenticationPrincipal UserDetails principal) {
        return InventoryResponse.from(inventoryService.update(id, request, currentUser(principal)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        inventoryService.delete(id, currentUser(principal).getUsername());
    }

    /**
     * Mobile bulk-scan verification: body = { "location": "...", "scannedEpcs": [...] }.
     * Anything expected at that location (status=IN) but not present in scannedEpcs
     * gets flagged MISSING and returned in the response for the "Missing/Unavailable
     * Inventory" report (BRD 2.1.2).
     */
    @PostMapping("/scan-verify")
    public ScanVerifyResponse scanVerify(@Valid @RequestBody ScanVerifyRequest request,
                                          @AuthenticationPrincipal UserDetails principal) {
        return inventoryService.scanVerify(request, currentUser(principal));
    }

    /**
     * Mobile offline-sync / bulk-scan endpoint (BRD 2.2.4 "Offline mode with
     * sync capability" and "Bulk scan mode"): body = a batch of {epc, status,
     * location, scannedAt, notes} events, either queued locally while offline
     * or sent live. Each event keeps its own scannedAt so status history stays
     * accurate regardless of when the sync call actually reaches the server.
     */
    @PostMapping("/sync")
    public InventorySyncResponse sync(@Valid @RequestBody InventorySyncRequest request,
                                       @AuthenticationPrincipal UserDetails principal) {
        return inventoryService.syncEvents(request, currentUser(principal));
    }

    /**
     * CSV export for the mobile/desktop "Generate Reports for Inventory Status
     * Discrepancies" and "Export Detailed Reports of Missing/Unavailable
     * Inventory for Cross-Verification" requirements (BRD 2.1.2). Accepts the
     * same filters as the list endpoint - e.g. ?status=MISSING&location=X for
     * the missing-items report.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String rackId,
            @RequestParam(required = false) String modelNo,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserDetails principal) {
        User requester = currentUser(principal);
        List<InventoryItem> items = inventoryService.search(q, status, location, rackId, modelNo, category, requester);

        byte[] csv = toCsv(items);
        String filename = "inventory-export-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private static byte[] toCsv(List<InventoryItem> items) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("EPC,Product Name,Model No,Price,Category,Location,Rack ID,Status,Last Scanned At\n");
        for (InventoryItem item : items) {
            sb.append(csvField(item.getEpc())).append(',')
                    .append(csvField(item.getProductName())).append(',')
                    .append(csvField(item.getModelNo())).append(',')
                    .append(csvField(item.getPrice() != null ? item.getPrice().toString() : "")).append(',')
                    .append(csvField(item.getCategory())).append(',')
                    .append(csvField(item.getLocation())).append(',')
                    .append(csvField(item.getRackId())).append(',')
                    .append(csvField(item.getStatus().name())).append(',')
                    .append(csvField(item.getLastScannedAt() != null ? item.getLastScannedAt().format(fmt) : ""))
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String csvField(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByUsernameIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
