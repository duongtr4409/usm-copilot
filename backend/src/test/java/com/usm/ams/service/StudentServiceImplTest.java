package com.usm.ams.service;

import com.usm.ams.dto.AddStudentToClassRequest;
import com.usm.ams.entity.Enrollment;
import com.usm.ams.entity.Outbox;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.entity.StudentProfile;
import com.usm.ams.entity.UserAccount;
import com.usm.ams.repository.EnrollmentRepository;
import com.usm.ams.repository.OutboxRepository;
import com.usm.ams.repository.OrganizationUnitRepository;
import com.usm.ams.repository.StudentProfileRepository;
import com.usm.ams.repository.UserAccountRepository;
import com.usm.ams.security.AclService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

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

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void encodesPasswordUsingInjectedEncoder() {
        AddStudentToClassRequest.Profile profile = new AddStudentToClassRequest.Profile("John", "Doe", LocalDate.of(2010,1,1), java.util.Map.of());
        AddStudentToClassRequest req = new AddStudentToClassRequest("u1", "pass", profile);

        UUID classId = UUID.randomUUID();
        OrganizationUnit clazz = new OrganizationUnit("Lớp", "CLS", "Test");
        when(organizationUnitRepository.findById(classId)).thenReturn(Optional.of(clazz));
        when(userAccountRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("HASHED");
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
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(outboxRepository.save(any(Outbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // set an authenticated admin so the method passes authorization
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(aclService.isClassAdmin(auth, classId)).thenReturn(true);

        var resp = studentService.addStudentToClass(classId, req);
        assertThat(resp).isNotNull();

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("HASHED");
    }

    @Test
    void deniesWhenNotAuthorized() {
        AddStudentToClassRequest.Profile profile = new AddStudentToClassRequest.Profile("John", "Doe", LocalDate.of(2010,1,1), java.util.Map.of());
        AddStudentToClassRequest req = new AddStudentToClassRequest("u2", "pass", profile);
        UUID classId = UUID.randomUUID();
        // no authentication
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> studentService.addStudentToClass(classId, req)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void returnsStudentsForClass() {
        UUID classId = UUID.randomUUID();
        UserAccount account = new UserAccount("suser", "h", "STUDENT");
        account.setId(UUID.randomUUID());
        StudentProfile p = new StudentProfile(account, "Anna", "Nguyen", LocalDate.of(2012,2,3));
        p.setId(UUID.randomUUID());
        Enrollment e = new Enrollment(classId, p);
        e.setId(UUID.randomUUID());

        when(enrollmentRepository.findByClassUnitId(classId)).thenReturn(List.of(e));

        var list = studentService.getStudentsInClass(classId);
        assertThat(list).isNotNull();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).firstName()).isEqualTo("Anna");
    }
}
