# Run results from Yunlu: request 12, option B (the full suite), 2026-10-05

**english got faster with the masking coder, by 17.6 % / 21.1 % at 200,000 / 1,000,000 (`radixHuskySortAuto`), not by
the 26.9 % / 27.7 % you predicted. At 32,000 both runs are ‡ (still warming up): 10.7 % faster by score, 7.5 % on
iterations 6–10.**

**The `perfect` character check (`f43ff52`) does not measure free. In a same-host A/B of a rebuild of the 11cd jar
(`d947e77`) and this one, the masking encode is 11–15 % slower on english (10–11 % between the two full suites);
`f43ff52` is the only change to that encode loop between the jars, and a patch to the loop alone recovers 66–84 % of the
loss. If the encode inside the sort pays the same penalty, which we did not measure, it accounts for 35.4 % / 53.8 % of
the shortfall above at 200,000 / 1,000,000.**

**chinesenames reproduces 11cd under the new names (0.983–1.036 over the 12 renamed cells), and the six classes whose
coders did not change held. chinese held within 0.90–1.10, but 58 of its 66 husky rows are slower than in 11cd (median
1.018), in the direction of its slower encode.**

We ran option B: one jar built at `d8958bf`, the eight per-class invocations of the 11cd full suite with
`-r 2s -w 2s -f 5 -wi 5 -i 10`, from Sat 2026-10-03 19:39 PDT to Sun 2026-10-04 23:56 PDT, 28 h 02 min of JMH time
(11cd: 28 h 09 min). Every row is in `Run results from Yunlu 2026-10-05 - appendix.md`; the raw JSONs are committed
alongside (Files). Afterwards we ran one supplement you did not ask for, a 78-minute A/B of four encode and sort methods
at two n on a rebuild of the 11cd jar and on this jar, and once on a patched jar of ours (Mon 2026-10-05 00:16–01:34
PDT), because the masking encode-only row had moved on code you expected to be timing-neutral.

**Marks.** † = the two 99.9 % CIs overlap. ‡ = some fork was still speeding up inside its measurement window (t = mean
of iterations 1–5 ÷ mean of 6–10 > 1.10), so the score is usually above steady state (the two exceptions are in the
appendix's ‡ list, with steady ÷ score above 1); "steady" is the mean of iterations 6–10 over all five forks. § = every
fork slowed down (t < 0.90 in all five), so the score is below its iterations 6–10 level. **11cd** is the full suite of
2026-09-27, on the `d947e77` jar (`doc/req11cd-full-<Class>.json`); its values are printed without ±, so every `a ± b`
in this document is a request-12 value from a JSON listed under Files. "coder s→m" marks an english row whose coder
changed from saturating to masking, "coder o→r" a chinesenames row whose coder changed from ordinal to rank, "same" a
row whose coder did not change (its ratio is drift plus any effect of the other code changes, "Checkout and build").
Every row whose coder is `englishCoder`, `englishSaturatingCoder` or `UNICODE_CODER` also runs the encode loop that
`f43ff52` changed: the english and chinese husky rows of the two string classes, the `huskyEncodeOnlyEnglishMasking` and
`huskyEncodeOnlyEnglishSaturating` rows on every corpus, and Adversarial's `sharedPrefix*` husky rows, which (e) labels
"englishCoder, encode loop changed by `f43ff52`". Ratios come from unrounded scores. Times are PT (PDT, UTC−7), with UTC
in brackets where we cite a log.

## What you can take from this

- **english got faster, by 17.6 % / 21.1 % at 200,000 / 1,000,000, not by about a quarter (section (a)).**
  `radixHuskySortAuto` went from 4.556 / 62.146 / 279.839 ms in 11cd to 4.069 ± 0.174 ‡ / 51.224 ± 0.968 / 220.857 ± 2.819 at
  32,000 / 200,000 / 1,000,000, against your 3.35 / 45.43 / 202.44, so the saving is 40.6 % / 65.3 % / 76.2 % of the
  encode saving your prediction subtracts. At 32,000 both runs are ‡: 10.7 % faster by score, 7.5 % on iterations 6–10
  (3.863 against 4.177). 57 of the 60 english husky rows got faster (median 0.828 of 11cd), and the 18 english rows that
  use no coder held (median 1.003, none outside 0.90–1.10).
- **The chinesenames rename is the right way round and reproduces 11cd (section (b)).** The 12 renamed cells read
  0.983–1.036 of the 11cd rows they replace (median 1.001). Spliced by name, they would read 0.06–16.40×.
- **The other 54 chinesenames husky rows changed coder too, so they are new measurements of the rank coder, not
  relabels.** They read 0.07–0.85× of their 11cd ordinal-coder values; `radixHuskySort16`@1,000,000, for example, is
  172.075 ± 1.719 against 772.406. Your table had no row for them.
- **chinese and the no-coder rows held, with a small shift on chinese (section (d)).** The 66 chinese husky rows read
  0.973–1.112 of 11cd, one of them outside 0.90–1.10, and your six figures reproduce within 4.2 %. But 58 of the 66 are
  slower (median 1.018; 23 with disjoint CIs, 22 of those slower), while the 15 chinese rows with no coder sit at a
  median of 0.998. The shift is in the direction of the slower chinese encode (last item) and about its size, which is
  an inference: the encode's 11cd → req12 difference alone would add 1.3–1.9 % to `radixHuskySortAuto`.
- **The six other classes held (section (e)).** 326 of their 327 rows are within 0.90–1.10 of 11cd (median 1.002). The
  one outside is `collapsedBitsQuickHuskySort` at fixedHighBits 0, n = 1,000,000: 0.882× an 11cd value that had itself
  moved 1.13× from request 11. Against request 11 it reads 0.994×. Adversarial's `sharedPrefix*` husky rows name
  `englishCoder`, so their coder did not change, but they run the encode loop `f43ff52` changed: at prefixLength 0 three
  of the four husky rows moved in the A/B's direction (the three radix rows, 1.013–1.045 of 11cd, no larger than the
  A/B's encode difference); `sharedPrefixQuickHuskySort`, which runs the same encode (`englishCoder.huskyEncode` via
  `QuickHuskySort.java:66`) on the same array, did not (−0.26 / +2.76 ms at 200,000 / 1,000,000 against +2.3 to 2.5 /
  +10.9 to 14.0 ms expected from the A/B), so these rows neither establish nor exclude the penalty in a whole sort.
- **Finding for you: the `perfect` character check (`f43ff52`) does not measure free.** In a same-host, interleaved A/B
  of the `d947e77` and `d8958bf` jars, the masking english encode is 11–15 % slower: new ÷ old is 1.127 / 1.132 at
  200,000 and 1.111 / 1.146 at 1,000,000 over two rounds, and every new fork is slower than every old fork (between the
  two full suites it is 10–11 %). `f43ff52` is the only change to that encode loop between the jars; the other commits
  change code the loop does not run, or comments (listed under "The encode A/B"). The check itself runs on 3 elements
  (13 characters) and then stops, so the cost is not work the check does. PrintInlining shows that C2 no longer inlines
  the coder's `exactlyEncodable` override at that call site (`low call site frequency`), so a call stays in the loop
  body, and a patch that stops testing after the first failure, so that the rest of the array is encoded by a loop with
  no call in it (the diff under "The encode A/B", on a patched jar that is not your code), recovers 70–84 % of the loss
  at 200,000 and 66–80 % at 1,000,000. Why the call costs this much we have not shown, and four facts do not fit a fixed
  per-element cost of that call site: the saturating coder keeps the same call and pays 3.2–4.1 ns per element at
  1,000,000 (fork ranges overlap) against masking's 10.9–14.0; masking at 32,000 did not move in the full suite
  (1.007×†; the A/B did not run 32,000); the patch recovers only part of the loss; and the chinese slowdown (next item)
  is flat in n while masking's is not. If the encode inside the sort pays the same penalty as the encode-only row, which
  we did not measure, the masking encode slowdown is 35.4 % / 53.8 % of the english shortfall at 200,000 / 1,000,000.
- **The chinese `huskyEncodeOnly` row (`UNICODE_CODER`) is slower too, for a reason we have not found.** It is 6.5–7.3 %
  slower in the full suite. The A/B establishes 5.7–6.3 % at 1,000,000 only; at 200,000 the fork ranges are disjoint in
  round 1 and overlap by 0.009 ms in round 2. The patch does not recover it. Rebuilding `d8958bf` with only
  `BaseHuskySequenceCoder.java:50` reverted and A/B-testing that jar against `d947e77` on english and chinese would
  settle both attributions.

## Build, host and checks

**Checkout and build**: `39ef74f`, the tip of `parallel-redesign` at our fetch; the last commit touching `src/` is
**`d8958bf`** ("Revert item 37: masking is the default english coder again"), and `git log d8958bf..39ef74f -- src/` is
empty. From `d947e77`, the 11cd jar, 12 commits change 49 files under `src/` (1,584 insertions, 322 deletions), 25 of
them in `src/main` (441 / 148) and 4 in `src/jmh` (132 / 58). The three that change what the string rows measure are
`f43ff52` (the `perfect` check), `9835a3d` (the rank coder as the chinesenames default, and the two renames) and
`d8958bf` (masking as the english default). The other `src/main` changes (among them `AbstractHuskySort`,
`QuickHuskySort`, `MSDStringSort` and `HuskyCoderFactory`) are ones you expected to be timing-neutral, so a "same" ratio
below is drift plus any effect they have. Corretto **21.0.12+8-LTS**, Maven **3.9.16**, JMH **1.37**. We ran
`mvn -B clean` first, because `9835a3d` renames two `@Benchmark` methods and an incremental build would keep their old
generated classes in the jar, then `mvn -B -Pjmh package -DskipTests` and `mvn -B test`: **465 tests, 0 failures, 0
errors, 0 skipped** (`req12/mvn-test.log`). We did not run `-Pintegration-test`; your `9835a3d` message reports
integration 487 / 0. `target/benchmarks.jar` is **76,380,538 B**, sha256
`0f9e5d4a4e57b7c881c936fc1f95cc02bc0904b5f7e87235aada6cf702678829`, built Sat 19:14:00 (02:14:00Z) from a clean
worktree, and was byte-identical after the test run and at every re-hash (unit start and before each class).
`VM options: <none>`; Cnt = 50 per row; ± is the 99.9 % CI half-width; one JSON per class, unedited.

**Correctness before timing**: the external `ValidateSorts` harness passed **1432 / 1432 checks** against this jar, Sat
19:24:18–19:29:56 (02:24:18Z–02:29:56Z), in the quota-free `app.slice/kiroom.service` (`req12/harness.log`). What we
changed in it and what we added is under "The harness, and what else changed since our jar".

**Environment**: aarch64 Graviton (ARM Model 1, r1p1), **16 CPUs, 1 thread per core**, L1d / L1i 1 MiB, L2 16 MiB, L3 32
MiB, 30 Gi RAM (26 Gi available at launch), swap 39 Gi with 0 B used at launch and 0.0 Ki at the end, kernel
`6.12.110-135.202.amzn2023.aarch64` (11cd ran on `6.12.103-129.197`). One detached unit,
`husky-req12-full-20261004-0237.service` in `app.slice`, never under the CPU-quota'd `kiro.slice`. **The `ForkJoinPool`
probe printed `15 16`** before the first class and after the last: 15 common-pool workers for `Arrays.parallelSort` and
the parallel cleanup, `availableProcessors()` = 16 for pAll. Before every class an idle guard (load1 < 2, swap < 1 GiB,
no other benchmark JVM, fewer than 3 active `MainThread` processes) ran after a 120 s gap. Every guard passed on its
first 60 s poll except Tuple's, which waited one poll (load1 2.01, then 0.80) for the tail of ParallelRadix's last rows
on 16 cores; ParallelString and Numeric started at load1 1.93 and 1.96, the same kind of tail.

| class | attempted | JSON rows | absent | `<failure>` | JMH total time | 11cd JMH total | START (PT) | START (UTC) | END (PT) | END (UTC) | load1 before / after |
|---|---:|---:|---:|---:|---:|---:|---|---|---|---|---|
| Date | 6 | 6 | 0 | 0 | 00:15:11 | 00:15:11 | Sat 10-03 19:39:06 PDT | 10-04 02:39:06 | Sat 10-03 19:54:17 PDT | 10-04 02:54:17 | 0.08 / 1.20 |
| ParallelRadix | 14 | 14 | 0 | 0 | 00:44:20 | 00:44:33 | Sat 10-03 19:56:18 PDT | 10-04 02:56:18 | Sat 10-03 20:40:39 PDT | 10-04 03:40:39 | 0.23 / 13.92 |
| Tuple | 18 | 18 | 0 | 0 | 00:46:03 | 00:46:07 | Sat 10-03 20:43:39 PDT | 10-04 03:43:39 | Sat 10-03 21:29:43 PDT | 10-04 04:29:43 | 0.80 / 1.13 |
| Permit | 42 | 42 | 0 | 0 | 01:47:36 | 01:47:34 | Sat 10-03 21:31:44 PDT | 10-04 04:31:44 | Sat 10-03 23:19:20 PDT | 10-04 06:19:20 | 0.26 / 13.26 |
| ParallelString | 90 | 84 | 6 | 30 | 03:50:04 | 03:53:16 | Sat 10-03 23:21:21 PDT | 10-04 06:21:21 | Sun 10-04 03:11:25 PDT | 10-04 10:11:25 | 1.93 / 14.57 |
| Numeric | 123 | 123 | 0 | 0 | 05:13:38 | 05:13:29 | Sun 10-04 03:13:26 PDT | 10-04 10:13:26 | Sun 10-04 08:27:05 PDT | 10-04 15:27:05 | 1.96 / 1.30 |
| Adversarial | 124 | 124 | 0 | 0 | 05:53:35 | 05:53:50 | Sun 10-04 08:29:05 PDT | 10-04 15:29:05 | Sun 10-04 14:22:41 PDT | 10-04 21:22:41 | 0.21 / 1.84 |
| String | 189 | 171 | 18 | 90 | 09:31:51 | 09:35:45 | Sun 10-04 14:24:41 PDT | 10-04 21:24:41 | Sun 10-04 23:56:33 PDT | 10-05 06:56:33 | 0.47 / 2.74 |
| **total** | 606 | 582 | 24 | 120 | 28 h 02 min | 28 h 09 min | | | | | |

Unit window: RUN BEGIN Sat 10-03 19:37:04 PDT (2026-10-04 02:37:04 UTC) → last STEP END Sun 10-04 23:56:33 PDT
(2026-10-05 06:56:33 UTC) = 28 h 19 min; JMH time 28 h 02 min (11cd 28 h 09 min, req12 ÷ 11cd 0.996).

The quiet-host evidence, from `load-monitor.log` (a 60 s `top -b -n 1` plus `free -m` sample inside each class's
window):

| class | samples | load1 max (at, PT) / mean / median | swap used MiB first → max → last | bench java %CPU median / max | non-benchmark samples ≥ 50 % of a core (process × count, max %) |
|---|---:|---|---|---|---|
| Date | 16 | 2.36 (19:43) / 1.33 / 1.31 | 0 → 0 → 0 | 100 / 119 | `unison` × 3 (max 100 %) |
| ParallelRadix | 44 | 14.83 (20:38) / 3.19 / 1.98 | 0 → 0 → 0 | 107 / 1431 | `unison` × 10 (max 100 %), `MainThr+` × 4 (max 127 %), `falcon-+` × 1 (max 81 %), `systemd` × 1 (max 56 %) |
| Tuple | 46 | 1.70 (20:47) / 1.37 / 1.32 | 0 → 0 → 0 | 100 / 200 | `unison` × 5 (max 100 %), `MainThr+` × 1 (max 112 %), `aim` × 1 (max 50 %), `falcon-+` × 1 (max 62 %) |
| Permit | 107 | 11.64 (23:18) / 2.28 / 1.54 | 0 → 0 → 0 | 100 / 1138 | `unison` × 16 (max 100 %), `MainThr+` × 1 (max 93 %) |
| ParallelString | 230 | 14.57 (03:11) / 3.14 / 2.07 | 0 → 0 → 0 | 156 / 1350 | `unison` × 35 (max 100 %), `MainThr+` × 5 (max 175 %), `metrics+` × 2 (max 94 %), `falcon-+` × 1 (max 56 %), `ld-linu+` × 1 (max 53 %), `snape` × 1 (max 62 %), `yum` × 1 (max 100 %) |
| Numeric | 312 | 2.20 (03:45) / 1.36 / 1.32 | 0 → 0 → 0 | 100 / 225 | `unison` × 66 (max 100 %), `MainThr+` × 4 (max 125 %), `aim` × 1 (max 50 %), `falcon-+` × 1 (max 56 %), `snape` × 1 (max 50 %) |
| Adversarial | 353 | 3.13 (12:50) / 1.46 / 1.39 | 0 → 0 → 0 | 100 / 394 | `unison` × 55 (max 100 %), `MainThr+` × 3 (max 131 %), `yum` × 3 (max 100 %), `falcon-+` × 2 (max 87 %), `ld-linu+` × 1 (max 100 %), `node_re+` × 1 (max 119 %), `python3+` × 1 (max 100 %), `snape` × 1 (max 62 %) |
| String | 570 | 14.95 (23:36) / 1.69 / 1.22 | 0 → 0 → 0 | 100 / 1356 | `unison` × 14 (max 100 %), `MainThr+` × 6 (max 125 %), `apolloH+` × 2 (max 81 %), `ld-linu+` × 2 (max 100 %), `snape` × 2 (max 62 %), `yum` × 2 (max 100 %), `aws` × 1 (max 94 %), `falcon-+` × 1 (max 60 %), `python3+` × 1 (max 88 %) |

Swap stayed at 0 MiB in every sample. The high load1 maxima are each class's own parallel rows (the benchmark JVM
reached 1,431 % of a core in ParallelRadix and 1,356 % in String). 117 samples caught a non-benchmark process at 100 %
of a core or more: 89 `unison` (our file sync reacting to the results directory), 20 `MainThread`, 4 `yum`, 2
`ld-linu…`, 1 `python3…` and 1 `node_re…`. Each fell inside a fork, 30 inside the slowest fork of its row, and 5 of
those 30 exceed the row's next-slowest fork by more than 2 %: Permit `radixHuskySort8`@100,000 fork 4 (16.493 against
14.468–15.185) and `radixHuskySort11`@198,900 fork 5 (40.250 against 31.562–37.219), Adversarial
`collapsedBitsDualPivotQuicksort` at fixedHighBits 60, n = 1,000,000, fork 3 (start interpolated), Tuple
`radixHuskySort8`@500,000 fork 2 and ParallelString `serialRadixHuskySortAuto` chinese@200,000 fork 3. None of those
five rows is outside 0.90–1.10 of 11cd. The full list is in the appendix.

**JSON checks** (`runner/validate-json.py --preset req12-full`, outside the repo): all 21 checks pass on each class JSON
(exact (method, params) row set with only the known-throwing rows absent, Cnt = 50, `rawData` 5 × 10, no null or NaN,
`score == mean(rawData)`, `scoreConfidence == score ± scoreError`, forks / warm-up / iterations 5 / 5 / 10, one JVM and
JMH version), and over the union of the eight (`--union`): **582 rows, none in two files, exactly the 24 known-throwing
rows absent**, one jvm / jdk / jmh / forks / iterations tuple. `runner/jmh-json-vs-log.py` matched every row to JMH's
stdout at 3 decimals, **582 / 582**, with 3,030 `# Fork:` lines = 582 × 5 + 120 failed forks. Every result was checked
per class as it landed, then as a union (`req12/DONE`).

**Two-methods rule: not met, and a whole-class run cannot meet it**, as you say. Each class ran as one invocation, with
the `systemSort*` baselines sorting last within their group. Every ratio against `Arrays.sort` or `Arrays.parallelSort`
below comes from such an invocation (String 21 methods, ParallelString 10, Permit 14, ParallelRadix 7).

## (a) english "should get faster by about a quarter"

`StringSortBenchmarks.radixHuskySortAuto` on english, against your figures and your prediction (the encode saving is
`huskyEncodeOnlyEnglishSaturating` − `huskyEncodeOnlyEnglishMasking`):

| n | 11cd | your 11cd figure | your prediction | req12 | req12 ÷ 11cd | prediction ÷ 11cd | req12 ÷ prediction | saving 11cd − req12 (ms) | your encode saving (ms) | saving ÷ your encode saving | req12 encode saving (ms) | saving ÷ req12 encode saving |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 4.556 | 4.56 | 3.35 | 4.069 ± 0.174‡ | 0.89× | 0.74× | 1.21× | 0.487 | 1.20 | 0.41× | 1.247 | 0.39× |
| 200,000 | 62.146 | 62.15 | 45.43 | 51.224 ± 0.968 | 0.82× | 0.73× | 1.13× | 10.921 | 16.72 | 0.65× | 14.076 | 0.78× |
| 1,000,000 | 279.839 | 279.84 | 202.44 | 220.857 ± 2.819 | 0.79× | 0.72× | 1.09× | 58.982 | 77.40 | 0.76× | 72.017 | 0.82× |

**Verdict (a), `radixHuskySortAuto`:** faster at every n: req12 ÷ 11cd 0.893 / 0.824 / 0.789 (10.7 % / 17.6 % / 21.1 %
faster; CIs disjoint at 3 of 3 n) against the predicted 0.735 / 0.731 / 0.723 (26.5 % / 26.9 % / 27.7 %); the saving is
40.6 % / 65.3 % / 76.2 % of your encode saving. Size: smaller than predicted at every n (req12 more than 5 % above the
prediction). The masking encode-only row `huskyEncodeOnlyEnglishMasking`, whose coder and method body did not change,
read 1.007×† / 1.111× / 1.101× of its 11cd value.

At 32,000 both scores are ‡ (fork t up to 1.22 here and 1.23 in 11cd). On iterations 6–10 the ratio is 0.925 (3.863
against 4.177), 7.5 % faster, so the converged gains are the 17.6 % and 21.1 % at 200,000 and 1,000,000.

**Why the prediction missed, as far as we can tell.** Your prediction is the 11cd row minus the 11cd encode saving:
4.556 − 1.202, 62.146 − 16.720 and 279.839 − 77.398, which give your 3.35 / 45.43 / 202.44 to the hundredth. It assumes
that the masking encode inside the sort costs what `huskyEncodeOnlyEnglishMasking` cost in 11cd. In request 12 that row
itself got slower; the A/B below reproduces the slowdown, and we trace it to `f43ff52` by reading the code and by a
patch:

| n | your prediction | req12 `radixHuskySortAuto` | shortfall, req12 − prediction (ms) | `huskyEncodeOnlyEnglishMasking`, 11cd → req12 | its slowdown (ms) | share of the shortfall | A/B new − old, rounds 1 / 2 (ms) | A/B share | not attributed (ms) |
|---:|---:|---:|---:|---|---:|---:|---|---|---|
| 32,000 | 3.35 | 4.069 ± 0.174 ‡ | 0.719 | 1.540 → 1.551 ± 0.022 (1.007×†) | 0.011 | 1.5 % | not run | — | 0.708 |
| 200,000 | 45.43 | 51.224 ± 0.968 | 5.794 | 18.498 → 20.547 ± 0.367 (1.111×) | 2.049 | 35.4 % | 2.312 / 2.451 | 39.9 % / 42.3 % | 3.746 (3.343–3.482 by the A/B) |
| 1,000,000 | 202.44 | 220.857 ± 2.819 | 18.417 | 98.077 → 107.987 ± 1.202 (1.101×) | 9.910 | 53.8 % | 10.932 / 13.953 | 59.4 % / 75.8 % | 8.507 (4.464–7.485 by the A/B) |

What is measured: both `radixHuskySortAuto` rows, both `huskyEncodeOnlyEnglishMasking` rows, and the A/B difference on
the encode-only row. What is inference: that the whole sort pays the same penalty as the encode-only row. Both run the
same loop, `BaseHuskySequenceCoder.huskyEncode(X[])` (`radixHuskySortAuto` reaches it through
`AbstractHuskySort.preSort` → `doCoding` → `HuskyHelper.doCoding`), but we did not A/B-test the whole sort. On that
inference the encode slowdown is 35.4 % of the shortfall at 200,000 and 53.8 % at 1,000,000 by the full-suite rows
(39.9–42.3 % and 59.4–75.8 % by the A/B rounds). **The rest, 3.3–3.7 ms at 200,000 and 4.5–8.5 ms at 1,000,000, we have
not attributed.** Two cross-checks bear on the inference, and neither settles it. First, `huskyEncodeOnly` on english
reads `state.coder`, the same call the sort makes, so its 11cd → req12 difference is the encode saving with `f43ff52`
included, measured in the same two runs as the sort: 0.999 / 14.032 / 68.805 ms at 32,000 / 200,000 / 1,000,000, of
which the sort saved 48.8 % / 77.8 % / 85.7 %; this is consistent with the inference but does not test it. Second,
Adversarial's prefixLength-0 `sharedPrefix*` husky rows name `englishCoder` in both runs, so their coder did not change
but their encode loop did. Three of the four moved in the A/B's direction: the `sharedPrefixRadixHuskySort*` rows are
1.013–1.045 of 11cd, no larger than the A/B's encode-only difference. `sharedPrefixQuickHuskySort`, which runs the same
encode (`englishCoder.huskyEncode` via `QuickHuskySort.java:66`) on the same array, did not: −0.258 / +2.763 ms at
200,000 / 1,000,000 against +2.3 to 2.5 / +10.9 to 14.0 ms expected from the A/B. So these rows neither establish nor
exclude the penalty in a whole sort ((e)). Your figures equal the 11cd row minus the whole encode saving, so they leave
out the cleanup cost your text mentions (11c measured masking's `timsortCleanup` penalty at 3.597 ms per million words,
in another benchmark and invocation); we did not measure the cleanup inside this sort and count nothing for it. At
32,000 the masking encode-only row did not move (1.007×†), and the `radixHuskySortAuto` row is ‡ in both runs, so that
ratio is the least reliable of the three.

Every english row that reads `state.coder` (all "coder s→m"), req12 ± error (11cd, req12 ÷ 11cd):

| class.method | 32,000 | 200,000 | 1,000,000 |
|---|---:|---:|---:|
| String.quickHuskySort | 8.266 ± 0.129 (11cd 9.477, 0.87×) | 104.170 ± 1.599 (11cd 114.408, 0.91×) | 662.778 ± 9.263 (11cd 782.113, 0.85×) |
| String.quickHuskySortPhase2 | 6.933 ± 0.121 (11cd 8.231, 0.84×) | 85.097 ± 1.197 (11cd 101.222, 0.84×) | 603.628 ± 7.450 (11cd 723.808, 0.83×) |
| String.quickHuskySortPhase2CodesOnly | 3.939 ± 0.099 (11cd 5.708, 0.69×) | 36.816 ± 0.359 (11cd 48.780, 0.75×) | 194.844 ± 2.701 (11cd 268.768, 0.72×) |
| String.radixHuskySort8 | 4.343 ± 0.159‡ (11cd 5.150, 0.84×) | 50.817 ± 0.763 (11cd 62.095, 0.82×) | 251.476 ± 2.076 (11cd 312.506, 0.80×) |
| String.radixHuskySort10 | 4.182 ± 0.220‡ (11cd 5.003, 0.84×) | 49.326 ± 0.922 (11cd 62.137, 0.79×) | 241.924 ± 1.855 (11cd 306.793, 0.79×) |
| String.radixHuskySort11 | 3.993 ± 0.200‡ (11cd 4.790, 0.83×) | 48.622 ± 0.700 (11cd 60.273, 0.81×) | 233.518 ± 2.272 (11cd 294.918, 0.79×) |
| String.radixHuskySort12 | 3.911 ± 0.224‡ (11cd 4.951, 0.79×) | 48.892 ± 0.793 (11cd 62.004, 0.79×) | 235.570 ± 2.196 (11cd 300.623, 0.78×) |
| String.radixHuskySort13 | 3.730 ± 0.238‡ (11cd 4.557, 0.82×) | 47.216 ± 1.187‡ (11cd 60.738, 0.78×) | 225.618 ± 3.469 (11cd 292.835, 0.77×) |
| String.radixHuskySort14 | 3.803 ± 0.221‡ (11cd 4.915, 0.77×) | 48.436 ± 1.065 (11cd 61.353, 0.79×) | 226.707 ± 3.751 (11cd 283.562, 0.80×) |
| String.radixHuskySort16 | 3.698 ± 0.241‡ (11cd 4.157, 0.89×†) | 47.462 ± 0.892 (11cd 58.892, 0.81×) | 217.009 ± 2.923 (11cd 277.812, 0.78×) |
| String.radixHuskySortAuto | 4.069 ± 0.174‡ (11cd 4.556, 0.89×) | 51.224 ± 0.968 (11cd 62.146, 0.82×) | 220.857 ± 2.819 (11cd 279.839, 0.79×) |
| String.huskyEncodeOnly | 1.750 ± 0.047 (11cd 2.749, 0.64×) | 21.217 ± 0.333 (11cd 35.249, 0.60×) | 109.132 ± 1.471 (11cd 177.937, 0.61×) |
| ParallelString.serialRadixHuskySortAuto | 4.211 ± 0.141‡ (11cd 4.621, 0.91×) | 51.627 ± 1.409 (11cd 62.000, 0.83×) | 215.345 ± 2.981 (11cd 279.893, 0.77×) |
| ParallelString.parallelRadixHuskySortAuto_p1 | 4.606 ± 0.289 (11cd 4.884, 0.94×†) | 48.828 ± 1.143 (11cd 61.836, 0.79×) | 220.129 ± 5.992 (11cd 282.925, 0.78×) |
| ParallelString.parallelRadixHuskySortAuto_p2 | 4.518 ± 0.258 (11cd 4.943, 0.91×†) | 32.536 ± 1.921‡ (11cd 40.058, 0.81×) | 210.929 ± 28.214‡ (11cd 248.448, 0.85×†) |
| ParallelString.parallelRadixHuskySortAuto_p4 | 4.409 ± 0.216 (11cd 5.018, 0.88×) | 26.543 ± 1.786‡ (11cd 28.045, 0.95×†) | 111.223 ± 12.456‡ (11cd 122.658, 0.91×†) |
| ParallelString.parallelRadixHuskySortAuto_p8 | 4.588 ± 0.362 (11cd 4.988, 0.92×†) | 23.622 ± 1.227‡ (11cd 26.454, 0.89×) | 100.949 ± 16.264‡ (11cd 105.946, 0.95×†) |
| ParallelString.parallelRadixHuskySortAuto_pAll | 4.962 ± 0.423 (11cd 4.947, 1.00×†) | 23.554 ± 1.217‡ (11cd 23.214, 1.01×†) | 97.517 ± 18.127‡ (11cd 104.570, 0.93×†) |
| ParallelString.parallelRadixHuskySortAuto_pAll_parCleanup | 4.285 ± 0.296‡ (11cd 5.477, 0.78×) | 9.527 ± 0.174 (11cd 9.461, 1.01×†) | 56.135 ± 8.227‡ (11cd 59.276, 0.95×†) |
| ParallelString.parallelRadixHuskySort11_p8 | 4.508 ± 0.158‡ (11cd 4.909, 0.92×†) | 24.278 ± 1.371‡ (11cd 24.858, 0.98×†) | 100.962 ± 15.833‡ (11cd 103.801, 0.97×†) |

- String: 36 rows: req12 ÷ 11cd median 0.797 (min 0.602, max 0.911); 36 faster in req12; 35 outside 0.90–1.10; 35 with
  disjoint CIs.
- ParallelString: 24 rows: req12 ÷ 11cd median 0.916 (min 0.769, max 1.015); 21 faster in req12; 9 outside 0.90–1.10; 9
  with disjoint CIs.
- both: 60 rows: req12 ÷ 11cd median 0.828 (min 0.602, max 1.015); 57 faster in req12; 44 outside 0.90–1.10; 44 with
  disjoint CIs.

The parallel rows moved least: pAll at 32,000 and 200,000 and `pAll_parCleanup` at 200,000 did not get faster (1.00×† /
1.01×† / 1.01×†), and most ParallelString rows at 200,000 and 1,000,000 are ‡ in both runs, so their ratios compare two
warm-up-inflated scores (Convergence).

english rows with no husky coder ("same"; you expected them not to move):

| class.method | 32,000 | 200,000 | 1,000,000 |
|---|---:|---:|---:|
| String.systemSort | 14.839 ± 0.304 (11cd 15.096, 0.983×†) | 157.808 ± 2.349 (11cd 156.327, 1.009×†) | 1269.099 ± 31.475 (11cd 1293.911, 0.981×†) |
| String.systemSortParallel | 4.882 ± 0.054 (11cd 4.864, 1.004×†) | 11.700 ± 0.148 (11cd 11.225, 1.042×) | 99.273 ± 9.874 (11cd 91.832, 1.081×†) |
| String.insertionSort | 43.215 ± 0.304 (11cd 42.608, 1.014×†) | 1215.238 ± 3.858 (11cd 1216.232, 0.999×†) | 26901.756 ± 52.197 (11cd 27011.104, 0.996×†) |
| String.multikeyQuicksort | 7.991 ± 0.071 (11cd 7.983, 1.001×†) | 79.973 ± 1.115 (11cd 81.839, 0.977×) | 739.699 ± 11.354 (11cd 747.621, 0.989×†) |
| String.msdStringSort | 4.218 ± 0.144‡ (11cd 4.218, 1.000×†) | 60.084 ± 1.819‡ (11cd 58.782, 1.022×†) | 321.415 ± 19.633‡ (11cd 319.260, 1.007×†) |
| ParallelString.systemSortParallel | 4.858 ± 0.048 (11cd 4.852, 1.001×†) | 11.486 ± 0.184 (11cd 11.414, 1.006×†) | 95.021 ± 2.108 (11cd 93.803, 1.013×†) |

- 18 rows: req12 ÷ 11cd median 1.003 (min 0.977, max 1.081); 7 faster in req12; 0 outside 0.90–1.10; 2 with disjoint
  CIs.

**Verdict (a), all english husky rows:** 57 of 60 faster in req12 (not faster:
ParallelString.parallelRadixHuskySortAuto_pAll 32,000 1.00×†; ParallelString.parallelRadixHuskySortAuto_pAll 200,000
1.01×†; ParallelString.parallelRadixHuskySortAuto_pAll_parCleanup 200,000 1.01×†), median 0.828; the no-coder rows'
median is 1.003, so the husky median relative to them is 0.826. No-coder rows outside 0.90–1.10: 0 of 18.

## (b) chinesenames "should reproduce your 11cd rows exactly, under the new names"

| class | req12 row | 11cd row | coder | n | your figure | 11cd | req12 | req12 ÷ 11cd | req12 ÷ yours | yours = 11cd to 2 dp |
|---|---|---|---|---:|---:|---:|---:|---:|---:|---|
| String | `radixHuskySortAuto` | `radixHuskySortAutoPinyinRank` | rank | 32,000 | 2.84 | 2.839 | 2.852 ± 0.059 | 1.005×† | 1.004× | yes |
| String | `radixHuskySortAuto` | `radixHuskySortAutoPinyinRank` | rank | 200,000 | 35.87 | 35.873 | 35.915 ± 0.306 | 1.001×† | 1.001× | yes |
| String | `radixHuskySortAuto` | `radixHuskySortAutoPinyinRank` | rank | 1,000,000 | 173.34 | 173.344 | 174.039 ± 1.870 | 1.004×† | 1.004× | yes |
| String | `radixHuskySortAutoPinyinOrdinal` | `radixHuskySortAuto` | ordinal | 32,000 | 24.41 | 24.411 | 24.994 ± 0.306 | 1.024× | 1.024× | yes |
| String | `radixHuskySortAutoPinyinOrdinal` | `radixHuskySortAuto` | ordinal | 200,000 | 155.02 | 155.016 | 152.397 ± 1.621 | 0.983× | 0.983× | yes |
| String | `radixHuskySortAutoPinyinOrdinal` | `radixHuskySortAuto` | ordinal | 1,000,000 | 765.83 | 765.833 | 759.740 ± 12.258 | 0.992×† | 0.992× | yes |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `parallelRadixHuskySortAuto_pAll_pinyinRank` | rank | 32,000 | 3.08 | 3.081 | 3.032 ± 0.073 | 0.984×† | 0.984× | yes |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `parallelRadixHuskySortAuto_pAll_pinyinRank` | rank | 200,000 | 8.19 | 8.194 | 8.193 ± 0.048 | 1.000×† | 1.000× | yes |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `parallelRadixHuskySortAuto_pAll_pinyinRank` | rank | 1,000,000 | 33.97 | 33.965 | 35.171 ± 1.071 | 1.036×† | 1.035× | yes |
| ParallelString | `parallelRadixHuskySortAuto_pAll_pinyinOrdinal` | `parallelRadixHuskySortAuto_pAll` | ordinal | 32,000 | 24.53 | 24.528 | 24.528 ± 0.295 | 1.000×† | 1.000× | yes |
| ParallelString | `parallelRadixHuskySortAuto_pAll_pinyinOrdinal` | `parallelRadixHuskySortAuto_pAll` | ordinal | 200,000 | 113.03 | 113.027 | 113.582 ± 1.669 | 1.005×† | 1.005× | yes |
| ParallelString | `parallelRadixHuskySortAuto_pAll_pinyinOrdinal` | `parallelRadixHuskySortAuto_pAll` | ordinal | 1,000,000 | 561.97 | 561.969 | 557.010 ± 11.041 | 0.991×† | 0.991× | yes |

Each of your twelve figures equals the 11cd row to 2 decimals, so the comparison with your figures and with 11cd is the
same comparison.

Compared by name instead (the splice you warn about: default with the 11cd default, `*PinyinOrdinal` with the 11cd
`*PinyinRank`), the same 12 cells read req12 ÷ 11cd 0.063–16.399.

**Verdict (b):** reproduced: the 12 renamed cells give req12 ÷ 11cd 0.983–1.036 (median 1.001), CIs overlap in 10 of 12;
the largest deviation is ParallelString.parallelRadixHuskySortAuto_pAll 1,000,000 at 1.036×† (its 11cd counterpart is
`parallelRadixHuskySortAuto_pAll_pinyinRank`, 33.965). The rename is the right way round: a swapped splice would read
0.06–16.40×.

## (c) The chinesenames rows you did not list: new measurements, not relabels

Your table lists the two renamed pairs. Every other chinesenames row that reads `state.coder` also changed coder, from
ordinal to rank, under an unchanged name, so its 11cd value measured a different coder and the ratio below is dominated
by the coder's effect (it also holds drift and the other code changes, which are small beside these ratios). req12 ± error
(11cd ordinal-coder row, req12 ÷ 11cd):

| class.method | 32,000 | 200,000 | 1,000,000 |
|---|---:|---:|---:|
| String.quickHuskySort | 6.758 ± 0.106 (11cd 28.032, 0.24×) | 71.545 ± 0.621 (11cd 187.152, 0.38×) | 664.876 ± 3.896 (11cd 1279.589, 0.52×) |
| String.quickHuskySortPhase2 | 6.939 ± 0.164 (11cd 9.848, 0.70×) | 71.242 ± 0.611 (11cd 87.183, 0.82×) | 662.305 ± 3.910 (11cd 780.430, 0.85×) |
| String.quickHuskySortPhase2CodesOnly | 4.188 ± 0.183 (11cd 7.926, 0.53×) | 41.195 ± 0.226 (11cd 56.768, 0.73×) | 219.023 ± 0.829 (11cd 295.518, 0.74×) |
| String.radixHuskySort8 | 3.121 ± 0.098 (11cd 25.084, 0.12×) | 37.221 ± 0.185 (11cd 155.380, 0.24×) | 202.561 ± 0.743 (11cd 794.069, 0.26×) |
| String.radixHuskySort10 | 3.023 ± 0.076 (11cd 24.843, 0.12×) | 36.850 ± 0.230 (11cd 156.170, 0.24×) | 199.162 ± 0.651 (11cd 782.905, 0.25×) |
| String.radixHuskySort11 | 2.908 ± 0.064 (11cd 25.183, 0.12×) | 36.003 ± 0.232 (11cd 153.468, 0.23×) | 185.627 ± 0.804 (11cd 774.346, 0.24×) |
| String.radixHuskySort12 | 2.914 ± 0.062 (11cd 24.458, 0.12×) | 36.174 ± 0.255 (11cd 151.992, 0.24×) | 183.646 ± 1.577 (11cd 770.782, 0.24×) |
| String.radixHuskySort13 | 2.839 ± 0.059 (11cd 24.426, 0.12×) | 35.470 ± 0.187 (11cd 153.714, 0.23×) | 180.405 ± 2.860 (11cd 770.096, 0.23×) |
| String.radixHuskySort14 | 2.887 ± 0.062 (11cd 24.625, 0.12×) | 36.115 ± 0.206 (11cd 155.248, 0.23×) | 178.688 ± 2.364 (11cd 778.319, 0.23×) |
| String.radixHuskySort16 | 2.874 ± 0.057 (11cd 24.363, 0.12×) | 34.716 ± 0.247 (11cd 151.756, 0.23×) | 172.075 ± 1.719 (11cd 772.406, 0.22×) |
| String.huskyEncodeOnly | 1.841 ± 0.031 (11cd 5.341, 0.34×) | 27.548 ± 0.190 (11cd 42.753, 0.64×) | 139.133 ± 0.594 (11cd 216.067, 0.64×) |
| ParallelString.serialRadixHuskySortAuto | 2.912 ± 0.062 (11cd 24.598, 0.12×) | 35.616 ± 0.213 (11cd 154.320, 0.23×) | 172.903 ± 1.851 (11cd 761.884, 0.23×) |
| ParallelString.parallelRadixHuskySortAuto_p1 | 3.068 ± 0.073 (11cd 24.759, 0.12×) | 36.375 ± 0.244 (11cd 156.182, 0.23×) | 176.245 ± 1.506 (11cd 770.270, 0.23×) |
| ParallelString.parallelRadixHuskySortAuto_p2 | 2.995 ± 0.099‡ (11cd 24.763, 0.12×) | 20.461 ± 0.106 (11cd 129.057, 0.16×) | 96.179 ± 1.260 (11cd 650.894, 0.15×) |
| ParallelString.parallelRadixHuskySortAuto_p4 | 3.083 ± 0.081 (11cd 24.505, 0.13×) | 12.416 ± 0.116 (11cd 120.046, 0.10×) | 61.294 ± 0.955 (11cd 610.150, 0.10×) |
| ParallelString.parallelRadixHuskySortAuto_p8 | 3.064 ± 0.069 (11cd 24.704, 0.12×) | 8.939 ± 0.061 (11cd 112.282, 0.08×) | 41.482 ± 1.204 (11cd 565.807, 0.07×) |
| ParallelString.parallelRadixHuskySortAuto_pAll_parCleanup | 3.041 ± 0.057 (11cd 15.692, 0.19×) | 8.157 ± 0.047 (11cd 25.015, 0.33×) | 34.606 ± 1.039 (11cd 105.019, 0.33×) |
| ParallelString.parallelRadixHuskySort11_p8 | 3.042 ± 0.075‡ (11cd 24.569, 0.12×) | 8.745 ± 0.054 (11cd 113.316, 0.08×) | 40.361 ± 1.058 (11cd 568.865, 0.07×) |

- String: 33 rows: req12 ÷ 11cd median 0.238 (min 0.115, max 0.849); 33 faster in req12; 33 outside 0.90–1.10; 33 with
  disjoint CIs.
- ParallelString: 21 rows: req12 ÷ 11cd median 0.124 (min 0.071, max 0.330); 21 faster in req12; 21 outside 0.90–1.10;
  21 with disjoint CIs.
- both: 54 rows: req12 ÷ 11cd median 0.230 (min 0.071, max 0.849); 54 faster in req12; 54 outside 0.90–1.10; 54 with
  disjoint CIs.

With the rank coder the cleanup pass does not run (the coder reports perfect), so `pAll_parCleanup` should equal `pAll`;
req12 `pAll_parCleanup` ÷ `pAll`: 32,000 1.003×†, 200,000 0.996×†, 1,000,000 0.984×†.

**Verdict (c):** 54 of 54 rows faster with the rank coder; the sorting rows read 0.07–0.85× of their 11cd ordinal-coder
values (median 0.229); `huskyEncodeOnly` 0.34×, 0.64×, 0.64× (encode cost of rank ÷ ordinal).

For the paper this means the chinesenames rows of every husky method in both string classes are now rank-coder rows. The
ParallelString thread sweep on chinesenames, for instance, now reads 172.903 ± 1.851 (serial) to 41.482 ± 1.204 (`_p8`) and
35.171 ± 1.071 (pAll) at 1,000,000.

## (d) chinese "should not move at all"

`UNICODE_CODER` in both runs, so every chinese row is "same"; the husky rows among them run the encode loop that
`f43ff52` changed.

| class.method | n | your figure | 11cd | req12 | req12 ÷ 11cd | req12 ÷ yours | yours = 11cd to 2 dp |
|---|---:|---:|---:|---:|---:|---:|---|
| String.radixHuskySortAuto | 32,000 | 2.35 | 2.346 | 2.362 ± 0.013 | 1.007×† | 1.005× | yes |
| String.radixHuskySortAuto | 200,000 | 14.85 | 14.853 | 14.456 ± 0.180 | 0.973×† | 0.973× | yes |
| String.radixHuskySortAuto | 1,000,000 | 69.62 | 69.615 | 70.167 ± 0.985 | 1.008×† | 1.008× | yes |
| ParallelString.parallelRadixHuskySortAuto_pAll | 32,000 | 3.16 | 3.159 | 3.233 ± 0.174 | 1.023×† | 1.023× | yes |
| ParallelString.parallelRadixHuskySortAuto_pAll | 200,000 | 10.04 | 10.037 | 10.073 ± 0.083 | 1.004×† | 1.003× | yes |
| ParallelString.parallelRadixHuskySortAuto_pAll | 1,000,000 | 39.95 | 39.953 | 41.619 ± 1.089 | 1.042× | 1.042× | yes |

- String, every chinese husky row (state.coder or a named coder): 42 rows: req12 ÷ 11cd median 1.018 (min 0.973, max
  1.112); 3 faster in req12; 1 outside 0.90–1.10; 18 with disjoint CIs.
- ParallelString, every chinese husky row (state.coder or a named coder): 24 rows: req12 ÷ 11cd median 1.017 (min 0.976,
  max 1.045); 5 faster in req12; 0 outside 0.90–1.10; 5 with disjoint CIs.
- both classes, chinese rows with no coder: 15 rows: req12 ÷ 11cd median 0.998 (min 0.967, max 1.013); 9 faster in
  req12; 0 outside 0.90–1.10; 2 with disjoint CIs.
- chinese encode-only rows: `huskyEncodeOnly` 1.065× / 1.069× / 1.073×; `huskyEncodeOnlyEnglishMasking` 1.039× / 1.038×
  / 1.031×; `huskyEncodeOnlyEnglishSaturating` 1.112× / 1.051× / 1.022×†.

chinese husky rows outside 0.90–1.10:

| class.method | n | 11cd | 11cd error | req12 | req12 ÷ 11cd |
|---|---:|---:|---:|---:|---:|
| String.huskyEncodeOnlyEnglishSaturating | 32,000 | 1.107 | 0.024 | 1.232 ± 0.009 | 1.112× |

**Verdict (d):** the 66 chinese husky rows read req12 ÷ 11cd 0.973–1.112 (median 1.018; the chinese no-coder rows'
median is 0.998), 1 outside 0.90–1.10 and 23 with disjoint CIs; your six figures are reproduced within 4.2 %.

**Within the band, the chinese husky rows shifted.** 58 of the 66 are slower than in 11cd (median 1.018), and 23 have
disjoint CIs, 22 of them slower; without the nine encode-only rows, 49 of the 57 sorting rows are slower (median 1.015).
The 15 chinese rows with no coder sit at a median of 0.998 (6 slower), and the six other classes at 1.002. The shift is
in the direction of the slower chinese encode (next paragraph) and about its size, which is an inference: the
`huskyEncodeOnly` row's 11cd → req12 difference would add 1.3 % / 1.6 % / 1.9 % to `radixHuskySortAuto` at 32,000 /
200,000 / 1,000,000 (that row itself reads 1.007×† / 0.973×† / 1.008×†).

The encode-only rows are the exception to "should not move". `huskyEncodeOnly` (`UNICODE_CODER`) is 1.065× / 1.069× /
1.073× of 11cd with CIs disjoint at every n. The A/B below reproduces the slowdown at 1,000,000 (1.057 / 1.063, fork
ranges disjoint in both rounds). At 200,000 it reads 1.043 / 1.041, with fork ranges disjoint in round 1 and overlapping
by 0.009 ms in round 2, so it is not established there. We have not identified its cause. The sorting rows that contain
that encode moved less: `radixHuskySortAuto` 1.007×† / 0.973×† / 1.008×†.

## (e) "Nothing outside the two string classes is affected"

No coder changed in the other six classes: Adversarial's `sharedPrefix*` rows name `englishCoder`, as you say, and its
`collapsedBits*` rows `longCoder`. The `sharedPrefix*` husky rows (`sharedPrefixQuickHuskySort` and
`sharedPrefixRadixHuskySort8` / `11` / `16`, `AdversarialSortBenchmarks.java:152`, `:159`, `:165`, `:171`) do encode
through `BaseHuskySequenceCoder.huskyEncode(X[])`, though, the loop `f43ff52` changed, so we label them "englishCoder,
encode loop changed by `f43ff52`" here and in the appendix. `longCoder` is not a `BaseHuskySequenceCoder`, so the
`collapsedBits*` rows do not run that loop.

| class | rows | req12 ÷ 11cd median (min–max) | outside 0.90–1.10 | CIs disjoint | `*systemSort*` baselines: median (min–max) |
|---|---:|---|---:|---:|---|
| Date | 6 | 0.991 (0.984–1.013) | 0 | 2 | 0.986 (0.986–0.986), 1 row |
| ParallelRadix | 14 | 1.003 (0.981–1.086) | 0 | 1 | 1.034 (0.981–1.086), 2 rows |
| Tuple | 18 | 1.002 (0.964–1.042) | 0 | 5 | 1.001 (0.993–1.014), 3 rows |
| Permit | 42 | 0.998 (0.922–1.068) | 0 | 5 | 0.997 (0.985–1.031), 6 rows |
| Numeric | 123 | 1.004 (0.969–1.072) | 0 | 31 | 1.006 (0.979–1.022), 15 rows |
| Adversarial | 124 | 1.000 (0.882–1.060) | 1 | 12 | 0.997 (0.949–1.030), 22 rows |
| **all six** | 327 | 1.002 (0.882–1.086) | 1 | 56 | |

Every row outside 0.90–1.10 (11cd value and its 99.9 % half-width in separate columns):

| class.method | params | 11cd | 11cd error | req12 | req12 ÷ 11cd |
|---|---|---:|---:|---:|---:|
| Adversarial.collapsedBitsQuickHuskySort | fixedHighBits=0, n=1,000,000 | 450.849 | 18.489 | 397.773 ± 4.288 | 0.882× |

**Verdict (e):** 326 of 327 rows of the six classes within 0.90–1.10 (median 1.002); 1 outside, 1 of them with disjoint
CIs and none in which either row is ‡.

**The `sharedPrefix*` husky rows.** All are within 0.90–1.10 (0.984–1.045). At prefixLength 0, where the radix rows take
52–288 ms, the three radix rows are all slower than in 11cd: 1.045 / 1.033 / 1.013 at 200,000 (radix 8 / 11 / 16; radix
8's CIs disjoint) and 1.019 / 1.018 / 1.042 at 1,000,000. That is +0.657 to +2.381 ms at 200,000 and +4.789 to +10.700
ms at 1,000,000, against an A/B encode-only difference of 2.312–2.451 and 10.932–13.953 ms at the same n: in its
direction and no larger than it. `sharedPrefixQuickHuskySort` at prefixLength 0 did not move (0.997 / 1.004), and the
no-coder `sharedPrefixSystemSort` read 1.008 / 0.965. At prefixLength 10 to 40 the husky rows take 93–1,058 ms, where
the encode difference would be 1.0–2.6 % and their ratios spread 0.984–1.022, so they cannot show it. These are
cross-invocation ratios, and with `sharedPrefixQuickHuskySort` not moving, they neither establish nor exclude the
penalty in a whole sort ((a)).

The one row outside the band is the 11cd value's doing. In 11cd, `collapsedBitsQuickHuskySort` at fixedHighBits 0, n =
1,000,000 had moved 1.13× from request 11 (09-27 doc, "What changed against request 11"), with the widest relative CI of
the six fixedHighBits-0 cells at that n (18.489 on 450.849, 4.1 %). Its request-12 value, 397.773 ± 4.288, is 0.994×
request 11's (`doc/req11-full-suite.json`).

The permits class, which shifted as a whole between request 11 and 11cd, did not shift again: its rows read 0.922–1.068
of 11cd (median 0.998), `Arrays.sort` 0.997× / 0.994× / 0.997× and `Arrays.parallelSort` 1.009× / 1.031× / 0.985×, all
CIs overlapping.

## The encode A/B: the `perfect` check is not free

**Why we ran it.** In the full suite, `huskyEncodeOnlyEnglishMasking` on english read 1.111× / 1.101× of 11cd at 200,000
/ 1,000,000 with disjoint CIs, although its method body and its coder did not change and the english no-coder rows held.
Encode-only rows have moved 0.86–1.28× between two invocations of one jar before (request 11, a 1 s and a 2 s
invocation), so one run could not tell code from drift. Your `f43ff52` message says "No measurable cost":
`huskyEncode(String[])` "measures within noise of a bare per-element loop". Ours is a different measurement, a same-host
JMH A/B of the `d947e77` and `d8958bf` jars on this Graviton host.

**What changed on the encode path.** The two jars are 7 `src/main` commits apart
(`git log d947e77..d8958bf -- src/main`), so the A/B measures all of them at once. We attribute the difference to
`f43ff52` by reading what each commit does on the path the encode-only rows run: `huskyEncodeOnly*`
(`StringSortBenchmarks.java:194–216`, identical in both jars, as is `radixHuskySortAuto` at `:396–399`) →
`BaseHuskySequenceCoder.huskyEncode(X[])` → the coder's `huskyEncode(String)` in `HuskyCoderFactory`.

- `f43ff52` changes line 50 of that loop and adds the `exactlyEncodable` overrides (next paragraph). It is the only
  change to code the loop runs.
- `4857dcc` deletes `BaseHuskySequenceCoder`'s `perfect()` and `toString()` overrides, which the loop does not call, and
  adds Javadoc to `HuskyCoderFactory`'s encoding helpers; its other files are not on this path.
- `1cbab86` changes only Javadoc in `HuskyCoderFactory`.
- `d5eb646` pads `HuskyCoder.huskyEncode(byte[])`, which `englishCoder`, `englishSaturatingCoder` and `UNICODE_CODER` do
  not use, and changes `GenericCollator`.
- `d82df1f` moves `REGEX_LEIPZIG` (the master arrays have the same sha256 under both jars, below) and moves
  `AbstractHuskySort.doCoding` unchanged within its file; only the sort rows reach `doCoding`.
- `c036f6f` (`Config.getString`) and `9835a3d` (the legacy `HuskySortBenchmark`) are off the path.

The patched jar below changes only the loop and recovers most of the masking loss, which supports the attribution on
english. On chinese it recovers nothing, so there the attribution is open. The line-50 revert under "The chinese encode
row is not explained" would turn both into measurements.

**The change.** Line 50 of `BaseHuskySequenceCoder.huskyEncode(X[])` went from `if (isPerfect) isPerfect =
perfectForLength(x.length());`, a `final` method (`:16–18`), to `if (isPerfect) isPerfect = exactlyEncodable(x);`, a
non-final method (`:81–83`, by default the length test) that four anonymous coders override. The `englishCoder` and
`englishSaturatingCoder` overrides (`HuskyCoderFactory.java:131–132` and `:222–223`) test the length first, then scan
the characters (`charactersWithin`, `BaseHuskySequenceCoder.java:91–97`) and stop at the first one outside 64..127.
`UNICODE_CODER` has no override, so for it the test is still the length alone.

**Design.** Two jars on one host, interleaved: old = `d947e77`, rebuilt in a scratch worktree (`mvn -B clean`, then
`mvn -B -Pjmh package -DskipTests`), and new = this request's jar, the one the suite ran (`0f9e5d4a…678829`). The order
was old-english, old-chinese, new-english, new-chinese, the same four again (round 2), then a patched jar once (round
3). "english" is `huskyEncodeOnlyEnglishMasking` and `huskyEncodeOnlyEnglishSaturating`, "chinese" is `huskyEncodeOnly`
and `radixHuskySortAuto`, each at n = 200,000 and 1,000,000, with `-r 2s -w 2s -f 3 -wi 5 -i 10` (commands under
Reproduction). Each invocation ran exactly those two methods, so your two-methods rule is met for every A/B figure, as
it is nowhere else in this document. Unit `husky-req12-encode-ab-20261005-0716.service` in `app.slice`, Mon 00:16–01:34
(07:16–08:34Z). The idle guard passed on its first poll before all 16 invocations (10 timed, 6 one-fork diagnostics),
load1 stayed within 0.46–2.37 (mean 1.45), swap at 1 MiB, and the probe printed `15 16` before and after. All 16 JSONs
pass our checks (rows, Cnt, `score == mean(rawData)`), and no timed row has a fork with t > 1.10 (largest 1.052).

ms/op, score ± 99.9 % error, 3 forks × 10 × 2 s; the full-suite columns are 5 forks.

| row | old r1 | new r1 | new/old r1 | old r2 | new r2 | new/old r2 | 11cd full | req12 full | full req12/11cd | verdict |
|---|---|---|---|---|---|---|---|---|---|---|
| english huskyEncodeOnlyEnglishMasking n=200000 | 18.261 ± 0.462 | 20.573 ± 0.465 | 1.127 | 18.545 ± 0.369 | 20.997 ± 0.486 | 1.132 | 18.498 | 20.547 ± 0.367 | 1.111 | REPRODUCED (slower) |
| english huskyEncodeOnlyEnglishMasking n=1000000 | 98.256 ± 1.657 | 109.188 ± 1.928 | 1.111 | 95.627 ± 2.305 | 109.580 ± 2.187 | 1.146 | 98.077 | 107.987 ± 1.202 | 1.101 | REPRODUCED (slower) |
| english huskyEncodeOnlyEnglishSaturating n=200000 | 34.401 ± 0.649 | 34.721 ± 0.722 | 1.009 | 34.505 ± 0.833 | 35.696 ± 0.636 | 1.035 | 35.218 | 34.622 ± 0.528 | 0.983 | same direction, ranges overlap |
| english huskyEncodeOnlyEnglishSaturating n=1000000 | 176.578 ± 2.971 | 180.724 ± 3.320 | 1.023 | 175.278 ± 3.677 | 178.452 ± 3.820 | 1.018 | 175.475 | 180.004 ± 2.555 | 1.026 | same direction, ranges overlap |
| chinese huskyEncodeOnly n=200000 | 3.527 ± 0.059 | 3.680 ± 0.057 | 1.043 | 3.522 ± 0.075 | 3.665 ± 0.067 | 1.041 | 3.488 | 3.727 ± 0.046 | 1.069 | same direction, ranges overlap |
| chinese huskyEncodeOnly n=1000000 | 18.618 ± 0.278 | 19.677 ± 0.584 | 1.057 | 18.594 ± 0.280 | 19.764 ± 0.380 | 1.063 | 18.267 | 19.597 ± 0.343 | 1.073 | REPRODUCED (slower) |
| chinese radixHuskySortAuto n=200000 | 14.813 ± 0.290 | 14.983 ± 0.305 | 1.011 | 14.405 ± 0.322 | 14.902 ± 0.438 | 1.034 | 14.853 | 14.456 ± 0.180 | 0.973 | same direction, ranges overlap |
| chinese radixHuskySortAuto n=1000000 | 69.631 ± 1.861 | 70.594 ± 1.611 | 1.014 | 69.588 ± 1.412 | 70.941 ± 1.371 | 1.019 | 69.615 | 70.167 ± 0.985 | 1.008 | same direction, ranges overlap |

The fork means (min–max of the 3 fork means, ms/op):

| row | old r1 | new r1 | old r2 | new r2 | patch r3 |
|---|---|---|---|---|---|
| english huskyEncodeOnlyEnglishMasking n=200000 | 17.977-18.654 | 20.450-20.743 | 18.287-19.010 | 20.920-21.043 | 18.680-19.235 |
| english huskyEncodeOnlyEnglishMasking n=1000000 | 96.583-99.962 | 109.010-109.487 | 92.170-98.432 | 108.080-111.496 | 99.245-101.593 |
| english huskyEncodeOnlyEnglishSaturating n=200000 | 33.894-34.669 | 34.222-35.125 | 34.040-34.869 | 35.618-35.790 | 34.908-35.442 |
| english huskyEncodeOnlyEnglishSaturating n=1000000 | 174.916-178.801 | 178.240-185.299 | 170.904-179.664 | 172.796-182.358 | 179.421-185.231 |
| chinese huskyEncodeOnly n=200000 | 3.459-3.573 | 3.628-3.733 | 3.405-3.609 | 3.600-3.757 | 3.755-4.000 |
| chinese huskyEncodeOnly n=1000000 | 18.283-19.059 | 19.108-20.752 | 18.017-18.933 | 19.122-20.303 | 19.491-20.393 |
| chinese radixHuskySortAuto n=200000 | 14.521-15.112 | 14.786-15.267 | 14.110-14.766 | 14.329-15.538 | 14.854-15.150 |
| chinese radixHuskySortAuto n=1000000 | 68.032-70.544 | 70.027-71.453 | 69.063-70.129 | 70.142-71.424 | 70.700-71.734 |

**The masking slowdown comes from the code, not from the host.** On masking english, every new fork is slower than every
old fork, at both n and in both rounds. new − old is +2.312 / +2.451 ms at 200,000 (+11.560 / +12.256 ns per element)
and +10.932 / +13.953 ms at 1,000,000 (+10.932 / +13.953 ns per element). The old jar reproduces 11cd (masking at
1,000,000: 98.256 ± 1.657 and 95.627 ± 2.305 against 98.077) and the new jar reproduces request 12 (109.188 ± 1.928 and
109.580 ± 2.187 against 107.987 ± 1.202). Saturating english (new ÷ old 1.009–1.035×) and chinese `radixHuskySortAuto`
(1.011–1.034×) moved the same way in both rounds, but their fork ranges overlap in at least one round, so no slowdown is
established for them. The saturating coder keeps the same call in its loop (below), and we do not know why it pays so
much less: at 1,000,000 its new − old is 3.2–4.1 ns per element, against masking's 10.9–14.0.

**The check does almost no work.** Counted without timing, through the benchmark's own `StringState.setup()`
(`req12-extra/count/`, outside the repo):

| array (`StringState.setup()`, `Random(42)`, with replacement) | `englishCoder` (masking) | `englishSaturatingCoder` | `UNICODE_CODER` |
|---|---|---|---|
| english, n = 200,000 and 1,000,000 | 3 elements tested, 13 characters scanned; stops at element 2, "environmentally" (length 15 > 10) | the same: 3 elements, 13 characters, element 2 | 1 element, 0 characters: element 0, "unridable" (length 9 > 3) |
| chinese, n = 200,000 and 1,000,000 | 1 element, 0 characters: element 0 is 12 characters long (> 10) | the same | 1 element, 0 characters (12 > 3) |

`huskyEncode(master).perfect` is false in all 12 cases, and the old jar stops at the same element with its length-only
test. The master arrays and the code arrays have the same sha256 under the old, new and patched jars
(`count/count-digests.txt`). After the first failure the source executes only the `isPerfect` test in each iteration, in
both jars (presumably a well-predicted branch; not measured), so the extra 10.9–14.0 ns per element is not work the
check does.

**What the compiler does differently: the compiled loop keeps a call.** From `-XX:+PrintInlining` in the one-fork
diagnostic runs (`jmh-diag-*.log`, outside the repo; their scores are not used):

- old jar, `englishCoder` (`jmh-diag-old-E.log:10754–10757`): `String::length` and `perfectForLength` are both
  `inline (hot)`, so the loop holds no call.
- new jar, `englishCoder` (`jmh-diag-new-E.log:10769–10771`): the bridge
  `HuskyCoderFactory$2::exactlyEncodable (9 bytes)` is `inline (hot)`, but the override it calls,
  `exactlyEncodable (25 bytes)`, gets **`low call site frequency`** and stays an out-of-line call inside the encode
  loop. `englishSaturatingCoder` (`$4`) has the same lines (`:21410–21412`). The `huskyEncode → stringToLong` subtree is
  inlined as in the old jar.
- new jar, `UNICODE_CODER` on chinese (`jmh-diag-new-C.log:9440–9443`): the base `exactlyEncodable` is inlined, but
  inside it `CharSequence::length` is a `virtual call` and `perfectForLength` gets `low call site frequency`, so two
  calls remain in the loop; the old jar inlines both (`jmh-diag-old-C.log:9703–9706`).
- patched jar: the test loop keeps the same out-of-line call (`jmh-diag-patch-E.log:10528`), but that loop runs 3
  iterations; the second loop's `huskyEncode` is `inline (hot)` with no call left (`:10547`, profile 194,738 counts
  against 3 for the test), and the same holds on chinese (`jmh-diag-patch-C.log:9448`).

The call is executed only until the first failure, so if the call site is what costs, it costs through the shape of the
compiled loop, a call site on the cold branch of a hot loop, and not through work. PrintInlining shows that the override
is no longer inlined at that call site; it does not show why that costs what it costs, and there is no `hsdis` on this
host to show which optimisation of the loop is lost. The evidence that the call site matters is the patched jar. Four
facts do not fit a fixed per-element cost of the call site, and we have no explanation for them:

- the saturating coder keeps the identical call and pays 3.2–4.1 ns per element at 1,000,000 (fork ranges overlap),
  against masking's 10.9–14.0;
- masking at n = 32,000 did not move in the full suite (1.540 → 1.551, 1.007×†), where 10.9–14.0 ns per element would
  have added 0.350–0.446 ms (22.7–29.0 %); the A/B did not run 32,000;
- the patch recovers only 66–84 % of the masking loss;
- the chinese `UNICODE_CODER` slowdown is flat in n (1.065× / 1.069× / 1.073× at 32,000 / 200,000 / 1,000,000), while
  masking's appears only at n ≥ 200,000 (1.007×† / 1.111× / 1.101×).

**A proposed minimal fix (a proposal, not applied to your code or to our PR):** test only until the first element that
fails, then encode the rest in a loop with no test in its body.

```diff
--- a/src/main/java/edu/neu/coe/huskySort/sort/huskySortUtils/BaseHuskySequenceCoder.java
+++ b/src/main/java/edu/neu/coe/huskySort/sort/huskySortUtils/BaseHuskySequenceCoder.java
@@ -45,10 +45,13 @@ public abstract class BaseHuskySequenceCoder<X extends CharSequence> implements
     public Coding huskyEncode(final X[] xs) {
         boolean isPerfect = true;
         final long[] result = new long[xs.length];
-        for (int i = 0; i < xs.length; i++) {
+        int i = 0;
+        // Test only until the first element that fails: the rest is encoded by a loop with no test in its body.
+        for (; isPerfect && i < xs.length; i++) {
             final X x = xs[i];
-            if (isPerfect) isPerfect = exactlyEncodable(x);
+            isPerfect = exactlyEncodable(x);
             result[i] = huskyEncode(x);
         }
+        for (; i < xs.length; i++) result[i] = huskyEncode(xs[i]);
         return new Coding(result, isPerfect);
     }
```

The semantics are the same: on all 12 arrays the patched jar gives the same `perfect` flag and the same codes (digests
above), and it passes `mvn -B test` with 465 tests, 0 failures (`req12-extra/build/patch-mvn-test.log`).
`git apply --check` of this block passes against `BaseHuskySequenceCoder.java` at `d8958bf`, and applying it gives the
file the patched jar was built from. Its jar (sha256 `0a72b683…a20a9b`) is ours, built in a scratch worktree at
`d8958bf` plus this diff; **it is not your code, and no request-12 row comes from it.** One pass, after the A/B:

| row | patch r3 | patch/old (mean of r1,r2 scores) | patch/new (mean of r1,r2 scores) | patch fork means vs old / new fork means |
|---|---|---|---|---|
| english huskyEncodeOnlyEnglishMasking n=200000 | 18.945 ± 0.457 | 1.029 | 0.911 | overlap / below all |
| english huskyEncodeOnlyEnglishMasking n=1000000 | 100.425 ± 1.473 | 1.036 | 0.918 | overlap / below all |
| english huskyEncodeOnlyEnglishSaturating n=200000 | 35.149 ± 0.442 | 1.020 | 0.998 | above all / overlap |
| english huskyEncodeOnlyEnglishSaturating n=1000000 | 181.521 ± 3.675 | 1.032 | 1.011 | overlap / overlap |
| chinese huskyEncodeOnly n=200000 | 3.840 ± 0.083 | 1.090 | 1.046 | above all / overlap |
| chinese huskyEncodeOnly n=1000000 | 19.807 ± 0.286 | 1.065 | 1.004 | above all / overlap |
| chinese radixHuskySortAuto n=200000 | 14.981 ± 0.244 | 1.025 | 1.003 | overlap / overlap |
| chinese radixHuskySortAuto n=1000000 | 71.332 ± 1.374 | 1.025 | 1.008 | above all / overlap |

On masking english the patch recovers 70.4 % / 83.7 % of new − old at 200,000 and 80.2 % / 65.6 % at 1,000,000, measured
against rounds 1 / 2 (77.3 % and 72.0 % against the mean of the two rounds), and every patched fork is faster than every
new fork. It is one pass, run last.

**The chinese encode row is not explained.** For `huskyEncodeOnly` with `UNICODE_CODER`, new ÷ old is 1.043 / 1.041 at
200,000 and 1.057 / 1.063 at 1,000,000, reproduced at 1,000,000 with disjoint fork ranges in both rounds and not
established at 200,000 (disjoint in round 1, old 3.459–3.573 against new 3.628–3.733; overlapping by 0.009 ms in round
2). The patched loop is call-free here too, yet patch ÷ new is 1.046 / 1.004, so for this coder the call in the loop is
not shown to be the cause. The patched pass ran last, and its `radixHuskySortAuto` rows sit 1.025× above old, so some
host drift during it is possible, but not enough to explain 1.090× at 200,000. **The test that would settle it:**
rebuild `d8958bf` with only `BaseHuskySequenceCoder.java:50` reverted to `perfectForLength(x.length())`, and A/B that
jar against `d947e77` on english as well as chinese. On english it would measure what we now attribute to `f43ff52` by
reading the code; on chinese it would show whether line 50 is the cause at all.

**Caveats.** 3 forks per A/B cell against 5 in the suite, and only 2 rounds; the patched jar ran once, last. The old jar
is a rebuild at `d947e77`: its sha256 `5f088118…a800d6` differs from the 11cd jar's `7d5e0bff…138295`, presumably
because the shaded jar embeds build times: all 364 of its `edu/neu/coe/huskySort` entries carry the rebuild's build time
(Mon 00:07 PT, 07:07Z) as their zip timestamp. The 11cd jar itself was not kept, so we could not compare its class
files. Its size equals the 11cd jar's, 76,379,654 B, and its master and code arrays have the same digests as the new
jar's. `unison` and `git-lfs` bursts (up to 1.6 cores) hit old and new passes alike (`load-heavy-times.txt`). The A/B
did not include the english whole sort, so the share of the shortfall in (a) remains an inference.

## The paper's tables, from the new data (ms/op)

All rows below come from this request's whole-class invocations, so each table is one dataset on one jar. The
two-methods rule is not met for any ratio (the two-methods paragraph under "Build, host and checks"); the baselines ran
after the husky methods within each class. "same in 11cd" is the same ratio from the 11cd row in the same role as the
request-12 row (english: the row of the same name, which ran the saturating coder; chinesenames: the 11cd
`..._pinyinRank` row, which ran the same code; chinese: the row of the same name, same coder).

### Strings: `ParallelStringSortBenchmarks` (84-row matrix in the appendix)

| corpus, n | `serialRadixHuskySortAuto` | `pAll` | `pAll_parCleanup` | `pAll_pinyinOrdinal` | `systemSortParallel` | parallelSort ÷ pAll | same in 11cd (mapped rows) | parallelSort ÷ pAll_parCleanup | parallelSort ÷ pAll_pinyinOrdinal | pAll_pinyinOrdinal ÷ pAll | req12 ÷ 11cd: pAll, parallelSort |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| english, 32,000 | 4.211 ± 0.141‡ | 4.962 ± 0.423 | 4.285 ± 0.296‡ | — | 4.858 ± 0.048 | 0.98×† | 0.98× | 1.13× (steady 1.20×) | — | — | 1.00×† (coder s→m), 1.001×† |
| english, 200,000 | 51.627 ± 1.409 | 23.554 ± 1.217‡ | 9.527 ± 0.174 | — | 11.486 ± 0.184 | 0.49× (steady 0.53×) | 0.49× | 1.21× | — | — | 1.01×† (coder s→m), 1.006×† |
| english, 1,000,000 | 215.345 ± 2.981 | 97.517 ± 18.127‡ | 56.135 ± 8.227‡ | — | 95.021 ± 2.108 | 0.97×† (steady 1.25×) | 0.90× | 1.69× (steady 1.99×) | — | — | 0.93×† (coder s→m), 1.013×† |
| chinese, 32,000 | 2.339 ± 0.019 | 3.233 ± 0.174 | 2.259 ± 0.026 | — | 3.279 ± 0.018 | 1.01×† | 1.03× | 1.45× | — | — | 1.023×†, 1.004×† |
| chinese, 200,000 | 14.682 ± 0.261 | 10.073 ± 0.083 | 6.059 ± 0.122 | — | 6.698 ± 0.038 | 0.66× | 0.66× | 1.11× | — | — | 1.004×†, 1.009× |
| chinese, 1,000,000 | 71.018 ± 1.472 | 41.619 ± 1.089 | 31.025 ± 0.527 | — | 45.878 ± 2.202 | 1.10× | 1.19× | 1.48× | — | — | 1.042×, 0.967×† |
| chinesenames, 32,000 | 2.912 ± 0.062 | 3.032 ± 0.073 | 3.041 ± 0.057 | 24.528 ± 0.295 | 21.744 ± 0.125 | 7.17× | 6.97× | 7.15× | 0.89× | 8.09× | 0.984×† (renamed), 1.013×† |
| chinesenames, 200,000 | 35.616 ± 0.213 | 8.193 ± 0.048 | 8.157 ± 0.047 | 113.582 ± 1.669 | 46.820 ± 0.838 | 5.71× | 5.68× | 5.74× | 0.41× | 13.86× | 1.000×† (renamed), 1.007×† |
| chinesenames, 1,000,000 | 172.903 ± 1.851 | 35.171 ± 1.071 | 34.606 ± 1.039 | 557.010 ± 11.041 | 241.984 ± 7.345 | 6.88× | 6.93× | 6.99× | 0.43× | 15.84× | 1.036×† (renamed), 1.028×† |

A ratio above 1 means the right-hand method is faster; for a ratio in which either row is ‡, the steady level is in
brackets. One 10-method invocation, `systemSortParallel` last. Steady levels (iterations 6–10) of the ‡ cells:
english@32,000 `serialRadixHuskySortAuto` 4.030 and `pAll_parCleanup` 4.049; english@200,000 pAll 21.745;
english@1,000,000 pAll 76.283 and `pAll_parCleanup` 47.732.

| corpus, n | serial ÷ p8 | serial ÷ pAll | serial ÷ pAll_parCleanup | serial ÷ pAll_pinyinOrdinal |
|---|---:|---:|---:|---:|
| english, 32,000 | 0.92×† (steady 0.88×) | 0.85× (steady 0.81×) | 0.98×† (steady 1.00×) | — |
| english, 200,000 | 2.19× (steady 2.40×) | 2.19× (steady 2.37×) | 5.42× | — |
| english, 1,000,000 | 2.13× (steady 2.76×) | 2.21× (steady 2.82×) | 3.84× (steady 4.51×) | — |
| chinese, 32,000 | 0.72× | 0.72× | 1.04× | — |
| chinese, 200,000 | 1.46× | 1.46× | 2.42× | — |
| chinese, 1,000,000 | 1.71× | 1.71× | 2.29× | — |
| chinesenames, 32,000 | 0.95× | 0.96×† | 0.96× | 0.12× |
| chinesenames, 200,000 | 3.98× | 4.35× | 4.37× | 0.31× |
| chinesenames, 1,000,000 | 4.17× | 4.92× | 5.00× | 0.31× |

- **chinesenames, now on the rank coder:** `parallelSort` ÷ pAll = 7.17× / 5.71× / 6.88× at 32,000 / 200,000 / 1,000,000
  (11cd's `pAll_pinyinRank`: 6.97× / 5.68× / 6.93×), and the ordinal coder, now `pAll_pinyinOrdinal` (11cd's
  chinesenames pAll), is 8.09× / 13.86× / 15.84× slower than pAll. `pAll_parCleanup` equals pAll within its CI, as it
  should when no cleanup runs.
- **english@1,000,000 is still unconverged after 5 × 2 s of warm-up**, as in 11cd: pAll 97.517 ± 18.127 ‡ (steady 76.283),
  so `parallelSort` ÷ pAll is 0.97×† by score and 1.25× on the steady level (11cd: 0.90×† and 1.24×), and `parallelSort`
  ÷ `pAll_parCleanup` is 1.69× (steady 1.99×).
- **english@200,000 and chinese**: pAll still trails `Arrays.parallelSort` at 200,000 (0.49×, steady 0.53×, english;
  0.66× chinese), and the parallel cleanup still turns those into wins (1.21× and 1.11×).

### Strings: `StringSortBenchmarks`, the columns the paper quotes (all 171 rows in the appendix)

| corpus, n | `radixHuskySortAuto` | `radixHuskySort16` | `radixHuskySortAutoPinyinOrdinal` | `quickHuskySort` | `huskyEncodeOnly` | `msdStringSort` | `multikeyQuicksort` | `systemSort` | `systemSortParallel` | `systemSortPinyin` | `insertionSort` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| english, 32,000 | 4.069 ± 0.174‡ | 3.698 ± 0.241‡ | — | 8.266 ± 0.129 | 1.750 ± 0.047 | 4.218 ± 0.144‡ | 7.991 ± 0.071 | 14.839 ± 0.304 | 4.882 ± 0.054 | — | 43.215 ± 0.304 |
| english, 200,000 | 51.224 ± 0.968 | 47.462 ± 0.892 | — | 104.170 ± 1.599 | 21.217 ± 0.333 | 60.084 ± 1.819‡ | 79.973 ± 1.115 | 157.808 ± 2.349 | 11.700 ± 0.148 | — | 1215.238 ± 3.858 |
| english, 1,000,000 | 220.857 ± 2.819 | 217.009 ± 2.923 | — | 662.778 ± 9.263 | 109.132 ± 1.471 | 321.415 ± 19.633‡ | 739.699 ± 11.354 | 1269.099 ± 31.475 | 99.273 ± 9.874 | — | 26901.756 ± 52.197 |
| chinese, 32,000 | 2.362 ± 0.013 | 2.257 ± 0.020 | — | 5.386 ± 0.037 | 0.509 ± 0.006 | — | 6.005 ± 0.054 | 10.800 ± 0.058 | 3.282 ± 0.013 | — | 33.786 ± 0.063 |
| chinese, 200,000 | 14.456 ± 0.180 | 13.670 ± 0.265 | — | 38.811 ± 0.326 | 3.727 ± 0.046 | — | 42.553 ± 0.287 | 82.802 ± 0.687 | 6.655 ± 0.033 | — | 1062.395 ± 3.082 |
| chinese, 1,000,000 | 70.167 ± 0.985 | 70.415 ± 1.165 | — | 283.821 ± 3.355 | 19.597 ± 0.343 | — | 307.586 ± 3.881 | 529.009 ± 3.612 | 46.611 ± 2.228 | — | 25925.815 ± 35.647 |
| chinesenames, 32,000 | 2.852 ± 0.059 | 2.874 ± 0.057 | 24.994 ± 0.306 | 6.758 ± 0.106 | 1.841 ± 0.031 | — | 37.212 ± 0.522 | 10.949 ± 0.167 | 21.547 ± 0.307 | 72.445 ± 0.987 | 37.052 ± 0.217 |
| chinesenames, 200,000 | 35.915 ± 0.306 | 34.716 ± 0.247 | 152.397 ± 1.621 | 71.545 ± 0.621 | 27.548 ± 0.190 | — | 295.260 ± 1.854 | 110.876 ± 0.821 | 47.412 ± 0.626 | 565.112 ± 3.195 | 1135.358 ± 3.048 |
| chinesenames, 1,000,000 | 174.039 ± 1.870 | 172.075 ± 1.719 | 759.740 ± 12.258 | 664.876 ± 3.896 | 139.133 ± 0.594 | — | 1676.192 ± 15.271 | 869.983 ± 8.535 | 232.975 ± 1.998 | 3207.508 ± 54.942 | 26344.876 ± 40.479 |

A dash marks a row absent by design (the method throws for that corpus). Steady levels of the ‡ cells: english@32,000
`radixHuskySortAuto` 3.863 and `radixHuskySort16` 3.307; english `msdStringSort` 4.021 / 58.264 / 286.824 at 32,000 /
200,000 / 1,000,000.

Speed-ups of `radixHuskySortAuto` (A ÷ B from unrounded scores; > 1 = B faster):

| corpus, n | systemSort ÷ Auto | same in 11cd | systemSortParallel ÷ Auto | msdStringSort ÷ Auto | multikeyQuicksort ÷ Auto | systemSort ÷ quickHuskySort | systemSortPinyin ÷ Auto | systemSortPinyin ÷ PinyinOrdinal | PinyinOrdinal ÷ Auto | Auto ÷ radixHuskySort16 | req12 ÷ 11cd: Auto, systemSort |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| english, 32,000 | 3.65× (steady 3.84×) | 3.31× | 1.20× (steady 1.26×) | 1.04×† (steady 1.04×) | 1.96× (steady 2.07×) | 1.80× | — | — | — | 1.10×† (steady 1.17×) | 0.89× (coder s→m), 0.983×† |
| english, 200,000 | 3.08× | 2.52× | 0.23× | 1.17× (steady 1.14×) | 1.56× | 1.51× | — | — | — | 1.08× | 0.82× (coder s→m), 1.009×† |
| english, 1,000,000 | 5.75× | 4.62× | 0.45× | 1.46× (steady 1.30×) | 3.35× | 1.91× | — | — | — | 1.02×† | 0.79× (coder s→m), 0.981×† |
| chinese, 32,000 | 4.57× | 4.58× | 1.39× | — | 2.54× | 2.01× | — | — | — | 1.05× | 1.007×†, 1.005×† |
| chinese, 200,000 | 5.73× | 5.55× | 0.46× | — | 2.94× | 2.13× | — | — | — | 1.06× | 0.973×†, 1.005×† |
| chinese, 1,000,000 | 7.54× | 7.50× | 0.66× | — | 4.38× | 1.86× | — | — | — | 1.00×† | 1.008×†, 1.013×† |
| chinesenames, 32,000 | — | — | 7.55× | — | 13.05× | — | 25.40× | 2.90× | 8.76× | 0.99×† | 1.005×† (renamed), 0.992×† |
| chinesenames, 200,000 | — | — | 1.32× | — | 8.22× | — | 15.73× | 3.71× | 4.24× | 1.03× | 1.001×† (renamed), 1.019× |
| chinesenames, 1,000,000 | — | — | 1.34× | — | 9.63× | — | 18.43× | 4.22× | 4.37× | 1.01×† | 1.004×† (renamed), 1.022× |

"same in 11cd" = `systemSort` ÷ the 11cd row in the same role as req12 `radixHuskySortAuto` (english: the row of that
name, which ran the saturating coder; chinesenames: `radixHuskySortAutoPinyinRank`). **On chinesenames the three
`systemSort` columns are dashed.** `systemSort` is `Arrays.sort` with no comparator, which orders chinesenames by UTF-16
code point, while the husky rows sort it in pinyin order; your comment at `StringSortBenchmarks.java:225–230` says
"Comparing a pinyin-correct sort against it is not a comparison", and our 2026-09-27 document quoted no such ratio. The
pinyin-correct ratio is `systemSortPinyin ÷ Auto`. `systemSortParallel` and `multikeyQuicksort` sort chinesenames by
pinyin (`:292`, `:300`), so their columns stand.

- **english, masking:** `systemSort` ÷ `radixHuskySortAuto` = 3.65× (steady 3.84×) / 3.08× / 5.75× at 32,000 / 200,000 /
  1,000,000, against 3.31× / 2.52× / 4.62× with the saturating coder in 11cd. `multikeyQuicksort` ÷ `radixHuskySortAuto`
  is 1.96× (steady 2.07×) / 1.56× / 3.35× and `msdStringSort` ÷ `radixHuskySortAuto` 1.04×† / 1.17× / 1.46× (steady
  1.04× / 1.14× / 1.30×).
- **chinese:** 4.57× / 5.73× / 7.54×, as in 11cd (4.58× / 5.55× / 7.50×).
- **chinesenames, rank coder:** against the pinyin-correct `systemSortPinyin`, 25.40× / 15.73× / 18.43×; the ordinal
  coder (`radixHuskySortAutoPinyinOrdinal`, 11cd's chinesenames `radixHuskySortAuto`) is 8.76× / 4.24× / 4.37× slower
  than the rank coder.

### Permits and `Long[]`

| n | `systemSort` | `systemSortParallel` | `dualPivotQuicksort` | `quickHuskySort` | `quickHuskySortWithCleanup` | `radixHuskySort8` | `radixHuskySort11` | `radixHuskySort16` | `radixHuskySortAuto` | `16_p4` | `16_p8` | `Auto_p8` | `Auto_p8_chunk4k` | `Auto_pAll` |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 14.199 ± 0.213 | 4.752 ± 0.038 | 12.754 ± 0.065 | 6.810 ± 0.087 | 7.804 ± 0.126 | 3.023 ± 0.035 | 2.801 ± 0.042 | 2.718 ± 0.028 | 2.857 ± 0.053 | 3.429 ± 0.061 | 3.454 ± 0.085 | 3.033 ± 0.055 | 2.987 ± 0.054 | 3.016 ± 0.061 |
| 100,000 | 65.210 ± 0.743 | 6.255 ± 0.091 | 56.255 ± 0.533 | 30.357 ± 0.449 | 40.220 ± 1.340 | 15.134 ± 0.374 | 14.246 ± 0.580 | 14.398 ± 0.557 | 13.302 ± 0.117 | 7.163 ± 0.144 | 7.038 ± 0.151 | 4.915 ± 0.030 | 4.930 ± 0.101 | 5.002 ± 0.106 |
| 198,900 | 159.080 ± 2.011 | 13.826 ± 0.131 | 142.047 ± 1.883 | 73.841 ± 1.859 | 91.144 ± 1.739 | 32.669 ± 1.417 | 35.612 ± 1.485 | 29.774 ± 0.335 | 33.672 ± 1.285 | 14.657 ± 0.521 | 11.915 ± 0.189 | 10.076 ± 0.319 | 9.865 ± 0.243 | 8.722 ± 0.177 |

| permits n | parallelSort ÷ pAll | same in 11cd | systemSort ÷ radixHuskySortAuto | req12 ÷ 11cd: systemSort, systemSortParallel, radixHuskySort16, Auto_pAll |
|---:|---:|---:|---:|---|
| 32,000 | 1.58× | 1.58× | 4.97× | 0.997×†, 1.009×†, 0.993×†, 1.011×† |
| 100,000 | 1.25× | 1.20× | 4.90× | 0.994×†, 1.031×†, 1.042×†, 0.993×† |
| 198,900 | 1.59× | 1.61× | 4.72× | 0.997×†, 0.985×†, 0.974×†, 0.998×† |

| `Long[]` n | `serialRadixHuskySort11` | `parallelRadixHuskySort11_p1` | `parallelRadixHuskySort11_p2` | `parallelRadixHuskySort11_p4` | `parallelRadixHuskySort11_p8` | `quickHuskySort` | `systemSortParallel` | parallelSort ÷ 11_p8 | same in 11cd | serial ÷ 11_p8 |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 2,000,000 | 157.153 ± 4.604‡ | 198.311 ± 4.609 | 151.793 ± 4.689 | 110.928 ± 2.491 | 98.822 ± 1.399 | 1142.740 ± 10.774 | 90.441 ± 3.117‡ | 0.92× (steady 0.88×) | 0.93× | 1.59× (steady 1.51×) |
| 10,000,000 | 960.320 ± 23.917 | 1104.141 ± 24.171 | 813.100 ± 24.612 | 625.787 ± 23.972 | 561.525 ± 19.773 | 5920.311 ± 24.540 | 785.359 ± 10.397 | 1.40× | 1.26× | 1.71× |

Permit ran as one 14-method invocation and ParallelRadix as one 7-method invocation, `systemSortParallel` among them.

Steady levels of the ‡ cells at 2,000,000: `serialRadixHuskySort11` 149.600, `systemSortParallel` 87.334.

- **permits:** `parallelSort` ÷ pAll = 1.58× / 1.25× / 1.59× at 32,000 / 100,000 / 198,900 (11cd 1.58× / 1.20× / 1.61×),
  and `systemSort` ÷ `radixHuskySortAuto` 4.97× / 4.90× / 4.72×.
- **`Long[]`:** `parallelSort` ÷ `11_p8` is 0.92× (steady 0.88×) at 2,000,000 and 1.40× at 10,000,000 (11cd 0.93× /
  1.26×). The change at 10,000,000 is the baseline's: `systemSortParallel` is 1.086× its 11cd value (785.359 ± 10.397
  against 723.153, whose 11cd half-width was 50.901), while the husky rows of the class read 0.982–1.024 of 11cd.

## Convergence and the absent rows

Per class, req12 (11cd): Date ‡ 0 (0), § 0 (0), ParallelRadix ‡ 2 (1), § 0 (0), Tuple ‡ 0 (0), § 0 (0), Permit ‡ 0 (0),
§ 0 (0), ParallelString ‡ 16 (19), § 2 (1), Numeric ‡ 0 (0), § 0 (0), Adversarial ‡ 10 (11), § 1 (0), String ‡ 12 (13),
§ 0 (0).

40 of the 582 rows are ‡ and 3 are §. 35 of the 40 ‡ rows were also ‡ in 11cd (same key, renamed rows mapped). The 40
are 14 english ParallelString rows at all three n (at 1,000,000 fork t reaches 1.91, and the steady level of p4, p8,
`11_p8`, pAll and `pAll_parCleanup` is 0.77–0.85 of the score), 12 english String rows (the eight radix rows at 32,000,
`radixHuskySort13`@200,000 and `msdStringSort` at every n), 10 Adversarial radix-8 / radix-11 rows at 1,000,000 (t
1.11–1.16), two chinesenames ParallelString rows at 32,000 (t 1.11–1.13) and two ParallelRadix rows at 2,000,000. No
chinese, Permit, Numeric, Date or Tuple row is ‡. The three § rows are new: ParallelString
`parallelRadixHuskySortAuto_p2` and `_p8` on chinese@32,000 (t 0.87–0.89 in every fork) and Adversarial
`collapsedBitsRadixHuskySort11` at fixedHighBits 63, n = 1,000,000 (t 0.85–0.88). 11cd's § row, chinesenames@1,000,000
`pAll_parCleanup`, is now a rank-coder row that runs no cleanup and is not §. The full ‡ and § lists, with each row's
steady level, are in the appendix. Two ‡ rows have a steady level above their score, english ParallelString
`parallelRadixHuskySortAuto_p2`@1,000,000 (steady ÷ score 1.025) and `parallelRadixHuskySort11_p8`@32,000 (1.009): in
each, some forks sped up and others slowed (mean t 0.99), so for them ‡ does not mean the score is above steady state.
In (a), `radixHuskySortAuto`@32,000 is ‡ in both runs, which is why we call its ratio the least reliable; the 200,000
and 1,000,000 rows are converged.

**24 rows are absent by design**, each a benchmark that throws for that corpus and that JMH's default `-foe false` logs
and skips (120 `<failure>` fork lines, every class exit 0). They are the 11cd absent set under the new names:

| throw site (d8958bf) | method | corpora | n | in JSON | `<failure>` lines expected |
|---|---|---|---|---:|---:|
| `StringSortBenchmarks.java:235` | `String.systemSortPinyin` | english, chinese | 32,000 / 200,000 / 1,000,000 | 0 | 30 |
| `StringSortBenchmarks.java:258` | `String.radixHuskySortAutoPinyinOrdinal` | english, chinese | 32,000 / 200,000 / 1,000,000 | 0 | 30 |
| `StringSortBenchmarks.java:319` | `String.msdStringSort` | chinese, chinesenames | 32,000 / 200,000 / 1,000,000 | 0 | 30 |
| `ParallelStringSortBenchmarks.java:177` | `ParallelString.parallelRadixHuskySortAuto_pAll_pinyinOrdinal` | english, chinese | 32,000 / 200,000 / 1,000,000 | 0 | 30 |

24 rows absent (expected 24); JSON rows 582 + 24 = 606 attempted (expected 606); `<failure>` lines in the logs 120
(expected 120).

## The harness, and what else changed since our jar

**The harness found no fault in your fixes.** Our 11cd harness (746 checks) did not compile against this jar: 8 errors,
from the `REGEX_LEIPZIG` move and the two renames (`req12/harness-old-compile.log`). With only those mechanical edits it
scored 740 / 746, and the 6 failures were intended changes: the `binaryInsertionCleanup` refusal message has new text
(I1), and the default-coder check failed 5 times, english at two n and chinesenames at three, because the defaults
changed (I4). We then updated the checks that encoded the old behaviour and added three sections, 1432 checks in all,
every one passing:

- **Updated:** I1's refusal-message prefix; I1's `pinyinRank` line, an INFO line that recorded our finding 2, is now a
  check that `binaryInsertionCleanup` refuses it; I4's default coders are now `englishCoder` and
  `chineseEncoderPinyinRank`.
- **Added to I1 and H4:** the six natural-order coders are still accepted by `binaryInsertionCleanup` and sort
  correctly; your new Javadoc witness, "NÂº" < "NÃ" while the saturating coder codes it higher.
- **J1** (171 checks): `perfect` requires the characters to fit. 10 cases × 4 coders, each checking the flag and that
  `RadixHuskySort`, `ParallelRadixHuskySort` pAll and the parallel cleanup sort correctly, including your Javadoc array
  {"cafÿ", "café", "cafa", "cafz"} and the "NÂº" / "NÃ" pair. It also shows that every english `StringState` and
  `SharedPrefixState` array already failed the old length-only test, so the new check changes no benchmark row's code
  path, only its cost.
- **J2** (498 checks): every husky row of both string classes, run through its own `@Benchmark` method on the
  `StringState` master, at 32,000 / 200,000 / 1,000,000 on all three corpora: the default coder is the expected one, and
  the output is sorted, a permutation, and equal to `Arrays.sort` in the corpus's order. That covers the masking
  `englishCoder` through `RadixHuskySort` AUTO and `ParallelRadixHuskySort` pAll, and the rank coder, which reports
  `perfect` on chinesenames, so no cleanup runs.
- **J3** (9 checks): the guards of all 24 absent rows throw on their excluded corpora, plus one `msdStringSort` control
  that runs.

Your "Also changed since your jar", one by one:

- **`perfect` now requires the characters to fit (`f43ff52`).** Correct, and it short-circuits: J1 passes, and the count
  under "The encode A/B" shows the check stops at the first element that fails, whatever the reason (element 2 on
  english, element 0 on chinese). No benchmark array was short enough throughout for the flag to change: every english
  `StringState` array has words over 10 characters (4,471 / 27,534 / 137,843 at 32,000 / 200,000 / 1,000,000,
  `req12/harness.log`), as does `SharedPrefixState`, so `perfect` was already false. It is not free, though (the encode
  A/B).
- **`binaryInsertionCleanup`'s guard tests the ordering (`1cbab86`).** It reads
  `state.ordering != Comparator.naturalOrder()` (`CleanupPassBenchmarks.java:304`) and refuses `pinyinRank` as well as
  `pinyin` (I1, `req12/harness.log:795`).
- **The saturating coders' Javadoc (`1cbab86`).** The class Javadocs of `englishSaturatingCoder` and
  `asciiSaturatingCoder` now say "per character", not per string, and `englishSaturatingCoder`'s gives the "NÂº" / "NÃ"
  witness, which H4 checks. One sentence remains: the `@return` of `stringToLongSaturating`
  (`HuskyCoderFactory.java:489`) still reads "a long, monotonically non-decreasing with respect to the natural ordering
  of str", the sentence our finding 1 quoted. Comment only.
- **`REGEX_LEIPZIG` moved to `HuskySortBenchmarkHelper` (`d82df1f`).** A pure relocation, as far as the data show: the
  harness loads 304,905 english and 49,446 chinese words, as at `d947e77`, and the `StringState` master arrays for
  english and chinese at 200,000 and 1,000,000 have the same sha256 under the `d947e77` and `d8958bf` jars
  (`req12-extra/count/count-digests.txt`).

## Methodology and disclosures

1. **Option B, not A.** The string-class rows are what option A would have produced; the other six classes add no
   information about the coders, as you said, and serve as a host check (e). Every row comes from one jar, one unit and
   one kernel.
2. **Two-methods rule**: not met by any full-suite ratio, and not meetable in a whole-class run (the two-methods
   paragraph under "Build, host and checks"). The encode A/B meets it: each of its invocations ran two methods.
3. **Machine of record**: the 11cd host (Graviton aarch64, 16 cores, one thread per core, 15 pool workers), on a newer
   kernel after a patch reboot; no host shift is visible: the 327 rows of the six classes read a median of 1.002 of
   11cd.
4. **Your figures** are typed from your request text.
5. **Derived figures** (ratios, medians, steady levels, fork-mean ranges, the convergence marks, the coder labels) come
   from `rawData` and the logs via `runner/req12-tables.py` and `runner/req12-ab-tables.py`, outside the repo, with the
   HALF_UP rule to 3 decimals; the shortfall table in (a) and the per-element costs were computed for this doc from the
   same JSONs.
6. **The encode A/B** was not requested. We ran it because a row you expected not to move did, and because "measures
   free" is a claim the paper may repeat. It ran on two jars and one patched jar of ours, never on modified source in
   `prwork`.

**What is not claimed.** Not a cause for the part of the english shortfall that the masking encode slowdown does not
cover (3.3–3.7 ms at 200,000, 4.5–8.5 ms at 1,000,000), nor that the whole sort pays exactly the encode-only penalty.
Not that `f43ff52` alone caused the A/B difference: the two jars are 7 `src/main` commits apart, and the attribution
rests on reading the code and on the patch. Not a mechanism for the masking cost: PrintInlining shows that the override
is no longer inlined at its call site, not why that costs 10.9–14.0 ns per element. Not a cause for the chinese
`huskyEncodeOnly` slowdown, nor for why the saturating coder pays much less than the masking coder for the same call in
its loop. Not that the proposed fix is the only one or the best: it is the smallest change we found that removes the
call from the hot loop, measured once on one jar, where it recovered 66–84 % of the masking loss. Not a verdict on
english@1,000,000 pAll against `Arrays.parallelSort` beyond the steady-level 1.25×. Not any ratio between two classes.

## Files

All under `doc/`, byte-identical copies (`cmp`) of the unedited JMH `-rff` output and the units' environment snapshots.

| file | sha256 | what |
|---|---|---|
| `req12-full-DateSortBenchmarks.json` | `12e62527aee27637fbb04ffca55794a48658382be4526f3cb124baf70a34cbab` | full suite, Date (Sat 19:39 PT), 6 rows |
| `req12-full-ParallelRadixSortBenchmarks.json` | `e462bc947c40c723ac850cf0b0e8271b2e5f791ee14595d2ce4dba1770731e31` | full suite, ParallelRadix, 14 rows |
| `req12-full-TupleSortBenchmarks.json` | `4b078605ec1b826bb4de4d15aced5eeba0cc0619eabd10555b45c2a2eff1da2a` | full suite, Tuple, 18 rows |
| `req12-full-PermitSortBenchmarks.json` | `537b685c7e94e9ef2a42c025d82f2ca031d981c403cb5638adbcb69c6a6f7ff2` | full suite, Permit, 42 rows |
| `req12-full-ParallelStringSortBenchmarks.json` | `e2260e720212c5613f4853efda3c4bb6ae2c755ba5a765b628620ae4fbe30edf` | full suite, ParallelString, 84 rows (6 absent by design) |
| `req12-full-NumericSortBenchmarks.json` | `91846f4e014926e577f0479a6e4a177a9fc702d9e252a59e42a6e2df366aedcd` | full suite, Numeric, 123 rows |
| `req12-full-AdversarialSortBenchmarks.json` | `cbe8a1edde7d65c5339f62aaeb41725e7e5df53bce6706e24cd834f63e8af81c` | full suite, Adversarial, 124 rows |
| `req12-full-StringSortBenchmarks.json` | `10509074a29a49a51b8c8f1d89441ebfddd6e76ed562f1febd065b3a567d3b89` | full suite, String (to Sun 23:56 PT), 171 rows (18 absent by design) |
| `req12-full-env-before.txt` | `cbdbc2d076c8c00644df8b9417375a15878dcc63da3b749932658057717ecd5a` | the full-suite unit: `date`, `uptime`, `lscpu`, `free -h`, `uname -r`, `java -version`, `mvn -v`, cgroup, `nproc`, git HEAD, src freeze, jar sha, idle-guard thresholds |
| `req12-encode-ab-r1-old-E.json` | `23f58d49a9d5c47d07506f816ad208cd7e7f0568a6d6f08558c5067212e83a00` | encode A/B round 1, `d947e77` rebuild, english `huskyEncodeOnlyEnglishMasking` / `...Saturating`, 4 rows, 3 forks |
| `req12-encode-ab-r1-old-C.json` | `584f22e2a8d938a3a0a25b84c45e4de70374898462ea21170f5d107101156302` | encode A/B round 1, `d947e77` rebuild, chinese `huskyEncodeOnly` / `radixHuskySortAuto`, 4 rows, 3 forks |
| `req12-encode-ab-r1-new-E.json` | `0e0171228b0f3fb9513b9019855be66ede2cf39d8331cf82e2e37514df2ded08` | encode A/B round 1, this request's jar, english `huskyEncodeOnlyEnglishMasking` / `...Saturating`, 4 rows, 3 forks |
| `req12-encode-ab-r1-new-C.json` | `4342d3c8223a9d029fb58f7d6ca98653cce9339c6507901531891f5141f25c36` | encode A/B round 1, this request's jar, chinese `huskyEncodeOnly` / `radixHuskySortAuto`, 4 rows, 3 forks |
| `req12-encode-ab-r2-old-E.json` | `71048b23747a3ad80cb597b3f9d24bf69fc8d69fb69e352fc507178c3bf0405f` | encode A/B round 2, `d947e77` rebuild, english `huskyEncodeOnlyEnglishMasking` / `...Saturating`, 4 rows, 3 forks |
| `req12-encode-ab-r2-old-C.json` | `b4101170e2eb827979ddd35dad75a6f9164ef6bd667dffb29e8549bf4920b32d` | encode A/B round 2, `d947e77` rebuild, chinese `huskyEncodeOnly` / `radixHuskySortAuto`, 4 rows, 3 forks |
| `req12-encode-ab-r2-new-E.json` | `a0c56968bb34b1bfc493f3d5d5952c0d115ddd5b2a01d7f230d2499bb17fc5cb` | encode A/B round 2, this request's jar, english `huskyEncodeOnlyEnglishMasking` / `...Saturating`, 4 rows, 3 forks |
| `req12-encode-ab-r2-new-C.json` | `a83efa0e330efbb416972ed9e84de9b68959754458492b7d51a794f6ff6f88b7` | encode A/B round 2, this request's jar, chinese `huskyEncodeOnly` / `radixHuskySortAuto`, 4 rows, 3 forks |
| `req12-encode-ab-r3-patch-E.json` | `b478494a486d6c52127985a07e7e0898dea9b6201b3d08868118b9b435659f68` | encode A/B round 3, patched jar (not your code), english `huskyEncodeOnlyEnglishMasking` / `...Saturating`, 4 rows, 3 forks |
| `req12-encode-ab-r3-patch-C.json` | `e70bb88f60ed360a150882fb6dda827dea38c6cb4c77098817d861adbcdc64fa` | encode A/B round 3, patched jar (not your code), chinese `huskyEncodeOnly` / `radixHuskySortAuto`, 4 rows, 3 forks |
| `req12-encode-ab-env-before.txt` | `1f8a06fc5deb8d87e82cb8c3f508281b2c515586c9d99616a1a4d6136b55cf0a` | the A/B unit: `date`, `uptime`, `free -m`, `nproc`, `uname -r`, `java -version`, cgroup, the three jars' sha256 and sizes, git heads, the patch's diffstat |

Plus this file and its appendix (every row of the eight full-suite JSONs with its 11cd value and coder label, the coder
classification, the ‡ and § lists, the load samples). The jars, the runner scripts, the logs, `harness.log`,
`load-monitor.log`, the probe, the count program and the PrintInlining logs are outside the repo, available on request.

## Reproduction

The full suite, from the package root with the pinned JDK, each its own `java -jar`, in the order run (arguments from
`req12/run.log`'s `STEP … START` lines):

```
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.DateSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-DateSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.ParallelRadixSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-ParallelRadixSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.TupleSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-TupleSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.PermitSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-PermitSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.ParallelStringSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-ParallelStringSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.NumericSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-NumericSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.AdversarialSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-AdversarialSortBenchmarks.json
java -jar target/benchmarks.jar '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.StringSortBenchmarks\.' -r 2s -w 2s -f 5 -wi 5 -i 10 -rf json -rff full-StringSortBenchmarks.json
```

The encode A/B, with `JAR` = the `d947e77` rebuild (old), this request's jar (new) or the patched jar (patch), in the
order old-E, old-C, new-E, new-C, old-E, old-C, new-E, new-C, patch-E, patch-C:

```
java -jar $JAR '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.StringSortBenchmarks\.(huskyEncodeOnlyEnglishMasking|huskyEncodeOnlyEnglishSaturating)$' -p corpus=english -p sampling=withreplacement -p n=200000,1000000 -r 2s -w 2s -f 3 -wi 5 -i 10 -rf json -rff ab-<round>-<jar>-E.json
java -jar $JAR '^edu\.neu\.coe\.huskySort\.sort\.huskySort\.StringSortBenchmarks\.(huskyEncodeOnly|radixHuskySortAuto)$' -p corpus=chinese -p sampling=withreplacement -p n=200000,1000000 -r 2s -w 2s -f 3 -wi 5 -i 10 -rf json -rff ab-<round>-<jar>-C.json
```

The six diagnostic runs used `-p n=1000000 -f 1 -wi 5 -i 1 -jvmArgsAppend "-XX:+UnlockDiagnosticVMOptions
-XX:+PrintCompilation -XX:+PrintInlining"` in place of the n, fork and iteration arguments (chinese: `huskyEncodeOnly`
only). Run each as a detached unit outside any CPU-quota'd cgroup, with `uptime` before and after, having checked that
`jshell -s -` prints `ForkJoinPool.commonPool().getParallelism() + " " + availableProcessors()` = `15 16` in that unit
(a quota'd slice reports `13 14` and corrupts every parallel row). `-l` with each class regex lists exactly the rows in
`req12/expected-rows.md`, and the `^…\.` anchors keep each regex to its own class.
