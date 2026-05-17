# Lithography Defect Detection Using Deep Learning

Deep-learning pipeline for detecting defects in lithography/SEM-style semiconductor wafer images. The project uses Faster R-CNN and a two-stage training strategy: first learning from PCB defect images, then adapting the data distribution using lithography-inspired image simulation and synthetic particle defects.

## Motivation

Real annotated lithography defect datasets are hard to obtain. This project explores how to transfer detection ability from a related PCB defect dataset to SEM/lithography-style imagery using simulation-based augmentation.

## Main Ideas

- Treat all defect categories as a single `defect` class.
- Use Faster R-CNN with a ResNet-50-FPN backbone.
- Convert images to SEM-like grayscale appearance.
- Apply optical lithography simulation using an Airy disk point spread function and resist-development-style transformations.
- Inject synthetic bright particle defects and automatically create bounding boxes.

## Files

```text
training.ipynb
experiments/
├── baseline_vs_finetune.ipynb
├── lithography_simulation.ipynb
├── run_model_on_single_image.ipynb
├── showcase_defects.ipynb
├── test_model.ipynb
└── test_particle_detection.ipynb
report.pdf
```

## How to Run

Install dependencies from the repository root:

```bash
pip install -r requirements.txt
```

Then open the notebooks:

```bash
jupyter lab
```

Start with `training.ipynb`, then use the notebooks under `experiments/` for visualizations and evaluation.

## Data

The notebooks assume access to a PCB defect dataset in COCO-style format. Large datasets and trained model checkpoints are intentionally not included in the repository.

## Skills Demonstrated

- Computer vision and object detection
- PyTorch/torchvision model fine-tuning
- Synthetic data generation
- Domain adaptation
- Scientific experimentation and technical reporting
