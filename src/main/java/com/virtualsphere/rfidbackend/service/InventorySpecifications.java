package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.model.InventoryItem;
import com.virtualsphere.rfidbackend.model.InventoryStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the "Filter search based on the Inventory details ... Location, Status,
 * Rack Id, Model No & product name" requirement (BRD 2.2.2) plus a free-text
 * search box, all as a single composable Specification.
 */
public final class InventorySpecifications {

    private InventorySpecifications() {
    }

    public static Specification<InventoryItem> search(String freeText, InventoryStatus status, String location,
                                                        String rackId, String modelNo, String category) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (freeText != null && !freeText.isBlank()) {
                String like = "%" + freeText.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("epc")), like),
                        cb.like(cb.lower(root.get("productName")), like),
                        cb.like(cb.lower(root.get("modelNo")), like),
                        cb.like(cb.lower(root.get("location")), like),
                        cb.like(cb.lower(root.get("rackId")), like),
                        cb.like(cb.lower(root.get("category")), like)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("location")), location.toLowerCase()));
            }
            if (rackId != null && !rackId.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("rackId")), rackId.toLowerCase()));
            }
            if (modelNo != null && !modelNo.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("modelNo")), modelNo.toLowerCase()));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
