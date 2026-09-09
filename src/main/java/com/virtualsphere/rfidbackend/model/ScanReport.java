package com.virtualsphere.rfidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One scan submission from the handheld app - either a single ad hoc tag
 * read or a full bulk scan-verify pass. Shared with the admin desktop app's
 * new Reports page (same table, mirrored entity there), same as every other
 * table in this schema.
 */
@Entity
@Table(name = "scan_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ScanReportType type;

    @Column(length = 150)
    private String location;

    @Column(name = "performed_by", length = 60)
    private String performedBy;

    /** When the physical scan actually happened - may be earlier than createdAt if synced late. */
    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    /** Bulk only - how long the scan pass took on the handheld, in seconds. */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /** Bulk only - how many items were expected (status=IN) at this location before the scan. */
    @Column(name = "expected_count")
    private Integer expectedCount;

    @Column(name = "found_count", nullable = false)
    private int foundCount;

    /** Bulk only - expected items not seen in this pass. */
    @Column(name = "missing_count", nullable = false)
    private int missingCount;

    @Column(name = "unexpected_count", nullable = false)
    private int unexpectedCount;

    @Column(name = "unknown_count", nullable = false)
    private int unknownCount;

    @Column(name = "unavailable_count", nullable = false)
    private int unavailableCount;

    @Column(name = "total_detected", nullable = false)
    private int totalDetected;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
