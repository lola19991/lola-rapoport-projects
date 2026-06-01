#!/bin/bash
# Stop LocalStack

echo "🛑 Stopping LocalStack..."
docker-compose down -v

echo "✅ LocalStack stopped and cleaned up"
