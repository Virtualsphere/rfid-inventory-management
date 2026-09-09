package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.ScanReportDetailResponse;
import com.virtualsphere.rfidbackend.dto.ScanReportItemResponse;
import com.virtualsphere.rfidbackend.dto.ScanReportSummaryResponse;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.Role;
import com.virtualsphere.rfidbackend.model.ScanReport;
import com.virtualsphere.rfidbackend.model.ScanReportType;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import com.virtualsphere.rfidbackend.service.ScanReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Backs the admin desktop app's Reports page (Single scans / Bulk scans tabs
 * with a date filter) and the mobile app's own "Scan History" screen - same
 * data, same role scoping as everywhere else: a USER only ever sees their
 * own scans at their own location, ADMIN sees everything.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ScanReportService scanReportService;
    private final UserRepository userRepository;

    @GetMapping("/single")
    public List<ScanReportSummaryResponse> single(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails principal) {
        return list(ScanReportType.SINGLE, location, performedBy, from, to, limit, principal);
    }

    @GetMapping("/bulk")
    public List<ScanReportSummaryResponse> bulk(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails principal) {
        return list(ScanReportType.BULK, location, performedBy, from, to, limit, principal);
    }

    @GetMapping("/bulk/{id}")
    public ScanReportDetailResponse bulkDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        return detail(id, principal);
    }

    @GetMapping("/single/{id}")
    public ScanReportDetailResponse singleDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        return detail(id, principal);
    }

    private List<ScanReportSummaryResponse> list(ScanReportType type, String location, String performedBy,
                                                  LocalDateTime from, LocalDateTime to, int limit,
                                                  UserDetails principal) {
        User requester = currentUser(principal);
        String effectiveLocation = requester.getRole() == Role.USER ? requester.getLocation() : location;
        String effectivePerformedBy = requester.getRole() == Role.USER ? requester.getUsername()
                : ("me".equalsIgnoreCase(performedBy) ? requester.getUsername() : performedBy);

        return scanReportService.query(type, effectiveLocation, effectivePerformedBy, from, to, limit).stream()
                .map(ScanReportSummaryResponse::from)
                .toList();
    }

    private ScanReportDetailResponse detail(Long id, UserDetails principal) {
        User requester = currentUser(principal);
        ScanReport report = scanReportService.getById(id);

        if (requester.getRole() == Role.USER) {
            boolean ownScan = requester.getUsername().equalsIgnoreCase(report.getPerformedBy());
            boolean ownLocation = requester.getLocation() != null
                    && requester.getLocation().equalsIgnoreCase(report.getLocation());
            if (!ownScan && !ownLocation) {
                throw new AccessDeniedException("You do not have access to this report");
            }
        }

        List<ScanReportItemResponse> items = scanReportService.getItems(id).stream()
                .map(ScanReportItemResponse::from)
                .toList();
        return new ScanReportDetailResponse(ScanReportSummaryResponse.from(report), items);
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByUsernameIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
