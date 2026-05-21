from __future__ import annotations

import math
from dataclasses import dataclass
from typing import Sequence

import numpy as np

from ride_dispatch_optimizer.entities import Node, Rider
from ride_dispatch_optimizer.graph import manhattan_distance


@dataclass(frozen=True, slots=True)
class HotSpot:
    center: Node
    strength: float = 4.0
    radius: float = 4.0


class StochasticArrivalGenerator:
    def __init__(
        self,
        nodes: Sequence[Node],
        *,
        base_rate: float = 0.25,
        peak_multiplier: float = 1.5,
        period_seconds: int = 300,
        max_wait_seconds: int = 120,
        hot_spots: Sequence[HotSpot] | None = None,
        seed: int | None = None,
    ):
        self.nodes = list(nodes)
        if not self.nodes:
            raise ValueError("nodes must be non-empty")
        self.base_rate = base_rate
        self.peak_multiplier = peak_multiplier
        self.period_seconds = period_seconds
        self.max_wait_seconds = max_wait_seconds
        self.hot_spots = list(hot_spots or [])
        self.rng = np.random.default_rng(seed)
        self._next_rider_id = 0
        self._origin_weights = self._build_origin_weights()

    def arrivals_at(self, current_time: int) -> list[Rider]:
        count = int(self.rng.poisson(self._rate(current_time)))
        riders = []
        for _ in range(count):
            origin = self._sample_origin()
            destination = self._sample_destination(origin)
            riders.append(Rider(self._next_rider_id, origin, destination, current_time, self.max_wait_seconds))
            self._next_rider_id += 1
        return riders

    def _rate(self, current_time: int) -> float:
        phase = 2.0 * math.pi * (current_time % self.period_seconds) / self.period_seconds
        peak = 0.5 + 0.5 * math.sin(phase - math.pi / 2.0)
        return self.base_rate * (1.0 + self.peak_multiplier * peak)

    def _build_origin_weights(self) -> np.ndarray:
        weights = np.ones(len(self.nodes), dtype=float)
        for hot_spot in self.hot_spots:
            radius = max(hot_spot.radius, 1e-6)
            for idx, node in enumerate(self.nodes):
                weights[idx] += hot_spot.strength * math.exp(-manhattan_distance(node, hot_spot.center) / radius)
        return weights / weights.sum()

    def _sample_origin(self) -> Node:
        return self.nodes[int(self.rng.choice(len(self.nodes), p=self._origin_weights))]

    def _sample_destination(self, origin: Node) -> Node:
        if len(self.nodes) == 1:
            return origin
        idx = int(self.rng.integers(0, len(self.nodes) - 1))
        destination = self.nodes[idx]
        return self.nodes[-1] if destination == origin else destination


def default_hotspots(width: int, height: int) -> list[HotSpot]:
    radius = max(width, height) / 4.0
    return [
        HotSpot((max(0, width // 4), max(0, height // 4)), 5.0, radius),
        HotSpot((max(0, 3 * width // 4), max(0, 3 * height // 4)), 3.0, radius),
    ]

