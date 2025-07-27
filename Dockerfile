# Multi-stage build for Java Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage  
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install tesseract, OpenCV dependencies, and other required packages
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    libtesseract-dev \
    libopencv-dev \
    libopencv-contrib-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set Tesseract data path environment variable
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/4.00/tessdata/

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