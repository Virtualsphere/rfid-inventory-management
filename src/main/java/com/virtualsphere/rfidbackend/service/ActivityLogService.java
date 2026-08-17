package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.model.ActivityAction;
import com.virtualsphere.rfidbackend.model.ActivityLog;
import com.virtualsphere.rfidbackend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void log(String epc, ActivityAction action, String performedBy, String location, String notes) {
        ActivityLog entry = new ActivityLog();
        entry.setEpc(epc);
        entry.setAction(action);
        entry.setPerformedBy(performedBy);
        entry.setLocation(location);
        entry.setNotes(notes);
        activityLogRepository.save(entry);
    }
}
