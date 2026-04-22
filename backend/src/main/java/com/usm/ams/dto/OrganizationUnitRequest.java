package com.usm.ams.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record OrganizationUnitRequest(
        @NotBlank String type,
        @NotBlank String code,
        @NotBlank String title,
        UUID parentId
) {}
