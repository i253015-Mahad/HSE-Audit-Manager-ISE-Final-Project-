package com.hse.reporting.web.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hse.reporting.report.SafetyReport;
import hse.reporting.report.SafetyReportRepository;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end checks for the SOR form rendered and submitted by an Inspector. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportFormIntegrationTest {

    private static final String INSPECTOR_EMAIL = "i253172@nu.edu.pk";
    private static final String MANAGER_EMAIL = "mahadwaqas28@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SafetyReportRepository reports;

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void formRendersTheSorLayoutWithObserverDetails() throws Exception {
        mockMvc.perform(get("/reports/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add Report")))
                .andExpect(content().string(containsString("SOR Details")))
                .andExpect(content().string(containsString("Observer Details")))
                .andExpect(content().string(containsString("Observation Details")))
                .andExpect(content().string(containsString("Inspector I253172")))
                .andExpect(content().string(containsString("I-253172")))
                .andExpect(content().string(containsString("Head Office Fit-Out")))
                .andExpect(content().string(containsString("Observation Hazard")))
                .andExpect(content().string(containsString("Risk Rating")))
                .andExpect(content().string(containsString("Violator Details")))
                .andExpect(content().string(containsString("Task Incharge")))
                .andExpect(content().string(containsString("Observation Date")))
                // The old SOR fields are replaced, not hidden on this page.
                .andExpect(content().string(not(containsString("Hazard Category"))))
                .andExpect(content().string(not(containsString("Severity Level"))));
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void managerIsResolvedFromTheSelectedProject() throws Exception {
        mockMvc.perform(post("/reports")
                        .with(csrf())
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(MANAGER_EMAIL)))
                .andExpect(content().string(containsString("Enter the observation date.")));
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void submittedSorSnapshotsObserverAndManager() throws Exception {
        String form = mockMvc.perform(get("/reports/new"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = submissionToken(form);
        String location = "Integration SOR " + UUID.randomUUID().toString().substring(0, 8);
        String description = "Unprotected edge " + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/reports")
                        .with(csrf())
                        .param("submissionToken", token)
                        .param("projectId", "1")
                        .param("observationDate", "2026-11-27")
                        .param("observationTime", "09:30")
                        .param("location", location)
                        .param("observationType", "INSPECTION_FINDING")
                        .param("violations[0].briefDescription", description)
                        .param("violations[0].observationHazard", "Unsafe condition")
                        .param("violations[0].riskRating", "High")
                        .param("violations[0].actionRecommended", "Install guardrails")
                        .param("violations[0].violatorName", "Integration Violator")
                        .param("violations[0].violatorBadgeNo", "B-999")
                        .param("violations[0].violatorTitle", "Labourer")
                        .param("violations[0].taskIncharge", "Integration Foreman"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports/submitted"));

        SafetyReport saved = reports.findAll().stream()
                .filter(report -> location.equals(report.getLocation()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Submitted SOR was not persisted"));
        assertThat(saved.getInspectorEmail()).isEqualTo(INSPECTOR_EMAIL);
        assertThat(saved.getObserverName()).isEqualTo("Inspector I253172");
        assertThat(saved.getObserverBadgeNo()).isEqualTo("I-253172");
        assertThat(saved.getObserverTitle()).isEqualTo("Safety Inspector");
        assertThat(saved.getObserverCompany()).isEqualTo("HSE Department");
        assertThat(saved.getManagerEmail()).isEqualTo(MANAGER_EMAIL);
        assertThat(saved.getLocation()).isEqualTo(location);
    }

    private String submissionToken(String html) {
        Matcher matcher = Pattern.compile("name=\"submissionToken\"[^>]*value=\"([^\"]+)\"")
                .matcher(html);
        assertThat(matcher.find()).as("submission token in rendered SOR form").isTrue();
        return matcher.group(1);
    }
}
