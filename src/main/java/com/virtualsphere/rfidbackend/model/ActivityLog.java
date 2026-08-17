package com.virtualsphere.rfidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Backs the "Activity logs" and inward/outward dashboard modules from BRD 2.2.2,
 * and the audit trail behind scan-verification results.
 */
@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String epc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityAction action;

    @Column(name = "performed_by", length = 60)
    private String performedBy;

    @Column(length = 150)
    private String location;

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
