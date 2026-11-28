package com.hse.reporting.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * End-to-end access control of Slice 1, run against the real schema and seed data:
 *
 * <ul>
 *   <li>NFR-01 — login with company email and password (BCrypt, seeded accounts),</li>
 *   <li>AC-32 — each role sees the interface of its role,</li>
 *   <li>AC-33 — every attempt outside the permission matrix is denied, including
 *       direct URL access, not only hidden interface controls,</li>
 *   <li>FR-08 — expired sessions are reported,</li>
 *   <li>logout ends the session.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardAccessIntegrationTest {

    private static final String ADMIN_EMAIL = "manal12cf@gmail.com";
    private static final String MANAGER_EMAIL = "mahadwaqas28@gmail.com";
    private static final String INSPECTOR_EMAIL = "i253172@nu.edu.pk";
    private static final String DEMO_PASSWORD = "hse-demo-123";

    @Autowired
    private MockMvc mockMvc;

    // ---- unauthenticated behaviour ---------------------------------------

    @Test
    void anonymousUsersAreSentToTheLoginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/manager"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void unknownUrlsRequireSignInByDefault() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginAndSessionExpiredPagesArePublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")))
                .andExpect(content().string(containsString("id=\"password\"")))
                .andExpect(content().string(containsString("id=\"password-toggle\"")))
                .andExpect(content().string(containsString("type=\"button\"")))
                .andExpect(content().string(containsString("aria-label=\"Show password\"")))
                .andExpect(content().string(containsString("src=\"/js/login.js\"")));

        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Access denied")));

        // the dedicated /session-expired page was retired: the notice now lives
        // on the login page, so the old URL is just a protected address again
        mockMvc.perform(get("/session-expired")) // FR-08
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void serverRestartShowsThePlainLoginPageWithoutAnExpiryNotice() throws Exception {
        // stale session id but recent activity marker = the site was restarted,
        // nothing expired, so no "session expired" message may appear
        Cookie recentActivity = new Cookie("hse_last_seen", Long.toString(System.currentTimeMillis()));

        mockMvc.perform(get("/login?expired").cookie(recentActivity))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")))
                .andExpect(content().string(not(containsString("session has expired"))));
    }

    @Test
    void genuineIdleTimeoutShowsTheExpiryNotice() throws Exception {
        // last activity older than any plausible session timeout = FR-08 expiry
        long fourHoursAgo = System.currentTimeMillis() - Duration.ofHours(4).toMillis();
        Cookie staleActivity = new Cookie("hse_last_seen", Long.toString(fourHoursAgo));

        mockMvc.perform(get("/login?expired").cookie(staleActivity))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("session has expired")));
    }

    // ---- real login with seeded accounts (NFR-01) ------------------------

    @Test
    void adminLogsInWithCompanyEmailAndSeesTheAdminInterface() throws Exception {
        MockHttpSession session = login(ADMIN_EMAIL, DEMO_PASSWORD);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HSE Engineer / Admin")))
                .andExpect(content().string(containsString("href=\"/admin\">ADMIN</a>")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString(">Admin</a>"))))
                .andExpect(content().string(containsString("Head Office Fit-Out")))
                .andExpect(content().string(containsString("Site A Laboratory")));
    }

    @Test
    void managerLogsInAndSeesOnlyAssignedProjects() throws Exception {
        MockHttpSession session = login(MANAGER_EMAIL, DEMO_PASSWORD);

        mockMvc.perform(get("/manager").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/manager\">MANAGER</a>")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString(">Manager</a>"))))
                .andExpect(content().string(containsString("Head Office Fit-Out")))
                .andExpect(content().string(containsString(MANAGER_EMAIL)))
                // project scope: never a project assigned to somebody else
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("Warehouse Extension"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("Site A Laboratory"))));
    }

    @Test
    void inspectorLogsInAndSeesTheInspectorInterface() throws Exception {
        MockHttpSession session = login(INSPECTOR_EMAIL, DEMO_PASSWORD);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inspector"));

        mockMvc.perform(get("/inspector").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Inspector")))
                .andExpect(content().string(containsString("href=\"/inspector\">INSPECTOR</a>")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString(">Inspector</a>"))));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", ADMIN_EMAIL)
                        .param("password", "wrong-password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    // ---- AC-32: each role sees the interface of its role ------------------

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void adminSeesItsOwnInterface() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HSE Engineer / Admin")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("+ New report"))));
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, authorities = "ROLE_MANAGER")
    void managerSeesItsOwnInterface() throws Exception {
        mockMvc.perform(get("/manager"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My projects")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("+ New report"))));
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void inspectorSeesItsOwnInterface() throws Exception {
        mockMvc.perform(get("/inspector"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Safety Team Inspector")))
                .andExpect(content().string(containsString("+ New report")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString(">New report</a>"))));
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void inspectorIsTakenToTheInspectorPageAfterSignIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inspector"));
    }

    // ---- AC-33: direct access outside the permission matrix is denied -----

    @Test
    @WithMockUser(username = ADMIN_EMAIL, authorities = "ROLE_ADMIN")
    void adminCannotOpenManagerOrInspectorAreas() throws Exception {
        mockMvc.perform(get("/manager")).andExpect(status().isForbidden());
        mockMvc.perform(get("/inspector")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = MANAGER_EMAIL, authorities = "ROLE_MANAGER")
    void managerCannotOpenAdminOrInspectorAreas() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
        mockMvc.perform(get("/inspector")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void inspectorCannotOpenManagerOrAdminAreas() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isForbidden());
        mockMvc.perform(get("/manager")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = INSPECTOR_EMAIL, authorities = "ROLE_INSPECTOR")
    void deniedRequestRendersTheAccessDeniedPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden())
                .andReturn();
        // Spring Security forwards to the access-denied page; MockMvc records forwards
        // without rendering them, so assert the forward target here. The page itself is
        // verified by GET /403 in loginAndSessionExpiredPagesArePublic.
        assertThat(result.getResponse().getForwardedUrl()).isEqualTo("/403");
    }

    // ---- logout -----------------------------------------------------------

    @Test
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = login(ADMIN_EMAIL, DEMO_PASSWORD);

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ---- helpers ----------------------------------------------------------

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).as("session after successful login").isNotNull();
        return (MockHttpSession) session;
    }
}
