from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Sequence

import networkx as nx
import numpy as np
import pandas as pd

from ride_dispatch_optimizer.algorithms import MatchContext, MatchingPolicy
from ride_dispatch_optimizer.arrivals import StochasticArrivalGenerator
from ride_dispatch_optimizer.demand import DemandForecast
from ride_dispatch_optimizer.entities import Assignment, Driver, Rider, RiderStatus, SimulationConfig
from ride_dispatch_optimizer.graph import DistanceOracle, move_one_step


@dataclass
class SimulationResult:
    policy_name: str
    horizon_seconds: int
    n_drivers: int
    completed_trips: pd.DataFrame
    rejected_riders: pd.DataFrame
    timeline: pd.DataFrame

    def summary(self) -> dict[str, float | int | str]:
        completed = len(self.completed_trips)
        rejected = len(self.rejected_riders)
        total = completed + rejected
        if completed:
            mean_wait = float(self.completed_trips["wait_time"].mean())
            p95_wait = float(self.completed_trips["wait_time"].quantile(0.95))
            mean_pickup = float(self.completed_trips["pickup_distance"].mean())
            mean_trip = float(self.completed_trips["trip_distance"].mean())
            busy_seconds = float(self.completed_trips["driver_busy_seconds"].sum())
        else:
            mean_wait = p95_wait = mean_pickup = mean_trip = float("nan")
            busy_seconds = 0.0
        return {
            "policy": self.policy_name,
            "arrivals": total,
            "completed": completed,
            "rejected": rejected,
            "completion_rate": completed / total if total else 0.0,
            "rejection_rate": rejected / total if total else 0.0,
            "mean_wait_time": mean_wait,
            "p95_wait_time": p95_wait,
            "mean_pickup_distance": mean_pickup,
            "mean_trip_distance": mean_trip,
            "fleet_utilization": busy_seconds / max(1, self.n_drivers * self.horizon_seconds),
        }

    def write_csvs(self, output_dir: str | Path) -> None:
        output_path = Path(output_dir)
        output_path.mkdir(parents=True, exist_ok=True)
        safe_name = self.policy_name.replace("/", "-")
        self.completed_trips.to_csv(output_path / f"{safe_name}_trips.csv", index=False)
        self.rejected_riders.to_csv(output_path / f"{safe_name}_rejections.csv", index=False)
        self.timeline.to_csv(output_path / f"{safe_name}_timeline.csv", index=False)


class RideDispatchSimulator:
    def __init__(
        self,
        graph: nx.Graph,
        policy: MatchingPolicy,
        arrival_generator: StochasticArrivalGenerator,
        *,
        n_drivers: int,
        config: SimulationConfig | None = None,
        demand_forecast: DemandForecast | None = None,
        seed: int | None = None,
    ):
        self.graph = graph
        self.policy = policy
        self.arrival_generator = arrival_generator
        self.config = config or SimulationConfig()
        self.distance_oracle = DistanceOracle(graph)
        self.demand_forecast = demand_forecast
        self.rng = np.random.default_rng(seed)
        self.drivers = self._initialize_drivers(n_drivers)
        self.pending_riders: dict[int, Rider] = {}
        self.completed_records: list[dict] = []
        self.rejected_records: list[dict] = []
        self.timeline_records: list[dict] = []

    def run(self) -> SimulationResult:
        for current_time in range(self.config.horizon_seconds):
            self._move_idle_drivers(current_time)
            if self.demand_forecast:
                self.demand_forecast.decay()

            new_riders = self.arrival_generator.arrivals_at(current_time)
            for rider in new_riders:
                self.pending_riders[rider.rider_id] = rider
            if self.demand_forecast:
                self.demand_forecast.observe_riders(new_riders)

            rejected_before = len(self.rejected_records)
            self._reject_expired(current_time, reason="max_wait_exceeded")

            context = MatchContext(
                current_time,
                self.drivers,
                list(self.pending_riders.values()),
                self.distance_oracle,
                self.demand_forecast,
            )
            self.policy.before_matching(context)
            matches = self._apply_assignments(self.policy.match(context), current_time)
            self.timeline_records.append(
                {
                    "time": current_time,
                    "arrivals": len(new_riders),
                    "matches": matches,
                    "rejections": len(self.rejected_records) - rejected_before,
                    "pending": len(self.pending_riders),
                    "available_drivers": sum(driver.is_available(current_time) for driver in self.drivers.values()),
                    "busy_drivers": sum(not driver.is_available(current_time) for driver in self.drivers.values()),
                }
            )

        if self.config.reject_unmatched_at_end:
            self._reject_all(self.config.horizon_seconds, reason="horizon_end")

        return SimulationResult(
            self.policy.name,
            self.config.horizon_seconds,
            len(self.drivers),
            pd.DataFrame.from_records(self.completed_records),
            pd.DataFrame.from_records(self.rejected_records),
            pd.DataFrame.from_records(self.timeline_records),
        )

    def _initialize_drivers(self, n_drivers: int) -> dict[int, Driver]:
        if n_drivers <= 0:
            raise ValueError("n_drivers must be positive")
        nodes = list(self.graph.nodes)
        return {i: Driver(i, nodes[int(self.rng.integers(0, len(nodes)))]) for i in range(n_drivers)}

    def _move_idle_drivers(self, current_time: int) -> None:
        for driver in self.drivers.values():
            if driver.is_available(current_time) and self.rng.random() < self.config.idle_move_probability:
                driver.node = move_one_step(self.graph, driver.node, self.rng)

    def _reject_expired(self, current_time: int, *, reason: str) -> None:
        expired = [
            rider_id
            for rider_id, rider in self.pending_riders.items()
            if rider.waited_so_far(current_time) > rider.max_wait_seconds
        ]
        for rider_id in expired:
            self._record_rejection(self.pending_riders.pop(rider_id), current_time, reason)

    def _reject_all(self, current_time: int, *, reason: str) -> None:
        for rider_id in list(self.pending_riders):
            self._record_rejection(self.pending_riders.pop(rider_id), current_time, reason)

    def _record_rejection(self, rider: Rider, current_time: int, reason: str) -> None:
        rider.status = RiderStatus.REJECTED
        self.rejected_records.append(
            {
                "time": current_time,
                "rider_id": rider.rider_id,
                "arrival_time": rider.arrival_time,
                "waited_seconds": current_time - rider.arrival_time,
                "origin": str(rider.origin),
                "destination": str(rider.destination),
                "reason": reason,
            }
        )

    def _apply_assignments(self, assignments: Sequence[Assignment], current_time: int) -> int:
        used_drivers, used_riders, applied = set(), set(), 0
        for assignment in assignments:
            if assignment.driver_id in used_drivers or assignment.rider_id in used_riders:
                continue
            driver = self.drivers.get(assignment.driver_id)
            rider = self.pending_riders.get(assignment.rider_id)
            if driver is None or rider is None or not driver.is_available(current_time):
                continue
            pickup = self.distance_oracle.distance(driver.node, rider.origin)
            trip = self.distance_oracle.distance(rider.origin, rider.destination)
            if not np.isfinite(pickup) or not np.isfinite(trip):
                continue
            response_wait = current_time - rider.arrival_time
            busy = pickup + trip
            driver_start = driver.node
            driver.busy_until = current_time + busy
            driver.node = rider.destination
            driver.completed_trips += 1
            driver.total_busy_seconds += busy
            rider.status = RiderStatus.MATCHED
            self.pending_riders.pop(rider.rider_id)
            self.completed_records.append(
                {
                    "time": current_time,
                    "driver_id": driver.driver_id,
                    "rider_id": rider.rider_id,
                    "arrival_time": rider.arrival_time,
                    "response_wait": response_wait,
                    "pickup_distance": pickup,
                    "wait_time": response_wait + pickup,
                    "trip_distance": trip,
                    "driver_busy_seconds": busy,
                    "driver_start": str(driver_start),
                    "origin": str(rider.origin),
                    "destination": str(rider.destination),
                    "score": assignment.score,
                }
            )
            used_drivers.add(driver.driver_id)
            used_riders.add(rider.rider_id)
            applied += 1
        return applied

