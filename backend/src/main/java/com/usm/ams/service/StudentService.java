package com.usm.ams.service;

import com.usm.ams.dto.AddStudentToClassRequest;
import com.usm.ams.dto.AddStudentToClassResponse;
import com.usm.ams.dto.StudentProfileResponse;

import java.util.UUID;
import java.util.List;

public interface StudentService {
    AddStudentToClassResponse addStudentToClass(UUID classId, AddStudentToClassRequest request);

    List<StudentProfileResponse> getStudentsInClass(UUID classId);
}
