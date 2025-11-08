# Multi-stage Dockerfile for the Spring Boot app
# Build stage uses a Maven image with JDK 21 (adjust if your project requires a different JDK)
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copy only the files needed for dependency resolution first to leverage Docker cache
COPY pom.xml ./
COPY src ./src
# Build the fat jar
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the jar produced by the build stage (pick the first jar in target/)
COPY --from=build /workspace/target/*.jar /app/app.jar
# Render provides the port in the $PORT env var; the app will read it from application.properties
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
