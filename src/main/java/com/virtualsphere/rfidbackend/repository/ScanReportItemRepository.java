package com.virtualsphere.rfidbackend.repository;

import com.virtualsphere.rfidbackend.model.ScanReportItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanReportItemRepository extends JpaRepository<ScanReportItem, Long> {
    List<ScanReportItem> findAllByReportId(Long reportId);
}
