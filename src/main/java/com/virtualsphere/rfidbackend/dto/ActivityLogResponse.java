package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.ActivityLog;

import java.time.LocalDateTime;

public record ActivityLogResponse(Long id, String epc, String action, String performedBy, String location,
                                   String notes, LocalDateTime createdAt) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(log.getId(), log.getEpc(), log.getAction().name(), log.getPerformedBy(),
                log.getLocation(), log.getNotes(), log.getCreatedAt());
    }
}
