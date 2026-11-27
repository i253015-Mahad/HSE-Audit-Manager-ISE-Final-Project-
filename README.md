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
| Language | Java 17 (merged build) |
| Framework | Spring Boot 3.3.5 |
| Views | Thymeleaf with the shared `fragments.html` layout + `app.css` theme |
| Security | Spring Security (email login, role + project-scope authorization) |
| Database | MySQL with Spring Data JPA |
| Photo storage | Local filesystem (references stored in DB, ADR-06) |
| Packaging | Single executable JAR |

## Build and Run

Requirements: **JDK 17** and **Maven 3.9+**. This machine has portable tools under
`C:\Users\hp\AppData\Local\Temp\opencode\tools\` (JDK 17.0.21.1, Maven 3.9.9,
MySQL 8.4.9). MySQL is needed **only to run the app**; `mvn test` uses in-memory H2.

### 1. Database

Start the portable MySQL server (database `hse_reporting`):

```powershell
mysqld --defaults-file=C:\Users\hp\AppData\Local\Temp\opencode\tools\mysql\my.ini --console
```

The merged application uses `root` with an empty local development password by default.
Override with `HSE_DB_URL` / `HSE_DB_USERNAME` / `HSE_DB_PASSWORD` if you use the
partners' MySQL instance or their `hse` / `hse-dev-pass` account instead.

On startup Spring runs `schema.sql` (idempotent DDL, now including the Slice 3
report/photo and designation tables) and `data.sql` (demo seeds). Hibernate runs with
`ddl-auto=validate`, so entities are checked against the schema rather than generating DDL.

### 2. Run the application

```powershell
mvn spring-boot:run
```

Open <http://127.0.0.1:8080/login>.

### 3. Demo accounts (development seeds)

| Email | Role | Password | Notes |
|---|---|---|---|
| `manal12cf@gmail.com` | Admin | `hse-demo-123` | Slice 1 demo account |
| `mahadwaqas28@gmail.com` | Manager | `hse-demo-123` | Manager of Head Office Fit-Out |
| `i253172@nu.edu.pk` | Inspector | `hse-demo-123` | Designated to `mahadwaqas28@gmail.com` |
| `inspector@hse.test` | Inspector | `password` | Slice 3 report-creation account |
| `manager@hse.test` | Manager | `password` | Manager of Sample Site |
| `admin@hse.test` | Admin | `password` | Slice 3 admin account |

### 4. Browser preview and testing

1. Sign in as `inspector@hse.test` / `password`. The login page uses the shared
   login-card design.
2. The Inspector dashboard shows the shared topbar (brand, role badge, email, log out)
   and a **New report** navlink; click it to open `/reports/new`.
3. On the report form, verify the same topbar/card styling, fill one incident, add a
   violation, choose from the example hazard/severity dropdown lists, then select
   **OTHER** to reveal and complete the free-text field. Attach a JPEG/PNG (max 3,
   10 MB each), and submit. Override the lists with `HSE_HAZARD_CATEGORIES` and
   `HSE_SEVERITY_LEVELS` if the team supplies different values.
4. The submitted page confirms the report is **Pending Approval** in the shared
   styling. Reloading/re-submitting the token shows the duplicate-submission warning
   without creating a second report.
5. Log out and sign in as `manal12cf@gmail.com` / `hse-demo-123` to check the Admin
   dashboard and `/projects` pages still render with the same theme.

### 5. Tests

```powershell
mvn test          # uses an in-memory H2 database (test profile), no MySQL needed
```

The merged suite currently has **70 tests, 0 failures**.

## Documentation

| Document | Purpose |
|---|---|
| [`docs/PRD.md`](docs/PRD.md) | Product requirements: functional/non-functional requirements, business rules, flows, acceptance criteria, resolved open questions. |
| [`docs/architecture.md`](docs/architecture.md) | Working architecture baseline: layers, modules, data ownership, technology stack. |
| [`docs/AGENTS.md`](docs/AGENTS.md) | Operating instructions for AI coding agents working in this repository. |
| [`docs/HSE-Architecture-ADRs/`](docs/HSE-Architecture-ADRs/) | Architecture Decision Records (ADR-01…ADR-06, ADR-08). |
| [`docs/plans/`](docs/plans/) | Living implementation plan (`plan.md`) with preserved per-author version history (`plan-v01-Mahad.md` … `plan-v03-Mahad.md`, `plan-v02-Manal.md`, `plan-v04-Eman.md`). |

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
