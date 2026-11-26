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
