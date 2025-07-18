#!/bin/bash

# Development script for User Service

echo "Starting User Service in development mode..."

# Create external network if it doesn't exist
docker network create microservices-network 2>/dev/null || true

# Start development environment
docker-compose up --build

echo "Development environment stopped."
