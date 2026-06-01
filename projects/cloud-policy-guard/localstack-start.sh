#!/bin/bash
# Start LocalStack for local AWS testing

set -e

echo "🚀 Starting LocalStack..."
docker-compose up -d localstack

# Wait for LocalStack to be healthy
echo "⏳ Waiting for LocalStack to be ready..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
  if curl -s http://localhost:4566/_localstack/health > /dev/null 2>&1; then
    echo "✅ LocalStack is ready!"
    break
  fi
  attempt=$((attempt + 1))
  echo "  Attempt $attempt/$max_attempts..."
  sleep 1
done

if [ $attempt -eq $max_attempts ]; then
  echo "❌ LocalStack failed to start within the timeout period"
  exit 1
fi

echo ""
echo "📋 LocalStack Configuration:"
echo "  Endpoint URL: http://localhost:4566"
echo "  Region: us-east-1"
echo "  Access Key: test"
echo "  Secret Key: test"
echo ""
echo "🎯 Example scan command:"
echo "  java -jar target/cloud-policy-guard.jar scan-aws \\"
echo "    --region us-east-1 \\"
echo "    --endpoint-url http://localhost:4566 \\"
echo "    --access-key test \\"
echo "    --secret-key test"
echo ""
