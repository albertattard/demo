FROM container-registry.oracle.com/java/jdk:25.0.4.1 AS builder

# binutils are needed by the `--strip-debug` command line option.
RUN dnf install -y binutils && dnf clean all

ARG JAVA_MODULES=java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.sql,java.security.jgss
RUN jlink \
      --output "/opt/jre" \
      --no-header-files \
      --no-man-pages \
      --strip-debug \
      --add-modules "${JAVA_MODULES}" \
      --compress=zip-9

FROM container-registry.oracle.com/os/oraclelinux:10-slim
WORKDIR "/opt/app"

COPY --from=builder ["/opt/jre", "/opt/jre"]

ARG JAR_FILE="./target/jlink-multi-architecture-spring-boot-*.jar"
COPY ["${JAR_FILE}", "app.jar"]

EXPOSE 8080

ENTRYPOINT ["/opt/jre/bin/java", "-jar", "app.jar"]
