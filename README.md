# Lola Rapoport — Project Portfolio

This repository collects selected software-engineering, cloud, algorithmic, optimization, and machine-learning projects. It is organized as a portfolio repository, with runnable code where available and PDF reports for theory-heavy projects.

## Highlights

### 1. Cloud Security Policy Guard
**Type:** Java / cloud security / backend automation / DevOps tooling  
**Tools:** Java 21, Spring Boot, Maven, AWS SDK, Docker, GitHub Actions, SARIF, JUnit 5  
**Location:** [`projects/cloud-policy-guard`](projects/cloud-policy-guard)

Built a cloud-security automation tool that scans network access rules and detects risky configurations before they reach production. The project supports YAML security-group files, Terraform plan JSON, AWS security groups, CLI usage, a REST API, Docker execution, SARIF export for GitHub code scanning, and a GitHub Actions policy gate. It demonstrates Java backend development, cloud/security thinking, testable architecture, CI integration, and practical infrastructure automation.

### 2. Online Ride Dispatch Optimizer
**Type:** online algorithms / optimization / simulation / data analysis  
**Tools:** Python, NumPy, SciPy, pandas, NetworkX, Matplotlib  
**Location:** [`projects/online-ride-dispatch-optimizer`](projects/online-ride-dispatch-optimizer)

Built a research-style simulator for real-time ride dispatch. Riders arrive online, drivers move on a graph, and multiple matching policies are compared using waiting time, pickup cost, rejection rate, completion rate, and fleet utilization. Implemented greedy matching, periodic batch matching, min-cost bipartite matching, an online primal-dual style heuristic, and a learned demand-aware heuristic. The project includes unit tests, a command-line experiment runner, reproducible result snapshots, and a technical report.

### 3. Lithography Defect Detection Using Deep Learning
**Type:** computer vision / deep learning / domain adaptation  
**Tools:** Python, PyTorch, torchvision, Faster R-CNN, NumPy, SciPy, Matplotlib  
**Location:** [`projects/lithography-defect-detection`](projects/lithography-defect-detection)

Built a defect-detection pipeline for lithography/SEM-style wafer images using Faster R-CNN with a ResNet-50-FPN backbone. The project handles a domain gap between PCB defect images and lithography-style imagery using physics-inspired augmentation, SEM-style conversion, optical blur simulation, and synthetic particle contamination.

### 4. Arkanoid Java Game
**Type:** Java / object-oriented programming / desktop application / game logic  
**Tools:** Java 17, Gradle, Swing, Java2D, JUnit 5  
**Location:** [`projects/arkanoid-java`](projects/arkanoid-java)

Implemented a standalone Arkanoid game using Java Swing and Java2D, with difficulty selection, generated levels, pause/resume behavior, countdowns, lives, scoring, power-ups, and unit-tested gameplay logic. This project is useful for demonstrating Java, OOP design, event-driven UI programming, Gradle project organization, and clean model/UI separation.

### 5. Convex Optimization: Simplex-Constrained Least Squares
**Type:** numerical optimization / convex optimization  
**Tools:** Python, NumPy  
**Location:** [`projects/convex-optimization-simplex-solver`](projects/convex-optimization-simplex-solver)

Implemented solvers for a constrained least-squares problem over the probability simplex, including projected gradient descent and an interior-point Newton/log-barrier method.

### 6. Academic and Theory Reports
**Type:** algorithms / online algorithms / optimization / game theory  
**Location:** [`projects/academic-reports`](projects/academic-reports)

Selected longer-form research and seminar reports, including work on the uniform k-client problem, potential-function proofs for gradient methods, online Bayesian matching/ski rental, and sonar object classification.

## Repository Structure

```text
.
├── README.md
├── .gitignore
├── requirements.txt
├── .github/
│   └── workflows/
│       └── cloud-policy-guard.yml
└── projects/
    ├── cloud-policy-guard/
    ├── online-ride-dispatch-optimizer/
    ├── lithography-defect-detection/
    ├── arkanoid-java/
    ├── convex-optimization-simplex-solver/
    └── academic-reports/
```

## Setup

### Python projects

Create a virtual environment and install the shared Python dependencies:

```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

The ride-dispatch project is a Python package. To install it in editable mode from the repository root:

```bash
pip install -e projects/online-ride-dispatch-optimizer
python -m unittest discover -s projects/online-ride-dispatch-optimizer/tests
```

Run its reproducible experiment from the repository root with:

```bash
python -m ride_dispatch_optimizer.cli \
  --horizon 600 \
  --drivers 45 \
  --grid-width 16 \
  --grid-height 16 \
  --base-rate 0.28 \
  --out projects/online-ride-dispatch-optimizer/outputs/latest
```

For notebooks:

```bash
jupyter lab
```

### Java projects

#### Cloud Security Policy Guard

The cloud-security project requires JDK 21. From the repository root:

```bash
cd projects/cloud-policy-guard
./mvnw test
./mvnw -DskipTests package
```

Run the CLI scanner:

```bash
java -jar target/cloud-policy-guard.jar scan examples/security-groups.yaml
```

Run the REST service:

```bash
java -jar target/cloud-policy-guard.jar serve
```

Or build and run with Docker:

```bash
docker build -t cloud-policy-guard .
docker run --rm -p 8080:8080 cloud-policy-guard
```

#### Arkanoid Java Game

The Arkanoid project requires JDK 17 or newer. From the repository root:

```bash
cd projects/arkanoid-java
./gradlew test
./gradlew run
```

For a non-graphical startup check:

```bash
./gradlew run --args="--smoke-test"
```

On Windows, use `mvnw.cmd` or `gradlew.bat` instead of `./mvnw` or `./gradlew`.

## GitHub Actions

The repository includes a workflow for the Cloud Security Policy Guard project:

```text
.github/workflows/cloud-policy-guard.yml
```

It runs when files under `projects/cloud-policy-guard/` change. The workflow installs JDK 21, runs Maven tests, builds the executable JAR, generates a SARIF report from the example policy, and uploads the SARIF file to GitHub code scanning.

## Notes for Recruiters

This repository contains a mix of research-oriented and implementation-oriented work. The Cloud Security Policy Guard project is the strongest Java/cloud/backend project because it combines Spring Boot, Maven, AWS concepts, Docker, GitHub Actions, SARIF reporting, CLI design, REST APIs, and automated tests. The online ride-dispatch project is the strongest algorithms/software-engineering project because it combines online optimization, simulation, testing, metrics, and reproducible experiments. The lithography project is the strongest ML/computer-vision-facing project. The Arkanoid project is a compact Java project that demonstrates object-oriented design, Gradle, Swing UI programming, and automated tests. The optimization solver and theory reports demonstrate mathematical maturity, algorithmic thinking, and the ability to write rigorous technical material.
