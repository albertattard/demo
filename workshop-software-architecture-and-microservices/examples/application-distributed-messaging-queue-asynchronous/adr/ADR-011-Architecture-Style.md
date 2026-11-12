# Architecture Decision Record: Migration to Asynchronous Communication Using RabbitMQ

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-011    |

## Context

The distributed application was initially designed around synchronous
communication between services, with the cart service depending directly on the
catalogue service’s availability for real-time data retrieval. This approach
introduced tight coupling and potential reliability issues, as the cart service
could not function if the catalogue service was unavailable. To increase service
independence and resilience, a shift to asynchronous communication is proposed.

## Decision

The architecture will be refactored to use asynchronous communication between
services. RabbitMQ has been chosen as the message broker, replacing synchronous
service calls. The cart service will now interact with other services via
asynchronous messaging, allowing it to function even when the catalogue service
is not running.

## Rationale

Asynchronous communication decouples services by allowing them to interact
through message queues, rather than direct API invocations. This enables
services to remain operational and process messages at their own pace,
regardless of each other’s availability. RabbitMQ was selected over alternatives
like ActiveMQ due to its greater popularity, strong active community,
comprehensive documentation, and proven reliability in production environments.
By breaking the real-time dependency, the cart service can continue handling
requests and processing messages even if the catalogue service is offline or
experiencing delays.

## Consequences

Adopting asynchronous communication with RabbitMQ increases system resiliency
and scalability, allowing services to fail and recover independently. This
refactor reduces immediate cross-service dependencies, improving fault tolerance
and overall availability. However, implementing asynchronous messaging
introduces additional complexity, including message schema management, eventual
consistency, and the need for robust error handling and monitoring of message
queues. Developers must also adapt business logic to account for asynchronous
processing.

## Related Decisions

See [ADR-008](./ADR-008-Architecture-Style.md) for the initial adoption of
synchronous communication in the distributed architecture and
[ADR-009](./ADR-009-Reverse-Proxy.md) for the use of Nginx as a reverse proxy.
