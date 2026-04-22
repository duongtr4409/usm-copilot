package com.usm.ams.service;

import com.usm.ams.dto.OrganizationUnitCreateRequest;
import com.usm.ams.dto.OrganizationUnitResponse;
import com.usm.ams.dto.OrganizationUnitUpdateRequest;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.exception.ResourceNotFoundException;
import com.usm.ams.repository.OrganizationUnitRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationUnitServiceImpl implements OrganizationUnitService {

    private final OrganizationUnitRepository organizationUnitRepository;

    public OrganizationUnitServiceImpl(OrganizationUnitRepository organizationUnitRepository) {
        this.organizationUnitRepository = organizationUnitRepository;
    }

    @Override
    public List<OrganizationUnitResponse> list(String type) {
        List<OrganizationUnit> units;
        if (type == null || type.isBlank()) {
            units = organizationUnitRepository.findAll();
        } else {
            units = organizationUnitRepository.findByType(type);
        }
        return units.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public OrganizationUnitResponse findById(UUID id) {
        OrganizationUnit unit = organizationUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizationUnit not found"));
        return toResponse(unit);
    }

    @Override
    @Transactional
    public OrganizationUnitResponse create(OrganizationUnitCreateRequest request) {
        OrganizationUnit unit = new OrganizationUnit(request.type(), request.code(), request.title());
        OrganizationUnit saved = organizationUnitRepository.save(unit);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public OrganizationUnitResponse update(UUID id, OrganizationUnitUpdateRequest request) {
        OrganizationUnit unit = organizationUnitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizationUnit not found"));
        unit.setType(request.type());
        unit.setCode(request.code());
        unit.setTitle(request.title());
        OrganizationUnit saved = organizationUnitRepository.save(unit);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!organizationUnitRepository.existsById(id)) {
            throw new ResourceNotFoundException("OrganizationUnit not found");
        }
        organizationUnitRepository.deleteById(id);
    }

    private OrganizationUnitResponse toResponse(OrganizationUnit u) {
        return new OrganizationUnitResponse(u.getId(), u.getType(), u.getCode(), u.getTitle());
    }
}
