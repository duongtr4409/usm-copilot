package com.usm.ams.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        UUID accountId,
        String username,
        String firstName,
        String lastName,
        LocalDate dob,
        OffsetDateTime createdAt
) {}
