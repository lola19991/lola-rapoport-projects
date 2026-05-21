from __future__ import annotations

import argparse
from pathlib import Path

from ride_dispatch_optimizer.experiments import DEFAULT_POLICIES, run_policy_suite, write_results


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Run online ride-dispatch matching experiments.")
    parser.add_argument("--horizon", type=int, default=600)
    parser.add_argument("--drivers", type=int, default=45)
    parser.add_argument("--grid-width", type=int, default=16)
    parser.add_argument("--grid-height", type=int, default=16)
    parser.add_argument("--base-rate", type=float, default=0.28)
    parser.add_argument("--batch-interval", type=int, default=30)
    parser.add_argument("--max-wait", type=int, default=120)
    parser.add_argument("--idle-move-probability", type=float, default=0.05)
    parser.add_argument("--max-pickup-distance", type=float, default=float("inf"))
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--out", type=Path, default=Path("outputs/latest"))
    parser.add_argument("--policies", nargs="+", default=DEFAULT_POLICIES)
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    results, summary = run_policy_suite(
        horizon_seconds=args.horizon,
        n_drivers=args.drivers,
        grid_width=args.grid_width,
        grid_height=args.grid_height,
        base_rate=args.base_rate,
        batch_interval_seconds=args.batch_interval,
        max_wait_seconds=args.max_wait,
        idle_move_probability=args.idle_move_probability,
        max_pickup_distance=args.max_pickup_distance,
        policies=args.policies,
        seed=args.seed,
    )
    write_results(results, summary, args.out)
    print(summary.round(4).to_string(index=False))
    print(f"\nWrote results to {args.out.resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

