FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY beacon-service/pom.xml beacon-service/
RUN mvn dependency:go-offline -pl common-lib,beacon-service -am -q
COPY common-lib/src common-lib/src
COPY beacon-service/src beacon-service/src
RUN mvn package -pl common-lib,beacon-service -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseZGC -Djava.security.egd=file:/dev/./urandom"
COPY --from=builder /build/beacon-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
