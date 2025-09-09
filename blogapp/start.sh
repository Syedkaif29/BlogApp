#!/bin/bash
# Set default port if PORT is not set
export PORT=${PORT:-8080}
echo "Starting application on port $PORT"
exec java -jar target/blogapp-0.0.1-SNAPSHOT.jar