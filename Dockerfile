# ─────────────────────────────────────────────
# Stage 1: BUILD — compile the Spring Boot JAR
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first (layer cache — faster rebuilds)
COPY pom.xml .

# Download all dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR, skip tests for faster deploy
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: RUN — lightweight runtime image
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create directory for H2 database persistence
RUN mkdir -p /app/data

# Copy the built JAR from Stage 1
COPY --from=build /app/target/rks-collections-1.0.0.jar app.jar

# Copy product images into the container
COPY src/main/resources/products ./resources/products

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
