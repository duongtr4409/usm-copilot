package com.usm.ams.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizationUnitResponse(
        UUID id,
        String type,
        String code,
        String title,
        UUID parentId,
        OffsetDateTime createdAt
) {}
