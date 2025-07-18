#!/bin/bash

# Development script for API Gateway

echo "Starting API Gateway in development mode..."

# Create external network if it doesn't exist
docker network create microservices-network 2>/dev/null || true

# Start development environment
docker-compose -f docker-compose.dev.yml up --build

echo "Development environment stopped."
