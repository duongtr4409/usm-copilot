package com.usm.ams.service;

import com.usm.ams.dto.StaffCreateRequest;
import com.usm.ams.dto.StaffResponse;
import com.usm.ams.dto.StaffUpdateRequest;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.entity.Staff;
import com.usm.ams.entity.UserAccount;
import com.usm.ams.exception.ResourceNotFoundException;
import com.usm.ams.repository.OrganizationUnitRepository;
import com.usm.ams.repository.StaffRepository;
import com.usm.ams.repository.UserAccountRepository;
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
class StaffServiceTest {

    @Mock
    StaffRepository repository;

    @Mock
    UserAccountRepository userAccountRepository;

    @Mock
    OrganizationUnitRepository organizationUnitRepository;

    @InjectMocks
    StaffServiceImpl service;

    @Test
    void shouldCreateAndReturn() {
        UUID accountId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        StaffCreateRequest req = new StaffCreateRequest(accountId, "SN001", "Full Name", "Teacher", unitId, "{}", null);

        UserAccount acc = new UserAccount("u", "p", "ROLE");
        acc.setId(accountId);
        OrganizationUnit unit = new OrganizationUnit("Dept", "D1", "Dept 1");
        unit.setId(unitId);

        when(userAccountRepository.findById(accountId)).thenReturn(Optional.of(acc));
        when(organizationUnitRepository.findById(unitId)).thenReturn(Optional.of(unit));

        Staff saved = new Staff();
        UUID id = UUID.randomUUID();
        saved.setId(id);
        saved.setAccount(acc);
        saved.setStaffNumber("SN001");
        saved.setFullName("Full Name");
        saved.setPosition("Teacher");
        saved.setUnit(unit);
        saved.setContact("{}");
        saved.setStatus("ACTIVE");
        when(repository.save(any(Staff.class))).thenReturn(saved);

        StaffResponse r = service.create(req);

        assertThat(r.id()).isEqualTo(id);
        assertThat(r.fullName()).isEqualTo("Full Name");
    }

    @Test
    void shouldFindById_whenExists() {
        UUID id = UUID.randomUUID();
        Staff s = new Staff();
        s.setId(id);
        s.setFullName("Full Name");
        when(repository.findById(id)).thenReturn(Optional.of(s));

        StaffResponse r = service.findById(id);

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
