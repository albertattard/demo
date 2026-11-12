# Architecture Decision Record: Splitting Modular Monolith into Multi-Project Structure

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-007    |

## Context

The project evolved from a layered monolithic architecture to a modular
monolith, with modules organised within a single codebase. As teams grow and the
application develops, coordination and parallel development become more complex.
Additionally, preparing for a possible future transition to a distributed
architecture is a strategic goal. Although all modules are still deployed
together as a single application, increasing independence and autonomy in
development is required.

## Decision

The modular monolith will be refactored into separate projects, each with its
own git repository. Each module will become an individual project, enabling
separate teams to work independently, while all modules will continue to be
built and deployed together as a single application.

## Rationale

By splitting the modular monolith into multiple projects, each managed in an
independent repository, we gain several benefits:

- **Team Independence:** Different teams can manage, develop, and maintain their
  assigned modules with reduced coordination overhead and fewer merge conflicts.
- **Codebase Clarity:** Each module’s repository is focused, simplifying
  dependency management and code reviews.
- **Preparation for Distributed Architecture:** Structuring modules as
  independent projects lays a clear foundation for migrating to a distributed or
  microservices-based architecture in the future, as each module is already
  isolated at the project level.

While the deployment model remains monolithic, this structure enables a gradual,
low-risk path to further decomposition if and when requirements change.

## Consequences

This approach increases the autonomy of development teams and improves
maintainability by isolating codebases. Build and integration complexity will
increase, requiring a robust continuous integration system to assemble and test
the full application from its constituent modules. Changes affecting multiple
modules may require coordination and clear versioning strategies. There is no
immediate impact on end users, as the application is still deployed as a single
unit. The new structure, however, positions the project for smoother migration
to a distributed architecture if so desired in the future.

## Related Decisions

Supersedes [ADR-006](./ADR-006-Architecture-Style.md) which defined the move to
a modular monolithic architecture. This ADR builds directly upon that decision.
