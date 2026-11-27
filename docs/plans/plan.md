# Implementation Plan — Current

**Version:** v04 (current; Mahad's line: `plan-v01-Mahad.md` … `plan-v03-Mahad.md`; Manal's line: `plan-v02-Manal.md`)
**Date:** 25 November 2026
**Status:** Slice 1 (Foundation & Auth) and Slice 6 (Project Management & Closure) — both delivered and integrated on branch `integration/slice1`; 56/56 tests pass (`mvn test`, H2 test profile).
**History:** Supersedes `plan-v03-Mahad.md` (pre-merge revision); this revision is preserved as `plan-v04-Mahad.md` when the next version is cut.

## Changes from v03 — Slice 1 merged in

1. **Merge of Manal's Slice 1 into Mahad's line** (branch `integration/slice1`): her
   skeleton (`pom.xml`, `HseReportingApplication`, `SecurityConfig`, `application.yml`,
   MySQL/H2 config) is now the build, and Mahad's Slice 6 module was re-packaged from
   `hse.reporting.project` to `com.hse.reporting.projectmanagement` so both are
   component-scanned under the single root `com.hse.reporting`.
2. **Data boundary resolved (team decision D1):** `projects.manager_email` is the single
   authoritative record of a project's Manager; the separate `project_manager` table was
   dropped and `JdbcProjectAccessQueries` now queries `projects` directly.
3. **Auth gates wired:** `/projects/**` requires ROLE_ADMIN in `SecurityConfig` *and*
   `requireAdmin()` (ActorContext + AuthorizationService) in every `ProjectController`
   handler — the `TODO(MANAL)` open issue is closed. All `/projects` pages now use
   Slice 1's `fragments :: topbar` (session-aware logout) and `fragments :: headWithSite`.
4. **Dashboard (Slice 2) templates merged:** Mahad's Reports and Notifications cards
   moved from the standalone `dashboard.html` into Slice 1's three role dashboards;
   `dashboard.html` and the duplicate `fragments/header.html` were removed. The model
   contract is recorded in §7 below and in `templates/fragments.html`.
5. **Schema & seeds:** `projects` gained `manager_email`, `state`, `closed_at`;
   demo seed accounts remain Manal's (password `hse-demo-123`, `TODO(Manal)` to remove
   before go-live).

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
| 1 | Project Foundation & Authentication | Manal | Skeleton, schema baseline, Spring Security login, role + project-scope authorization (ADR-03). |
| 2 | Dashboard Interface | Mahad | Post-login landing page: role-based report lists (FR-14/15/16), rejection notifications (FR-42). **UI delivered** — Reports/Notifications cards now live inside Slice 1's role dashboards; data integration pending Slices 3/5. |
| 3 | Report Creation & Photo Evidence | Eman | Submit reports (FR-01–FR-05), duplicate check (FR-06), photo upload (FR-07, ADR-06), Pending Approval. |
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

## 7. Current Slice Status

| Slice | Status |
|---|---|
| 1 — Foundation & Auth | **Delivered (Manal) and merged** — login, three role areas, deny-by-default security, demo seeds (her 32 tests) |
| 2 — Dashboard Interface | **UI delivered (Mahad) and merged into Slice 1's dashboards** — Reports/Notifications cards render in `dashboard/admin|manager|inspector.html` with empty-state until Slices 3/4/5 wire the data |
| 3 — Report Creation & Photos | Owned by Eman — in progress (per team) |
| 4 — Approval & Rejection | Not started (Eman) |
| 5 — Visibility & Corrective Actions | Not started |
| 6 — Project Management & Closure | **Delivered (Mahad) and merged** — now compilable/runnable on Slice 1's skeleton; report side-effects still via the `ProjectReportSummary` port stub until Eman's module |
| 7 — Delete, Restore & Audit | Not started |
| 8 — Retention Purge | Not started |

**Overall:** Slice 1 (Manal) + Slice 6 (Mahad) integrated on branch `integration/slice1`;
full suite green (56/56, H2). Slices 3–5 pending (Eman); dashboard lists stay empty
until then.

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
