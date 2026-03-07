FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl inotify-tools \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/
COPY src src/

RUN chmod +x ./gradlew

RUN addgroup --system --gid 1000 spring && adduser --system --uid 1000 --ingroup spring spring
RUN mkdir -p /home/spring/.gradle && chown -R spring:spring /home/spring/.gradle
RUN chown -R spring:spring /app

ENV GRADLE_USER_HOME=/home/spring/.gradle

USER spring:spring

RUN ./gradlew --version

EXPOSE 8083

ENTRYPOINT ["./gradlew", "bootRun"]
