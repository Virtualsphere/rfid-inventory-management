package com.virtualsphere.rfidbackend.repository;

import com.virtualsphere.rfidbackend.model.InventoryItem;
import com.virtualsphere.rfidbackend.model.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long>,
        JpaSpecificationExecutor<InventoryItem> {

    boolean existsByEpcIgnoreCase(String epc);
    Optional<InventoryItem> findByEpcIgnoreCase(String epc);
    List<InventoryItem> findAllByLocationIgnoreCase(String location);
    List<InventoryItem> findAllByLocationIgnoreCaseAndStatus(String location, InventoryStatus status);

    /**
     * Bulk lookup for the mobile sync endpoint, so a batch of scan events is one
     * query instead of N. EPCs are always persisted upper-cased (see
     * InventoryService.create), so callers must upper-case theirs too.
     */
    List<InventoryItem> findAllByEpcIn(Collection<String> epcs);
}
