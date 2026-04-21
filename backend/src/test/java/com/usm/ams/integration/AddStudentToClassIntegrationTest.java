package com.usm.ams.integration;

import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.repository.EnrollmentRepository;
import com.usm.ams.repository.OutboxRepository;
import com.usm.ams.repository.OrganizationUnitRepository;
import com.usm.ams.repository.StudentProfileRepository;
import com.usm.ams.repository.UserAccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-tests")
@Testcontainers
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")
public class AddStudentToClassIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrganizationUnitRepository orgRepo;

    @Autowired
    private UserAccountRepository userRepo;

    @Autowired
    private StudentProfileRepository profileRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private OutboxRepository outboxRepo;

    @BeforeEach
    public void cleanup() {
        enrollmentRepo.deleteAll();
        profileRepo.deleteAll();
        userRepo.deleteAll();
        outboxRepo.deleteAll();
        orgRepo.deleteAll();
    }

    @Test
    public void TC_ADD_001_happyPath_createsAccountProfileEnrollmentAndOutbox() {
        OrganizationUnit clazz = new OrganizationUnit("Lớp", "CLS1", "Class 1");
        clazz = orgRepo.save(clazz);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "student1");
        payload.put("initialPassword", "Password123");
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "John");
        profile.put("lastName", "Doe");
        payload.put("profile", profile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/classes/" + clazz.getId() + "/students", req, Map.class);
        Assertions.assertEquals(201, resp.getStatusCodeValue());

        // verify persisted
        Assertions.assertTrue(userRepo.findByUsername("student1").isPresent());
        Assertions.assertEquals(1, profileRepo.count());
        Assertions.assertEquals(1, enrollmentRepo.count());
        Assertions.assertEquals(1, outboxRepo.count());
    }

    @Test
    public void TC_ADD_002_rollbackOnFailure_noPartialPersisted() {
        OrganizationUnit clazz = new OrganizationUnit("Lớp", "CLS2", "Class 2");
        clazz = orgRepo.save(clazz);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "student2");
        payload.put("initialPassword", "Password123");
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "FAIL_RB");
        profile.put("lastName", "Trigger");
        payload.put("profile", profile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/classes/" + clazz.getId() + "/students", req, Map.class);
        Assertions.assertEquals(500, resp.getStatusCodeValue());

        // ensure nothing persisted
        Assertions.assertTrue(userRepo.findByUsername("student2").isEmpty());
        Assertions.assertEquals(0, profileRepo.count());
        Assertions.assertEquals(0, enrollmentRepo.count());
        Assertions.assertEquals(0, outboxRepo.count());
    }

    @Test
    public void TC_ADD_003_duplicateUsername_returns409_and_noSideEffects() {
        // pre-create user
        com.usm.ams.entity.UserAccount existing = new com.usm.ams.entity.UserAccount("dupuser", "x", "STUDENT");
        userRepo.save(existing);

        OrganizationUnit clazz = new OrganizationUnit("Lớp", "CLS3", "Class 3");
        clazz = orgRepo.save(clazz);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "dupuser");
        payload.put("initialPassword", "Password123");
        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "Alice");
        profile.put("lastName", "Dup");
        payload.put("profile", profile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/v1/classes/" + clazz.getId() + "/students", req, Map.class);
        Assertions.assertEquals(409, resp.getStatusCodeValue());

        // ensure no new profile or enrollment or outbox created
        Assertions.assertEquals(0, profileRepo.count());
        Assertions.assertEquals(0, enrollmentRepo.count());
        Assertions.assertEquals(0, outboxRepo.count());
    }
}
