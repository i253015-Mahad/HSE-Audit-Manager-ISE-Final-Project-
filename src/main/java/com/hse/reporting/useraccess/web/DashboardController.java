package com.hse.reporting.useraccess.web;

import com.hse.reporting.application.DashboardUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The three role landing pages (FR-40).
 *
 * <p>This controller only forwards to the use case and renders its result;
 * authorization happens in {@link DashboardUseCase} together with the URL rules
 * in {@code SecurityConfig} (ASR-01: presentation is never the sole enforcer).
 */
@Controller
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;

    public DashboardController(DashboardUseCase dashboardUseCase) {
        this.dashboardUseCase = dashboardUseCase;
    }

    @GetMapping("/inspector")
    public String inspector(Model model) {
        DashboardUseCase.InspectorDashboard dashboard = dashboardUseCase.inspectorDashboard();
        model.addAttribute("reports", dashboard.reports());
        model.addAttribute("notifications", dashboard.notifications());
        return "dashboard/inspector";
    }

    @GetMapping("/manager")
    public String manager(Model model) {
        DashboardUseCase.ManagerDashboard dashboard = dashboardUseCase.managerDashboard();
        model.addAttribute("assignedProjects", dashboard.assignedProjects());
        model.addAttribute("reports", dashboard.reports());
        return "dashboard/manager";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        DashboardUseCase.AdminDashboard dashboard = dashboardUseCase.adminDashboard();
        model.addAttribute("projects", dashboard.projects());
        model.addAttribute("reports", dashboard.reports());
        return "dashboard/admin";
    }
}
