from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Hashable

Node = Hashable


class RiderStatus(str, Enum):
    PENDING = "pending"
    MATCHED = "matched"
    REJECTED = "rejected"


@dataclass(slots=True)
class Rider:
    rider_id: int
    origin: Node
    destination: Node
    arrival_time: int
    max_wait_seconds: int
    status: RiderStatus = RiderStatus.PENDING

    def waited_so_far(self, current_time: int) -> int:
        return current_time - self.arrival_time


@dataclass(slots=True)
class Driver:
    driver_id: int
    node: Node
    busy_until: float = 0.0
    completed_trips: int = 0
    total_busy_seconds: float = 0.0

    def is_available(self, current_time: int) -> bool:
        return self.busy_until <= current_time


@dataclass(frozen=True, slots=True)
class Assignment:
    driver_id: int
    rider_id: int
    pickup_distance: float
    score: float


@dataclass(frozen=True, slots=True)
class SimulationConfig:
    horizon_seconds: int = 600
    max_wait_seconds: int = 120
    idle_move_probability: float = 0.05
    reject_unmatched_at_end: bool = True

