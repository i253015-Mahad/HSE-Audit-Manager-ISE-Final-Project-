# Implementation Plan — Current

**Version:** v01 (see `plan-v01.md` for this revision's snapshot)
**Date:** 25 November 2026
**Status:** Initial baseline

> This is the current, living `plan.md`. When its content changes meaningfully, the prior
> version is preserved as `plan-vNN.md` in this directory (zero-padded numbering) before
> saving the new current version.

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

All 12 original open questions (OQ-01…OQ-12) and all 8 assumptions (A-01…A-08) are
**resolved/confirmed** in `docs/PRD.md` §15 and §16. Key confirmed points:

- **Tech stack (ADR-08):** Java, Spring Boot, Thymeleaf, Spring Security, MySQL
  (Spring Data JPA), local filesystem photo storage, single executable JAR.
- **A-03 (revised):** Rejection reason is stored in-system and notified to the owning
  Inspector; rejected reports are editable and resubmittable; each submission saves an
  immutable version snapshot.
- **Incident identity:** date + location + incident type (FR-05).
- **Corrective action:** editable only while Open; visible to Manager + Admin only;
  retained through delete/restore; purged under FR-39.
- **Closure:** Open reports auto-closed on confirmed project closure; Pending reports
  warned then discarded on confirm (OQ-06).
- **Retention:** at 3-month expiry purge ALL project data (OQ-08/A-08).
- **Manager reassignment:** allowed while project open; project names unique (OQ-10).
- **Test load:** 50 concurrent users, 1,000 reports, 50 projects, 2,000 photos;
  95% requests < 2s (OQ-09/NFR-02).

**Open issues:** None. Any future change to these decisions requires human approval and
an updated ADR / PRD revision per `docs/AGENTS.md`.

## 3. Vertical Slices

| Slice | Name | Scope |
|---|---|---|
| 1 | Project Foundation & Authentication | Project skeleton, database schema baseline, Spring Security login, role + project-scope authorization (ASR-01, ADR-03), base layout/templates. |
| 2 | Report Creation & Photo Evidence | Inspector submits reports (FR-01–FR-05), duplicate incident check (FR-06), photo upload validation (FR-07, ADR-06), Pending Approval state. |
| 3 | Manager Approval & Rejection Workflow | Manager review, approve → Open (FR-09, FR-11), reject with reason → Rejected + in-system notification (FR-42), Inspector edit/resubmit with version snapshots (FR-43, FR-44). |
| 4 | Report Visibility & Corrective Actions | Report lists/detail by role (FR-14, FR-15, FR-16), corrective action CRUD while Open, Manager/Admin-only view (FR-20, FR-21, FR-47). |
| 5 | Project Management & Closure | Admin project CRUD, unique names, Manager assignment/reassignment (FR-31, FR-45), project closure with auto-close Open + discard Pending on confirm (FR-34, FR-35). |
| 6 | Delete, Restore & Audit History | Admin delete Closed → Deleted tab (FR-25), restore while project open (FR-26), automatic audit records actor/action/timestamp (FR-46), Admin-only audit view. |
| 7 | Retention Purge & Dashboard | 3-month retention purge of ALL project data (FR-39, NFR-03), role-based dashboards/notifications (FR-17, FR-18). |

## 4. Slice Priorities and Order

1. **Slice 1** — Foundation (blocking everything else).
2. **Slice 2** — Report creation (first end-to-end Inspector value).
3. **Slice 3** — Approval/rejection loop (core lifecycle).
4. **Slice 4** — Visibility + corrective actions (Manager/Admin value).
5. **Slice 5** — Project management + closure.
6. **Slice 6** — Delete/restore/audit.
7. **Slice 7** — Retention purge + dashboards (final hardening).

## 5. Dependencies

- Slice 1 must complete before any other slice (auth, schema, layout).
- Slice 3 depends on Slice 2 (reports must exist before approval).
- Slice 4 depends on Slice 3 (Open/Rejected states must exist).
- Slice 5 depends on Slice 3 (closure interacts with Pending/Open/Rejected).
- Slice 6 depends on Slice 5 (restore checks project-open state).
- Slice 7 depends on Slice 5 (retention triggers from project closure timestamp) and
  Slice 6 (purges audit/deleted data).

## 6. Acceptance Criteria

Full criteria live in `docs/PRD.md` §11 (AC-01…AC-41). Per-slice mapping:

| Slice | PRD Acceptance Criteria |
|---|---|
| 1 | NFR-01 (email + password auth), AC-32 (role-appropriate interface), AC-33 (deny everything outside permission matrix) |
| 2 | AC-04…AC-08, AC-37 (dup check), AC-41 (reject-notify-only-owner precondition) |
| 3 | AC-09…AC-15, AC-34 (resubmit + version) |
| 4 | AC-16…AC-24, AC-40 (audit + corrective restore) |
| 5 | AC-25…AC-33, AC-35 (reassign + audit), AC-36 (unique name) |
| 6 | AC-38 (cannot delete/restore rejected), AC-40 |
| 7 | AC-30 (purge all), NFR-02 (test volumes), NFR-03 (retention) |

*(Exact AC text is authoritative in `docs/PRD.md`.)*

## 7. Current Slice Status

| Slice | Status |
|---|---|
| 1 — Foundation & Auth | **Complete** |
| 2 — Report Creation & Photos | Not started |
| 3 — Approval & Rejection | Not started |
| 4 — Visibility & Corrective Actions | Not started |
| 5 — Project Management & Closure | Not started |
| 6 — Delete, Restore & Audit | Not started |
| 7 — Retention Purge & Dashboard | Not started |

**Overall:** Documentation complete; Slice 1 implemented and passing (32 tests). Slices 2–7 not started.

---

*Living artefact. Update whenever sequencing, dependencies, scope, decisions, or
implementation understanding changes. Preserve every meaningful version as
`plan-vNN.md` in `docs/plans/`.*
