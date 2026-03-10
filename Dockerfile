# ─────────────────────────────────────────────
# Stage 1: BUILD — compile the Spring Boot JAR
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: RUN — lightweight runtime image
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN mkdir -p /app/data

COPY --from=build /app/target/rks-collections-1.0.0.jar app.jar

# ✅ FIXED — copy from build stage, not local filesystem
COPY --from=build /app/src/main/resources/products ./resources/products

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]