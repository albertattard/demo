# Architecture Decision Record: Refactoring to Modular Monolithic Architecture

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-006    |

## Context

The project initially adopted a layered monolithic architecture as a simple and
common starting point for illustrating architectural styles. As the next step in
the workshop, the goal is to demonstrate alternative approaches to organising
application code. The refactoring is intended as an educational exercise and
does not introduce new business functionality.

## Decision

The architecture will be refactored from a layered monolithic structure to a
modular monolithic architecture.

## Rationale

A modular monolithic architecture decomposes the application into distinct,
self-contained modules within a single deployable artefact. This approach
enhances code organisation by grouping related functionality into modules with
well-defined interfaces and boundaries, improving maintainability and
testability over time. It also facilitates clearer separation of concerns and
can help identify potential domains for future decomposition, should the
application later evolve into a distributed or microservices-based architecture.
This refactor aligns with the workshop’s learning objectives by showcasing a
practical alternative to traditional layered structures.

## Consequences

The refactoring is expected to improve codebase clarity and modularity, making
it easier for developers to reason about the system’s components and their
interactions. There is a short-term overhead associated with reorganising the
code and updating project documentation. No new business functionality will be
added, so there should be minimal impact on users. This modular structure also
lays the groundwork for possible future transitions to more distributed
architectural styles.

## Related Decisions

Supersedes [ADR-005](./ADR-005-Architecture-Style.md) which defined the initial
architecture style.
