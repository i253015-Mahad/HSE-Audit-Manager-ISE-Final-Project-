# HSE Reporting System — Architecture Decision Records

This document collects the accepted architectural decisions. ADR-07 was intentionally skipped and is not included. ADR-08 was added on 25 November 2026.

# ADR-01: Overall Architectural Style

- **Status:** Accepted

## Context

The HSE Reporting System supports multiple projects and several user responsibilities. Each project has one assigned Manager, while the HSE Engineer/Admin oversees all projects. The system supports safety-report creation and submission, corrective actions, report closure, administration, and audit history.

The architecture must provide clear separation of responsibilities and support maintainability without introducing infrastructure or operational complexity that is disproportionate to a small-to-medium website developed by a small student team.

## Alternatives Considered

1. **Monolithic architecture:** Implement the system as one application without an explicit modular structure.
2. **Modular monolithic architecture:** Implement one deployable application divided into modules with defined responsibilities and boundaries.
3. **Microservices architecture:** Implement functionality as independently deployable services that communicate over service interfaces.

## Decision

Adopt a **modular monolithic architecture**.

The system will be delivered as a single application and deployment unit, internally divided into well-defined modules. Modules will have clear responsibilities and communicate through defined interfaces.

## Rationale

A modular monolith provides useful separation of concerns while keeping development, deployment, testing, and maintenance manageable for a small student team. Microservices would introduce additional deployment, communication, and operational concerns that are not justified by the current system scope.

## Trade-offs / Consequences

- The application remains a single deployable unit; modules are not independently deployed.
- Module boundaries and interfaces must be respected to avoid tight coupling.
- The approach is simpler to operate than a microservices architecture.
- Future extraction of a module into a separate service may be possible if a genuine need arises, but is not part of the current decision.

---

# ADR-02: Internal Module Organization

- **Status:** Accepted

## Context

ADR-01 establishes a modular monolithic architecture. The system therefore needs an internal organization that keeps business responsibilities clear without creating unnecessary layers, folders, abstractions, or boilerplate.

The project is being developed by a small student team as a relatively small-to-medium website. The organization should support collaboration and maintainability while remaining proportionate to the project's size.

## Alternatives Considered

1. **Technical layer-based organization:** Organize the application primarily into shared technical layers such as controllers, services, repositories, and models.
2. **Business feature-based organization:** Organize the application by business functionality, with implementation details grouped inside each feature.
3. **Hybrid organization:** Organize primarily by business module, with technical responsibilities separated within each module where useful.

## Decision

Adopt a **lightweight hybrid organization**.

The application will be organized around business modules. Within each module, separate request-handling, business-logic, and data/model responsibilities where appropriate.

A repository layer will be introduced only where database-access complexity or a clear design need justifies it. Shared functionality will be placed in suitable shared components rather than duplicated across modules.

## Rationale

This approach preserves business-focused boundaries while keeping technical responsibilities understandable. A lightweight implementation avoids imposing a full set of layers on every module when those layers would add little value for this project's scale.

## Trade-offs / Consequences

- Business functionality remains easier to locate and assign among team members.
- Technical responsibilities can be separated without requiring every module to have identical folder structures.
- Some modules may be simpler than others and may not need a repository abstraction.
- The team must keep module responsibilities clear and avoid unnecessary cross-module dependencies.
- The structure favors maintainability without overengineering.

---

# ADR-03: Authentication and Authorization Architecture

- **Status:** Accepted

## Context

The system has distinct roles and project-level access rules. A Manager is assigned to each project, and a Manager may manage multiple projects. The HSE Engineer/Admin oversees all projects. Safety Team users create and submit safety reports.

Authorization must therefore consider both what a user's role is permitted to do and which project data that user is allowed to access.

## Alternatives Considered

1. **Role-Based Access Control (RBAC) only:** Determine permissions from a user's assigned system role, without considering project assignment as an additional access boundary.
2. **Role-based plus project-based authorization:** Determine permissions from the user's role and restrict project-specific access according to project assignment and the user's oversight responsibilities.
3. **Custom per-user permissions:** Assign and manage individual permissions for each user independently of predefined roles.

## Decision

Adopt **role-based authorization combined with project-based access restrictions**.

- Each user has an assigned system role.
- Role permissions determine which operations the user may perform.
- Managers may access and manage projects assigned to them, and may manage multiple assigned projects.
- The HSE Engineer/Admin has oversight across all projects.
- Protected operations and data access must be checked against the user's role and applicable project scope.

The exact permission matrix remains governed by the agreed functional requirements and business rules.

## Rationale

Role-based authorization reflects the system's established roles, while project-based restrictions enforce the rule that Managers operate within their assigned projects. This is more appropriate than role-only checks because users with the same role may be assigned to different projects.

Custom per-user permissions would add administration and implementation complexity without a demonstrated requirement.

## Trade-offs / Consequences

- Authorization checks must account for both role and project assignment.
- Project assignment data must be maintained accurately.
- Access-control logic should be applied consistently to relevant operations and data queries.
- The approach is more precise than role-only authorization but requires careful testing of project boundaries.

---

# ADR-04: Database Architecture

- **Status:** Accepted

## Context

The HSE Reporting System manages structured and related business entities, including users, roles, projects, reports, safety violations, corrective actions, and audit history.

The system must preserve relationships and data consistency across these entities.

## Alternatives Considered

1. **Relational database (SQL):** Store structured data in related tables and use relational constraints and transactions.
2. **NoSQL database:** Store data in flexible document or other non-relational structures.
3. **Hybrid database:** Use SQL for structured business data and a separate NoSQL database for selected data.

## Decision

Use a **relational database (SQL)** as the system's database.

The data model will use structured tables and relationships. Foreign keys, appropriate database constraints, and transactions will be used where needed to maintain data integrity.

## Rationale

The system's core entities have clear relationships and consistency requirements. A relational database is a natural fit for these structured business records and is manageable for the project's scale.

A second database technology is not justified by the currently established requirements.

## Trade-offs / Consequences

- The team must design and maintain a relational schema.
- Relationships and integrity constraints can be enforced by the database.
- Transactions can help keep related changes consistent.
- Schema changes require deliberate migration or update practices.
- The decision does not prescribe a particular SQL database product.

---

# ADR-05: Report Lifecycle and Audit History Architecture

- **Status:** Accepted

## Context

The report lifecycle has defined business rules. Reports can be Open or Closed. Deleted reports can be restored by an Admin while the project is open. After project closure, a Manager may view but may not change individual Closed reports. Audit history must be retained for three months after project completion and then removed.

The architecture must make the current report state straightforward to retrieve while retaining a record of relevant changes and actions.

## Alternatives Considered

1. **Current state with separate audit history:** Store the latest report state in the report record and record relevant changes in a separate audit-history store/table.
2. **Event sourcing:** Store state changes as events and reconstruct the current state from the event stream.
3. **Current state only:** Store the latest report state without a separate audit-history mechanism.

## Decision

Use **current report state with separate audit history**.

- The Reports data stores each report's current state.
- A separate Audit History data store/table records relevant actions and changes.
- Audit entries will identify the acting user, action, timestamp, and relevant change details.
- Report deletion, restoration, closure, and post-closure access must follow the agreed business rules.
- Audit history will be retained for three months after project completion and then removed.

## Rationale

This approach supports the required auditability without the additional implementation and operational complexity of event sourcing. It also allows the application to retrieve the current report state directly.

## Trade-offs / Consequences

- Current state and audit history are stored separately and must be kept consistent when an audited action occurs.
- The team must define which actions and changes are recorded.
- Audit-history retention and deletion must follow the three-month rule.
- The application does not reconstruct report state by replaying events.
- The decision does not prescribe a particular audit-table schema or event payload format.

---

# ADR-06: Photograph Storage

- **Status:** Accepted

## Context

The HSE Reporting System will support photographs associated with safety reports. The agreed scope is limited to photographs; uploading supporting documents or other file types is not included.

Photograph content should not be stored directly in the relational database. The database still needs to retain the association between a photograph and its report, together with relevant metadata and a reference to the stored file.

## Alternatives Considered

1. **Store file content in the database:** Store photograph binary data directly in SQL database records.
2. **Separate file storage with database references:** Store photograph files outside the SQL database and store their references and relevant metadata in the database.
3. **No attachment support:** Do not support file uploads.

## Decision

Use **separate file storage with database references** for photographs.

- Photograph files will be stored outside the relational database.
- The database will store references to the photographs and relevant metadata.
- Photographs will be associated with their respective safety reports.
- Access to photographs must follow the authorization rules applicable to their associated reports.
- The supported attachment type is photographs only.

The specific file-storage technology or provider is not selected by this ADR.

## Rationale

Separating photograph content from structured business data keeps the relational database focused on application records and avoids unnecessarily increasing its size with binary files. Database references preserve the relationship between photographs and reports.

## Trade-offs / Consequences

- The application must manage both database records and stored files.
- The team must ensure that photograph references remain valid and that file operations are handled safely.
- Photograph access must enforce the same project and role-based authorization boundaries as report access.
- Backups and deployment arrangements must account for both the database and photograph storage.
- Supporting documents and other file types remain outside the agreed scope.

---

# ADR-08: Technology Stack

- **Status:** Accepted

## Context

The HSE Safety Reporting System is a web-based application to be built by a small student team using HTML, CSS, and Java. Earlier ADRs fixed the architectural categories — modular monolith (ADR-01), business-module-first organization (ADR-02), role-plus-project authorization (ADR-03), relational database (ADR-04), current-state plus audit history (ADR-05), and separate photograph file storage (ADR-06) — but did not name concrete products. The PRD (CON-03) requires explicit human approval before any stack is adopted.

The team confirmed the stack on 25 November 2026.

## Alternatives Considered

1. **Spring Boot with REST API + static HTML/CSS/JS frontend:** Separate frontend and backend pieces; requires securing every API endpoint individually and running two pieces.
2. **Spring Boot with Thymeleaf server-rendered templates (Option A):** Java renders HTML pages server-side; HTML/CSS/JavaScript live inside the same project; one deployable unit.
3. **Plain Java Servlets/JSP without Spring:** Fewer dependencies but no integrated security, data-access, or validation framework.

## Decision

Adopt **Spring Boot + Thymeleaf + MySQL**, packaged as a single executable JAR (Option A):

- **Language:** Java.
- **Web framework:** Spring Boot (single deployable application, per ADR-01).
- **Templating:** Thymeleaf server-rendered HTML with the team's CSS and JavaScript — no separate SPA and no separate API deployment.
- **Security:** Spring Security for company email + password login (NFR-01) and role-plus-project-scope authorization (ADR-03), including server-side enforcement, XSS escaping, and CSRF protection.
- **Database:** MySQL with Spring Data JPA/Hibernate (relational, per ADR-04).
- **Photograph storage:** Local filesystem directory; the database stores only references and metadata (ADR-06).
- **Build:** Maven or Gradle producing one executable JAR.

## Rationale

- Option A keeps authorization checks on the server in one place, reducing the risk of an unguarded API endpoint leaking data, which satisfies ASR-01 and AGENTS.md §8.
- Thymeleaf lets the team keep ordinary HTML/CSS while Spring Security provides XSS/CSRF protection automatically.
- One JAR matches the modular-monolith decision (ADR-01) and the small-team maintainability goal (ASR-09).
- MySQL is the course-standard relational database and satisfies ADR-04 without prescribing a product elsewhere.
- A separate REST API (alternative 1) would add JavaScript complexity and a second surface to secure, with no benefit at this project scale.

## Trade-offs / Consequences

- Pages reload on navigation (no single-page-app feel); acceptable for this scale.
- Server-side rendering means richer client-side interactivity requires explicit Thymeleaf + JavaScript work.
- Local filesystem photo storage ties backups to the application host; the DB must keep references consistent with files on disk.
- The stack is now fixed; changing any component requires a human-approved ADR update (AGENTS.md §10, §12).

---

