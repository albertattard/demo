FROM container-registry.oracle.com/java/jdk:25.0.4.1@sha256:8d233560cfa91301d256148ecb6f031b3c96a77bf4153dad8bae65b4ecb19bb9 AS builder
WORKDIR /opt/app

ARG JAVA_MODULES=java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.sql,java.security.jgss,jdk.jcmd,jdk.jfr

# binutils are needed by the `--strip-debug` command line option
RUN dnf install -y binutils && dnf clean all
RUN jlink \
      --output '/opt/jre' \
      --generate-cds-archive \
      --no-header-files \
      --no-man-pages \
      --strip-debug \
      --add-modules "${JAVA_MODULES}" \
      --compress=zip-9

ARG JAR_FILE=./target/all-in-one-spring-boot-1.0.0.jar
COPY ${JAR_FILE} app.jar

# The AOT cache is tied to the exact runtime and class path used during
# training. Create the cache with the custom runtime produced above, then reuse
# the same java.args file at container startup.
RUN java -Djarmode=tools -jar 'app.jar' extract --layers --destination 'extracted' \
    && printf '%s\n' '--class-path' "extracted/application/app.jar:$(find 'extracted' -path '*/lib/*.jar' -type f | sort | paste -sd ':' -)" 'demo.Main' > 'java.args' \
    && /opt/jre/bin/java \
        -Dspring.context.exit=onRefresh \
        -XX:AOTCacheOutput='app.aot' \
        @java.args

FROM container-registry.oracle.com/os/oraclelinux:10-slim@sha256:0e951757cac91a9f5f384a46e3f88306df8a8314cde1eaf94e69a406a8c8b646
WORKDIR /opt/app

# Copy the custom Java image
COPY --from=builder /opt/jre /opt/jre

# Use an arbitrary high numeric UID so the container does not run as root.
COPY --chown=10001:0 --from=builder /opt/app/extracted/dependencies/          ./extracted/dependencies/
COPY --chown=10001:0 --from=builder /opt/app/extracted/snapshot-dependencies/ ./extracted/snapshot-dependencies/
COPY --chown=10001:0 --from=builder /opt/app/extracted/application/           ./extracted/application/
COPY --chown=10001:0 --from=builder /opt/app/java.args                        ./java.args
COPY --chown=10001:0 --from=builder /opt/app/app.aot                          ./app.aot
USER 10001:0

# Add the custom runtime tools to PATH so `docker exec ... jcmd` works.
ENV PATH="/opt/jre/bin:${PATH}"
ENV JDK_JAVA_OPTIONS="-XX:MinRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:NativeMemoryTracking=summary -XX:AOTCache=app.aot -XX:AOTMode=on"

EXPOSE 8080

# Reuse java.args so runtime sees the same class path order used to create the
# AOT cache.
ENTRYPOINT ["/opt/jre/bin/java", "@java.args"]
