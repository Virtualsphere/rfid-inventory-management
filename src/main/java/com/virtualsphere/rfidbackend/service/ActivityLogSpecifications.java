package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.model.ActivityAction;
import com.virtualsphere.rfidbackend.model.ActivityLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Backs GET /api/activity-logs: the mobile app's scan/sync history, "my
 * activity" feed, and missing-item alert polling (action=MISSING_DETECTED
 * combined with since=<last poll time>) are all this one query with
 * different filters.
 */
public final class ActivityLogSpecifications {

    private ActivityLogSpecifications() {
    }

    public static Specification<ActivityLog> search(String epc, String location, ActivityAction action,
                                                       String performedBy, LocalDateTime since) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (epc != null && !epc.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("epc")), epc.toUpperCase()));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("location")), location.toLowerCase()));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (performedBy != null && !performedBy.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("performedBy")), performedBy.toLowerCase()));
            }
            if (since != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), since));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
