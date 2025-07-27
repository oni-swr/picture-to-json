# Multi-stage build for Java Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install tesseract and required dependencies
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-eng
RUN apk add --no-cache libc6-compat

# Create upload directory
RUN mkdir -p /tmp/picture-to-json/uploads

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]