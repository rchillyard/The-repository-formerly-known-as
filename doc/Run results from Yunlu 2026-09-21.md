# Run results from Yunlu — request 11, the cleanup pass and then everything again, 2026-09-21

Request 11 answered on one jar: 11a both invocations (Timsort vs adaptive insertion, and the optional binary-search form),
11b all three (the encode pair, `ParallelStringSortBenchmarks`, the full suite), plus a supplementary run of three short
invocations on the same jar the next night — a 50 s-warm-up measurement of the english@1M rows the first pass left
unconverged, the middle of your `0.2 < pn < 25` window, and a two-method permits confirmation. Raw JMH JSONs committed
alongside (Files); every full-suite row is in `Run results from Yunlu 2026-09-21 - appendix.md`. **11a: Timsort at every
cell you asked for.** Adaptive insertion is 1.14× slower at englishSaturating@1M (59.098 ± 1.538 vs 51.875 ± 0.653 ms/op,
every adaptive fork above every Timsort fork) — the cell your pn/4 rule predicted it would win, and the surprise you asked
to have flagged; it wins only in the middle of the window (unicode n ≤ 100,000, pn ≤ 11.5), which the supplement sampled.
**11b headline: `parallelRadixHuskySortAuto_pAll` beats `Arrays.parallelSort` at english@1M on this jar — 70.370 ± 0.710 vs
94.336 ± 7.760 (1.34× by score, 1.24–1.28× against the system sort's spike-free level) with a 50 s warm-up; the 2 s full
suite agrees (72.663 ± 4.505 vs 89.415 ± 3.106, 1.23×); the 1 s step-4 figure for the same row (111.772 ± 17.865, "1.24×
slower") is an unconverged mean and is superseded, not averaged in.** The permits invert request 10 and the saturating
encode costs 1.7–3.0× the masking one (Summary). † throughout = the two 99.9 % CIs overlap; ‡ = the row was still speeding
up inside its measurement window (mean of iterations 1–5 ÷ mean of 6–10 > 1.10 per fork; see Warm-up convergence), so its
score is above steady state; ratios are computed from unrounded JSON scores. Times are PT (UTC−7).

**Checkout**: branch `parallel-redesign`, tip **`82bcfa8`** ("Repair what the PR #66 merge cost the request document");
the last commit touching `src/` is **`15cc2ff`** ("Parallelize the encoding in the parallel sorter") — `git log
15cc2ff..82bcfa8 -- src/` is empty, so `15cc2ff` is the commit these numbers belong to, as you asked.

**Toolchain and build**: pinned Corretto **21.0.12+8-LTS**, Maven **3.9.16**, JMH **1.37** (from the JSON); `mvn -B test`
at the tip **423 tests, 0 failures** (Sat 14:26 PT, 38.8 s, `req11/mvn-test.log`, outside the repo — Files); `target/benchmarks.jar` **76,350,880
B**, sha256 `95ccd2c75924ecac33f104d1c2b1ab08467aa538d1548817c922c27779c1d408`, built Fri 14:51:40 PT (21:51:40Z)
2026-09-18, byte-identical at launch. A first attempt from that jar the same afternoon was killed before its first
measurement iteration by host contention from unrelated processes (thirteen 630 MB JVMs, swap 8 → 20 GiB, load 165);
nothing from it is used, and the run reported here started 23 h 48 min after the build on a rebooted, idle host. Every
invocation used `-f 5 -wi 5 -i 10 -rf json`: steps 1–4 and supplement 2 at the class default of 1 s iterations, the full
suite and supplement 3 with `-r 2s -w 2s`, supplement 1 with `-w 10s -r 2s`; `VM options: <none>` in every log; Cnt = 50
per row = 5 forks × 10 iterations; ± is JMH's 99.9 % CI half-width; one JSON per invocation, unedited.

**Environment**: aarch64 Graviton (Model 1 r1p1), **16 CPUs, 1 thread per core**, L1d/L1i 1 MiB (16 instances), L2 16 MiB
(16), L3 32 MiB (1); `free -h` before step 1: Mem 30 Gi total / 7.8 Gi used / 22 Gi available, **Swap 39 Gi / 2.0 Mi
used** — 8.1 Gi used and swap still 2.0 Mi after step 5; `uname -r` `6.12.103-127.188.amzn2023.aarch64`; host up 14 h 35
min at launch (rebooted 00:02 PT Sat). **No CPU-frequency or governor information is exposed to the guest**
(`/sys/devices/system/cpu/cpu0/cpufreq` absent). Both units ran detached in `app.slice` (`husky-req11-20260919-2138.service`,
`husky-req11-extra-20260921-0728.service`; cgroup verified on the JMH parent and a fork PID), never under the CPU-quota'd
`kiro.slice`. **The `ForkJoinPool` probe** (`jshell -s -`, as in request 10) printed **`15 16`** before step 1, after step 5,
and before and after the supplement — 15 common-pool workers plus the caller for `Arrays.parallelSort`,
`availableProcessors()` = 16 for pAll, in every invocation. Units launched Sat 14:38:02 and Mon 00:28:34 PT; 120 s idle gap
before each step; `uptime` immediately before and after each (UTC in brackets):

| unit / step | invocation | before | load average before (1 / 5 / 15) | after | load average after | wall |
|---|---|---|---|---|---|---:|
| step 1 | cleanup | Sat 14:40:04 (21:40:04Z) | 0.21, 0.23, 0.26 | 15:30:28 | 1.66, 1.44, 1.28 | 50 min |
| step 2 | cleanup-binary | 15:32:28 | 0.34, 1.04, 1.16 | 15:39:55 | 1.17, 1.40, 1.31 | 7 min |
| step 3 | encode-masking-vs-saturating | 15:41:55 | 0.23, 0.97, 1.16 | 16:08:17 | 1.14, 1.22, 1.26 | 26 min |
| step 4 | strings-parallel-full | 16:10:17 | 0.24, 0.85, 1.11 | 17:59:05 | **12.89**, 9.82, 6.24 | 1 h 49 min |
| step 5 | full-suite | 18:01:05 (01:01:05Z) | 1.94, **6.63**, 5.50 | Sun 21:28:23 (04:28:23Z) | 1.15, 1.14, 1.33 | 27 h 27 min |
| supplement 1 | english-1m-longwarmup | Mon 00:30:35 (07:30:35Z) | 0.28, 0.31, 0.31 | 00:56:21 | 12.64, 10.14, 5.90 | 26 min |
| supplement 2 | cleanup-unicode-smalln | 00:58:21 | 1.90, 6.84, 5.20 | 01:08:52 | 1.19, 2.00, 3.32 | 11 min |
| supplement 3 | permits-confirm | 01:10:52 | 0.29, 1.40, 2.93 | 01:26:10 | 11.24, 8.15, 5.24 | 15 min |

The after-loads of 11–13 are the steps that end on `systemSortParallel` forks; step 5's 5-minute 6.63 and supplement 2's
1.90 / 6.84 are step 4's and supplement 1's own tails decaying through the 120 s gap (first monitor sample after step 4
ended, 00:59:40Z: load1 7.18, CPU 98.9 % idle, no java process) — request 10's mechanism, not another process. Step 5 took
27 h 27 min, not request 4's 20 h 30 min, because the suite has since gained 152 rows (`ParallelStringSortBenchmarks` 72, six new
`StringSortBenchmarks` methods 54, seven new `PermitSortBenchmarks` methods 21, `integerSinglePivotQuicksort` 3,
`ParallelRadixSortBenchmarks.systemSortParallel` 2) and 60 guard-throw fork start-ups.

## Summary

- **11a — use Timsort.** It wins or ties at all six requested cells: adaptive ÷ Timsort = 1.06×† / 1.14× (englishSaturating
  200k / 1M), 1.03×† / 2.80× (unicode), 9.77× / 47.88× (pinyin); adaptive's cost grows 410× from englishSaturating@1M to
  pinyin@1M (59.098 → 24225.361) against Timsort's 9.75× (51.875 → 505.914).
- **The pn/4 rule fails at the one requested cell it called for adaptive**: englishSaturating@1M (pn = 0.47), 59.098 ± 1.538
  vs 51.875 ± 0.653, adaptive's best fork 54.52 above Timsort's worst 52.94, both converged; unicode@200k (pn = 23, also in
  the window) is a tie. In the window's middle it holds: unicode n = 20k / 50k / 100k, adaptive ÷ Timsort = 0.63× (1.087 ±
  0.018 / 1.729 ± 0.032), 0.68× (5.966 ± 0.243 / 8.735 ± 0.212), 0.82× (15.762 ± 0.269 / 19.189 ± 0.213). Crossover between
  pn 11.5 and 23; the lower edge does not deliver.
- **Binary-search insertion ÷ Timsort = 5.18× / 4.67×** on english (48.547 ± 0.603 / 9.375 ± 0.165; 242.002 ± 2.377 / 51.875
  ± 0.653) — §A.5's 4.5× is the binary-search form; 2.07× / 1.89× on unicode, where at 1M it beats adaptive (0.68×).
- **Saturating encode costs 1.7–3.0× the masking encode**, a range across the two invocations: step 3 (1 s) 2.402 ± 0.042 vs
  1.433 ± 0.031 / 28.773 ± 2.684 vs 9.542 ± 0.292 / 176.393 ± 3.746 vs 61.829 ± 1.399 = 1.68× / 3.02× / 2.85×; full suite (2 s)
  2.349 ± 0.069 vs 1.272 ± 0.076 / 24.831 ± 1.184 vs 11.103 ± 0.937 / 152.534 ± 4.030 vs 79.242 ± 2.045 = 1.85× / 2.24× /
  1.92×. By your framing, the argument against the change — for the *serial* encode; the parallel sorter now encodes in
  parallel, and the cleanup side of the trade was not measured for the masking coder.
- **pAll vs `Arrays.parallelSort`, english@1M: husky faster, 1.34× by score / 1.24–1.28× spike-free** (70.370 ± 0.710 vs
  94.336 ± 7.760, 50 s warm-up, a four-method invocation; every pAll fork ≤ 72.2 below every parallelSort fork ≥ 86.0). Full suite 1.23× (72.663 ±
  4.505 vs 89.415 ± 3.106). Step 4's 111.772 ± 17.865 is not converged (t 1.67) and must not be used. `Auto_p2` english@1M
  converges to 128.152 ± 1.708 (1.91× faster than serial 244.663 ± 12.775); its two published means (268.135 ± 21.034,
  244.657 ± 16.039) are warm-up-inflated in both files.
- **Permits invert request 10**: pAll 1.55× / 1.13× / 1.51× faster than `Arrays.parallelSort` at 32k / 100k / 198,900 in the
  full suite, 1.55× / 1.25× / 1.57× in the two-method confirmation (3.173 ± 0.071 vs 4.928 ± 0.049; 5.077 ± 0.102 vs 6.360 ±
  0.151; 8.971 ± 0.200 vs 14.075 ± 0.135); request 10 measured 0.63× / 2.20× / 1.99× of the system sort's time — slower at
  100k and 198,900. Serial `radixHuskySort16` @198,900 is unchanged (32.125 ± 2.254 vs 30.5–32.2 then): the parallel encode.
- **Full suite vs request 4 (e92610f): no regression from the radix rework.** 42 of 415 common rows moved ≥ 10 % with
  disjoint CIs; against the String-class drift floor of untouched `systemSort` rows (0.99–1.18), three groups stand out:
  `huskyEncodeOnly english` 2.02–3.36× (a changed measurement — the english corpus changed coder),
  `parallelRadixHuskySort11_p1` +19 % / +25 % with p4 / p8 −9 to −11 % (the parallel encode's fixed cost at low p), and
  `collapsedBitsRadixHuskySort16` 0.88–0.93 at every fixedHighBits with `…8` 1.05–1.13 on the same cells (a per-pass cost
  shift between digit widths, systematic and small).

## Correctness was checked before any timing was trusted

`mvn -B test` at the tip: 423 / 0 / 0 / 0. The external `ValidateSorts` harness (request 10's sections A–G plus a new
**section H** for this request's code) ran against this jar: **508 / 508 checks passed** (`req11/harness.log`). Section H
checks what request 11 changed underneath the suite: **H1** `AdaptiveInsertionSort` sorted, a permutation, equal to
`Arrays.sort` and **stable** on random / sorted / reversed / nearly-sorted / all-equal inputs, n = 0 … 50,000; **H2**
`RadixHuskySort` at AUTO / 8 / 11 / 16 bits on every element type the suite sorts (strings on all three coders, ints, longs,
dates, tuples, permits) equal to `Arrays.sort` after the long-array and final-pass removals; **H3** `ParallelRadixHuskySort`
at p = 1 / 2 / 4 / 8 / pAll with the parallel encode — correct, stable, **identical to the serial result** — plus
`chooseDigitBits(n, chunks)` for every (n, p) run here; **H4** the saturating coders non-decreasing over all 65,536
characters while the masking `englishCoder` is shown *not* monotonic (first decrease at char 64; ties "don't" with "dongt");
**H5** Timsort, adaptive and binary cleanups of the hand-over array agree per coder. Each JSON then passed structural
validation (`runner/validate-json.py --preset req11`, outside the repo: exact (method, params) row set, Cnt = 50 every row, `rawData` 5 × 10, no
null/NaN, ms/op, forks/wi/i = 5/5/10, `score == mean(rawData)`, `scoreConfidence == score ± scoreError`, no JVM flags; the
full suite's extra checks assert 567 rows and exactly the 12 guard-throw rows absent), and every row was cross-checked
against its JMH stdout table (`runner/jmh-json-vs-log.py`): **all 679 rows in the eight files match to the 3 decimals JMH
prints**, and `# Fork:` counts equal rows × 5 (+ 60 for step 5's throwing rows: 2,895) in every log.

## Reading aid — what each `ParallelStringSortBenchmarks` row actually ran

`chunks = max(1, min(p, n / 16384))` (`ParallelRadixHuskySort.java:212`, `MIN_CHUNK_SIZE = 1 << 14` at `:62`);
`chooseDigitBits(n, chunks)` takes the widest digit with `buckets × chunks ≤ n / 4`, clamped to [8, 16]; passes = ⌈64 /
bits⌉. New since request 10: the encode is chunked onto the pool too (`doCoding`, `:203–204`), so the timed region is
`Arrays.copyOf` → **parallel** encode → **parallel** counting passes → serial `applyPermutation` (`:216`) → serial cleanup
`Arrays.sort`. `serialRadixHuskySortAuto` is `RadixHuskySort` choosing its own width, chunks = 1.

| n | serial Auto, `Auto_p1` | `Auto_p2` | `Auto_p4` | `Auto_p8` | `Auto_pAll` (16) | `11_p8` |
|---:|---|---|---|---|---|---|
| 32,000 | 1 chunk / 12 bits / 6 passes | **1** / 12 / 6 | **1** / 12 / 6 | **1** / 12 / 6 | **1** / 12 / 6 | **1** / 11 / 6 |
| 200,000 | 1 / 15 / 5 | 2 / 14 / 5 | 4 / 13 / 5 | 8 / 12 / 6 | 12 / 12 / 6 | 8 / 11 / 6 |
| 1,000,000 | 1 / 16 / 4 | 2 / 16 / 4 | 4 / 15 / 5 | 8 / 14 / 5 | 16 / 13 / 5 | 8 / 11 / 6 |

So at n = 32,000 **every `Auto_*` and `11_p8` row is one chunk — the serial path of the parallel class** — and "p" is a
label there, not a thread count (your Q4: the tables print chunks, bits and passes; label those rows "1 chunk (serial)" in
the paper). The permits follow the same rule: 1 / 6 / 12 chunks at 32k / 100k / 198,900.

## 11a step 1 — the cleanup pass: Timsort vs adaptive insertion sort (ms/op)

`java -jar target/benchmarks.jar "CleanupPassBenchmarks.(timsortCleanup|adaptiveInsertionCleanup)$" -f 5 -wi 5 -i 10 -rf
json -rff cleanup.json` (`req11-cleanup.json`, 12 rows). Both arms time `Arrays.copyOf` of the hand-over array plus the
sort (`CleanupPassBenchmarks.java:114`, `:124`, `:135`), so the copy is inside both and the ratios stand. pn = p × n (p = 4.7e-7
englishSaturating, 1.15e-4 unicode, `CleanupPassBenchmarks.java:32–33`; pinyin = 4 × your 35 / 175); unicode@200k, blank in
your text, is computed from the same p. Your rule: adaptive for 0.2 < pn < 25.

| coder, n | `timsortCleanup` | `adaptiveInsertionCleanup` | adaptive ÷ Timsort | pn | your pn/4 → predicted | agrees? |
|---|---:|---:|---:|---:|---|---|
| englishSaturating, 200,000 | 9.375 ± 0.165 | 9.899 ± 0.647 | 1.06×† | 0.094 | 0.024 → Timsort | yes, as a tie |
| englishSaturating, 1,000,000 | 51.875 ± 0.653 | 59.098 ± 1.538 | **1.14×** | 0.47 | 0.12 → adaptive | **no — the surprise** |
| unicode, 200,000 | 25.232 ± 0.513 | 25.978 ± 1.049 | 1.03×† | 23 | (5.75 computed) → adaptive | no — a tie |
| unicode, 1,000,000 | 155.892 ± 1.737 | 436.596 ± 2.786 | 2.80× | 115 | 28.8 → Timsort | yes |
| pinyin, 200,000 | 99.245 ± 1.106 | 969.829 ± 2.303 | 9.77× | 140 | 35 → Timsort | yes |
| pinyin, 1,000,000 | 505.914 ± 4.105 | 24225.361 ± 183.888 | 47.88× | 700 | 175 → Timsort | yes |

**E11a-1 — Timsort at every requested cell, and the englishSaturating@1M miss is real, not noise.** Timsort forks 52.25 /
50.91 / 51.72 / 51.57 / 52.94, adaptive forks 61.27 / 61.92 / 59.73 / 54.52 / 58.05: CIs disjoint, every adaptive fork above
every Timsort fork, both arms converged (t 1.00 / 1.01), no host spike in the window. The two † cells are ties with one wide
adaptive fork each (english@200k adaptive forks 9.16 / 9.70 / 9.09 / 9.88 / **11.66** vs Timsort 9.21–9.69, no external spike
that minute; unicode@200k 26.21 / 26.15 / 26.40 / 25.00 / 26.12 vs 24.94–25.66). Of the two requested cells inside your
window (pn 0.47 and 23) adaptive won neither, and both sit at the window's edges — the middle is the supplement below.
Scale: the whole english@1M cleanup is 52–59 ms inside a 276.778 ± 6.926 ms serial `radixHuskySortAuto`, so this choice
moves that headline by ≈ 2.6 %; the reason to fix Timsort is the tail — adaptive degrades 410× across the coder range where
Timsort degrades 9.75×.

## 11a step 2 — binary-search insertion, the paper's "insertion sort" (ms/op)

`java -jar target/benchmarks.jar "CleanupPassBenchmarks.binaryInsertionCleanup$" -p coder=englishSaturating,unicode -f 5
-wi 5 -i 10 -rf json -rff cleanup-binary.json` (`req11-cleanup-binary.json`, 4 rows; pinyin excluded, as you said — it
throws). Timsort / adaptive columns are step 1's (a different invocation, 7 min earlier, same flags).

| coder, n | `binaryInsertionCleanup` | `timsortCleanup` (step 1) | `adaptiveInsertionCleanup` (step 1) | binary ÷ Timsort | binary ÷ adaptive |
|---|---:|---:|---:|---:|---:|
| englishSaturating, 200,000 | 48.547 ± 0.603 | 9.375 ± 0.165 | 9.899 ± 0.647 | **5.18×** | 4.90× |
| englishSaturating, 1,000,000 | 242.002 ± 2.377 | 51.875 ± 0.653 | 59.098 ± 1.538 | **4.67×** | 4.09× |
| unicode, 200,000 | 52.356 ± 0.560 | 25.232 ± 0.513 | 25.978 ± 1.049 | 2.07× | 2.02× |
| unicode, 1,000,000 | 294.985 ± 1.631 | 155.892 ± 1.737 | 436.596 ± 2.786 | 1.89× | **0.68×** |

**E11a-2 — §A.5's 4.5× is confirmed as the binary-search form** (5.18× / 4.67× bracket it on the production coder); on
unicode, where Timsort has more merging to do, the factor is 2.07× / 1.89×. Binary insertion's n log n comparisons are
indifferent to disorder, so at unicode@1M it is *faster* than adaptive (294.985 / 436.596 = 0.68×). All four rows
converged (t 0.98–1.01), fork spread ≤ 1.05×.

## 11a supplement — inside the window: unicode at n = 20,000 / 50,000 / 100,000 (ms/op)

`java -jar target/benchmarks.jar "CleanupPassBenchmarks.(timsortCleanup|adaptiveInsertionCleanup)$" -p coder=unicode -p
n=20000,50000,100000 -f 5 -wi 5 -i 10 -rf json -rff cleanup-unicode-smalln.json` (`req11-extra-cleanup-unicode-smalln.json`,
6 rows, class-default 1 s iterations as step 1; Mon 00:58 PT). Not requested; run because both requested in-window points
were at the window's edges. pn/4 computed from p = 1.15e-4, which reproduces your 28.8 at 1M.

| n | `timsortCleanup` | `adaptiveInsertionCleanup` | adaptive ÷ Timsort | pn | pn/4 → predicted | agrees? |
|---:|---:|---:|---:|---:|---|---|
| 20,000 | 1.729 ± 0.032 | **1.087 ± 0.018** | **0.63×** | 2.3 | 0.575 → adaptive | yes |
| 50,000 | 8.735 ± 0.212 | **5.966 ± 0.243** | **0.68×** | 5.75 | 1.44 → adaptive | yes |
| 100,000 | 19.189 ± 0.213 | **15.762 ± 0.269** | **0.82×** | 11.5 | 2.88 → adaptive | yes |
| 200,000 (step 1) | 25.232 ± 0.513 | 25.978 ± 1.049 | 1.03×† | 23 | 5.75 → adaptive | no — tie |
| 1,000,000 (step 1) | 155.892 ± 1.737 | 436.596 ± 2.786 | 2.80× | 115 | 28.8 → Timsort | yes |

**E11a-3 — your window exists; it is narrower at the bottom and ends before 25.** Adaptive wins at pn 2.3 / 5.75 / 11.5
with every fork disjoint (20k Timsort forks 1.672–1.813 vs adaptive 1.047–1.147; 50k 8.611–8.852 vs 5.793–6.061; 100k
18.948–19.404 vs 15.308–16.299; all converged), the advantage shrinking monotonically (adaptive ÷ Timsort 0.63× → 0.68× → 0.82× → 1.03×†) as
`N + pn²/4` against `N log(runs)` predicts, so the crossover lies between n = 100,000 and 200,000 — pn ≈ 12–23. What did not
appear is the win at the window's lower edge on the production coder (pn 0.09–0.47 gave 1.06×† / 1.14× *Timsort*), so for
`englishSaturating` at 200k–1M the answer stays Timsort; pn 1–10 on that coder would need n ≈ 2–20 M and was not run.

## 11b step 3 — the encode: `englishCoder` (masking) vs `englishSaturatingCoder` (ms/op)

`java -jar target/benchmarks.jar "StringSortBenchmarks.huskyEncodeOnlyEnglish" -f 5 -wi 5 -i 10 -rf json -rff
encode-masking-vs-saturating.json` (`req11-encode-masking-vs-saturating.json`, 18 rows). Both methods encode `state.master`
with the English 6-bit coder pair whatever the corpus (`StringSortBenchmarks.java:167–168`, `:172–173`), so **only the six
english rows answer the question; the twelve chinese / chinesenames rows are an English coder on Chinese text — a control
for the `min`-vs-`&` code path, nothing about the coders' intended use** — printed, kept in the JSON, not read further.

| corpus, n | `huskyEncodeOnlyEnglishMasking` | `huskyEncodeOnlyEnglishSaturating` | saturating ÷ masking | meaningful? |
|---|---:|---:|---:|---|
| english, 32,000 | 1.433 ± 0.031 | 2.402 ± 0.042 | **1.68×** | yes |
| english, 200,000 | 9.542 ± 0.292 | 28.773 ± 2.684 | **3.02×** | yes (saturating forks 23.52–34.80, spread 1.48×) |
| english, 1,000,000 | 61.829 ± 1.399 | 176.393 ± 3.746 | **2.85×** | yes |
| chinese, 32,000 | 0.733 ± 0.021 | 1.037 ± 0.022 | 1.42× | no — English coder on Chinese text, control only |
| chinese, 200,000 | 5.070 ± 0.040 | 7.182 ± 0.094 | 1.42× | control only |
| chinese, 1,000,000 | 25.542 ± 0.131 | 36.779 ± 0.087 | 1.44× | control only |
| chinesenames, 32,000 | 0.884 ± 0.008 | 0.906 ± 0.006 | 1.02× | control only |
| chinesenames, 200,000 | 6.119 ± 0.169 | 13.108 ± 0.372 | 2.14× | control only |
| chinesenames, 1,000,000 | 51.637 ± 1.235 | 69.188 ± 0.612 | 1.34× | control only |

The full suite re-measured the same six english rows at 2 s iterations, in the same file as `huskyEncodeOnly` (the
production path, `:152`, which uses `englishSaturatingCoder` for the english corpus since `:85`):

| english, n | masking, step 3 (1 s) | masking, full suite (2 s) | saturating, step 3 | saturating, full suite | sat ÷ mask, step 3 | sat ÷ mask, full | `huskyEncodeOnly`, full |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 1.433 ± 0.031 | 1.272 ± 0.076‡ | 2.402 ± 0.042 | 2.349 ± 0.069 | 1.68× | 1.85× | 2.244 ± 0.072 |
| 200,000 | 9.542 ± 0.292 | 11.103 ± 0.937‡ | 28.773 ± 2.684 | 24.831 ± 1.184 | 3.02× | 2.24× | 22.106 ± 1.329 |
| 1,000,000 | 61.829 ± 1.399 | 79.242 ± 2.045 | 176.393 ± 3.746 | 152.534 ± 4.030 | 2.85× | 1.92× | 136.906 ± 4.227 |

**E11b-1 — saturating is materially slower to encode; by your own framing that is the argument against the change, and
the factor must be quoted as a range.** In both invocations, at every english size, every saturating fork is above every
masking fork with CIs disjoint. But the two invocations disagree per arm beyond their CIs on identical code — masking@1M
61.829 → 79.242 (×1.28), saturating@1M 176.393 → 152.534 (×0.86); both arms converged at 1M in both runs, JVM at 88–106 %,
no competing process, swap flat — a between-invocation shift of ±15–28 % that ±2–4 % CIs do not cover and these logs cannot
explain (iteration length, JIT variant, heap sizing). So: **1.7–2.0× at 32k, 2.2–3.4× at 200k, 1.9–2.9× at 1M, i.e. +73 to
+115 ms per million words** (152.534 − 79.242; 176.393 − 61.829). At 200k the full suite's masking row is itself ‡ (t 1.29,
forks 10.58–11.70), so its 2.24× is a lower bound (iterations 8–10 give 2.75×; step 3's give 3.40×). A third estimate in the
same suite, `huskyEncodeOnly english@1M` 136.906 ± 4.227 (the production path, same coder), is 1.11× below `…Saturating`
152.534 — same-run same-code noise ≈ 10 %. Two limits. (i) The penalty lands in full only on serial `RadixHuskySort`; since
`15cc2ff` `ParallelRadixHuskySort` encodes in parallel (your Q2: 72.9 → 21.0 ms at 8 chunks). (ii) It is not a net verdict
on the coder: the cleanup saving that saturation buys over `englishCoder` (17,506 → 16,641 runs) was not measured —
`cleanup.json` has no masking-coder cell; the measured unicode → englishSaturating saving (155.892 → 51.875, −104.0 ms at
1M) is the character-count lever, not saturation. Masking at 61.8–79.2 ms per million brackets your hand-harness 69 ms.

## 11b step 4 — `ParallelStringSortBenchmarks`, all 72 rows at 1 s iterations (ms/op)

`java -jar target/benchmarks.jar "ParallelStringSortBenchmarks" -f 5 -wi 5 -i 10 -rf json -rff strings-parallel-full.json`
(`req11-strings-parallel-full.json`). ‡ = not converged (Warm-up convergence); the full suite re-measured the same 72 rows
at 2 s (appendix) and the supplement re-measured the english@1M headline rows with a 50 s warm-up.

| corpus, n | `serialRadixHuskySortAuto` | `Auto_p1` | `Auto_p2` | `Auto_p4` | `Auto_p8` | `Auto_pAll` | `11_p8` | `systemSortParallel` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| english, 32,000 | 4.558 ± 0.152‡ | 4.832 ± 0.158‡ | 4.738 ± 0.150‡ | 4.828 ± 0.150 | 4.597 ± 0.147‡ | 4.754 ± 0.155‡ | 5.336 ± 0.308‡ | 4.643 ± 0.064 |
| english, 200,000 | 51.230 ± 2.163 | 47.190 ± 2.508 | 34.283 ± 1.374‡ | 24.333 ± 1.727‡ | 21.864 ± 1.289‡ | 20.994 ± 0.987‡ | 23.895 ± 1.346‡ | 10.329 ± 0.160 |
| english, 1,000,000 | 238.916 ± 5.087 | 236.205 ± 5.145 | 268.135 ± 21.034 (slow throughout, see below) | 119.731 ± 10.893‡ | 110.818 ± 15.042‡ | 111.772 ± 17.865‡ | 117.742 ± 16.729‡ | 90.495 ± 2.598 |
| chinese, 32,000 | 2.059 ± 0.050 | 2.349 ± 0.183 | 2.341 ± 0.176 | 2.396 ± 0.176 | 2.385 ± 0.180 | 2.400 ± 0.193 | 2.219 ± 0.176 | 2.985 ± 0.013 |
| chinese, 200,000 | 10.853 ± 0.316 | 11.019 ± 0.100 | 8.407 ± 0.065 | 6.761 ± 0.065 | 6.363 ± 0.046 | 6.527 ± 0.083 | 6.082 ± 0.096 | 5.993 ± 0.027 |
| chinese, 1,000,000 | 58.090 ± 2.289 | 59.971 ± 2.908‡ | 41.891 ± 1.823 | 35.591 ± 0.673 | 30.654 ± 0.370 | 30.009 ± 0.500 | 30.647 ± 0.506 | 41.178 ± 2.492 |
| chinesenames, 32,000 | 21.358 ± 0.528 | 21.937 ± 0.490 | 22.117 ± 0.559 | 21.825 ± 0.564 | 21.395 ± 0.461 | 22.397 ± 0.690 | 20.848 ± 0.410 | 20.854 ± 0.223 |
| chinesenames, 200,000 | 151.563 ± 1.284 | 150.852 ± 1.120 | 127.748 ± 0.243 | 116.614 ± 0.970 | 114.232 ± 1.797 | 110.294 ± 0.862 | 111.313 ± 1.130 | 46.195 ± 0.918 |
| chinesenames, 1,000,000 | 753.332 ± 10.178 | 760.059 ± 19.567 | 648.079 ± 22.275 | 590.016 ± 11.645 | 557.670 ± 5.759 | 549.370 ± 11.735 | 551.322 ± 4.505 | 230.477 ± 2.869 |

**E11b-2 — where the parallel husky sort stands against `Arrays.parallelSort`, per cell, in both invocations** (baseline ÷
candidate from unrounded scores; > 1 = husky faster; the fastest husky row is chosen among the six `parallelRadix*` rows):

| corpus, n | step 4: parallelSort ÷ pAll | full suite: parallelSort ÷ pAll | full suite fastest husky | parallelSort ÷ fastest (full) | serial ÷ pAll (full) |
|---|---:|---:|---|---:|---:|
| english, 32,000 (1 chunk) | 0.98×† | 1.13× (pAll ‡) | `Auto_p2` 3.938 ± 0.238‡ | 1.14× | 0.96×† |
| english, 200,000 | 0.49× | 0.52× | `Auto_p8` 18.851 ± 0.967 | 0.53× | 2.55× |
| english, 1,000,000 | 0.81× (pAll ‡) | **1.23×** (pAll ‡; supplement 1.34×) | `Auto_pAll` 72.663 ± 4.505‡ | 1.23× | 3.52× |
| chinese, 32,000 (1 chunk) | 1.24× | 1.03×† | `11_p8` 2.764 ± 0.155 | 1.08× | 0.70× |
| chinese, 200,000 | 0.92× | 0.90× | `11_p8` 6.325 ± 0.103 | 0.95× | 1.66× |
| chinese, 1,000,000 | 1.37× | **1.27×** | `11_p8` 31.836 ± 0.454 | 1.30× | 1.76× |
| chinesenames, 32,000 (1 chunk) | 0.93× | 0.91× | `Auto_pAll` 23.343 ± 0.412 | 0.91× | 0.98×† |
| chinesenames, 200,000 | 0.42× | 0.43× | `Auto_pAll` 111.003 ± 1.574 | 0.43× | 1.36× |
| chinesenames, 1,000,000 | 0.42× | 0.42× | `Auto_pAll` 558.039 ± 10.631 | 0.42× | 1.35× |

The standing gap is english@200k (≈ 1.9× behind) and chinesenames at 200k / 1M (≈ 2.4× behind, its pinyin cleanup
serial); at 1M on english and chinese the husky sort wins. Against its own serial form the parallel sort saturates at p8 /
pAll (within 6 %; 5.5 % at english@1M, both rows ‡): 2.55× / 3.52× english 200k / 1M, 1.66× / 1.76× chinese, 1.36× / 1.35× chinesenames (full suite). `11_p8`
is the fastest husky at chinese 32k / 200k / 1M and within the fastest Auto row's CI elsewhere. Four caveats. **(i) The
english@1M husky rows at 1 s are not converged**: every pAll fork trends 100–213 → 69–80 ms within its ten iterations
(fork 5: 127, 127, 213, 158, 129, 108, 82, 71, 69, 71; t 1.67), so 111.772 is a warm-up-inflated mean — its iterations 8–10
average 72.6, equal to the full suite's score — and the "1.24× slower" it implies is withdrawn (supplement). **(ii)
`Auto_p2` english@1M** (2 chunks, 16 bits) is slow in every fork here (262.6 / 256.0 / 283.2 / 269.4 / 269.5, all iterations
158–360) and in the full suite drops only at iterations 9–10 (fork 1: 252, 281, 277, 256, 258, 248, 230, 243, 181, 175) — a
reproducible ≈ 28 s warm-up specific to this row; both published means are inflated, the supplement gives the converged
value. **(iii) The 1-chunk rows shift between invocations**: on chinese@32k the parallel class at one chunk is 1.14–1.17×
its serial form here but 1.38–1.42× in the full suite (2.813–2.896 vs 2.040 ± 0.041) on identical code, while
`serialRadixHuskySortAuto` (2.059 → 2.040) and `systemSortParallel` (2.985 → 2.983) did not move — so no verdict finer than
±25 % at n = 32,000 and no fixed "parallel-class overhead" figure. **(iv) Step 4 and the full suite disagree beyond CI on 40
of 72 rows**, for three identifiable reasons: (i–ii), (iii), and english@32k, where every step-4 husky row is 1.12–1.33× its 2 s
value (`systemSortParallel` 1.03×; both still warming, t 1.09–1.13 at 1 s, 1.16–1.23 at 2 s); chinese and chinesenames at
200k / 1M agree (step 4 ÷ full suite 0.91–1.02×). Rule: quote the
longest-warm-up converged measurement of a row; when two converged measurements exist, quote both.

## 11b supplement — english@1M with a 50 s warm-up per fork (the headline row, settled)

`java -jar target/benchmarks.jar
"edu.neu.coe.huskySort.sort.huskySort.ParallelStringSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll|parallelRadixHuskySortAuto_p2|serialRadixHuskySortAuto)$"
-p corpus=english -p n=1000000 -w 10s -r 2s -f 5 -wi 5 -i 10 -rf json -rff english-1m-longwarmup.json`
(`req11-extra-english-1m-longwarmup.json`, 4 rows; anchored on the fully-qualified class so
`StringSortBenchmarks.systemSortParallel` cannot co-match; four methods in one invocation, `systemSortParallel` last —
your two-methods rule not met here either, as for the full suite; Mon 00:30 PT). t = mean(iterations 1–5) ÷ mean(6–10) per fork.

| method | `-w 10s -r 2s` | t per fork | fork means | full suite (2 s) | step 4 (1 s) |
|---|---:|---|---|---:|---:|
| `systemSortParallel` | 94.336 ± 7.760 | 1.16 / 1.16 / 1.12 / 1.12 / 1.17 | 86.0 / 88.9 / 98.3 / 100.6 / 97.9 | 89.415 ± 3.106 | 90.495 ± 2.598 |
| `parallelRadixHuskySortAuto_pAll` | **70.370 ± 0.710** | 1.00 / 0.99 / 1.00 / 0.97 / 0.99 | 69.7 / 69.5 / 69.5 / 70.8 / 72.2 | 72.663 ± 4.505‡ | 111.772 ± 17.865‡ |
| `parallelRadixHuskySortAuto_p2` | **128.152 ± 1.708** | 0.98 / 1.03 / 1.01 / 1.01 / 1.00 | 129.8 / 128.6 / 127.2 / 124.9 / 130.3 | 244.657 ± 16.039‡ | 268.135 ± 21.034 |
| `serialRadixHuskySortAuto` | 244.663 ± 12.775 | 1.04 / 1.02 / 1.04 / 1.04 / 1.08 | 246.6 / 245.0 / 241.4 / 243.0 / 247.3 | 256.039 ± 5.808 | 238.916 ± 5.087 |

**E11b-3 — pAll beats `Arrays.parallelSort` at english@1M: 94.336 / 70.370 = 1.34× by score, CIs disjoint (69.7–71.1 vs
86.6–102.1), every pAll fork (≤ 72.2) below every parallelSort fork (≥ 86.0), fork-extreme range 1.19–1.45×.**
`systemSortParallel`'s t = 1.15 is not warm-up — its iteration 1 is at level (89.9) and iterations 2–3 of *every* fork spike
(per-iteration means 89.9 / 112.8 / 128.1 / 86.3 / 86.5 / 87.5 / 86.7 / 86.2 / 86.4 / 92.9), a periodic event ≈ 52–56 s into
the fork that the serial row also shows; so 94.336 is ≈ 7 % above the system sort's spike-free level 87.5 (iterations 4–10),
against which pAll is **1.24×** faster (iterations 8–10: 1.25×; medians: 1.28×). Quote 1.24–1.34×, with 70.370 as pAll's
converged level; the full suite's 72.663 was ≈ 3 % high (1.23× by score, 1.31× by iterations 8–10). `Auto_p2` converges to
128.152 ± 1.708 — 1.91× faster than serial (244.663) and 2.01× faster than the full suite's p1 (258.118 ± 5.906), as two
16-bit chunks should be; both earlier means were 1.9–2.1× inflated and should be replaced by this row. `serialRadixHuskySortAuto` at 1M reads 238.9 / 244.7 / 256.0 across the three invocations; sixteen threads buy 3.48× over it here
(244.663 / 70.370). Convergence of these rows is wall-time driven (steady ≈ 13–16 s into a fork for pAll, ≈ 28 s for p2),
which is why `-wi 5` at 1 s or 2 s leaves them 50–90 % above steady state with an innocent-looking CI.

## 11b step 5 — the full suite again (2 s iterations; ms/op)

`java -jar target/benchmarks.jar -e "CleanupPassBenchmarks" -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-suite.json`
(`req11-full-suite.json`, **567 rows**, Sat 18:01 → Sun 21:28 PT). **One deviation from "no filter": `-e
CleanupPassBenchmarks`** excludes the diagnostic class steps 1–2 had just measured with your flags, because its
`binaryInsertionCleanup` throws on the unfiltered pinyin sweep; everything else is request 4's invocation. 579 (method,
params) rows were attempted (2,895 `# Fork:` lines); **12 are absent from the JSON by design** — the benchmark throws for that
parameter and JMH (its default `-foe false`) logs the failure and continues: `StringSortBenchmarks.systemSortPinyin` × {english,
chinese} × 3 n (`StringSortBenchmarks.java:193`) and `StringSortBenchmarks.msdStringSort` × {chinese, chinesenames} × 3 n
(`:255`). Neither is a row you need; they cost 60 fork start-ups. Rows per class: Adversarial 124, Date 6, Numeric 123,
ParallelRadix 14, ParallelString 72, Permit 42, String 168, Tuple 18. **Every row is in the appendix at full precision; this
section carries the tables you asked to have replaced (strings, permits) and the readings.** All chinese / chinesenames /
Permit / Numeric / Date / Tuple rows are converged (t 0.89–1.08, bar ten Adversarial / ParallelRadix rows at 1.10–1.23,
listed in the appendix); 35 of the 81 english String-class rows are not (‡).

### Strings — `ParallelStringSortBenchmarks` at 2 s (72-row matrix in the appendix)

Speed-up over `serialRadixHuskySortAuto` saturates by p8 on every corpus (p8 → pAll within 6 %): serial ÷ p8 / pAll =
2.62× / 2.55× english@200k, 3.34×‡ / 3.52×‡ english@1M (against ‡ rows, so *understated* — the supplement's converged pAll
gives 3.48×, and p2's converged 128.152 gives 1.91×, not the 1.05×† the inflated 244.657 implies), 1.68× / 1.66× and 1.71× /
1.76× chinese 200k / 1M, 1.36× / 1.36× and 1.33× / 1.35× chinesenames; `11_p8` 2.34× / 3.26×‡ / 1.74× / 1.80× / 1.34× / 1.34×
on the same cells. At 32k every parallel row is 1 chunk and ties its serial (0.94–0.98×†) on english and chinesenames; on
chinese it costs 0.70–0.74× at 2 s against 0.86–0.93× at 1 s — "the parallel class costs 15–40 % at one chunk on
chinese@32k", not a figure. `systemSortParallel` at 2 s: 4.507 ± 0.050 / 10.019 ± 0.317 / 89.415 ± 3.106 english, 2.983 ±
0.010 / 5.988 ± 0.024 / 41.256 ± 1.760 chinese, 21.318 ± 0.128 / 47.725 ± 0.789 / 235.053 ± 4.123 chinesenames.

### Strings — `StringSortBenchmarks`, the columns the paper's tables quote (full 20-method rows in the appendix)

| corpus, n | `radixHuskySortAuto` | `radixHuskySort16` | `quickHuskySort` | `huskyEncodeOnly` | `msdStringSort` | `systemSort` | `systemSortParallel` | `systemSortPinyin` | `insertionSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| english, 32,000 | 4.000 ± 0.300‡ | 3.782 ± 0.263‡ | 10.488 ± 0.244 | 2.244 ± 0.072 | 4.264 ± 0.047 | 15.926 ± 0.362 | 5.119 ± 0.064 | — | 39.047 ± 0.356 |
| english, 200,000 | 54.843 ± 2.634‡ | 51.862 ± 2.686‡ | 106.482 ± 1.504 | 22.106 ± 1.329‡ | 48.010 ± 1.400 | 159.103 ± 2.937 | 10.525 ± 0.341 | — | 1160.132 ± 6.680 |
| english, 1,000,000 | 276.778 ± 6.926 | 269.177 ± 7.083 | 786.362 ± 11.175 | 136.906 ± 4.227 | 278.229 ± 9.375 | 1320.434 ± 24.126 | 99.413 ± 1.920 | — | 26756.221 ± 77.651 |
| chinese, 32,000 | 2.117 ± 0.067 | 1.919 ± 0.035 | 5.122 ± 0.039 | 0.412 ± 0.007 | — | 9.828 ± 0.035 | 2.999 ± 0.011 | — | 32.135 ± 0.060 |
| chinese, 200,000 | 11.431 ± 0.123 | 10.440 ± 0.105 | 31.114 ± 0.144 | 2.858 ± 0.048 | — | 71.459 ± 0.509 | 5.922 ± 0.029 | — | 1027.624 ± 2.827 |
| chinese, 1,000,000 | 59.577 ± 1.593 | 59.083 ± 1.532 | 221.714 ± 1.893 | 14.459 ± 0.157 | — | 443.670 ± 2.096 | 41.554 ± 1.538 | — | 25740.719 ± 53.833 |
| chinesenames, 32,000 | 24.914 ± 0.313 | 24.630 ± 0.245 | 28.467 ± 0.305 | 4.161 ± 0.065 | — | 11.883 ± 0.255 | 22.290 ± 0.141 | 73.517 ± 0.441 | 38.098 ± 0.269 |
| chinesenames, 200,000 | 152.537 ± 1.690 | 150.726 ± 0.276 | 188.872 ± 1.241 | 38.773 ± 1.003 | — | 113.753 ± 1.166 | 48.701 ± 0.428 | 567.210 ± 2.708 | 1141.647 ± 3.306 |
| chinesenames, 1,000,000 | 764.259 ± 12.352 | 762.765 ± 11.709 | 1294.983 ± 8.106 | 209.555 ± 2.321 | — | 902.895 ± 8.115 | 246.456 ± 5.762 | 3257.845 ± 26.207 | 26453.738 ± 34.795 |

**E11b-4 — serial husky vs the system sort, on the coders the paper now uses.** `systemSort` ÷ `radixHuskySortAuto`:
english **3.98× / 2.90× / 4.77×** at 32k / 200k / 1M (15.926 / 4.000; 159.103 / 54.843; 1320.434 / 276.778), chinese **4.64×
/ 6.25× / 7.45×**; against the fair pinyin baseline `systemSortPinyin` on chinesenames **2.95× / 3.72× / 4.26×** (3257.845 /
764.259 at 1M; code-point `systemSort` is the wrong order there, 902.895). The english 32k / 200k husky rows are ‡ (t 1.25 /
1.13; iterations 8–10 at 3.536 / 49.525), so those two ratios are *understated* — steady state would read ≈ 4.5× / 3.2×; the
other rows are converged. `systemSort` ÷ `quickHuskySort`: 1.52× / 1.49× / 1.68× english, 1.92× / 2.30× / 2.00× chinese, 2.58×
/ 3.00× / 2.52× chinesenames (pinyin). The automatic width lands within 3 % of the best fixed width at 1M on every corpus
(276.778 vs `radixHuskySort16` 269.177; 59.577 vs 59.083; 764.259 vs `radixHuskySort14` 760.852) and within 1–11 % at 32k /
200k; `msdStringSort` (english only) 278.229 ± 9.375 at 1M ties it. **Superseded**: every english and commonwords string
row of requests 4, 9 and 10 (coder changed), every `RadixHuskySort` / `ParallelRadixHuskySort` row of every earlier request
(internals changed), and request 9's chinesenames `systemSortParallel` (now pinyin order) — replace, do not splice.

### Permits — all 14 methods (ms/op)

| n | `systemSort` | `systemSortParallel` | `dualPivotQuicksort` | `quickHuskySort` | `quickHuskySortWithCleanup` | `radixHuskySort8` | `radixHuskySort11` | `radixHuskySort16` | `radixHuskySortAuto` | `16_p4` | `16_p8` | `Auto_p8` | `Auto_p8_chunk4k` | `Auto_pAll` |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 13.417 ± 0.078 | 4.379 ± 0.030 | 12.575 ± 0.075 | 6.437 ± 0.046 | 7.049 ± 0.066 | 2.857 ± 0.027 | 2.674 ± 0.028 | **2.630 ± 0.019** | 2.705 ± 0.030 | 3.183 ± 0.021 | 3.174 ± 0.032 | 2.836 ± 0.012 | 2.786 ± 0.029 | 2.830 ± 0.028 |
| 100,000 | 56.463 ± 0.994 | 5.420 ± 0.072 | 51.764 ± 1.173 | 27.977 ± 0.731 | 34.537 ± 1.014 | 13.171 ± 0.613 | 13.479 ± 0.652 | 12.386 ± 0.487 | 11.570 ± 0.565 | 6.934 ± 0.158 | 6.818 ± 0.119 | **4.525 ± 0.055** | 4.808 ± 0.131 | 4.789 ± 0.137 |
| 198,900 | 138.892 ± 2.317 | 12.623 ± 0.179 | 129.818 ± 2.307 | 70.710 ± 1.342 | 85.191 ± 1.901 | 34.981 ± 1.264 | 32.815 ± 0.807 | 32.125 ± 2.254 | 32.337 ± 2.131 | 14.883 ± 0.305 | 12.275 ± 0.230 | 9.782 ± 0.268 | 9.811 ± 0.296 | **8.380 ± 0.079** |

Two-method confirmation (`java -jar target/benchmarks.jar "PermitSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll)$"
-r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff permits-confirm.json`, `req11-extra-permits-confirm.json`, Mon 01:10 PT), beside
the full suite's rows and request 10's (`req10-permits-auto.json`, 1 s, jar `ef53f444…`, before the parallel encode):

| n | confirmation `systemSortParallel` | confirmation `Auto_pAll` | parallelSort ÷ pAll | fork-extreme | full suite parallelSort ÷ pAll | request 10 parallelSort ÷ pAll |
|---:|---:|---:|---:|---|---:|---:|
| 32,000 (1 chunk) | 4.928 ± 0.049 | 3.173 ± 0.071 | **1.55×** | 1.49–1.59× | 1.55× (4.379 / 2.830) | 1.60× (4.642 ± 0.026 / 2.905 ± 0.016) |
| 100,000 (6 chunks) | 6.360 ± 0.151 | 5.077 ± 0.102 | **1.25×** | 1.16–1.32× | 1.13× (5.420 / 4.789) | 0.46× (5.959 ± 0.192 / 13.086 ± 0.706) |
| 198,900 (12 chunks) | 14.075 ± 0.135 | 8.971 ± 0.200 | **1.57×** | 1.48–1.68× | 1.51× (12.623 / 8.380) | 0.50× (13.314 ± 0.124 / 26.508 ± 0.468) |

**E11b-5 — request 10's permits verdict is inverted on this jar, and the two-method run agrees with the 14-method one.**
pAll is 1.55× / 1.13–1.25× / 1.51–1.57× faster than `Arrays.parallelSort` with fork ranges disjoint at every n in both runs;
request 10 had it 2.20× / 1.99× *slower* at 100k / 198,900 on the same host and flags. Serial `radixHuskySort16` at
198,900 is 32.125 ± 2.254 against 30.5–32.2 in request 10's two runs, so the serial path did not move and the change is `15cc2ff`'s parallel
encode — what your Q2 answer predicted. Caveats: the full suite is a 14-method invocation with `systemSortParallel` last
(your two-methods rule was met only by the confirmation); both arms of the confirmation are 6–17 % slower than in the full
suite (4.928 vs 4.379, 14.075 vs 12.623; 3.173 vs 2.830, 8.971 vs 8.380) — the same between-invocation shift as the encode
arms — while the ratios move ≤ 0.12; pAll@100k is fork-bimodal in both (confirmation forks 5.39 / 4.94 / 5.23 / 4.90 / 4.93;
full suite 5.30 / 4.74 / 4.59 / 4.69 / 4.64), so 100k is the least certain cell. The 32k row is one chunk — serial — and
serial `radixHuskySort16` (2.630) still beats it; label it so. `systemSort` ÷ serial `radixHuskySortAuto` is 4.96× / 4.88× /
4.30×; the automatic width is best or tied among the serial radix rows at 100k / 198,900 (11.570 vs 12.386; 32.337 vs 32.125
± 2.254†) and 3 % behind fixed 16 at 32k. The three fork-bimodal Permit rows — `radixHuskySort16` @198,900 (forks 30.5 / 28.9
/ 37.8 / 36.9 / 26.4, spread 1.43×), `radixHuskySortAuto` @198,900 (35.7 / 33.4 / 26.6 / 37.6 / 28.4, 1.41×),
`radixHuskySort11` @100k (12.0 / 13.0 / 13.8 / 15.6 / 13.0, 1.30×) — were ±6–7 % in request 4 too, with no host spike in
their windows: intrinsic, and JMH's ± is anti-conservative there, as in request 10.

### `Long[]` — `ParallelRadixSortBenchmarks` (ms/op) and everything else

| n | `serialRadixHuskySort11` | `11_p1` | `11_p2` | `11_p4` | `11_p8` | `quickHuskySort` | `systemSortParallel` |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 2,000,000 | 157.006 ± 4.679 | 195.960 ± 4.944 | 149.612 ± 4.762 | 105.152 ± 1.833 | 98.344 ± 1.601 | 1103.052 ± 16.407 | 87.059 ± 4.528 |
| 10,000,000 | 957.730 ± 23.269 | 1107.859 ± 23.229 | 809.548 ± 24.574 | 629.169 ± 23.211 | 586.108 ± 26.024 | 5722.351 ± 43.141 | 765.466 ± 9.464 |

`11_p8` beats `Arrays.parallelSort` at 10M (765.466 / 586.108 = 1.31×) and trails it at 2M (87.059 / 98.344 = 0.89×);
`11_p1` is now 1.25× / 1.16× slower than its own serial (157.006 / 195.960 = 0.80×; 957.730 / 1107.859 = 0.86×) — the
parallel encode's fixed cost with nothing to share it (sanity check below). Elsewhere request 4's picture holds: Numeric
@500k `radixHuskySort16` vs `systemSort` integer 23.962 ± 0.311 vs 125.252 ± 1.366, long 26.922 ± 0.248 vs 140.149 ± 1.884,
double 30.104 ± 0.270 vs 142.815 ± 1.677 (new `integerSinglePivotQuicksort` 72.462 ± 0.477, between dual-pivot 85.196 ± 0.649
and radix); Tuple @500k 62.506 ± 0.726 vs 227.525 ± 2.953; Adversarial @1M `collapsedBitsDualPivotQuicksort` 323.140 ± 7.969
→ 845.393 ± 26.894 from fixedHighBits 0 → 60 while `collapsedBitsRadixHuskySort16` improves 67.287 ± 1.604 → 40.440 ± 1.505,
and the `sharedPrefix*` husky sorts lose their edge at prefixLength ≥ 10 (radix16 910.590 ± 6.737 vs `systemSort` 906.164 ±
16.458). Full rows: appendix.

## Sanity check against request 4 (e92610f, 2026-09-06) — not part of the tables

You asked for replacement, not splicing; this section only asks whether anything moved in a way that looks like a defect.
Baseline `rerun/full-suite.json` (freeze `e92610f`, same flags — 5 forks, 5 × 2 s, 10 × 2 s, JMH 1.37, Corretto 21.0.12 —
same host class, identical corpora). 415 rows common, 152 new, none only-old. Ratio new ÷ old over the 415: median 1.007,
371 within ±10 %; **42 rows moved ≥ 10 % with disjoint 99.9 % CIs (14 faster, 28 slower)** — all 42 in the appendix, grouped
and attributed here. Drift floor from `systemSort` rows, whose code no commit touched: String **0.99–1.18** (median 1.11;
english 1.16 / 1.13 / 1.18, chinesenames 1.06 / 1.11 / 1.14, chinese 0.99–1.00), Adversarial 0.98–1.08, Tuple 1.01–1.09,
Permit 0.92–1.01, Numeric and Date 0.98–1.03.

| group | rows (old → new, new ÷ old) | attribution |
|---|---|---|
| `String.huskyEncodeOnly english` | 32k 0.667 ± 0.063 → 2.244 ± 0.072 (3.36); 200k 8.819 ± 1.128 → 22.106 ± 1.329 (2.51); 1M 67.686 ± 0.824 → 136.906 ± 4.227 (2.02) | **changed measurement, not a regression**: the english corpus moved from the 4 × 16-bit `UNICODE_CODER` to `englishSaturatingCoder` (`StringSortBenchmarks.java:85`); chinese / chinesenames `huskyEncodeOnly` (coders unchanged) moved 0.91–1.04 |
| `ParallelRadix.parallelRadixHuskySort11_p1` / `_p2` slower, `_p4` / `_p8` faster | p1 2M 165.224 ± 2.169 → 195.960 ± 4.944 (1.19), 10M 884.462 ± 21.556 → 1107.859 ± 23.229 (**1.25**); p2 10M 731.573 ± 19.886 → 809.548 ± 24.574 (1.11); p4 2M 118.475 ± 2.418 → 105.152 ± 1.833 (0.89); p8 2M 109.316 ± 1.802 → 98.344 ± 1.601 (0.90) | **code, real**: `15cc2ff`'s parallel encode adds fixed cost at p = 1–2 and pays at p ≥ 4 (that class's `quickHuskySort` drifted 1.08 / 0.98) — a trade-off at the freeze worth a sentence in the paper |
| `Adversarial.collapsedBitsRadixHuskySort16` faster, `…8` slower | radix16 @1M 0.88–0.93 at all seven fixedHighBits, disjoint (fhb 0: 76.239 ± 1.573 → 67.287 ± 1.604; fhb 56: 58.421 ± 1.664 → 52.122 ± 1.656); radix8 @1M 1.10–1.13 at fhb 0 / 16 / 60 / 63 (71.612 ± 2.587 → 78.844 ± 2.333 at 0) | **code, small and systematic**: consistent with a per-pass cost shift between digit widths (`a6f8eaf`, `350c99b`); `collapsedBitsSystemSort` on the same cells is flat (1.00–1.06) |
| `String.systemSort` english / chinesenames +11–18 %, `quickHuskySort` english 1.12 / 1.13, `msdStringSort` english@200k 1.14, `multikeyQuicksort` chinesenames@32k 1.12 | e.g. `systemSort` english@1M 1120.873 ± 14.433 → 1320.434 ± 24.126 (1.18) | **host drift on the comparison-heavy String rows** (`systemSort` = `Arrays.copyOf` + `Arrays.sort`, untouched; `StringState` byte-identical); so no String-class ratio inside 0.85–1.18 is read as code (the `systemSort` floor itself is 0.99–1.18) |
| String chinese radix family @1M 0.84–0.91 (all seven widths, disjoint), english `radixHuskySort8` @1M 0.89, `radixHuskySort14` @32k 0.84; english `radixHuskySort12` @1M 1.24 (307.375 ± 5.223 → 382.103 ± 15.561) | — | at the String floor's edge / single wide-CI row (its neighbours 10 / 11 / 13 / 14 / 16 are 0.90–1.02); not called either way |
| Permit `radixHuskySort8` / `16` / `11` @100k 0.85 / 0.88 / 0.89; Tuple @500k all six methods 1.05–1.16 incl. `systemSort` 1.09 | Permit `systemSort` 0.92–1.01 in the same run | at the class floors; drift |

Verdict: the radix-sorter rework (`0f22d44`, `350c99b`) produced no regression the drift floor cannot absorb; the two
code-level movements are the low-p `ParallelRadixHuskySort` cost and the width-dependent per-pass shift, both named above.

## Warm-up convergence — the one methodological problem in this run

t = mean over the five forks of mean(iterations 1–5) ÷ mean(iterations 6–10) from `rawData`; 1.00 = flat, > 1.10 = still
speeding up inside the window, score above steady state (‡). `-wi 5` at 1 s / 2 s is 5 s / 10 s of warm-up against JMH's
default 50 s. Measured consequence: **16 of 72 step-4 rows and 45 of 567 full-suite rows have t > 1.10, and every
String-class case is english** (35 of 81 english rows vs 0 of 159 chinese / chinesenames); the other ten are Adversarial
radix rows at 200k / 1M and `serialRadixHuskySort11` @2M, t 1.10–1.23. Handling: english@1M pAll / p8 / p4 / `11_p8` (step 4
t 1.67 / 1.53 / 1.33 / 1.44; full suite 1.12 / 1.15 / 1.22 / 1.18) and `Auto_p2` english@1M (full suite 1.21) → the 50 s
supplement is quoted and the inflated means marked; english@32k radix rows (1.16–1.25, both classes) and english@200k
(1.12–1.18) → husky-vs-`systemSort` ratios understated by 10–25 %, said so; `huskyEncodeOnlyEnglishMasking` @200k (1.29)
→ 2.24× is a lower bound. Everything else in the full suite — all chinese / chinesenames, Permit, Numeric, Date, Tuple —
and the 11a rows are at t 0.89–1.08 and stand. Full lists: appendix. Rule: quote the longest-warm-up converged
measurement; never average a ‡ row into a headline.

## Host conditions per invocation

`load-monitor.log`: one `top -b -n 1` + swap sample per minute; spikes = a non-benchmark process above 20 % of one core
(50 % for step 5, whose 1,642 samples would otherwise list the `falcon` EDR agent's routine 6–25 %).

| invocation | samples | load1 min / median / max | swap MiB | benchmark java %CPU med / max | non-java spikes, and where they fell |
|---|---:|---|---|---|---|
| step 1 cleanup | 51 | 0.36 / 1.28 / 1.97 | 2 → 2 | 100 / 331 | `yum` 100 %, `systemd` 38 % + `falcon` 25 %, `unison` 94 %, `unison` 24 % — all inside `adaptiveInsertionCleanup pinyin@1M` forks 2–3 (fork means 23894 / 24553 / 23736 / 24693 / 24250, spread 1.04×); no mark |
| step 2 cleanup-binary | 7 | 0.91 / 1.59 / 2.08 | 2 → 2 | 100 / 265 | `bun` 25 % (last sample) |
| step 3 encode | 26 | 0.45 / 1.25 / 2.84 | 2 → 2 | 100 / 106 | none |
| step 4 strings-parallel | 109 | 0.30 / 1.82 / 12.18 | 2 → 2 | 125 / 1294 | `find` 29 % (`Auto_p8 english@200k` fork 3 — that row's fastest fork), `cwagent` 24 % and `falcon` 44 % (`Auto_p8 english@1M` forks 1 and 4); no mark |
| step 5 full-suite | 1642 | 1.00 / 1.25 / 15.16 | 2 → 2 | 100 / 1494 | 46 samples in 44 distinct minutes: `falcon` 12, `unison` 10, `apolloH` 7, `snape` 5, `yum` 5, singles incl. `ssm-doc` 475 % (Sat 21:04, inside `collapsedBitsSystemSort fixedHighBits=0 n=200k` fork 2, spread 1.04×); none inside a row with fork spread > 1.17×; none in the three bimodal Permit rows' windows |
| supplement 1 english-1m | 26 | 0.28 / 3.03 / 14.57 | 2 → 2 | 234 / 1394 | `unison` 62 % once (serial fork 1, whose iterations match forks 2–5) |
| supplement 2 cleanup small-n | 11 | 1.15 / 1.54 / 1.83 | 2 → 2 | 100 / 169 | none |
| supplement 3 permits-confirm | 15 | 0.81 / 3.23 / 11.13 | 2 → 2 | 319 / 1162 | `unison` 100 % once, in the 120 s idle gap before the step |

The load1 maxima are the run's own `systemSortParallel` and pAll forks (java > 1000 %); every single-threaded window sits at
load1 ≤ 2.84. Swap never moved. `unison` is my file sync reacting to the results directory; it never hit an outlying fork.

## Methodology and disclosures

1. **Code and gates**: HEAD `82bcfa8`, src freeze `15cc2ff` (`git log 15cc2ff..HEAD -- src/` empty), jar sha256
   `95ccd2c7…c1d408`, 76,350,880 B, built 14:51 PT Fri 2026-09-18; `mvn -B test` 423 / 0; harness 508 / 508 on the same jar;
   first benchmark 23 h 48 min after the build (an earlier attempt from the same jar discarded — host contention).
2. **JMH**: `-f 5 -wi 5 -i 10 -rf json`; 1 s iterations (class default) for steps 1–4 and supplement 2, `-r 2s -w 2s` for
   step 5 and supplement 3, `-w 10s -r 2s` for supplement 1; `VM options: <none>`; JMH 1.37; Corretto 21.0.12+8; Cnt 50 = 5 ×
   10 every row; ± = 99.9 % CI half-width; one JSON per invocation, unedited.
3. **Step 5 deviated from "no filter" by `-e CleanupPassBenchmarks`** (its `binaryInsertionCleanup` throws on pinyin; steps
   1–2 already measured the class); 567 rows = 579 − 12 guard-throw rows (`StringSortBenchmarks.java:193`, `:255`); 2,895
   `# Fork:` lines = 567 × 5 + 60; wall 27 h 27 min.
4. **Environment** as stated above: no CPU-frequency information exposed to the guest; swap 2 MiB flat and probe `15 16` at
   both ends of both units; `uptime` before / after all eight steps; 120 s idle gaps; 46 spike samples in step 5, none inside
   a fork with spread > 1.17×; three fork-bimodal Permit rows with their fork means.
5. **Warm-up convergence**: ‡ rows and their iterations-8–10 level beside every affected headline cell; `Auto_p2` english@1M
   marked unconverged in both files it appears in.
6. **Step 4 vs full suite**: 40 / 72 rows disjoint, three causes named, and the rule for which value a headline quotes.
7. **Encode**: the 12 Chinese-corpus rows labelled controls; the factor a range across both files; per-arm shift 0.86–1.28×
   stated; `huskyEncodeOnly` 136.906 ± 4.227 as the third estimate; cleanup side of the saturation trade unmeasured.
8. **Cleanup**: pn/4 column = your own figures (unicode@200k computed from the Javadoc p, marked); unicode@200k added as a
   predicted-adaptive cell that tied; only two requested points inside the window, both at its edges; timed region includes
   `Arrays.copyOf`; fork means for the two † cells.
9. **Cross-run section** labelled a sanity check against e92610f: per-class drift floors, the coder swap behind
   `huskyEncodeOnly`, the p1 / p2 vs p4 / p8 movement.
10. **32k rows**: every `Auto_*` / `11_p8` row is 1 chunk; chunks / bits / passes printed per row; ±25 % caveat on those cells.
11. **Permits inversion** vs request 10 with the 14-method-invocation caveat; supplement 1 (the english@1M headline) is a
    four-method invocation with `systemSortParallel` last, so the two-methods rule was met by steps 1 and 3 and supplements
    2 and 3 only; request-9 / request-10 string and permits tables, and every earlier `RadixHuskySort` row, marked superseded.
12. **Files**: the eight JSONs unedited (step 3's 12 control rows and the full suite's `ParallelStringSortBenchmarks`
    duplicates included) plus `env-before.txt`; logs, `uptime-step*.txt`, `forkjoin-probe*.txt`, `harness.log`,
    `mvn-test.log`, `load-monitor.log` are outside the repo, available on request (the full-suite log is 2.5 MB).

**What is not claimed.** Not "adaptive insertion never wins" (it wins at unicode n ≤ 100k, pn ≤ 11.5); not a saturating
cost of "2.85×" or "1.92×" as a point, nor a net verdict on the coder; not "pAll trails parallelSort 1.24×" (step 4) and not
"1.23× faster" without the convergence caveat, never an average of invocations; not "the `Auto_p2` anomaly did not
reproduce" (the slow warm-up reproduced); no fixed 1-chunk overhead and no husky-vs-parallelSort verdict at n = 32,000
finer than ±25 %; not "memory-bound" (unmeasured; one-shot `top` put the english@1M pAll JVM at 119–275 % against
1100–1244 % for parallelSort — three samples, an observation); not "step 4 and the full suite agree"; no String-class
cross-run change below 1.2× read as code; not "the full suite is request 4's invocation" (`-e CleanupPassBenchmarks`; 27 h).

## Files

All under `doc/`, unedited JMH `-rff` output from the jar above (runner scripts live outside the repo; the exact commands are
quoted in each section and collected below).

| file | sha256 | invocation |
|---|---|---|
| `req11-cleanup.json` | `6d0367c72f055500e40dbb1c3f3ff8faeacd6d48bae01136e6f9c3d05715778b` | step 1 (Sat 14:40 PT), 12 rows |
| `req11-cleanup-binary.json` | `7c07a2cdc24f51e883ab3afdd5040e0d1167d721ef84c3ac5f7bd7457688b0f2` | step 2, 4 rows |
| `req11-encode-masking-vs-saturating.json` | `a22bafd6f4707d561803d4ffc95911bef56719497841971b98beb75bc2ecf938` | step 3, 18 rows (12 are controls) |
| `req11-strings-parallel-full.json` | `6b714d404337e549c176560ae04dea1c18d4d707c731004ed7b38e3d6eadaceb` | step 4, 72 rows, 1 s iterations |
| `req11-full-suite.json` | `7aeedd8700bbe3dc395ab2ce7a18457f260531d8b8c3675681ee6abb051648a2` | step 5 (Sat 18:01 → Sun 21:28 PT), 567 rows, 2 s |
| `req11-extra-english-1m-longwarmup.json` | `29796dc02c29644c2cdb17b92a115f03c80642b8b48d111bbaf72cf4ee3580ad` | supplement 1 (Mon 00:30 PT), 4 rows, `-w 10s -r 2s` |
| `req11-extra-cleanup-unicode-smalln.json` | `f1a6f19d5360afee99078c7851469c4c52543a5c70b7df34ef6f32e6e0e1ea33` | supplement 2, 6 rows |
| `req11-extra-permits-confirm.json` | `03a70357322a48fc5fda714e386e1ac6792f7502650a166a3ab6c02e45c06dea` | supplement 3, 6 rows, `-r 2s -w 2s` |
| `req11-env-before.txt` | `da6d39485d534e10b4a2a269f2ca660a0e812ee280a91e6ee4f1da7ba0055804` | `date`, `uptime`, `lscpu`, `free -h`, `uname -r`, `java -version`, `mvn -v`, cgroup, `nproc`, git HEAD, src freeze, jar sha at launch |

Plus this file and `Run results from Yunlu 2026-09-21 - appendix.md` (every full-suite row at full precision, the t > 1.10
lists, the step-4 per-row speed-ups, the 42-row cross-run table). Validation, all exit 0: `validate-json.py --preset req11`
on the eight files → VALID (97 + 57 checks PASS, 0 FAIL); `jmh-json-vs-log.py --results-dir req11 --preset req11` and
`--results-dir req11-extra --preset req11-extra` → ALL MATCH (679 / 679 rows to 3 decimals; `# Fork:` 60 / 20 / 90 / 360 /
2,895 and 20 / 30 / 30). The jar (76,350,880 B, sha256 `95ccd2c7…c1d408`) is not committed.

## Reproduction — the eight invocations, from the package root with the pinned JDK, each its own `java -jar`

```
java -jar target/benchmarks.jar "CleanupPassBenchmarks.(timsortCleanup|adaptiveInsertionCleanup)$" -f 5 -wi 5 -i 10 -rf json -rff cleanup.json
java -jar target/benchmarks.jar "CleanupPassBenchmarks.binaryInsertionCleanup$" -p coder=englishSaturating,unicode -f 5 -wi 5 -i 10 -rf json -rff cleanup-binary.json
java -jar target/benchmarks.jar "StringSortBenchmarks.huskyEncodeOnlyEnglish" -f 5 -wi 5 -i 10 -rf json -rff encode-masking-vs-saturating.json
java -jar target/benchmarks.jar "ParallelStringSortBenchmarks" -f 5 -wi 5 -i 10 -rf json -rff strings-parallel-full.json
java -jar target/benchmarks.jar -e "CleanupPassBenchmarks" -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-suite.json
java -jar target/benchmarks.jar "edu.neu.coe.huskySort.sort.huskySort.ParallelStringSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll|parallelRadixHuskySortAuto_p2|serialRadixHuskySortAuto)$" -p corpus=english -p n=1000000 -w 10s -r 2s -f 5 -wi 5 -i 10 -rf json -rff english-1m-longwarmup.json
java -jar target/benchmarks.jar "CleanupPassBenchmarks.(timsortCleanup|adaptiveInsertionCleanup)$" -p coder=unicode -p n=20000,50000,100000 -f 5 -wi 5 -i 10 -rf json -rff cleanup-unicode-smalln.json
java -jar target/benchmarks.jar "PermitSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll)$" -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff permits-confirm.json
```

Run each as a detached unit outside any CPU-quota'd cgroup, `uptime` before and after, having checked that `jshell -s -`
prints `ForkJoinPool.commonPool().getParallelism() + " " + availableProcessors()` = `15 16` in that unit (a quota'd slice
reports `13 14` and corrupts every parallel row). For the english@1M rows, `-w 10s` is what separates a converged mean
from an inflated one.
