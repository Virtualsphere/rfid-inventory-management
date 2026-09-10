package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.ActivityAction;
import com.virtualsphere.rfidbackend.model.ActivityLog;

import java.time.LocalDateTime;

public record ActivityLogResponse(Long id, String epc, ActivityAction action, String performedBy, String location,
                                   String notes, LocalDateTime createdAt) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(log.getId(), log.getEpc(), log.getAction(), log.getPerformedBy(),
                log.getLocation(), log.getNotes(), log.getCreatedAt());
    }
}
