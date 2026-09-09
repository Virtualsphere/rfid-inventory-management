package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.model.ScanReport;
import com.virtualsphere.rfidbackend.model.ScanReportType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Backs the admin Reports page's two tabs (type) plus its date range / location / operator filters. */
public final class ScanReportSpecifications {

    private ScanReportSpecifications() {
    }

    public static Specification<ScanReport> search(ScanReportType type, String location, String performedBy,
                                                     LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("location")), location.toLowerCase()));
            }
            if (performedBy != null && !performedBy.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("performedBy")), performedBy.toLowerCase()));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scannedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scannedAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
