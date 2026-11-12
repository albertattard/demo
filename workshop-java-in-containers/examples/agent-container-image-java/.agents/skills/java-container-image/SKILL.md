---
name: java-container-image
description: Use to generate a locally tagged OCI container image and Dockerfile for a Maven or Gradle Java application, using Oracle Java Container Registry base images. Require clarification before guessing the build, entry point, Java version, runtime requirements, registry access, tag, or optimisation strategy.
---

# Java Container Image

Turn a Java source project into a reviewed, locally tagged OCI image. Start from
the project evidence; do not select a runtime technique merely because it exists
in the workshop examples.

## Discover and confirm

1. Treat the path supplied by the user as the project root. Do not treat a
   directory containing only `.java` files as a buildable application.
2. Inspect, without changing files: `pom.xml`, `build.gradle*`, wrappers, lock
   files, `src`, existing container files, application configuration, tests, and
   packaged artefacts. Determine the build tool, Java release, packaging type,
   main class or framework launcher, and a likely listening port.
3. Read existing Dockerfiles and build configuration before proposing a
   replacement. Preserve project conventions unless the user asks to change them.
4. Ask for clarification and stop before editing if any of these remain unknown
   or conflicting:

   - the project is not Maven or Gradle, cannot produce a runnable artefact, or
     has no unambiguous entry point;
   - the required Java release is not established by the build;
   - the application’s port, required runtime arguments, or external runtime
     dependencies affect how it can be run;
   - the desired image tag or whether an actual local build is wanted is
     unspecified;
   - a requested platform-specific or multi-architecture build does not name its
     target platforms;
   - the Oracle Java base-image digest cannot be resolved and approved;
   - pulling Oracle Java images requires acceptance of terms or registry
     authentication that is not available;
   - the user requests smaller images, startup optimisation, diagnostics, or
     multi-architecture output without stating the goal and constraints.

Do not guess credentials, an image digest, production resource limits, or a
registry destination. Do not push an image unless the user explicitly asks.

## Select a packaging strategy

Use the least specialised strategy that satisfies the stated objective.

| Evidence and goal | Strategy |
| --- | --- |
| Verified runnable executable JAR; no special optimisation request | Copy the built JAR into a small Oracle Java JDK runtime image. |
| No usable packaged artefact, or a self-contained image build is explicitly required | Build from source in a multi-stage Dockerfile with the project wrapper, then copy the resulting artefact into the runtime image. |
| Spring Boot executable JAR; rebuild speed matters | Extract Spring Boot layers and copy stable dependency layers before application layers. Verify the project supports its selected extraction command. |
| Image size matters and the module set can be validated | Use `jdeps` against the built artefact, then create a `jlink` runtime. Treat reflection, service loading, JNI, and dynamically loaded code as reasons to seek confirmation or retain a complete JDK. |
| Startup work is demonstrably important and the deployment class path is fixed | Consider AppCDS or a Project Leyden AOT cache only with explicit confirmation. Reuse the exact runtime and ordered class path used during training. Benchmark the result; do not claim a universal gain. |
| In-container JFR, `jcmd`, or Native Memory Tracking is required | Include the required diagnostic modules and configure diagnostics explicitly. Do not silently sacrifice diagnostic tools for image size. |
| Multiple CPU architectures are a release requirement | Ask for exact platforms, builder availability, tag and manifest publishing intent. Use platform-aware builds; do not present a single-platform image as multi-architecture. |

## Create the Dockerfile

Create the Dockerfile in the project’s established location; otherwise use
`containers/Dockerfile`.

Resolve the exact digest for the selected, versioned Oracle Java image with a
registry-capable tool such as `crane digest`. Use authenticated registry access
when required, never expose its credentials, and present the resolved immutable
reference for approval. If it cannot be resolved, ask the user; do not emit a
floating tag.

For an artefact-copy build, create a restrictive `.dockerignore` that admits
only the selected JAR and necessary container files. Do not omit it: the
container engine sends the whole build context before executing `COPY`. Inspect
for local secrets and exclude them without excluding a required build input.

Use a source-in-container build only when it is explicitly required or no
verified artefact exists. In that case, retain the wrapper and all required
source and build inputs, while excluding VCS metadata, build output, secrets,
and generated workshop files.

For the standard artefact-copy strategy:

- use the Oracle Java Container Registry JDK image for the runtime;
- use the Java version established by the project, not a hard-coded release;
- use an `ARG JAR_FILE` with the verified artefact path and copy only that
  artefact into the runtime image;
- use an exec-form `ENTRYPOINT` with the verified launcher and runtime arguments;
- copy application files with ownership `10001:0` and start the process as
  `10001:0`;
- expose a port only when the application documentation or configuration proves
  one. `EXPOSE` documents a port; it does not publish it.

For a source-in-container build, use the Oracle Java JDK image for both build
and runtime, use the project wrapper rather than silently substituting a system
Maven or Gradle version, and separate dependency-resolution inputs from source
copies when the build tool supports it.

Do not bake credentials, tokens, private certificates, or environment-specific
configuration into an image. Prefer runtime configuration supplied by the
deployment environment.

## Build and verify

1. Before a build, state the selected strategy, generated files, exact image
   reference, local tag, and anything that remains unverified.
2. Generate or update only the Dockerfile and `.dockerignore` unless the user
   also authorises application changes.
3. Build only after the user requests an actual local build or supplies a task
   that clearly requires it. First confirm a compatible container engine and
   Oracle Container Registry access. Never print or inspect secret values.
4. Verify the image proportionately: inspect the image configuration, confirm
   the configured non-root user and entry point, and run it when its required
   runtime dependencies and port are known. Report the exact commands and
   observed results.
5. Report limitations honestly. In particular, source inspection cannot prove
   that a `jlink`, CDS, or AOT configuration is correct for all dynamic runtime
   paths.
