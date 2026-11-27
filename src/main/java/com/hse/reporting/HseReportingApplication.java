package com.hse.reporting;

import hse.reporting.config.ReportOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Single deployable application (ADR-01) built with the approved stack (ADR-08):
 * Java, Spring Boot, Thymeleaf, Spring Security and MySQL.
 *
 * <p>Business capabilities are organized in cohesive modules (ADR-02):
 * <ul>
 *   <li>{@link com.hse.reporting.useraccess} — User and Access Management</li>
 *   <li>{@link com.hse.reporting.projectmanagement} — Project Management</li>
 *   <li>{@link com.hse.reporting.application} — application layer (use cases)</li>
 *   <li>{@code hse.reporting.report} / {@code hse.reporting.photo} — report creation
 *       and photograph evidence (merged from Eman's branch)</li>
 * </ul>
 * Both package trees share this scan root, one schema baseline and one security stack.
 */
@SpringBootApplication(scanBasePackages = {"com.hse.reporting", "hse.reporting"})
@EntityScan(basePackages = {"com.hse.reporting", "hse.reporting"})
@EnableJpaRepositories(basePackages = {"com.hse.reporting", "hse.reporting"})
@EnableConfigurationProperties(ReportOptions.class)
public class HseReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HseReportingApplication.class, args);
    }
}
