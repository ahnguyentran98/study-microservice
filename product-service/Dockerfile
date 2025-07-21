FROM eclipse-temurin:17-jdk-alpine

# Install curl for health checks and inotify-tools for file watching
RUN apk add --no-cache curl inotify-tools

WORKDIR /app

# Copy Gradle files first (for better caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src/

# Create non-root user for security (with specific UID to match docker-compose user)
RUN addgroup --system --gid 1000 spring && adduser --system --uid 1000 --ingroup spring spring

# Create gradle cache directory and set permissions
RUN mkdir -p /home/spring/.gradle && chown -R spring:spring /home/spring/.gradle
RUN chown -R spring:spring /app

# Pre-download Gradle distribution to avoid downloading on every container start
RUN ./gradlew --version

USER spring:spring

EXPOSE 8082

# Set JVM options for containerized environment and use bootRun for hot reload
ENTRYPOINT ["./gradlew", "bootRun", \
    "--args=--spring.profiles.active=docker \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.devtools.restart.enabled=true"]
