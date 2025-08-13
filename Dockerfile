FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests

# Create a non-root user
RUN groupadd -r uavs && useradd -r -g uavs uavs

# Copy the jar file
RUN cp target/*.jar app.jar

# Change ownership
RUN chown -R uavs:uavs /app

# Switch to non-root user
USER uavs

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/v1/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]