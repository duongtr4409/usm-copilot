package com.usm.ams.controller;

import com.usm.ams.dto.StaffCreateRequest;
import com.usm.ams.dto.StaffResponse;
import com.usm.ams.dto.StaffUpdateRequest;
import com.usm.ams.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<List<StaffResponse>> list() {
        return ResponseEntity.ok(staffService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.findById(id));
    }

    @PostMapping
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody StaffCreateRequest request) {
        StaffResponse resp = staffService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/staff/" + resp.id())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody StaffUpdateRequest request) {
        StaffResponse resp = staffService.update(id, request);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
