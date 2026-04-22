package com.usm.ams.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record StudentCreateRequest(
        @NotBlank String username,
        @NotBlank String initialPassword,
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dob
) {}
