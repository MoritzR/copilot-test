#!/bin/bash

# This script helps with local Docker development

case "$1" in
  start)
    echo "🚀 Starting Docker containers..."
    docker-compose up -d
    ;;
  stop)
    echo "🛑 Stopping Docker containers..."
    docker-compose down
    ;;
  logs)
    echo "📄 Viewing logs..."
    docker-compose logs -f
    ;;
  restart)
    echo "🔄 Restarting Docker containers..."
    docker-compose down && docker-compose up -d
    ;;
  clean)
    echo "🧹 Cleaning up Docker resources..."
    docker-compose down -v
    docker rmi $(docker images -q customer-api:latest 2>/dev/null) 2>/dev/null || true
    echo "Volumes and images removed"
    ;;
  build)
    echo "🔨 Building application and Docker image..."
    ./gradlew build -x test && docker-compose build
    ;;
  *)
    echo "Usage: $0 {start|stop|logs|restart|clean|build}"
    exit 1
esac

exit 0