package com.usm.ams.dto;

import java.time.LocalDate;
import java.util.UUID;

public record StudentProfileResponse(
        UUID id,
        UUID accountId,
        String firstName,
        String lastName,
        LocalDate dob
) {}
