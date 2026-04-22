package com.usm.ams.service;

import com.usm.ams.dto.AddStudentToClassRequest;
import com.usm.ams.dto.AddStudentToClassResponse;
import com.usm.ams.dto.StudentProfileResponse;
import com.usm.ams.dto.StudentCreateRequest;
import com.usm.ams.dto.StudentResponse;
import com.usm.ams.dto.StudentUpdateRequest;

import java.util.UUID;
import java.util.List;

public interface StudentService {
    AddStudentToClassResponse addStudentToClass(UUID classId, AddStudentToClassRequest request);
    List<StudentProfileResponse> getStudentsInClass(UUID classId);

    List<StudentResponse> list(String firstName, String lastName, String username, UUID classId);
    StudentResponse findById(UUID id);
    StudentResponse create(StudentCreateRequest request);
    StudentResponse update(UUID id, StudentUpdateRequest request);
    void delete(UUID id);
}
