# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy POMs first — Docker re-uses this layer if only sources change
COPY pom.xml .
COPY domain/pom.xml           domain/
COPY repository-api/pom.xml   repository-api/
COPY repository-impl/pom.xml  repository-impl/
COPY service-api/pom.xml      service-api/
COPY service-impl/pom.xml     service-impl/
COPY app/pom.xml              app/
RUN mvn dependency:go-offline -B -q

# Copy sources and build the fat JAR
COPY domain/src          domain/src
COPY repository-api/src  repository-api/src
COPY repository-impl/src repository-impl/src
COPY service-api/src     service-api/src
COPY service-impl/src    service-impl/src
COPY app/src             app/src
RUN mvn package -DskipTests -B -q

# ── Stage 2: run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/app/target/app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
