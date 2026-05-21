from ride_dispatch_optimizer.experiments import run_policy_suite, write_results


if __name__ == "__main__":
    results, summary = run_policy_suite(
        horizon_seconds=300,
        n_drivers=30,
        grid_width=12,
        grid_height=12,
        base_rate=0.22,
        seed=7,
    )
    write_results(results, summary, "outputs/example")
    print(summary.round(3).to_string(index=False))

