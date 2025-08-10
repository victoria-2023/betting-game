# Multi-stage build for optimization
FROM eclipse-temurin:17-jdk-alpine AS build

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Create non-root user (Alpine Linux uses addgroup and adduser)
RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/betting-game-backend-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE ${PORT:-8080}

# Run the application with dynamic port
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
