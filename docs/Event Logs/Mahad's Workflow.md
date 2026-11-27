# Workflow Evidence Log — Mahad (Deliverable 4)

## Overview & Scope
This log records the evolution of the HSE Audit Manager product driven by **Mahad** (Project Lead / Core Integrator). It details the integration of authentication, implementation of project management capabilities, schema unification, security enforcement, and workflow verification.

---

## Slice & Work Log

### 1. Integration & Foundation Setup (Slice 1 & Base Architecture)
* **Slice ID / Name**: Slice 1 (Authentication & Access Foundation Integration) & Architecture Consolidation
* **Related Requirement(s)**: FR-01 to FR-12 (Authentication, Role-Based Access Control, Session Handling), ADR-01 to ADR-08
* **Leading / Responsible Member**: Mahad (Integrator & Lead)
* **Acceptance Criteria**:
  * AC-01: Anonymous users attempting to access protected endpoints (`/`, `/admin`, `/manager`, `/inspector`, `/projects/**`) are redirected to `/login`.
  * AC-02: Authenticated users are routed to their role-specific landing page based on `ROLE_ADMIN`, `ROLE_MANAGER`, or `ROLE_INSPECTOR`.
  * AC-03: Invalid credentials render clear login error alerts.
  * AC-04: Session expiry gracefully redirects to `/login?expired` with a clear user notice without crashing or creating redirect loops.
* **HITL / AFK Mode**: HITL (Human-in-the-Loop — interactive verification of login security, session cookie behavior, and role redirection).
* **AI Chat / Task Reference**: Session integration task (`Merge Manal's branch into Mahad`).
* **Important Iterations & Self-Corrections**:
  * *Iteration 1 (Build System Creation)*: Added root `pom.xml` (Java 21, Spring Boot 3.5.16, Spring Data JPA, Security, Thymeleaf, H2) and `application.yml`.
  * *Iteration 2 (Logout & Session Fix)*: Fixed session redirect loop on logout by adding `.deleteCookies("JSESSIONID")` to `SecurityConfig` and tracking session age via `hse_last_seen` cookie in `CurrentUserModelAdvice`.
  * *Iteration 3 (UI Center Alignment)*: Updated `session-expired` / login flow styling to inherit `.page-login` layout so warning messages center properly.
* **Test / Check Result**:
  * Unit & Integration Tests: `HseUserDetailsServiceTest`, `AuthorizationServiceTest`, `DashboardAccessIntegrationTest` all passing.
  * Live HTTP Verification: Verified login redirects, valid/invalid credentials, session expiration banner via browser & curl.
* **Human Decision**: **Accept** (Live browser test passed).
* **Major Issue / Change**: Resolved redirect loops caused by stale JSESSIONID cookies during application restarts; unified session expiry onto `/login?expired`.
* **Relevant plan.md Version**: `docs/plans/plan.md` (v04)
* **Integration Status**: Merged & fully operational on `Mahad` branch.

---

### 2. Project Management & Data Model Alignment (Slice 6)
* **Slice ID / Name**: Slice 6 (Project Management & Assignment System)
* **Related Requirement(s)**: FR-17 (Create Project), FR-18 (Assign Manager), FR-19 (Close Project), FR-20 (Project Access), BR-19 (Manager Directory Validation)
* **Leading / Responsible Member**: Mahad
* **Acceptance Criteria**:
  * AC-05: Admins can view all projects, create new projects, and assign/reassign project managers.
  * AC-06: Assigning/reassigning a project manager validates that the entered email exists in the user directory with `ROLE_MANAGER` (BR-19 constraint).
  * AC-07: Attempting to assign a manager who is already assigned to the project displays an explicit warning message.
  * AC-08: Non-Admin roles attempting to access `/projects/**` receive a styled 403 Access Denied page.
* **HITL / AFK Mode**: HITL (Interactive confirmation of Decision D1, schema migrations, and role assertion logic).
* **AI Chat / Task Reference**: Integration & BR-19 validation task (`com.hse.reporting.projectmanagement` consolidation).
* **Important Iterations & Self-Corrections**:
  * *Iteration 1 (Package Consolidation)*: Moved legacy `hse.reporting.project` classes into a flat, clean package structure `com.hse.reporting.projectmanagement` to preserve package-private testing seams (`Project.setIdForTest()`).
  * *Iteration 2 (Data Boundary Decision D1)*: Dropped duplicate `project_manager` junction table. Made `projects.manager_email` the single source of truth in `schema.sql`, `schema-test.sql`, `data.sql`, and `data-test.sql`.
  * *Iteration 3 (BR-19 Directory Validation)*: Introduced `ManagerDirectory` interface and `JpaManagerDirectory` implementation to query `UserRepository` for manager role verification during assignment.
  * *Iteration 4 (CSRF Security Alignment)*: Ensured all 4 project management forms in Thymeleaf use `th:action` for automatic CSRF token inclusion. Added CSRF assertion unit tests in `ProjectAccessIntegrationTest`.
* **Test / Check Result**: `mvn test` executed — **62 / 62 tests passing** (including `ProjectServiceTest`, `ProjectAccessIntegrationTest`).
* **Human Decision**: **Accept** (Live validation confirmed: assigning invalid manager or duplicate manager produces expected error feedback).
* **Major Issue / Change**: Unified data model to eliminate table redundancy; enforced server-side validation for BR-19.
* **Relevant plan.md Version**: `docs/plans/plan.md` (v04)
* **Integration Status**: Integrated on `Mahad` branch with local MySQL & H2 test profiles.

---

### 3. Dashboard UI & Template Unification
* **Slice ID / Name**: Role Dashboard Consolidation & Navigation Integration
* **Related Requirement(s)**: FR-13 (Admin Dashboard), FR-14 (Inspector Dashboard), FR-15 (Manager Dashboard), FR-16 (Role Navigation), FR-42 (Notifications Display)
* **Leading / Responsible Member**: Mahad
* **Acceptance Criteria**:
  * AC-09: Shared topbar (`fragments.html`) dynamically displays `Projects` navigation link only for `ROLE_ADMIN`.
  * AC-10: Inspector, Manager, and Admin dashboards render role-filtered Reports and Notifications cards with empty-state placeholders (pending Slices 3–5).
  * AC-11: Duplicate templates (`dashboard.html`, `header.html`) deleted; unified layout uses `headWithSite` fragment to load CSS cascade (`site.css` -> `dashboard.css` -> `app.css` -> `projects.css`).
* **HITL / AFK Mode**: HITL (User confirmed Decision D4 to merge Notifications and Reports cards into role templates).
* **AI Chat / Task Reference**: Dashboard merge & template fragment alignment task.
* **Important Iterations & Self-Corrections**:
  * *Iteration 1 (Decision D4 Execution)*: Merged Mahad's notifications/reports cards into `inspector.html`, `manager.html`, and `admin.html`. Deleted orphan `templates/dashboard.html` and redundant `fragments/header.html`.
  * *Iteration 2 (CSS Resolution)*: Added `:has(> .card-header)` rule in `app.css` to prevent card layout collisions between design systems.
* **Test / Check Result**: HTML templates validated with Spring Thymeleaf engine; 200 OK across all dashboard routes.
* **Human Decision**: **Accept** ("I have tested the site and it is working wonderfully").
* **Major Issue / Change**: Eliminated template duplication and mapped role dashboard model contracts clearly in `plan.md` §7.
* **Relevant plan.md Version**: `docs/plans/plan.md` (v04)
* **Integration Status**: Fully merged & active.

---

### 4. Portable Toolchain & Environment Setup
* **Slice ID / Name**: Environment Toolchain & MySQL Integration
* **Related Requirement(s)**: ADR-04 (Database Architecture), ADR-08 (Technology Stack)
* **Leading / Responsible Member**: Mahad
* **Acceptance Criteria**:
  * AC-12: Portable JDK 21, Maven 3.9, and MySQL 8.4 configured under `D:\Mahad\Tools\` without requiring global system installations.
  * AC-13: `start-mysql.ps1` script initializes and runs portable MySQL instance on port 3306.
* **HITL / AFK Mode**: HITL (User-guided toolchain folder location selection).
* **AI Chat / Task Reference**: Environment setup & script debugging task.
* **Important Iterations & Self-Corrections**:
  * *Iteration 1 (Script Syntax Fix)*: Replaced garbled non-ASCII em-dash (`—`) on line 41 of `start-mysql.ps1` with standard hyphen (`-`) to fix PowerShell parser exception.
  * *Iteration 2 (Data Dir Recovery)*: Cleaned up locked MySQL InnoDB log files (`ibdata1`) to allow clean database initialization and service startup.
* **Test / Check Result**: Portable MySQL running on 3306; Spring Boot successfully connected and verified via `mysql -u hse -phse-dev-pass`.
* **Human Decision**: **Accept**.
* **Major Issue / Change**: Resolved PowerShell character encoding mismatch in script file.
* **Relevant plan.md Version**: `docs/plans/plan.md` (v04) & `README.md`
* **Integration Status**: Deployed and functional on local machine.

---

## Technical & Architectural Summary

### Branches & Repository Policy
* **Active Branch**: `Mahad` (ahead of `origin/Mahad`).
* **Branch Policy (AGENTS.md §15.1)**: All work performed directly on current working branch (`Mahad`). No unrequested branch creation.

### Rejected / Revised Approaches
1. **Junction Table `project_manager` (Rejected)**: Initially present in Manal's Slice 1 DDL. Rejected in favor of direct `projects.manager_email` column (Decision D1) to simplify ownership queries and prevent state sync bugs.
2. **Dedicated `/session-expired` Page (Revised)**: Originally rendered an isolated error page. Revised to redirect directly to `/login?expired` with an inline alert banner for better UX and alignment with FR-08.
3. **Subpackage Hierarchy for Project Module (Rejected)**: Splitting into `projectmanagement.web/data/domain` was rejected to preserve package-private seams for `ProjectServiceTest`.

### ADR & Requirements Alignment
* **ADR-01 / ADR-02**: Monolithic Spring Boot web app with clear module boundaries (`useraccess` vs `projectmanagement`).
* **ADR-03**: Spring Security with BCrypt password hashing, session-based auth, and CSRF protection.
* **ADR-04**: MySQL 8.4 primary database with Spring Data JPA & JdbcTemplate queries; H2 in-memory DB for test execution.
* **BR-19**: Strict role verification before project manager assignment.
