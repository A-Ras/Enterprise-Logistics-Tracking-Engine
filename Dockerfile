# ----------------------------------------------------
# Stage 1: Build-Umgebung
# ----------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Maven Wrapper und Konfiguration kopieren (für besseres Docker-Caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Source-Code kopieren und JAR bauen (Tests überspringen wir im Image-Build,
# da diese separat in der CI-Pipeline laufen)
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ----------------------------------------------------
# Stage 2: Schlanke Laufzeit-Umgebung (Production)
# ----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Aus Sicherheitsgründen nicht als 'root' laufen lassen!
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Nur das fertige JAR aus Stage 1 übernehmen
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-jar", "app.jar"]