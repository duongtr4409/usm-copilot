package com.usm.ams.controller;

import com.usm.ams.dto.OrganizationUnitCreateRequest;
import com.usm.ams.dto.OrganizationUnitResponse;
import com.usm.ams.dto.OrganizationUnitUpdateRequest;
import com.usm.ams.service.OrganizationUnitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationUnitControllerTest {

    @Mock
    OrganizationUnitService service;

    @InjectMocks
    OrganizationUnitController controller;

    @Test
    void listDelegatesToService() {
        UUID id = UUID.randomUUID();
        OrganizationUnitResponse r = new OrganizationUnitResponse(id, "Dept", "D1", "Department 1");
        when(service.list(null)).thenReturn(List.of(r));

        ResponseEntity<List<OrganizationUnitResponse>> resp = controller.list(null);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        List<OrganizationUnitResponse> body = resp.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).id()).isEqualTo(id);
    }

    @Test
    void createReturnsCreatedWithLocation() {
        OrganizationUnitCreateRequest req = new OrganizationUnitCreateRequest("Dept", "D2", "Department 2");
        UUID id = UUID.randomUUID();
        OrganizationUnitResponse created = new OrganizationUnitResponse(id, "Dept", "D2", "Department 2");
        when(service.create(req)).thenReturn(created);

        ResponseEntity<OrganizationUnitResponse> resp = controller.create(req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(201);
        assertThat(resp.getHeaders().getLocation()).isEqualTo(URI.create("/api/v1/org-units/" + id));
        assertThat(resp.getBody().id()).isEqualTo(id);
    }

    @Test
    void updateReturnsOk() {
        UUID id = UUID.randomUUID();
        OrganizationUnitUpdateRequest req = new OrganizationUnitUpdateRequest("Dept", "D3", "Department 3");
        OrganizationUnitResponse updated = new OrganizationUnitResponse(id, "Dept", "D3", "Department 3");
        when(service.update(id, req)).thenReturn(updated);

        ResponseEntity<OrganizationUnitResponse> resp = controller.update(id, req);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody().id()).isEqualTo(id);
    }

}
