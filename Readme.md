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

## Running locally (Slice 3 baseline)

Prerequisites: **JDK 17**, **Maven 3.9**, and **MySQL 8+**. In this environment the
tools are portable installs under `C:\Users\hp\AppData\Local\Temp\opencode\tools\`.
`mvn test` passed with 14 tests on 27 November 2026.

### Local MySQL (development)

A portable MySQL 8.4.9 instance is configured at `127.0.0.1:3306` with database
`hse_reporting`, root user with an empty password (development only), and data in
`C:\Users\hp\AppData\Local\Temp\opencode\tools\mysql-data`. Its option file is
`...\tools\mysql\my.ini`.

```powershell
# Start MySQL from its portable installation
mysqld --defaults-file=C:\Users\hp\AppData\Local\Temp\opencode\tools\mysql\my.ini --console

# Start the application with the dev seed data
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Then open `http://127.0.0.1:8080/login` and sign in as `inspector@hse.test` /
`password`. The `dev` profile seeds `inspector@hse.test`, `manager@hse.test`,
`admin@hse.test` (all password `password`) plus `Sample Site`. It also creates the
seven Slice 3 tables in `hse_reporting`. The seeded empty root password and users are
**development only** — production users and Manager designations come from company data.

Configuration overrides: `HSE_DB_URL`, `HSE_DB_USERNAME`, `HSE_DB_PASSWORD`,
`HSE_PHOTO_DIR`. Set the agreed category/severity lists in
`application.properties` before real use; the form always offers **OTHER**.

Verified on 27 November 2026: login → report form → one report with a PNG photograph
saved to MySQL + the local photo directory → first version snapshot → reused form token
produced the duplicate-submission warning without creating a second report. The test
submission was removed afterwards so the dev database starts clean.

## Documentation

| Document | Purpose |
|---|---|
| [`docs/PRD.md`](docs/PRD.md) | Product requirements: functional/non-functional requirements, business rules, flows, acceptance criteria, resolved open questions. |
| [`docs/architecture.md`](docs/architecture.md) | Working architecture baseline: layers, modules, data ownership, technology stack. |
| [`docs/AGENTS.md`](docs/AGENTS.md) | Operating instructions for AI coding agents working in this repository. |
| [`docs/HSE-Architecture-ADRs/`](docs/HSE-Architecture-ADRs/) | Architecture Decision Records (ADR-01…ADR-06, ADR-08). |
| [`docs/plans/`](docs/plans/) | Living implementation plan (`plan.md`) with preserved version history (`plan-v01.md`, `plan-v03-Mahad.md`, `plan-v04-Eman.md`, …). |

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
