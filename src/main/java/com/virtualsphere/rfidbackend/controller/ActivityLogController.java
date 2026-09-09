package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.ActivityLogResponse;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.ActivityAction;
import com.virtualsphere.rfidbackend.model.Role;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import com.virtualsphere.rfidbackend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One flexible feed behind three mobile handheld needs from the BRD: per-EPC
 * scan history (2.1.2 "Maintain Records of Scanned vs. Available Inventory
 * Data"), an operator's own sync history, and missing-item alert polling
 * (2.1.2 "Send Status Alerts for Unavailable Inventory Items" - poll with
 * action=MISSING_DETECTED&since=<last poll time>). Non-admin users are
 * always scoped to their assigned location, same as inventory search.
 */
@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    @GetMapping
    public List<ActivityLogResponse> list(
            @RequestParam(required = false) String epc,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) ActivityAction action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails principal) {
        User requester = userRepository.findByUsernameIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String effectiveLocation = requester.getRole() == Role.USER ? requester.getLocation() : location;
        String effectivePerformedBy = "me".equalsIgnoreCase(performedBy) ? requester.getUsername() : performedBy;

        return activityLogService.query(epc, effectiveLocation, action, effectivePerformedBy, since, limit).stream()
                .map(ActivityLogResponse::from)
                .toList();
    }
}
