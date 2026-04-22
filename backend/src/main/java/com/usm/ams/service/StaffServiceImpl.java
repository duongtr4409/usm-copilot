package com.usm.ams.service;

import com.usm.ams.dto.StaffCreateRequest;
import com.usm.ams.dto.StaffResponse;
import com.usm.ams.dto.StaffUpdateRequest;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.entity.Staff;
import com.usm.ams.entity.UserAccount;
import com.usm.ams.exception.ResourceNotFoundException;
import com.usm.ams.repository.OrganizationUnitRepository;
import com.usm.ams.repository.StaffRepository;
import com.usm.ams.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final UserAccountRepository userAccountRepository;
    private final OrganizationUnitRepository organizationUnitRepository;

    public StaffServiceImpl(StaffRepository staffRepository,
                            UserAccountRepository userAccountRepository,
                            OrganizationUnitRepository organizationUnitRepository) {
        this.staffRepository = staffRepository;
        this.userAccountRepository = userAccountRepository;
        this.organizationUnitRepository = organizationUnitRepository;
    }

    @Override
    public List<StaffResponse> list() {
        return staffRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public StaffResponse findById(UUID id) {
        Staff s = staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        return toResponse(s);
    }

    @Override
    @Transactional
    public StaffResponse create(StaffCreateRequest request) {
        Staff s = new Staff();

        if (request.accountId() != null) {
            UserAccount acc = userAccountRepository.findById(request.accountId())
                    .orElseThrow(() -> new ResourceNotFoundException("UserAccount not found"));
            s.setAccount(acc);
        }

        s.setStaffNumber(request.staffNumber());
        s.setFullName(request.fullName());
        s.setPosition(request.position());

        if (request.unitId() != null) {
            OrganizationUnit unit = organizationUnitRepository.findById(request.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrganizationUnit not found"));
            s.setUnit(unit);
        }

        s.setContact(request.contact());
        s.setStatus(request.status() == null ? "ACTIVE" : request.status());

        Staff saved = staffRepository.save(s);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public StaffResponse update(UUID id, StaffUpdateRequest request) {
        Staff s = staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        if (request.accountId() != null) {
            UserAccount acc = userAccountRepository.findById(request.accountId())
                    .orElseThrow(() -> new ResourceNotFoundException("UserAccount not found"));
            s.setAccount(acc);
        } else {
            s.setAccount(null);
        }

        s.setStaffNumber(request.staffNumber());
        s.setFullName(request.fullName());
        s.setPosition(request.position());

        if (request.unitId() != null) {
            OrganizationUnit unit = organizationUnitRepository.findById(request.unitId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrganizationUnit not found"));
            s.setUnit(unit);
        } else {
            s.setUnit(null);
        }

        s.setContact(request.contact());
        s.setStatus(request.status() == null ? s.getStatus() : request.status());

        Staff saved = staffRepository.save(s);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staff not found");
        }
        staffRepository.deleteById(id);
    }

    private com.usm.ams.dto.StaffResponse toResponse(Staff s) {
        return new com.usm.ams.dto.StaffResponse(
                s.getId(),
                s.getAccount() == null ? null : s.getAccount().getId(),
                s.getStaffNumber(),
                s.getFullName(),
                s.getPosition(),
                s.getUnit() == null ? null : s.getUnit().getId(),
                s.getContact(),
                s.getStatus(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                s.getCreatedBy(),
                s.getUpdatedBy()
        );
    }
}
