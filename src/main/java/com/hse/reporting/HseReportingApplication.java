package com.hse.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the HSE Safety Reporting System.
 *
 * <p>The application is a single deployable unit (ADR-01) built with the approved
 * stack (ADR-08): Java, Spring Boot, Thymeleaf, Spring Security and MySQL.
 * Business capabilities are organized in cohesive modules (ADR-02):
 * <ul>
 *   <li>{@link com.hse.reporting.useraccess} — User and Access Management</li>
 *   <li>{@link com.hse.reporting.projectmanagement} — Project Management</li>
 *   <li>{@link com.hse.reporting.application} — application layer (use cases)</li>
 * </ul>
 */
@SpringBootApplication
public class HseReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HseReportingApplication.class, args);
    }
}
