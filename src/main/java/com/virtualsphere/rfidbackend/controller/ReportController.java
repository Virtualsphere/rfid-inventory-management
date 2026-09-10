package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.ScanReportDetailResponse;
import com.virtualsphere.rfidbackend.dto.ScanReportItemResponse;
import com.virtualsphere.rfidbackend.dto.ScanReportSummaryResponse;
import com.virtualsphere.rfidbackend.dto.SingleReportExportRequest;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.Role;
import com.virtualsphere.rfidbackend.model.ScanReport;
import com.virtualsphere.rfidbackend.model.ScanReportItem;
import com.virtualsphere.rfidbackend.model.ScanReportType;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import com.virtualsphere.rfidbackend.service.ExcelReportService;
import com.virtualsphere.rfidbackend.service.ScanReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ScanReportService scanReportService;
    private final ExcelReportService excelReportService;
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

    /**
     * Excel export for one bulk scan-verify session - the mobile app's
     * "Export" button on the Bulk Scan report screen. Two sheets: Summary
     * (header/counts) and Items (every EPC with its category).
     */
    @GetMapping("/bulk/{id}/export")
    public ResponseEntity<byte[]> bulkExport(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        User requester = currentUser(principal);
        ScanReport report = scanReportService.getById(id);
        assertAccess(requester, report);

        List<ScanReportItem> items = scanReportService.getItems(id);
        byte[] workbook = excelReportService.bulkReportWorkbook(report, items);
        return excelResponse(workbook, "bulk-scan-report-" + id + ".xlsx");
    }

    /**
     * Excel export for a one-by-one scanning session - the mobile app calls
     * scan-log once per tag, collects each response's reportId client-side,
     * and posts the whole list here when the operator taps "Export". One
     * combined workbook covering every scan in that session.
     */
    @PostMapping("/single/export")
    public ResponseEntity<byte[]> singleExport(@Valid @RequestBody SingleReportExportRequest request,
                                                @AuthenticationPrincipal UserDetails principal) {
        User requester = currentUser(principal);
        List<ScanReport> reports = scanReportService.getByIds(request.getReportIds());

        for (ScanReport report : reports) {
            if (report.getType() != ScanReportType.SINGLE) {
                throw new IllegalArgumentException("Report " + report.getId() + " is not a single-scan report");
            }
            assertAccess(requester, report);
        }

        List<Long> ids = reports.stream().map(ScanReport::getId).toList();
        List<ScanReportItem> items = scanReportService.getItems(ids);
        byte[] workbook = excelReportService.singleReportsWorkbook(reports, items);
        return excelResponse(workbook, "single-scan-report-" + LocalDate.now() + ".xlsx");
    }

    private ResponseEntity<byte[]> excelResponse(byte[] workbook, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(XLSX)
                .body(workbook);
    }

    private void assertAccess(User requester, ScanReport report) {
        if (requester.getRole() == Role.USER) {
            boolean ownScan = requester.getUsername().equalsIgnoreCase(report.getPerformedBy());
            boolean ownLocation = requester.getLocation() != null
                    && requester.getLocation().equalsIgnoreCase(report.getLocation());
            if (!ownScan && !ownLocation) {
                throw new AccessDeniedException("You do not have access to report " + report.getId());
            }
        }
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
        assertAccess(requester, report);

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
