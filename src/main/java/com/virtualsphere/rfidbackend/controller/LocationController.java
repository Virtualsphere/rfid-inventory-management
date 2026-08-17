package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.LocationRequest;
import com.virtualsphere.rfidbackend.dto.LocationResponse;
import com.virtualsphere.rfidbackend.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * "Location Management" from BRD 2.2.3. Reads are open to any authenticated
 * user (both apps need the location list for dropdowns); writes are admin-only
 * (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public List<LocationResponse> list() {
        return locationService.findAll().stream().map(LocationResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse create(@Valid @RequestBody LocationRequest request) {
        return LocationResponse.from(locationService.create(request));
    }

    @PutMapping("/{id}")
    public LocationResponse update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return LocationResponse.from(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
