package com.usm.ams.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record StaffCreateRequest(
        UUID accountId,
        String staffNumber,
        @NotBlank String fullName,
        String position,
        UUID unitId,
        String contact,
        String status
) {}
