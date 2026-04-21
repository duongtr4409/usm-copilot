package com.usm.ams.service;

import com.usm.ams.dto.AddStudentToClassRequest;
import com.usm.ams.dto.AddStudentToClassResponse;

import java.util.UUID;

public interface StudentService {
    AddStudentToClassResponse addStudentToClass(UUID classId, AddStudentToClassRequest request);
}
