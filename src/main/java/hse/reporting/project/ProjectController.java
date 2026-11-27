package hse.reporting.project;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Presentation entry points for Project Management (Slice 6).
 *
 * <p>Routes:
 * <ul>
 *   <li>GET  /projects               — project list</li>
 *   <li>GET  /projects/new           — create form</li>
 *   <li>POST /projects/new           — create (FR-30, EC-36)</li>
 *   <li>GET  /projects/{id}/manager  — assign / reassign Manager form (FR-31)</li>
 *   <li>POST /projects/{id}/manager  — submit Manager email (FR-31)</li>
 *   <li>GET  /projects/{id}/close    — closure warning page (FR-32 / FR-35, flow 9.4)</li>
 *   <li>POST /projects/{id}/close    — confirm (FR-33/35) or decline (FR-34)</li>
 * </ul>
 *
 * <p>TODO(MANAL / Slice 1 — auth): every route here is Admin-only per the
 * permission matrix (PRD §4: Inspector No, Manager No, Admin Yes — create,
 * assign/reassign, close). Enforce server-side with Spring Security before
 * delivery; frontend hiding alone is never the enforcement (AGENTS.md §8).
 * Direct URL access attempts must be denied (AC-33).
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projects;

    public ProjectController(ProjectService projects) {
        this.projects = projects;
    }

    @GetMapping
    public String list(Model model) {
        // TODO(MANAL): Admin-only gate + current user for the header fragment.
        List<Project> all = projects.findAll();
        model.addAttribute("projects", all);
        model.addAttribute("currentUser", "Signed-in user");   // TODO(MANAL): session
        model.addAttribute("currentRole", "ADMIN");            // TODO(MANAL): session
        return "projects/projects";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        // TODO(MANAL): Admin-only gate.
        model.addAttribute("currentUser", "Signed-in user");
        model.addAttribute("currentRole", "ADMIN");
        return "projects/project-form";
    }

    @PostMapping("/new")
    public String create(@RequestParam("name") String name,
                         Model model,
                         RedirectAttributes redirect) {
        // TODO(MANAL): Admin-only gate.
        try {
            Project project = projects.create(name);
            redirect.addFlashAttribute("flashMessage",
                    "Project \"" + project.getName() + "\" created. Now assign its Manager.");
            return "redirect:/projects/" + project.getId() + "/manager";
        } catch (DuplicateProjectNameException e) {
            // EC-36 — message names the duplicated project.
            model.addAttribute("error", e.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("currentUser", "Signed-in user");
            model.addAttribute("currentRole", "ADMIN");
            return "projects/project-form";
        } catch (ProjectValidationException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("currentUser", "Signed-in user");
            model.addAttribute("currentRole", "ADMIN");
            return "projects/project-form";
        }
    }

    @GetMapping("/{id}/manager")
    public String managerForm(@PathVariable("id") Long id, Model model) {
        // TODO(MANAL): Admin-only gate.
        Project project = projects.get(id);
        model.addAttribute("project", project);
        model.addAttribute("assigning", project.getManagerEmail() == null
                || project.getManagerEmail().isBlank());
        model.addAttribute("currentUser", "Signed-in user");
        model.addAttribute("currentRole", "ADMIN");
        return "projects/project-manager";
    }

    @PostMapping("/{id}/manager")
    public String saveManager(@PathVariable("id") Long id,
                              @RequestParam("managerEmail") String managerEmail,
                              Model model,
                              RedirectAttributes redirect) {
        // TODO(MANAL): Admin-only gate.
        try {
            Project project = projects.get(id);
            Project saved;
            boolean assigning = project.getManagerEmail() == null
                    || project.getManagerEmail().isBlank();
            if (assigning) {
                saved = projects.assignManager(id, managerEmail); // first Manager (FR-31)
            } else {
                saved = projects.reassignManager(id, managerEmail); // replace (FR-31/FR-45)
            }
            redirect.addFlashAttribute("flashMessage", assigning
                    ? "Manager assigned for \"" + saved.getName() + "\"."
                    : "Manager reassigned for \"" + saved.getName()
                            + "\" — the previous Manager no longer sees its reports.");
            return "redirect:/projects";
        } catch (ProjectValidationException e) {
            Project project = projects.get(id);
            model.addAttribute("project", project);
            model.addAttribute("assigning", project.getManagerEmail() == null
                    || project.getManagerEmail().isBlank());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("managerEmail", managerEmail);
            model.addAttribute("currentUser", "Signed-in user");
            model.addAttribute("currentRole", "ADMIN");
            return "projects/project-manager";
        }
    }

    /**
     * Flow 9.4 warning page — shows the Open-report warning (FR-32) and the
     * Pending-discard warning (FR-35) before any change happens (AC-27, AC-28).
     */
    @GetMapping("/{id}/close")
    public String closeForm(@PathVariable("id") Long id, Model model) {
        // TODO(MANAL): Admin-only gate.
        Project project = projects.get(id);
        if (!project.isOpen()) {
            return "redirect:/projects";
        }
        model.addAttribute("project", project);
        model.addAttribute("openCount", projects.countOpenReports(id));
        model.addAttribute("pendingCount", projects.countPendingReports(id));
        model.addAttribute("currentUser", "Signed-in user");
        model.addAttribute("currentRole", "ADMIN");
        return "projects/project-close";
    }

    /**
     * confirm=true  → FR-33 (auto-close Open) + FR-35 (discard Pending) + close.
     * confirm=false → FR-34: nothing changes; back to the list.
     */
    @PostMapping("/{id}/close")
    public String close(@PathVariable("id") Long id,
                        @RequestParam(value = "confirm", defaultValue = "false") boolean confirm,
                        RedirectAttributes redirect) {
        // TODO(MANAL): Admin-only gate.
        Project project = projects.get(id);
        if (!confirm) {
            // FR-34 / AC-27 decline path — no state change at all.
            redirect.addFlashAttribute("flashMessage",
                    "Closure declined — nothing changed for \"" + project.getName() + "\".");
            return "redirect:/projects";
        }
        Project saved = projects.closeConfirmed(id);
        redirect.addFlashAttribute("flashMessage",
                "Project \"" + saved.getName() + "\" closed. Its reports were finalised; "
                        + "data is kept for 3 months, then purged.");
        return "redirect:/projects";
    }
}
