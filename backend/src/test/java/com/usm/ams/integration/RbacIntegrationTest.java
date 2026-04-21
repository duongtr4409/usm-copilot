package com.usm.ams.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Integration tests require Testcontainers / Docker and are intentionally disabled
@SpringBootTest
@Disabled("Integration tests require Testcontainers and Docker; enable with 'integration-tests' profile")
public class RbacIntegrationTest {

    @Test
    void template_addStudent_as_classAdmin_shouldReturnCreated() {
        // TODO: implement integration scenario using TestRestTemplate and Testcontainers DB
    }

    @Test
    void template_addStudent_as_regularUser_shouldReturnForbidden() {
        // TODO: implement integration scenario using TestRestTemplate and Testcontainers DB
    }
}
