package com.hse.reporting.useraccess.security;

import com.hse.reporting.useraccess.data.UserRepository;
import com.hse.reporting.useraccess.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads the user identified by the company email for Spring Security (NFR-01:
 * login with company-issued email and password).
 *
 * <p>Account creation, password reset and lockout are handled outside the
 * software (PRD section 2.2); this service only reads existing records.
 */
@Service
public class HseUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public HseUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) // canonical email as stored
                .password(user.getPasswordHash())
                .authorities(user.getRole().authority())
                .build();
    }
}
