package com.virtualsphere.rfidbackend.repository;

import com.virtualsphere.rfidbackend.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>,
        JpaSpecificationExecutor<ActivityLog> {
    List<ActivityLog> findTop20ByOrderByCreatedAtDesc();
    List<ActivityLog> findTop20ByLocationIgnoreCaseOrderByCreatedAtDesc(String location);
    List<ActivityLog> findAllByEpcIgnoreCaseOrderByCreatedAtDesc(String epc);
}
