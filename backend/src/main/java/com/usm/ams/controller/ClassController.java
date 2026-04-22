package com.usm.ams.controller;

import com.usm.ams.dto.ClassDto;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.repository.OrganizationUnitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    private final OrganizationUnitRepository organizationUnitRepository;

    public ClassController(OrganizationUnitRepository organizationUnitRepository) {
        this.organizationUnitRepository = organizationUnitRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClassDto>> listClasses(@RequestParam(required = false) String type) {
        String t = (type == null || type.isBlank()) ? "Lớp" : type;
        List<OrganizationUnit> units = organizationUnitRepository.findByType(t);
        List<ClassDto> dtos = units.stream()
                .map(u -> new ClassDto(u.getId(), u.getCode(), u.getTitle()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
