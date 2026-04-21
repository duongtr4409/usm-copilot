package com.usm.ams.controller;

import com.usm.ams.dto.AddStudentToClassRequest;
import com.usm.ams.dto.AddStudentToClassResponse;
import com.usm.ams.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassesController {

    private final StudentService studentService;

    public ClassesController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/{classId}/students")
    @PreAuthorize("hasRole('ADMIN') or @aclService.isClassAdmin(authentication, #classId)")
    public ResponseEntity<AddStudentToClassResponse> addStudentToClass(
            @PathVariable UUID classId,
            @Validated @RequestBody AddStudentToClassRequest request) {
        AddStudentToClassResponse resp = studentService.addStudentToClass(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
