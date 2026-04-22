package com.usm.ams.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usm.ams.dto.AddStudentToClassRequest;
import com.usm.ams.dto.AddStudentToClassResponse;
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
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.usm.ams.security.AclService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import com.usm.ams.dto.StudentProfileResponse;

@Service
public class StudentServiceImpl implements StudentService {

    private final UserAccountRepository userAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final AclService aclService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudentServiceImpl(UserAccountRepository userAccountRepository,
                              StudentProfileRepository studentProfileRepository,
                              EnrollmentRepository enrollmentRepository,
                              OrganizationUnitRepository organizationUnitRepository,
                              OutboxRepository outboxRepository,
                              PasswordEncoder passwordEncoder,
                              AclService aclService) {
        this.userAccountRepository = userAccountRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.organizationUnitRepository = organizationUnitRepository;
        this.outboxRepository = outboxRepository;
        this.passwordEncoder = passwordEncoder;
        this.aclService = aclService;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @aclService.isClassAdmin(authentication, #classId)")
    public AddStudentToClassResponse addStudentToClass(UUID classId, AddStudentToClassRequest request) {
        // runtime defense-in-depth: double-check caller has permission
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean allowed = false;
        if (authentication != null && authentication.isAuthenticated()) {
            allowed = authentication.getAuthorities().stream().anyMatch(ga -> {
                String g = ga.getAuthority();
                if (g != null && g.startsWith("ROLE_")) g = g.substring(5);
                return "ADMIN".equals(g);
            });
            if (!allowed) {
                allowed = aclService.isClassAdmin(authentication, classId);
            }
        }
        if (!allowed) {
            throw new AccessDeniedException("Not authorized to add student to class");
        }
        // 1. Validate class exists and is type 'Lớp' (or accept any for test)
        OrganizationUnit unit = organizationUnitRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        // optional type check
        if (unit.getType() == null || !unit.getType().equalsIgnoreCase("Lớp")) {
            throw new IllegalArgumentException("Target unit is not a Class (Lớp)");
        }

        // 2. Username uniqueness
        Optional<UserAccount> existing = userAccountRepository.findByUsername(request.username());
        if (existing.isPresent()) {
            throw new UsernameConflictException("username already exists");
        }

        // 3. Create account
        String hashed = passwordEncoder.encode(request.initialPassword());
        UserAccount account = new UserAccount(request.username(), hashed, "STUDENT");
        account = userAccountRepository.save(account);

        // 4. Create profile
        AddStudentToClassRequest.Profile p = request.profile();
        StudentProfile profile = new StudentProfile(account, p.firstName(), p.lastName(), p.dob());
        profile = studentProfileRepository.save(profile);

        // Test-only trigger: if firstName == "FAIL_RB" simulate failure to verify rollback
        if ("FAIL_RB".equals(p.firstName())) {
            throw new RuntimeException("simulated failure for rollback test");
        }

        // 5. Enrollment
        Enrollment enrollment = new Enrollment(classId, profile);
        enrollment = enrollmentRepository.save(enrollment);

        // 6. Outbox write
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "EnrollmentCreated");
        payload.put("enrollmentId", enrollment.getId().toString());
        payload.put("studentProfileId", profile.getId().toString());
        payload.put("classId", classId.toString());
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Outbox outbox = new Outbox("enrollment", enrollment.getId(), "EnrollmentCreated", payloadJson);
        outboxRepository.save(outbox);

        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/students/" + profile.getId());
        links.put("class", "/api/v1/classes/" + classId + "");

        return new AddStudentToClassResponse(profile.getId(), account.getId(), links);
    }

    @Override
    public List<StudentProfileResponse> getStudentsInClass(UUID classId) {
        List<Enrollment> enrollments = enrollmentRepository.findByClassUnitId(classId);
        return enrollments.stream().map(e -> {
            StudentProfile p = e.getStudentProfile();
            UUID accId = (p.getAccount() != null) ? p.getAccount().getId() : null;
            return new StudentProfileResponse(p.getId(), accId, p.getFirstName(), p.getLastName(), p.getDob());
        }).collect(Collectors.toList());
    }

    public static class UsernameConflictException extends RuntimeException {
        public UsernameConflictException(String msg) { super(msg); }
    }
}
