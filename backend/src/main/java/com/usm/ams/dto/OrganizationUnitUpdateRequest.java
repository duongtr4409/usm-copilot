package com.usm.ams.dto;

import jakarta.validation.constraints.NotBlank;

public record OrganizationUnitUpdateRequest(
        @NotBlank String type,
        String code,
        @NotBlank String title
) {}
