# Architecture Decision Record: Adoption of Layered Monolithic Architecture Style

| **Status**          | Superseded                                 |
| ------------------- | ------------------------------------------ |
| **Date**            | 27.04.2077                                 |
| **Superseded by**   | [ADR-006](./ADR-006-Architecture-Style.md) |
| **Decision Number** | ADR-005                                    |

## Context

This project aims to explore different software architecture styles as part of a
workshop. At the outset, it is necessary to select an initial architectural
style to provide a clear, stable foundation before progressing to more complex
alternatives. The Layered Monolithic Architecture is widely recognised and
understood in the industry, although it may not be suitable for all production
scenarios.

## Decision

We have chosen to start the project with the Layered Monolithic Architecture
style.

## Rationale

The Layered Monolithic Architecture organises the application into clearly
defined layers (such as presentation, business logic, and data access) within a
single deployable unit. This structure is familiar to most developers and has
lower initial complexity, making it an effective entry point for learning and
discussion. Starting with this style enables participants to focus on
fundamental architectural concepts before tackling the challenges of distributed
architectures. Although the layered monolith has limitations—such as
maintainability issues—it is a commonly used starting point and aligns with the
educational goals of this workshop.

## Consequences

Beginning with a layered monolithic approach provides a straightforward, unified
codebase that is easier to understand and modify at the start of the workshop.
This supports rapid development and clear demonstrations of architectural
principles. However, this style can lead to tight coupling, and limited
flexibility as applications grow. These limitations are acknowledged, and the
workshop will later cover migration to more suitable architectures as the
project requirements evolve.

## Related Decisions

[ADR-006](./ADR-006-Architecture-Style.md) superseded this ADR, which defined
the new architecture style.
