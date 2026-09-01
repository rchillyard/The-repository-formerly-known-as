# Run results from Yunlu — string sorts on English, 2026-09-01

This answers `doc/Run request for Yunlu.md`. Both runs completed with the exact commands from the
request, at the requested commit. Raw JMH JSON outputs are committed alongside this file:

- `doc/english-baselines.json` — primary run (6 sorts, sampling with replacement)
- `doc/english-distinct.json` — secondary run (3 sorts, sampling without replacement)

## System environment

| | |
|---|---|
| Machine | AWS EC2 c7g.4xlarge (AWS Graviton3, ARM Neoverse V1, aarch64) |
| Cores | 16 physical cores, no SMT (1 thread/core) |
| Caches | L1d 64 KiB/core · L2 1 MiB/core · L3 32 MiB shared |
| Memory | 32 GiB |
| OS | Amazon Linux 2023, kernel 6.12.100 |
| JVM | OpenJDK 21.0.12, Corretto-21.0.12.8.1 (build 21.0.12+8-LTS) |
| Maven | 3.9.9 |

Dedicated cloud workstation, no desktop environment, no indexing or backup agents.
Load average before the runs: 0.13. Note this is still ARM64 like the M1, but a different
microarchitecture (server-class Neoverse V1, flat 16-core topology, no performance/efficiency split).

## Provenance and pre-checks

- Commit: `a83e7ea` on branch `Revisions` (subject verified: *"Let the string benchmark draw
  without replacement, and ask Yunlu for that too"*).
- `mvn test` at this commit: **378 tests, 0 failures, 0 errors** — clean, as advertised.
- Primary run wall time ≈ 56 min; secondary ≈ 22 min. No fork failures; every result row has
  Cnt = 50 (5 forks × 10 iterations). ± values below are JMH's 99.9% confidence half-intervals.
- Sanity check passed: `systemSort` is comfortably the slowest at every size (1.8–2.0× slower
  than the next-slowest sort).

Commands (verbatim from the request):

```
mvn -Pjmh package -DskipTests
java -jar target/benchmarks.jar "StringSortBenchmarks.(msdStringSort|multikeyQuicksort|radixHuskySort8|radixHuskySort11|radixHuskySort16|systemSort)$" -p corpus=english -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-baselines.json
java -jar target/benchmarks.jar "StringSortBenchmarks.(msdStringSort|multikeyQuicksort|radixHuskySort16)$" -p corpus=english -p sampling=distinct -p n=32000,200000,250000 -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff english-distinct.json
```

## Primary run — corpus=english, sampling=withreplacement (ms/op)

| n | MSD | radix/16 | radix/11 | radix/8 | multikey | system |
|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 4.054 ± 0.061 | 4.224 ± 0.283 | 4.487 ± 0.263 | 4.543 ± 0.218 | 7.000 ± 0.048 | 13.522 ± 0.121 |
| 200,000 | 39.300 ± 1.413 | 52.599 ± 2.608 | 51.917 ± 2.680 | 59.342 ± 2.100 | 73.176 ± 2.042 | 143.819 ± 1.592 |
| 1,000,000 | 258.536 ± 12.281 | 282.051 ± 4.206 | 312.620 ± 7.297 | 321.089 ± 6.483 | 637.568 ± 8.717 | 1166.176 ± 18.773 |

The ordering matches your sanity table, with one local difference: radix/11 and radix/16 are
statistically indistinguishable at 200,000 here (heavily overlapping intervals).

## Secondary run — corpus=english, sampling=distinct (ms/op)

| n | MSD | radix/16 | multikey |
|---:|---:|---:|---:|
| 32,000 | 4.191 ± 0.191 | 4.500 ± 0.259 | 7.402 ± 0.050 |
| 200,000 | 46.394 ± 2.323 | 60.829 ± 2.615 | 78.648 ± 0.755 |
| 250,000 | 63.399 ± 3.251 | 78.426 ± 2.704 | 101.534 ± 0.667 |

## Answers to the three questions

### 1. Does MSD really beat RadixHuskySort at 200,000 and 1,000,000?

**Yes at both sizes, with non-overlapping intervals — but the margin has a different shape here.**

| n | radix/16 ÷ MSD | intervals |
|---:|---:|---|
| 200,000 | **1.34×** | MSD [37.9, 40.7] vs radix/16 [50.0, 55.2] — clearly separated |
| 1,000,000 | **1.09×** | MSD [246.3, 270.8] vs radix/16 [277.8, 286.3] — separated, but thin |

On the M1 you saw 1.20–1.32× at 1M; on Graviton3 the 1M advantage shrinks to 1.09× while the
200k advantage grows to 1.34×. MSD's win is real on a second architecture, but its *size* is
machine-dependent — worth keeping in mind for how strongly the paper words the 1M row.

### 2. Are they genuinely tied at 32,000?

**Yes.** MSD 4.054 ± 0.061 vs radix/16 4.224 ± 0.283 — overlapping intervals. The tie
reproduces on a server-class ARM core with a completely different cache hierarchy, so it does
not look like an M1 cache artifact. (MSD does separate from radix/11 and radix/8 at this size.)

### 3. Real margin over repaired multikey quicksort?

multikey ÷ radix/16: **1.66× / 1.39× / 2.26×** at 32k / 200k / 1M.

The paper's 1.3–1.75× claim breaks at the top end on this machine (2.26× at 1M) — interestingly
the opposite end from your M1 numbers (2.0× at 32k). Between the two machines, the honest
statement is that the margin over multikey varies with size and machine from ~1.3× to ~2.3×.

## With-replacement vs distinct — the actual experiment

MSD's advantage over radix/16 at the sizes present in both runs:

| n | with replacement | distinct |
|---:|---:|---:|
| 32,000 | 1.04× (overlapping) | 1.07× (marginally overlapping) |
| 200,000 | **1.34×** | **1.31×** |

Removing duplicates leaves MSD's 200k advantage essentially unchanged (1.34× → 1.31×). This
agrees with your n=200,000 indicative result in direction — duplicates do **not** explain MSD's
advantage — though here the advantage stays flat rather than growing. At 250,000 distinct it is
1.24×. As expected, all three sorts get slower in absolute terms on fully-distinct data at
200,000 (MSD +18%, radix/16 +16%, multikey +7.5%).
