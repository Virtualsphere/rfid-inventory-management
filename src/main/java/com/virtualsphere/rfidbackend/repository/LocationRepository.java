package com.virtualsphere.rfidbackend.repository;

import com.virtualsphere.rfidbackend.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Location> findByNameIgnoreCase(String name);
    List<Location> findAllByOrderByNameAsc();
}
