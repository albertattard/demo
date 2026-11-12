# Architecture Decision Records (ADRs)

This directory contains the Architecture Decision Records (ADRs) for this
project. ADRs are concise documents that capture and explain important
architectural decisions made during the lifecycle of the software. By recording
the context, alternatives, and rationale behind each decision, ADRs provide
valuable transparency and help the team and new project members understand how
and why key choices were made.

ADRs are intended to be living documents, updated as the architecture evolves.
For more information on the ADR process and best practices, see
[Michael Nygard’s ADRs](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

## ADR Index

Below is a list of the ADRs for this project:

|     # | ADR                                                       | Decision                                                                                                         |
| ----: | --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
|     1 | [Framework](./ADR-001-Framework.md)                       | Adoption of Spring Boot 3 Framework                                                                              |
|     2 | [Database](./ADR-002-Database.md)                         | Selection of H2 In-Memory Database                                                                               |
|     3 | [Database Migration](./ADR-003-Database-Migration.md)     | Adoption of FlywayDB for Database Migration                                                                      |
|     4 | [Data Access Layer](./ADR-004-Data-Access-Layer.md)       | Adoption of Spring Data JPA over JDBC                                                                            |
| ~~5~~ | ~~[Architecture Style](./ADR-005-Architecture-Style.md)~~ | ~~Adoption of Layered Monolithic Architecture Style~~ (superseded by [ARD-006](./ADR-006-Architecture-Style.md)) |
| ~~6~~ | ~~[Architecture Style](./ADR-006-Architecture-Style.md)~~ | ~~Refactoring to Modular Monolithic Architecture~~ (superseded by [ARD-007](./ADR-007-Architecture-Style.md))    |
|     7 | [Architecture Style](./ADR-007-Architecture-Style.md)     | Splitting Modular Monolith into Multi-Project Structure                                                          |

Each ADR provides the context, decision, rationale, and consequences for an
important architectural choice in this project.
