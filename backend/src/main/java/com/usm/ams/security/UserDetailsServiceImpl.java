package com.usm.ams.security;

import com.usm.ams.entity.UserAccount;
import com.usm.ams.repository.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public UserDetailsServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount ua = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        List<SimpleGrantedAuthority> authorities = List.of();
        if (ua.getRole() != null) {
            String role = ua.getRole();
            if (!role.startsWith("ROLE_")) role = "ROLE_" + role;
            authorities = List.of(new SimpleGrantedAuthority(role));
        }
        return new org.springframework.security.core.userdetails.User(ua.getUsername(), ua.getPasswordHash(), authorities);
    }
}
