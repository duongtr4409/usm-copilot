package com.usm.ams.controller;

import com.usm.ams.dto.ClassDto;
import com.usm.ams.entity.OrganizationUnit;
import com.usm.ams.repository.OrganizationUnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassControllerTest {

    @Mock
    OrganizationUnitRepository organizationUnitRepository;

    @InjectMocks
    ClassController classController;

    @Test
    void listClasses_defaultsToLop() {
        UUID id = UUID.randomUUID();
        OrganizationUnit unit = new OrganizationUnit("Lớp", "CLS001", "Class 1");
        unit.setId(id);
        when(organizationUnitRepository.findByType("Lớp")).thenReturn(List.of(unit));

        ResponseEntity<List<ClassDto>> resp = classController.listClasses(null);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        List<ClassDto> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        ClassDto dto = body.get(0);
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.code()).isEqualTo("CLS001");
        assertThat(dto.title()).isEqualTo("Class 1");
    }
}
