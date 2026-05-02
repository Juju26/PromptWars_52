# Multi-stage build for user-service
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy parent POM and all module POMs first (layer-cache friendly)
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY user-service/pom.xml user-service/

# Pre-download dependencies (cached layer)
RUN mvn dependency:go-offline -pl common-lib,user-service -am -q

# Copy source
COPY common-lib/src common-lib/src
COPY user-service/src user-service/src

# Build — skip tests in Docker (run in CI pipeline separately)
RUN mvn package -pl common-lib,user-service -am -DskipTests -q

# Stage 2: Runtime — distroless for minimal attack surface
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Non-root user for Cloud Run security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# JVM tuning for containerised / serverless environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseZGC \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.backgroundpreinitializer.ignore=true"

COPY --from=builder /build/user-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
