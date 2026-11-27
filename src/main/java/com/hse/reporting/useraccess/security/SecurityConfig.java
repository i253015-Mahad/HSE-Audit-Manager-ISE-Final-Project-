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
                        // public: sign-in and error pages
                        .requestMatchers(
                                "/login",
                                "/403",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico",
                                "/error",
                                "/error/**")
                        .permitAll()
                        // role-protected areas (FR-41, permission matrix PRD section 4.1)
                        .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/manager/**").hasRole(Role.MANAGER.name())
                        .requestMatchers("/inspector/**").hasRole(Role.INSPECTOR.name())
                        // Project Management: Admin only (PRD section 4 — Inspector No,
                        // Manager No, Admin Yes for create/assign/close; AC-33).
                        // The handlers re-check the actor as well (ASR-01).
                        .requestMatchers("/projects/**").hasRole(Role.ADMIN.name())
                        // Report creation is Inspector-only; the use case repeats the check (ASR-01).
                        .requestMatchers("/reports/**").hasRole(Role.INSPECTOR.name())
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
                        // Logout invalidates the session server-side but leaves the
                        // JSESSIONID cookie behind; without clearing it the redirect
                        // to /login?logout arrives with a dead session id and
                        // invalidSessionUrl would hijack it to /login?expired.
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionFixation(f -> f.migrateSession())
                        // FR-08: an unknown session id lands on the login page with an
                        // ?expired flag; CurrentUserModelAdvice shows the notice only
                        // when the session really timed out — a stale cookie right
                        // after a server restart gets the plain login page.
                        .invalidSessionUrl("/login?expired"))
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
