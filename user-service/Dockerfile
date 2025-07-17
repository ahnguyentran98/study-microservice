FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy the jar file
COPY build/libs/user-service-1.0.0.jar app.jar

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown spring:spring app.jar
USER spring:spring

EXPOSE 8080

# Set JVM options for containerized environment and specify the Spring profile
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=docker", \
    "-Xmx512m", \
    "-Xms256m", \
    "-jar", \
    "app.jar"]