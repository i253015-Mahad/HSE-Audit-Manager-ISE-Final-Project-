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
