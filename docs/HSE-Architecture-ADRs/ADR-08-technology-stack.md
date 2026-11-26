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
