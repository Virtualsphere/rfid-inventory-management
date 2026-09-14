package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.CheckpointScanRequest;
import com.virtualsphere.rfidbackend.dto.CheckpointScanResponse;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import com.virtualsphere.rfidbackend.service.SecurityCheckpointService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints, kept under their own /api/admin path and Swagger section so
 * they read as clearly separate from the mobile app's own API surface, even where
 * both ultimately touch the same InventoryItem data. Nothing here is called by the
 * mobile app - see SecurityCheckpointService for why this doesn't reuse the mobile
 * scan-log/scan-verify endpoints.
 */
@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@Tag(name = "Admin - Security")
public class AdminSecurityController {

    private final SecurityCheckpointService securityCheckpointService;
    private final UserRepository userRepository;

    /**
     * Called by the admin desktop's Security Reader page for every tag its gate
     * reader sees. Replaces that page's previous direct-to-database lookup +
     * activity-log write - the desktop keeps the reader hardware I/O (COM/IP
     * connect, buzzer/light GPO) local, but the inventory lookup and
     * SECURITY_ALERT logging now happen server-side.
     */
    @PostMapping("/checkpoint-scan")
    public CheckpointScanResponse checkpointScan(@Valid @RequestBody CheckpointScanRequest request,
                                                  @AuthenticationPrincipal UserDetails principal) {
        String performedBy = userRepository.findByUsernameIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getUsername();
        return securityCheckpointService.checkpointScan(request.getEpc(), performedBy);
    }
}
