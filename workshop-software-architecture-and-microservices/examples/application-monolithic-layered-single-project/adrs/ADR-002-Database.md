# Architecture Decision Record: Selection of H2 In-Memory Database

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-002    |

## Context

For this project, a database was required to persist application data during
development and workshop exercises. The primary focus of the project is to
explore software architecture styles, rather than database management or
configuration. Simplicity and ease of setup were prioritised to avoid
unnecessary complexity.

## Decision

The H2 in-memory database has been selected for use in this project.

## Rationale

H2 is a lightweight, fast, and open-source in-memory database that can be
integrated with minimal configuration. Its simplicity allows the team to focus
on the core objectives of the workshop, without the need to install or maintain
a separate database server. Using H2 enables rapid development and easy reset of
data between sessions, supporting repeatable workshop exercises. Alternatives
such as MySQL or Oracle Database were considered, but were deemed unnecessary
given the project’s scope and objectives.

## Consequences

The use of H2 facilitates a streamlined development process and reduces setup
overhead. However, it is not suitable for production use due to limitations in
scalability, durability, and concurrent access. Should the project evolve beyond
the current scope, a migration to a more robust, production-grade database may
be required.

## Related Decisions

None at this time.
