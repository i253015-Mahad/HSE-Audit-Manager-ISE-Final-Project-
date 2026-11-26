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
