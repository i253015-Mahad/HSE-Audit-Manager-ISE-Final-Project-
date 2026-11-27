# AGENTS.md
# AI Coding Agent Instructions

## 1. Purpose and Operating Rule

You are an implementation assistant for the HSE Safety Reporting System. Your job is to implement agreed requirements while preserving the project's documented architecture. You are not an independent architecture decision-maker.

Before changing code:

1. Inspect the repository and read this file.
2. Read the relevant parts of `PRD.md`.
3. Read the relevant parts of `architecture.md`.
4. Read all applicable accepted Architecture Decision Records (ADRs).
5. Inspect the existing implementation and follow its established conventions.

Do not invent missing requirements, architectural structures, technologies, interfaces, or business rules. If the available documents do not support a decision, ask the human developer.

## 2. Product Intent

The system is a web-based HSE Safety Reporting System for recording and managing safety reports across projects.

It supports:
- Safety Team Inspectors creating and submitting standardized safety reports.
- Corrective-action follow-up through the report's agreed progress lifecycle.
- Managers reviewing reports for projects assigned to them.
- HSE Engineer/Admin users overseeing all projects and performing authorized administrative actions.

The system is intended to be a relatively small-to-medium website developed by a small student team. Prefer a cohesive, understandable, maintainable implementation over unnecessary operational or architectural complexity.

Do not add functionality outside the agreed scope in `PRD.md` without human approval.

## 3. Source Documents and Decision Authority

Use the project documents for different purposes:

| Document | How the agent must use it |
|---|---|
| `PRD.md` | Source of agreed product intent, functional requirements, non-functional requirements, business rules, and constraints. |
| `architecture.md` | Source of the documented architectural structure, module responsibilities, component boundaries, interactions, and architectural rationale. |
| Accepted ADRs | Authoritative records of specific architectural decisions, their status, rationale, and trade-offs. |
| Project glossary / ubiquitous language | Source of agreed domain terminology. |
| `AGENTS.md` | Operational instructions governing how you work in this repository. |

### Conflict handling

- Do not silently resolve a conflict between `PRD.md`, `architecture.md`, an accepted ADR, or the existing implementation.
- Accepted ADRs govern the architectural decisions they explicitly cover. Check their status and whether they have been superseded.
- `architecture.md` is the documented working architecture baseline and now cites the accepted ADRs (ADR-01 to ADR-06, ADR-08) directly. Where it and an accepted ADR appear inconsistent, treat the ADR as governing and report the conflict.
- If the architecture document and an accepted ADR appear inconsistent, identify the exact conflict and ask the human developer which document should be updated. Do not independently rewrite either decision.
- If a requirement is unclear, ask one focused clarification question before implementing behavior that depends on it.

## 4. Architecture Shape to Preserve

Follow the documented direction in `architecture.md`: a **modular, layered web application**, kept as **one cohesive application / single deployable unit**, unless a relevant accepted ADR explicitly establishes otherwise.

Preserve these logical layers:

### 4.1 Presentation Layer

- Owns the web interface and user interaction.
- Collects user input and displays permitted information, actions, validation feedback, and lifecycle status.
- Sends requests to application-layer use cases.
- Must not be the sole enforcer of authorization or business rules.
- Must not directly own persistence behavior or access infrastructure details.

### 4.2 Application Layer

- Coordinates use cases and workflows.
- Identifies the acting user and coordinates authorization checks.
- Calls the relevant business-module interfaces.
- Coordinates operations that involve more than one business module.
- Defines appropriate transaction/workflow boundaries.
- Returns results suitable for the presentation layer.
- Must not become a second copy of domain rules already owned by business modules.

### 4.3 Business / Domain Modules

Keep business capabilities cohesive and respect the ownership boundaries in Section 5. Each module must expose the capabilities other modules need through explicit interfaces.

### 4.4 Infrastructure and Persistence Layer

- Implements persistence, photograph-storage integration, and other approved technical adapters.
- Keeps database, storage, and configuration details out of presentation code and domain rules.
- Implements the needs expressed through the application's established abstractions and interfaces.
- Must not become the owner of business policy or lifecycle decisions.

Do not add extra layers, independently deployed services, microservices, message brokers, or other distributed-system infrastructure unless an accepted ADR explicitly requires them and the human team approves the implementation.

## 5. Business Module Boundaries and Responsibilities

The following are logical responsibilities documented in `architecture.md`. Follow the repository's established module/package names if they already exist; do not rename or restructure modules merely to match these labels.

### 5.1 User and Access Management

Owns:
- User and role representation.
- Distinction between Safety Team Inspector, Manager, and HSE Engineer/Admin.
- Authorization decisions and access-policy responsibilities.
- Manager project-scoped access and HSE Engineer/Admin cross-project oversight.

Must not own report lifecycle data or project records.

### 5.2 Project Management

Owns:
- Project information and project lifecycle state.
- Assignment of exactly one Manager to each project.
- Project-state information needed by other modules.

Business relationship to preserve:
- Every project has exactly one assigned Manager.
- A Manager may be assigned to / manage multiple projects.

Must not own the contents or lifecycle of individual safety reports.

### 5.3 Safety Report Management

Owns:
- Standardized safety report content.
- Report creation and submission behavior.
- Report-specific validation.
- Safety violations associated with reports.
- Report progress using the agreed `Open` and `Closed` states.
- Report lifecycle behavior, including eligible deleted-report restoration through its owning capability.

Must consult Project Management for authoritative project state and User and Access Management for authorization rather than duplicating those responsibilities.

### 5.4 Report Evidence / Photograph Handling

Owns:
- Photograph evidence handling.
- Validation that report attachments are photographs within the agreed scope.
- Photograph association with the relevant report.

Do not expand this capability into general document, video, or arbitrary-file management unless the requirements and architecture are formally changed.

### 5.5 Administration and Oversight

Owns:
- HSE Engineer/Admin cross-project oversight use cases.
- Administrative workflows such as eligible deleted-report restoration.
- Coordination of privileged operations after authorization.

Must use the owning Project Management and Safety Report Management capabilities. Do not create parallel copies of project/report data or duplicate their business rules.

## 6. Dependency and Interface Rules

These rules apply to all new and modified code:

1. Use the defined module interfaces or application-layer use cases when one capability needs another module's functionality.
2. Do not reach into another module's internal classes, repositories, tables, data structures, or implementation details.
3. Do not create circular dependencies.
4. Do not introduce direct module-to-module dependencies that are not supported by `architecture.md`, an accepted ADR, or an established project interface.
5. Coordinate cross-module workflows in the Application Layer where appropriate; do not make the Presentation Layer orchestrate business modules.
6. Keep interfaces small, explicit, and aligned with the capability being requested.
7. Keep authoritative data with its owning module. Other modules must not create competing authoritative copies.
8. When an existing interface is insufficient, propose an interface change and explain its impact. Do not bypass the boundary as a shortcut.
9. Do not infer a dependency direction that the documents do not specify. If the required direction is unclear, ask for clarification or propose an ADR change for review.

In particular:
- Safety Report Management must obtain project state through the Project Management capability.
- Safety Report Management must use User and Access Management for authorization decisions rather than implementing a second access policy.
- Administration and Oversight must invoke the owning report/project capabilities for administrative workflows.
- Presentation code must call application use cases, not directly access domain persistence or infrastructure.

## 7. Domain Rules That Must Remain Consistent

Use the terminology and rules in `PRD.md`, `architecture.md`, and the glossary.

- A Safety Team Inspector creates and submits a standardized safety report.
- Submission and report progress are separate concepts. Do not treat `Submitted` as an `Open`/`Closed` progress status.
- Report progress uses only the agreed `Open` and `Closed` states unless an approved requirement changes this.
- Multiple safety violations are supported only when the report concerns an incident; an incident is identified by date + location + incident type (PRD FR-05).
- Only a Rejected report may be edited, and only by its owning Inspector, who may then resubmit it; each submission/resubmission saves an immutable version snapshot (PRD FR-43, FR-44).
- Rejection stores a reason with the report and creates an in-system notification for the owning Inspector (PRD FR-42); there is no other user-to-user messaging.
- A Manager's access is limited to projects assigned to that Manager.
- HSE Engineer/Admin oversight is cross-project.
- A Manager may view, but must not change, an individual `Closed` report after its project has been closed.
- An Admin may restore a deleted report only while the relevant project is open and the actor is authorized.
- Corrective action is editable/deletable only while the report is Open, is visible to Manager and Admin only (not Inspectors), and is retained through delete/restore (PRD FR-20, FR-21, FR-47).
- Report evidence is limited to photographs.

Enforce these rules in the appropriate application/business logic. Do not rely on frontend hiding, disabled buttons, or client-side validation as the only enforcement.

## 8. Authorization and Security Instructions

- Enforce authentication and authorization on the server/application side for every protected use case.
- Check both the acting user's role and relevant project scope.
- Do not assume that being a Manager grants access to every project.
- Do not assume that HSE Engineer/Admin project oversight makes the Manager's project-scoped rules applicable to that role.
- Before a Manager modifies an individual report, evaluate the report state and project state as required by the agreed rule.
- Before restoring a deleted report, verify administrative authority and that the project is still open.
- Do not bypass authorization checks for convenience, testing, or frontend behavior.
- Do not expose protected report or project data through unguarded endpoints, queries, or alternate code paths.

Follow the security NFRs in `PRD.md`; do not weaken them.

## 9. Data Ownership and Persistence

Respect the logical data ownership documented in `architecture.md`:

| Data | Logical owner |
|---|---|
| Users, roles, and access policies | User and Access Management |
| Project records, project state, and Manager assignment | Project Management |
| Report content, submission information, violations, and progress | Safety Report Management |
| Photograph evidence and its storage references | Report Evidence / Photograph Handling |
| Administrative oversight workflows | Administration and Oversight, using owning module capabilities |

Operational rules:
- Do not duplicate authoritative data across modules.
- Preserve existing relationships, constraints, and integrity checks.
- Validate lifecycle and authorization rules before persistence.
- Keep persistence-specific code behind the established infrastructure boundaries.
- Do not select or introduce a new database, storage product, or persistence pattern without an accepted ADR and human approval.

## 10. Agreed Constraints and Technology Policy

The following constraints are documented:
- Web-based application.
- Modular, layered application direction (modular monolith, ADR-01).
- One cohesive application / single deployable unit (ADR-01).
- Small-to-medium expected website and small student development team.
- Photograph-only report evidence (ADR-06).
- Technology stack approved 25 November 2026 in ADR-08: Java, Spring Boot, Thymeleaf server-rendered HTML/CSS/JavaScript, Spring Security, MySQL (Spring Data JPA), local filesystem photograph storage, single executable JAR.

Therefore:
- Use only the stack approved in ADR-08. Do not add or replace a framework, database, authentication provider, hosting platform, storage service, external API, or build tool without raising the proposal for human approval and an ADR update.
- Do not invent an external integration because it seems useful.
- Do not introduce independent services or deployment units without explicit architectural approval.

## 11. Coding and Repository Conventions

Before creating or moving files:

1. Inspect the current repository structure.
2. Locate the owning business module and the relevant layer.
3. Find existing interfaces, patterns, validation, error handling, and tests.
4. Reuse established conventions where appropriate.
5. Keep the change within the smallest responsible module/layer boundary.

When coding:
- Use the project's existing naming and formatting conventions.
- Use agreed domain terms consistently.
- Keep functions, classes, and modules focused on their responsibilities.
- Avoid unnecessary abstractions, duplication, and cross-module coupling.
- Keep business rules out of presentation components and infrastructure adapters.
- Keep database/storage-specific details out of domain logic.
- Document non-obvious business rules and important interface contracts.
- Add or update tests for changed behavior, especially authorization, project/report lifecycle, and module interfaces.
- Do not reformat or refactor unrelated code unless necessary and approved.

Do not create a new folder structure or impose a new naming convention when the repository already has an established one.

## 12. Changes Requiring Human Approval

Do not perform these actions independently:

1. Change or bypass an accepted ADR.
2. Change the modular, layered, single-deployable architectural direction.
3. Add, remove, merge, split, or materially redefine business modules or architectural layers.
4. Change module dependency directions or introduce new cross-module dependencies.
5. Bypass an established module interface or access another module's internals.
6. Introduce a framework, language, database, storage technology, authentication provider, external service, or major library not already approved.
7. Change data ownership, persistence strategy, or authoritative data boundaries.
8. Add independently deployed services or distributed infrastructure.
9. Change user roles, authorization scopes, or access policies.
10. Change project assignment rules or project/report lifecycle behavior.
11. Add report progress states or change the meaning of `Submitted`, `Open`, or `Closed`.
12. Expand report evidence beyond photographs.
13. Change audit/history retention, deletion, or restoration rules.
14. Remove agreed requirements or existing functionality.
15. Implement functionality outside the agreed product scope.

When one of these changes appears necessary, stop before making it. Present:
- The reason the current architecture or requirement is insufficient.
- The affected modules, layers, interfaces, and data.
- The proposed change and alternatives.
- Expected benefits, risks, and trade-offs.
- Any PRD, architecture document, or ADR updates required.

Wait for explicit human approval.

## 13. Handling Ambiguity and Missing Information

When a requirement, interface, dependency, or implementation detail is unclear:

1. Check `PRD.md`, `architecture.md`, the glossary, and relevant ADRs.
2. Inspect existing code for established behavior and conventions.
3. Separate confirmed facts from assumptions.
4. Identify the smallest unresolved question that blocks a safe implementation.

If the documents and code do not answer the question:
- Do not invent a business rule or architectural decision.
- Do not silently choose a module owner, dependency direction, technology, or data store.
- Do not change behavior based only on convenience.
- Ask the human developer for clarification.
- If the ambiguity is architectural, recommend documenting the decision in an ADR before implementation.

If an existing implementation conflicts with the documented requirements or architecture, report the conflict rather than silently treating the code as authoritative.

## 14. Required Implementation Workflow

For each feature or change, follow these steps.

### Step 1 — Understand the requirement
- Find the relevant PRD requirement and business rule.
- Identify the expected user, use case, and outcome.
- Confirm the agreed terminology.

### Step 2 — Check the architecture
- Read the relevant section of `architecture.md`.
- Check applicable accepted ADRs.
- Identify the owning business module, layer, interfaces, and data owner.

### Step 3 — Inspect the codebase
- Locate existing implementation and reusable capabilities.
- Check current module boundaries and dependency directions.
- Identify affected tests and other modules that consume the interface.

### Step 4 — Plan
- State which files/modules/layers need changes.
- Explain how module communication will occur.
- Identify authorization, lifecycle, and data-integrity implications.
- Flag any decision requiring human approval before implementation.

### Step 5 — Implement
- Implement through the existing architecture and interfaces.
- Keep changes localized.
- Preserve business rules and ownership boundaries.
- Do not add unapproved dependencies or technologies.

### Step 6 — Verify
- Run relevant tests and project checks available in the repository.
- Test applicable role- and project-scoped authorization.
- Test lifecycle rules and validation.
- Check that module interfaces and dependency boundaries remain intact.
- Report any checks that could not be run; do not claim they passed.

### Step 7 — Report
Summarize:
- What changed and why.
- Requirements addressed.
- Modules, layers, interfaces, and files affected.
- Tests/checks run and their results.
- Any assumptions, unresolved questions, or architectural concerns.

## 15. Token Efficiency, Git Controls, and Planning Workflow

### 15.1 Git & Version Control Operations

1. **Explicit Consent Required:** Never execute `git commit`, `git push`, or alter repository/remote history without asking for and receiving explicit, action-specific approval from the human developer.
2. **Pre-commit Summary:** Present a clear breakdown of uncommitted changes and their rationale before asking for permission to commit.

### 15.2 Context Window & Token Efficiency

1. **Targeted Reading:** Read only files and line ranges directly relevant to the active task. Avoid whole-project reads or unnecessary multi-file dumps.
2. **Search First:** Use targeted text search (`grep`) and pattern matching (`glob`) to locate exact code or documentation sections before reading.
3. **Targeted Edits:** Use surgical string replacement (`edit`) rather than overwriting full files (`write`).
4. **Context Compression:** Periodically compress context after completing major task phases to keep the context window focused and high-signal.
5. **Concise Communication:** Keep conversational output lean, referencing file paths and line numbers instead of reproducing full file contents unnecessarily.

### 15.3 Plan Artifact & History (`docs/plans/`)

1. **Repository-Relative Location:** Store all plan documents within a dedicated `docs/plans/` directory in the repository so all team members have equal access across environments (do not use personal/user-specific paths).
2. **Mandatory `plan.md` Structure:** Every `plan.md` must include the following required sections:
   - Product Objective
   - Implementation Assumptions & Open Issues
   - Vertical Slices
   - Slice Priorities and Order
   - Dependencies
   - Acceptance Criteria (or explicit references to PRD criteria)
   - Current Slice Status
3. **Mandatory History & Versioning:** `plan.md` is a living artefact. Update it whenever sequencing, dependencies, scope, decisions, or implementation understanding changes. Preserve every meaningful version using zero-padded versioning **with the author's name in the filename** in `docs/plans/` (e.g., `plan-v01-Mahad.md`, `plan-v01-Manal.md`, `plan-v01-Eman.md`, etc., alongside the current active `plan.md`). Each team member keeps their own version line so parallel plans never collide; the shared current artefact remains `plan.md`.
4. **Continuous Maintenance of README and Plan:** Actively maintain and refine `README.md` and `plan.md` as requirements and implementation progress.

## 16. Final Rule

Implement the agreed product within the documented architecture. Keep business rules with their owning capabilities, communicate through defined interfaces, preserve the layered structure, and respect accepted ADRs.

**When the documents do not provide enough information to make a safe implementation decision, stop and ask the human developer. Do not invent the architecture.**
