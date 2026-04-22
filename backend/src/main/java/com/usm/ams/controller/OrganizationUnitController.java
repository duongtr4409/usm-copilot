package com.usm.ams.controller;

import com.usm.ams.dto.OrganizationUnitCreateRequest;
import com.usm.ams.dto.OrganizationUnitResponse;
import com.usm.ams.dto.OrganizationUnitUpdateRequest;
import com.usm.ams.service.OrganizationUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org-units")
public class OrganizationUnitController {
  
    private final OrganizationUnitService organizationUnitService;

    public OrganizationUnitController(OrganizationUnitService organizationUnitService) {
        this.organizationUnitService = organizationUnitService;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationUnitResponse>> list(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(organizationUnitService.list(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationUnitResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationUnitService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrganizationUnitResponse> create(@Valid @RequestBody OrganizationUnitCreateRequest request) {
        OrganizationUnitResponse resp = organizationUnitService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/org-units/" + resp.id())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationUnitResponse> update(@PathVariable UUID id,
                                                            @Valid @RequestBody OrganizationUnitUpdateRequest request) {
        OrganizationUnitResponse resp = organizationUnitService.update(id, request);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        organizationUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
