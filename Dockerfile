# Stage 1: Build the application
FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# Stage 2: Create a slim runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from the builder stage
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# Application metadata
LABEL maintainer="developer@example.com"
LABEL version="1.0"
LABEL description="Customer Management API"

EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application with proper JVM settings
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]