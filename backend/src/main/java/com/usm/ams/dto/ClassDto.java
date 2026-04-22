package com.usm.ams.dto;

import java.util.UUID;

public record ClassDto(
        UUID id,
        String code,
        String title
) {}
