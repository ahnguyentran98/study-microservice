FROM gradle:8.14.3-jdk17-jammy AS builder

WORKDIR /app

# Copy build files first for layer caching
COPY build.gradle .
COPY settings.gradle .
# Copy source code
COPY src src

# Build the application
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install curl for health checks
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
