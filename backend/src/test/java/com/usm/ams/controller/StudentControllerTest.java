package com.usm.ams.controller;

import com.usm.ams.dto.StudentCreateRequest;
import com.usm.ams.dto.StudentResponse;
import com.usm.ams.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    StudentService studentService;

    @InjectMocks
    StudentController studentController;

    @Test
    void listStudents_defaults() {
        UUID id = UUID.randomUUID();
        StudentResponse respDto = new StudentResponse(id, UUID.randomUUID(), "u1", "John", "Doe", LocalDate.of(2010,1,1), OffsetDateTime.now());
        when(studentService.list(null, null, null, null)).thenReturn(List.of(respDto));

        ResponseEntity<List<StudentResponse>> resp = studentController.listStudents(null, null, null, null);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        List<StudentResponse> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).id()).isEqualTo(id);
    }

    @Test
    void createStudent_returnsCreated() {
        StudentCreateRequest req = new StudentCreateRequest("u2", "pass", "Jane", "Doe", LocalDate.of(2011,2,2));
        StudentResponse respDto = new StudentResponse(UUID.randomUUID(), UUID.randomUUID(), "u2", "Jane", "Doe", LocalDate.of(2011,2,2), OffsetDateTime.now());
        when(studentService.create(req)).thenReturn(respDto);

        ResponseEntity<StudentResponse> resp = studentController.create(req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(201);
        assertThat(resp.getBody()).isEqualTo(respDto);
    }
}
