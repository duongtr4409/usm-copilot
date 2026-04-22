package com.usm.ams.controller;

import com.usm.ams.dto.OrganizationUnitRequest;
import com.usm.ams.dto.OrganizationUnitResponse;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.repository.OrganizationUnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/org-units")
public class OrganizationUnitController {

    private final OrganizationUnitRepository repo;

    public OrganizationUnitController(OrganizationUnitRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationUnitResponse>> list() {
        List<OrganizationUnitResponse> out = repo.findAll().stream().map(u ->
                new OrganizationUnitResponse(u.getId(), u.getType(), u.getCode(), u.getTitle(), null, null)
        ).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationUnitResponse> create(@Validated @RequestBody OrganizationUnitRequest req) {
        OrganizationUnit u = new OrganizationUnit(req.type(), req.code(), req.title());
        OrganizationUnit saved = repo.save(u);
        OrganizationUnitResponse resp = new OrganizationUnitResponse(saved.getId(), saved.getType(), saved.getCode(), saved.getTitle(), null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationUnitResponse> update(@PathVariable UUID id, @Validated @RequestBody OrganizationUnitRequest req) {
        OrganizationUnit u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Organization unit not found"));
        u.setType(req.type());
        u.setCode(req.code());
        u.setTitle(req.title());
        OrganizationUnit saved = repo.save(u);
        OrganizationUnitResponse resp = new OrganizationUnitResponse(saved.getId(), saved.getType(), saved.getCode(), saved.getTitle(), null, null);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
