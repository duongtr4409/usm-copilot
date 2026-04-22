package com.usm.ams.service;

import com.usm.ams.dto.StudentCreateRequest;
import com.usm.ams.entity.StudentProfile;
import com.usm.ams.entity.UserAccount;
import com.usm.ams.exception.ResourceNotFoundException;
import com.usm.ams.repository.EnrollmentRepository;
import com.usm.ams.repository.OutboxRepository;
import com.usm.ams.repository.OrganizationUnitRepository;
import com.usm.ams.repository.StudentProfileRepository;
import com.usm.ams.repository.UserAccountRepository;
import com.usm.ams.security.AclService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    UserAccountRepository userAccountRepository;
    @Mock
    StudentProfileRepository studentProfileRepository;
    @Mock
    EnrollmentRepository enrollmentRepository;
    @Mock
    OrganizationUnitRepository organizationUnitRepository;
    @Mock
    OutboxRepository outboxRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AclService aclService;

    @InjectMocks
    StudentServiceImpl studentService;

    @Test
    void createStudent_success() {
        when(userAccountRepository.findByUsername("u1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("HASH");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount ua = invocation.getArgument(0);
            ua.setId(UUID.randomUUID());
            return ua;
        });
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> {
            StudentProfile p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        StudentCreateRequest req = new StudentCreateRequest("u1", "pass", "John", "Doe", LocalDate.of(2010,1,1));
        var resp = studentService.create(req);
        assertThat(resp).isNotNull();
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void findById_notFound() {
        UUID id = UUID.randomUUID();
        when(studentProfileRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> studentService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
