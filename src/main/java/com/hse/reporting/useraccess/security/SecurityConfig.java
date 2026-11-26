package com.hse.reporting.useraccess.security;

import com.hse.reporting.useraccess.domain.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security wiring (ADR-08, NFR-01, ADR-03).
 *
 * <p>Two cooperating server-side layers:
 * <ol>
 *   <li>URL-level role rules below — coarse protection of whole areas.</li>
 *   <li>{@code AuthorizationService} calls inside use cases — the authoritative
 *       decision for each operation and data scope (ASR-01: hiding UI controls
 *       is never the only enforcement).</li>
 * </ol>
 *
 * <p>Anything not explicitly permitted is denied (deny-by-default).
 * CSRF protection stays on for all forms (ADR-08); Thymeleaf renders the token
 * automatically for {@code th:action} forms.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // public: sign-in, session-expired and error pages
                        .requestMatchers(
                                "/login",
                                "/session-expired",
                                "/403",
                                "/css/**",
                                "/favicon.ico",
                                "/error",
                                "/error/**")
                        .permitAll()
                        // role-protected areas (FR-41, permission matrix PRD section 4.1)
                        .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/manager/**").hasRole(Role.MANAGER.name())
                        .requestMatchers("/inspector/**").hasRole(Role.INSPECTOR.name())
                        // deny-by-default: everything else needs a signed-in user
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionFixation(f -> f.migrateSession())
                        // FR-08: an expired session is reported, never silently reused
                        .invalidSessionUrl("/session-expired"))
                .exceptionHandling(ex -> ex
                        // signed-in user without the required role
                        .accessDeniedPage("/403"));
        return http.build();
    }

    /** Passwords are stored only as BCrypt hashes (NFR-01). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
