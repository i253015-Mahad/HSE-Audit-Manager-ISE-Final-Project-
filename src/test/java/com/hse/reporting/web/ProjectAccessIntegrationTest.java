package com.hse.reporting.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end access control of the integrated slices — Project Management
 * behind the Slice 1 authentication, run against the real schema and seed data:
 *
 * <ul>
 *   <li>FR-30…FR-36 — project list, create, assign/reassign Manager, close
 *       are Admin-only (PRD section 4: Inspector No, Manager No, Admin Yes),</li>
 *   <li>AC-33 — direct URL access outside the permission matrix is denied
 *       server-side; hiding UI controls is never the only enforcement (ASR-01),</li>
 *   <li>CSRF — a state-changing POST without the token is refused (ADR-08),</li>
 *   <li>FR-15 / FR-31 — a Manager sees exactly the projects assigned to their
 *       email, including one assigned through the web application,</li>
 *   <li>FR-14 / FR-15 / FR-16 / FR-42 — the merged dashboard cards render
 *       their empty state until Safety Report Management supplies data
 *       (plan-v02-Mahad.md section 7).</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectAccessIntegrationTest {

    private static final String ADMIN_EMAIL = "manal12cf@gmail.com";
    private static final String MANAGER_EMAIL = "mahadwaqas28@gmail.com";
    private static final String INSPECTOR_EMAIL = "i253172@nu.edu.pk";

    @Autowired
    private MockMvc mockMvc;

    // ---- unauthenticated behaviour ---------------------------------------

    @Test
    void anonymousUsersAreSentToTheLoginPageForTheProjectArea() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/projects/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ---- AC-33: direct access outside the permission matrix ---------------

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void inspectorCannotReachTheProjectArea() throws Exception {
        mockMvc.perform(get("/projects")).andExpect(status().isForbidden());
        mockMvc.perform(get("/projects/new")).andExpect(status().isForbidden());
        mockMvc.perform(post("/projects/new").param("name", "Any name").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, authorities = "ROLE_MANAGER")
    void managerCannotReachTheProjectArea() throws Exception {
        mockMvc.perform(get("/projects")).andExpect(status().isForbidden());
        mockMvc.perform(post("/projects/new").param("name", "Any name").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---- Admin: the project list (FR-30) ----------------------------------

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void adminSeesEveryProjectWithItsManager() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Head Office Fit-Out")))
                .andExpect(content().string(containsString("Warehouse Extension")))
                .andExpect(content().string(containsString("Site A Laboratory")))
                // the Manager column reads projects.manager_email (integration D1)
                .andExpect(content().string(containsString(MANAGER_EMAIL)))
                // Site A Laboratory is deliberately unassigned in the seed data
                .andExpect(content().string(containsString("Not assigned yet")))
                .andExpect(content().string(containsString("Assign / reassign Manager")));
    }

    // ---- list filters (GET /projects?q=&state= — display refinement) ------

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void filterFormIsRenderedOnTheProjectList() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("name=\"state\"")))
                // no filter set → the "nothing exists" empty state is NOT shown
                .andExpect(content().string(not(containsString("No projects match"))));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void textFilterNarrowsToNameAndManagerMatches() throws Exception {
        // matches the seeded project's NAME
        mockMvc.perform(get("/projects").param("q", "warehouse"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Warehouse Extension")))
                .andExpect(content().string(not(containsString("Head Office Fit-Out"))))
                .andExpect(content().string(not(containsString("Site A Laboratory"))))
                // the submitted text is echoed back into the form
                .andExpect(content().string(containsString("value=\"warehouse\"")));

        // matches on the MANAGER column (case-insensitive); "Site A Laboratory"
        // is deliberately unassigned in the seed data, so it can never match here
        mockMvc.perform(get("/projects").param("q", "MAHADWAQAS28"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Head Office Fit-Out")))
                .andExpect(content().string(not(containsString("Site A Laboratory"))));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void stateFilterNarrowsByLifecycleState() throws Exception {
        // every seeded project is OPEN (schema default); nothing closes one here
        mockMvc.perform(get("/projects").param("state", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No projects match your filters")))
                .andExpect(content().string(containsString("Clear filters")))
                .andExpect(content().string(not(containsString("Head Office Fit-Out"))));

        mockMvc.perform(get("/projects").param("state", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Head Office Fit-Out")))
                .andExpect(content().string(containsString("Warehouse Extension")))
                .andExpect(content().string(containsString("Site A Laboratory")));

        // a hand-edited unknown value degrades to the full list — never a 500
        mockMvc.perform(get("/projects").param("state", "GARBAGE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Site A Laboratory")));

        // no match at all → the filtered empty state with a way back
        mockMvc.perform(get("/projects").param("q", "no-such-project-anywhere"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No projects match your filters")))
                .andExpect(content().string(not(containsString("No projects yet"))));
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void filtersDoNotWidenAccessToTheProjectArea() throws Exception {
        mockMvc.perform(get("/projects").param("q", "warehouse"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/projects").param("state", "OPEN"))
                .andExpect(status().isForbidden());
    }

    // ---- live filtering (?partial=true serves the results fragment) --------

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void theListIsWiredForLiveFilteringWithoutAnApplyButton() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"filter-form\"")))
                .andExpect(content().string(containsString("id=\"project-results\"")))
                .andExpect(content().string(containsString("/js/project-filter.js")))
                // filtering is automatic — no Apply/Clear controls left to press
                .andExpect(content().string(not(containsString("Apply</button>"))))
                .andExpect(content().string(not(containsString(">Clear</a>"))));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void partialResponseReturnsOnlyTheFilteredResultsFragment() throws Exception {
        // no filters → every row, but only the fragment (no page chrome)
        mockMvc.perform(get("/projects").param("partial", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"project-results\"")))
                .andExpect(content().string(containsString("Head Office Fit-Out")))
                .andExpect(content().string(containsString("Site A Laboratory")))
                .andExpect(content().string(not(containsString("class=\"topbar\""))))
                .andExpect(content().string(not(containsString("id=\"filter-form\""))));

        // a text filter narrows the fragment exactly like the full page
        mockMvc.perform(get("/projects")
                        .param("q", "warehouse")
                        .param("partial", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Warehouse Extension")))
                .andExpect(content().string(not(containsString("Head Office Fit-Out"))));

        // no match → the filtered empty state inside the fragment
        mockMvc.perform(get("/projects")
                        .param("state", "CLOSED")
                        .param("partial", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No projects match your filters")))
                .andExpect(content().string(containsString("Clear filters")))
                .andExpect(content().string(not(containsString("Head Office Fit-Out"))));
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void partialResponsesStayAdminOnly() throws Exception {
        // the fragment route is the same URL + requireAdmin() (ASR-01, AC-33)
        mockMvc.perform(get("/projects").param("partial", "true"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/projects")
                        .param("q", "warehouse")
                        .param("partial", "true"))
                .andExpect(status().isForbidden());
    }

    // ---- CSRF (ADR-08) ----------------------------------------------------

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void creatingProjectRequiresACsrfToken() throws Exception {
        String name = uniqueProjectName();

        // Spring Security refuses a POST without a CSRF token before dispatch:
        // the invariant that matters (ADR-08) is that the handler never runs —
        // so no create-success redirect is issued and no project is created.
        mockMvc.perform(post("/projects/new").param("name", name))
                .andExpect(result -> {
                    String redirectTo = result.getResponse().getRedirectedUrl();
                    boolean reachedCreateFlow = redirectTo != null
                            && redirectTo.matches("/projects/[^/]+/manager");
                    assertThat(reachedCreateFlow)
                            .as("create flow entered without a CSRF token, redirect: %s", redirectTo)
                            .isFalse();
                });

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(name))))
                .andExpect(content().string(containsString("Site A Laboratory")));

        // Positive control: the same request with the token succeeds.
        mockMvc.perform(post("/projects/new").param("name", uniqueProjectName()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/projects/*/manager"));
    }

    // ---- FR-31 + FR-15: assignment flows through to the Manager's scope --

    @Test
    void assignedManagerSeesTheProjectOnTheirDashboard() throws Exception {
        String name = uniqueProjectName();

        // Admin creates the project — the per-request user() keeps the two
        // halves of this test in their own security contexts.
        String redirectTo = mockMvc.perform(post("/projects/new")
                        .param("name", name)
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();
        assertThat(redirectTo)
                .as("creation redirects to the Manager assignment form")
                .matches("/projects/\\d+/manager");
        String projectId = redirectTo.replaceAll("\\D", "");

        mockMvc.perform(post("/projects/" + projectId + "/manager")
                        .param("managerEmail", MANAGER_EMAIL)
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        // FR-15 — the assignment is now part of that Manager's project scope
        mockMvc.perform(get("/manager").with(user(asUser(MANAGER_EMAIL, "MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(name)));
    }

    // ---- BR-19: only an existing Manager account may be assigned ---------

    @Test
    void assigningAnEmailThatIsNotAManagerIsRejected() throws Exception {
        String redirectTo = mockMvc.perform(post("/projects/new")
                        .param("name", uniqueProjectName())
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();
        String projectId = redirectTo.replaceAll("\\D", "");

        // an existing account with the wrong role is refused with a message,
        // the form is redisplayed and the typed email is kept
        mockMvc.perform(post("/projects/" + projectId + "/manager")
                        .param("managerEmail", INSPECTOR_EMAIL)
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is not a registered Manager")))
                .andExpect(content().string(containsString("value=\"" + INSPECTOR_EMAIL + "\"")));

        // an address that is not an account at all is refused the same way
        mockMvc.perform(post("/projects/" + projectId + "/manager")
                        .param("managerEmail", "nobody@nowhere.test")
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is not a registered Manager")));

        // nothing was written: the project still has no Manager
        mockMvc.perform(get("/projects/" + projectId + "/manager")
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No Manager assigned yet")));
    }

    @Test
    void reassigningTheManagerWhoAlreadyHoldsTheProjectIsRejected() throws Exception {
        String redirectTo = mockMvc.perform(post("/projects/new")
                        .param("name", uniqueProjectName())
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();
        String projectId = redirectTo.replaceAll("\\D", "");

        mockMvc.perform(post("/projects/" + projectId + "/manager")
                        .param("managerEmail", MANAGER_EMAIL)
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // the same Manager again — refused with a message instead of a fake success
        mockMvc.perform(post("/projects/" + projectId + "/manager")
                        .param("managerEmail", MANAGER_EMAIL)
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is already the Manager")));

        // unchanged: still shown as the current Manager
        mockMvc.perform(get("/projects/" + projectId + "/manager")
                        .with(user(asUser(ADMIN_EMAIL, "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Current: " + MANAGER_EMAIL)));
    }

    // ---- FR-14/15/16 + FR-42: merged dashboard cards ----------------------

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void inspectorSeesNotificationAndReportCardsEmptyUntilReportsArrive() throws Exception {
        mockMvc.perform(get("/inspector"))
                .andExpect(status().isOk())
                // FR-42 — notifications belong to the owning Inspector only
                .andExpect(content().string(containsString("Notifications")))
                .andExpect(content().string(containsString("No notifications.")))
                // FR-14 — own reports; empty until Safety Report Management lands
                .andExpect(content().string(containsString("Your reports")))
                .andExpect(content().string(containsString("No reports to show.")));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, authorities = "ROLE_MANAGER")
    void managerSeesAssignedProjectReportCardEmptyUntilReportsArrive() throws Exception {
        mockMvc.perform(get("/manager"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reports of your assigned projects")))
                .andExpect(content().string(containsString("No reports to show.")))
                // notifications are never offered to a Manager
                .andExpect(content().string(not(containsString("No notifications."))));
    }

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void adminSeesCrossProjectReportCardEmptyUntilReportsArrive() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Open and closed reports across all projects")))
                .andExpect(content().string(containsString("No reports to show.")))
                .andExpect(content().string(not(containsString("No notifications."))));
    }

    // ---- display-only navigation (enforcement stays server-side) ---------

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void theProjectsNavlinkIsOfferedToAdmins() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/projects\"")));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, authorities = "ROLE_MANAGER")
    void theProjectsNavlinkIsNotOfferedToOtherRoles() throws Exception {
        mockMvc.perform(get("/manager"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/projects\""))));
    }

    // ---- helpers ----------------------------------------------------------

    /** Unique name so this test can run against the shared seed data safely. */
    private String uniqueProjectName() {
        return "Integration Test Project " + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Signed-in identity for a single request (Spring Security test support
     * builds the granted authorities from the role, prefixing {@code ROLE_}).
     * The password is never checked — authentication itself is covered by
     * {@link DashboardAccessIntegrationTest}.
     */
    private static UserDetails asUser(String email, String role) {
        return User.withUsername(email)
                .password("{noop}not-used")
                .roles(role)
                .build();
    }
}
