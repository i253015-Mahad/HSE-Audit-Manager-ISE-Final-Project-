package hse.reporting.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Slice 6 — ProjectService unit tests (EC-10, EC-13, EC-36, FR-30…FR-36).
 *
 * <p>NOTE: not runnable until Manal's application skeleton (pom.xml with
 * JUnit) lands — reported honestly per AGENTS.md §14 Step 6. Uses an
 * in-memory {@link ProjectStore} fake and a recording
 * {@link ProjectReportSummary} fake, so no Spring context is required.
 */
class ProjectServiceTest {

    /** In-memory store fake. */
    private static class InMemoryProjectStore implements ProjectStore {
        private final Map<Long, Project> rows = new HashMap<>();
        private long seq = 0;

        @Override
        public boolean existsByName(String name) {
            return rows.values().stream().anyMatch(p -> p.getName().equals(name));
        }

        @Override
        public Optional<Project> find(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public Project save(Project project) {
            if (project.getId() == null) {
                project.setIdForTest(++seq);
            }
            rows.put(project.getId(), project);
            return project;
        }

        @Override
        public List<Project> findAll() {
            return new ArrayList<>(rows.values());
        }
    }

    /** Recording port fake — captures closure side-effect calls (FR-33/FR-35). */
    private static class RecordingReportSummary implements ProjectReportSummary {
        long open = 0;
        long pending = 0;
        final List<String> calls = new ArrayList<>();

        @Override
        public long countOpen(Long projectId) {
            return open;
        }

        @Override
        public long countPending(Long projectId) {
            return pending;
        }

        @Override
        public void closeAllOpen(Long projectId) {
            calls.add("closeAllOpen:" + projectId);
            open = 0;
        }

        @Override
        public void discardPending(Long projectId) {
            calls.add("discardPending:" + projectId);
            pending = 0;
        }
    }

    private InMemoryProjectStore store;
    private RecordingReportSummary reports;
    private ProjectService service;

    @BeforeEach
    void setUp() {
        store = new InMemoryProjectStore();
        reports = new RecordingReportSummary();
        service = new ProjectService(store, reports);
    }

    // ------------------------------------------------------------ FR-30 / EC-36

    @Test
    @DisplayName("FR-30: Admin creates a project - OPEN, no Manager yet")
    void createProjectStartsOpenWithoutManager() {
        Project p = service.create("Tower B - Structural Works");

        assertNotNull(p.getId());
        assertEquals("Tower B - Structural Works", p.getName());
        assertEquals(Project.State.OPEN, p.getState());
        assertNull(p.getManagerEmail());
        assertNull(p.getClosedAt());
    }

    @Test
    @DisplayName("EC-36: duplicate name refused and the message names the duplicate")
    void duplicateNameRefusedWithMessageNamingIt() {
        service.create("Tower B");

        DuplicateProjectNameException e =
                assertThrows(DuplicateProjectNameException.class, () -> service.create("Tower B"));

        assertTrue(e.getMessage().contains("Tower B"),
                "EC-36 requires the message to say the name: " + e.getMessage());
        assertEquals("Tower B", e.getDuplicateName());
    }

    @Test
    @DisplayName("FR-31: blank name refused")
    void blankNameRefused() {
        assertThrows(ProjectValidationException.class, () -> service.create("   "));
    }

    // ------------------------------------------------------------ FR-31 / EC-10

    @Test
    @DisplayName("FR-31: first Manager assigned after creation")
    void assignFirstManager() {
        Project p = service.create("Warehouse");

        Project updated = service.assignManager(p.getId(), "manager@example.com");

        assertEquals("manager@example.com", updated.getManagerEmail());
    }

    @Test
    @DisplayName("EC-10: a second Manager is refused while one exists")
    void secondManagerRefused() {
        Project p = service.create("Warehouse");
        service.assignManager(p.getId(), "first@example.com");

        ManagerAlreadyAssignedException e = assertThrows(
                ManagerAlreadyAssignedException.class,
                () -> service.assignManager(p.getId(), "second@example.com"));

        assertTrue(e.getMessage().contains("first@example.com"),
                "should name the current Manager: " + e.getMessage());
        assertEquals("first@example.com", service.get(p.getId()).getManagerEmail());
    }

    @Test
    @DisplayName("FR-31/FR-45: reassign while open replaces the Manager")
    void reassignWhileOpenReplacesManager() {
        Project p = service.create("Warehouse");
        service.assignManager(p.getId(), "old@example.com");

        Project updated = service.reassignManager(p.getId(), "new@example.com");

        assertEquals("new@example.com", updated.getManagerEmail());
        assertTrue(updated.isOpen());
    }

    @Test
    @DisplayName("FR-31: reassign with no Manager yet is refused - assign first")
    void reassignWithoutManagerRefused() {
        Project p = service.create("Warehouse");

        assertThrows(ProjectValidationException.class,
                () -> service.reassignManager(p.getId(), "new@example.com"));
    }

    // ------------------------------------------------------------ FR-32...FR-35

    @Test
    @DisplayName("FR-32/FR-35: warning inputs come from the report port")
    void warningCountsComeFromPort() {
        Project p = service.create("Warehouse");
        reports.open = 4;
        reports.pending = 2;

        assertEquals(4, service.countOpenReports(p.getId()));
        assertEquals(2, service.countPendingReports(p.getId()));
    }

    @Test
    @DisplayName("FR-33/FR-35/AC-27/AC-28: confirm closes Open, discards Pending, closes project, stamps closedAt")
    void confirmedClosureRunsFullSequence() {
        Project p = service.create("Warehouse");
        service.assignManager(p.getId(), "m@example.com");
        reports.open = 3;
        reports.pending = 1;

        Project closed = service.closeConfirmed(p.getId());

        assertEquals(Project.State.CLOSED, closed.getState());
        assertNotNull(closed.getClosedAt(), "NFR-03: exact closure timestamp starts retention");
        assertEquals(List.of("closeAllOpen:" + p.getId(), "discardPending:" + p.getId()),
                reports.calls, "both report side-effects must run, in order");
        assertEquals(0, reports.open);
        assertEquals(0, reports.pending);
    }

    @Test
    @DisplayName("EC-13: declining closure (never calling closeConfirmed) changes nothing")
    void decliningClosureChangesNothing() {
        Project p = service.create("Warehouse");
        service.assignManager(p.getId(), "m@example.com");
        reports.open = 2;
        reports.pending = 5;

        // FR-34 decline path = controller never invokes closeConfirmed.
        Project unchanged = service.get(p.getId());

        assertTrue(unchanged.isOpen());
        assertNull(unchanged.getClosedAt());
        assertTrue(reports.calls.isEmpty(), "no report side-effects on decline");
        assertEquals(2, service.countOpenReports(p.getId()));
        assertEquals(5, service.countPendingReports(p.getId()));
    }

    // ------------------------------------------------------------ FR-36

    @Test
    @DisplayName("FR-36: a closed project cannot be changed (assign, reassign, re-close)")
    void closedProjectIsImmutable() {
        Project p = service.create("Warehouse");
        service.assignManager(p.getId(), "m@example.com");
        service.closeConfirmed(p.getId());

        assertThrows(ProjectValidationException.class,
                () -> service.assignManager(p.getId(), "other@example.com"));
        assertThrows(ProjectValidationException.class,
                () -> service.reassignManager(p.getId(), "other@example.com"));
        assertThrows(ProjectValidationException.class,
                () -> service.closeConfirmed(p.getId()));
    }

    // ------------------------------------------------------------ misc

    @Test
    @DisplayName("EC-09: a new project has no Manager (reports must refuse it until assigned)")
    void newProjectHasNoManager() {
        Project p = service.create("Bridge Deck");
        assertNull(p.getManagerEmail());
        assertFalse(p.getManagerEmail() != null && !p.getManagerEmail().isBlank());
    }

    @Test
    @DisplayName("ProjectNotFoundException for unknown id")
    void unknownProjectNotFound() {
        assertThrows(ProjectNotFoundException.class, () -> service.get(999L));
    }
}
