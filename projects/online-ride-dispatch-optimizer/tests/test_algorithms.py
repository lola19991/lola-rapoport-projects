import unittest

import networkx as nx

from ride_dispatch_optimizer.algorithms import BatchMatcher, MatchContext, MinCostBipartiteMatcher
from ride_dispatch_optimizer.entities import Driver, Rider
from ride_dispatch_optimizer.graph import DistanceOracle


class AlgorithmTests(unittest.TestCase):
    def test_min_cost_bipartite_matches_global_nearest_pairs(self):
        graph = nx.path_graph(11)
        context = MatchContext(
            time=0,
            drivers={0: Driver(0, 0), 1: Driver(1, 10)},
            pending_riders=[
                Rider(0, origin=9, destination=8, arrival_time=0, max_wait_seconds=30),
                Rider(1, origin=1, destination=2, arrival_time=0, max_wait_seconds=30),
            ],
            distance_oracle=DistanceOracle(graph),
        )
        pairs = {(assignment.driver_id, assignment.rider_id) for assignment in MinCostBipartiteMatcher().match(context)}
        self.assertEqual(pairs, {(0, 1), (1, 0)})

    def test_batch_matcher_waits_until_interval_boundary(self):
        graph = nx.path_graph(4)
        context = MatchContext(
            time=3,
            drivers={0: Driver(0, 0)},
            pending_riders=[Rider(0, origin=1, destination=2, arrival_time=0, max_wait_seconds=30)],
            distance_oracle=DistanceOracle(graph),
        )
        self.assertEqual(BatchMatcher(interval_seconds=10).match(context), [])


if __name__ == "__main__":
    unittest.main()

