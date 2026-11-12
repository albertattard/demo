# Architecture Decision Record: Adoption of OpenTelemetry and Jaeger for Distributed Tracing

| **Status**          | Accepted   |
| ------------------- | ---------- |
| **Date**            | 27.04.2077 |
| **Decision Number** | ADR-009    |

## Context

As the project migrates to a distributed architecture, observability becomes
increasingly important to understand interactions between services and to
diagnose issues effectively. Comprehensive observability typically involves
distributed tracing, logging, and metrics. While full observability is critical
in production, implementing such a setup can be complex. For demonstration and
learning purposes within this workshop, the focus is on providing clear
visibility into how requests flow through the system.

## Decision

OpenTelemetry will be adopted to enable distributed tracing across services,
with Jaeger used to collect and visualise the trace data. To keep the setup
simple, only distributed tracing will be implemented at this stage.

## Rationale

OpenTelemetry is an open-source, standardised framework for collecting
observability data (traces, metrics, logs) across distributed systems. Adopting
OpenTelemetry allows for vendor-neutral instrumentation and future
extensibility. Jaeger is a well-supported and widely-used tracing system that
integrates seamlessly with OpenTelemetry, enabling the team to visualise trace
data with minimal additional configuration. Focusing on tracing provides
immediate value for understanding requests as they traverse multiple services,
supporting both learning and debugging. Although logs and metrics are essential
in production environments, restricting the implementation to tracing simplifies
the setup while still demonstrating core observability concepts.

## Consequences

Implementing OpenTelemetry and Jaeger for distributed tracing increases the
team’s ability to monitor and debug cross-service interactions, identify
bottlenecks, and provide a clearer understanding of system behaviour. The
limited scope keeps the observability infrastructure simple, avoiding the
complexity of managing additional logging and metrics aggregation systems.
However, this reduced setup does not provide a full observability solution and
would need to be expanded in a production context to include logs and metrics
for comprehensive monitoring and alerting.

## Related Decisions

See [ADR-008](./ADR-008-Architecture-Style.md) for the transition to distributed
architecture.
