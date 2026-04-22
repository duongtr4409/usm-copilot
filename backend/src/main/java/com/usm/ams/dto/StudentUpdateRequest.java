package com.usm.ams.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record StudentUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dob
) {}
