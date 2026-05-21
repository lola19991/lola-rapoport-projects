from __future__ import annotations

import math
from functools import lru_cache
from typing import Iterable

import networkx as nx
import numpy as np

from ride_dispatch_optimizer.entities import Node


def make_grid_graph(width: int, height: int, *, diagonal: bool = False) -> nx.Graph:
    if width <= 0 or height <= 0:
        raise ValueError("width and height must be positive")

    graph = nx.grid_2d_graph(width, height)
    if diagonal:
        for x in range(width - 1):
            for y in range(height - 1):
                graph.add_edge((x, y), (x + 1, y + 1))
                graph.add_edge((x + 1, y), (x, y + 1))
    nx.set_edge_attributes(graph, 1.0, "weight")
    nx.set_node_attributes(graph, {node: node for node in graph.nodes}, "pos")
    return graph


class DistanceOracle:
    def __init__(self, graph: nx.Graph):
        self.graph = graph
        self._lengths = dict(nx.all_pairs_shortest_path_length(graph))

    def distance(self, source: Node, target: Node) -> float:
        if source == target:
            return 0.0
        try:
            return float(self._lengths[source][target])
        except KeyError:
            return math.inf


def random_node(nodes: Iterable[Node], rng: np.random.Generator, weights=None) -> Node:
    node_list = list(nodes)
    idx = int(rng.choice(len(node_list), p=weights))
    return node_list[idx]


def move_one_step(graph: nx.Graph, node: Node, rng: np.random.Generator) -> Node:
    neighbors = list(graph.neighbors(node))
    if not neighbors:
        return node
    return neighbors[int(rng.integers(0, len(neighbors)))]


@lru_cache(maxsize=4096)
def zone_id(node: Node, zone_size: int = 4):
    if isinstance(node, tuple) and len(node) >= 2:
        return (int(node[0]) // zone_size, int(node[1]) // zone_size)
    return ("all", 0)


def manhattan_distance(a: Node, b: Node) -> float:
    if isinstance(a, tuple) and isinstance(b, tuple) and len(a) >= 2 and len(b) >= 2:
        return abs(float(a[0]) - float(b[0])) + abs(float(a[1]) - float(b[1]))
    return 0.0 if a == b else 1.0

