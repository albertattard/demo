FROM container-registry.oracle.com/java/jdk:25.0.3

WORKDIR /opt/app
COPY ./target/application-monolithic-layered-single-project-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:MinRAMPercentage=75", "-jar", "app.jar"]
