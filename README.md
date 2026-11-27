# HSE Safety Reporting System

A web-based application for recording and managing safety incident reports across
projects, with role-based workflows for Inspectors, Managers, and Admin/HSE Engineers.

## Features

- **Inspector:** Create and submit standardized safety reports with photograph-only
  evidence; edit and resubmit rejected reports (with version history).
- **Manager:** Review pending reports for assigned projects; approve or reject with a
  reason; manage corrective actions while reports are Open.
- **Admin / HSE Engineer:** Cross-project oversight; close/delete/restore reports;
  reassign Managers; view audit history; manage project lifecycle and retention purge.

**Report lifecycle:** Pending Approval → Open (approved) / Rejected (editable, resubmittable)
→ Closed (immutable) → Deleted (Admin-only tab) → purged after 3-month retention.

## Technology Stack (ADR-08)

| Layer | Choice |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| Views | Thymeleaf (server-rendered HTML/CSS/JavaScript) |
| Security | Spring Security (role + project-scope authorization) |
| Database | MySQL with Spring Data JPA |
| Photo storage | Local filesystem (references stored in DB, ADR-06) |
| Packaging | Single executable JAR |

## Documentation

| Document | Purpose |
|---|---|
| [`docs/PRD.md`](docs/PRD.md) | Product requirements: functional/non-functional requirements, business rules, flows, acceptance criteria, resolved open questions. |
| [`docs/architecture.md`](docs/architecture.md) | Working architecture baseline: layers, modules, data ownership, technology stack. |
| [`docs/AGENTS.md`](docs/AGENTS.md) | Operating instructions for AI coding agents working in this repository. |
| [`docs/HSE-Architecture-ADRs/`](docs/HSE-Architecture-ADRs/) | Architecture Decision Records (ADR-01…ADR-06, ADR-08). |
| [`docs/plans/`](docs/plans/) | Living implementation plan (`plan.md`) with preserved per-author version history (`plan-v01-Mahad.md`, `plan-v02-Mahad.md`, … — each teammate versions under their own name). |

## Working with AI Agents

See [`docs/AGENTS.md`](docs/AGENTS.md) for the full rules. Highlights:

- The agent implements; it does not make architecture decisions. Ambiguities go to a human.
- Only the ADR-08 stack is allowed; changes need human approval + an ADR update.
- Git commits/pushes always require explicit human approval.
- Read only what is relevant to the task; prefer targeted search over whole-project reads.
- Plans live in `docs/plans/`, versioned as `plan-vNN.md`.

## Plan

The current implementation plan is [`docs/plans/plan.md`](docs/plans/plan.md). It defines
the product objective, assumptions, vertical slices, priorities, dependencies, acceptance
criteria references, and slice status. It is updated whenever sequencing, dependencies,
scope, or understanding changes — with every meaningful version preserved.
