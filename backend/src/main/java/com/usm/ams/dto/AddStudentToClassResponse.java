package com.usm.ams.dto;

import java.util.Map;
import java.util.UUID;

public record AddStudentToClassResponse(
        UUID studentId,
        UUID accountId,
        Map<String, String> links
) {}
