# Architecture Decision Record: Adoption of Nginx as Reverse Proxy

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-009    |

## Context

As the project transitions to a distributed architecture, there is a need to
efficiently route client requests to the appropriate backend service
(component), such as the cart and catalogue services. A reverse proxy is
required to centralise and manage incoming traffic, provide routing, and
potentially enable additional features such as SSL termination, load balancing,
and request filtering. Selecting a widely adopted, robust solution helps ensure
maintainability and community support.

## Decision

Nginx has been selected as the reverse proxy for routing external traffic to the
corresponding backend services.

## Rationale

Nginx is one of the most popular and proven reverse proxy solutions available,
widely used and well maintained by an active community. It is highly performant,
lightweight, and easy to configure for common use cases including routing, load
balancing, and SSL termination. Nginx’s flexibility supports a wide variety of
deployment scenarios, and many developers and operators are already familiar
with its configuration and maintenance. Alternative solutions, such as Apache
HTTP Server or HAProxy, were considered but Nginx’s reputation, ease of use, and
extensive documentation made it the preferred option for this project.

## Consequences

Adopting Nginx as the reverse proxy provides the project with a robust, widely
supported, and performant solution for routing and managing incoming client
requests. It simplifies initial setup and future enhancements (such as adding
SSL support or implementing load balancing) and leverages a well-understood
technology in the industry. Teams must ensure that Nginx is configured and
updated securely, as misconfiguration can expose the system to security risks.
Future architectural changes—such as migrating to a service mesh or using a more
advanced API gateway—can also build upon this initial adoption.

## Related Decisions

See [ADR-008](./ADR-008-Architecture-Style.md) for the migration to a
distributed architecture with synchronous communication.
