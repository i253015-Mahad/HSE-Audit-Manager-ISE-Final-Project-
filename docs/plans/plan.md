# Implementation Plan — Current

**Version:** v03 (current; Mahad's line: `plan-v01-Mahad.md`, `plan-v02-Mahad.md`, `plan-v03-Mahad.md`)
**Date:** 25 November 2026
**Status:** Slice 6 (Project Management & Closure) — delivered (awaiting Manal's skeleton to run)
**History:** Supersedes `plan-v02-Mahad.md`; this revision is preserved as `plan-v03-Mahad.md`.

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
- **Auth gates:** `TODO(MANAL)` — server-side Admin-only enforcement required before
  delivery (AGENTS.md §8; permission matrix §4: create/assign/close = Admin Yes only).
- **Package:** `hse.reporting.project` is a temporary package pending alignment with
  Manal's skeleton layout (flag if different).

## 3. Vertical Slices

| Slice | Name | Owner | Scope |
|---|---|---|---|
| 1 | Project Foundation & Authentication | Manal | Skeleton, schema baseline, Spring Security login, role + project-scope authorization (ADR-03). |
| 2 | Dashboard Interface | Mahad | Post-login landing page: role-based report lists (FR-14/15/16), rejection notifications (FR-42). **UI delivered**; data integration pending Slices 1/3/4/5. |
| 3 | Report Creation & Photo Evidence | Eman | Submit reports (FR-01–FR-05), duplicate check (FR-06), photo upload (FR-07, ADR-06), Pending Approval. |
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

## 7. Current Slice Status

| Slice | Status |
|---|---|
| 1 — Foundation & Auth | Owned by Manal — in progress (per team) |
| 2 — Dashboard Interface | **UI delivered** (dashboard.html, fragments/header.html, dashboard.css); data integration pending Slices 1/3/4/5 |
| 3 — Report Creation & Photos | Owned by Eman — in progress (per team) |
| 4 — Approval & Rejection | Not started (Eman) |
| 5 — Visibility & Corrective Actions | Not started |
| 6 — Project Management & Closure | **UI + module + tests delivered (Mahad)** — not runnable/compilable until Manal's skeleton (pom.xml) lands; report side-effects via port stub until Eman's module |
| 7 — Delete, Restore & Audit | Not started |
| 8 — Retention Purge | Not started |

**Overall:** Docs complete; Slice 2 UI delivered; Slice 6 implementation underway in
parallel with Manal's auth (Slice 1) and Eman's reports (Slice 3).

---

*Living artefact. Update whenever sequencing, dependencies, scope, decisions, or
implementation understanding changes; preserve every meaningful version as
`plan-vNN-Mahad.md` in `docs/plans/` (zero-padded; teammates use their own names).*
