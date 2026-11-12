FROM container-registry.oracle.com/java/jdk:25.0.4.1
WORKDIR "/opt/app"

ARG JAR_FILE="./target/compact-object-headers-spring-boot-1.0.0.jar"
COPY ["${JAR_FILE}", "app.jar"]

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
