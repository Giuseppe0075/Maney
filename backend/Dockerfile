# syntax=docker/dockerfile:1.7

# -------- STAGE 1: BUILD (Maven + cache) --------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache delle dipendenze (si invalida solo se cambia il pom)
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw mvnw
RUN ./mvnw -q -B -DskipTests dependency:go-offline

# Sorgenti e build
COPY src ./src
RUN ./mvnw -q -B -DskipTests clean package

# -------- STAGE 2: RUNTIME (JRE slim, non-root) --------
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Utente non-root
RUN useradd -r -u 10001 -g users appuser

# Spring Boot usa /tmp: creiamolo e rendiamolo scrivibile
RUN mkdir -p /app/tmp && chown -R appuser:users /app/tmp
ENV TMPDIR=/app/tmp

# Copia del JAR
COPY --from=build /app/target/*.jar /app/app.jar

# Porta esposta (adegua se diversa)
EXPOSE 8080

# Opzioni JVM sicure per container; sovrascrivibili a runtime
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+AlwaysActAsServerClassMachine \
 -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 \
 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Profilo di default; sovrascrivibile a runtime
ENV SPRING_PROFILES_ACTIVE=prod

# Healthcheck (richiede actuator /actuator/health)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

# Esecuzione come non-root, PID 1 gestito (usa exec form)
USER appuser
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
