# ==============================================================
# Build Stage: Compile and package the Spring Boot JAR
# ==============================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Pre-fetch dependencies to take advantage of Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code and package executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================
# Runtime Stage: Lightweight container image
# ==============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as non-privileged user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Render supplies the PORT environment variable dynamically
ENV PORT=8080
EXPOSE ${PORT}

# Run with container memory limits and explicit port binding
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]
