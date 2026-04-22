package com.usm.ams.service;

import com.usm.ams.dto.OrganizationUnitCreateRequest;
import com.usm.ams.dto.OrganizationUnitResponse;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.exception.ResourceNotFoundException;
import com.usm.ams.repository.OrganizationUnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationUnitServiceTest {

    @Mock
    OrganizationUnitRepository repository;

    @InjectMocks
    OrganizationUnitServiceImpl service;

    @Test
    void shouldListAllWhenTypeNull() {
        UUID id = UUID.randomUUID();
        OrganizationUnit u = new OrganizationUnit("Dept", "D001", "Dept 1");
        u.setId(id);
        when(repository.findAll()).thenReturn(List.of(u));

        List<OrganizationUnitResponse> res = service.list(null);

        assertThat(res).hasSize(1);
        OrganizationUnitResponse r = res.get(0);
        assertThat(r.id()).isEqualTo(id);
        assertThat(r.type()).isEqualTo("Dept");
    }

    @Test
    void shouldFindById_whenExists() {
        UUID id = UUID.randomUUID();
        OrganizationUnit u = new OrganizationUnit("Dept", "D002", "Dept 2");
        u.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(u));

        OrganizationUnitResponse r = service.findById(id);

        assertThat(r).isNotNull();
        assertThat(r.id()).isEqualTo(id);
    }

    @Test
    void shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void shouldCreateAndReturn() {
        OrganizationUnitCreateRequest req = new OrganizationUnitCreateRequest("Dept", "D003", "Dept 3");
        OrganizationUnit saved = new OrganizationUnit("Dept", "D003", "Dept 3");
        UUID id = UUID.randomUUID();
        saved.setId(id);
        when(repository.save(any(OrganizationUnit.class))).thenReturn(saved);

        OrganizationUnitResponse r = service.create(req);

        assertThat(r.id()).isEqualTo(id);
        assertThat(r.title()).isEqualTo("Dept 3");
    }

    @Test
    void shouldDelete_whenExists() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteShouldThrowNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
    }
}
