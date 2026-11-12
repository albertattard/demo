FROM container-registry.oracle.com/java/jdk:25.0.4.1 AS builder
WORKDIR "/opt/app"

ARG JAR_FILE="./target/leyden-aot-cache-spring-boot-*.jar"
COPY ["${JAR_FILE}", "app.jar"]

# The AOT cache is tied to the exact class path used during training. Even a
# different dependency JAR order invalidates the cache, so write the class path
# to java.args and use that same argument file for cache creation and startup.
RUN mkdir "extracted" \
    && (cd "extracted" && jar -xf "../app.jar") \
    && jar --create --file "application.jar" -C "extracted/BOOT-INF/classes" "." \
    && printf '%s\n' "--class-path" "application.jar:$(find "extracted/BOOT-INF/lib" -type f -name '*.jar' | sort | paste -sd ":" -)" "demo.Main" > "java.args" \
    && java \
        -Dspring.context.exit=onRefresh \
        -XX:AOTCacheOutput=app.aot \
        @java.args

FROM container-registry.oracle.com/java/jdk:25.0.4.1
WORKDIR "/opt/app"

COPY --from=builder ["/opt/app/extracted/BOOT-INF/lib", "./extracted/BOOT-INF/lib"]
COPY --from=builder ["/opt/app/application.jar", "./application.jar"]
COPY --from=builder ["/opt/app/java.args", "./java.args"]
COPY --from=builder ["/opt/app/app.aot", "./app.aot"]

ENV JDK_JAVA_OPTIONS="-XX:AOTCache=app.aot -XX:AOTMode=on"

EXPOSE 8080

# Reuse java.args so runtime sees the same class path order used to create the
# AOT cache.
ENTRYPOINT ["java", "@java.args"]
