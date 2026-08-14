# Cross-machine crossover-N benchmark (for coauthors)

This is a self-contained recipe for reproducing the crossover-N sweep behind the paper's
Use-Case Guidance figure (`paper/HuskySort.tex`, `fig:usecase`) on a different machine, so we
have more than one machine's timings to compare. For general JMH usage in this repo (building
other benchmark classes, overriding parameters, etc.) see
[JMH Benchmarks.md](JMH%20Benchmarks.md) instead — this doc is just the one specific command.

## What this measures

Four sorters — plain System sort, binary-search-based `InsertionSort`, `QuickHuskySort`, and
`RadixHuskySort` (16-bit digits) — sorting random English words at a sweep of array sizes ($N$)
from 4 to 10,000. This is exactly the data that produced the crossover points shown in Figure 5
of the paper (System sort → InsertionSort → QuickHuskySort → RadixHuskySort as $N$ grows).

## Setup

```bash
git clone <repo-url> HuskySort   # or: cd HuskySort && git pull
cd HuskySort
git checkout Revisions
mvn -Pjmh clean package -DskipTests
```

This produces a self-contained `target/benchmarks.jar`.

## Run

```bash
java -jar target/benchmarks.jar \
  "StringSortBenchmarks\.(systemSort|insertionSort|quickHuskySort|radixHuskySort16)$" \
  -p corpus=english \
  -p n=4,10,20,50,100,200,500,1000,2000,5000,10000 \
  -rf json -rff usecase-benchmark-$(hostname -s).json
```

- `n` and `corpus` are the benchmark's own `@Param` fields (`StringSortBenchmarks.StringState`);
  no other flags are needed since fork/warmup/measurement counts (2 forks, 3×1s warmup, 5×1s
  measurement) are already set on the class itself.
- The benchmark regex was checked against the built jar (`-l` to list matches) to confirm it
  selects exactly these four methods and nothing else.
- The whole sweep (4 benchmarks × 11 sizes) should finish in well under 10 minutes.
- `-rff` embeds the machine's hostname in the output filename so results from different
  contributors don't collide if collected in one place.

## Before running

Check for background CPU contention first (`uptime`, `ps aux | sort -rn -k3 | head`) — a busy
machine will widen the confidence intervals enough to obscure the actual crossover points. This
came up directly during this benchmark's own development: Microsoft Teams and (on Robin's
machine specifically) a university-managed `LabStatsGoClient` process were both found
contributing real load during a supposedly idle run.

## Sending results back

Send the `usecase-benchmark-<hostname>.json` file back along with the machine's rough spec
(CPU, core count, OS) — that context matters for interpreting any difference in exactly where
the crossover points fall.
