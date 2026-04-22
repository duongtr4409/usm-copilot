package com.usm.ams.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationUnitRepositoryTest {

    @Test
    void hasFindByTypeMethod() {
        boolean has = java.util.Arrays.stream(OrganizationUnitRepository.class.getMethods())
                .anyMatch(m -> m.getName().equals("findByType") && m.getParameterCount() == 1);
        assertThat(has).isTrue();
    }
}
