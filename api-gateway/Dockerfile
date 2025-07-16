FROM openjdk:17-jre-slim

WORKDIR /app

COPY build/libs/api-gateway-1.0.0.jar app.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]