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
