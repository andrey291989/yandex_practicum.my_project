# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM openjdk:21-jdk-slim AS builder

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Download dependencies and build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM openjdk:21-jre-slim

# Set working directory
WORKDIR /app

# Copy the jar file from builder stage
COPY --from=builder /app/target/ecommerce-showcase-1.0.0.jar app.jar

# Create a non-root user to run the application
RUN addgroup --system spring && \
    adduser --system spring --ingroup spring

# Change ownership of the application file
RUN chown spring:spring app.jar

# Switch to the non-root user
USER spring

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]