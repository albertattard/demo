FROM container-registry.oracle.com/java/jdk:25.0.4.1
WORKDIR "/opt/app"

ARG JAR_FILE="./target/application-lifecycle-spring-boot-*.jar"
COPY ["${JAR_FILE}", "app.jar"]

EXPOSE 8080

# The Oracle JDK image includes curl. Docker reports this container as healthy
# when Spring Boot says it is ready to receive traffic.
HEALTHCHECK --interval=5s --timeout=3s --start-period=30s --retries=3 \
    CMD curl --fail --silent --output "/dev/null" "http://127.0.0.1:8080/actuator/health/readiness" || exit 1

# Exec form keeps Java as PID 1 so docker stop delivers SIGTERM to the JVM.
ENTRYPOINT ["java", "-jar", "app.jar"]
