package com.virtualsphere.rfidbackend.controller;

import com.virtualsphere.rfidbackend.dto.LocationRequest;
import com.virtualsphere.rfidbackend.dto.LocationResponse;
import com.virtualsphere.rfidbackend.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * "Location Management" from BRD 2.2.3. Reads are open to any authenticated
 * user (both apps need the location list for dropdowns); writes are admin-only
 * (enforced in SecurityConfig) - so only the write methods below are tagged
 * "Admin - Locations" in Swagger; list() stays untagged since mobile uses it too.
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
    @Operation(tags = "Admin - Locations")
    public LocationResponse create(@Valid @RequestBody LocationRequest request) {
        return LocationResponse.from(locationService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(tags = "Admin - Locations")
    public LocationResponse update(@PathVariable Long id, @Valid @RequestBody LocationRequest request) {
        return LocationResponse.from(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(tags = "Admin - Locations")
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
