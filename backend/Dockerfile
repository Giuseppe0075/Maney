# syntax=docker/dockerfile:1.7

# -------- STAGE 1: BUILD (Maven + cache) --------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml ./
# You don't need to copy .mvn or mvnw anymore!
# RUN chmod +x mvnw  <-- You can remove this too

# Use 'mvn' instead of './mvnw'
RUN mvn -q -B -DskipTests dependency:go-offline

# Sources and build
COPY src ./src
# Use 'mvn' instead of './mvnw'
RUN mvn -q -B -DskipTests clean package

# -------- STAGE 2: RUNTIME --------
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Non-root user setup
RUN useradd -r -u 10001 -g users appuser

RUN mkdir -p /app/tmp && chown -R appuser:users /app/tmp
ENV TMPDIR=/app/tmp

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:+AlwaysActAsServerClassMachine \
 -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 \
 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENV SPRING_PROFILES_ACTIVE=prod

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

USER appuser
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]