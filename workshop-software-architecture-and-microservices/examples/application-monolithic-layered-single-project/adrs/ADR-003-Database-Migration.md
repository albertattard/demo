# Architecture Decision Record: Adoption of FlywayDB for Database Migration

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-003    |

## Context

The project requires a simple method to manage database schema changes in a
controlled and repeatable manner. As the project evolves, there may be a need to
apply updates to the database structure without manual intervention or risk of
incompatibility between different development environments. Effective database
version control is essential, even though the H2 in-memory database is used for
simplicity during development and workshops.

## Decision

FlywayDB has been selected as the tool for managing database migrations.

## Rationale

FlywayDB provides an intuitive approach to versioning and automating database
schema changes through migration scripts. It integrates easily with the existing
technology stack and requires minimal configuration, supporting the goal of a
simple development environment. FlywayDB is compatible with H2 and major
production databases, which ensures future scalability. While alternatives such
as Liquibase were considered, FlywayDB’s simplicity and transparent migration
model made it more suitable for the needs of this project.

## Consequences

Adopting FlywayDB enables the team to manage database changes reliably and
consistently across environments, reducing manual overhead and the risk of
configuration drift. However, there are potential security risks to consider:

- Migration scripts are executed with the database user’s privileges, which may
  result in unintended schema or data modifications if not properly managed.
- Scripts must be carefully reviewed to prevent malicious code or errors.
- Credentials should not be stored in scripts or configuration files; secure
  credential management is essential.
- Access to migration tools and scripts must be restricted to authorised
  personnel to avoid unauthorised changes.

While FlywayDB is well-suited to this project, these security considerations
mean the team must follow secure development and deployment practices.

Switching to a different migration tool in the future would also require
migrating existing scripts and adapting project workflows.

## Related Decisions

Refer to [ADR-001](./ADR-001-Framework.md) for the decision regarding the Spring
Boot Framework and [ADR-002](./ADR-002-Database.md) for the decision regarding
the H2 in-memory database.
