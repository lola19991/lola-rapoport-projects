import unittest

from ride_dispatch_optimizer.algorithms import GreedyNearestMatcher
from ride_dispatch_optimizer.arrivals import StochasticArrivalGenerator
from ride_dispatch_optimizer.entities import SimulationConfig
from ride_dispatch_optimizer.graph import make_grid_graph
from ride_dispatch_optimizer.simulator import RideDispatchSimulator


class SimulatorTests(unittest.TestCase):
    def test_simulator_runs_and_produces_consistent_counts(self):
        graph = make_grid_graph(5, 5)
        arrivals = StochasticArrivalGenerator(
            list(graph.nodes),
            base_rate=0.6,
            peak_multiplier=0.0,
            max_wait_seconds=15,
            seed=123,
        )
        simulator = RideDispatchSimulator(
            graph,
            GreedyNearestMatcher(),
            arrivals,
            n_drivers=8,
            config=SimulationConfig(horizon_seconds=40, max_wait_seconds=15, idle_move_probability=0.0),
            seed=456,
        )
        result = simulator.run()
        summary = result.summary()
        self.assertEqual(summary["arrivals"], summary["completed"] + summary["rejected"])
        self.assertEqual(len(result.timeline), 40)
        self.assertGreater(summary["arrivals"], 0)


if __name__ == "__main__":
    unittest.main()

