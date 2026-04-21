package com.usm.ams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AddStudentToClassRequest(
        @NotBlank String username,
        @NotBlank String initialPassword,
        @NotNull Profile profile
) {
    public static record Profile(
            @NotBlank String firstName,
            @NotBlank String lastName,
            LocalDate dob,
            java.util.Map<String, Object> guardianInfo
    ) {}
}
