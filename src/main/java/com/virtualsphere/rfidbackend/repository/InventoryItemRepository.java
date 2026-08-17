package com.virtualsphere.rfidbackend.repository;

import com.virtualsphere.rfidbackend.model.InventoryItem;
import com.virtualsphere.rfidbackend.model.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long>,
        JpaSpecificationExecutor<InventoryItem> {

    boolean existsByEpcIgnoreCase(String epc);
    Optional<InventoryItem> findByEpcIgnoreCase(String epc);
    List<InventoryItem> findAllByLocationIgnoreCase(String location);
    List<InventoryItem> findAllByLocationIgnoreCaseAndStatus(String location, InventoryStatus status);
}
