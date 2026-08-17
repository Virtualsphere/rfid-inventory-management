package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.dto.LocationRequest;
import com.virtualsphere.rfidbackend.exception.DuplicateResourceException;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.Location;
import com.virtualsphere.rfidbackend.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<Location> findAll() {
        return locationRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Location create(LocationRequest request) {
        if (locationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A location with this name already exists: " + request.getName());
        }
        Location location = new Location();
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        return locationRepository.save(location);
    }

    @Transactional
    public Location update(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));

        if (request.getName() != null && !request.getName().equalsIgnoreCase(location.getName())
                && locationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A location with this name already exists: " + request.getName());
        }
        if (request.getName() != null) location.setName(request.getName());
        if (request.getAddress() != null) location.setAddress(request.getAddress());
        return locationRepository.save(location);
    }

    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found: " + id);
        }
        locationRepository.deleteById(id);
    }
}
