# Implementation Plan — Current

**Version:** v04 (current; preserved as `plan-v04-Eman.md`; Mahad's line: `plan-v01-Mahad.md`, `plan-v02-Mahad.md`, `plan-v03-Mahad.md`)
**Date:** 27 November 2026
**Status:** Slice 3 (Report Creation & Photo Evidence) — implemented in this repository; compiles, 14 unit tests pass, and the app was started against local MySQL with an end-to-end submission verified (27 Nov 2026)
**History:** Supersedes v03; the pre-change document is preserved as `plan-v04-Eman.md`.

## Changes from v03

1. **Slice 3 implemented here** (`docs/`-only repository had no source): added a minimal
   runnable Maven/Spring Boot baseline (ADR-08 stack) plus the report-creation flow.
2. **Team decisions recorded:** photographs are capped at 3 per violation, 10 MB each,
   JPEG or PNG only; hazard category and severity level lists are team-supplied via
   configuration and always include an **OTHER** free-text option.
3. **Open issues resolved by proposal** (no separate human decision requested):
   FR-44 version snapshot is recorded on the first submission too (AC-37);
   double submission is blocked by a pessimistic-locked one-time form token (EC-12);
   A-04 Inspector→Manager designation is one `manager_email` on the user record.
4. **Status representation:** `submission_status` and nullable `progress_status` are
   separate columns, keeping Pending Approval distinct from Open/Closed (AGENTS.md §7).
5. **Conflict still open:** this repo contains no code from Slices 1/2/6 despite their
   "delivered" status in v03; the new baseline is a minimum skeleton, not a replacement
   for Manal's/Dashboard code. Reconcile when those sources are available.

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
  `hse.form.hazard-categories` / `hse.form.severity-levels`; the form always offers
  **OTHER** with a required free-text field. No values are hard-coded by the agent.
- First submission records an immutable version snapshot (FR-44 / AC-37), so Slice 4
  only needs to add resubmission behavior.
- Double submission (EC-12) is prevented by a one-time form token with a pessimistic
  database lock; the second POST reuses the first report instead of creating another.
- A-04 is implemented as one `manager_email` on the Inspector's user record.
- No JDK or Maven is installed in this environment: the code is written but **not
  compiled and tests are not run**. Slices 1/2/6 sources are not present here.

**Slice 6 open issues:**

- **Port for Eman (AGENTS.md §6.8 proposal):** `ProjectReportSummary`
  (`countOpen`, `countPending`, `closeAllOpen`, `discardPending`). Interim default
  returns 0 / no-op with a loud `TODO(EMAN)` — closure works but the Flow 9.4 warnings
  show zero counts until the report module lands. Real report side-effects are Eman's.
- **Auth gates:** `TODO(MANAL)` — server-side Admin-only enforcement required before
  delivery (AGENTS.md §8; permission matrix §4: create/assign/close = Admin Yes only).
- **Package:** `hse.reporting.project` is a temporary package pending alignment with
  Manal's skeleton layout (flag if different).

## 3. Vertical Slices

| Slice | Name | Owner | Scope |
|---|---|---|---|
| 1 | Project Foundation & Authentication | Manal | Skeleton, schema baseline, Spring Security login, role + project-scope authorization (ADR-03). Minimal baseline added here for Slice 3; reconcile with Manal's version. |
| 2 | Dashboard Interface | Mahad | Post-login landing page: role-based report lists (FR-14/15/16), rejection notifications (FR-42). **UI delivered** elsewhere; not present in this repo. |
| 3 | Report Creation & Photo Evidence | Eman | **Implemented here** (unit tested): report form, project eligibility, single-incident + duplicate checks, photograph evidence, atomic submit with Pending Approval, version snapshot. |
| 4 | Manager Approval & Rejection Workflow | Eman | Approve → Open (FR-09), reject with reason → Rejected + notification (FR-11, FR-42), edit/resubmit + versions (FR-43, FR-44). |
| 5 | Report Visibility & Corrective Actions | — | Role-scoped report queries backing the dashboard lists (FR-14/15/16), corrective action CRUD while Open (FR-20, FR-21, FR-47). |
| 6 | Project Management & Closure | **Mahad — IN PROGRESS** | Create project (FR-30), assign/reassign Manager email + unique names (FR-31, EC-36), close with Open warning (FR-32) / confirm auto-close (FR-33) / decline (FR-34), Pending discard warning (FR-35), post-closure read-only (FR-36). |
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
- **New tables:** `app_user`, `project`, `safety_report`, `safety_violation`,
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
| 1 — Foundation & Auth | Owned by Manal — in progress (per team). Minimal baseline added here for Slice 3; not a substitute for Manal's implementation. |
| 2 — Dashboard Interface | **UI delivered** (dashboard.html, fragments/header.html, dashboard.css); data integration pending Slices 1/3/4/5 |
| 3 — Report Creation & Photos | **Implemented here** (Eman's slice) — compiles; 14 unit tests pass; started against MySQL and end-to-end submission verified (27 Nov 2026) |
| 4 — Approval & Rejection | Not started (Eman) |
| 5 — Visibility & Corrective Actions | Not started |
| 6 — Project Management & Closure | **UI + module + tests delivered (Mahad)** — not runnable/compilable until Manal's skeleton (pom.xml) lands; report side-effects via port stub until Eman's module |
| 7 — Delete, Restore & Audit | Not started |
| 8 — Retention Purge | Not started |

**Overall:** Docs complete; Slice 2 UI and Slice 6 implementation reported elsewhere
but absent from this repository. Slice 3 source has been added here together with the
minimum Spring Boot/MySQL skeleton needed to host it. The code compiles, 14 unit tests
pass, and the web application was started and exercised against a local MySQL instance.

---

*Living artefact. Update whenever sequencing, dependencies, scope, decisions, or
implementation understanding changes; preserve every meaningful version as
`plan-vNN-Mahad.md` in `docs/plans/` (zero-padded; teammates use their own names).*
