FROM container-registry.oracle.com/java/jdk:25.0.4.1
WORKDIR "/opt/app"

# The application user cannot write here unless the deployment mounts
# writable storage with suitable ownership and permissions.
RUN mkdir "/diagnostics" && chmod 0755 "/diagnostics"

# Keep application files owned by root and readable by the application user.
ARG JAR_FILE="./target/container-runtime-hardening-spring-boot-*.jar"
COPY --chown=0:0 --chmod=0444 ["${JAR_FILE}", "app.jar"]

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
