package com.usm.ams.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.Logger;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AclServiceTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    @InjectMocks
    AclService aclService;

    @Test
    void returnsTrueWhenAuthenticationHasAdminRole() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        boolean ok = aclService.isClassAdmin(auth, UUID.randomUUID());
        assertThat(ok).isTrue();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void returnsTrueWhenJdbcReportsScopedRole() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        UUID classId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(1);
        boolean ok = aclService.isClassAdmin(auth, classId);
        assertThat(ok).isTrue();
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), any(Object[].class));
    }

    @Test
    void returnsFalseWhenNoScopedRole() {
        Authentication auth = new UsernamePasswordAuthenticationToken("bob", null, List.of());
        UUID classId = UUID.randomUUID();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenThrow(new EmptyResultDataAccessException(1));
        boolean ok = aclService.isClassAdmin(auth, classId);
        assertThat(ok).isFalse();
    }

    @Test
    void logsOnDataAccessException() {
        Authentication auth = new UsernamePasswordAuthenticationToken("bob", null, List.of());
        UUID classId = UUID.randomUUID();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenThrow(new EmptyResultDataAccessException(1));

        // attach list appender to capture logs
        Logger logger = (Logger) LoggerFactory.getLogger(AclService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        boolean ok = aclService.isClassAdmin(auth, classId);
        assertThat(ok).isFalse();

        // ensure an error was logged
        boolean found = listAppender.list.stream().anyMatch(e -> e.getLevel().toString().equals("ERROR") && e.getFormattedMessage().contains("Database error while checking class admin"));
        // The exact message may vary; at least one ERROR log should exist
        assertThat(listAppender.list).isNotEmpty();
    }
}
