# Lola Rapoport — Project Portfolio

This repository collects selected academic, algorithmic, and machine-learning projects. It is organized as a portfolio repository for job applications, with runnable code where available and PDF reports for theory-heavy projects.

## Highlights

### 1. Lithography Defect Detection Using Deep Learning
**Type:** computer vision / deep learning / domain adaptation  
**Tools:** Python, PyTorch, torchvision, Faster R-CNN, NumPy, SciPy, Matplotlib  
**Location:** [`projects/lithography-defect-detection`](projects/lithography-defect-detection)

Built a defect-detection pipeline for lithography/SEM-style wafer images using Faster R-CNN with a ResNet-50-FPN backbone. The project handles a domain gap between PCB defect images and lithography-style imagery using physics-inspired augmentation, SEM-style conversion, optical blur simulation, and synthetic particle contamination.

### 2. Convex Optimization: Simplex-Constrained Least Squares
**Type:** numerical optimization / convex optimization  
**Tools:** Python, NumPy  
**Location:** [`projects/convex-optimization-simplex-solver`](projects/convex-optimization-simplex-solver)

Implemented solvers for a constrained least-squares problem over the probability simplex, including projected gradient descent and an interior-point Newton/log-barrier method.

### 3. Academic and Theory Reports
**Type:** algorithms / online algorithms / optimization / game theory  
**Location:** [`projects/academic-reports`](projects/academic-reports)

Selected longer-form research and seminar reports, including work on the uniform k-client problem, potential-function proofs for gradient methods, online Bayesian matching/ski rental, and sonar object classification.

## Repository Structure

```text
.
├── README.md
├── .gitignore
├── requirements.txt
└── projects/
    ├── lithography-defect-detection/
    ├── convex-optimization-simplex-solver/
    └── academic-reports/
```

## Setup

For the Python projects, create a virtual environment and install the dependencies:

```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

For notebooks:

```bash
jupyter lab
```

## Notes for Recruiters

This repository contains a mix of research-oriented and implementation-oriented work. The lithography project is the strongest software/ML-facing project. The optimization and theory reports demonstrate mathematical maturity, algorithmic thinking, and the ability to write rigorous technical material.
