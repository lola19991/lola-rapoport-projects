from __future__ import annotations

import os
from pathlib import Path
from typing import Sequence

import pandas as pd

from ride_dispatch_optimizer.algorithms import make_policy
from ride_dispatch_optimizer.arrivals import StochasticArrivalGenerator, default_hotspots
from ride_dispatch_optimizer.demand import DemandForecast
from ride_dispatch_optimizer.entities import SimulationConfig
from ride_dispatch_optimizer.graph import make_grid_graph
from ride_dispatch_optimizer.simulator import RideDispatchSimulator, SimulationResult

DEFAULT_POLICIES = [
    "greedy-nearest",
    "batch",
    "min-cost-bipartite",
    "online-primal-dual",
    "learned-demand-aware",
]


def run_policy_suite(
    *,
    horizon_seconds: int = 600,
    n_drivers: int = 45,
    grid_width: int = 16,
    grid_height: int = 16,
    base_rate: float = 0.28,
    batch_interval_seconds: int = 30,
    max_wait_seconds: int = 120,
    idle_move_probability: float = 0.05,
    max_pickup_distance: float = float("inf"),
    policies: Sequence[str] = DEFAULT_POLICIES,
    seed: int = 42,
) -> tuple[list[SimulationResult], pd.DataFrame]:
    graph = make_grid_graph(grid_width, grid_height)
    hot_spots = default_hotspots(grid_width, grid_height)
    config = SimulationConfig(horizon_seconds, max_wait_seconds, idle_move_probability)
    results, summaries = [], []
    for policy_name in policies:
        policy = make_policy(policy_name, batch_interval_seconds=batch_interval_seconds, max_pickup_distance=max_pickup_distance)
        arrivals = StochasticArrivalGenerator(
            list(graph.nodes),
            base_rate=base_rate,
            max_wait_seconds=max_wait_seconds,
            hot_spots=hot_spots,
            seed=seed,
        )
        simulator = RideDispatchSimulator(
            graph,
            policy,
            arrivals,
            n_drivers=n_drivers,
            config=config,
            demand_forecast=DemandForecast(),
            seed=seed + 10_000,
        )
        result = simulator.run()
        results.append(result)
        summaries.append(result.summary())
    return results, pd.DataFrame.from_records(summaries).sort_values("mean_wait_time", na_position="last")


def write_results(results: Sequence[SimulationResult], summary: pd.DataFrame, output_dir: str | Path) -> None:
    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)
    summary.to_csv(output_path / "summary.csv", index=False)
    for result in results:
        result.write_csvs(output_path)
    plot_summary(summary, output_path)
    write_latex_table(summary, output_path)


def plot_summary(summary: pd.DataFrame, output_dir: str | Path) -> Path:
    output_path = Path(output_dir)
    cache_path = output_path / ".matplotlib"
    xdg_cache_path = output_path / ".cache"
    cache_path.mkdir(parents=True, exist_ok=True)
    xdg_cache_path.mkdir(parents=True, exist_ok=True)
    os.environ.setdefault("MPLCONFIGDIR", str(cache_path))
    os.environ.setdefault("XDG_CACHE_HOME", str(xdg_cache_path))

    import matplotlib.pyplot as plt

    ordered = summary.sort_values("mean_wait_time", na_position="last")
    fig, axes = plt.subplots(1, 3, figsize=(15, 4.5))
    axes[0].bar(ordered["policy"], ordered["mean_wait_time"], color="#356c9c")
    axes[0].set_title("Mean wait")
    axes[0].set_ylabel("seconds")
    axes[1].bar(ordered["policy"], ordered["rejection_rate"], color="#b44e3a")
    axes[1].set_title("Rejection rate")
    axes[1].set_ylim(0, max(0.05, min(1.0, ordered["rejection_rate"].max() * 1.2)))
    axes[2].bar(ordered["policy"], ordered["fleet_utilization"], color="#3d7f5f")
    axes[2].set_title("Fleet utilization")
    axes[2].set_ylim(0, max(0.05, min(1.0, ordered["fleet_utilization"].max() * 1.2)))
    for axis in axes:
        axis.tick_params(axis="x", rotation=35)
        axis.grid(axis="y", alpha=0.25)
    fig.tight_layout()
    plot_path = output_path / "policy_summary.png"
    fig.savefig(plot_path, dpi=180)
    plt.close(fig)
    return plot_path


def write_latex_table(summary: pd.DataFrame, output_dir: str | Path) -> Path:
    path = Path(output_dir) / "summary_table.tex"
    columns = [
        "policy",
        "completed",
        "rejected",
        "mean_wait_time",
        "p95_wait_time",
        "mean_pickup_distance",
        "fleet_utilization",
    ]
    formatted = summary[columns].copy()
    for column in columns[3:]:
        formatted[column] = formatted[column].map(lambda x: f"{x:.3f}")
    formatted.to_latex(path, index=False, escape=True)
    return path

