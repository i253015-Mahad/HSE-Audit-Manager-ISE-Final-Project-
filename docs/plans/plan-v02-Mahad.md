# Implementation Plan — v02

**Version:** v02
**Date:** 25 November 2026
**Status:** Baseline revised — dashboard re-sequenced; FR references corrected
**Supersedes:** `docs/plans/plan-v01-Mahad.md`

## Changes from v01

1. **Dashboard extracted from Slice 7 and moved forward to Slice 2** — team division: Manal = login authorization, Eman = report interface, dashboard = implemented first by this agent.
2. **Corrected FR reference error in v01:** Slice 7 previously cited "role-based dashboards/notifications (FR-17, FR-18)". FR-17 concerns report statuses (Open/Closed only) and FR-18 concerns Admin report closure — neither concerns dashboards. Dashboard content is actually governed by **FR-14/FR-15/FR-16** (role-based visibility) and **FR-42** (rejection notification).
3. Slice count 7 → 8 (Retention Purge split from Dashboard).

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

All 12 OQs (OQ-01…OQ-12) and 8 assumptions (A-01…A-08) are resolved/confirmed in
`docs/PRD.md` §15/§16. Key points (unchanged from v01): ADR-08 stack (Java, Spring Boot,
Thymeleaf, Spring Security, MySQL+JPA, local photo storage, single JAR); A-03 revised
(rejection notification + edit/resubmit + version snapshots); incident = date + location +
type; corrective action Open-only, Manager+Admin only; closure auto-closes Open and
discards Pending on confirm; purge-all at 3 months; Manager reassignment + unique project
names; test load 50 users / 1,000 reports / 50 projects / 2,000 photos.

**New dashboard decisions (25 Nov 2026):**

- The PRD has **no explicit dashboard FR**. Dashboard scope = role-based report lists per
  **FR-14/FR-15/FR-16** + rejection notifications per **FR-42**, shown server-side per the
  permission matrix (PRD §4). **No statistics/charts/summary counts** (not in PRD; adding
  them needs human approval per AGENTS.md §12.15).
- **Team division:** Manal implements login authorization (User and Access Management +
  application skeleton); Eman implements the report interface (Safety Report Management).
  The dashboard agent builds the interface now and **leaves those integration points empty
  (placeholder/TODO) until their code arrives** — no parallel/stub reimplementation of
  their modules (AGENTS.md §6.7: no competing authoritative copies).
- **Assumption:** application skeleton (`pom.xml`, main class, Security config) is owned by
  Manal's auth work. Dashboard files are created only in standard Spring Boot paths and do
  not create the skeleton. If this is wrong, tell the human (AGENTS.md §13).

**Open issues:** None blocking. Coordination note: dashboard route must be protected by
Manal's server-side authorization as soon as auth lands (AGENTS.md §8 — frontend hiding
alone must never be the enforcement).

## 3. Vertical Slices

| Slice | Name | Owner | Scope |
|---|---|---|---|
| 1 | Project Foundation & Authentication | Manal | Skeleton, schema baseline, Spring Security login, role + project-scope authorization (ADR-03). |
| 2 | **Dashboard Interface (NEW)** | This agent | Post-login landing page: role-based report lists (FR-14/15/16), rejection notifications for Inspector (FR-42), Thymeleaf template + CSS. Integration points for current user, report queries, notifications left empty until Slices 1/3 deliver. |
| 3 | Report Creation & Photo Evidence | Eman | Submit reports (FR-01–FR-05), duplicate check (FR-06), photo upload (FR-07, ADR-06), Pending Approval. |
| 4 | Manager Approval & Rejection Workflow | Eman | Approve → Open (FR-09), reject with reason → Rejected + notification (FR-11, FR-42), edit/resubmit + versions (FR-43, FR-44). |
| 5 | Report Visibility & Corrective Actions | — | Role-scoped report queries backing the dashboard lists (FR-14/15/16), corrective action CRUD while Open (FR-20, FR-21, FR-47). |
| 6 | Project Management & Closure | — | Project CRUD, unique names, Manager reassignment (FR-31, FR-45), closure auto-close/discard (FR-34, FR-35). |
| 7 | Delete, Restore & Audit History | — | Delete → Deleted tab (FR-25), restore while project open (FR-26), audit actor/action/timestamp (FR-46), Admin-only audit view. |
| 8 | Retention Purge | — | 3-month purge of ALL project data (FR-39, NFR-03). *(Dashboard removed from this slice vs v01.)* |

## 4. Slice Priorities and Order

1. **Slice 1** — Foundation & Auth (Manal; blocking everything).
2. **Slice 2** — Dashboard interface (started immediately as UI work; data sections empty).
3. **Slice 3** — Report creation (Eman, parallel).
4. **Slice 4** — Approval/rejection loop (Eman).
5. **Slice 5** — Visibility queries + corrective actions (feeds dashboard lists).
6. **Slice 6** — Project management + closure.
7. **Slice 7** — Delete/restore/audit.
8. **Slice 8** — Retention purge.

## 5. Dependencies

- Slice 1 blocks all data-wired slices (auth/session/roles).
- **Slice 2 (Dashboard) UI: no hard dependency** — templates/CSS buildable now.
- **Dashboard data wiring:** current user ← Slice 1 (Manal); report lists ← Slices 3+5 (Eman);
  rejection notifications ← Slice 4. Until then, list/notification sections render empty
  with explicit integration TODOs.
- Slice 3 ← Slice 1; Slice 4 ← Slice 3; Slice 5 ← Slice 4 (states must exist);
  Slice 6 ← Slice 4; Slice 7 ← Slice 6; Slice 8 ← Slice 6 (+ 7 for purging audit/deleted data).

## 6. Acceptance Criteria

Authoritative text in `docs/PRD.md` §11. Dashboard (Slice 2) mapping:

| Concern | ACs |
|---|---|
| Inspector sees only own reports (FR-14) | AC-14 |
| Manager sees only assigned-project reports (FR-15) | AC-15 |
| Admin sees approved Open/Closed across all projects (FR-16) | AC-16 |
| Admin never sees pending/rejected (FR-12) | AC-13 |
| Rejection notification to owner Inspector only (FR-42) | AC-12, AC-41 |

Other slices: Slice 1 → AC-01…AC-05 + NFR-01; Slice 3 → AC-06…AC-10, AC-37;
Slice 4 → AC-11, AC-12, AC-34; Slice 5 → AC-17…AC-24, AC-40; Slice 6 → AC-25…AC-33,
AC-35, AC-36; Slice 7 → AC-38; Slice 8 → AC-30, NFR-02, NFR-03.

## 7. Current Slice Status

| Slice | Status |
|---|---|
| 1 — Foundation & Auth | Owned by Manal — in progress (per team) |
| **2 — Dashboard Interface** | **In progress — UI first; data integration pending Slices 1/3/4/5** |
| 3 — Report Creation & Photos | Owned by Eman — in progress (per team) |
| 4 — Approval & Rejection | Not started (Eman) |
| 5 — Visibility & Corrective Actions | Not started |
| 6 — Project Management & Closure | Not started |
| 7 — Delete, Restore & Audit | Not started |
| 8 — Retention Purge | Not started |

**Overall:** Documentation complete; implementation started with Slice 2 (dashboard UI)
in parallel with teammates' auth (Slice 1) and reports (Slice 3).

---

*Living artefact. Update whenever sequencing, dependencies, scope, decisions, or
implementation understanding changes; preserve every meaningful version as `plan-vNN.md`
in `docs/plans/` (zero-padded).*
