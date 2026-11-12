FROM container-registry.oracle.com/java/jdk:25.0.3 AS builder
WORKDIR /opt

# Create the small JRE
RUN dnf install -y binutils
RUN jlink \
      --output './jre' \
      --no-header-files \
      --no-man-pages \
      --add-modules java.base,java.desktop,java.instrument,java.management,java.naming,java.sql,java.security.jgss,java.logging,java.compiler,jdk.unsupported,jdk.crypto.ec,jdk.charsets \
      --strip-debug \
      --compress=zip-9

# Extract the FAT Jar into smaller single JARs
COPY ./application/target/application-monolithic-modular-multi-project-*.jar app.jar
RUN java -Djarmode=tools -jar 'app.jar' extract --layers --destination 'extracted'


FROM container-registry.oracle.com/os/oraclelinux:9-slim
WORKDIR /opt/app

COPY --from=builder /opt/jre /opt/jre
COPY --from=builder /opt/extracted/dependencies/ ./
COPY --from=builder /opt/extracted/spring-boot-loader/ ./
COPY --from=builder /opt/extracted/snapshot-dependencies/ ./
COPY --from=builder /opt/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["/opt/jre/bin/java", "-XX:MaxRAMPercentage=75", "-XX:MinRAMPercentage=75", "-jar", "app.jar"]
