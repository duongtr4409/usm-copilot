package com.usm.ams.service;

import com.usm.ams.dto.OrganizationUnitCreateRequest;
import com.usm.ams.dto.OrganizationUnitResponse;
import com.usm.ams.dto.OrganizationUnitUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface OrganizationUnitService {
    List<OrganizationUnitResponse> list(String type);
    OrganizationUnitResponse findById(UUID id);
    OrganizationUnitResponse create(OrganizationUnitCreateRequest request);
    OrganizationUnitResponse update(UUID id, OrganizationUnitUpdateRequest request);
    void delete(UUID id);
}
