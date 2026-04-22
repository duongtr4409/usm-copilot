package com.usm.ams.dto;
import java.util.UUID;

public record OrganizationUnitResponse(
        UUID id,
        String type,
        String code,
        String title
) {}
