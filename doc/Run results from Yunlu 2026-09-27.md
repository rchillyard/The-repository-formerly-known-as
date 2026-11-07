# Run results from Yunlu — requests 11c and 11d, then the full suite again, 2026-09-27

**11c: the masking coders' 855–1,637× more inversions cost `timsortCleanup` only 1.06–1.07×, inside your band, so `k(N +
pX)` is not Timsort's cost.**

**11d: the rank coder makes the parallel cleanup irrelevant on chinesenames, and the parallel cleanup won every step-A
cell, including chineseUnicode@200k.**

**Full suite: the corpus fix moved the english and chinese rows; on unchanged input the `Arrays.sort` /
`Arrays.parallelSort` baselines stayed within 0.93–1.06 of request 11, except in the permits class.**

One jar, built at `d947e77`, served 11c, 11d and the full suite, which we re-ran because `9bb0385` changed the english
and chinese corpora. Our logs number the invocations: step 1 is 11c, steps 2–4 are 11d's steps A, B and C, and step 5
was a first attempt at the full suite that a patch reboot killed. Every full-suite row is in `Run results from Yunlu
2026-09-27 - appendix.md`; the raw JSONs are committed alongside (Files).

**Marks.** † = the two 99.9 % CIs overlap. ‡ = some fork was still speeding up inside its measurement window (t = mean
of iterations 1–5 ÷ mean of 6–10 > 1.10), so the score is above steady state and verdicts use the "steady" level, the
mean of iterations 6–10 over all five forks. § = every fork slowed down (t < 0.90 in all five), so the score is below
the iterations 6–10 level; a ratio involving a § row is also given on iterations 6–10 of both rows, and so is
`parallelSort` ÷ `pAll_pinyinRank` at chinesenames@1M, whose `pAll_pinyinRank` row slows in every fork without being §
(t 0.87–0.95 in step C, 0.82–0.94 in the full suite). Fork-extreme = a ratio's range over fork means, from
min(numerator) ÷ max(denominator) to max ÷ min. Ratios come from unrounded JSON scores. Times are PT (UTC−7), Sat
2026-09-26 unless stated.

**Checkout and build**: `5eadb46`, the tip of `parallel-redesign` at our last fetch; the last commit touching `src/` is
**`d947e77`** ("Add an exactly order-preserving pinyin coder: rank, do not decompose"; `git log d947e77..5eadb46 --
src/` is empty). From `9bb0385`, which 11c names, `src/` changes in 8 files (490 insertions, 1 deletion):
`CleanupPassBenchmarks` gains two coder values, two `setup()` cases and `parallelTimsortCleanup`, while
`RadixHuskySort`, 11c's five coders and the `timsortCleanup` / `adaptiveInsertionCleanup` bodies are untouched, so 11c's
20 cells run the code they ran at `9bb0385`. Corretto **21.0.12+8-LTS**, Maven **3.9.16**, JMH **1.37**; `mvn -B test`
**432 tests, 0 failures, 0 errors, 0 skipped**, including your test that sorts all 1,145,009 names by code alone.
`target/benchmarks.jar` **76,379,654 B**, sha256 `7d5e0bffe227d4414e44b96f487b4daa1c4977be0ad3b36c70c0ceb026138295`,
built 01:03:12 from a clean worktree, 17 min before the first benchmark, and byte-identical at every re-hash (before
every step and every full-suite class). Every invocation used `-f 5 -wi 5 -i 10 -rf json` with your flags: 1 s
iterations (the class default) for 11c and step A, as your commands have it, and `-r 2s -w 2s` for steps B and C and the
full suite. `VM options: <none>`; Cnt = 50 per row; ± is the 99.9 % CI half-width; one JSON per invocation, unedited.

**Environment**: aarch64 Graviton (ARM Model 1, r1p1), **16 CPUs, 1 thread per core**, L1d / L1i 1 MiB, L2 16 MiB, L3 32
MiB, 30 Gi RAM (19 Gi available), swap 39 Gi (390 Mi used), kernel `6.12.103-127.188.amzn2023.aarch64`; the full suite
ran after a patch reboot, on `6.12.103-129.197.amzn2023.aarch64`, with 0 B of swap used. Two detached units in
`app.slice`, never under the CPU-quota'd `kiro.slice`: `husky-req11cd-20260926-0818.service` (steps 1–5) and
`husky-req11cd-full-20260926-2149.service`. **The `ForkJoinPool` probe printed `15 16`**, 15 common-pool workers for
`Arrays.parallelSort` and `parallelTimsortCleanup` and `availableProcessors()` = 16 for pAll, again after the full
suite's last class (the first unit died before its closing probe). An idle guard (load1 < 2, swap < 1 GiB, no other
benchmark JVM, fewer than 3 active `MainThread` processes) and a 120 s gap preceded steps 1 and 5 and every full-suite
class, with `uptime` before and after each (UTC in brackets):

| step | invocation | before | load average before (1 / 5 / 15) | after | load average after | wall (JMH) |
|---|---|---|---|---|---|---:|
| step 1 | cleanup-coders (11c) | 01:20:39 (08:20:39Z) | 0.11, 1.38, 1.60 | 01:59:43 | 1.98, 3.22, 2.75 | 39 min |
| step 2 | cleanup-parallel (11d A) | 02:01:43 | 0.65, 2.29, 2.46 | 02:53:42 | 2.14, 2.00, 3.03 | 52 min |
| step 3 | pinyin-rank-serial (11d B) | 02:55:42 | 0.95, 1.56, 2.74 | 03:21:26 | 1.94, 1.72, 1.81 | 26 min |
| step 4 | strings-cleanup (11d C) | 03:23:26 | 0.48, 1.23, 1.62 | 04:19:18 (11:19:18Z) | **15.51**, 14.16, 11.04 | 56 min |
| step 5, attempt 1 | full-suite, one command | 04:22:18 (11:22:18Z) | 1.19, 7.90, 9.14 | died 13:30:45 in a patch reboot | — | no data |
| full suite | Date | 14:51:56 (21:51:56Z) | 0.23, 0.18, 0.15 | 15:07:08 | 1.53, 1.43, 0.98 | 15 min |
| full suite | ParallelRadix | 15:09:09 | 1.02, 1.19, 0.94 | 15:53:43 | 14.60, 10.56, 5.54 | 45 min |
| full suite | Tuple | 15:56:44 | 0.83, 5.82, 4.58 | 16:42:51 | 1.19, 1.20, 1.36 | 46 min |
| full suite | Permit | 16:44:51 | 0.44, 0.90, 1.23 | 18:32:26 | 12.65, 8.24, 4.35 | 1 h 48 min |
| full suite | ParallelString | 18:35:27 | 0.98, 4.66, 3.64 | 22:28:44 | 15.82, 12.39, 9.03 | 3 h 53 min |
| full suite | Numeric | 22:31:45 | 0.75, 6.71, 7.41 | Sun 03:45:14 | 1.31, 1.24, 1.19 | 5 h 13 min |
| full suite | Adversarial | Sun 03:47:15 | 0.44, 0.90, 1.07 | 09:41:06 | 1.01, 1.12, 1.22 | 5 h 54 min |
| full suite | String | Sun 09:43:06 | 0.13, 0.74, 1.06 | 19:18:52 (Mon 02:18:52Z) | 1.14, 2.37, 4.76 | 9 h 36 min |

The high after-loads are each invocation's own last forks (JMH runs `systemSortParallel` last, on all 16 cores); the
guard waited one 60 s poll for that tail before step 5 (load1 2.19, then 1.19) and before Tuple, ParallelString and
Numeric (2.06, 2.02, 2.23, then 0.83, 0.98, 0.82).

## Summary

- **11c, the cleanup cost model: the data refute `N + pX` for Timsort, and `N log r` fits in kind only.** At n =
  1,000,000 the masking coders' 1,637× (english pair) and 855× (ascii pair) more inversions, with 1.077× / 1.041× the
  runs, cost `timsortCleanup` 1.06× (63.262 ± 1.764 vs 59.665 ± 1.525) and 1.07× (67.025 ± 2.992 vs 62.776 ± 1.085;
  1.04× by medians, as one fork ran under a build), inside your 0.94–1.25×. `N + X` predicts 163.4× / 153.0×. `N·log r`
  predicts 1.008× / 1.004×, and the measured excess is 7.8× / 17.5× its prediction, unexplained.
  `adaptiveInsertionCleanup` pays 38.39× / 37.16× and fits 62.5 ns per element plus 13.268 ns per inversion, so one `k`
  in `k(N + pX)` does not describe it either. You wrote that if Timsort follows runs and not inversions, the paper body
  and the `p_crit` framing have to change; the data rule out inversions, and fit the table's `O(N + N log r)` in kind
  but not in size.
- **11c, whether to keep the saturating coder: on the serial components masking comes out 73.8 ms ahead per million
  words (81.7 at 200,000), inside your "more like 70–90 ms on yours".** Saturation saves 3.597 ms per million in the
  Timsort cleanup (11c) and costs 77.398 more to encode (175.475 ± 2.388 against 98.077 ± 1.204, 1.79×, full suite); the
  two figures come from different invocations. Not established: the cost inside the parallel sorter, which encodes in
  parallel, and a whole-sort figure, since no benchmark sorts english end to end with the masking coder. On monotonicity
  each side has a case: saturation mis-orders strings ("NÂº" < "NÃ"), while masking costs the adaptive cleanup 38.39× as
  much and, where the cleanup is skipped, leaves 611 / 609 code descents on short words where saturation leaves 8
  (finding 3).
- **11d, which answer makes the other irrelevant: the rank coder, on chinesenames.** `pAll_pinyinRank` (8.251 ± 0.061 /
  35.326 ± 1.598 ms at 200,000 / 1,000,000) is 3.09× / 3.03× faster than `pAll_parCleanup` (25.519 ± 0.273 /
  107.063 ± 6.892 §; 3.14× on iterations 6–10) and 5.72× / 6.79× faster than `Arrays.parallelSort` (6.45×), inside your
  5–9×. Serial, the rank coder is 4.18× / 4.43× faster than the ordinal coder and 15.21× / 18.51× faster than
  `systemSortPinyin`, inside your 4–6× and 15–27×. On chinesenames the parallel cleanup gains 4.35× / 5.22× over pAll
  (4.77×) against your 2.4–2.9× and beats `Arrays.parallelSort` 1.85× / 2.24× (2.05×) where you expected parity. On
  english and chinese it is the only fix: parallelSort ÷ pAll_parCleanup = 1.20× / 1.94× (english, 1M steady) and 1.12×
  / 1.47× (chinese).
- **11d, the rule: at n ≥ 200,000, parallelize the cleanup whatever the coder, on this host.** All 16 step-A cells win,
  from 1.63× (chineseUnicode@1M, steady) to 9.06× (unicode@200k), and chineseUnicode@200k wins 3.56× where you expected
  0.5×, so your reason for not stating the rule in n did not reproduce. The one loss we measured is end to end at n =
  32,000 on english, where the radix phase is one chunk: pAll ÷ pAll_parCleanup = 0.90×† (steady 0.88×). The gain tracks
  the serial cost per element within n = 1,000,000 (Spearman ρ = 0.90) but hardly within 200,000 (ρ = 0.21; 0.73 over
  all 16), and not the descents (ρ = 0.34). We measured no break-even point.
- **Where this host and your Mac part.** Our cost per extra inversion, 13.268 ns against your 4.393, doubles 11c's
  adaptive ratio. With 15 pool workers against your 7, worker count alone lets our step-A gain be at most 15/7 = 2.14×
  yours. Three of the six Javadoc cells stay within that (gain ratio = our speed-up ÷ yours: englishSaturating@1M 1.48×
  and chineseUnicode@1M 1.09× on the steady level, pinyin@200k 1.89×); pinyin@1M (3.42×) and the two 200,000 cells
  (englishSaturating 6.63×, 7.49× steady; chineseUnicode 6.69×) exceed it, unexplained (E11d-A2).
- **Full suite: the corpus fix moved the english and chinese rows; on unchanged input the sorting rows held except the
  permits class and four Adversarial rows.** 582 rows in eight per-class invocations, 24 absent by design; english,
  chinese and `sharedPrefix*` rows sort new arrays and supersede request 11's. On unchanged input new ÷ request 11 is
  0.93–1.07 for the chinesenames sorting rows, Numeric, Date, Tuple and `Long[]`. Four Adversarial rows at 1,000,000
  moved ≥ 10 % with disjoint CIs (`collapsedBitsDualPivotQuicksort` at fixedHighBits 0 / 32 / 48, 1.11× / 1.10× / 1.15×;
  `collapsedBitsQuickHuskySort` at 0, 1.13×; their `Arrays.sort` baseline 0.97–1.04), and the chinesenames encode-only
  rows moved up to 1.54×. The permits class shifted as a whole while `parallelSort` ÷ pAll held (1.58× / 1.20× / 1.61×
  against 1.55× / 1.13× / 1.51×). On the new corpora the serial radix husky sort beats `Arrays.sort` 2.52×–4.62× on
  english and 4.58×–7.50× on chinese; english@1M is still unconverged (pAll 1.24× faster than `Arrays.parallelSort` on
  the steady level, 0.90×† by score). The chinesenames rows reproduce step C within 0.961–1.019.
- **Three findings, none affecting any timing** (Correctness): the saturating coders' Javadoc overstates monotonicity,
  `binaryInsertionCleanup` accepts `pinyinRank`, and the `perfect` flag trusts word length alone, so an all-short array
  holding an out-of-window character comes out unsorted (a probe; no benchmark array is all-short).

## Correctness was checked before any timing was trusted

Before the unit launched, the external `ValidateSorts` harness ran against this jar (01:12:44–01:15:58, quota-free
`app.slice/kiroom.service`): **746 / 746 checks passed** (`req11cd/harness.log`) after we rewrote its check H4, which
fails on this jar as request 11 wrote it (the old harness scored 507 / 508 here, H4 the only failure; below). That is
request 11's sections A–H (508 checks) plus a new **section I** of 238, which calls the JMH `@State` `setup()` and
`@Benchmark` methods directly, so it checks the code JMH times:

- **I1**, `CleanupState.setup()` for every 11c and step-A cell: the hand-over is a permutation of the harness's sample
  and stays untouched, the ordering is right (`NAME_ORDER` for the two pinyin coders), and every cleanup's output equals
  `Arrays.sort(input, ordering)`. Descents are exactly your run counts minus one: 16,060 / 17,304 / 29,326 / 30,519 /
  366,864 for the five english coders at 1,000,000 against your 16,061 / 17,305 / 29,327 / 30,520 / 366,865, and all six
  cells of the `ParallelRadixHuskySort` Javadoc table (1,999 / 16,060 english, 3,106 / 16,789 chinese, 30,461 / 162,689
  chinesenames). The `pinyinRank` coder leaves 0 at both n.
- **I2**, the masking and saturating twins agree on every character inside their window (64..127 english, 0..127 ascii)
  and on every vocabulary word whose encoded prefix stays inside it (303,122 english and 303,195 ascii of 304,905); the
  1,783 and 1,710 others get different codes. The `9bb0385` splitter no longer truncates at a digit or at U+3002, and
  every token is letters only.
- **I3**, `chineseEncoderPinyinRank` over all 1,145,009 names: 1,145,009 distinct codes (ordinal coder: 818,114),
  `perfect` true, 0 `NAME_ORDER` descents after sorting by code alone, positionwise equal to `Arrays.sort(NAME_ORDER)`,
  sign agreement on 2,000,000 random pairs. The post-sorter then runs 0 times in `RadixHuskySort` and
  `ParallelRadixHuskySort` (p = 16) at 200,000 and 1,000,000 (once with the ordinal coder). The rank methods throw on
  english and chinese, hence step C's 4 absent rows.
- **I4**, every step-B and step-C row through `StringState.setup()`: the master is the harness's own sample, the coder
  is the expected one (`englishSaturatingCoder`, `UNICODE_CODER`, `chineseEncoderPinyin`), and each output is sorted, a
  permutation and equal to `Arrays.sort`.

**H4 was rewritten, and why.** Request 11's H4 asserted that `englishSaturatingCoder` codes are non-decreasing along a
sorted 20,000-word english sample. On this jar the sample has one code descent, **"NÂº" < "NÃ"**: at index 1 both U+00C2
and U+00C3 are above the window and clamp to 63, so index 2 decides, where º (U+00BA) also clamps to 63 and "NÃ" has the
padding zero. It is the mechanism your 11d note gives for 41.4 % of the pinyin descents, a tie at *i* falling through to
padding at *i* + 1. The new H4 asserts what the design guarantees, that every code descent is such an out-of-window tie
at the first differing character; the per-character checks over all 65,536 characters pass unchanged. Request 11's
pre-`9bb0385` sample had no such pair (508 / 508 then).

**Three findings for you.**

1. **The saturating coders' Javadoc overstates monotonicity.** `stringToLongSaturating` promises "a long, monotonically
   non-decreasing with respect to the natural ordering of str" (`HuskyCoderFactory.java:408`), and
   `englishSaturatingCoder` is "monotonic over the whole character range … it never mis-orders -- it only ties"
   (`:142–154`). The per-character map is monotonic; the string map is not (the H4 witness), and the shared helper
   carries the claim to `asciiSaturatingCoder`. Every row we ran sorted correctly because the cleanup ran on every
   hand-over (H5, I1, I4); where it is skipped the mis-order reaches the output (finding 3).
2. **`binaryInsertionCleanup` accepts `pinyinRank`.** Its guard is `state.coder.equals("pinyin")`
   (`CleanupPassBenchmarks.java:285`), so it refuses `pinyin` (I1 checks that) but accepts `pinyinRank`, added in
   `d947e77`, and returns code-point order instead of `NAME_ORDER` (n = 2,000, not a requested row). No row here is
   affected; a sweep that included it would time a sort into the wrong order. Testing the state's ordering instead of
   the coder's name would close it.
3. **The `perfect` flag trusts word length alone, so a short input can come out unsorted.** `perfectForLength` is
   `length <= maxLength` (`BaseHuskySequenceCoder.java:16–18`) and `huskyEncode(X[])` combines only that (`:50`). A
   saturating or masking coder therefore reports `perfect` for any array of words of ≤ 10 characters (english) or ≤ 9
   (ascii), even with characters outside its window, and `postSort` skips the cleanup (`AbstractHuskySort.java:62–63`).
   A probe against this jar (`ProbePerfectFlag.java` and its log `req11cd/probe-perfect-flag.log`, outside the repo,
   13:50 PT) gets `["NÃ", "NÂº"]` and the saturated tie `["Né", "Nè"]` back unsorted, with `perfect=true`, from
   `RadixHuskySort` AUTO and `ParallelRadixHuskySort` (p = 16, with and without the parallel cleanup) under both
   saturating coders; one added 11-character word makes `perfect` false and every sort correct. The masking coders
   (`englishMasking` and `asciiMasking`, the factory's `englishCoder` and `asciiCoder`) fail on `["café", "cafz"]`. On
   200,000 words of at most 9 characters drawn with replacement they leave 611 and 609 code descents (adjacent pairs of
   the sorted array whose codes run backwards), against 8 descents and 4 ties for either saturating coder; a mis-coded
   word lands far from its place, so that is 193,103 and 199,321 output positions wrong against 57. No benchmark row is
   affected: every `StringState` english array has words longer than 10 characters (4,471 / 27,534 / 137,843 at 32,000 /
   200,000 / 1,000,000), as does `SharedPrefixState` (probed at 200,000, prefixLength 0), `CleanupPassBenchmarks` sorts
   the hand-over unconditionally (`CleanupPassBenchmarks.java:230`, `:259`), and `UNICODE_CODER`'s 16-bit slots hold
   every `char`. A fix: require every character inside the window, as the rank coder's `exactlyEncodable` does.

**Corpus sizes.** The harness loads 304,905 distinct english words and 49,446 chinese against your 304,959 and 50,009.
`getWords`' minimum word length of 2 (`getWordArray(resource, …, 2)`) would plausibly account for the gaps of 54 and
563, but we have not counted them. chinesenames matches your 1,145,009. Stale since the splitter fix:
`StringSortBenchmarks.java:47` ("the 275,333-word English corpus" and its duplicate density) and the run counts and
"275,333-word vocabulary" at `:67–74`.

**JSON checks** (`runner/validate-json.py`, outside the repo, presets `req11cd` and `req11cd-full`): exact (method,
params) row set with only the known-throwing rows absent, Cnt = 50, `rawData` 5 × 10, no null or NaN, `score ==
mean(rawData)`, `scoreConfidence == score ± scoreError`, one JVM and JMH version; 78 checks, 0 failures on steps 1–4,
and **582 rows, none in two files, exactly the 24 known-throwing rows absent** over the full suite (`--union`).
`runner/jmh-json-vs-log.py` matched every row to JMH's stdout at 3 decimals: **81 / 81** and **582 / 582** (`# Fork:`
3,030 = 582 × 5 + 120 failed forks).

## 11c — masking vs saturating coders in the cleanup pass (ms/op)

Command 1 under Reproduction (`req11c-cleanup-coders.json`, 20 rows, 1 s iterations, 01:20–01:59): all five coders in
one invocation, as you asked. No row is ‡ or § (mean t 0.987–1.016). Your columns are your hand timings (eight cores,
best of four).

| coder | n | `timsortCleanup` | `adaptiveInsertionCleanup` | adaptive ÷ Timsort | your Timsort / adaptive | your adaptive ÷ Timsort | ours ÷ yours (Timsort, adaptive) |
|---|---:|---:|---:|---:|---:|---:|---|
| englishSaturating | 200,000 | 17.049 ± 0.253 | 16.763 ± 0.240 | 0.98×† | 10.73 / 10.79 | 1.01× | 1.59×, 1.55× |
| englishSaturating | 1,000,000 | 59.665 ± 1.525 | 63.955 ± 1.735 | 1.07× | 38.03 / 39.09 | 1.03× | 1.57×, 1.64× |
| englishMasking | 200,000 | 17.434 ± 0.186 | 63.680 ± 0.397 | 3.65× | 11.18 / 47.84 | 4.28× | 1.56×, 1.33× |
| englishMasking | 1,000,000 | 63.262 ± 1.764 | 2455.429 ± 30.203 | 38.81× | 43.51 / 830.89 | 19.10× | 1.45×, 2.96× |
| asciiSaturating | 200,000 | 18.354 ± 0.307 | 17.356 ± 0.258 | 0.95× | 12.15 / 10.65 | 0.88× | 1.51×, 1.63× |
| asciiSaturating | 1,000,000 | 62.776 ± 1.085 | 67.302 ± 1.374 | 1.07× | 43.57 / 45.04 | 1.03× | 1.44×, 1.49× |
| asciiMasking | 200,000 | 19.586 ± 0.694 | 64.992 ± 0.387 | 3.32× | 15.97 / 57.27 | 3.59× | 1.23×, 1.13× |
| asciiMasking | 1,000,000 | 67.025 ± 2.992 | 2500.991 ± 24.467 | 37.31× | 54.19 / 888.70 | 16.40× | 1.24×, 2.81× |
| unicode | 200,000 | 38.385 ± 0.352 | 36.137 ± 0.794 | 0.94× | 30.35 / 27.83 | 0.92× | 1.26×, 1.30× |
| unicode | 1,000,000 | 171.958 ± 1.688 | 441.733 ± 2.428 | 2.57× | 133.25 / 223.66 | 1.68× | 1.29×, 1.98× |

The discriminating ratio, masking ÷ saturating within each pair:

| pair, method | 200,000 | 1,000,000 | fork-extreme at 1M | medians at 1M | your hand ratio (200k / 1M) | your prediction at 1M | verdict |
|---|---:|---:|---|---:|---|---|---|
| english, `timsortCleanup` | 1.02×† | **1.06×** | 0.94–1.18× | 1.07× | 1.04× / 1.14× | 0.94–1.25× | IN |
| ascii, `timsortCleanup` | 1.07× | **1.07×** | 0.98–1.23× | 1.04× | 1.31× / 1.24× | 0.94–1.25× | IN; fork 4 inflated by a build (Host conditions) |
| english, `adaptiveInsertionCleanup` | 3.80× | **38.39×** | 34.85–41.59× | 39.84× | 4.43× / 21.26× | 15–25× | OUT, above |
| ascii, `adaptiveInsertionCleanup` | 3.74× | **37.16×** | 35.53–41.13× | 36.22× | 5.38× / 19.73× | 15–25× | OUT, above |

Against the two cost models, with your structural counts (english corpus, n = 1,000,000). `N·log2 r` is Timsort's bound
as a ratio of log run counts (N cancels); `N + X` is adaptive insertion's comparisons plus moves; cost per extra
inversion = (adaptive masking − adaptive saturating) ÷ (X masking − X saturating).

| pair | runs ratio | inversions ratio | `N·log2 r` predicts | `N + X` predicts | Timsort measured | adaptive measured | adaptive cost per extra inversion, ours / yours (ns) | Timsort extra (ms) |
|---|---:|---:|---:|---:|---:|---:|---|---:|
| englishMasking ÷ englishSaturating | 1.077× | 1,637× | 1.008× | 163.4× | 1.06× | 38.39× | 13.268 / 4.393 | 3.597 |
| asciiMasking ÷ asciiSaturating | 1.041× | 855× | 1.004× | 153.0× | 1.07× | 37.16× | 13.159 / 4.562 | 4.249 |

**E11c-1 — the data refute `N + pX` for Timsort; `N log r` is right in kind and under-predicts.** Timsort's masking
penalty is 1.06× and 1.07× at 1,000,000 (1.07× / 1.04× by medians) and 1.02×† / 1.07× at 200,000, nowhere near the
inversion ratios or `N + X`'s 163.4× / 153.0×. The CIs are disjoint at 1,000,000, so the 3.597 and 4.249 ms are resolved
differences. `N·log2 r` predicts an excess of 0.8 % and 0.4 %; the measured 6.0 % and 6.8 % (6.6 % and 4.1 % by medians)
is 7.8× and 17.5× that, and we have not found what the rest is. The ascii 1.07× includes asciiMasking fork 4, which a
build on the host inflated to 74.371 against 63.935–66.973 for the other four (Host conditions).

**E11c-2 — adaptive insertion's masking penalty is about 38×, above your 15–25× and about a quarter of `N + X`.** 38.39×
(fork-extreme 34.85–41.59×) and 37.16× (35.53–41.13×) against your 21.26× / 19.73×. Fitting `a·N + b·X` to each pair
gives a = 62.5 ns per element and b = 13.268 ns per inversion (english; 64.5 and 13.159 for ascii), where a single `k`
in `k(N + pX)` would need a = b. Our cost per extra inversion (13.268 / 13.159 ns against your 4.393 / 4.562) agrees
across the pairs to 1 %, a property of this host: our Timsort rows take 1.23×–1.59× your time but our masking adaptive
rows 2.96× / 2.81× (2455.429 ± 30.203 and 2500.991 ± 24.467 against your 830.89 / 888.70 ms/op), which is why the ratio
doubles. Your conclusion stands: 180 M extra inversions cost Timsort 3.597 ms.

**E11c-3 — the 11a directions replicate on the new corpus.** At 1,000,000 Timsort still beats adaptive:
englishSaturating 1.07× (1.14× in 11a), unicode 2.57× (2.80×). At 200,000 englishSaturating ties in both (0.98×†, 1.06×†
in 11a), and unicode moved from a tie (1.03×†) to an adaptive win (0.94×). The rows moved with the corpus:
englishSaturating `timsortCleanup` is 1.82× its 11a value at 200,000 (9.375 then) and 1.15× at 1,000,000 (51.875). The
clean replication is pinyin, whose corpus is unchanged, through step A's `timsortCleanup`: 99.861 ± 1.035 against 99.245
in 11a (1.01×†) and 517.831 ± 3.730 against 505.914 (1.02×).

## 11d step A — timsortCleanup vs parallelTimsortCleanup (ms/op)

Command 2 under Reproduction (`req11d-cleanup-parallel.json`, 32 rows, 1 s iterations, 02:01–02:53). Speed-up = Timsort
÷ parallel, > 1 = parallel faster; descents from I1; "your table" is the `ParallelRadixHuskySort` Javadoc table at
`d947e77` (eight cores, seven pool workers). JMH forks a fresh JVM per row, so each cell runs one coder per JVM, as your
table was measured. Verdict rule, on the steady level where a row is ‡: a point "~p" is IN within ÷/× 1.2, NEAR within
÷/× 1.5, OUT beyond that or if the winner flips; a band is IN inside it.

| coder | n | descents | `timsortCleanup` | `parallelTimsortCleanup` | speed-up | steady | fork-extreme | your table: serial / parallel / speed-up | your prediction | verdict |
|---|---:|---:|---:|---:|---:|---:|---|---|---|---|
| englishSaturating | 200,000 | 1,999 | 17.382 ± 0.241 | 2.511 ± 0.237 ‡ | 6.92× | 7.82× | 5.93–8.52× | 10.82 / 10.36 / 1.04× | ~1.0× | OUT |
| englishSaturating | 1,000,000 | 16,060 | 62.928 ± 2.061 | 26.169 ± 7.950 ‡ | 2.40× | 2.77× | 1.52–4.14× | 42.38 / 22.72 / 1.87× | ~1.9× | NEAR |
| englishMasking | 200,000 | 2,592 | 17.542 ± 0.193 | 3.236 ± 0.148 ‡ | 5.42× | 5.75× | 5.07–5.92× | — | — | — |
| englishMasking | 1,000,000 | 17,304 | 64.243 ± 1.304 | 23.347 ± 6.216 ‡ | 2.75× | 3.02× | 1.41–3.62× | — | — | — |
| asciiSaturating | 200,000 | 3,747 | 18.195 ± 0.268 | 2.637 ± 0.208 ‡ | 6.90× | 7.63× | 6.05–8.12× | — | — | — |
| asciiSaturating | 1,000,000 | 29,326 | 67.803 ± 1.397 | 16.479 ± 1.191 ‡ | 4.11× | 4.14× | 3.64–4.41× | — | — | — |
| asciiMasking | 200,000 | 4,325 | 18.639 ± 0.265 | 3.217 ± 0.196 ‡ | 5.79× | 6.22× | 5.41–6.62× | — | — | — |
| asciiMasking | 1,000,000 | 30,519 | 69.645 ± 1.545 | 17.941 ± 1.224 | 3.88× | — | 3.33–4.23× | — | — | — |
| unicode | 200,000 | 66,042 | 38.379 ± 0.291 | 4.237 ± 0.097 | 9.06× | — | 8.83–9.30× | — | — | — |
| unicode | 1,000,000 | 366,864 | 172.822 ± 1.502 | 29.625 ± 5.987 ‡ | 5.83× | 6.29× | 3.48–7.25× | — | — | — |
| chineseUnicode | 200,000 | 3,106 | 3.791 ± 0.033 | 1.065 ± 0.013 | **3.56×** | — | 3.48–3.68× | 4.99 / 9.38 / 0.53× | ~0.5×, a loss | OUT |
| chineseUnicode | 1,000,000 | 16,789 | 13.305 ± 0.299 | 9.945 ± 1.734 ‡ | 1.34× | 1.63× | 1.12–1.44× | 12.47 / 8.34 / 1.50× | ~1.5× | IN |
| pinyin | 200,000 | 30,461 | 99.861 ± 1.035 | 15.618 ± 0.188 | 6.39× | — | 6.11–6.82× | 70.71 / 20.93 / 3.38× | 2.5–3.4× | OUT, above |
| pinyin | 1,000,000 | 162,689 | 517.831 ± 3.730 | 59.704 ± 1.035 | 8.67× | — | 8.19–8.91× | 290.82 / 114.64 / 2.54× | 2.5–3.4× | OUT, above |
| pinyinRank | 200,000 | 0 | 27.707 ± 0.104 | 8.251 ± 0.072 | 3.36× | — | 3.25–3.42× | — | no faster, possibly slower | OUT |
| pinyinRank | 1,000,000 | 0 | 90.527 ± 0.640 | 25.765 ± 0.803 | 3.51× | — | 3.45–3.59× | — | no faster, possibly slower | OUT |

**E11d-A1 — the parallel cleanup wins every cell, and chineseUnicode@200k does not reproduce your loss.** It wins 3.56×
(fork-extreme 3.48–3.68×, row converged) against your 0.53×. It is not a JMH warm-up effect: the first 1 s warm-up
iteration, averaged over the five forks, already shows 3.28× (`jmh-step2.log`, not in the JSON). Our data cannot say why
your machine lost.

**E11d-A2 — every gain is at or above your prediction, and three of the six cells stay within what 15 against 7 pool
workers allow.** englishSaturating@200k is 6.92× (steady 7.82×) against ~1.0×; englishSaturating@1M 2.40× by score and
2.77× steady against ~1.9×, NEAR, with bimodal fork means (37.733 / 16.106 / 28.445 / 32.159 / 16.403) whose
fork-extreme range 1.52–4.14× contains your 1.87×; chineseUnicode@1M 1.63× steady (1.34× by score) against ~1.5×, IN;
pinyin 6.39× and 8.67× against 2.5–3.4×, both rows converged. The halving you predicted concerns ratios against
`Arrays.parallelSort` (step C). Here the serial arm is `Arrays.sort` on one thread (`CleanupPassBenchmarks.java:230`)
and the parallel arm is `Arrays.parallelSort` itself, so more cores widen the gain instead of halving it. With 15 pool
workers against your 7, and the serial arm on one thread in both, worker count alone can make our gain at most 15/7 =
2.14× yours; a per-core speed difference cancels in that ratio. The gain ratio, our speed-up ÷ yours from your serial
and parallel times, equals (our serial time ÷ yours) × (your parallel time ÷ ours). It stays within 2.14× in three
cells: englishSaturating@1M 1.48× and chineseUnicode@1M 1.09× (steady), where our parallel arm is no faster than yours
(1.00× and 1.02×) and our serial arm takes 1.48× and 1.07× your time, and pinyin@200k 1.89× (parallel 1.34× faster,
serial 1.41× your time). Three cells exceed it: pinyin@1M 3.42× (parallel 1.92× faster than yours, serial 1.78× your
time), englishSaturating@200k 6.63×, 7.49× steady (parallel 4.13× faster, 4.66× steady, serial 1.61× your time), and
chineseUnicode@200k 6.69× (parallel 8.80× faster, serial 0.76× your time, the one cell where our serial arm is the
faster). Those three are unexplained.

**E11d-A3 — the `pinyinRank` coder puts the cleanup at the floor, and the parallel cleanup still divides it.** The
hand-over has 0 descents, so `timsortCleanup` is n − 1 `NAME_ORDER` comparisons: 27.707 ± 0.104 and 90.527 ± 0.640 ms,
138.5 and 90.5 ns per comparison. `Arrays.parallelSort` splits them across the pool and is 3.36× and 3.51× faster. A
comparison costs 1.53× more at 200,000 than at 1,000,000, and we have not explained why. One untested candidate: the
benchmark samples with replacement (`CleanupPassBenchmarks.java:205`), so we expect 33.3 % of adjacent pairs at
1,000,000 to be the same name against 8.2 % at 200,000, and `NAME_ORDER` skips the pinyin lookup on equal characters
(`HuskyCoderChinesePinyin.compareCharacter`). `pinyin`'s serial cleanup is 3.60× / 5.72× `pinyinRank`'s; in the real
sorter the rank coder reports `perfect` and no cleanup runs (I3).

**The rule.** You asked for a rule not stated in n, because chinese and chinesenames at n = 200,000 are the same size
and want opposite answers (your step A; `ParallelRadixHuskySort.java:168–169`). That premise did not reproduce
(chineseUnicode@200k wins 3.56×, and end to end chinese@200k's `pAll_parCleanup` takes 0.59× pAll's time), and with all
16 cells won, from 1.63× to 9.06×, an n-bounded rule is defensible. What the gain tracks, as Spearman ρ of the speed-up
over the 16 cells (steady level for ‡ rows):

- serial cost per element: ρ = 0.73 (0.90 within n = 1,000,000; 0.21 within n = 200,000)
- descents per element: ρ = 0.53; descents: ρ = 0.34; n: ρ = −0.43

Neither descents nor the cost of a comparison predicts the gain. The `pinyinRank` coder has 0 descents and gains 3.36× /
3.51×, englishSaturating@200k has 1,999 and gains 7.82× steady, unicode@200k has 66,042 and gains 9.06×. The
`pinyinRank` coder compares with `NAME_ORDER`, and its serial cost per element (138.5 / 90.5 ns) is the 5th and 8th
highest of the 16 cells, yet its gains are the 4th and 5th smallest; englishSaturating@200k, at 86.9 ns, gains 7.82×.
The one loss of the parallel cleanup we measured is outside step A: end to end at n = 32,000 on english, where the radix
phase is one chunk, pAll ÷ pAll_parCleanup = 0.90×† (steady 0.88×) and parallelSort ÷ pAll_parCleanup = 0.89× (full
suite; the husky rows are ‡). **The rule the data support on this host: at n ≥ 200,000, parallelize the cleanup whatever
the coder.** The smallest gain measured is chineseUnicode@1M (1.63× steady), the cheapest cleanup per element (13.3 ns).
We measured nothing between 32,000 and 200,000, so we cannot say where it stops paying.

The ten `timsortCleanup` cells shared by 11c and step A agree within 0.95×–1.08× (nine with CIs overlapping;
asciiSaturating@1M, 1.08×, does not), the drift to expect between two invocations on one jar the same night.

## 11d step B — the serial claim on chinesenames

Command 3 under Reproduction (`req11d-pinyin-rank-serial.json`, 9 rows, 02:55–03:21). No row is ‡ or § (mean t
0.983–1.047).

| n | `radixHuskySortAuto` (ordinal) | `radixHuskySortAutoPinyinRank` | `systemSortPinyin` | ordinal ÷ rank | `systemSortPinyin` ÷ rank | `systemSortPinyin` ÷ ordinal | yours: ordinal ÷ rank | yours: systemSort(pinyin) ÷ rank |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 24.528 ± 0.248 | 2.886 ± 0.079 | 73.072 ± 0.386 | 8.50× | 25.32× | 2.98× | — | — |
| 200,000 | 151.268 ± 0.377 | 36.208 ± 0.297 | 550.801 ± 8.896 | **4.18×** | **15.21×** | 3.64× | 5.67× | 20.03× |
| 1,000,000 | 768.193 ± 15.637 | 173.470 ± 1.870 | 3211.504 ± 64.063 | **4.43×** | **18.51×** | 4.18× | 5.94× | 27.12× |

**E11d-B1 — the rank coder is the serial headline, inside both bands.** Ordinal ÷ rank = 4.18× (fork-extreme 4.05–4.25×)
and 4.43× (4.27–4.65×) against 4–6×; `systemSortPinyin` ÷ rank = 15.21× (14.29–15.91×) and 18.51× (16.84–19.32×) against
15–27×. The request-11 figure this replaces, `systemSortPinyin` ÷ `radixHuskySortAuto` at 1,000,000, was 4.26×; it is
4.18× now. At 32,000, where your table has no column, the rank coder is 8.50× the ordinal and 25.32× `systemSortPinyin`.
Our ratios sit at the low end of your bands because our rank row takes 2.16× / 2.12× your time against 1.59× / 1.58×
(ordinal) and 1.64× / 1.45× (`systemSortPinyin`), which we cannot explain. Against request 11, on the same corpus,
`radixHuskySortAuto` is 0.99×† / 1.01×† and `systemSortPinyin` 0.97× / 0.99×† of request 11's full-suite rows. `d947e77`
touches the encode path of `radixHuskySortAuto` here (`HuskyCoderChinesePinyin` now overrides `huskyEncode(String[])` to
test for the rank dialect first), and the row did not move.

## 11d step C — end to end, all corpora

Command 4 under Reproduction (`req11d-strings-cleanup.json`, 03:23–04:19). **Two-methods rule: not met, by your
command**, which puts the baseline `systemSortParallel` (`Arrays.parallelSort`, sorting last) in one invocation with
three husky methods; we ran no two-method confirmation. The 10-method full suite reproduces the 20 rows the two share
within 0.928–1.019. 20 of 24 rows are in the JSON: `_pAll_pinyinRank` throws by design on english and chinese, and JMH's
default `-foe false` logged 20 `<failure>` fork lines and continued, exit 0.

| corpus | n | parallelSort | pAll | pAll_parCleanup | pAll_pinyinRank | parallelSort ÷ pAll | parallelSort ÷ pAll_parCleanup | parallelSort ÷ pAll_pinyinRank | pAll ÷ pAll_parCleanup |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| english | 200,000 | 11.648 ± 0.174 | 24.900 ± 1.402 ‡ | 9.668 ± 0.160 | — | 0.47× | 1.20× | — | 2.58× |
| english | 1,000,000 | 93.530 ± 3.387 | 111.467 ± 24.530 ‡ | 63.867 ± 15.580 ‡ | — | 0.84×† (steady 1.22×) | 1.46× (steady 1.94×) | — | 1.75× |
| chinese | 200,000 | 6.708 ± 0.061 | 10.216 ± 0.115 | 5.988 ± 0.089 | — | 0.66× | 1.12× | — | 1.71× |
| chinese | 1,000,000 | 46.869 ± 2.561 | 40.690 ± 0.787 | 31.844 ± 1.173 | — | 1.15× | 1.47× | — | 1.28× |
| chinesenames | 200,000 | 47.233 ± 0.731 | 110.958 ± 1.174 | 25.519 ± 0.273 | 8.251 ± 0.061 | 0.43× | **1.85×** | **5.72×** | **4.35×** |
| chinesenames | 1,000,000 | 240.026 ± 4.829 | 558.565 ± 14.330 | 107.063 ± 6.892 § | 35.326 ± 1.598 | 0.43× | **2.24×** (6–10: 2.05×) | **6.79×** (6–10: 6.45×) | **5.22×** (6–10: 4.77×) |

A ratio above 1 means the right-hand method is faster; "(6–10: …)" is the ratio on iterations 6–10 of both rows.
Fork-extreme ranges on chinesenames at 200,000 / 1,000,000: parallelSort ÷ pAll_pinyinRank 5.53–5.98× / 6.29–7.54×, pAll
÷ pAll_parCleanup 4.16–4.59× / 4.96–5.52×, parallelSort ÷ pAll_parCleanup 1.75–1.95× / 2.09–2.44×, pAll_parCleanup ÷
pAll_pinyinRank 3.03–3.20× / 2.80–3.32×. ‡ rows on their steady level (median of all 50 iterations in brackets): pAll
english@200k 22.913 (24.958), pAll english@1M 76.538 (83.546), pAll_parCleanup english@1M 48.255 (50.381). **The §
row**, pAll_parCleanup chinesenames@1M, averages 97.260 over iterations 1–5 and 116.867 over 6–10 (t 0.78–0.88);
pAll_pinyinRank also slows (33.465 against 37.187) but has two forks above 0.90 (t 0.87–0.95), so no §. On iterations
6–10 of both, pAll_parCleanup ÷ pAll_pinyinRank is 3.14×. No verdict changes; we have not established the cause.

Your chinesenames table beside ours (ms; the serial rows are step B's):

| row | ours, 200,000 | yours | ours ÷ yours | ours, 1,000,000 | yours | ours ÷ yours |
|---|---:|---:|---:|---:|---:|---:|
| serial ordinal (`radixHuskySortAuto`) | 151.268 ± 0.377 | 95.01 | 1.59× | 768.193 ± 15.637 | 486.10 | 1.58× |
| serial rank (`radixHuskySortAutoPinyinRank`) | 36.208 ± 0.297 | 16.76 | 2.16× | 173.470 ± 1.870 | 81.85 | 2.12× |
| pAll | 110.958 ± 1.174 | 90.13 | 1.23× | 558.565 ± 14.330 | 337.90 | 1.65× |
| pAll parCleanup | 25.519 ± 0.273 | 31.48 | 0.81× | 107.063 ± 6.892 § | 141.32 | 0.76× (6–10: 0.83×) |
| pAll pinyinRank | 8.251 ± 0.061 | 7.41 | 1.11× | 35.326 ± 1.598 | 31.48 | 1.12× |
| parallelSort (`systemSortParallel`) | 47.233 ± 0.731 | 87.52 | 0.54× | 240.026 ± 4.829 | 497.04 | 0.48× |
| systemSort(pinyin) (`systemSortPinyin`) | 550.801 ± 8.896 | 335.78 | 1.64× | 3211.504 ± 64.063 | 2219.84 | 1.45× |

Your ratios from the same table: parallelSort ÷ pAll pinyinRank 11.81× / 15.79×, pAll ÷ pAll parCleanup 2.86× / 2.39×,
parallelSort ÷ pAll parCleanup 2.78× / 3.52×, parallelSort ÷ pAll 0.97× / 1.47×.

**E11d-C1 — `parallelSort` ÷ `pAll_pinyinRank` is 5.72× and 6.79× (6.45× on iterations 6–10), inside your 5–9×, and
about half of yours, as you predicted.** Our `Arrays.parallelSort` takes 0.54× / 0.48× your time and our pAll_pinyinRank
row 1.11× / 1.12×, so the ratio reads 0.48× / 0.43× (0.41× on iterations 6–10) of your 11.81× / 15.79×. On chinesenames
`systemSortParallel` and pAll are the same input and code as in request 11 and did not move (0.99×† / 1.02×† and 1.00×†
/ 1.00×†); pAll is still 0.43× / 0.43× of `Arrays.parallelSort`, against 0.43× / 0.42× then.

**E11d-C2 — the parallel cleanup is worth 4.35× / 5.22× over pAll on chinesenames (4.77× on iterations 6–10), above your
2.4–2.9×, and beats `Arrays.parallelSort` 1.85× / 2.24× (2.05×) where you expected parity.** A cross-invocation phase
sum comes close: step C's pAll (2 s iterations) minus step A's serial pinyin cleanup plus its parallel one (1 s
iterations) predicts 26.715 / 100.437 ms against 25.519 / 107.063 measured (0.96× / 1.07×; 1.16× with the § row on
iterations 6–10). parallelSort ÷ pAll_parCleanup is 0.67× / 0.64× of yours (0.58× on iterations 6–10), less than halved,
although C1's ratio, whose two arms are also both parallel, did halve. The difference is that pAll_parCleanup's cleanup
phase is `Arrays.parallelSort` itself, so it scales with the same cores as the baseline: step A's parallel pinyin
cleanup takes 0.75× / 0.52× your time and our `Arrays.parallelSort` 0.54× / 0.48×, where our pAll_pinyinRank, which runs
no cleanup (I3), takes 1.11× / 1.12×.

**E11d-C3 — on chinesenames the rank coder wins by about 3×.** pAll_parCleanup ÷ pAll_pinyinRank = 3.09× (fork-extreme
3.03–3.20×) and 3.03× (2.80–3.32×; 3.14× on iterations 6–10).

**E11d-C4 — on english and chinese, where the rank coder does not apply, the parallel cleanup turns every loss into a
win.** parallelSort ÷ pAll_parCleanup = 1.20× (english@200k), 1.94× steady (english@1M; 1.46× by score, ‡), 1.12×
(chinese@200k), 1.47× (chinese@1M), where parallelSort ÷ pAll is 0.47×, 1.22× steady, 0.66× and 1.15×. The phase sum
predicts these cells less well: measured ÷ predicted is 1.25× / 1.33× on english (the end-to-end row is slower than its
phases) and 0.80× / 0.90× on chinese (faster), and we have not looked into why. These are new inputs since `9bb0385`
(`systemSortParallel` itself moved 1.16× / 1.05×† on english and 1.12× / 1.14× on chinese), so their request-11 rows are
superseded, not compared. The `parCleanup` Javadoc's three projections from request 11 are graded in the Predictions
table.

## Predictions, one line each

| step | your prediction | ours | verdict |
|---|---|---|---|
| 11c | Timsort masking ÷ saturating at 1M within 0.94–1.25× | 1.06× english, 1.07× ascii (1.04× by medians) | **CONFIRMED** |
| 11c | adaptive masking ÷ saturating at 1M within 15–25× | 38.39×, 37.16× | **NOT CONFIRMED**, above |
| 11c | "1,637x the inversions should cost Timsort about nothing and cost adaptive insertion sort about 20x" | Timsort 1.06×; adaptive about 38× | **PARTLY** |
| 11c | masking adaptive rows at 1M roughly 830–890 ms/op, "still under a second, so no row here should behave like 11a's pinyin case" | 2455.429 ± 30.203 and 2500.991 ± 24.467 ms/op, 2.96× / 2.81× your time; each row took about 3 min 20 s and all of 11c 39 min, against about 30 min for 11a's pinyin row alone | **NOT CONFIRMED** on time per op; **CONFIRMED** that no row behaved like 11a's pinyin case |
| 11c | masking wins the whole sort by "more like 70–90 ms on yours" per million words | encode 77.398 minus cleanup 3.597 = 73.8 ms per million at 1M, 81.7 at 200k (two invocations; no whole-sort row) | **CONFIRMED** on the components |
| 11d A | englishSaturating@200k ~1.0× | 6.92× (steady 7.82×) | **OUT** |
| 11d A | englishSaturating@1M ~1.9× | 2.40× (steady 2.77×, 1.46× off; forks 1.52–4.14×) | **NEAR** |
| 11d A | chineseUnicode@200k ~0.5×, a loss | 3.56×, a win | **OUT** |
| 11d A | chineseUnicode@1M ~1.5× | 1.34× ‡ (steady 1.63×) | **IN** |
| 11d A | pinyin 2.5–3.4× | 6.39×, 8.67× | **OUT**, above |
| 11d A | pinyinRank: parallel no faster, possibly slower | 3.36×, 3.51× faster | **OUT** |
| 11d B | ordinal ÷ rank 4–6× | 4.18×, 4.43× | **CONFIRMED** |
| 11d B | `systemSortPinyin` ÷ rank around 15–27× | 15.21×, 18.51× | **CONFIRMED** |
| 11d B/C | "below 3x on either" means a misunderstanding | 0 of 6 serial and 0 of 2 parallel ratios below 3× | **NOT TRIGGERED** |
| 11d C | `parallelSort` ÷ `pAll_pinyinRank` 5–9× | 5.72×, 6.79× (6.45× on iterations 6–10) | **CONFIRMED** |
| 11d C | ratios roughly half of yours | parallelSort ÷ pAll_pinyinRank 0.48× / 0.43× of yours (0.41× on iterations 6–10); parallelSort ÷ pAll_parCleanup 0.67× / 0.64× (0.58×) | **PARTLY** |
| 11d C | pAll ÷ pAll_parCleanup 2.4–2.9× on chinesenames | 4.35×, 5.22× (4.77× on iterations 6–10) | **NOT CONFIRMED**, above |
| 11d C | … "roughly parity with `Arrays.parallelSort`" | parallelSort ÷ pAll_parCleanup 1.85×, 2.24× (2.05×) | **NOT CONFIRMED**, beats it |
| 11d | "one of them to make the other largely irrelevant" | rank coder, 3.09× / 3.03× (3.14×) over pAll_parCleanup on chinesenames | **the rank coder** |
| Javadoc | parCleanup rescues chinesenames, widens english@1M, regresses chinese@200k | parallelSort ÷ pAll_parCleanup 1.85× / 2.24× (2.05× on iterations 6–10); 1.22× → 1.94× steady; chinese@200k 0.59× of pAll's time | **CONFIRMED / CONFIRMED / NOT CONFIRMED** |

Pairs are n = 200,000 then 1,000,000 unless stated; step-A rows carry the step-A table's IN / NEAR / OUT verdict. In 11c
the Timsort prediction held and the adaptive one was exceeded, for the host reason in E11c-2. In 11d the serial
predictions and the rank coder's end-to-end gain held; every miss, pinyinRank's step-A cell included, is a larger
parallel-cleanup gain than predicted (E11d-A2 for which of them stay within what 15 against 7 pool workers allow).

## The full suite again (2 s iterations; ms/op)

Eight invocations, one per class (commands 5–12 under Reproduction), shortest class first, Sat 14:51 PT to Sun 19:18 PT,
28 h 10 min of JMH time. Before launch, `-l` showed that the eight class regexes list exactly the 116 methods of request
11's `-e CleanupPassBenchmarks`, so the rows are request 11's plus the 15 that `243b317` and `d947e77` added: 606
attempted, **582 in the eight JSONs** (Adversarial 124, Date 6, Numeric 123, ParallelRadix 14, ParallelString 84, Permit
42, String 171, Tuple 18), 24 absent by design (disclosure d).

**What changed against request 11, and why.** Keyed by (class, method, params) against `req11-full-suite.json`; the
baselines are each class's `Arrays.sort` / `Arrays.parallelSort` rows, whose code no commit touched.

| class, input | rows compared | new ÷ request 11, median (range) | baselines, new ÷ request 11 | reading |
|---|---:|---|---|---|
| String, english | 57 | 1.095 (0.787–1.666) | 0.92–1.07 | `corpus changed (9bb0385)` |
| String, chinese | 54 | 1.172 (1.006–1.299) | 1.09–1.18 | `corpus changed (9bb0385)` |
| String, chinesenames, sorting rows | 48 | 0.995 (0.929–1.040) | 0.93–1.00 | unchanged |
| String, chinesenames, encode-only rows | 9 | 1.103 (0.958–1.543) | — | invocation-sensitive (below) |
| ParallelString, english | 24 | 1.219 (1.015–1.439) | 1.05–1.14 | `corpus changed (9bb0385)` |
| ParallelString, chinese | 24 | 1.214 (1.060–1.567) | 1.09–1.15 | `corpus changed (9bb0385)` |
| ParallelString, chinesenames | 24 | 1.014 (0.975–1.071) | 0.97–1.01, CIs overlap | unchanged |
| Adversarial, `sharedPrefix*` (english Leipzig words) | 40 | 1.049 (1.018–1.253) | 1.02–1.09 | `corpus changed (9bb0385)` |
| Adversarial, `collapsedBits*` | 84 | 1.014 (0.965–1.145) | 0.97–1.04 | unchanged |
| Numeric | 123 | 1.004 (0.951–1.064) | 0.97–1.06 | unchanged |
| Date / Tuple / ParallelRadix | 6 / 18 / 14 | 1.005 / 0.984 / 1.011 | 1.02 / 0.94–0.99 / 0.94–1.06 | unchanged |
| Permit | 42 | 1.060 (0.952–1.197) | 1.06–1.16, CIs disjoint | class-wide shift (disclosure e) |

The english and chinese rows of both string classes and the Adversarial `sharedPrefix*` rows sort different arrays than
in request 11 (`9bb0385`; `SharedPrefixState` loads the english Leipzig file through the same `REGEX_LEIPZIG`), so their
request-11 values are superseded, not compared. On unchanged input the chinesenames sorting rows, Numeric, Date, Tuple
and `Long[]` stay within 0.93–1.07 of request 11, and the chinesenames parallel rows within 0.975–1.071. Three groups on
unchanged input moved more, and we read none of them as code:

- **Four Adversarial rows at 1,000,000**, CIs disjoint: `collapsedBitsDualPivotQuicksort` at fixedHighBits 0 / 32 / 48
  (1.11× / 1.10× / 1.15×) and `collapsedBitsQuickHuskySort` at fixedHighBits 0 (1.13×). No commit since request 11
  touched their code, and their `Arrays.sort` baseline moved 0.97–1.04.
- **The chinesenames encode-only rows.** `huskyEncodeOnly` moved 1.28× / 1.10× / 1.03× at 32,000 / 200,000 / 1,000,000,
  and the two English-coder controls 0.96× / 1.54× / 1.13× (`…Masking`) and 1.00×† / 1.25× / 1.09× (`…Saturating`).
  `d947e77` sits on the path of `huskyEncodeOnly` here (the `huskyEncode(String[])` override), but the controls, through
  untouched `BaseHuskySequenceCoder`, moved as much or more, and request 11 saw encode-only rows move 0.86–1.28× between
  two invocations of one jar, so we do not attribute the pinyin row to `d947e77`.
- **The permits class** (disclosure e).

### Strings — `ParallelStringSortBenchmarks` (84-row matrix in the appendix)

| corpus, n | `serial Auto` | `pAll` | `pAll_parCleanup` | `pAll_pinyinRank` | `systemSortParallel` | parallelSort ÷ pAll | parallelSort ÷ pAll_parCleanup | parallelSort ÷ pAll_pinyinRank | pAll ÷ pAll_parCleanup | new ÷ req 11: pAll, parallelSort |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| english, 32,000 | 4.621 ± 0.260‡ | 4.947 ± 0.263‡ | 5.477 ± 0.318‡ | — | 4.852 ± 0.041 | 0.98×† (steady 1.07×) | 0.89× (steady 0.94×) | — | 0.90×† (steady 0.88×) | corpus changed (9bb0385) |
| english, 200,000 | 62.000 ± 0.976 | 23.214 ± 1.267‡ | 9.461 ± 0.073 | — | 11.414 ± 0.199 | 0.49× (steady 0.54×) | 1.21× | — | 2.45× (steady 2.24×) | corpus changed (9bb0385) |
| english, 1,000,000 | 279.893 ± 4.550 | 104.570 ± 21.502‡ | 59.276 ± 12.147‡ | — | 93.803 ± 2.385 | 0.90×† (steady 1.24×) | 1.58× (steady 2.01×) | — | 1.76× (steady 1.61×) | corpus changed (9bb0385) |
| chinese, 32,000 | 2.333 ± 0.032 | 3.159 ± 0.157 | 2.216 ± 0.029 | — | 3.266 ± 0.019 | 1.03×† | 1.47× | — | 1.43× | corpus changed (9bb0385) |
| chinese, 200,000 | 14.745 ± 0.336 | 10.037 ± 0.089 | 5.897 ± 0.064 | — | 6.638 ± 0.017 | 0.66× | 1.13× | — | 1.70× | corpus changed (9bb0385) |
| chinese, 1,000,000 | 68.440 ± 0.950 | 39.953 ± 0.573 | 30.800 ± 1.483 | — | 47.437 ± 2.757 | 1.19× | 1.54× | — | 1.30× | corpus changed (9bb0385) |
| chinesenames, 32,000 | 24.598 ± 0.278 | 24.528 ± 0.291 | 15.692 ± 0.117 | 3.081 ± 0.090‡ | 21.474 ± 0.274 | 0.88× | 1.37× | 6.97× (steady 7.24×) | 1.56× | 1.05×, 1.01×† |
| chinesenames, 200,000 | 154.320 ± 1.420 | 113.027 ± 0.934 | 25.015 ± 0.080 | 8.194 ± 0.046 | 46.512 ± 0.811 | 0.41× | 1.86× | 5.68× | 4.52× | 1.02×†, 0.97×† |
| chinesenames, 1,000,000 | 761.884 ± 12.608 | 561.969 ± 12.639 | 105.019 ± 7.370§ | 33.965 ± 1.455 | 235.311 ± 4.356 | 0.42× | 2.24× (6–10: 2.04×) | 6.93× (6–10: 6.51×) | 5.35× (6–10: 4.81×) | 1.01×†, 1.00×† |

A ratio above 1 means the right-hand method is faster. Brackets give the steady level where a side is ‡ (the one to
quote) and, as "(6–10: …)", iterations 6–10 of both rows for the § row, chinesenames@1M `pAll_parCleanup` (t 0.76–0.89
in every fork; 94.550 over iterations 1–5, 115.488 over 6–10), and for `parallelSort` ÷ `pAll_pinyinRank` in that row
(Marks). This class ran as one 10-method invocation with `systemSortParallel` last, so step C's ratios (a 4-method
invocation) remain the ones of record; the 20 rows the two share agree within 0.928–1.019 (median 0.981), 19 of them
with overlapping CIs.

- **chinesenames (unchanged input) is where the rank coder wins, as in step C.** `parallelSort` ÷ `pAll_pinyinRank` =
  5.68× / 6.93× at 200,000 / 1,000,000 (6.51× on iterations 6–10; 7.24× steady at 32,000, where one fork spiked), ÷
  `pAll_parCleanup` 1.86× / 2.24× (2.04×), ÷ `pAll` 0.41× / 0.42×. pAll and `systemSortParallel` are 0.97×–1.05× of
  request 11's rows.
- **english@1M is still unconverged after 5 × 2 s of warm-up.** pAll is 104.570 ± 21.502 ‡ (fork t up to 1.96, steady
  75.399) and `pAll_parCleanup` 59.276 ± 12.147 ‡ (steady 46.750). By score `parallelSort` ÷ pAll is 0.90×†; on the
  steady level it is 1.24×, and `parallelSort` ÷ `pAll_parCleanup` 2.01× (1.58× by score), against step C's 1.22× and
  1.94×. Request 11 needed `-w 10s` to settle this cell on the old corpus; nothing here settles it on the new one.
- **english@200k and chinese: the parallel cleanup turns pAll's losses into wins.** `parallelSort` ÷ `pAll_parCleanup` =
  1.21× on english@200k, where pAll trails 0.49× (steady 0.54×), and on chinese 1.47× / 1.13× / 1.54× at 32,000 /
  200,000 / 1,000,000 against pAll's 1.03×† / 0.66× / 1.19×. At 32,000 every husky row is one chunk, and english@32k is
  the one measured loss of the parallel cleanup (the rule, under step A): `pAll_parCleanup` is 0.89× `parallelSort`
  (steady 0.94×) against pAll's 0.98×† (steady 1.07×).
- **Speed-up over the serial sorter** (`serialRadixHuskySortAuto` ÷ pAll): 2.67× (steady 2.93×) / 2.68× (steady 3.71×)
  on english at 200,000 / 1,000,000, 1.47× / 1.71× on chinese, 1.37× / 1.36× on chinesenames; with the parallel cleanup
  6.55× / 4.72× (steady 5.99×), 2.50× / 2.22× and 6.17× / 7.25× (6.65× on iterations 6–10).

### Strings — `StringSortBenchmarks`, the columns the paper's tables quote (full 21-method rows in the appendix)

| corpus, n | `radixHuskySortAuto` | `radixHuskySort16` | `radixHuskySortAutoPinyinRank` | `quickHuskySort` | `huskyEncodeOnly` | `msdStringSort` | `systemSort` | `systemSortParallel` | `systemSortPinyin` | `insertionSort` | new ÷ req 11: `radixHuskySortAuto`, `systemSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| english, 32,000 | 4.556 ± 0.248‡ | 4.157 ± 0.353‡ | — | 9.477 ± 0.241 | 2.749 ± 0.067 | 4.218 ± 0.134‡ | 15.096 ± 0.354 | 4.864 ± 0.046 | — | 42.608 ± 0.353 | corpus changed (9bb0385) |
| english, 200,000 | 62.146 ± 0.958 | 58.892 ± 1.111 | — | 114.408 ± 1.298 | 35.249 ± 0.359 | 58.782 ± 2.266‡ | 156.327 ± 1.774 | 11.225 ± 0.208 | — | 1216.232 ± 3.587 | corpus changed (9bb0385) |
| english, 1,000,000 | 279.839 ± 4.303 | 277.812 ± 4.128 | — | 782.113 ± 25.874 | 177.937 ± 2.156 | 319.260 ± 19.741‡ | 1293.911 ± 24.789 | 91.832 ± 3.792 | — | 27011.104 ± 69.270 | corpus changed (9bb0385) |
| chinese, 32,000 | 2.346 ± 0.064 | 2.252 ± 0.023 | — | 5.237 ± 0.019 | 0.478 ± 0.010 | — | 10.743 ± 0.053 | 3.289 ± 0.020 | — | 33.658 ± 0.080 | corpus changed (9bb0385) |
| chinese, 200,000 | 14.853 ± 0.408 | 13.192 ± 0.164 | — | 38.153 ± 0.258 | 3.488 ± 0.066 | — | 82.426 ± 0.885 | 6.689 ± 0.023 | — | 1064.908 ± 2.614 | corpus changed (9bb0385) |
| chinese, 1,000,000 | 69.615 ± 1.142 | 68.927 ± 1.166 | — | 283.144 ± 3.768 | 18.267 ± 0.132 | — | 522.223 ± 6.460 | 46.619 ± 3.080 | — | 25980.364 ± 44.391 | corpus changed (9bb0385) |
| chinesenames, 32,000 | 24.411 ± 0.200 | 24.363 ± 0.275 | 2.839 ± 0.062 | 28.032 ± 0.302 | 5.341 ± 0.182 | — | 11.038 ± 0.192 | 21.655 ± 0.158 | 70.792 ± 1.133 | 37.968 ± 0.181 | 0.98×†, 0.93× |
| chinesenames, 200,000 | 155.016 ± 0.799 | 151.756 ± 1.395 | 35.873 ± 0.293 | 187.152 ± 0.592 | 42.753 ± 0.138 | — | 108.822 ± 0.964 | 47.241 ± 0.799 | 555.178 ± 8.194 | 1138.158 ± 2.764 | 1.02×†, 0.96× |
| chinesenames, 1,000,000 | 765.833 ± 12.932 | 772.406 ± 16.950 | 173.344 ± 1.703 | 1279.589 ± 5.965 | 216.067 ± 0.761 | — | 851.309 ± 9.251 | 229.075 ± 2.007 | 3248.071 ± 12.952 | 26424.280 ± 42.598 | 1.00×†, 0.94× |

— = absent by design. **Serial husky against the system sort, on the `9bb0385` corpora.** `systemSort` ÷
`radixHuskySortAuto` = 3.31× (steady 3.61×) / 2.52× / 4.62× on english at 32,000 / 200,000 / 1,000,000 and 4.58× / 5.55×
/ 7.50× on chinese. On chinesenames, against the pinyin-correct `systemSortPinyin`, the ordinal coder is 2.90× / 3.58× /
4.24× and the rank coder 24.94× / 15.48× / 18.74×, with ordinal ÷ rank 8.60× / 4.32× / 4.42× (step B: 25.32× / 15.21× /
18.51× and 8.50× / 4.18× / 4.43×; its 9 rows agree with the full suite within 0.969–1.025). `systemSort` ÷
`quickHuskySort` = 1.59× / 1.37× / 1.65× english and 2.05× / 2.16× / 1.84× chinese. The automatic width is within 1 % of
the best fixed width at 1,000,000 on english and chinese (279.839 ± 4.303 against `radixHuskySort16` 277.812 ± 4.128,
1.01×†; 69.615 ± 1.142 against 68.927 ± 1.166, 1.01×†) and the fastest ordinal-coder radix row on chinesenames. The
english 32,000 radix rows are ‡ (t up to 1.36), so those ratios are understated; `msdStringSort` is ‡ at every n (t up
to 1.28) and not compared.

**The encode pair on the new corpus**, which 11c's summary needed (request 11 measured it on the old tokenization):

| n | `huskyEncodeOnlyEnglishMasking` | `huskyEncodeOnlyEnglishSaturating` | saturating ÷ masking | saturating − masking (ms) | per million words (ms) | request 11 full suite: saturating ÷ masking |
|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 1.540 ± 0.027 | 2.742 ± 0.220‡ | 1.78× (steady 1.73×) | 1.202 | 37.561 | 1.85× |
| 200,000 | 18.498 ± 0.331 | 35.218 ± 0.436 | 1.90× | 16.720 | 83.599 | 2.24× |
| 1,000,000 | 98.077 ± 1.204 | 175.475 ± 2.388 | 1.79× | 77.398 | 77.398 | 1.92× |

Saturation costs 1.79×–1.90× the masking encode at 200,000 and 1,000,000, against request 11's 1.92×–2.24× on the old
corpus.

### Permits — all 14 methods (ms/op)

| n | `systemSort` | `systemSortParallel` | `dualPivotQuicksort` | `quickHuskySort` | `quickHuskySortWithCleanup` | `radixHuskySort8` | `radixHuskySort11` | `radixHuskySort16` | `radixHuskySortAuto` | `16_p4` | `16_p8` | `Auto_p8` | `Auto_p8_chunk4k` | `Auto_pAll` |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 14.241 ± 0.173 | 4.710 ± 0.036 | 12.825 ± 0.073 | 6.900 ± 0.099 | 7.414 ± 0.104 | 3.090 ± 0.053 | 2.806 ± 0.042 | 2.737 ± 0.032 | 2.856 ± 0.053 | 3.414 ± 0.061 | 3.427 ± 0.067 | 2.961 ± 0.035 | 2.951 ± 0.044 | 2.982 ± 0.049 |
| 100,000 | 65.585 ± 0.828 | 6.067 ± 0.127 | 55.805 ± 0.707 | 30.839 ± 0.267 | 38.913 ± 0.998 | 15.290 ± 0.428 | 15.444 ± 0.726 | 13.816 ± 0.151 | 13.845 ± 0.412 | 7.028 ± 0.031 | 6.824 ± 0.054 | 5.022 ± 0.032 | 4.975 ± 0.139 | 5.036 ± 0.143 |
| 198,900 | 159.560 ± 3.223 | 14.031 ± 0.191 | 140.333 ± 1.389 | 73.107 ± 1.623 | 91.575 ± 1.655 | 34.395 ± 1.246 | 33.340 ± 0.654 | 30.581 ± 1.045 | 35.995 ± 1.737 | 14.789 ± 0.551 | 12.287 ± 0.290 | 9.708 ± 0.344 | 9.681 ± 0.296 | 8.737 ± 0.184 |

| n | parallelSort ÷ pAll | fork-extreme | request 11 full suite parallelSort ÷ pAll | new ÷ req 11: systemSort | systemSortParallel | radixHuskySort16 | Auto_pAll | systemSort ÷ radixHuskySortAuto |
|---:|---:|---|---:|---:|---:|---:|---:|---:|
| 32,000 | 1.58× | 1.55–1.63× | 1.55× | 1.06× | 1.08× | 1.04× | 1.05× | 4.99× |
| 100,000 | 1.20× | 1.06–1.29× | 1.13× | 1.16× | 1.12× | 1.12× | 1.05×† | 4.74× |
| 198,900 | 1.61× | 1.44–1.74× | 1.51× | 1.15× | 1.11× | 0.95×† | 1.04× | 4.43× |

**The permits ratio holds; the absolute times moved.** `parallelSort` ÷ pAll = 1.58× / 1.20× / 1.61× at 32,000 / 100,000
/ 198,900 (every pAll fork faster than every `parallelSort` fork, at every n), against 1.55× / 1.13× / 1.51× in request
11's full suite, although both arms and `Arrays.sort` itself are 4–16 % slower than then (disclosure e). The class ran
as one 14-method invocation, so request 11's two-method confirmation remains the figure of record for that rule.
`radixHuskySortAuto`@198,900 is fork-bimodal again (fork spread 1.28), as in request 11.

### `Long[]` — `ParallelRadixSortBenchmarks` (ms/op) and everything else

| n | `serialRadixHuskySort11` | `parallelRadixHuskySort11_p1` | `parallelRadixHuskySort11_p2` | `parallelRadixHuskySort11_p4` | `parallelRadixHuskySort11_p8` | `quickHuskySort` | `systemSortParallel` |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 2,000,000 | 156.311 ± 4.665‡ | 199.300 ± 4.646 | 151.471 ± 4.601 | 109.478 ± 3.627 | 99.543 ± 1.010 | 1137.337 ± 12.474 | 92.150 ± 3.497 |
| 10,000,000 | 937.800 ± 22.884 | 1096.071 ± 23.453 | 817.041 ± 24.270 | 626.213 ± 25.924 | 571.985 ± 19.024 | 5897.260 ± 28.628 | 723.153 ± 50.901 |
| new ÷ req 11, 2,000,000 | 1.00×† | 1.02×† | 1.01×† | 1.04×† | 1.01×† | 1.03× | 1.06×† |
| new ÷ req 11, 10,000,000 | 0.98×† | 0.99×† | 1.01×† | 1.00×† | 0.98×† | 1.03× | 0.94×† |

`11_p8` against `Arrays.parallelSort`: 0.93× at 2,000,000 and 1.26× at 10,000,000 (request 11: 0.89× and 1.31×; the 10M
fork-extreme range is 0.90–1.43× because `systemSortParallel`@10M is 723.153 ± 50.901). `11_p1` is still slower than the
serial sorter, serial ÷ `11_p1` = 0.78× (steady 0.75×) / 0.86×. Elsewhere request 11's picture holds on unchanged input:
Numeric @500k `radixHuskySort16` against the system sort 5.08× (integer, 25.114 ± 0.215 vs 127.515 ± 1.031), 5.32×
(long) and 4.75× (double); Tuple @500k 3.54× (60.417 ± 0.738 vs 213.743 ± 3.111); Date @20k `radixHuskySort11` 4.93×;
Adversarial @1M `collapsedBitsDualPivotQuicksort` 360.297 ± 6.033 → 901.807 ± 20.295 from fixedHighBits 0 → 60 while
`collapsedBitsRadixHuskySort16` improves 70.730 ± 1.674 → 41.456 ± 1.256. On the new english corpus the `sharedPrefix*`
husky sorts still lose their edge at prefixLength ≥ 10: at 1,000,000 and prefixLength 10, radix16 960.652 ± 11.028
against `sharedPrefixSystemSort` 927.973 ± 9.702 (0.97×).

**Convergence.** 44 of 582 rows are ‡ (57 of 567 in request 11's full suite): 31 english string rows, `pAll_pinyinRank`
chinesenames@32k (one spiking fork), 11 Adversarial radix-8 / radix-11 rows at 1,000,000 (t 1.11–1.15) and
`serialRadixHuskySort11`@2M; no chinese, Permit, Numeric, Date or Tuple row. One row is §, chinesenames@1M
`pAll_parCleanup`, as in step C. The appendix lists and marks them all.

**Disclosures for the full suite.**

a. **Attempt 1 (step 5) was lost to a patch reboot and contributes no data.** It ran request 11's single command (`-e
   "CleanupPassBenchmarks"`) from 04:22 PT until an orderly patch reboot at 13:32 PT (last log write 13:30:45, shutdown
   13:32:23, new boot 13:32:56, `last -x -F`), at 33 % of the suite (1,002 `# Fork:` lines; Adversarial and Date
   complete, Numeric 70 of 123 rows). JMH writes the `-rff` file only at the end, so that JSON is 0 bytes; its logs are
   outside the repo.
b. **The suite ran as eight per-class invocations, so that a reboot would lose one class and not the run.** The row set
   is the single command's (checked with `-l`). Every comparison within a class is within one invocation; we quote no
   ratio between two classes. Request 11's full suite was one invocation.
c. **The kernel changed between steps 1–4 and the full suite**, from `6.12.103-127.188` to `6.12.103-129.197` (the patch
   reboot); same host, same jar, launched 1 h 16 min after the boot. The replication checks (steps B and C, 0.928–1.025)
   span the two kernels.
d. **24 rows are absent by design**, each a benchmark that throws for that corpus and that JMH's default `-foe false`
   logs and skips (120 `<failure>` fork lines, exit 0): `parallelRadixHuskySortAuto_pAll_pinyinRank` × {english,
   chinese} (`ParallelStringSortBenchmarks.java:172`), `radixHuskySortAutoPinyinRank` × {english, chinese}
   (`StringSortBenchmarks.java:212`), `systemSortPinyin` × {english, chinese} (`:193`) and `msdStringSort` × {chinese,
   chinesenames} (`:273`), each at the three n. Request 11 had the last 12.
e. **The permits class moved as a whole.** Every Permit row at 32,000 is 1.02×–1.08× its request-11 value and at 100,000
   and 198,900 0.95×–1.20×, with `Arrays.sort` (1.06× / 1.16× / 1.15×) and `Arrays.parallelSort` (1.08× / 1.12× /
   1.11×), CIs disjoint. The permits read no text corpus, Numeric did not move four hours later in the same unit
   (0.951–1.064), and `systemSort` is `Arrays.sort` on a copy, which no commit can change: a class-specific shift
   between invocations, of unknown cause. Ratios inside the class are unaffected.

## Host conditions per invocation

`load-monitor.log`: one `top -b -n 1` plus swap sample per minute. The table lists non-benchmark processes above 50 % of
one core; for each above 100 % it names the JMH fork that was running and whether that fork stands out from the row's
other four (how forks were placed in time: appendix, "Host conditions: fork placement").

| step | window | samples | load1 max / mean / median | swap MiB | benchmark java %CPU med / max | non-benchmark processes ≥ 50 %, and where they fell |
|---|---|---:|---|---|---|---|
| step 1 cleanup-coders | 01:20:40–01:59:43 | 39 | 7.46 / 2.36 / 1.86 | 391 → 390 | 100 / 450 | **a JVM build outside this run, 01:51–01:56**: four `java-or…` processes plus `javadoc`, up to 400 % each; `unison` 94 %, a `MainThread` 112 % (inside adaptive englishMasking@200k fork 4, 63.154 against 62.885–64.329) |
| step 2 cleanup-parallel | 02:01:43–02:53:42 | 52 | 13.01 / 5.13 / 4.43 | 390 → 390 | 244 / 1294 | `kcompac…` 100 % (parallel asciiSaturating@1M fork 4, 15.954 against 15.815–17.270), `git` 69–100 % (Timsort asciiMasking@1M fork 3, 71.446 against 64.257–72.084), `apolloH…` 81 %, `falcon…` 62 %, `unison` 56 % |
| step 3 pinyin-rank-serial | 02:55:42–03:21:26 | 26 | 2.26 / 1.53 / 1.50 | 390 → 390 | 100 / 131 | `ssm-doc…` 475 % once (`systemSortPinyin`@1M fork 1, 3249.939 against 2962.627–3323.684), `git` 100 % (`systemSortPinyin`@32k fork 1, 72.668 against 72.466–73.631), `aws` 94 %, `falcon…` 62–67 % |
| step 4 strings-cleanup | 03:23:26–04:19:18 | 56 | 16.64 / 7.11 / 6.26 | 390 → 390 | 445 / 1375 | `unison` 78 %, `yum` 53 %, `falcon…` 50 % |
| step 5, attempt 1 | 04:22:18–13:30:45 | — | — | — | — | no data (patch reboot) |
| full suite, Date | 14:51:56–15:07:08 | 16 | 2.01 / 1.29 / 1.31 | 0 → 0 | 100 / 125 | `git` 88 %, `falcon…` 81 % |
| full suite, ParallelRadix | 15:09:09–15:53:43 | 44 | 15.93 / 3.01 / 1.62 | 0 → 0 | 119 / 1512 | `snape` 106 % (`quickHuskySort`@2M fork 3, start interpolated, 1150.593 against 1122.038–1148.490), `unison` 65 %, `MainThread` 53 %, `falcon…` 50 % |
| full suite, Tuple | 15:56:44–16:42:51 | 46 | 1.77 / 1.23 / 1.19 | 0 → 0 | 100 / 312 | none |
| full suite, Permit | 16:44:51–18:32:26 | 107 | 11.01 / 2.07 / 1.42 | 0 → 0 | 100 / 1131 | `MainThread` 144 % (`systemSort`@198,900 fork 3, start interpolated, 161.969 against 153.739–169.489), `unison` 100 % (`systemSort`@32k fork 2, start interpolated, 14.424 against 14.001–14.517), one more sample each ≥ 50 %, `yum` 69 % |
| full suite, ParallelString | 18:35:27–22:28:44 | 233 | 15.52 / 2.94 / 1.64 | 0 → 0 | 131 / 1259 | `unison` 7 samples up to 100 %, `yum` 94 %, `apollo…` 88 %, `apolloH…` 56 %, `falcon…` 50 % |
| full suite, Numeric | 22:31:45–Sun 03:45:14 | 313 | 2.19 / 1.19 / 1.15 | 0 → 0 | 100 / 281 | `unison` 7 samples up to 100 %, `MainThread` 125 %, `yum` 100 %, `apolloH…` 81 %, `ld-linu…` 75 %, `snape` 56 %, `falcon…` 50 % |
| full suite, Adversarial | Sun 03:47:15–09:41:06 | 352 | 2.97 / 1.29 / 1.20 | 0 → 0 | 100 / 400 | `unison` 4 samples up to 100 % (the one at 100 % in `collapsedBitsDualPivotQuicksort` fixedHighBits 16 @1M fork 1, start interpolated, 319.795 against 318.787–339.517), `MainThread` 106 %, `falcon…` 81 %, `systemd` 56 % |
| full suite, String | Sun 09:43:06–19:18:52 | 574 | 14.83 / 1.58 / 1.16 | 0 → 0 | 100 / 1300 | `MainThread` 112 % with `metrics…` 106 % (english `huskyEncodeOnly`@1M fork 2, 178.484 against 176.490–180.815), `yum` 100 %, `python3…` 100 %, `apolloH…` 88 %, `falcon…` 75 %, `snape` 50 % |

Load1 in steps 2 and 4 is the parallel benchmarks themselves (the benchmark JVM reached 1294 % and 1375 %). Swap did not
move. **The only interference that shows in the data is the step-1 build**, over `timsortCleanup` asciiSaturating@1M
fork 3 (65.549 against 60.586–63.005 for the other forks) and asciiMasking@1M forks 1 and 4, of which **fork 4 is
inflated** (74.371 against 63.935–66.973); the ascii-pair ratio they form is 1.07× by score and 1.04× by medians, inside
your band either way. **In the full suite nothing shows in the data**: all 17 of its non-benchmark samples at ≥ 100 % of
a core fell inside a fork, and where the hit fork is a row's slowest it exceeds the next by less than 1 % (appendix).
`unison` is our file sync reacting to the results directory.

## Methodology and disclosures

1. **Two-methods rule**: not met in step C, by your command (four methods, the baseline among them); every 11d ratio
   against `systemSortParallel` comes from that invocation, and the 10-method full suite reproduces its shared rows
   within 0.928–1.019. 11c and steps A and B are internal comparisons, as you classified them. The full suite's ratios
   against `Arrays.sort` and `Arrays.parallelSort` come from its per-class invocations (ParallelString 10 methods,
   Permit 14, String 21), and none replaces a figure of record from steps 1–4.
2. **Machine of record**: Graviton aarch64, 16 cores, one thread per core, 15 pool workers; your Mac has eight cores and
   seven pool workers. Our serial rows take 0.76–2.96× your time and `Arrays.parallelSort` 0.48–0.54×. The bottom of the
   serial range is step A's chineseUnicode@200k, the one cell where our serial arm is the faster; the top is the masking
   adaptive rows. Two predicted ratios moved with the host: 11c's adaptive ratio through the cost per inversion
   (E11c-2), and the step-A gains, of whose six Javadoc cells three stay within the 2.14× that 15 against 7 pool workers
   allow and three exceed it, unexplained (E11d-A2). Your figures are best of four or five; ours are means over 50
   iterations, which by construction sit at or above a best-of-N.
3. **Your figures** are typed from your request text, and the step-A serial / parallel / speed-up column from the
   `ParallelRadixHuskySort` Javadoc at `d947e77`. StringState's coders (english `englishSaturatingCoder`, chinese
   `UNICODE_CODER`, chinesenames `chineseEncoderPinyin`, confirmed by I4) map step C's rows onto step A's cells for the
   phase sum.
4. **Derived figures** (fork-extreme ranges, medians, steady levels, Spearman ρ, costs per element and per inversion,
   the warm-up-1 ratio, the 11c / step-A cross-check) come from `rawData` and the logs via `runner/req11cd-tables.py`;
   the § rows' iterations 6–10 ratios, the per-arm comparison and gain ratios against your Javadoc table, the fit of the
   adaptive rows and the net saturation cost were computed for this doc from the same JSONs with the same quantize rule.
5. **The full suite** was not requested in 11c or 11d. We offer it as the full re-run you said would follow them, since
   `9bb0385` superseded every english and chinese row, and we are happy to run it again on request, at a later commit or
   as a single invocation.
6. **Convergence** (t per fork from `rawData`). In steps 1–4, 12 of the 81 rows are ‡: 9 of the 16
   `parallelTimsortCleanup` rows at the 1 s iterations your step-A command uses, and step C's pAll english@200k and @1M
   and pAll_parCleanup english@1M at 2 s; no serial row is. Per-row mean t spans 0.955–1.454 in step A and 0.834–1.915
   in step C. A flagged fork counts as warm-up when median(iterations 1–5) ÷ median(6–10) > 1.10, else as a spike:
   warm-up in every flagged fork for 8 rows, spikes only for parallel asciiSaturating@1M, and mixed for parallel
   englishSaturating@1M, parallel chineseUnicode@1M and pAll_parCleanup english@1M. Parallel englishMasking@1M has one
   slow fork (fork means 18.524 / 18.402 / 18.569 / 18.844 / 42.397), so its speed-up spans 1.41–3.62×. 5 rows of steps
   1–4 and 13 of the full suite have a fork with t < 0.90; only the two § rows have all five.
7. **The mojibake is left in the corpus**, as you decided; the H4 witness is a mojibake pair, and so is finding 3's.

**What is not claimed.** Not a break-even point for the parallel cleanup (it lost in one end-to-end cell at n = 32,000,
english, ‡ rows, and nothing between 32,000 and 200,000 was measured). Not a reason for your chineseUnicode@200k loss,
nor for the three step-A cells (pinyin@1M and both 200,000 cells) whose gain exceeds yours by more than 15 against 7
pool workers allow. Not an account of Timsort's masking penalty beyond what `N log r` predicts. Not a net whole-sort
verdict on the saturating coder: its encode figure (full suite) and cleanup figure (11c) come from different
invocations, no benchmark sorts english end to end with the masking coder, and its cost inside the parallel sorter,
which encodes in parallel, was not measured. Not a cause for the § rows' slow-down, for the phase-sum misses on english
and chinese, or for the `pinyinRank` coder's dearer comparisons at 200,000. Not "the saturating coder is monotonic", and
not that the length-only `perfect` flag has produced a wrong sort in any benchmark row. In the full suite: not a verdict
on english@1M pAll against `Arrays.parallelSort` beyond the steady-level 1.24×; not any ratio between two classes; not a
cause for the permits class shift, for the four Adversarial rows at 1,000,000 that moved 1.10×–1.15×
(`collapsedBitsDualPivotQuicksort` at fixedHighBits 0 / 32 / 48, `collapsedBitsQuickHuskySort` at 0), or for the
chinesenames encode-only movements.

## Files

All under `doc/`, unedited JMH `-rff` output from the jar above.

| file | sha256 | invocation |
|---|---|---|
| `req11c-cleanup-coders.json` | `8f2aaddc4e638486af0d2f4eec384c7bd8a590e5909509f172033a93f4fd2434` | step 1, 11c (Sat 01:20 PT), 20 rows |
| `req11d-cleanup-parallel.json` | `1aa235253c97fae548e9770abbd3e51e39d9abd56735d823d37a65a358bdf47c` | step 2, 11d A, 32 rows |
| `req11d-pinyin-rank-serial.json` | `a8e730b4a160d2b7042b33768f89af1f4d590d9ef08950aeb2359c19079834f9` | step 3, 11d B, 9 rows, `-r 2s -w 2s` |
| `req11d-strings-cleanup.json` | `6b464fb819daccce833341e85906f9581f197008df61ae8f303bc299ae352752` | step 4, 11d C, 20 rows (4 absent by design), `-r 2s -w 2s` |
| `req11cd-full-DateSortBenchmarks.json` | `4585f75d571e27999546e279878bc4f487b5ac5bfd152c51d7ad6f279913c473` | full suite (Sat 14:51 PT), 6 rows |
| `req11cd-full-ParallelRadixSortBenchmarks.json` | `69600524eebc65584c1071659866bf0ff921334486e710cbaff1125fb6bf9e2a` | full suite, 14 rows |
| `req11cd-full-TupleSortBenchmarks.json` | `d6bfd0283087d385694b694cb3d512f53c8a6f40f500256dee1c6d1a6c002981` | full suite, 18 rows |
| `req11cd-full-PermitSortBenchmarks.json` | `89ae40ae59f10568536ba177bb682b08ed1f243ca4cb3c007372979da30a36f3` | full suite, 42 rows |
| `req11cd-full-ParallelStringSortBenchmarks.json` | `6179d5dfce6f35f0027c3c671f0ed80aea3fe7a3eaf4c4874e93bfde6a0ef245` | full suite, 84 rows (6 absent by design) |
| `req11cd-full-NumericSortBenchmarks.json` | `3af7cae50870d012a70d0a82443dc189780d12c921d24a2bc33f27da0a677b9a` | full suite, 123 rows |
| `req11cd-full-AdversarialSortBenchmarks.json` | `b947ced4d7c318a92a8ee0611c6f617beb5e4eba62ede189eb5dd8fe49118575` | full suite, 124 rows |
| `req11cd-full-StringSortBenchmarks.json` | `7de92e400cf719a76654ee462f0e4eaf851348fc9ff53ec15b75723328abf113` | full suite (→ Sun 19:18 PT), 171 rows (18 absent by design) |
| `req11cd-env-before.txt` | `467d2d1b54fc89766c55d37b19cfaba8e5cb0b4280dd78310f4bb64f193e6fad` | steps 1–4 unit: `date`, `uptime`, `lscpu`, `free -h`, `uname -r`, `java -version`, `mvn -v`, cgroup, `nproc`, git HEAD, src freeze, jar sha, idle-guard thresholds |
| `req11cd-full-env-before.txt` | `391b603f28c04acd6c1489e57f3ed7cb45825d81095ed5f6e40c8950e9fa2fb2` | the full-suite unit, same fields (kernel `6.12.103-129.197`) |

Plus this file and its appendix (every full-suite row, the ‡ and § lists, the unchanged-input rows that moved ≥ 10 %,
the fork-placement note). The jar (sha256 `7d5e0bff…138295`) is not committed; the runner scripts, logs, `harness.log`,
`load-monitor.log`, the probe and attempt 1's logs are outside the repo, available on request.

## Reproduction — the twelve invocations, from the package root with the pinned JDK, each its own `java -jar`

```
java -jar target/benchmarks.jar "CleanupPassBenchmarks.(timsortCleanup|adaptiveInsertionCleanup)$" -p coder=englishSaturating,englishMasking,asciiSaturating,asciiMasking,unicode -f 5 -wi 5 -i 10 -rf json -rff cleanup-coders.json
java -jar target/benchmarks.jar "CleanupPassBenchmarks.(timsortCleanup|parallelTimsortCleanup)$" -f 5 -wi 5 -i 10 -rf json -rff cleanup-parallel.json
java -jar target/benchmarks.jar "StringSortBenchmarks.(radixHuskySortAuto|radixHuskySortAutoPinyinRank|systemSortPinyin)$" -p corpus=chinesenames -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff pinyin-rank-serial.json
java -jar target/benchmarks.jar "ParallelStringSortBenchmarks.(systemSortParallel|parallelRadixHuskySortAuto_pAll|parallelRadixHuskySortAuto_pAll_parCleanup|parallelRadixHuskySortAuto_pAll_pinyinRank)$" -p n=200000,1000000 -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff strings-cleanup.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.DateSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-DateSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.ParallelRadixSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-ParallelRadixSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.TupleSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-TupleSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.PermitSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-PermitSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.ParallelStringSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-ParallelStringSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.NumericSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-NumericSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.AdversarialSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-AdversarialSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.StringSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-StringSortBenchmarks.json
```

Commands 1–4, in the order listed, are steps 1–4 and commands 5–12 the full suite in the order run (arguments from
`run.log`'s `STEP … START` lines). Attempt 1 ran `java -jar target/benchmarks.jar -e "CleanupPassBenchmarks" -r 2s -w 2s
-f 5 -wi 5 -i 10 -rf json -rff full-suite.json`, whose rows the eight class regexes list exactly (`-l`). Run each as a
detached unit outside any CPU-quota'd cgroup, `uptime` before and after, having checked that `jshell -s -` prints
`ForkJoinPool.commonPool().getParallelism() + " " + availableProcessors()` = `15 16` in that unit (a quota'd slice
reports `13 14` and corrupts every parallel row). `-l` with each regex lists exactly the rows in
`req11cd/expected-rows.md`: step B's `StringSortBenchmarks.` cannot match a `ParallelStringSortBenchmarks` method, step
C's `$` keeps `_pAll` from matching its two suffixed variants, and the `^…\.` anchors keep each full-suite regex to its
own class.
