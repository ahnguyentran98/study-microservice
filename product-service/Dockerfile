FROM openjdk:11-jre-slim

WORKDIR /app

COPY build/libs/product-service-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]