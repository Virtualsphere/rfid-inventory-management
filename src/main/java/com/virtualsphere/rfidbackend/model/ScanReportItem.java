package com.virtualsphere.rfidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One EPC's outcome within a ScanReport. Kept as a plain reportId column
 * rather than a JPA relation, matching how the rest of this schema avoids
 * entity associations (see InventoryItem / ActivityLog).
 */
@Entity
@Table(name = "scan_report_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanReportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(nullable = false, length = 64)
    private String epc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ScanCategory category;

    /** Snapshot at report time - so the report still reads correctly even if the item is edited/deleted later. */
    @Column(name = "product_name", length = 150)
    private String productName;

    /** The item's actual registered location - the interesting field for an UNEXPECTED row. */
    @Column(name = "item_location", length = 150)
    private String itemLocation;

    /** The item's status immediately before this scan - the interesting field for an UNAVAILABLE row. */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private InventoryStatus previousStatus;
}
