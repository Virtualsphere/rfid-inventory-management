package com.virtualsphere.rfidbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mirrors the "inventory_items" table created by the admin desktop app - same
 * columns, same table name - so this backend and the desktop app share one schema.
 */
@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String epc;

    @Column(name = "product_name", length = 150)
    private String productName;

    @Column(name = "model_no", length = 100)
    private String modelNo;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String category;

    @Column(length = 150)
    private String location;

    @Column(name = "rack_id", length = 50)
    private String rackId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryStatus status = InventoryStatus.IN;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
