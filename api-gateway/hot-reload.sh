#!/bin/bash

# Hot reload script for development
# This script watches for file changes and triggers a rebuild when needed

echo "Starting API Gateway with hot reload support..."

# Function to build and restart
rebuild_app() {
    echo "File changes detected, rebuilding..."
    ./gradlew compileJava processResources
    if [ $? -eq 0 ]; then
        echo "Build successful - Spring DevTools will restart the application"
    else
        echo "Build failed - please check the logs"
    fi
}

# Start the application in background
./gradlew bootRun --args="--spring.profiles.active=docker -Djava.security.egd=file:/dev/./urandom -Dspring.devtools.restart.enabled=true" &
APP_PID=$!

# Watch for file changes (using inotifywait if available, otherwise use polling)
if command -v inotifywait >/dev/null 2>&1; then
    echo "Using inotifywait for file watching..."
    while true; do
        inotifywait -r -e modify,create,delete src/ && rebuild_app
    done
else
    echo "Using polling for file watching..."
    LAST_MODIFIED=$(find src -name "*.java" -o -name "*.properties" -o -name "*.yml" -o -name "*.xml" | xargs stat -c %Y | sort -n | tail -1)
    
    while true; do
        sleep 2
        CURRENT_MODIFIED=$(find src -name "*.java" -o -name "*.properties" -o -name "*.yml" -o -name "*.xml" | xargs stat -c %Y | sort -n | tail -1)
        
        if [ "$CURRENT_MODIFIED" != "$LAST_MODIFIED" ]; then
            LAST_MODIFIED=$CURRENT_MODIFIED
            rebuild_app
        fi
    done
fi

# Clean up
kill $APP_PID 2>/dev/null
