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

## Build and Run

Requirements: JDK 21, Maven, MySQL 8 (`hse_reporting` database).

```powershell
# Environment (this machine's install locations)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
$maven = "C:\Users\Manal\tools\apache-maven-3.9.9\bin\mvn"
```

### 1. Start MySQL (portable install)

```powershell
$base = "$env:USERPROFILE\tools\mysql-8.4.13.1-winx64"
Start-Process -FilePath "$base\bin\mysqld.exe" `
  -ArgumentList "--no-defaults","--basedir=$base","--datadir=$env:USERPROFILE\tools\mysql-data","--port=3306" `
  -WindowStyle Hidden
& "$base\bin\mysqladmin.exe" --no-defaults -u root ping   # expect: mysqld is alive
```

One-time database/user setup (already done):

```sql
CREATE DATABASE hse_reporting CHARACTER SET utf8mb4;
CREATE USER 'hse'@'localhost' IDENTIFIED BY 'hse-dev-pass';
GRANT ALL PRIVILEGES ON hse_reporting.* TO 'hse'@'localhost';
```

Connection settings live in `src/main/resources/application.yml` (override with
`HSE_DB_USER` / `HSE_DB_PASSWORD` env vars). On every start Spring runs
`schema.sql` (idempotent DDL) and `data.sql` (demo seeds); Hibernate only
**validates** entities against the schema (`ddl-auto: validate`).

### 2. Run the application

```powershell
& $maven spring-boot:run
```

Open <http://localhost:8080/login>.

### 3. Demo accounts (development seed, password `hse-demo-123`)

| Email | Role | Scope |
|---|---|---|
| `manal12cf@gmail.com` | Admin | All projects (cross-project oversight) |
| `mahadwaqas28@gmail.com` | Manager | Head Office Fit-Out only |
| `i253172@nu.edu.pk` | Inspector | Own reports only (Slice 2+) |
| `extra.manager@hse.local` | Manager | Warehouse Extension only |
| `extra.inspector@hse.local` | Inspector | — |

### 4. Tests

```powershell
& $maven test          # uses an in-memory H2 database (test profile), no MySQL needed
```

## Documentation

| Document | Purpose |
|---|---|
| [`docs/PRD.md`](docs/PRD.md) | Product requirements: functional/non-functional requirements, business rules, flows, acceptance criteria, resolved open questions. |
| [`docs/architecture.md`](docs/architecture.md) | Working architecture baseline: layers, modules, data ownership, technology stack. |
| [`docs/AGENTS.md`](docs/AGENTS.md) | Operating instructions for AI coding agents working in this repository. |
| [`docs/HSE-Architecture-ADRs/`](docs/HSE-Architecture-ADRs/) | Architecture Decision Records (ADR-01…ADR-06, ADR-08). |
| [`docs/plans/`](docs/plans/) | Living implementation plan (`plan.md`) with preserved version history (`plan-v01.md`, `plan-v02.md`, …). |

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
