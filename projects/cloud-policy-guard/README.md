# Cloud Security Policy Guard

Cloud Security Policy Guard is a Java 21 cloud-security automation project. It scans cloud network access rules and flags risky connectivity before those rules reach production.

The scanner can run as a CLI, a REST service, a GitHub Actions policy gate, or an AWS SDK importer. It also exports SARIF so findings can appear in GitHub code scanning.

## Risk Rules

The default policy catalog detects:

- SSH open to `0.0.0.0/0` or `::/0`
- RDP open to `0.0.0.0/0` or `::/0`
- public database ports such as PostgreSQL, MySQL, MongoDB, Redis, SQL Server, Oracle, Cassandra, Memcached, and Elasticsearch
- public admin ports such as WinRM, VNC, Kubernetes API, and Docker API
- overly broad egress
- duplicate rules
- security groups without `owner`, `team`, or `service-owner` tags
- production rules that expose anything except public HTTP/HTTPS entry points

## Quick Start

Build and test:

```bash
./mvnw test
./mvnw -DskipTests package
```

Scan YAML:

```bash
java -jar target/cloud-policy-guard.jar scan examples/security-groups.yaml
```

Scan Terraform plan JSON:

```bash
terraform show -json plan.out > terraform-plan.json
java -jar target/cloud-policy-guard.jar scan terraform-plan.json --input-format terraform-plan
```

Generate SARIF:

```bash
java -jar target/cloud-policy-guard.jar scan examples/security-groups.yaml \
  --format sarif \
  --output policy-guard.sarif \
  --fail-on high
```

Run the REST service:

```bash
java -jar target/cloud-policy-guard.jar serve
```

Then open:

- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/docs`

### Web UI

Open `http://localhost:8080` in your browser to access the Cloud Policy Guard homepage. The web UI provides an intuitive interface to upload and scan security group files:

- **Drag and drop** a YAML or JSON file onto the upload area
- **Click to browse** and select a file from your computer
- Files are **automatically scanned** just like API requests
- View **color-coded violation results** with severity levels (Critical, High, Medium, Low)
- Each scan receives a **unique ID** for reference

This is a great way to quickly test and validate security group configurations before deploying them.

### REST API

Create a scan programmatically:

```bash
curl -s http://localhost:8080/scans \
  -H 'Content-Type: application/json' \
  -d @examples/rest-scan.json
```

## AWS Security Group Scans

Scan live AWS security groups:

```bash
java -jar target/cloud-policy-guard.jar scan-aws --region us-east-1 --fail-on high
```

Scan LocalStack:

```bash
java -jar target/cloud-policy-guard.jar scan-aws \
  --region us-east-1 \
  --endpoint-url http://localhost:4566 \
  --access-key test \
  --secret-key test
```

## REST API

The REST service stores scans in memory for simple orchestration demos.

- `POST /scans` creates a scan from a JSON list of security groups
- `GET /scans/{id}` returns the full scan result
- `GET /scans/{id}/violations` returns only violations

## GitHub Actions

The included workflow:

- runs unit tests
- runs LocalStack/Testcontainers integration tests
- builds the executable jar
- scans an example policy
- uploads SARIF to GitHub code scanning

## Docker

Build:

```bash
docker build -t cloud-policy-guard .
```

Run REST service:

```bash
docker run --rm -p 8080:8080 cloud-policy-guard
```

Run CLI:

```bash
docker run --rm -v "$PWD/examples:/examples" cloud-policy-guard scan /examples/security-groups.yaml
```
