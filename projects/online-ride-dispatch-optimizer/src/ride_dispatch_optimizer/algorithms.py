from __future__ import annotations

import math
from dataclasses import dataclass, field
from typing import Mapping, Protocol, Sequence

import numpy as np
from scipy.optimize import linear_sum_assignment

from ride_dispatch_optimizer.demand import DemandForecast
from ride_dispatch_optimizer.entities import Assignment, Driver, Rider
from ride_dispatch_optimizer.graph import DistanceOracle, zone_id


@dataclass(frozen=True, slots=True)
class MatchContext:
    time: int
    drivers: Mapping[int, Driver]
    pending_riders: Sequence[Rider]
    distance_oracle: DistanceOracle
    demand: DemandForecast | None = None
    zone_size: int = 4

    def available_drivers(self) -> list[Driver]:
        return [driver for driver in self.drivers.values() if driver.is_available(self.time)]


class MatchingPolicy(Protocol):
    @property
    def name(self) -> str:
        ...

    def before_matching(self, context: MatchContext) -> None:
        ...

    def match(self, context: MatchContext) -> list[Assignment]:
        ...


class BaseMatcher:
    @property
    def name(self) -> str:
        return self.__class__.__name__

    def before_matching(self, context: MatchContext) -> None:
        return None


@dataclass
class GreedyNearestMatcher(BaseMatcher):
    max_pickup_distance: float = math.inf

    @property
    def name(self) -> str:
        return "greedy-nearest"

    def match(self, context: MatchContext) -> list[Assignment]:
        available = {driver.driver_id: driver for driver in context.available_drivers()}
        assignments = []
        for rider in sorted(context.pending_riders, key=lambda r: (r.arrival_time, r.rider_id)):
            if not available:
                break
            best_driver, best_distance = None, math.inf
            for driver in available.values():
                distance = context.distance_oracle.distance(driver.node, rider.origin)
                if distance < best_distance:
                    best_driver, best_distance = driver, distance
            if best_driver is not None and best_distance <= self.max_pickup_distance:
                assignments.append(Assignment(best_driver.driver_id, rider.rider_id, best_distance, best_distance))
                del available[best_driver.driver_id]
        return assignments


@dataclass
class MinCostBipartiteMatcher(BaseMatcher):
    max_pickup_distance: float = math.inf
    pickup_weight: float = 1.0
    wait_credit_per_second: float = 0.03

    @property
    def name(self) -> str:
        return "min-cost-bipartite"

    def match(self, context: MatchContext) -> list[Assignment]:
        drivers = context.available_drivers()
        riders = list(context.pending_riders)
        if not drivers or not riders:
            return []
        big_m = 1e9
        costs = np.full((len(drivers), len(riders)), big_m)
        pickups = np.full_like(costs, math.inf)
        for i, driver in enumerate(drivers):
            for j, rider in enumerate(riders):
                pickup = context.distance_oracle.distance(driver.node, rider.origin)
                if pickup <= self.max_pickup_distance:
                    costs[i, j] = self.pickup_weight * pickup - self.wait_credit_per_second * rider.waited_so_far(context.time)
                    pickups[i, j] = pickup
        rows, cols = linear_sum_assignment(costs)
        return [
            Assignment(drivers[i].driver_id, riders[j].rider_id, float(pickups[i, j]), float(costs[i, j]))
            for i, j in zip(rows, cols, strict=True)
            if costs[i, j] < big_m / 2
        ]


@dataclass
class BatchMatcher(BaseMatcher):
    interval_seconds: int = 30
    base_matcher: MinCostBipartiteMatcher = field(default_factory=MinCostBipartiteMatcher)

    @property
    def name(self) -> str:
        return f"batch-{self.interval_seconds}s"

    def match(self, context: MatchContext) -> list[Assignment]:
        if context.time % self.interval_seconds != 0:
            return []
        return self.base_matcher.match(context)


@dataclass
class OnlinePrimalDualMatcher(BaseMatcher):
    max_pickup_distance: float = math.inf
    step_size: float = 0.35
    dual_decay: float = 0.95
    wait_credit_per_second: float = 0.04
    duals: dict[object, float] = field(default_factory=dict)

    @property
    def name(self) -> str:
        return "online-primal-dual"

    def before_matching(self, context: MatchContext) -> None:
        riders, drivers = {}, {}
        for rider in context.pending_riders:
            z = zone_id(rider.origin, context.zone_size)
            riders[z] = riders.get(z, 0) + 1
        for driver in context.available_drivers():
            z = zone_id(driver.node, context.zone_size)
            drivers[z] = drivers.get(z, 0) + 1
        for z in set(self.duals) | set(riders) | set(drivers):
            imbalance = riders.get(z, 0) - drivers.get(z, 0)
            self.duals[z] = max(0.0, self.dual_decay * self.duals.get(z, 0.0) + self.step_size * imbalance)

    def match(self, context: MatchContext) -> list[Assignment]:
        pairs = []
        for driver in context.available_drivers():
            driver_dual = self.duals.get(zone_id(driver.node, context.zone_size), 0.0)
            for rider in context.pending_riders:
                pickup = context.distance_oracle.distance(driver.node, rider.origin)
                if pickup <= self.max_pickup_distance:
                    rider_dual = self.duals.get(zone_id(rider.origin, context.zone_size), 0.0)
                    score = pickup + driver_dual - 1.25 * rider_dual - self.wait_credit_per_second * rider.waited_so_far(context.time)
                    pairs.append((score, pickup, driver.driver_id, rider.rider_id))
        pairs.sort()
        used_drivers, used_riders, assignments = set(), set(), []
        for score, pickup, driver_id, rider_id in pairs:
            if driver_id not in used_drivers and rider_id not in used_riders:
                used_drivers.add(driver_id)
                used_riders.add(rider_id)
                assignments.append(Assignment(driver_id, rider_id, float(pickup), float(score)))
        return assignments


@dataclass
class LearnedDemandAwareMatcher(BaseMatcher):
    max_pickup_distance: float = math.inf
    wait_credit_per_second: float = 0.035

    @property
    def name(self) -> str:
        return "learned-demand-aware"

    def match(self, context: MatchContext) -> list[Assignment]:
        drivers = context.available_drivers()
        riders = list(context.pending_riders)
        if not drivers or not riders:
            return []
        big_m = 1e9
        costs = np.full((len(drivers), len(riders)), big_m)
        pickups = np.full_like(costs, math.inf)
        for i, driver in enumerate(drivers):
            reserve = 2.0 * (context.demand.score(driver.node) if context.demand else 0.0)
            for j, rider in enumerate(riders):
                pickup = context.distance_oracle.distance(driver.node, rider.origin)
                if pickup <= self.max_pickup_distance:
                    dest_reward = context.demand.score(rider.destination) if context.demand else 0.0
                    costs[i, j] = pickup + reserve - dest_reward - self.wait_credit_per_second * rider.waited_so_far(context.time)
                    pickups[i, j] = pickup
        rows, cols = linear_sum_assignment(costs)
        return [
            Assignment(drivers[i].driver_id, riders[j].rider_id, float(pickups[i, j]), float(costs[i, j]))
            for i, j in zip(rows, cols, strict=True)
            if costs[i, j] < big_m / 2
        ]


def make_policy(name: str, *, batch_interval_seconds: int = 30, max_pickup_distance: float = math.inf) -> BaseMatcher:
    normalized = name.strip().lower().replace("_", "-")
    if normalized in {"greedy", "greedy-nearest"}:
        return GreedyNearestMatcher(max_pickup_distance)
    if normalized in {"batch", "batch-matching"}:
        return BatchMatcher(batch_interval_seconds, MinCostBipartiteMatcher(max_pickup_distance))
    if normalized in {"min-cost", "min-cost-bipartite", "hungarian"}:
        return MinCostBipartiteMatcher(max_pickup_distance)
    if normalized in {"primal-dual", "online-primal-dual"}:
        return OnlinePrimalDualMatcher(max_pickup_distance)
    if normalized in {"learned", "demand-aware", "learned-demand-aware"}:
        return LearnedDemandAwareMatcher(max_pickup_distance)
    raise ValueError(f"unknown policy: {name}")

