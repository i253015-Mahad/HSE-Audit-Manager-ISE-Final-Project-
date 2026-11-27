# Implementation Plan — Current

**Version:** v06 (current; author: Eman Ishaq; supersedes the v05 merged-integration revision)
**Author:** Eman Ishaq
**Date:** 27 November 2026
**Status:** Slices 1, 2, 3 and 6 are merged into one runnable application on branch `eman`; Slice 3 pages use the shared theme and layout fragments. The report form now ships example hazard/severity dropdown lists and reveals the OTHER free-text inputs only after OTHER is selected.
**History:** Supersedes v05 (merge/integration and UI consistency). The pre-merge Eman revision is preserved as `plan-v04-Eman.md`; v05 remains in git history.

## Changes from v05 — report-form choices and evidence

1. **Hazard category and severity level dropdowns** now include example team-style
   values from `application.yml` (`Working at height`, `Slip/trip/fall`,
   `Manual handling`, `Low`, `Medium`, `High`, etc.) while remaining overridable via
   `HSE_HAZARD_CATEGORIES` / `HSE_SEVERITY_LEVELS`.
2. **Conditional OTHER inputs:** the free-text boxes are hidden, disabled and cleared
   until **OTHER** is chosen; selecting a listed value hides them again. The behavior
   also applies to cloned violation rows.
3. **Evidence:** `Events Log/Eman Ishaq workflow evidence log.md` records the clean
   build, browser flow and database checks for this revision.
4. **Verification:** `mvn clean test` passes 70/70, and the submitted-page flow was
   rechecked with a listed hazard/severity pair (no OTHER text required).

## Changes from v04 — integration of Eman's Slice 3

1. **Merged `origin/main` (Slices 1/2/6, packages `com.hse.reporting`) into Eman's
   branch (Slice 3, package `hse.reporting`)**, keeping Slice 1's `pom.xml`,
   `SecurityConfig` (email login), `fragments.html`, CSS design system and role
   dashboards as the shared baseline. The merged build stays on Java 17 / Spring Boot
   3.3.5 so the verified portable toolchain works.
2. **Slice 3 report/photo use cases kept intact**, but its duplicate infrastructure
   (own application class, security config, user details service, login controller and
   user table) was removed in favour of the merged Slice 1 infrastructure.
3. **FR-04 Inspector→Manager designation** is now held in the slice-owned
   `inspector_designation` table; the shared `users` table stays owned by User and
   Access Management. `project` collapsed into the shared `projects` table (`state`
   replaces the old `closed` flag).
4. **Theme/structure consistency:** report pages now use `fragments :: head` and
   `fragments :: topbar`; `report-form.css` layers on the shared `app.css` tokens;
   an INSPECTOR-only "New report" navlink was added to the shared topbar.
5. **Team decisions carried forward:** photographs capped at 3 per violation, 10 MB
   each, JPEG/PNG only; hazard/severity lists are configuration-driven and always
   include **OTHER**; FR-44 snapshots on first submission; EC-12 double submission
   blocked with a pessimistic-locked one-time token.
6. **Schema:** `schema.sql` now includes the Slice 3 report/photo/designation tables,
   so Hibernate stays on `ddl-auto=validate` (no runtime DDL generation). Slices 3
   reads Project Management's owned `projects` entity/repository instead of defining
   a second `Project` mapping.
7. **Verification:** 70/70 tests pass (`mvn test`), and the browser flow was checked:
   email login → Inspector dashboard → New report → photo submission → submitted
   page → duplicate-warning replay. Admin dashboards and `/projects` still render
   with the same shared theme.
8. **Remaining integration debt (not in this UI pass):** dashboard `reports` /
   `notifications` model attributes are still unsupplied (Slices 3–5 wiring).

## Changes from v02

1. **Plan version naming convention (team decision, 25 Nov 2026):** each team member
   preserves their own plan versions with their name in the filename
   (`plan-vNN-Mahad.md`, `plan-vNN-Manal.md`, `plan-vNN-Eman.md`). The shared current
   artefact remains `docs/plans/plan.md`; historical versions are per-author so parallel
   plans never collide.
2. **Labor split recorded:** Manal = Slice 1 (foundation & auth), Eman = Slices 3–4
   (report creation / approval), Mahad = Slice 2 (dashboard) + Slice 6 (project
   management & closure).
3. **Corrected closure FR citations:** v02 Slice 6 row cited "FR-34, FR-35" only.
   Project closure is governed by **FR-32** (Open-report warning + confirm),
   **FR-33** (confirm → auto-close all Open, then close project), **FR-34**
   (decline → nothing changes), **FR-35** (Pending-report discard warning),
   plus **FR-30** (create), **FR-31** (Manager assign/reassign + unique names),
   **FR-36** (post-closure no report changes).
4. **Slice 2 status:** UI delivered (dashboard.html, fragments/header.html,
   dashboard.css); data integration pending Slices 1/3/4/5.
5. **Slice 6 status:** in progress (this revision).

---

## 1. Product Objective

Build the **HSE Safety Reporting System**: a web-based application for recording and
managing safety incident reports across projects.

- **Inspectors** create and submit standardized safety reports (photograph-only evidence).
- **Managers** (one per project) review and approve or reject pending reports with a reason.
- **Admin / HSE Engineers** oversee all projects, manage corrective actions, close/delete/
  restore reports, reassign Managers, view audit history, and purge expired project data.

Core lifecycle: submit → Pending Approval → approve → Open / reject → Rejected (editable
by owner Inspector, resubmittable, versioned) → Admin closes → Closed (immutable) →
delete → Deleted tab (Admin-only) → 3-month retention → purge all project data.

## 2. Implementation Assumptions & Open Issues

All 12 OQs and 8 assumptions are resolved/confirmed in `docs/PRD.md` §15/§16.
Key points: ADR-08 stack (Java, Spring Boot, Thymeleaf, Spring Security, MySQL+JPA,
local photo storage, single JAR); A-03 revised (rejection notification + edit/resubmit +
version snapshots); incident = date + location + type; corrective action Open-only,
Manager+Admin only; closure auto-closes Open and discards Pending on confirm; purge-all
at 3 months; Manager reassignment + unique project names.

**Team division (confirmed):**

- **Manal** — Slice 1: application skeleton (pom.xml, main class, Spring Security,
  MySQL config) + login/authorization.
- **Eman** — Slice 3: report creation & photo evidence; Slice 4: approval/rejection.
- **Mahad** — Slice 2: dashboard interface (UI delivered); Slice 6: project management
  & closure (this revision).

**Foundation decision:** "Wait for Manal" — Mahad writes files in standard Spring Boot
paths; they are not compilable/runnable until Manal's skeleton arrives. Auth-dependent
behavior is stubbed with `TODO(MANAL)`; report-dependent behavior with `TODO(EMAN)`.

**Slice 3 decisions (27 Nov 2026):**

- Photograph evidence: **max 3 per violation, 10 MB per file, JPEG and PNG only**;
  content type and file signature are both validated server-side (ADR-06 unchanged).
- Hazard category and severity level lists are **team-supplied** through
  `hse.form.hazard-categories` / `hse.form.severity-levels`; `application.yml` ships
  example values that can be replaced via `HSE_HAZARD_CATEGORIES` /
  `HSE_SEVERITY_LEVELS` without code changes. The form always appends **OTHER** and
  shows its free-text input only after OTHER is selected. No values are hard-coded
  in Java classes.
- First submission records an immutable version snapshot (FR-44 / AC-37), so Slice 4
  only needs to add resubmission behavior.
- Double submission (EC-12) is prevented by a one-time form token with a pessimistic
  database lock; the second POST reuses the first report instead of creating another.
- A-04 is implemented as one `manager_email` on the Inspector's user record.
- A portable JDK 17 / Maven 3.9.9 toolchain is used for local verification;
  `mvn clean test` passes 70/70. Slices 1/2/6 sources are now present in the merged
  tree.

**Slice 6 open issues:**

- **Port for Eman (AGENTS.md §6.8 proposal):** `ProjectReportSummary`
  (`countOpen`, `countPending`, `closeAllOpen`, `discardPending`). Interim default
  returns 0 / no-op with a loud `TODO(EMAN)` — closure works but the Flow 9.4 warnings
  show zero counts until the report module lands. Real report side-effects are Eman's.
- **Auth gates — RESOLVED (v04):** `/projects/**` requires ROLE_ADMIN in
  `SecurityConfig`, re-enforced in the service layer by `requireAdmin()` in every
  `ProjectController` handler (AGENTS.md §8; permission matrix §4: create/assign/close =
  Admin Yes only). Covered by `ProjectAccessIntegrationTest`.
- **Package — RESOLVED (v04):** Mahad's module lives in the single flat package
  `com.hse.reporting.projectmanagement` (root `com.hse.reporting` matches Manal's
  `@SpringBootApplication` scan root; kept flat so the package-private test seams
  `Project.setIdForTest()` and `new ProjectService(store, reports)` stay usable).

## 3. Vertical Slices

| Slice | Name | Owner | Scope |
|---|---|---|---|
| 1 | Project Foundation & Authentication | Manal | Skeleton, schema baseline, Spring Security login, role + project-scope authorization (ADR-03). **Delivered and merged.** |
| 2 | Dashboard Interface | Mahad | Post-login landing page: role-based report lists (FR-14/15/16), rejection notifications (FR-42). **UI delivered** inside Slice 1's role dashboards; data integration pending Slices 3/5. |
| 3 | Report Creation & Photo Evidence | Eman | **Implemented and merged**: report form, project eligibility, single-incident + duplicate checks, photograph evidence, atomic submit with Pending Approval, version snapshot. |
| 4 | Manager Approval & Rejection Workflow | Eman | Approve → Open (FR-09), reject with reason → Rejected + notification (FR-11, FR-42), edit/resubmit + versions (FR-43, FR-44). |
| 5 | Report Visibility & Corrective Actions | — | Role-scoped report queries backing the dashboard lists (FR-14/15/16), corrective action CRUD while Open (FR-20, FR-21, FR-47). |
| 6 | Project Management & Closure | **Mahad — delivered (merged)** | Create project (FR-30), assign/reassign Manager email + unique names (FR-31, EC-36), close with Open warning (FR-32) / confirm auto-close (FR-33) / decline (FR-34), Pending discard warning (FR-35), post-closure read-only (FR-36). |
| 7 | Delete, Restore & Audit History | — | Delete → Deleted tab (FR-25), restore while project open (FR-26), audit actor/action/timestamp (FR-46), Admin-only audit view. |
| 8 | Retention Purge | — | 3-month purge of ALL project data (FR-39, NFR-03). |

## 4. Slice Priorities and Order

1. **Slice 1** — Foundation & Auth (Manal; blocking everything data-wired).
2. **Slice 2** — Dashboard interface (Mahad; UI delivered).
3. **Slice 3** — Report creation (Eman, parallel).
4. **Slice 4** — Approval/rejection loop (Eman).
5. **Slice 5** — Visibility queries + corrective actions (feeds dashboard lists).
6. **Slice 6** — Project management + closure (Mahad; in progress).
7. **Slice 7** — Delete/restore/audit.
8. **Slice 8** — Retention purge.

## 5. Dependencies

- Slice 1 blocks all data-wired slices (auth/session/roles).
- Slice 2 UI: no hard dependency (delivered); data wiring ← Slices 1/3/4/5.
- Slice 3 ← Slice 1; Slice 4 ← Slice 3; Slice 5 ← Slice 4;
  Slice 6 ← Slice 1 (auth skeleton) + port stub for Slice 3/4 report counts;
  Slice 7 ← Slice 6; Slice 8 ← Slice 6 (+ 7 for purging audit/deleted data).

## 6. Acceptance Criteria

Authoritative text in `docs/PRD.md` §11.

| Slice | ACs |
|---|---|
| 1 | AC-01…AC-05 + NFR-01 |
| 2 (Dashboard) | AC-12, AC-13, AC-14, AC-15, AC-16, AC-41 |
| 3 | AC-06…AC-10, AC-37 |
| 4 | AC-11, AC-12, AC-34 |
| 5 | AC-17…AC-24, AC-40 |
| **6 (Projects)** | **AC-27** (Open warning + confirm/decline), **AC-28** (Pending discard warning), **AC-31** (create + add Manager; second Manager refused), **AC-33** (authorization denial incl. direct access), plus AC-25, AC-26, AC-29, AC-30, AC-32, AC-35, AC-36 |
| 7 | AC-38 |
| 8 | AC-30, NFR-02, NFR-03 |

Key exception/error cases for Slice 6: EC-09 (project without Manager not accepted for
reports), EC-10 (second Manager refused), EC-13 (pending stays if Manager never acts),
EC-14 (Manager replaced → new Manager views), EC-36 (duplicate project name refused
with the name in the message).

### Slice 3 implementation notes (v04)

- **Presentation:** `web/report/ReportController`, `ReportForm`, `ViolationForm`,
  `ReportFormValidator`, `templates/report/new.html`, `templates/report/submitted.html`,
  `static/js/report-form.js`, `static/css/report-form.css`. The form has one incident
  header plus repeatable violations; incident mixing is structurally impossible and is
  still checked for crafted per-violation fields in `SafetyReportService`.
- **Application:** `application/report/ReportSubmissionUseCase` (single transaction),
  `SubmissionToken` (EC-12), `application/project/ProjectSelectionQuery`, ports
  `AuthorizationPort` and `ProjectEligibilityPort`.
- **Business modules:** `report/` (report, violations, submission state, progress state,
  duplicate query, version snapshot) and `photo/` (validator, `PhotoStore`, local store,
  metadata entity). Photograph access will inherit report authorization.
- **Infrastructure:** JPA repositories, MySQL configuration, `data.sql` (dev profile seed
  only), local photograph directory (`hse.photos.directory`).
- **New tables (after merge):** shared `users` / `projects` from Slice 1/6 plus
  `inspector_designation` (FR-04), `safety_report`, `safety_violation`,
  `report_version`, `photograph_evidence`, `submission_token`.
- **Verification:** JDK 17 + Maven 3.9 were installed and `mvn test` was run on
  27 Nov 2026: **14 tests passed, 0 failed** (compilation succeeded). Unit tests cover
  FR-02/05/06, FR-04 refusal cases, and photograph constraints.
- **Local runtime verification (27 Nov 2026):** portable MySQL 8.4.9 configured at
  `127.0.0.1:3306/hse_reporting`; the app started with the `dev` profile and served
  `/login` and `/reports/new`. An Inspector login, report submission with one PNG
  photograph, first version snapshot, and the duplicate-token refusal were all verified
  against MySQL (one report, one violation, one photo row, no second report); the test
  rows were then removed so the dev database starts clean. Automated integration tests
  for AC-06…AC-10 and EC-12 are still to be added.

## 7. Current Slice Status

| Slice | Status |
|---|---|
| 1 — Foundation & Auth | **Delivered (Manal) and merged** — login, three role areas, deny-by-default security, demo seeds (her 32 tests) |
| 2 — Dashboard Interface | **UI delivered (Mahad) and merged into Slice 1's dashboards** — Reports/Notifications cards render in `dashboard/admin|manager|inspector.html` with empty-state until Slices 3/5 wire the data; topbar now links Inspectors to `/reports/new` |
| 3 — Report Creation & Photos | **Delivered (Eman) and merged** — 14 unit tests; end-to-end submission verified against MySQL (27 Nov 2026); pages restyled with the shared fragments/CSS |
| 4 — Approval & Rejection | Not started (Eman) |
| 5 — Visibility & Corrective Actions | Not started |
| 6 — Project Management & Closure | **Delivered (Mahad) and merged** — now compilable/runnable on Slice 1's skeleton; report side-effects still via the `ProjectReportSummary` port stub until Eman's module |
| 7 — Delete, Restore & Audit | Not started |
| 8 — Retention Purge | Not started |

**Overall:** Slices 1 (Manal), 2 (Mahad), 3 (Eman) and 6 (Mahad) are now integrated in
one application on branch `eman`. Slice 3's pages use the shared Slice 1 theme. The
dashboard lists stay empty until the role-scoped query (Slices 3–5) supplies them; this
integration pass was UI/structure only.

### Dashboard model contract (Slice 2)

Source of truth is the comment block in `templates/fragments.html` (copied verbatim in
summary form here), which the merged role dashboards render via `fragments ::
reportsCard` and `fragments :: notificationsCard`:

| Attribute | Supplied by | Meaning |
|---|---|---|
| `currentUserEmail` / `currentUserRole` | `CurrentUserModelAdvice`, every request | Acting identity; role keys the card titles and nav visibility. |
| `reports` | Application layer (Slices 3–5, **not yet supplied**) | **Already filtered** server-side to the acting role — presentation must not re-implement filtering (AGENTS.md §8): FR-14 Inspector → own reports only (pending, rejected+reason, Open, Closed); FR-15 Manager → reports of assigned projects only; FR-16 Admin → approved non-deleted Open + Closed across ALL projects, never Pending/Rejected (FR-12, AC-13). Fields used: `id, reference, project, incidentDate, location, status, rejectionReason` (nullable). |
| `notifications` | Slice 4 (**not yet supplied**) | Rejection notifications for the OWNING Inspector only (FR-42, AC-12/AC-41). Fields used: `id, reportReference, reason, createdAt`. |

While the report module is absent both attributes are simply not supplied — the cards
render their empty state ("No reports to show." / "No notifications.").
**TODO(EMAN, Slices 3–5):** supply `reports` (role-scoped query owned by Safety Report
Management). **TODO(Slice 4):** supply `notifications`.
Scope note: no statistics/charts — outside PRD scope (AGENTS.md §12.15).

---

---

*Living artefact. Update whenever sequencing, dependencies, scope, decisions, or
implementation understanding changes; preserve every meaningful version as
`plan-vNN-Mahad.md` in `docs/plans/` (zero-padded; teammates use their own names).*
