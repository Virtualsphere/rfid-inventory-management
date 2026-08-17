package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.DashboardSummaryResponse;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import com.virtualsphere.rfidbackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@AuthenticationPrincipal UserDetails principal) {
        User requester = userRepository.findByUsernameIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return dashboardService.getSummary(requester);
    }
}
