package com.usm.ams.service;

import com.usm.ams.dto.StaffCreateRequest;
import com.usm.ams.dto.StaffResponse;
import com.usm.ams.dto.StaffUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface StaffService {
    List<StaffResponse> list();
    StaffResponse findById(UUID id);
    StaffResponse create(StaffCreateRequest request);
    StaffResponse update(UUID id, StaffUpdateRequest request);
    void delete(UUID id);
}
