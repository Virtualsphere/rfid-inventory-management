package com.virtualsphere.rfidbackend.repository;

import com.virtualsphere.rfidbackend.model.ScanReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScanReportRepository extends JpaRepository<ScanReport, Long>,
        JpaSpecificationExecutor<ScanReport> {
}
