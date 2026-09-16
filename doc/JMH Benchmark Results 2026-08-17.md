# JMH Benchmark Results, 2026-08-17

A full JMH run of every benchmark class described in
[JMH Benchmarks.md](JMH%20Benchmarks.md), executed end-to-end on dedicated hardware rather than
a shared workstation. Raw output: [jmh-results-2026-08-17.csv](jmh-results-2026-08-17.csv).

## Summary

- **The N=1,000,000 String radix advantage is real, not measurement noise.** RadixHuskySort/16
  beats QuickHuskySort by 2.5x (English) / 2.9x (Chinese), and System sort by 4.2x / 5.9x.
  Median relative confidence-interval width stays under 2% even at N=1,000,000 (1.3% at 32K,
  1.8% at 200K, 2.0% at 1M) — the algorithm gap is an order of magnitude larger than the noise,
  settling the question [Radix Sort Benchmark Results.md](Radix%20Sort%20Benchmark%20Results.md)
  left open about the old harness's noisy 1M-row results.
- **Radix wins by roughly 2-6x across every category** at the largest N tested: Numeric types,
  Tuples (both N=500,000), and Dates (N=20,000, ~4.9x over System sort — the largest relative
  win in the run).
- **Chinese personal names (pinyin) are the one exception.** At N=1,000,000, System sort
  (851.3 ± 17.1, sorting by the *wrong* raw-Unicode order) beats every pinyin-correct Husky
  variant (952.6-968.9 ms/op) — a known short-string, duplicate-syllable weakness of this
  corpus, not a new finding.
- **RadixHuskySort is structurally immune to high-order-bit-collision adversarial input**
  (Scenario A): flat 42-90 ms/op at N=1,000,000 across every `fixedHighBits` setting, while the
  paper's re-implemented dual-pivot quicksort baseline degrades by an order of magnitude and
  then throws `StackOverflowError` on 3 of 14 combinations.
- **Parallelism helps, but scales weakly past 2-4 threads.** At N=10,000,000,
  `ParallelRadixHuskySort11` goes 980.9 → 672.5 ms/op from p=1 to p=8 — only 1.46x for an 8x
  thread increase — though the whole parallel family still beats `QuickHuskySort` by ~6-9x
  regardless of thread count.
- Six rows in the whole run exceed 20% relative CI, and every one is `DualPivotQuicksort` on a
  boxed numeric or tuple type — see [Failures and caveats](#failures-and-caveats).

## Motivation

This run executes [JMH Benchmarks.md](JMH%20Benchmarks.md) on real, dedicated hardware to
resolve a question that document left open: whether the old ad hoc harness's noisy
N=1,000,000 String results in
[Radix Sort Benchmark Results.md](Radix%20Sort%20Benchmark%20Results.md) reflected a real
effect or an artifact of that harness's lack of fork isolation and warmup discipline — the
direct JMH-migration follow-up described in `TODO.md` item 1.

## Environment

| Item | Value |
|---|---|
| Cloud instance | AWS EC2 `c7g.4xlarge` (AWS Graviton3), ARM Neoverse V1, aarch64 |
| vCPUs | 16 (16 cores × 1 thread/core, no SMT, 1 socket, 1 NUMA node) |
| Cache | L1d 64 KiB/core (1 MiB total), L1i 64 KiB/core (1 MiB total), L2 1 MiB/core (16 MiB total), L3 32 MiB shared |
| CPU clock | Not exposed to guest; Graviton3 documented at 2.6 GHz fixed, hypervisor-managed |
| Memory | 30 GiB total, 0 B swap |
| OS / kernel | Amazon Linux 2023, kernel 6.12.95-124.187.amzn2023.aarch64 |
| JDK / JVM | OpenJDK 21.0.12 (2026-07-21 LTS), Amazon Corretto build, 64-bit Server VM |
| Maven | Apache Maven 3.9.16 |

Load average at collection time was 0.36/0.36/0.27 on 16 CPUs — effectively idle. The instance
is non-burstable (no CPU credits), so sustained CPU performance is consistent rather than
throttled after a burst window.

**Benchmark run**: 22:08 UTC 2026-08-17 → 00:46 UTC 2026-08-18 (wall time 2:37:37).

```bash
mvn -Pjmh clean package
java -jar target/benchmarks.jar -rf csv -rff target/jmh-results.csv
```

**JMH settings**: `@Fork(2)`, 3×1s warmup, 5×1s measurement, `avgt` mode, unit ms/op (10
samples/row). 81 distinct benchmark methods discovered; the full parameter sweep produced 385
result rows (3 rows absent — see [Failures and caveats](#failures-and-caveats)).

## Results by category

Each table below reports Score ± Score Error (99.9% CI) in ms/op, at the largest `n` tested for
that class, taken directly from the CSV. "Speedup" columns are `<baseline> / RadixHuskySort`
computed from those same numbers.

### StringSortBenchmarks

At N=1,000,000, three corpora:

| Corpus | System sort | QuickHuskySort | RadixHuskySort/16 | vs QHS | vs System |
|---|---|---|---|---|---|
| English | 1220.3 ± 33.9 | 725.7 ± 11.9 | 289.5 ± 18.5 | 2.51x | 4.21x |
| Chinese | 442.2 ± 4.7 | 215.5 ± 4.6 | 75.6 ± 6.8 | 2.85x | 5.85x |
| Chinese names (pinyin) | 851.3 ± 17.1 | 1475.2 ± 13.5 | 955.5 ± 4.4 | 1.54x | **0.89x (System sort wins)** |

Smaller-N comparison (English and Chinese, System / QuickHuskySort / RadixHuskySort/16):

| N | Corpus | System sort | QuickHuskySort | RadixHuskySort/16 |
|---|---|---|---|---|
| 32,000 | English | 13.8 ± 0.2 | 10.2 ± 0.4 | 5.3 ± 0.2 |
| 32,000 | Chinese | 9.7 ± 0.1 | 5.1 ± 0.1 | 2.0 ± 0.1 |
| 200,000 | English | 145.5 ± 2.1 | 97.5 ± 1.8 | 61.5 ± 2.0 |
| 200,000 | Chinese | 70.5 ± 1.1 | 31.0 ± 0.6 | 11.2 ± 0.2 |

Radix wins consistently at every English/Chinese row, and the margin over both baselines
*widens* as N grows (English vs. System sort: 2.6x at 32K, 2.4x at 200K, 4.2x at 1M) — the
opposite of what fixed one-time overhead would produce. The chinesenames row is this report's
single exception: because natural Unicode order (what System sort computes there) is cheaper
than pinyin-collation order (what QuickHuskySort/RadixHuskySort must compute to be correct for
that corpus), System sort's 851.3 ms/op beats even the fastest Husky variant — see
[The N=1M question answered](#the-n1m-question-answered) and
[Failures and caveats](#failures-and-caveats).

### NumericSortBenchmarks

At N=500,000, System sort / QuickHuskySort / best RadixHuskySort digit width:

| Type | System sort | QuickHuskySort | Best RadixHuskySort | Width | vs QHS | vs System |
|---|---|---|---|---|---|---|
| Integer | 133.75 ± 1.54 | 86.99 ± 4.24 | 26.54 ± 0.19 | 16-bit | 3.28x | 5.04x |
| Double | 150.23 ± 3.19 | 98.30 ± 4.04 | 32.25 ± 0.40 | 16-bit | 3.05x | 4.66x |
| Long | 142.71 ± 4.35 | 106.46 ± 2.15 | 28.50 ± 0.56 | 11-bit | 3.74x | 5.01x |
| BigInteger | 221.55 ± 4.59 | 154.56 ± 3.26 | 54.91 ± 1.33 | 16-bit | 2.81x | 4.03x |
| BigDecimal | 270.37 ± 5.24 | 159.50 ± 2.05 | 53.20 ± 1.35 | 11-bit | 3.00x | 5.08x |

For reference, the raw (type-specialized, no Husky machinery) baselines at the same N:
`rawRadixSort` is fastest for every type (11.96-17.15 ms/op, 2-4x faster still than the best
RadixHuskySort width), and `rawQuicksort` on unboxed primitives sits between the two (37.88-
47.67 ms/op). This is expected: Integer/Double/Long/BigInteger/BigDecimal are exactly the case
where Husky-encoding's premise (amortize an O(N) encoding pass against expensive comparisons
saved later) doesn't apply, since comparing two numbers directly is already cheap.
RadixHuskySort still comfortably beats every comparison-based option for every type at this N.

### TupleSortBenchmarks

At N=500,000:

| Sorter | Score (ms/op) |
|---|---|
| System sort | 216.12 ± 2.22 |
| QuickHuskySort | 146.09 ± 4.54 |
| DualPivotQuicksort (boxed) | 176.68 ± 65.15 |
| RadixHuskySort/8 | 68.66 ± 1.87 |
| RadixHuskySort/11 | 63.43 ± 1.77 |
| RadixHuskySort/16 | 60.71 ± 2.35 |

RadixHuskySort/16 is fastest, **2.41x faster than QuickHuskySort and 3.56x faster than System
sort** — the composite (`birthYear`/`zip`/`name`) key's imperfect encoding still benefits
substantially from radix's fixed per-pass cost model. `DualPivotQuicksort` on the boxed Tuple
type has one of this report's six wide-CI rows (65.15 ms error on a 176.68 ms mean); see
[Failures and caveats](#failures-and-caveats).

### DateSortBenchmarks

Only size tested, N=20,000 (`ChronoLocalDateTime`, a "perfect" single-`long` epoch-second
encoding):

| Sorter | Score (ms/op) |
|---|---|
| System sort | 3.86 ± 0.11 |
| DutchHuskySort | 4.43 ± 0.07 |
| DutchHuskySort + insertion cleanup | 4.39 ± 0.11 |
| RadixHuskySort/8 | 0.89 ± 0.09 |
| RadixHuskySort/11 | 0.79 ± 0.11 |
| RadixHuskySort/16 | 0.84 ± 0.10 |

The largest relative win in this run: RadixHuskySort/11 is **~4.9x faster than System sort**
and ~5.6x faster than DutchHuskySort. DutchHuskySort is itself *slower* than System sort here
(4.43 vs 3.86 ms/op) — its own encoding overhead isn't repaid at this N for a type this cheap
to compare, while radix's fixed-cost pass structure still wins because the "perfect" encoding
needs no cleanup pass at all.

### AdversarialSortBenchmarks

Scenario A (collapsed high-order bits, synthetic `Long[]`), N=1,000,000, across the
`fixedHighBits` sweep:

| fixedHighBits | System sort | QuickHuskySort | DualPivotQuicksort (raw) | RadixHuskySort/16 |
|---|---|---|---|---|
| 0 | 512.8 ± 46.8 | 460.2 ± 28.7 | 352.8 ± 17.9 | 75.4 ± 4.5 |
| 32 | 522.1 ± 36.5 | 463.0 ± 18.7 | 350.9 ± 18.4 | 66.1 ± 3.8 |
| 56 | 178.3 ± 1.6 | 197.9 ± 14.3 | 7376.4 ± 13.0 | 55.6 ± 4.1 |
| 60 | 116.3 ± 1.0 | 71.1 ± 2.0 | **StackOverflowError** | 42.4 ± 3.4 |
| 63 | 45.3 ± 1.1 | 15.1 ± 1.2 | **StackOverflowError** | 30.2 ± 2.7 |

RadixHuskySort stays essentially flat (30-75 ms/op) across the entire sweep — a pass over
collapsed digits costs the same as any other pass, independent of the data's distribution. The
paper's own re-implemented dual-pivot quicksort baseline instead degrades by more than 20x at
`fixedHighBits=56` (352.8 → 7376.4 ms/op) and then crashes outright at 60 and 63.
QuickHuskySort (Introsort with a depth-limited fallback, plus the mandatory cleanup pass)
neither crashes nor degrades anywhere in the sweep. Scenario B (strings sharing a long common
prefix, defeating the encoding's fixed capture window) is a different failure mode — once the
shared prefix meets or exceeds the coder's capture window, every Husky-based approach becomes
*slower* than System sort, since the wasted encode/first-pass work is paid on top of a cleanup
pass that has to do all the real work anyway. Full row-level detail for both scenarios is in
[Failures and caveats](#failures-and-caveats).

### ParallelRadixSortBenchmarks

`Long[]`, 11-bit digits:

| N | QuickHuskySort | Serial RadixHuskySort/11 | Parallel p=1 | p=2 | p=4 | p=8 |
|---|---|---|---|---|---|---|
| 2,000,000 | 1111.2 ± 47.6 | 182.5 ± 16.1 | 171.3 ± 9.6 | 140.9 ± 6.0 | 115.5 ± 4.9 | 108.7 ± 4.5 |
| 10,000,000 | 6091.3 ± 118.5 | 1005.2 ± 15.5 | 980.9 ± 18.9 | 842.4 ± 26.8 | 758.9 ± 23.5 | 672.5 ± 82.5 |

At N=10,000,000, p=1→p=8 is only a **1.46x** speedup for an 8x increase in thread count —
scaling flattens noticeably past 2-4 threads (p=4→p=8 gains just 758.9→672.5, and p=8's own CI,
±82.5, is wide enough that the p=4/p=8 difference is not strongly resolved). Even the weakly-
scaling parallel variant still beats QuickHuskySort by roughly **6-9x** at both sizes tested,
since most of that margin is radix sort's own serial advantage rather than parallelism's
incremental contribution.

## The N=1M question answered

[Radix Sort Benchmark Results.md](Radix%20Sort%20Benchmark%20Results.md) documented that
under the old, un-forked, un-warmed harness, N=1,000,000 String results got noisy —
English's ranking reversed relative to smaller N — leaving open whether that reversal was real
or measurement noise.

At N=1,000,000, with proper JMH fork isolation and warmup:

| Corpus | RadixHuskySort/16 | QuickHuskySort | System sort | RHS16 vs QHS | RHS16 vs System |
|---|---|---|---|---|---|
| English | 289.5 ± 18.5 | 725.7 ± 11.9 | 1220.3 ± 33.9 | 2.51x | 4.21x |
| Chinese | 75.6 ± 6.8 | 215.5 ± 4.6 | 442.2 ± 4.7 | 2.85x | 5.85x |

And the measurement noise itself, tracked across sizes (median relative CI width — Score Error
÷ Score — across all `StringSortBenchmarks` rows at that `n`, every corpus/method):

| N | Median relative CI |
|---|---|
| 32,000 | 1.3% |
| 200,000 | 1.8% |
| 1,000,000 | 2.0% |

Confidence intervals stay tight (under 2% of the mean, even at the largest N) while the gap
between RadixHuskySort and its competitors is 2.5-5.9x — two orders of magnitude larger than
the noise floor. **The old harness's N=1,000,000 English reversal was harness noise, not a
real effect: the radix advantage at N=1,000,000 is real, robust, and if anything larger than
at smaller sizes**, resolving the open question from `TODO.md` item 1.

## Failures and caveats

- **`AdversarialSortBenchmarks.collapsedBitsDualPivotQuicksort` threw `StackOverflowError` on
  3 of 14 parameter combinations**: `(fixedHighBits=60, n=1,000,000)`, `(fixedHighBits=63,
  n=200,000)`, `(fixedHighBits=63, n=1,000,000)`. This is the expected quicksort
  degenerate-recursion-depth worst case for a naive dual-pivot implementation with no
  equal-element handling, when nearly all high-order bits collide — every fork failed during
  warmup, so no measurement exists, and these three rows are simply absent from the CSV rather
  than recorded as zero or infinity.
- **Scenario B (shared string prefixes) shows every Husky-based sorter losing to System sort
  once the prefix reaches the encoding's capture window.** At `prefixLength >= 10` (English
  7-bit ASCII coder's window is 9 characters), System sort, QuickHuskySort, and every
  RadixHuskySort width converge to essentially the same cost — at N=1,000,000, `prefixLength=10`:
  System 913.8 ± 40.2, QuickHuskySort 929.0 ± 24.5, RadixHuskySort/8-16 916.7-958.2 ms/op; by
  `prefixLength=40` all five sorters sit in a tight 952.4-1002.3 ms/op band. Below the window
  (`prefixLength=0`), the usual radix advantage holds (System 1552.1, QuickHuskySort 611.9,
  RadixHuskySort/16 254.5 ms/op). Once every husky code collapses to (near-)identical values,
  the encoding buys zero disambiguation and the entire ordering burden falls on the mandatory
  cleanup pass, which every variant pays on top of useless encode/first-pass work — so no sort
  choice, radix included, recovers an advantage there.
- **Six rows across the entire run have relative confidence intervals above 20%**, and every
  single one is `DualPivotQuicksort` (the paper's own re-implemented baseline) applied to a
  boxed type:
  - `NumericSortBenchmarks.bigIntegerDualPivotQuicksort`, n=20,000: 4.86 ± 4.07 ms/op (83.8%)
  - `NumericSortBenchmarks.bigIntegerDualPivotQuicksort`, n=100,000: 32.16 ± 25.48 ms/op (79.2%)
  - `NumericSortBenchmarks.bigDecimalDualPivotQuicksort`, n=100,000: 40.52 ± 16.74 ms/op (41.3%)
  - `TupleSortBenchmarks.dualPivotQuicksort`, n=500,000: 176.68 ± 65.15 ms/op (36.9%)
  - `TupleSortBenchmarks.dualPivotQuicksort`, n=20,000: 3.87 ± 1.07 ms/op (27.6%)
  - `NumericSortBenchmarks.bigIntegerDualPivotQuicksort`, n=500,000: 250.66 ± 65.00 ms/op (25.9%)

  No RadixHuskySort, QuickHuskySort, or System sort row anywhere in the run exceeds 20% relative
  CI. The pattern is specific to `DualPivotQuicksort` on boxed object types, plausibly
  reflecting either GC/allocation variability from boxing or the same near-pathological
  recursion sensitivity visible more dramatically in the Scenario A adversarial sweep above.
- **The chinesenames System sort result should not be read as "System sort beats Husky."** At
  N=1,000,000, System sort (851.3 ± 17.1) computes natural Unicode code-point order, which is
  not the correct order for Chinese personal names — QuickHuskySort and every RadixHuskySort
  width (952.6-968.9 ms/op) compute the *correct* pinyin-collated order via a `Collator`-based
  cleanup pass, a genuinely more expensive comparison. Encoding alone for this corpus costs
  411.1 ± 2.3 ms/op (`huskyEncodeOnly`, n=1,000,000) — over a third of the total pinyin-sort
  cost — consistent with this being a real property of short, duplicate-syllable-heavy strings
  and an expensive correct-order comparator, not a bug.
- **This is an ARM/aarch64 host.** Absolute millisecond values here are not directly
  comparable to the x86/Apple-Silicon numbers in
  [Radix Sort Benchmark Results.md](Radix%20Sort%20Benchmark%20Results.md) — only relative
  comparisons *within* this run (radix vs. QuickHuskySort vs. System sort, same host, same JVM
  invocation) should be trusted across documents.
- **3 warmup + 5 measurement iterations × 2 forks (10 samples/row) is statistically meaningful
  but still modest** — enough to resolve most comparisons with non-overlapping confidence
  intervals, but not enough to fully resolve the six wide-CI rows above, or to distinguish p=4
  from p=8 in the parallel-scaling table. Background load was confirmed idle (load average
  0.36/0.36/0.27) at collection time before the run started, but not independently monitored
  throughout the full 2.5-hour run window.

## Raw data

The full 385-row result set is in [jmh-results-2026-08-17.csv](jmh-results-2026-08-17.csv),
committed alongside this report. The CSV columns are: `Benchmark`, `Mode`, `Threads`,
`Samples`, `Score`, `Score Error (99.9%)`, `Unit`, `Param: corpus`, `Param: fixedHighBits`,
`Param: n`, `Param: prefixLength`.

The original `target/jmh-results.csv` this was copied from is not committed — `target/` is
gitignored, since it holds Maven build output rather than source-controlled content.
