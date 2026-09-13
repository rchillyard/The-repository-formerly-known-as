# Run results from Yunlu — request 9, `Arrays.parallelSort` as a baseline, 2026-09-13

Request 9 answered: all three runs done on one machine and one build, 3 forks each as asked, raw JMH outputs committed
alongside (see Files), plus a second full run of step 2 — the step you asked to hear about before the 15th. **Headline: at
2,000,000 `Long`s, `Arrays.parallelSort` beats `ParallelRadixHuskySort` — 90.1 ± 5.2 vs 112.8 ± 2.5 ms/op for p8 (p4 118.9),
so the best husky parallel row is 1.25× *slower*, bars disjoint; at 10,000,000 the order reverses and p8 (644.4 ± 32.9)
beats `Arrays.parallelSort` (765.1 ± 11.3) by 1.19× (p4 by 1.14×), bars disjoint. The repeat reproduces both verdicts (p8
1.27× slower at 2M, 1.16× faster at 10M, bars disjoint both times); only the p4 margin at 10M shrinks to 1.06× with
overlapping bars.** On the permits the pre-registered picture does not hold on sixteen cores: at the full corpus
`Arrays.parallelSort` (13.1 ± 0.2) is 2.35× faster than serial radix/16 (30.9 ± 1.2), 2.49× faster than p4 (32.7 ± 2.4) and
2.77× faster than p8 (36.4 ± 0.5); and at no n is the parallel husky path faster than its own serial radix/16 — p8 is slower
at every n with disjoint bars, p4 is slower at 32,000 and level within the error bars at 100,000 and 198,900. On strings,
`Arrays.parallelSort` beats serial radix/16 by **5.17× at 200k and 2.98× at 1M** — far more than the 1.20x seen on eight.

**Checkout**: branch tip `6c3dcb8`. Request 9 names no freeze commit; the last commit touching `src/` is **`5a34af8`**
(2026-09-12, "Put the permits in the parallel bakeoff") — that is the commit the paper should record. `git log
5a34af8..6c3dcb8 -- src/` is empty (doc/paper/TODO only after it). No sort code under `src/main` changed since the 09-06
runs (`e92610f`); only benchmark methods were added.

**Environment**: same instance and pins as 09-06 — c7g.4xlarge (Graviton3 / Neoverse V1, 16 cores, 1 thread per core, 30
GiB), Corretto **21.0.12+8-LTS** (pinned), Maven **3.9.16**, AL2023, kernel **6.12.103** (unchanged since 09-06). `mvn test`
at the tip: **396 tests, 0 failures**. The common `ForkJoinPool` behind `Arrays.parallelSort` has **15 workers on 16
processors** in the benchmark slice: after the runs, `systemd-run --user --slice=app.slice --wait --pipe --collect
$JDK/bin/java Par.java` (the pinned JDK; `Par` prints `ForkJoinPool.commonPool().getParallelism()` and
`Runtime.getRuntime().availableProcessors()`) printed `15 16`; the same probe from an interactive shell, under this host's
14-core `kiro.slice` quota, prints `13 14` — the JVM sizes the pool from the cgroup CPU quota, so a quota-limited or
containerised reproduction moves every `parallelSort` column and no fixed-thread husky row (no p15/p16 husky row was
requested or run). Runs were strictly sequential in detached user units in `app.slice` (`cpu.max` = `max` all the way up).
Launched 07:55:22Z at load average 0.50 / 0.50 / 0.40; wall clock 1 h 24 m 09 s (steps 1–3) + 26 m 40 s (repeat, launched
09:55:10Z). Drift to disclose:

- **3 forks × 10 iterations** (`-f 3 -wi 5 -i 10 -r 2s -w 2s`, per the request) instead of the 5 forks of requests 1–7, so
  every row has Cnt=30 and the ± bars (JMH 99.9% CI) are somewhat wider.
- **The host was not empty.** Other sessions held ~21 GiB RAM and 3.5 GiB of zram swap before launch (09-06: 0 B swap used).
  Swap in use grew from 3,566 to 6,308 MiB between the 08:08:24Z and 08:10:25Z samples — ≈1.5–3.5 min into step 2, the
  window of the 10M p1 config (forks started 08:08:22 / 08:09:03 / 08:09:41Z; fork RES 6.7 GiB at the 08:09:25Z sample) —
  stepped +88 MiB during the 10M p8 config (6,296 → 6,384 MiB, 08:18:26Z → 08:19:27Z), and was otherwise flat (6,354 MiB at
  the end of step 3); the largest fork RES seen, 7.5 GiB, was at 08:12:25Z inside the 10M p2 config. Whose pages were
  evicted the monitor cannot tell (see step 2); steps 1 and 3 ran outside the growth. The repeat ran on the same loaded host
  — 5,879 MiB of swap in use at launch, +2,028 MiB during the run (to 7,907; max 7,920), again inside its 10M p1 window — so
  it is a reproducibility check, not a clean-host measurement.
- **Background load**: 84 one-minute samples, highest single non-benchmark process median 6.2% / max 70.6% of one core;
  three single-sample bursts — `unison` 70.6% at 07:56:22Z (step 1, minute 1) and 68.8% at 09:14:37Z (step 3, ~90%),
  `systemd` 50.0% at 08:10:25Z (step 2, the sample closing the swap growth) — none sustained. Repeat: 27 samples, load
  average median 1.84 / max 13.50, highest non-benchmark process median 6.2% / max 58.8% (`unison`, two single samples).

Request 8, step 0 is **not runnable here**: the check printed `command -v perf: not found (no perf binary on PATH); perf
stat not run.`, and there is no `perf` under `/usr/bin` or `/usr/sbin` either. Per your note that step 0 should only be
tried if quick, I stopped there; installing `perf` is post-deadline, and request 8 remains open.

## Correctness was checked before any timing was trusted

The external `ValidateSorts` harness (the 09-06 one: reuses the repo's classes and each benchmark's exact input generation
and seeds) was extended for the new methods and run against the same jar — **102/102 checks passed** (36 pre-existing in
sections A–E — the 09-06 doc's "37/37" over-counted by one, nothing was removed — + 66 new; 37 s). **F1 permits** (n =
32,000 / 100,000 / 198,900; Fisher–Yates `Random(42)` shuffle of the corpus, prefix n): `Arrays.parallelSort`,
`ParallelRadixHuskySort` p4 and p8 (`digitBits=16`, `PermitCoder`) and `radixHuskySort16` each **sorted under
`Permit.compareTo`**, a **permutation**, **order-equal to `Arrays.sort`**. **F2 english** (n = 200,000 / 1,000,000;
`Random(42)` with replacement): `Arrays.parallelSort` and `radixHuskySort16` sorted, permutation, **`equals` positionwise vs
`Arrays.sort`**. **F3 `Long[]`** (n = 2,000,000 / 10,000,000; `Random(43)`): `Arrays.parallelSort`, p8 (`digitBits=11`) and
`serialRadixHuskySort11` sorted, permutation, equal to `Arrays.sort` (10M under `-Xmx8g`). Each JSON passed structural
validation, **17/17 checks × 4 files** (exact (method, params) row set — 6 / 14 / 30 / 14 rows — Cnt=30 every row, `rawData`
3 × 10, no null/NaN, ms/op, forks/wi/i = 3/5/10, no JVM flags); the JMH logs hold no warning, error or exception.

## Step 1 — english strings: `Arrays.sort` vs `Arrays.parallelSort` vs serial radix/16 (ms/op)

`java -jar target/benchmarks.jar "StringSortBenchmarks.(systemSort|systemSortParallel|radixHuskySort16)$"
-p corpus=english -p n=200000,1000000 -f 3 -wi 5 -i 10 -r 2s -w 2s -rf json -rff parallelsort.json`

| n | systemSort | systemSortParallel | radixHuskySort16 | systemSort ÷ parallelSort | parallelSort ÷ RHSort16 (>1 = parallelSort slower) | systemSort ÷ RHSort16 |
|---:|---:|---:|---:|---:|---:|---:|
| 200,000 | 146.9 ± 3.4 | 9.94 ± 0.50 | 51.4 ± 3.1 | 14.78× | 0.19× | 2.86× |
| 1,000,000 | 1244.9 ± 18.4 | 92.8 ± 2.5 | 276.9 ± 7.2 | 13.41× | 0.34× | 4.50× |

**E1 — you wrote "We expect `Arrays.parallelSort` to beat serial RHSort at both sizes on sixteen cores, by more than the
1.20x seen on eight." It does, by far more**: `Arrays.parallelSort` is **5.17× faster at 200k and 2.98× faster at 1M** than
serial radix/16 (51.4 ÷ 9.94, 276.9 ÷ 92.8; bars nowhere near overlapping); its own speed-up over `Arrays.sort`, 14.78× /
13.41× on 15 pool workers plus the caller, is just under one-per-thread. Cross-run: radix/16 agrees with 09-05/09-06 within
2.5% (51.4 vs 50.6/50.3, +1.7% / +2.3%; 276.9 vs 271.5/272.7, +2.0% / +1.5%); **systemSort@1M does not — 1244.9 vs
1124.9/1120.9, +10.7% / +11.1%** (200k: 146.9 vs 140.7/141.4, +4.4% / +3.9%; cause undetermined). The E1 factor uses
neither. The 4.50× systemSort ÷ radix/16 at 1M in the last column inherits that +11% in its numerator — **do not let it
replace the paper's 4.11× (09-06) / 4.14× (09-05) figure.** `systemSortParallel` has no prior here beyond your M1 probe (2
forks × 5 iters at 1M), whose direction holds.

## Step 2 — `Long[]`: ParallelRadixHuskySort vs `Arrays.parallelSort` (the one that settles it)

`java -jar target/benchmarks.jar "ParallelRadixSortBenchmarks" -f 3 -wi 5 -i 10 -r 2s -w 2s -rf json -rff
parallel-vs-parallel.json` (sizes: the `@Param` defaults; > 1 in the last three columns = `Arrays.parallelSort` slower).

| n | serial RHSort11 | p1 | p2 | p4 | p8 | quickHuskySort | systemSortParallel | serial ÷ p8 (scaling) | parallelSort ÷ p8 | parallelSort ÷ p4 | parallelSort ÷ serial |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 2,000,000 | 172.8 ± 7.7 | 168.7 ± 3.3 | 140.7 ± 1.6 | 118.9 ± 2.2 | 112.8 ± 2.5 | 1075.8 ± 10.9 | 90.1 ± 5.2 | 1.53× | 0.80× | 0.76× | 0.52× |
| 10,000,000 | 932.4 ± 35.7 | 990.9 ± 111.7 | 751.0 ± 54.9 | 670.6 ± 30.4 | 644.4 ± 32.9 | 5621.4 ± 40.4 | 765.1 ± 11.3 | 1.45× | 1.19× | 1.14× | 0.82× |

**E2 — you wrote "We expect ParallelRadixHuskySort to beat `Arrays.parallelSort` ... If it does not, we need to know before
the 15th rather than from a referee." At 2M it does not**: `Arrays.parallelSort` 90.1 ± 5.2 vs p8 112.8 ± 2.5 — **p8 is
1.25× slower** (112.8 ÷ 90.1; p4 1.32× slower), bars disjoint (95.3 vs 110.3). **At 10M it does**: p8 644.4 ± 32.9 vs
parallelSort 765.1 ± 11.3 — **p8 1.19× faster** (765.1 ÷ 644.4), p4 1.14× faster, bars disjoint (677.3 / 701.0 vs 753.8); p2
(751.0 ± 54.9) overlaps parallelSort. Serial→p8 scaling: **1.53× @2M, 1.45× @10M** (09-06: 1.57× / 1.45×). p1 vs serial:
168.7 vs 172.8 @2M (level); 990.9 ± 111.7 vs 932.4 @10M is the only step-2 row outside 5% of 09-06 (884.5 ± 21.6, +12.0%).
Memory pressure rather than the code is the likely cause, but it is not proven: the 10M p1 config is exactly the window in
which swap grew by 2,742 MiB, and its fork 1 has iterations up to 1524.5 ms/op against a ~890 floor; the next 10M config, p2
(751.0 ± 54.9; 09-06 731.6 ± 19.9, +2.7%), ran after the growth and still has a 1092.0 ms/op iteration in fork 2 and a CI
2.8× the prior's; swap stepped +88 MiB during the 10M p8 config without moving its score (644.4, −0.1% vs 09-06). The
comparison is 15 pool workers vs 8 threads. Cross-run vs 09-06 (2M / 10M): serial +0.5% / −0.3% (172.0 / 935.2), p1 +2.1% /
+12.0% (165.2 / 884.5), p2 +0.3% / +2.7% (140.3 / 731.6), p4 +0.3% / −2.5% (118.5 / 687.9), p8 +3.2% / −0.1% (109.3 /
645.3), quickHuskySort +4.9% / −3.3% (1025.7 / 5811.1); `systemSortParallel` has no prior.

### Step 2 repeated (`parallel-vs-parallel-repeat.json`; same command and jar, 09:55:10Z → 10:21:50Z)

| n | serial RHSort11 | p1 | p2 | p4 | p8 | quickHuskySort | systemSortParallel | serial ÷ p8 (scaling) | parallelSort ÷ p8 | parallelSort ÷ p4 | parallelSort ÷ serial |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 2,000,000 | 169.0 ± 7.7 | 168.6 ± 3.5 | 142.7 ± 1.5 | 124.4 ± 3.1 | 112.9 ± 2.9 | 1088.7 ± 16.3 | 88.7 ± 3.2 | 1.50× | 0.79× | 0.71× | 0.52× |
| 10,000,000 | 921.5 ± 40.5 | 935.2 ± 70.5 | 743.7 ± 35.8 | 705.9 ± 49.4 | 643.6 ± 27.7 | 5644.3 ± 30.6 | 748.7 ± 7.2 | 1.43× | 1.16× | 1.06×† | 0.81× |

† = the two ± bars overlap. Per row vs the primary run (2M / 10M): serial −2.2% / −1.2%, p1 −0.1% / −5.6%, p2 +1.4% / −1.0%,
p4 +4.6% / +5.3%, p8 +0.0% / −0.1%, quickHuskySort +1.2% / +0.4%, systemSortParallel −1.6% / −2.1%; 13 of 14 rows have
overlapping CIs, the exception is p4@2M (118.9 ± 2.2 vs 124.4 ± 3.1). **Both E2 verdicts reproduce**: at 2M p8 is 1.27×
slower than `Arrays.parallelSort` (112.9 ± 2.9 vs 88.7 ± 3.2; p4 1.40×), bars disjoint (110.0 vs 91.9); at 10M p8 is 1.16×
faster (643.6 ± 27.7 vs 748.7 ± 7.2), bars disjoint (671.3 vs 741.5) — but p4 (705.9 ± 49.4) is now only 1.06× faster with
overlapping bars (755.3 vs 741.5), so the 10M p4 win is not established. The 10M p1 row came back at 935.2 ± 70.5 (+5.7% vs
09-06, −5.6% vs the primary), again with a 1237.0 ms/op fork maximum and again in the window where swap grew (+1,905 MiB,
09:56:10Z → 09:59:10Z samples); the 10M p2 row was 743.7 ± 35.8 with nothing above 872.8; this time the 10M p4 config took
the later swap step (+120 MiB, 10:03:11Z → 10:05:12Z) and carries the outlier (a 1002.4 ms/op iteration). The 10M p1 (−5.6%)
and p4 (+5.3%) rows are the two outside 5% of the primary, each inside a swap-step window; quote p1–p4 at 10M with their
CIs.

## Step 3 — the permits (ms/op)

`java -jar target/benchmarks.jar "PermitSortBenchmarks" -f 3 -wi 5 -i 10 -r 2s -w 2s -rf json -rff permits-parallel.json`
(`@Param n = 32000, 100000, 198900`; 198,900 is the whole corpus; > 1 in the first three ratio columns =
`Arrays.parallelSort` slower; the JSON's other rows are in the cross-run table).

| n | systemSort | systemSortParallel | radixHuskySort16 | p4 | p8 | quickHuskySort | dualPivot | parallelSort ÷ RHSort16 | parallelSort ÷ p4 | parallelSort ÷ p8 | systemSort ÷ parallelSort |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 13.6 ± 0.1 | 4.62 ± 0.03 | 2.72 ± 0.09 | 3.34 ± 0.07 | 3.42 ± 0.07 | 6.39 ± 0.05 | 12.2 ± 0.0 | 1.69× | 1.38× | 1.35× | 2.95× |
| 100,000 | 62.1 ± 1.2 | 6.13 ± 0.08 | 14.8 ± 0.9 | 16.0 ± 1.1 | 16.6 ± 0.5 | 29.2 ± 0.9 | 54.0 ± 2.2 | 0.41× | 0.38× | 0.37× | 10.12× |
| 198,900 | 152.1 ± 2.8 | 13.1 ± 0.2 | 30.9 ± 1.2 | 32.7 ± 2.4 | 36.4 ± 0.5 | 70.9 ± 3.5 | 139.3 ± 4.4 | 0.42× | 0.40× | 0.36× | 11.58× |

**E3 — you wrote "If that holds up, the parallel husky sort is ahead of `Arrays.parallelSort` on this corpus while serial
RHSort is level with it". It does not hold up on sixteen cores — the M1 direction inverts.** At 198,900,
`Arrays.parallelSort` (13.1 ± 0.2) is **2.35× faster than serial radix/16, 2.49× faster than p4 and 2.77× faster than p8**;
every bar is disjoint from parallelSort's (13.3 vs 29.7 at the nearest), and at 100k serial radix/16 is not level either
(0.41×). The husky sorts beat `Arrays.parallelSort` only at 32,000 (radix/16 1.69×, p4 1.38×, p8 1.35×), so on this corpus
the crossover lies somewhere between 32,000 and 100,000 — only those three n were measured. **At no n is the parallel husky
path faster than serial radix/16**: p8 is slower at every n with disjoint bars (3.42 vs 2.72 at 32k; 16.6 vs 14.8 at 100k;
36.4 vs 30.9 at 198,900, 1.18× slower, and slower than p4 — 35.8 vs 35.0 at the nearest bar edges); p4 is 1.23× slower at
32k (3.34 vs 2.72, disjoint) and level within the error bars at 100k and 198,900 (16.0 ± 1.1 vs 14.8 ± 0.9; 32.7 ± 2.4 vs
30.9 ± 1.2). The serial-vs-serial result is unchanged: systemSort ÷ radix/16 = 4.92× at the full corpus (09-05: 4.83×;
09-06: 4.53×). p4, p8 and `systemSortParallel` have no prior here; the M1 permits probe (1 fork × 3) is direction only.

One reading aid: `ParallelRadixHuskySort` splits each pass into `chunks = max(1, min(parallelism, n / MIN_CHUNK_SIZE))` with
`MIN_CHUNK_SIZE = 1 << 14` (`ParallelRadixHuskySort.java:62`, `:117`). So at n = 32,000 both p4 and p8 run **one chunk** —
the same single-worker path (their rows agree: 3.34 ± 0.07 vs 3.42 ± 0.07); at 100,000 p4 uses 4 chunks and p8 only 6; **p8
reaches 8 threads only at 198,900** (~24,860 elements per chunk). The fixed thread pool is created inside the timed call
(`:118`), so pool start-up is inside every measurement. Hypothesis, not measured: with `digitBits=16` each permits chunk
histograms 65,536 buckets per pass — more buckets than elements — against `digitBits=11` (2,048 buckets) on
250,000–1,250,000-element `Long[]` chunks; bookkeeping scaling with chunks × buckets rather than n would fit the permits
losing where 2M–10M gain.

## Cross-run agreement on the permits (this run vs 09-05 / 09-06, both 5 forks; Δ% on point estimates)

Cells read "09-05 / 09-06 → this run (Δ vs 09-05 / Δ vs 09-06)". Over 5%: radix/16 @32k **+6.2%** vs 09-05 and @198,900
**−7.3%** vs 09-06 — the two priors disagree with each other by about as much there — plus, not requested, radix/8 @100k
−7.5%, @198,900 −6.1%, quickHuskySortWithCleanup @100k −5.5%. Every other permits row is within 4.9% of both priors.

| method | 32,000 | 100,000 | 198,900 |
|---|---|---|---|
| systemSort | 13.7 / 13.3 → 13.6 (−0.5% / +2.2%) | 62.0 / 60.2 → 62.1 (+0.1% / +3.1%) | 149.4 / 150.9 → 152.1 (+1.8% / +0.8%) |
| radixHuskySort16 | 2.56 / 2.82 → 2.72 (+6.2% / −3.5%) | 14.3 / 14.1 → 14.8 (+3.7% / +4.9%) | 31.0 / 33.3 → 30.9 (−0.2% / −7.3%) |
| radixHuskySort11 | 2.60 / 2.58 → 2.61 (+0.3% / +1.2%) | 15.2 / 15.1 → 15.5 (+2.3% / +2.6%) | 33.6 / 33.9 → 33.5 (−0.4% / −1.3%) |
| radixHuskySort8 | 2.87 / 2.82 → 2.88 (+0.6% / +2.4%) | 16.0 / 15.5 → 14.8 (−7.5% / −4.7%) | 36.3 / 37.2 → 35.0 (−3.6% / −6.1%) |
| quickHuskySort | 6.48 / 6.50 → 6.39 (−1.4% / −1.7%) | 28.9 / 29.1 → 29.2 (+1.2% / +0.5%) | 71.8 / 70.8 → 70.9 (−1.3% / +0.2%) |
| quickHuskySortWithCleanup | 7.09 / 7.01 → 6.93 (−2.3% / −1.1%) | 38.5 / 35.1 → 36.4 (−5.5% / +3.8%) | 85.2 / 85.5 → 86.7 (+1.8% / +1.3%) |
| dualPivotQuicksort | 12.6 / 12.4 → 12.2 (−2.9% / −1.4%) | 53.3 / 53.2 → 54.0 (+1.3% / +1.5%) | 134.8 / 133.6 → 139.3 (+3.3% / +4.3%) |

## What this means for the paper (facts only; wording is yours)

- Table `ParallelRadix` gains an `Arrays.parallelSort` column: 90.1 at 2M and 765.1 at 10M (repeat 88.7 / 748.7). Against
  it, eight husky threads **lose by 1.25× at 2M and win by 1.19× at 10M** (repeat 1.27× / 1.16×); four threads 1.32× / 1.14×
  (repeat 1.40× / 1.06×, the latter with overlapping bars). So "eight threads beat `Arrays.parallelSort`" is true at 10M
  only, and the crossover lies somewhere between 2,000,000 and 10,000,000 — only those two n were measured. This is the case
  you tied the qualification to — "If it does not, the paper's claim at large N needs qualifying, which is a bigger edit and
  the reason this is urgent rather than deferred" — and at 2M it does not.
- The permits sentence you drafted ("the parallel husky sort is ahead of `Arrays.parallelSort` on this corpus while serial
  RHSort is level with it") is contradicted at every n ≥ 100,000: parallelSort is 2.35–2.77× ahead of all three husky rows
  at the full corpus, and at no n is the parallel husky path faster than its own serial radix/16 (p4 level within the bars
  at 100k and 198.9k, slower otherwise; p8 slower everywhere). The serial claim (radix/16 4.92× over `Arrays.sort`) stands.
- Strings: this run asks for no wording beyond the change you said is "already in" ("we will ship the wording change alone,
  which is already in"); the sixteen-core factors, should you print them, are 5.17× / 2.98× at 200k / 1M (1.20x on eight).
  `ParallelRadixHuskySort` is still `Long[]`-only (TODO item 33): no parallel husky string row exists to set against it.

## Files

`doc/parallelsort.json` (step 1), `doc/parallel-vs-parallel.json` (step 2 — the file request 9 asked for),
`doc/permits-parallel.json` (step 3), all new; `doc/parallel-vs-parallel-repeat.json` (new; the confirmatory second run of
step 2, not requested — same command and jar, launched 09:55:10Z, 36 min after step 3 ended); `doc/Run results from Yunlu
2026-09-13.md` (this file). Ratios and bar edges throughout are computed from the unrounded JSON scores; cell-derived values
may differ in the last digit (e.g. systemSort ÷ parallelSort at 32,000: 2.95× unrounded, 2.94× from the cells).
