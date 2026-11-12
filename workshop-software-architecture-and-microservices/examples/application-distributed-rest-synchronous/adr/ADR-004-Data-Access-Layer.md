# Architecture Decision Record: Adoption of Spring Data JPA over JDBC

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-004    |

## Context

The project requires a mechanism for data persistence and retrieval. Available
options included using Spring Data JPA, which provides a higher level of
abstraction over SQL and JDBC, or directly using JDBC for manual SQL handling.
The database requirements for this workshop are minimal, and the workshop’s
primary focus is on software architecture styles, not database management or
advanced persistence strategies.

## Decision

Spring Data JPA has been chosen as the data access framework for the project,
instead of using low-level JDBC.

## Rationale

Spring Data JPA offers an easy and efficient way to manage data persistence
without the need to write boilerplate code for SQL queries and object-relational
mapping. This allows the participants to focus on the core workshop objectives
rather than on data access implementation details. With simple data models and
basic table structures, Spring Data JPA’s straightforward configuration meets
all current requirements. Using JDBC would introduce unnecessary complexity for
this context, providing limited benefit given the simplicity of the data layer
and the temporary nature of the underlying database.

## Consequences

Adopting Spring Data JPA reduces development effort by eliminating boilerplate
code and enabling rapid implementation of data repositories. This choice allows
the participants to allocate time and attention to architectural experiments,
as intended for the workshop. However, advanced features of JPA are not
leveraged, and the decision may need to be revisited if data requirements become
more complex or if custom SQL and fine-tuned control over queries is required in
the future.

## Related Decisions

Refer to [ADR-001](./ADR-001-Framework.md) for the decision regarding the Spring
Boot Framework and [ADR-002](./ADR-002-Database.md) for the decision regarding
the H2 in-memory database.
