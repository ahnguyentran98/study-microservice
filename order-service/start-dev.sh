#!/bin/bash

echo "Starting Order Service in development mode..."

docker network create microservices-network 2>/dev/null || true

docker-compose up --build

echo "Development environment stopped."
