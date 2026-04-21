package com.usm.ams.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AclService {

    private static final Logger logger = LoggerFactory.getLogger(AclService.class);

    private final JdbcTemplate jdbcTemplate;

    public AclService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isClassAdmin(Authentication authentication, UUID classId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            String role = ga.getAuthority();
            if (role != null && role.startsWith("ROLE_")) role = role.substring(5);
            if ("ADMIN".equals(role)) return true;
        }
        String username = authentication.getName();
        if (username == null) return false;

        String sql = "SELECT 1 FROM user_role_scopes urs JOIN user_accounts ua ON urs.user_account_id = ua.id JOIN roles r ON urs.role_id = r.id WHERE ua.username = ? AND r.name = ? AND urs.scope_type = 'CLASS' AND urs.scope_id = ? LIMIT 1";
        try {
            Integer found = jdbcTemplate.queryForObject(sql, Integer.class, username, "CLASS_ADMIN", classId);
            return found != null && found == 1;
        } catch (DataAccessException dae) {
            logger.error("Database error while checking class admin for user='{}' class='{}'", username, classId, dae);
            return false;
        }
    }
}
