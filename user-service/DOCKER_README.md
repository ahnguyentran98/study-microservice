# User Service Docker Setup

This directory contains Docker configuration for running the User Service in isolation.

## Prerequisites

- Docker
- Docker Compose
- Java 11+ (for building the application)

## Quick Start

1. **Build the application** (if not already built):
   ```bash
   ./gradlew build
   ```

2. **Start the services**:
   ```bash
   docker-compose up -d
   ```

3. **Check the logs**:
   ```bash
   docker-compose logs -f user-service
   ```

4. **Test the service**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Services

### user-db
- **Image**: PostgreSQL 13
- **Port**: 5433 (mapped to 5432 inside container)
- **Database**: user_service_db
- **Username**: admin
- **Password**: password

### user-service
- **Port**: 8080
- **Health Check**: http://localhost:8080/actuator/health
- **Profile**: docker

## Useful Commands

### Start services
```bash
docker-compose up -d
```

### Stop services
```bash
docker-compose down
```

### View logs
```bash
docker-compose logs -f user-service
docker-compose logs -f user-db
```

### Rebuild and restart
```bash
docker-compose down
./gradlew build
docker-compose up -d --build
```

### Access database directly
```bash
docker-compose exec user-db psql -U admin -d user_service_db
```

Or connect from host:
```bash
psql -h localhost -p 5433 -U admin -d user_service_db
```

### Clean up (remove volumes)
```bash
docker-compose down -v
```

## Configuration

The service uses the `application-docker.properties` profile when running in Docker, which includes:
- Environment variable-based database configuration
- Optimized logging levels
- Docker-specific JPA settings

## Database Initialization

The database is automatically initialized with the SQL script from `../z-init-db/user_service_db.sql` when the container starts for the first time.

## Health Checks

Both services include health checks:
- **Database**: PostgreSQL ready check
- **Application**: Spring Boot actuator health endpoint

## Troubleshooting

1. **Database connection issues**: Ensure the database is fully started before the application
2. **Port conflicts**: Change the port mapping in docker-compose.yml if needed
3. **Build issues**: Make sure to run `./gradlew build` before starting containers
