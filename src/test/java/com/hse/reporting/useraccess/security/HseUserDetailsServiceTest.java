package com.hse.reporting.useraccess.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hse.reporting.useraccess.data.UserRepository;
import com.hse.reporting.useraccess.domain.Role;
import com.hse.reporting.useraccess.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class HseUserDetailsServiceTest {

    @Mock
    private UserRepository users;

    @Test
    void loadsUserByCompanyEmailWithItsRoleAuthority() {
        when(users.findByEmailIgnoreCase("manal12cf@gmail.com"))
                .thenReturn(Optional.of(new User("manal12cf@gmail.com", "hash", Role.ADMIN)));

        UserDetails details = new HseUserDetailsService(users).loadUserByUsername("manal12cf@gmail.com");

        assertThat(details.getUsername()).isEqualTo("manal12cf@gmail.com");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void emailMatchingIsCaseInsensitive() {
        // The service passes the submitted email through; Spring Data matches case-insensitively.
        when(users.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(new User("i253172@nu.edu.pk", "hash", Role.INSPECTOR)));

        UserDetails details = new HseUserDetailsService(users).loadUserByUsername("I253172@NU.EDU.PK");

        assertThat(details.getUsername()).isEqualTo("i253172@nu.edu.pk"); // canonical email
        verify(users).findByEmailIgnoreCase("I253172@NU.EDU.PK");
    }

    @Test
    void unknownAccountIsRejected() {
        when(users.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new HseUserDetailsService(users).loadUserByUsername("nobody@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
