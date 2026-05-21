from __future__ import annotations

from collections import defaultdict
from dataclasses import dataclass, field
from typing import DefaultDict, Iterable

from ride_dispatch_optimizer.entities import Node, Rider
from ride_dispatch_optimizer.graph import zone_id


@dataclass
class DemandForecast:
    zone_size: int = 4
    decay_rate: float = 0.985
    counts: DefaultDict[object, float] = field(default_factory=lambda: defaultdict(float))

    def decay(self) -> None:
        for key in list(self.counts.keys()):
            self.counts[key] *= self.decay_rate
            if self.counts[key] < 1e-6:
                del self.counts[key]

    def observe_riders(self, riders: Iterable[Rider]) -> None:
        for rider in riders:
            self.counts[self.zone(rider.origin)] += 1.0

    def zone(self, node: Node):
        return zone_id(node, self.zone_size)

    def score(self, node: Node) -> float:
        if not self.counts:
            return 0.0
        maximum = max(self.counts.values())
        return 0.0 if maximum <= 0 else self.counts[self.zone(node)] / maximum

