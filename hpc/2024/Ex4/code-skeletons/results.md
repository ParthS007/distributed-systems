# Task 1: MPI + OpenMP General Matrix Multiplication (GEMM)

## Test 1: 2 Ranks Configuration

```bash
# Configuration
Ranks: 2
Threads per Rank: 10
Total Threads: 20

# Workload Distribution
Rows per Rank: 2000
Rows per Thread: 200

Execution Time: 8.042278 seconds
```

## Test 2: 4 Ranks Configuration

```bash
# Configuration
Ranks: 4
Threads per Rank: 10
Total Threads: 40

# Workload Distribution
Rows per Rank: 1000
Rows per Thread: 100

Execution Time: 4.057506 seconds
```
## Result Analysis

- Each rank processed equal number of rows
- Speedup: `1.96x` (from `2` to `4` ranks)
- Efficiency: `98%` (nearly linear scaling)
- Time Reduction: `49%`

# Task 2: MPI + OpenMP Mandelbrot

## Test Configuration
```bash
# Configuration
Ranks: 4
Threads per Rank: 10
Total Threads: 40

# Workload Distribution
Total Pixels: 1,048,576 pixels
Pixels per MPI rank: ~262,144 pixels
Processing rate: ~330,587 pixels/second

Execution Time: 3.171854 seconds
```
