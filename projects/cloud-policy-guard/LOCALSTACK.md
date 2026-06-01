# LocalStack Setup Guide

This guide explains how to use **LocalStack** to test the `scan-aws` command without needing real AWS credentials or infrastructure.

## ✅ Status

LocalStack is **fully integrated** with cloud-policy-guard. The `scan-aws` command has built-in support for custom endpoints, making it perfect for local testing.

## What is LocalStack?

LocalStack is a fully functional local AWS cloud stack that allows you to develop and test AWS applications locally. For cloud-policy-guard, it's perfect for testing security group scanning without touching real AWS resources.

**Key benefits:**
- 🔐 Test with fake credentials (no AWS account needed)
- 🚀 Run security group scanning locally
- 🧪 Perfect for CI/CD pipelines
- 💰 Zero AWS costs
- ⚡ Fast iteration during development

## Quick Start

### 1. Start LocalStack

```bash
./localstack-start.sh
```

This will:
- Start the LocalStack container with Docker Compose
- Enable the EC2 service
- Wait for the service to be healthy
- Display connection details

### 2. Run Your Scan

```bash
java -jar target/cloud-policy-guard.jar scan-aws \
  --region us-east-1 \
  --endpoint-url http://localhost:4566 \
  --access-key test \
  --secret-key test \
  --format json \
  --output localstack-scan-results.json
```

### 3. Stop LocalStack

```bash
./localstack-stop.sh
```

## How It Works

The application already has built-in LocalStack support through the `ScanAwsCommand` class, which accepts:
- `--endpoint-url` — Points to LocalStack instead of real AWS
- `--access-key` — Test credentials (default: `test`)
- `--secret-key` — Test credentials (default: `test`)

## Environment Variables

You can control LocalStack behavior with environment variables in `docker-compose.yml`:

| Variable | Purpose | Default |
|----------|---------|---------|
| `SERVICES` | Which AWS services to emulate | `ec2` |
| `DEBUG` | Enable verbose logging | `0` |

## Troubleshooting

### LocalStack won't start

**Check Docker is running:**
```bash
docker ps
```

**View LocalStack logs:**
```bash
docker-compose logs localstack
```

### Connection refused error

**Make sure LocalStack is fully started:**
```bash
curl http://localhost:4566/_localstack/health
```

If this fails, wait another 10 seconds and try again.

**Check LocalStack is running:**
```bash
docker-compose ps
```

## Files in This Directory

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Docker Compose configuration for LocalStack |
| `localstack-start.sh` | Start LocalStack and verify it's ready |
| `localstack-stop.sh` | Stop LocalStack and clean up |
| `LOCALSTACK.md` | This guide |

## Integration with CI/CD

You can use LocalStack in your CI/CD pipeline by:

1. Starting LocalStack in your pipeline
2. Running the scan command
3. Validating the results

Example GitHub Actions step:

```yaml
- name: Start LocalStack
  run: docker-compose up -d

- name: Wait for LocalStack
  run: sleep 10

- name: Run scan against LocalStack
  run: |
    java -jar target/cloud-policy-guard.jar scan-aws \
      --region us-east-1 \
      --endpoint-url http://localhost:4566 \
      --access-key test \
      --secret-key test
```

## Advanced Usage

### Custom EC2 Rules

To test with specific security group configurations, create security groups manually using AWS CLI:

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

aws ec2 create-security-group \
  --group-name my-test-sg \
  --description "Test security group" \
  --vpc-id vpc-xxxxx \
  --endpoint-url http://localhost:4566 \
  --region us-east-1
```

### Inspect LocalStack Data

List all security groups:

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

aws ec2 describe-security-groups \
  --endpoint-url http://localhost:4566 \
  --region us-east-1
```

### Run Multiple Tests

LocalStack persists data while running. You can:

1. Start LocalStack once
2. Create or modify security group data
3. Run multiple scans with different configurations
4. Modify security groups and re-scan
5. Stop LocalStack when done

## Performance Notes

- First run may take 30-60 seconds as Docker pulls the image
- Subsequent runs start in 5-10 seconds
- LocalStack uses ~500MB RAM when running

## References

- [LocalStack Documentation](https://docs.localstack.cloud)
- [AWS EC2 Security Groups Documentation](https://docs.aws.amazon.com/vpc/latest/userguide/VPC_SecurityGroups.html)

## ✅ Tested & Working

This setup has been tested and verified to work with cloud-policy-guard:

```
✅ LocalStack 3.8.1 running through Docker Compose and Testcontainers
✅ EC2 service emulated
✅ Test security group created in LocalStack
✅ AWS security group import executed through the EC2 client
✅ Scanner detected the expected PUBLIC_SSH violation
```

Example scan output showing detected violations:
- PUBLIC_SSH: SSH port 22 open to 0.0.0.0/0
- PUBLIC_RDP: RDP port 3389 open to ::/0
- WIDE_OPEN_ADMIN_PORT: Administrative ports exposed
- OVERLY_BROAD_EGRESS: Unrestricted outbound traffic
