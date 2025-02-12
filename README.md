Based on the content of the optimization.pdf file, here's a suggested README.md for a GitHub repository:

# Thrust Bearing Optimization

This repository contains code and results for optimizing thrust bearing design using random search and simulated annealing algorithms.

## Overview

The project aims to minimize power loss under axial load conditions for a thrust bearing. It compares two optimization approaches:

1. Random search with death penalty
2. Simulated annealing with logarithmic cooling

Key features:
- Implements multiple constraint functions (g1-g7)
- Defines complex objective function f(x) with several sub-functions
- Includes statistical analysis of optimizer performance
- Provides visualization of results

## Files

- `optimization.py`: Contains main optimization functions and objective/constraint definitions
- `analysis.ipynb`: Jupyter notebook for results analysis and visualization
- `results/`: Directory containing output data from optimization runs

## Optimization Functions

### Objective Function
f(x) = (P0(x) * x[0]) / 0.7 + Ef(x)

### Constraints
- g1(x): Weight constraint
- g2(x): Pressure constraint
- g3(x): Temperature change constraint
- g4(x): Height constraint
- g5(x): Dimensional relationship
- g6(x): Geometric constraint
- g7(x): Stress constraint

## Results

After 21 repetitions of each algorithm:

Random Search:
- Best solution: [7.722, 5.116, 6.898, 6.816]
- Best objective value: 44355.434

Simulated Annealing:
- Optimal solution: [6.618, 15.261, 11.980, 9.980]
- Optimal energy: 1520.564

Statistical analysis (t-test):
- T-statistic: 0.4346
- P-value: 0.6685

No statistically significant difference between optimizers was found.

## Usage

1. Clone the repository
2. Install dependencies:
   ```
   pip install numpy matplotlib
   ```
3. Run optimization:
   ```python
   python optimization.py
   ```
4. Analyze results in Jupyter notebook:
   ```
   jupyter notebook analysis.ipynb
   ```
