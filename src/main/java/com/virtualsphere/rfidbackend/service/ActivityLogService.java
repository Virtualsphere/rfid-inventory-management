package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.model.ActivityAction;
import com.virtualsphere.rfidbackend.model.ActivityLog;
import com.virtualsphere.rfidbackend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private static final int MAX_LIMIT = 200;

    private final ActivityLogRepository activityLogRepository;

    public void log(String epc, ActivityAction action, String performedBy, String location, String notes) {
        log(epc, action, performedBy, location, notes, LocalDateTime.now());
    }

    /**
     * Same as log(), but lets a caller record when the action actually happened
     * rather than when the server processed it - used by the mobile sync
     * endpoint so an offline scan keeps its real timestamp after a delayed sync.
     */
    public void log(String epc, ActivityAction action, String performedBy, String location, String notes,
                     LocalDateTime occurredAt) {
        ActivityLog entry = new ActivityLog();
        entry.setEpc(epc);
        entry.setAction(action);
        entry.setPerformedBy(performedBy);
        entry.setLocation(location);
        entry.setNotes(notes);
        entry.setCreatedAt(occurredAt != null ? occurredAt : LocalDateTime.now());
        activityLogRepository.save(entry);
    }

    /**
     * Backs GET /api/activity-logs (scan history, sync history, missing-item
     * alert polling). Caller is responsible for location scoping by role.
     */
    public List<ActivityLog> query(String epc, String location, ActivityAction action, String performedBy,
                                    LocalDateTime since, int limit) {
        int effectiveLimit = limit <= 0 ? 50 : Math.min(limit, MAX_LIMIT);
        return activityLogRepository.findAll(
                ActivityLogSpecifications.search(epc, location, action, performedBy, since),
                PageRequest.of(0, effectiveLimit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
}
