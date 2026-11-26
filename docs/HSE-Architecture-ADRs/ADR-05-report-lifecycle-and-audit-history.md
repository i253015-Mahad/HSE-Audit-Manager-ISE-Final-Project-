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
