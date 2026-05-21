# Online Ride Dispatch Optimizer

A research-style Python simulator for online ride dispatch. Riders arrive over time, drivers operate on a graph, and matching policies compete on waiting time, pickup cost, rejection rate, and fleet utilization.

This project connects online algorithms, min-cost matching, and production-like dispatch tradeoffs in a real-time mobility domain.

It is included in this portfolio as an implementation-oriented algorithms project with source code, tests, reproducible experiments, and a technical write-up.

## Implemented Algorithms

- Greedy nearest-driver matching
- Batch matching every `T` seconds
- Min-cost bipartite matching using SciPy's Hungarian assignment
- Online primal-dual style heuristic with zone shadow prices
- Learned demand-aware heuristic using an online demand forecast

## Technical Report

The full technical write-up is in [report.tex](report.tex). It documents the formal model, objective function, algorithmic details, complexity, experimental design, and demonstrated results.

The repository includes a demonstrated result snapshot used by the report:

- `docs/figures/policy_summary.png`
- `docs/results/summary_table.tex`
- `docs/results/summary.csv`

After running a new experiment, refreshed artifacts are also written to `outputs/latest/`:

- `outputs/latest/policy_summary.png`
- `outputs/latest/summary_table.tex`
- `outputs/latest/summary.csv`

Compile the report with:

```bash
pdflatex report.tex
```

If you want the result table and figure populated first, run:

```bash
python -m ride_dispatch_optimizer.cli --out outputs/latest
pdflatex report.tex
```

## Project Structure

```text
src/ride_dispatch_optimizer/
  algorithms.py      Matching policies
  arrivals.py        Stochastic online rider generation
  demand.py          Online demand forecast
  graph.py           Grid graph and shortest-path utilities
  simulator.py       Event loop, metrics, and result tables
  experiments.py     Multi-policy experiment runner and plots
  cli.py             Command-line entrypoint
tests/               Unit tests for policies and simulator behavior
examples/            Small runnable example
report.tex           Technical report and research write-up
```

## Quick Start

```bash
python -m venv .venv
source .venv/bin/activate
pip install -e .
python -m unittest discover -s tests
```

Run a reproducible experiment:

```bash
python -m ride_dispatch_optimizer.cli \
  --horizon 600 \
  --drivers 45 \
  --grid-width 16 \
  --grid-height 16 \
  --base-rate 0.28 \
  --out outputs/latest
```

The run writes:

- `summary.csv`: metrics by policy
- `summary_table.tex`: LaTeX table for the report
- `<policy>_trips.csv`: completed trip-level records
- `<policy>_rejections.csv`: rejected rider records
- `<policy>_timeline.csv`: per-second operational state
- `policy_summary.png`: comparison plot

To promote a fresh run into the report snapshot:

```bash
cp outputs/latest/policy_summary.png docs/figures/policy_summary.png
cp outputs/latest/summary_table.tex docs/results/summary_table.tex
cp outputs/latest/summary.csv docs/results/summary.csv
```

## Metrics

Each policy is evaluated on:

- Mean and p95 rider wait time
- Mean pickup distance
- Rejection rate
- Completion rate
- Fleet utilization
- Completed trip count
