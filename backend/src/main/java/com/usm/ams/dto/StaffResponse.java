package com.usm.ams.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        UUID accountId,
        String staffNumber,
        String fullName,
        String position,
        UUID unitId,
        String contact,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
