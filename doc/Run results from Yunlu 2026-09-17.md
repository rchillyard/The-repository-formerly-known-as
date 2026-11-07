# Run results from Yunlu — request 10, the optimised `ParallelRadixHuskySort`, 2026-09-17

Request 10 answered: 10a run twice on one jar (a full second run of all three permits invocations, 120 s apart), 10b run
in full (all four optional invocations), plus two supplementary string invocations that put the serial-encode floor next
to the 10b rows on the same jar and the same night. Raw JMH JSONs committed alongside (see Files). **Headline (10a):
`parallelRadixHuskySortAuto_pAll` does not beat `Arrays.parallelSort` on the permits on sixteen cores except at n =
32,000 — and there it is not a parallel sort.** At 32,000 pAll takes 0.63× the system sort's time (2.905 ± 0.016 vs
4.642 ± 0.026 ms/op; repeat 2.928 vs 4.626), but `chunks = max(1, min(16, 32000 / 16384)) = 1`, so that row is a serial
radix sort at the automatic 12-bit width, and serial `radixHuskySort16` (2.688 ± 0.010) beats both. At 100,000 pAll is
**2.20× slower** (13.086 ± 0.706 vs 5.959 ± 0.192; repeat 2.01×, 12.223 vs 6.087) and at 198,900 **1.99× slower**
(26.508 ± 0.468 vs 13.314 ± 0.124; repeat 2.02×, 27.285 vs 13.514) — CIs disjoint in both runs and the ordering holds on
every one of the ten forks (weakest fork edges 1.95× and 1.79×). pAll ran 6 chunks at 100,000 and 12 at 198,900; sixteen
threads were never used on the permits. The automatic digit width beats fixed 16 bits at p8 by 11.3% / 21.6% at 32k /
100k (repeat 11.8% / 15.5%) and is unresolved at 198,900 (+0.6%† / +8.1%†; the per-run median ratio flips sign). Against
its own serial radix/16, pAll is 1.10× slower at 32k on every fork; at 100k and 198,900 its median fork is about 10%
faster (pooled 0.895× / 0.904×) but not on every fork. **10b: on strings at 1,000,000, pAll is 3.20× / 1.28× / 3.23×
slower than `Arrays.parallelSort` (english / chinese / chinesenames), and the p1→p8 sweep speeds it up by only 1.00×† /
1.26× / 1.04×†.** The serial husky encode alone (`huskyEncodeOnly`) costs 70.345 / 14.450 / 210.916 ms at 1M — **76% /
34% / 90% of `Arrays.parallelSort`'s whole time** — consistent with a parallel path bounded by its serial phases rather
than by thread count (analysis below). † throughout = the two 99.9% CIs overlap; ratios are computed from unrounded JSON
scores.

**Checkout**: branch `parallel-redesign`, tip **`bd51494`** ("Record what the parallel rework measured, and ask Yunlu
for the run that settles it"). Request 10 names no freeze commit; the last commit touching `src/` is **`dbb0cad`**
(2026-09-16, "Set the parallel husky sort against parallelSort on strings, and on every core") — `git log
dbb0cad..bd51494 -- src/` is empty, so `dbb0cad` is the commit the paper should record for these numbers.

**Toolchain and build**: pinned Corretto **21.0.12+8-LTS** (`openjdk version "21.0.12" 2026-07-21 LTS`), Maven
**3.9.16**, JMH **1.37** (from the JSON); `mvn -B test` at the tip **405 tests, 0 failures** (396 at request 9 + 9 new
`ParallelRadixHuskySortTest` cases); `mvn -B -Pjmh package -DskipTests` → `target/benchmarks.jar` **76,287,165 B**,
sha256 `ef53f4444d15a74f4b3920c73d368d25297157c1c71807282c413e2a1b606e4f`, built 23:03:11 PT 2026-09-16 (06:03:11Z); the
first benchmark started 8 min later (no antivirus here beyond the falcon EDR agent, which never exceeded 29% of one
core). Every invocation used `-f 5 -wi 5 -i 10 -rf json` and nothing else (1 s iterations from the class annotations, no
JVM flags) → Cnt = 50 per row = 5 forks × 10 iterations; ± is JMH's 99.9% CI half-width. One JSON per invocation, as you
asked; nothing was consolidated.

**Environment**: same instance as 09-13 — aarch64 (Graviton3), **16 CPUs, 1 thread per core**, L1d/L1i 1 MiB (16
instances), L2 16 MiB (16), L3 32 MiB (1); `free -h` before run 1: Mem 30 Gi total / 16 Gi used / 12 Gi available, Swap
39 Gi / 5.1 Gi used — identical after it (17 Gi used before the repeat and the supplement; swap unchanged); `uname -r`
`6.12.103-127.188.amzn2023.aarch64`; `mvn -v` `Apache Maven 3.9.16 (2bdd9fddda4b…)`. All three units ran detached in
`app.slice` (`cpu.max = max`; cgroup `…/app.slice/husky-req10-20260917-0611.service` etc., verified on the JMH parent
and a fork PID), never under the 14-core `kiro.slice` quota. **The `ForkJoinPool` probe** (`jshell -s -`, your `-q -s -`
is rejected by this JDK — "Only one feedback option … may be used" — `-s -` prints the same two numbers;
`commonPool().getParallelism() + " " + availableProcessors()`) ran inside each unit and printed **`15 16`** before step
1 and again after step 3 of run 1, and before/after the repeat and the supplement — so `Arrays.parallelSort` had 15 pool
workers plus the calling thread and pAll had `availableProcessors()` = 16 in every invocation. Other sessions held ≈19
GiB RSS at launch; swap in use moved 5,226 → 5,210 MiB over run 1 (Δ −16), 5,210 → 5,208 over the repeat, 5,208 → 5,206
over the supplement — flat. `uptime` immediately before and after every step, as asked (PT, UTC in brackets):

| unit / step | invocation | before | load average before (1 / 5 / 15) | after | load average after |
|---|---|---|---|---|---|
| run 1 step 1 | permits-auto | 23:11:31 (06:11:31Z) | 0.49, 0.73, 0.53 | 23:19:21 (06:19:21Z) | 10.24, 5.07, 2.35 |
| run 1 step 2 | permits-auto-vs-16 | 23:19:21 (06:19:21Z) | **10.24**, 5.07, 2.35 | 23:27:12 (06:27:12Z) | 1.87, 2.26, 1.99 |
| run 1 step 3 | permits-parallel-vs-serial | 23:27:12 (06:27:12Z) | 1.87, 2.26, 1.99 | 23:35:02 (06:35:02Z) | 1.41, 1.72, 1.82 |
| run 1 step 4 | strings-parallel | 23:35:03 (06:35:03Z) | 1.54, 1.74, 1.83 | 23:44:54 (06:44:54Z) | 12.77, 8.29, 4.63 |
| run 1 step 5 | strings-parallel-sweep | 23:44:54 (06:44:54Z) | **12.77**, 8.29, 4.63 | 00:05:29 (07:05:29Z) | 1.43, 1.73, 2.49 |
| run 1 step 6 | parallelsort-pinyin | 00:05:29 (07:05:29Z) | 1.43, 1.73, 2.49 | 00:13:55 (07:13:55Z) | 14.27, 9.77, 6.06 |
| repeat step 1 | permits-auto | 00:40:31 (07:40:31Z) | 0.23, 0.21, 1.23 | 00:48:20 (07:48:20Z) | 13.20, 6.09, 3.20 |
| repeat step 2 | permits-auto-vs-16 | 00:50:20 (07:50:20Z) | 2.14, 4.24, 2.87 | 00:58:11 (07:58:11Z) | 1.40, 2.06, 2.31 |
| repeat step 3 | permits-parallel-vs-serial | 01:00:11 (08:00:11Z) | 0.41, 1.45, 2.05 | 01:08:01 (08:08:01Z) | 1.64, 1.60, 1.87 |
| supplement step 1 | strings-encode-only | 01:12:45 (08:12:45Z) | 0.43, 0.97, 1.54 | 01:17:15 (08:17:15Z) | 1.62, 1.40, 1.57 |
| supplement step 2 | strings-serial-vs-parallel | 01:19:15 (08:19:15Z) | 0.26, 0.94, 1.38 | 01:29:42 (08:29:42Z) | 1.45, 1.70, 1.64 |

Two before-loads in run 1 fail your "starts at 4 is not clean" rule at face value, and I want to be exact about why they
are cosmetic rather than hide them. Run 1's runner is strictly sequential with no gap: each step's `before:` line is the
previous step's `after:` line to the second (06:19:21, 06:44:54). The 10.24 and 12.77 are the 1-minute load average left
by the *preceding* step's own 16-thread `systemSortParallel` forks (step 1 and step 4), not another process. The load
monitor (one `top -b -n 1` per minute) shows the decay while the machine ran ≈ 1 core: inside step 2 at 23:19:33 PT
(06:19:33Z) load1 8.82 with the benchmark JVM at 100.0% and **no other process ≥ 10%**, then 4.07 (23:20:33), 2.29
(23:21:33), 1.60 (23:22:34); inside step 5, 7.56 → 3.99 → 2.14 over 23:45:38–23:47:38 PT (06:45:38–06:47:38Z) with java
at 94–125%. Load average measures the run queue's recent past; the machine was running ≈1 core. To meet the letter of
the rule anyway, **the repeat inserted a 120 s gap before every step**: its before-loads are 0.23 / 2.14 / 0.41 (the
2.14 is again step 1's own 13.20 decaying, 13.20·e⁻² ≈ 1.8 plus ambient; no non-java process ≥ 30% in that gap). The
supplement also used 120 s gaps (0.43 / 0.26). After-loads of 10–14 are the steps that contain `systemSortParallel` rows
(run 1 steps 1, 4, 6; repeat step 1) — the system sort is the only method here that occupies the machine (see the %CPU
evidence below); husky-only steps end at 1.4–1.9.

## Correctness was checked before any timing was trusted

The external `ValidateSorts` harness (reuses the repo's classes and each benchmark's exact input generation and seeds)
gained a section G for the new code and ran against this jar: **276/276 checks passed** (102 pre-existing A–F + 174 new;
53 s; `validate/logs/req10-validate-20260917-060831.log`). **G1 permits** (n = 32,000 / 100,000 / 198,900; Fisher–Yates
`Random(42)` shuffle, prefix n, as in `PermitState`): `Auto_pAll`, an explicit `Auto p16` (what pAll is in `app.slice`),
`Auto_p8`, `Auto_p8_chunk4k` and `16_p8`, constructed verbatim from `PermitSortBenchmarks`, each **sorted under
`Permit.compareTo`, a permutation, order-equal to `Arrays.sort`, and stable — reference-identical (`==`) to the stable
`Arrays.sort` result**, which is observable because the permits have 983 / 7,813 / 26,092 adjacent `compareTo` ties at
the three n. **G2 strings** (english / chinese via the unicode coder, chinesenames via the pinyin coder; `Random(42)`
with replacement, as in `StringState`; n = 32,000 / 200,000 / 1,000,000): `new ParallelRadixHuskySort<>(AUTO_DIGIT_BITS,
coder, config, parallelism)` exactly as `sortAuto()`, at pAll (14 in the harness's own quota-limited scope), an explicit
p16 and p1, **positionwise equal to `Arrays.sort`**, or to `Arrays.sort(copy, HuskyCoderChinesePinyin.NAME_ORDER)` for
chinesenames (`NAME_ORDER` is a total order, so equality is exact); `Arrays.parallelSort` with the same comparator
matched too. **G3 `chooseDigitBits`**: the width every Auto row actually runs at (table below), all within [8, 16];
24/24 (n, chunks) pairs agree with the formula. Each JSON passed structural validation **19/19 × 10 files** (exact
(method, params) row set; Cnt = 50 every row; `rawData` 5 × 10; no null/NaN; ms/op; forks/wi/i = 5/5/10; `score ==
mean(rawData)`; `scoreConfidence == score ± scoreError`; no JVM flags), and the eleventh (`parallelsort-pinyin`, see
step 6) 19/19 once restricted to the requested class. Every JSON was cross-checked against its JMH stdout table: **all
69 rows match to the 3 decimals JMH prints**, and the `# Fork:` count equals rows × 5 in every log.

## Reading aid — what each Auto row actually ran, and what is inside the timed region

`ParallelRadixHuskySort.sort` (`ParallelRadixHuskySort.java:223`) splits each pass into `chunks = max(1,
min(parallelism, n / MIN_CHUNK_SIZE))` with `MIN_CHUNK_SIZE = 1 << 14` (`:62`); `chooseDigitBits(n, chunks)` (`:100`)
takes the widest power-of-two digit with `buckets × chunks ≤ n / 4`, clamped to [8, 16]; passes = ⌈64 / bits⌉.

| dataset | n | parallelism | chunks | digit bits | passes |
|---|---:|---:|---:|---:|---:|
| permits | 32,000 | 8 or 16 (pAll) | **1** | 12 | 6 |
| permits | 100,000 | 8 or 16 (pAll) | 6 | 12 | 6 |
| permits | 198,900 | 8 | 8 | 12 | 6 |
| permits | 198,900 | 16 (pAll) | 12 | 12 | 6 |
| strings | 1,000,000 | 1 / 2 | 1 / 2 | 16 | 4 |
| strings | 1,000,000 | 4 | 4 | 15 | 5 |
| strings | 1,000,000 | 8 | 8 | 14 | 5 |
| strings | 1,000,000 | 16 (pAll) | 16 | 13 | 5 |

Fixed-16 rows (`parallelRadixHuskySort16_p8`, `radixHuskySort16`) run 4 passes; `serialRadixHuskySort8` runs 8. So on
the permits every Auto row at 32,000 is **one chunk — the single-worker path**, and pAll reaches 16 chunks only on the
1M strings. (Doc nit: the `parallelRadixHuskySortAuto_p8` Javadoc, `PermitSortBenchmarks.java:148`, says 11 bits at n =
100,000; with 6 chunks the budget is 100000 / 24 = 4,166 buckets, so it is 12.) Inside the timed region: both sides
start with the same `Arrays.copyOf`. `Arrays.parallelSort`'s region is then one parallel comparison sort on the common
pool. The husky region is serial encode of all n keys (`preSort` → the coder), then the **only parallel part**,
`radixSortIndices` (`:226`, the counting passes over the chunks), then a **serial** `applyPermutation` (`:227`), then
for the string corpora a **serial** cleanup `Arrays.sort` (the unicode coder is exact only for strings of ≤ 3 chars, the
pinyin coder never). It is a fair end-to-end comparison — what a caller pays — but not parallel-radix vs
parallel-mergesort.

## 10a step 1 — permits: `Arrays.parallelSort` vs `parallelRadixHuskySortAuto_pAll` (the headline; ms/op)

`java -jar target/benchmarks.jar "PermitSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll)$" -f 5 -wi 5
-i 10 -rf json -rff permits-auto.json` — run twice (`req10-permits-auto.json`, `req10-repeat-permits-auto.json`).

| n | run | `systemSortParallel` | `parallelRadixHuskySortAuto_pAll` | pAll ÷ parallelSort | pAll chunks / bits |
|---:|---|---:|---:|---:|---|
| 32,000 | 1 | 4.642 ± 0.026 | 2.905 ± 0.016 | 0.626× | 1 / 12 (6 passes) — **serial** |
| 32,000 | repeat | 4.626 ± 0.021 | 2.928 ± 0.023 | 0.633× | |
| 100,000 | 1 | 5.959 ± 0.192 | 13.086 ± 0.706 | 2.196× | 6 / 12 |
| 100,000 | repeat | 6.087 ± 0.121 | 12.223 ± 0.125 | 2.008× | |
| 198,900 | 1 | 13.314 ± 0.124 | 26.508 ± 0.468 | 1.991× | 12 / 12 |
| 198,900 | repeat | 13.514 ± 0.177 | 27.285 ± 0.692 | 2.019× | |

**E10a-1 — you wrote that pAll "is the row to compare against the system sort", and that on the M1 the automatic width
won at 32,000 and 198,900. On sixteen cores it wins only at 32,000, and that win is request 9's serial result
restated.** At 32,000 pAll ÷ parallelSort = 0.626× (2.905 / 4.642), but pAll runs one chunk there, and the plain serial
`radixHuskySort16` is faster still (4.642 / 2.688 = 1.73× over parallelSort; 2.905 / 2.688 = 1.08× over pAll) — the
system sort simply pays ForkJoin overhead on 32,000 elements. At 100,000 pAll is 2.196× slower (13.086 / 5.959), at
198,900 1.991× slower (26.508 / 13.314); CIs disjoint. **Pooled over the 10 forks of both runs** (median fork means):
0.630× (2.917 / 4.628), **2.036×** (12.297 / 6.040), **2.024×** (27.245 / 13.459); at the weakest edge — the fastest
pAll fork against the slowest parallelSort fork — pAll is still **1.947× slower at 100,000** (12.065 / 6.196) and
**1.793× slower at 198,900** (25.070 / 13.984), so the verdict holds on every fork of both runs. Run 1's 2.196× at 100k
was inflated by one fork (15.85 against siblings 12.20–12.61); the repeat's 12.223 ± 0.125 disagrees with it beyond CI
(0.934×) and the pooled 2.036× is the number to quote. `systemSortParallel` reproduces within 2.2% at every n and agrees
with request 9 (13.314 vs 13.129 at 198,900, +1.4%†). Your thread-asymmetry reading of request 9 — "15 threads against 8
… very likely most of why" — does not survive this: giving the husky sort 12 chunks moved it from p8's 36.4 (request 9)
to 26.5–27.3, still 2× behind a 13.3 ms baseline.

## 10a step 2 — permits: automatic digit width vs fixed 16 bits, both p8 (ms/op)

`java -jar target/benchmarks.jar "PermitSortBenchmarks.(parallelRadixHuskySort16_p8|parallelRadixHuskySortAuto_p8)$" -f
5 -wi 5 -i 10 -rf json -rff permits-auto-vs-16.json` — run twice.

| n | run | `parallelRadixHuskySort16_p8` | `parallelRadixHuskySortAuto_p8` | Auto ÷ 16 | Auto faster by | chunks; bits 16 → auto |
|---:|---|---:|---:|---:|---:|---|
| 32,000 | 1 | 3.306 ± 0.026 | 2.932 ± 0.021 | 0.887× | 11.3% | 1 (serial); 16 (4 passes) → 12 (6) |
| 32,000 | repeat | 3.295 ± 0.032 | 2.907 ± 0.027 | 0.882× | 11.8% | |
| 100,000 | 1 | 16.078 ± 0.501 | 12.608 ± 0.182 | 0.784× | 21.6% | 6; 16 → 12 |
| 100,000 | repeat | 15.106 ± 0.116 | 12.766 ± 0.401 | 0.845× | 15.5% | |
| 198,900 | 1 | 32.395 ± 1.936 | 32.195 ± 1.748 | 0.994×† | 0.6%† | 8; 16 → 12 |
| 198,900 | repeat | 34.421 ± 1.912 | 31.650 ± 2.115 | 0.919×† | 8.1%† | |

**E10a-2 — your Mac said 11.9% / 11.7% / 6.2%; the band you named is 6–12%.** Here: **+11.3% at 32,000** (0.887 = 2.932
/ 3.306; repeat +11.8%) — in the band, but a *serial* comparison (both rows one chunk: 12-bit/6-pass against
16-bit/4-pass); **+21.6% at 100,000** (0.784 = 12.608 / 16.078; repeat +15.5%) — above the band in both runs, although
run 1's 16_p8 row carries a 17.63 fork; **+0.6%† at 198,900** (0.994 = 32.195 / 32.395; repeat +8.1%†, 0.919 = 31.650 /
34.421) — unresolved: both rows at 198,900 are fork-bimodal in both runs (16_p8 forks 28.3–39.1, Auto_p8 26.8–37.7;
Appendix), the per-run median ratio flips 1.140× → 0.815×, the pooled median is 0.999× (32.229 / 32.264) and the fork
ranges overlap (edge 0.750×). Pooled, the automatic width is faster **on every fork** at 32k and 100k: fixed 16 takes at
least 1.088× as long as auto on every fork at 32k (3.235 / 2.975, unrounded 1.0876) and at least 1.044× at 100k (14.877
/ 14.247). Safe statement: at p8 the automatic width is 11–22% faster than fixed 16 at n ≤ 100,000 on every fork, and at
198,900 it is faster by score in both runs (0.994×, 0.919×) but not resolved by 20 forks — run 1's median fork was 1.140×
slower. Note also that the parallel class at one chunk and 16 bits (3.306, step 2) is 1.23× slower than plain
`RadixHuskySort/16` (2.688, step 3 — a cross-invocation comparison), so part of the 32k gain is the automatic width
recovering the parallel class's own single-chunk overhead.

## 10a step 3 — permits: pAll vs its own serial `radixHuskySort16` (ms/op)

`java -jar target/benchmarks.jar "PermitSortBenchmarks.(radixHuskySort16|parallelRadixHuskySortAuto_pAll)$" -f 5 -wi 5
-i 10 -rf json -rff permits-parallel-vs-serial.json` — run twice.

| n | run | `radixHuskySort16` (1 chunk, 16 bits, 4 passes) | `parallelRadixHuskySortAuto_pAll` | pAll ÷ serial | pAll is |
|---:|---|---:|---:|---:|---|
| 32,000 | 1 | 2.688 ± 0.010 | 2.964 ± 0.025 | 1.103× | 1.10× slower (1 chunk, serial) |
| 32,000 | repeat | 2.739 ± 0.020 | 3.025 ± 0.039 | 1.104× | 1.10× slower (1 chunk, serial) |
| 100,000 | 1 | 13.466 ± 0.453 | 12.420 ± 0.099 | 0.922× | 1.08× faster |
| 100,000 | repeat | 14.631 ± 0.555 | 14.489 ± 0.972 | 0.990×† | level † |
| 198,900 | 1 | 30.482 ± 1.239 | 31.375 ± 2.692 | 1.029×† | level † |
| 198,900 | repeat | 32.165 ± 1.579 | 26.550 ± 0.803 | 0.825× | 1.21× faster |

**E10a-3 — the "most damning line" of 09-13 (parallel slower than its own serial radix/16 at every n) is softened, not
reversed.** At 32,000 pAll is 1.103× slower (2.964 / 2.688; repeat 1.104×) on every fork (edge 1.055×) — serial against
serial by construction, six 12-bit passes against four 16-bit ones. At 100,000 run 1 says 1.08× faster with disjoint CIs
(12.420 / 13.466), the repeat says level (0.990×†; its pAll row has an 18.03 fork). At 198,900 run 1 says level
(1.029×†) and the repeat says 1.21× faster (26.550 / 32.165, disjoint). **Run 1's step-3 pAll@198,900 row (31.375 ±
2.692) is the one outlier among four measurements of that row**: its fork means are **37.04 / 25.75 / 38.68 / 27.14 /
28.28** — two forks in a slow mode from their first warmup iteration (38.7, 40.7), within-fork spread ≤ 1.03× — while
steps 1 of both runs and the repeat's step 3 gave 26.508 / 27.285 / 26.550 with forks 24.8–29.2. The monitor sample that
lands inside a slow fork (23:30:35 PT, 06:30:35Z) shows java at 125% and nothing else ≥ 10%, and the same two-mode
pattern hits the *serial* `radixHuskySort16` (28.1 vs 33.6 in run 1; 28.9 vs 36.9 in the repeat), so it is a
per-JVM-fork mode (JIT or layout; indistinguishable from these logs), not load and not the parallel code. Consequence:
JMH's ± treats 50 samples as independent and is anti-conservative when fork means are bimodal — the effective n is the
fork count, which is why both runs are reported and pooled. **Pooled 10-fork medians**: 1.108× slower (2.990 / 2.699) at
32k; **0.895× (12.748 / 14.244) at 100k and 0.904× (27.459 / 30.386) at 198,900** — i.e. pAll's median fork is ~10%
faster than serial radix/16 — but the fork ranges overlap (edges 0.687× / 0.727×: pAll has 3 slow forks in 40 (18.03 at
100k; 37.04, 38.68 at 198,900), radix/16 has 4 at 198,900 (33.6, 33.4, 36.9, 34.4)), so the sign is not established on
every fork. Cross-run, `radixHuskySort16` agrees with request 9 at every n
(30.482 vs 30.894, −1.3%†).

**Where the 20 forks of pAll @ 198,900 sit** (all four invocations): 24.8, 25.1, 25.2, 25.4, 25.4, 25.7, 26.0, 26.7,
27.1, 27.2 | 27.3, 27.3, 27.5, 27.8, 27.9, 28.3, 29.0, 29.2, **37.0, 38.7** — min 24.780 / median 27.245 / max 38.679;
18 of 20 in 24.8–29.2, the two ≥ 37 both from run 1 step 3. Against `systemSortParallel` @ 198,900 (forks 13.06–13.98
over both runs): run 1 alone, fastest pAll fork 25.36 / slowest parallelSort fork 13.51 = **1.88×**; pooled step 1,
25.070 / 13.984 = 1.79×; the fastest of all 20 pAll forks, 24.780 / 13.984 = 1.77×; pooled medians 27.245 / 13.459 =
2.02×; run 1 step 3's score 31.375 / 13.314 = 2.36×. The headline does not depend on which is picked.

## The 15-vs-16 question

You asked whether pAll should match the pool's 15 rather than the machine's 16. **My judgement: keep
`availableProcessors()`; do not switch pAll to 15.** (a) It is moot for 10a: pAll used 1 / 6 / 12 chunks, never 16. (b)
On the strings it is already symmetric: `Arrays.parallelSort` runs on 15 pool workers **plus the calling thread**
(`ForkJoinTask.invoke` runs the root task on the caller), ≈ 16 compute threads; pAll = 16 workers plus a parked caller.
A 15-worker pAll would idle one core the baseline uses. (c) It is not measurable at this precision: p8 and pAll are
indistinguishable on every corpus (1.061×† / 0.992×† / 1.011×†, below), and on a ≤ 25% parallel fraction a 15-vs-16 step
is ≤ ~0.15% of the total (f · (1/15 − 1/16)). If you want the number on record, add a `pAll-1` row *beside* pAll and I
will run the pair.

## Why the parallel path loses — analysis, labelled as such

Everything above is measurement; this paragraph is inference from it. Only `radixSortIndices` is parallel; the encode,
the `Arrays.copyOf`, `applyPermutation` and (strings) the cleanup sort are serial. **CPU evidence**: the per-minute
`top` samples of the benchmark JVM read **94–150% of one core in every husky-only window** (run 1 steps 2–3: max 125%
over 16 samples; step 4 pAll rows 94–150%; repeat steps 2–3 max 244% / 162%) against **962–1,406%** for
`systemSortParallel` rows — the husky rows never occupy more than ~1.5 cores. **Permits**: from request 9's serial rows
(3 forks; cross-run) the per-pass cost at 198,900 is (34.954 − 30.894) / 4 = 1.02 ms (radix/8 vs /16) or (33.468 −
30.894) / 2 = 1.29 ms (radix/11 vs /16), so radix/16's four passes are ≈ 4–5 ms of its 30.9 ms and ≈ 26 ms is serial
work outside the passes; pAll @ 198,900 = 26.5 ms sits on that floor, and even infinitely fast passes leave it ≈ 2×
behind parallelSort's 13.3. **Strings, same jar, same night** (`huskyEncodeOnly` = `coder.huskyEncode(master)` on the 1M
array, nothing else):

| corpus @ 1,000,000 (ms/op) | `huskyEncodeOnly` | `Arrays.parallelSort` (step 4) | encode ÷ parallelSort | pAll (step 4) | encode ÷ pAll |
|---|---:|---:|---:|---:|---:|
| english | 70.345 ± 1.003 | 92.377 ± 4.144 | **0.762** (70.345 / 92.377) | 295.152 ± 24.155 | 0.238 |
| chinese | 14.450 ± 0.094 | 42.431 ± 2.476 | **0.341** (14.450 / 42.431) | 54.145 ± 2.822 | 0.267 |
| chinesenames | 210.916 ± 0.569 | 234.429 ± 3.239 | **0.900** (210.916 / 234.429) | 756.720 ± 8.151 | 0.279 |

The serial encode alone is 76% / 34% / 90% of the system sort's whole time, and only 24–28% of pAll's — the other ~70%
of pAll is the passes plus the serial permutation and cleanup. **Amdahl's fraction from the sweep** (derived: T(p) =
T₁(1 − f) + T₁ f / p, perfect scaling assumed): english f ≈ (278.022 − 265.943) / (278.022 × 0.75) = **0.058** (p1→p4;
p1→p8 gives ≈ 0), chinese f ≈ (68.957 − 56.297) / (68.957 × 0.75) = **0.245** (p1→p8: 0.238, consistent), chinesenames f
≈ (776.370 − 750.297) / (776.370 × 0.5) = **0.067** (p1→p2; p4/p8 give 0.03–0.04). The parallelisable share of the timed
region is ~5–7% on english and chinesenames and ~25% on chinese, which the ~100% CPU readings corroborate. The data
supports a serial-fraction explanation; memory bandwidth was not measured and should not be named as the cause. The
premise that husky coding has most to offer where comparisons are expensive inverts here: the expensive ordering that
the coding "buys back" is exactly the step that stays serial.

## 10b step 4 — strings @ 1M: `Arrays.parallelSort` vs pAll, three corpora (ms/op)

`java -jar target/benchmarks.jar "ParallelStringSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll)$"
-p n=1000000 -f 5 -wi 5 -i 10 -rf json -rff strings-parallel.json` (pAll = 16 chunks, 13 bits, 5 passes).

| corpus | `systemSortParallel` | `parallelRadixHuskySortAuto_pAll` | pAll ÷ parallelSort | fork means (parallelSort · pAll) |
|---|---:|---:|---:|---|
| english | 92.377 ± 4.144 | 295.152 ± 24.155 | **3.195×** (295.152 / 92.377) | 97.0 / 82.4 / 101.7 / 82.8 / 98.1 · 266.1 / 288.5 / 302.3 / 304.8 / 314.0 |
| chinese | 42.431 ± 2.476 | 54.145 ± 2.822 | **1.276×** (54.145 / 42.431) | 42.3 / 42.1 / 41.8 / 41.4 / 44.5 · 56.3 / 54.6 / 53.9 / 53.4 / 52.6 |
| chinesenames | 234.429 ± 3.239 | 756.720 ± 8.151 | **3.228×** (756.720 / 234.429) | 243.3 / 234.7 / 232.8 / 233.4 / 228.0 · 749.4 / 754.6 / 748.1 / 747.1 / 784.4 |

**E10b-1 — the gap request 9 left open closes the other way**: with a parallel husky sort finally wired against
`Arrays.parallelSort` on strings, pAll is 3.20× / 1.28× / 3.23× slower; CIs disjoint on all three corpora, fork means
disjoint too (chinese: 41.4–44.5 vs 52.6–56.3, although raw samples overlap). Two caveats belong next to the english
row. **(i) Every english pAll fork spikes in its last one or two iterations** (per-fork mean of iterations 9–10 ÷ 1–8 =
1.08 / 1.45 / 1.34 / 1.39 / 1.56; raw 256–448), so the score 295.152 is 1.073× the mean of iterations 1–8 (274.989); on
those the ratio is 2.98× (274.989 / 92.377), and at the fork extremes (266.13 / 101.65) 2.62×. The supplement's pAll
english row reproduces the pattern exactly (289.452 ± 25.777, iterations 1–8 mean 266.324, tails 1.63 / 1.50 / 1.29 /
1.60 / 1.17), so it is a property of this benchmark (plausibly heap growth of the ≈ 50–60 MB/op of `long[]`/`int[]`
scratch reaching a GC boundary; unproven — `-prof gc` would change your no-JVM-args condition). **(ii) english
`Arrays.parallelSort` is fork-bimodal** (82.35 / 82.78 vs 96.99 / 98.11 / 101.65). Neither moves the verdict.

## 10b step 5 — strings @ 1M: thread-count sweep p1 / p2 / p4 / p8 (ms/op)

`java -jar target/benchmarks.jar "ParallelStringSortBenchmarks.parallelRadixHuskySortAuto_p.$" -p n=1000000 -f 5 -wi 5
-i 10 -rf json -rff strings-parallel-sweep.json` (`p.$` is one character, so p1/p2/p4/p8 only; four methods in one
invocation — your command). pAll columns are from **separate invocations** (step 4; supplement step 2).

| corpus | p1 (16 bits, 4 passes) | p2 (16, 4) | p4 (15, 5) | p8 (14, 5) | pAll step 4 (13, 5) | pAll supplement | p1 ÷ p2 | p2 ÷ p4 | p4 ÷ p8 | p1 ÷ p8 (ideal 8×) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| english | 278.022 ± 3.987 | 271.828 ± 2.394 | 265.943 ± 2.484 | 278.158 ± 9.637 | 295.152 ± 24.155 | 289.452 ± 25.777 | 1.023×† | 1.022× | 0.956× | **1.000×†** |
| chinese | 68.957 ± 2.800 | 60.091 ± 2.316 | 56.297 ± 2.629 | 54.580 ± 2.392 | 54.145 ± 2.822 | 54.232 ± 2.527 | 1.148× | 1.067×† | 1.031×† | **1.263×** |
| chinesenames | 776.370 ± 22.448 | 750.297 ± 4.715 | 758.576 ± 24.712 | 748.413 ± 18.544 | 756.720 ± 8.151 | 760.127 ± 21.458 | 1.035×† | 0.989×† | 1.014×† | **1.037×†** |

**E10b-2 — "whether strings scale with cores differently from a cheap ordering": they barely scale at all.** p1→p8 buys
1.000×† on english (278.022 / 278.158), 1.263× on chinese (68.957 / 54.580) and 1.037×† on chinesenames (776.370 /
748.413); p8 → pAll is flat (step-4 pAll ÷ p8 1.061×† / 0.992×† / 1.011×†; supplement pAll ÷ p8 1.041×† / 0.994×† /
1.016×†). `chunks = min(p, 10⁶ / 16384 = 61) = p`, so `MIN_CHUNK_SIZE` is not the limiter here. Two caveats: the
automatic width couples to the chunk count (16 / 16 / 15 / 14 bits → 4 / 4 / 5 / 5 passes), so this is not a pure thread
sweep; and single ≈ 1,000 ms samples sit in the last iteration of some forks (chinesenames p1 forks 3–4: 1007.8, 936.4;
p4 forks 1, 5: 993.0, 1003.6; p8 fork 5: 1002.1; english p8 forks 4–5: 381.9, 354.5), inflating those scores by ≤ 1.8% —
the same late-iteration behaviour as step 4, not external load (no non-java process ≥ 30% coincides with a slow fork).

## 10b supplement — serial radix/8 vs pAll @ 1M, same invocation (ms/op)

`java -jar target/benchmarks.jar 'ParallelStringSortBenchmarks\.(serialRadixHuskySort8|parallelRadixHuskySortAuto_pAll)$'
-p n=1000000 -f 5 -wi 5 -i 10 -rf json -rff strings-serial-vs-parallel.json` (not requested; puts the class's own
serial reference — `RadixHuskySort` at the default 8 bits, 8 passes — beside pAll in one invocation).

| corpus | `serialRadixHuskySort8` | `parallelRadixHuskySortAuto_pAll` | serial ÷ pAll | pAll vs step 4 |
|---|---:|---:|---:|---:|
| english | 304.383 ± 12.927 | 289.452 ± 25.777 | 1.052×† (304.383 / 289.452) | 0.981×† |
| chinese | 81.358 ± 2.642 | 54.232 ± 2.527 | **1.500×** (81.358 / 54.232) | 1.002×† |
| chinesenames | 770.833 ± 4.710 | 760.127 ± 21.458 | 1.014×† (770.833 / 760.127) | 1.005×† |

Sixteen threads and the automatic width buy 1.50× over serial radix/8 on chinese and nothing resolvable on english or
chinesenames — consistent with the sweep's f. pAll reproduces step 4 within 2% on every corpus.

## 10b step 6 — `StringSortBenchmarks.systemSortParallel` on chinesenames, pinyin comparator (ms/op)

`java -jar target/benchmarks.jar "StringSortBenchmarks.systemSortParallel$" -p corpus=chinesenames -f 5 -wi 5 -i 10 -rf
json -rff parallelsort-pinyin.json` (no `-p n`, so all three sizes).

| n | `StringSortBenchmarks.systemSortParallel` (`NAME_ORDER`) | co-matched `ParallelStringSortBenchmarks.systemSortParallel` | Parallel ÷ String |
|---:|---:|---:|---:|
| 32,000 | 21.390 ± 0.086 | 21.289 ± 0.079 | 0.995×† |
| 200,000 | 47.776 ± 0.763 | 47.949 ± 0.701 | 1.004×† |
| 1,000,000 | 241.693 ± 4.993 | 235.323 ± 3.434 | 0.974×† |

**E10b-3 and the superseded note.** Request 9 measured `StringSortBenchmarks.systemSortParallel` on the english corpus
only, so no chinesenames figure was ever taken under the code-point ordering and there is nothing to mark superseded.
The rows above are the first chinesenames measurement of this benchmark; they use `HuskyCoderChinesePinyin.NAME_ORDER`
(`StringSortBenchmarks.java:186`) and may be tabulated beside request-10 husky rows. Step 4's 234.429 ± 3.239 at 1M is a
third, †-consistent measurement. **One quirk of your regex**: JMH matches benchmark patterns with `find()`
(left-unanchored), so `StringSortBenchmarks.systemSortParallel$` also selected
`ParallelStringSortBenchmarks.systemSortParallel`, which is byte-identical code (`Arrays.parallelSort(copy,
HuskyCoderChinesePinyin.NAME_ORDER)`, `StringSortBenchmarks.java:184–189` vs `ParallelStringSortBenchmarks.java:76–81`).
The JSON therefore holds six rows, kept unedited; the extra three are a free same-invocation replication (all †), not an
error. Anchor future patterns on the class: `\.StringSortBenchmarks\.systemSortParallel$` (the supplement did).

## Reproducibility and quality of the numbers

- **JSON ↔ log**: every row of all 11 files matches its JMH stdout table to 3 decimals (69 rows); `# Fork:` counts 30 /
  30 / 30 / 30 / 60 / 30 (run 1), 30 / 30 / 30 (repeat), 15 / 30 (supplement) = rows × 5 everywhere.
- **Fork bimodality, not iteration noise, is the dominant spread.** `rawData` max ÷ min > 1.3 on **17 of 42** run-1
  rows; the worst: english pAll 1.747 (256.2–447.7, the iteration tail), chinese parallelSort 1.571 (36.7–57.7), step-3
  pAll@198,900 1.523 (fork modes), sweep english p8 1.449 (381.9 sample), 16_p8@198,900 1.421, chinese pAll 1.418,
  pAll@100k 1.390 (one 15.85 fork), parallelSort@100k 1.377. Rows whose fork means differ by > 1.3× are listed in the
  Appendix; within-fork spread is ≤ 1.035× in the bimodal permits rows, and slow forks are slow from warmup iteration 1.
- **Run vs repeat (18 permits rows)**: 12 reproduce within CI; **6 do not** — pAll@100k step 1 (13.086 → 12.223,
  0.934×), 16_p8@100k (16.078 → 15.106, 0.940×), radix16@32k (2.688 → 2.739, 1.019×), pAll@100k step 3 (12.420 → 14.489,
  1.167×), radix16@100k (13.466 → 14.631, 1.086×), pAll@198,900 step 3 (31.375 → 26.550, 0.846×). No
  `systemSortParallel` row moved more than 2.2%. Verdicts that flip or lose significance between runs: E10a-2 @ 198,900
  (median ratio 1.140× → 0.815×), E10a-3 @ 100k (disjoint → †) and @ 198,900 († → disjoint the other way).
- **Non-java CPU** (single one-minute `top` samples; jiffy-quantised; PT, UTC in brackets): run 1 — `unison` 94.1% +
  `falcon` 29.4% at 23:27:34 (06:27:34Z; 22 s into step 3, inside pAll@32k fork 2, which is that row's *fastest* fork,
  2.92), `unison` 62.5% at 23:22:34 (06:22:34Z; step 2, ≈ 5 s after 16_p8@198,900's slow fork 2 ended — that fork was
  already slow at warmup iteration 1), `snape` 31.2% / `unison` 76.5% / `aws` 100.0% at 23:49:39 / 00:03:41 / 00:04:41
  (06:49:39 / 07:03:41 / 07:04:41Z; step 5, chinesenames p1 and p8 rows whose fork means are flat); repeat — `unison`
  75.0% + `kiro` 31.2% at 00:46:32 (07:46:32Z; step 1, interpolates to parallelSort@100k fork ≈ 4: forks 6.02 / 6.09 /
  6.07 / 6.20 / 6.06); supplement — `unison` 41.2%, `dockerd` 23.5%. None is sustained, none lands on an outlying fork,
  none changes a verdict. `unison` (my file sync) fires when the results directory grows; future runs will write outside
  synced trees.
- **Swap** flat in all three units (Δ −16 / −2 / −2 MiB); `free -h` used and swap columns identical before and after each
  unit (supplement: free 2.7 → 3.9 Gi, available 11 → 12 Gi).

## Verdicts against your stated expectations

| expectation | observed | verdict |
|---|---|---|
| E10a-1 pAll is the row to beat `Arrays.parallelSort`; M1 won at 32k and 198.9k | 0.626× / 2.196× / 1.991× (repeat 0.633× / 2.008× / 2.019×); pooled 0.630× / 2.036× / 2.024× | wins only at 32k, where it is 1 chunk (serial); slower on every fork of both runs — 1.95× / 1.79× at the weakest fork edge, 2.0× at the pooled median, at 100k / 198.9k |
| E10a-2 auto width beats fixed 16 by 6–12% | +11.3% / +21.6% / +0.6%† (repeat +11.8% / +15.5% / +8.1%†) | yes at 32k (serial) and 100k (above the band); unresolved at 198,900 |
| E10a-3 parallel no longer slower than serial radix/16 | 1.103× / 0.922× / 1.029×† (repeat 1.104× / 0.990×† / 0.825×); pooled 1.108× / 0.895× / 0.904× | slower at 32k on every fork; ~10% faster by median fork at 100k–198.9k, sign not robust |
| thread asymmetry (15 vs 8) explains request 9's inversion | 12 chunks moved 36.4 → 26.5–27.3 vs 13.3–13.5 | refuted on this machine |
| E10b-1 parallel husky vs parallelSort on strings | 3.195× / 1.276× / 3.228× slower | no; serial encode alone is 76% / 34% / 90% of parallelSort |
| E10b-2 strings scale with cores | p1→p8 1.000×† / 1.263× / 1.037×†; f ≈ 0.06 / 0.25 / 0.07 | barely; consistent with a 5–25% parallel fraction (bandwidth unmeasured) |
| E10b-3 pinyin baseline for chinesenames | 21.390 ± 0.086 / 47.776 ± 0.763 / 241.693 ± 4.993 | first such measurement; nothing to supersede |
| 15 vs 16 threads for pAll | chunks 1 / 6 / 12 on permits; p8 ≈ pAll on strings | keep `availableProcessors()`; add `pAll-1` if wanted |

## How the numbers should be described (facts only; wording is yours)

- Not "the parallel husky sort beats `Arrays.parallelSort` at 32,000": it is a one-chunk serial radix sort there and
  serial radix/16 beats both. Label the 32k Auto/pAll rows "1 chunk (serial)" and print chunks and bits per row.
- Not "pAll used 16 threads on the permits": 1 / 6 / 12 chunks; `MIN_CHUNK_SIZE` caps the parallelism there. Not "the
  15-vs-8 thread asymmetry explains the request-9 inversion": refuted.
- Not "auto width is 6–12% faster" as a flat figure: +11.3% (serial), +21.6% (+15.5%), unresolved at 198,900.
- Not "parallel now beats serial radix/16": only by ~10% in the median fork at 100k–198.9k, not on every fork.
- Not "3.20× slower on english" without the iteration-tail caveat (2.98× on iterations 1–8) — the verdict is the same.
- Not "the parallel radix passes are slow": on the permits they are ≈ 4–5 of 30.9 ms (inference from request 9); the
  serial encode and permutation are the floor. Not "memory-bound": the JVM is not using the cores; bandwidth unmeasured.
- Do not average step-1 and step-3 pAll into one number without the fork means, and do not quote JMH's ± as covering
  fork bimodality — quote both runs, or the pooled fork median with its range.
- Do not compare against a request-9 chinesenames `systemSortParallel` row — none exists; request 9 was english-only.
- The step-6 extra rows are a replication of identical code, not duplicates or an error.

## Open questions for you

1. `MIN_CHUNK_SIZE = 16,384` caps the permits at 1 / 6 / 12 chunks — is the paper's parallel claim meant to hold at n ≤
   200k at all, or should the permits be presented as a serial-encode-bound case and the parallel claim confined to
   `Long[]` at ≥ 10M?
2. Given the serial encode floor (70.3 / 210.9 ms at 1M) is 76–90% of `Arrays.parallelSort`'s total on english and
   chinesenames, is parallelising `huskyEncode` (and the permutation) in scope, or is the variant to be described as
   "radix passes only"?
3. pAll = `availableProcessors()` (16): keep as is (my recommendation), or add a `pAll-1` row beside it?
4. Should the 32,000 Auto/pAll rows be labelled "1 chunk (serial)" in the paper's tables, with digit width and pass
   count printed per row (12 bits / 6 passes vs 16 / 4)?
5. Is a fixed `p16` row, or the unrun `Auto_p8_chunk4k`, wanted for the record, given both would still sit on the ≈ 26
   ms serial floor at 198,900?

## Files

All under `doc/`, unedited JMH `-rff` output. The runner scripts live outside the repo, so the exact commands are the
ones quoted in each section (run from the package root with the pinned JDK, each as its own `java -jar` invocation).

| file | sha256 | invocation |
|---|---|---|
| `req10-permits-auto.json` | `69a2182c776bb9d480b60f5bb2a03f34ec7d39448f66638d34834198cd8d8697` | run 1 step 1 (23:11 PT / 06:11Z) — the 10a headline |
| `req10-permits-auto-vs-16.json` | `aa351af6cc2352a3b5901fb141d80f7e9dfa1366595a8c007454425756c71d99` | run 1 step 2 |
| `req10-permits-parallel-vs-serial.json` | `b42a099dd8ed2238b86e38ef89a31f1018bc381bf50882a913eab81d59c7864b` | run 1 step 3 |
| `req10-strings-parallel.json` | `50b6bc7892781cb5ccb71a9f81057859c107d043fd74cdd7cebcce039971ddb9` | run 1 step 4 (10b) |
| `req10-strings-parallel-sweep.json` | `8617695daf484c8fdc9e13af16541e11ec962b3f0f3b3a5fbf6fb4d2b9b41c2c` | run 1 step 5 |
| `req10-parallelsort-pinyin.json` | `a7c88a995121f831b752580d351e838878e9f0f18e7d2d9a215134d7af459a20` | run 1 step 6 (six rows: both matched classes) |
| `req10-repeat-permits-auto.json` | `d6d2981b18254f2040b39029c12baeb194326310943230be389c25c1625ad170` | repeat step 1 (00:40 PT / 07:40Z), same command and jar |
| `req10-repeat-permits-auto-vs-16.json` | `66d6cd493e45a5c3a06a3966b49e008c6033e15fe5bebe64cb6a4e7b57c9e906` | repeat step 2 |
| `req10-repeat-permits-parallel-vs-serial.json` | `056b1dc39c4978603bb1ead1fc4b420dca02649280d7f2ade9bc66a494437b45` | repeat step 3 |
| `req10-extra-strings-encode-only.json` | `07646f0dd6e56441779ea2f8ea049641294644c8fcd9c8d445e7b21b714487a8` | supplement step 1 (01:12 PT / 08:12Z): `java -jar target/benchmarks.jar '\.StringSortBenchmarks\.huskyEncodeOnly$' -p n=1000000 -f 5 -wi 5 -i 10 -rf json -rff strings-encode-only.json` |
| `req10-extra-strings-serial-vs-parallel.json` | `37da8ece7a96a025916f95b5f023278954038b99dcde6dc8ac90295bbef2aeb2` | supplement step 2 (01:19 PT / 08:19Z), command in its section |

Plus `Run results from Yunlu 2026-09-17.md` (this file). The jar every row came from: `target/benchmarks.jar`, sha256
`ef53f4444d15a74f4b3920c73d368d25297157c1c71807282c413e2a1b606e4f`, 76,287,165 B, not committed.

## Appendix — per-fork means (ms/op) for every 10a row, run 1 → repeat

| step | benchmark | n | run 1 forks 1→5 | repeat forks 1→5 | pooled median | pooled min–max |
|---|---|---:|---|---|---:|---|
| 1 | pAll | 32,000 | 2.92 / 2.90 / 2.90 / 2.91 / 2.90 | 2.89 / 2.93 / 2.96 / 2.93 / 2.94 | 2.917 | 2.887–2.961 |
| 1 | systemSortParallel | 32,000 | 4.67 / 4.63 / 4.67 / 4.65 / 4.59 | 4.67 / 4.63 / 4.62 / 4.60 / 4.61 | 4.628 | 4.591–4.674 |
| 1 | pAll | 100,000 | 12.28 / **15.85** / 12.49 / 12.61 / 12.20 | 12.06 / 12.31 / 12.17 / 12.23 / 12.34 | 12.297 | 12.065–15.853 |
| 1 | systemSortParallel | 100,000 | 5.92 / 6.14 / 5.86 / 5.86 / 6.01 | 6.02 / 6.09 / 6.07 / 6.20 / 6.06 | 6.040 | 5.860–6.196 |
| 1 | pAll | 198,900 | 25.41 / 27.28 / 27.28 / 27.21 / 25.36 | 26.74 / 25.07 / 29.21 / 27.47 / 27.93 | 27.245 | 25.070–29.211 |
| 1 | systemSortParallel | 198,900 | 13.44 / 13.51 / 13.06 / 13.06 / 13.50 | 13.98 / 13.07 / 13.75 / 13.29 / 13.48 | 13.459 | 13.060–13.984 |
| 2 | 16_p8 | 32,000 | 3.31 / 3.32 / 3.30 / 3.29 / 3.31 | 3.28 / 3.24 / 3.30 / 3.38 / 3.27 | 3.300 | 3.235–3.382 |
| 2 | Auto_p8 | 32,000 | 2.97 / 2.91 / 2.90 / 2.92 / 2.96 | 2.88 / 2.86 / 2.92 / 2.96 / 2.91 | 2.914 | 2.859–2.975 |
| 2 | 16_p8 | 100,000 | **17.63** / 15.31 / 14.88 / 15.88 / 16.70 | 15.46 / 15.04 / 14.95 / 14.95 / 15.13 | 15.219 | 14.877–17.627 |
| 2 | Auto_p8 | 100,000 | 12.73 / 12.42 / 13.13 / 12.46 / 12.31 | 12.60 / 12.04 / 12.57 / **14.25** / 12.38 | 12.513 | 12.041–14.247 |
| 2 | 16_p8 | 198,900 | 28.30 / **39.09** / 30.34 / 34.19 / 30.06 | **38.72** / 29.81 / 29.83 / 36.66 / 37.07 | 32.264 | 28.296–39.095 |
| 2 | Auto_p8 | 198,900 | 34.88 / 29.33 / 34.59 / 35.41 / 26.77 | 27.35 / 29.87 / 27.74 / 35.55 / **37.74** | 32.229 | 26.768–37.738 |
| 3 | pAll | 32,000 | 2.98 / 2.92 / 2.93 / 2.98 / 3.01 | 3.02 / 2.94 / 3.08 / 3.00 / 3.09 | 2.990 | 2.925–3.086 |
| 3 | radixHuskySort16 | 32,000 | 2.70 / 2.68 / 2.70 / 2.68 / 2.68 | 2.76 / 2.77 / 2.75 / 2.74 / 2.67 | 2.699 | 2.671–2.774 |
| 3 | pAll | 100,000 | 12.61 / 12.42 / 12.46 / 12.30 / 12.32 | 13.50 / **18.03** / 12.94 / 12.89 / 15.09 | 12.748 | 12.299–18.027 |
| 3 | radixHuskySort16 | 100,000 | 13.31 / 14.31 / 12.71 / 14.61 / 12.39 | 15.25 / 14.18 / 12.70 / 15.30 / 15.73 | 14.244 | 12.389–15.733 |
| 3 | pAll | 198,900 | **37.04** / 25.75 / **38.68** / 27.14 / 28.28 | 26.05 / 28.98 / 24.78 / 27.78 / 25.16 | 27.459 | 24.780–38.679 |
| 3 | radixHuskySort16 | 198,900 | 28.11 / 28.32 / 29.01 / 33.60 / 33.38 | 28.85 / **36.93** / 34.42 / 31.76 / 28.88 | 30.386 | 28.113–36.926 |

Fork mean = mean of that fork's 10 measurement iterations; pooled = the 10 forks of both runs; bold = the forks
discussed as outliers in the text. Strings: step 4's fork means are in its table; every other strings row (sweep,
supplement, encode-only) has fork max ÷ min ≤ 1.093.
