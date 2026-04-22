package com.usm.ams.dto;

import java.util.Map;
import java.util.UUID;

public record StaffRequest(String staffNumber, String fullName, String position, UUID unitId, Map<String,Object> contact) {}
