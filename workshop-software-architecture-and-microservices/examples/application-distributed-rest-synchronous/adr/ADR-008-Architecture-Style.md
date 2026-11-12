# Architecture Decision Record: Migration to Distributed Architecture with Synchronous Communication

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-008    |

## Context

The project has evolved from a layered monolithic structure to a modular
monolith and, subsequently, to multiple independent projects for each module. To
further support scalability, maintainability, and autonomous team development,
the decision has been made to migrate from a monolithic to a distributed
architecture. In this new model, each major business function, such as the cart
and catalogue, will be deployed as an independent service. Effective
inter-service communication is required to coordinate functionality between
these components.

## Decision

We will migrate to a distributed architecture style based on synchronous
communication. Specifically, the cart service will retrieve required information
by invoking API endpoints exposed by the catalogue service.

## Rationale

Synchronous communication using HTTP-based APIs is straightforward to implement
and aligns with the skills of the current development team. This approach allows
services to interact in real time, providing immediate data consistency between
components. Selecting this style enables the cart service to fetch up-to-date
product or inventory information directly from the catalogue service, ensuring
business processes remain synchronised and reducing the risk of data staleness.
This pattern simplifies initial monitoring and debugging, and supports the
project’s goal of illustrating distributed architectures within the workshop
context.

## Consequences

Adopting a distributed architecture with synchronous interactions brings several
trade-offs:

- **Benefits:**
  - Enables each service to scale, evolve, and be deployed independently.
  - Increases team autonomy and allows technology stack flexibility for each
    service.
  - Establishes clear module boundaries aligned with business domains.

- **Drawbacks:**
  - Introduces network latency and potential reliability issues, as the cart
    service relies directly on the availability of the catalogue service.
  - Requires careful attention to error handling, timeouts, and service
    discovery.
  - Increases operational overhead related to deploying, monitoring, and
    maintaining multiple independent services.
  - Synchronous communication can reduce overall system resilience in the face
    of partial service failures.

Despite these challenges, this approach provides an effective stepping stone
toward more advanced distributed architectures, such as those involving
asynchronous messaging or event-driven interactions, should they become
necessary in the future.

## Related Decisions

See [ADR-007](./ADR-007-Architecture-Style.md) for the reorganisation into
multiple independent projects and [ADR-006](./ADR-006-Architecture-Style.md) for
the modular monolithic architecture.
