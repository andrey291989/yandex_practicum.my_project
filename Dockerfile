# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy Maven files first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -DskipTests

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar file from builder stage
COPY --from=builder /app/target/ecommerce-showcase-1.0.0.jar app.jar

# Create a non-root user to run the application
RUN addgroup -S spring && \
    adduser -S spring -G spring

# Change ownership of the application file
RUN chown spring:spring app.jar

# Switch to the non-root user
USER spring

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]