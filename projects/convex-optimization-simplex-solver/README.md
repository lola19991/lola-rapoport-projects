# Convex Optimization: Simplex-Constrained Least Squares

Implementation of numerical methods for solving a constrained least-squares problem over the probability simplex:

```math
\min_x \|y - Hx\|_2 \quad \text{s.t.} \quad \mathbf{1}^T x = 1, \; x \ge 0.
```

## Methods

- Projected gradient descent using Euclidean projection onto the simplex.
- Interior-point Newton/log-barrier method.
- Condition-number-based selection between the two methods.

## Files

```text
simplex_solver.py
report.pdf
```

## Usage Example

```python
import numpy as np
from simplex_solver import solve

H = np.random.randn(20, 5)
y = np.random.randn(20)
x = solve(H, y)
print(x)
print(x.sum())
```
