package com.usm.ams.controller;

import com.usm.ams.dto.StaffCreateRequest;
import com.usm.ams.dto.StaffResponse;
import com.usm.ams.dto.StaffUpdateRequest;
import com.usm.ams.service.StaffService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Mock
    StaffService service;

    @InjectMocks
    StaffController controller;

    @Test
    void listDelegatesToService() {
        UUID id = UUID.randomUUID();
        StaffResponse r = new StaffResponse(id, null, "SN1", "Full Name", "Teacher", null, "{}", "ACTIVE", OffsetDateTime.now(), OffsetDateTime.now(), null, null);
        when(service.list()).thenReturn(List.of(r));

        ResponseEntity<List<StaffResponse>> resp = controller.list();

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        List<StaffResponse> body = resp.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).id()).isEqualTo(id);
    }

    @Test
    void createReturnsCreatedWithLocation() {
        StaffCreateRequest req = new StaffCreateRequest(null, "SN2", "Full Name", "Teacher", null, "{}", null);
        UUID id = UUID.randomUUID();
        StaffResponse created = new StaffResponse(id, null, "SN2", "Full Name", "Teacher", null, "{}", "ACTIVE", OffsetDateTime.now(), OffsetDateTime.now(), null, null);
        when(service.create(req)).thenReturn(created);

        ResponseEntity<StaffResponse> resp = controller.create(req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(201);
        assertThat(resp.getHeaders().getLocation()).isEqualTo(URI.create("/api/v1/staff/" + id));
        assertThat(resp.getBody().id()).isEqualTo(id);
    }

    @Test
    void updateReturnsOk() {
        UUID id = UUID.randomUUID();
        StaffUpdateRequest req = new StaffUpdateRequest(null, "SN3", "Full Name", "Teacher", null, "{}", null);
        StaffResponse updated = new StaffResponse(id, null, "SN3", "Full Name", "Teacher", null, "{}", "ACTIVE", OffsetDateTime.now(), OffsetDateTime.now(), null, null);
        when(service.update(id, req)).thenReturn(updated);

        ResponseEntity<StaffResponse> resp = controller.update(id, req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody().id()).isEqualTo(id);
    }
}
