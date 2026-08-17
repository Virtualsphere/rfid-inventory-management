package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.InventoryRequest;
import com.virtualsphere.rfidbackend.dto.InventoryResponse;
import com.virtualsphere.rfidbackend.dto.ScanVerifyRequest;
import com.virtualsphere.rfidbackend.dto.ScanVerifyResponse;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.InventoryStatus;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import com.virtualsphere.rfidbackend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    private User currentUser(UserDetails principal) {
        return userRepository.findByUsernameIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
