# Run results from Yunlu: request 13, the `perfect` check fixed and the affected rows re-run, 2026-10-18

**We took your first option, with your hoist as the fix (F1 below): test `exactlyEncodable` up to the first failure,
then encode in a loop with no call in it. This PR carries it on `28005b7` as one commit with this report; the re-run jar
was built at `52b0894`, which has the same `src/` tree (`5ade192`). The 173 rows whose timed path changed since request
12 (170 by the fix, 3 by `8b4639b`) were re-run beside 32 baselines in the same invocations. Our split loop is not the
fix: in a same-session A/B it recovered 0.589 / 0.663 of the masking loss at 200,000 / 1,000,000, and none of the
chinese loss at 200,000.**

**Line 50 also explains the chinese slowdown. At 200,000, where every new fork is slower than every old fork, reverting
that line alone recovers 0.779 of the chinese new − old; at 1,000,000, where only round 2 separates the forks, rev reads
0.979 of old (share 1.463). Before run 1 we expected line 50 not to be the chinese cause, and that did not hold. With
your hoist, the six key encode cells read 0.975–1.010 of the `d947e77` jar.**

**english `radixHuskySortAuto` is now 22.8 % / 25.9 % faster than in 11cd at 200,000 / 1,000,000 (request 12: 17.6 % /
21.1 %; your prediction: 26.9 % / 27.7 %). In its own invocation it is 3.31× / 6.41× faster than `Arrays.sort` (request
12: 3.08× / 5.75×). These come from invocations of 18 and 21 methods, and `Arrays.sort` itself read 1.006 / 1.046 of its
request-12 time. The english masking encode-only row is back at its 11cd value. The other re-run groups moved by medians
of 0.995–1.013, while the baselines moved by 0.995–1.020.**

We ran two units on the request-12 host. Run 1 (Fri 2026-10-16 21:59 PDT to Sat 01:47) was the line-50 A/B with our
request-12 patch as a fourth arm, plus the question-3 probes; its gate stopped the unit before the re-run. Run 2 (Sun
2026-10-18 00:18 to 14:26 PDT) A/B-tested three shapes of the fix, chose yours by a rule fixed before the run, and
re-ran the 205 rows. Every table is in `Run results from Yunlu 2026-10-18 - appendix.md`; the raw JSONs are under Files.

**Names and marks.** The jars: **old** is `d947e77` (the 11cd code, request 12's rebuild); **new** is `d8958bf` (request
12's jar); **rev** is new with only `BaseHuskySequenceCoder.java:50` reverted to the length test, a diagnostic only,
since it brings back item 48's wrong answers; **patch** is new plus our request-12 split loop; **F1** is new plus your
hoist; **F2** is new plus its mirror (encode first, then test to the first failure); **F3**, our addition, is new with
rev's loop and the exactness test after it; **fix** is the jar built at `52b0894`, which ran every re-run row. "Pooled"
is the mean of an arm's 6 fork means (3 forks × 2 rounds). Fork tests read `>` (every fork of the arm above every fork
of old), `<` (every one below) or `~` (overlap), as round 1 / round 2 / pooled. † = the two 99.9 % CIs overlap. ‡ = some
fork has t = mean(iterations 1–5) ÷ mean(6–10) > 1.10. § = t < 0.90 in every fork. **11cd** is the 2026-09-27 full suite
(`doc/req11cd-full-*.json`), **req12** request 12's (`doc/req12-full-*.json`). Ratios come from unrounded values, except
that question 5 uses your prediction as you stated it; every `a ± b` is a score and its 99.9 % half-width from a JSON
under Files or from those two suites. Times are PT (PDT, UTC−7), with UTC in brackets where we cite a log.

## Answers to request 13

### 1. Is the fix worth taking, and in what shape?

**Yes, in your shape: we ship F1, your hoist, not our split loop.** F1 passed the gate in all six key cells with the
lowest geometric mean of X/old, 0.984, and it puts the masking encode at 0.975 / 0.977 of old at 200,000 / 1,000,000.

- **Our split loop recovers less than request 12 suggested, and nothing on chinese.** Interleaved with old, new and rev
  over two rounds (`req13-ab-*.json`), the patch recovers 0.589 / 0.663 of the masking new − old at 200,000 / 1,000,000,
  and every patch fork is still slower than every old fork at both n. On chinese `huskyEncodeOnly` it recovers −0.166 at
  200,000, where new − old is established on every fork (0.162 at 1,000,000, where it is not). Request 12's 66–84 % came
  from one patch round, run last.
- **Your hoist recovers all of it** (`req13b-ab-*.json`). F1/old is 0.975 / 0.977 on masking, 0.984 / 0.982 on
  saturating and 0.976 / 1.010 on chinese `huskyEncodeOnly` at 200,000 / 1,000,000, and its pooled forks overlap old's
  in every key cell.
- **The gate, fixed before run 2** (`req13b-gate.txt`): pass = pooled X/old ≤ 1.025 in the six key cells (masking,
  saturating and chinese `huskyEncodeOnly`, each at 200,000 and 1,000,000); lowest geometric mean wins, F1 first if the
  best two are within 1 %. F1 passed 6/6 at 0.984, F2 6/6 at 0.990, F3 4/6 at 1.010, the control rev 6/6 at 0.995. F2
  ÷ F1 is 1.006, inside the 1 % margin, so the rule's order picked F1; this run does not separate them.
- **The hoist changes no answer.** A probe of 8 coders × 29 cases gives byte-identical codes and `perfect` flags under
  new, F1, F2, F3 and the three fix commits. At `52b0894`, `mvn -B test` runs 548 tests with 0 failures, and our harness
  passes 1432 / 1432.

Caveats. **The double scan of a perfect array is still unmeasured**: there F1 and F2 read the array twice and our split
loop once. No benchmark corpus feeds this loop a perfect array: every english `StringState` and `SharedPrefixState`
array fails the length test (our harness's check J1), chinese fails at element 0, and the one perfect array in the
matrix, chinesenames under the rank coder, runs the unchanged pinyin-rank loop. On the four arrays we counted (english
and chinese at 200,000 and 1,000,000) the exactness scan stops by element 2; on the others we did not record where it
stops, and on perfect data its cost is not known. **F3 does not fit.** It keeps rev's loop and adds an exactness
scan after it that never runs on these arrays, because the length test has already failed, so it executes what rev
executes. Yet it reads 1.034 / 1.044 of old on masking, slower than old on every fork at 1,000,000 in both rounds, where
rev reads 0.998 / 0.991. Our untested reading: the cost follows how C2 compiles the whole method.

### 2. Is the line-50 revert worth the hours?

**Yes, and it changed the fix.** Reverting line 50 alone removes the chinese slowdown: rev recovers 0.779 of the chinese
`huskyEncodeOnly` new − old at 200,000, and at 1,000,000, where new − old is established in round 2 only, rev/old is
0.979 (share 1.463, or 1.497 / 1.430 by round; `req13-gate.txt`). Before run 1 we expected otherwise, because our patch
took the call out of the bulk loop and recovered nothing on chinese; that reasoning did not hold.

- chinese `huskyEncodeOnly`, pooled: new/old 1.076 / 1.046 at 200,000 / 1,000,000, every new fork above every old fork
  at 200,000 (at 1,000,000 in round 2 only); rev/old 1.017 / 0.979, its forks overlapping old's at both n.
- The run-1 gate held the re-run if, at either n, new/old ≥ 1.02 and (new − rev)/(new − old) > 0.5. Both n met it, so
  the unit stopped as designed: the fix we had prepared was the patch, which does not undo what line 50 does to chinese.
- On english masking rev recovers 0.912 / 1.006 at 200,000 / 1,000,000, so request 12's attribution to `f43ff52` is now
  a measurement.
- `UNICODE_CODER` has no override, so line 50 runs the base `exactlyEncodable`, in which `CharSequence::length` stays a
  virtual call and `perfectForLength` gets `low call site frequency`, at 32,000 (run 1) as at 1,000,000 (request 12);
  old inlines both.
- The hours: run 1's A/B took 2 h 40 min of JMH time.

Caveats. Why the patch, whose bulk loop is also call-free, recovers nothing on chinese while F1, F2 and F3 all pass
there (F/old 0.976–1.015 in the chinese key cells), we have not shown. At 32,000 chinese stays slower with line 50
reverted too (rev/old 1.032 in run 1, 1.054 in run 2; F1/old 1.038; forks overlap when pooled): reverting line 50 does
not remove it, and the residual itself is not established. Across sessions the chinese encode-only row still reads
1.068 / 1.028 / 1.049 of 11cd at 32,000 / 200,000 / 1,000,000 (request 12: 1.065 / 1.069 / 1.073), three weeks apart,
against String chinese baselines at 1.008 (0.993–1.035) and the old jar's own A/B means at 0.998–1.043 of its 11cd
scores. We cannot explain the 32,000 residual, nor the part of 1,000,000 the old jar does not share; for the fix we
trust the same-session A/B.

### 3. What is your instinct on the saturating/masking asymmetry?

**Your guess fits part of what we measured.** Under `-XX:LoopUnrollLimit=0` the masking gap at 1,000,000 falls by almost
half, from 12.152 to 6.644 ms, with new still slower on every fork, while saturating stays at 0.975–0.995 of its no-flag
time at 200,000 and 1,000,000. At 32,000 PrintInlining shows the same out-of-line call as at 1,000,000, yet masking pays
nothing there. That refutes the idea we started with, that C2 inlines the override at 32,000. It does not support yours,
that 32,000 is too short to reach the optimised state: both C2 compiles happen there, and the flag slows old masking at
32,000 to 1.341 of its no-flag time (new masking 1.442). What remains is a hypothesis; we stopped because shipped code
no longer has the penalty.

- **Unrolling** (`req13-probe-lu0-*.json`, one round, 3 forks per jar; appendix, Table R1-c). The flag slows old masking
  to 1.661 / 1.611 of its no-flag time at 200,000 / 1,000,000 and new masking to 1.503 / 1.494, so the gap falls from
  2.502 to 0.842 ms and from 12.152 to 6.644 ms. That fits "a transformation that needs unrolling is worth more to old
  masking than to new". But the flag changes every loop in the JVM, so it cannot name the transformation. We expected
  old masking to slow to new's level and the gap to mostly close; under the flag old masking (157.694 ± 4.241 ms at
  1,000,000) is far slower than new without it (110.014 pooled), and more than half the gap remains.
- **32,000** (`req13-probe-diag-*.json`, logs outside the repo; appendix, Table I-1). In the new jar the english
  override `exactlyEncodable (25 bytes)` gets `low call site frequency` in both C2 compiles of `huskyEncode` for both
  english coders, 4 lines at 32,000 as at 1,000,000 in request 12; the old jar has no such call. Yet masking at 32,000
  shows no penalty (new/old 0.995, forks overlap) against 1.124 at 1,000,000. The same inlining decision costs nothing
  on the small array; we did not disassemble the loop.
- **A hypothesis we have not tested:** the lost optimisation matters only when the loop waits on memory. Masking takes
  about half as long per element at 32,000 as at 1,000,000 (old: 1.583 ms for 32,000 words, 97.863 ms for 1,000,000), as
  if the small array were served from cache. Testing it needs hardware counters, and this host has no `perf`.
- Item 55's four unexplained facts now read: saturating's smaller cost is consistent with unrolling but not shown; the
  32,000 non-result has the same inlining as 1,000,000, on smaller data; the partial recovery was our patch's shape,
  since F1 recovers it all; and the chinese slowdown is line 50 at 200,000 (at 1,000,000 its means reproduce in both
  rounds, its forks in one), while reverting line 50 does not remove its 32,000 part.

### 4. How much needs re-running, and when?

**We took your first option: the fix, then the 173 rows whose timed path changed plus 32 baselines in the same
invocations, 205 rows in 5 invocations and 9 h 56 min of JMH time. The other 377 request-12 rows stand.** english
String rows got faster (0.944 normalised). ParallelString english did not (1.013 normalised), and the other groups read
normalised medians of 0.985–1.009.

- **The row set.** The fix changes `BaseHuskySequenceCoder.huskyEncode(X[])`, which 170 rows run: 90 in
  `StringSortBenchmarks` (42 english, 42 chinese, and the 6 chinesenames rows of the two english coders), 48 in
  `ParallelStringSortBenchmarks` (english and chinese) and Adversarial's 32 `sharedPrefix*` husky rows, which name
  `englishCoder`. With the 3 english `msdStringSort` rows (next item) that is 173. Your set, 141 rows by our count,
  contains the 3 chinesenames `huskyEncodeOnly` rows, which run the pinyin-rank loop and do not change, and misses the
  32 Adversarial rows and the 3 MSD rows. The 32 baselines are the `systemSort`, `systemSortParallel`,
  `multikeyQuicksort` and `sharedPrefixSystemSort` rows of the same classes and corpora, so that every husky ÷ baseline
  ratio comes from one invocation and the drift since request 12 is measured.
- **The rest of the jar.** `git log d8958bf..52b0894 -- src/main src/jmh` lists 23 commits: the fix and your 22 since
  the request-12 jar, none in `src/jmh`. By our reading of each, only `8b4639b` (the MSD cutoff, 14 → 15) changes a
  timed path, reaching the 3 english `msdStringSort` rows only; the others add composite-coder classes no benchmark
  loads, refactor size tests to identical decisions, rename `perfectForLength` with its body unchanged (`508351d`),
  change Javadoc, or fix sorts no benchmark calls (`MergeHuskySort`, `HuskyBucketSort`). `msdStringSort` reads 0.977† /
  1.026† / 1.024† of request 12, ‡ in both runs: our 1–2 % estimate is not resolvable.
- **What stands on request 12, and why** (377 rows). Numeric (123), Permit (42), Tuple (18), ParallelRadix (14) and Date
  (6): none runs the changed loop, and the 22 commits leave their timed paths unchanged. Adversarial's 84
  `collapsedBits*` rows: no `BaseHuskySequenceCoder` on their path. The other 84 chinesenames rows of the two string
  classes: 60 are husky rows of the rank coder, whose pinyin-rank loop we left unchanged as decided (its check is a
  private static method, every chinesenames name is exact, and request 12 measured no penalty there), and the other 24
  run neither changed loop. The 6 english and chinese `insertionSort` rows: no coder.
- **What the re-run shows** ("Re-run vs request 12"): baselines 1.013 (the host was 1.3 % slower), english String husky
  rows 0.957 (0.944 normalised, 20 of 45 faster with disjoint CIs, none slower), 8 chinese husky rows at 200,000 slower
  with disjoint CIs (1.018–1.048) that we have not explained, and english `radixHuskySortAuto` 3.31× / 6.41× faster
  than `Arrays.sort` at 200,000 / 1,000,000 in one invocation (request 12: 3.08× / 5.75×).

Caveats. No re-run ratio meets the two-methods rule (5 to 18 methods per invocation with a baseline), and fix ÷ req12
spans two sessions two weeks apart, hence the baselines first. 29 of the 205 rows are ‡.

### 5. Is the shortfall attribution solid enough to publish?

**We would still not publish the 35.4 % / 53.8 %: they are shares of a penalty the shipped code no longer has. The
measured answer is now direct: the fix closes 56.5 % / 73.5 % of request 12's shortfall at 200,000 / 1,000,000 in raw
ms, 67.5 % / 88.4 % after the english baseline drift.**

english `radixHuskySortAuto`, ms/op (`req13b-rerun-StringSortBenchmarks-english.json` against
`doc/req12-full-StringSortBenchmarks.json` and `doc/req11cd-full-StringSortBenchmarks.json`; gain = 1 − X ÷ 11cd; your
prediction is 11cd minus the 11cd encode saving), then the same gains with each run divided by `Arrays.sort` in its own
invocation (ratio = `radixHuskySortAuto` ÷ `systemSort`). Shortfalls, shares and predicted gains use your prediction as
you gave it (3.35 / 45.43 / 202.44 ms), as our request-12 report did. With the rule values in parentheses, the 32,000
predicted gain is 26.4 % and the drift-corrected share at 200,000 is 67.4 %; the other gains and shares round the same.

| n | 11cd | your prediction (rule value) | req12 | fix | predicted gain | req12 gain | fix gain | shortfall req12 − pred (ms) | shortfall fix − pred (ms) | share of req12 shortfall closed (req12 − fix)/(req12 − pred) | same, fix ÷ baseline drift |
|---:|---|---|---|---|---|---|---|---:|---:|---:|---:|
| 32,000 | 4.556 ± 0.248‡ | 3.35 (3.354) | 4.069 ± 0.174‡ | 4.094 ± 0.140‡ | 26.5 % | 10.7 % | 10.1 % | 0.719 | 0.744 | -0.035 | 0.041 |
| 200,000 | 62.146 ± 0.958 | 45.43 (45.426) | 51.224 ± 0.968 | 47.949 ± 0.996 | 26.9 % | 17.6 % | 22.8 % | 5.794 | 2.519 | 0.565 | 0.675 |
| 1,000,000 | 279.839 ± 4.303 | 202.44 (202.441) | 220.857 ± 2.819 | 207.322 ± 2.580 | 27.7 % | 21.1 % | 25.9 % | 18.417 | 4.882 | 0.735 | 0.884 |

| n | 11cd ratio | req12 ratio | fix ratio | predicted gain | req12 gain | fix gain |
|---:|---:|---:|---:|---|---|---|
| 32,000 | 0.302 | 0.274 | 0.264 | 26.5 % | 9.2 % | 12.6 % |
| 200,000 | 0.398 | 0.325 | 0.302 | 26.9 % | 18.3 % | 24.1 % |
| 1,000,000 | 0.216 | 0.174 | 0.156 | 27.7 % | 19.5 % | 27.8 % |

- The fix gains 22.8 % / 25.9 % in ms. That is the low end of the range, since the english String baselines also ran
  slower, at a median 1.023 (1.013–1.048) of 11cd. With each run divided by `Arrays.sort` in its own invocation it gains
  24.1 % / 27.8 %, which reaches your prediction at 1,000,000; but those invocations ran 18 methods (fix) and 21 (11cd,
  req12), so the two-methods rule is not met, and the ratio absorbs the 1.016 / 1.026 that `Arrays.sort` itself moved
  since 11cd. What is left of the shortfall is 2.519 / 4.882 ms.
- The masking encode-only row is back at 11cd (0.998† / 0.987†), so the remainder is not the encode penalty. The cleanup
  cost of masking, which your rule leaves out, is a candidate we did not measure in this sort.
- The drift correction assumes that `radixHuskySortAuto` drifted like `Arrays.sort`, multikey and `Arrays.parallelSort`
  (median 1.013), so quote the raw share and the corrected one as the other end of a range. At 32,000 every
  `radixHuskySortAuto` row is ‡; on iterations 6–10 the fix is 4.6 % faster than 11cd.

Wording we would use: "With the masking coder, english `radixHuskySortAuto` is 22.8–24.1 % / 25.9–27.8 % faster at
200,000 / 1,000,000 than with the saturating coder (the lower figure in ms across two sessions, the higher as a ratio to
`Arrays.sort` within each invocation), against 26.9 % / 27.7 % predicted from the encode saving alone."

## What we ran

| label | source | sha256 (first 12) | used in |
|---|---|---|---|
| old | `d947e77`, the 11cd code (request 12's rebuild) | `5f088118169f` | run 1 and 2 A/B |
| new | `d8958bf`, request 12's jar | `0f9e5d4a4e57` | run 1 A/B and probes |
| rev | `d8958bf` with only line 50 reverted to the length test; diagnostic, item 48's wrong answers return | `ef3375bb3872` | run 1 and 2 A/B (control) |
| patch | `d8958bf` plus our request-12 split loop (request 12's patched jar) | `0a72b68388aa` | run 1 A/B |
| F1 | `d8958bf` plus your hoist | `028f61a2987e` | run 2 A/B |
| F2 | `d8958bf` plus the mirror: encode first, then test to the first failure | `a7fe2d710385` | run 2 A/B |
| F3 | `d8958bf` with rev's loop and the exactness test after it (ours) | `6af261ddee04` | run 2 A/B |
| fix | `52b0894` = `28005b7` plus your hoist; same `src/` tree as this PR's commit | `46bb30fc687c` | run 2 re-run (all 205 rows) |
| `549976a` | `28005b7` plus our split loop, run 1's prepared fix | `8baf277b6fdd` | built, not timed (run 1 held) |
| `0848a37` | `28005b7` plus F2 | `7a3b7264b371` | built, not timed |
| `886c578` | `28005b7` plus F3 | `e8f53ab8fb3f` | built, not timed |

Every jar's full sha256 is in `req13-env-before.txt` and `req13b-env-before.txt` (section "jars"), and the first line of
each JMH log names the jar it ran. F1 at `d8958bf` and the fix at `28005b7` add and remove the same lines, so the fix
jar differs from F1's by your 22 commits since `d8958bf`. This PR's commit replaces `52b0894` with the same `src/` tree
(`5ade192`).

- **A/B, both runs.** `StringSortBenchmarks.huskyEncodeOnlyEnglishMasking` and `huskyEncodeOnlyEnglishSaturating` on
  english (E), `huskyEncodeOnly` and `radixHuskySortAuto` on chinese (C), at n = 32,000 / 200,000 / 1,000,000,
  `-r 2s -w 2s -f 3 -wi 5 -i 10`, one invocation per arm and corpus, round 2 in reverse order. Run 1: old, new, rev and
  patch, 16 invocations; run 2: old, rev, F1, F2 and F3, 20 invocations.
- **Probes, run 1.** PrintCompilation and PrintInlining at n = 32,000, one fork and one iteration, on old and new, E and
  C (scores not used); and `-XX:LoopUnrollLimit=0` on old and new, english, all three n, 3 forks, one round.
- **Re-run, run 2.** The 205 rows in 5 invocations (String english 54 rows of 18 methods, chinese 51 of 17, chinesenames
  6 of 2, ParallelString 54 of 9, Adversarial 40 of 5) with request 12's `-f 5 -wi 5 -w 2s -i 10 -r 2s`.
- **Units.** Each run was one detached systemd user unit in `app.slice` (no CPU quota), with a 120 s settle and an idle
  guard (load1 < 2, swap < 1 GiB, no other benchmark JVM, fewer than 3 busy `MainThread` processes) before every
  invocation. Run 1, `husky-req13-20261017-0459`, Fri 2026-10-16 21:59 PDT (10-17 04:59Z) to Sat 01:47 (08:47Z): A/B 2 h
  40 min of JMH time, probes 23 min. Run 2, `husky-req13b-20261018-0718`, Sun 2026-10-18 00:18 PDT (07:18Z) to 14:26
  (21:26Z): A/B 3 h 20 min of JMH time, the gate at 04:19, the re-run from 04:21, 9 h 56 min of JMH time.
- **Host and load.** The request-12 host (aarch64, ARM Model 1 r1p1, 16 CPUs, one thread per core, 30 Gi RAM), kernel
  `6.12.111-136.204.amzn2023.aarch64`, Corretto 21.0.12+8-LTS, Maven 3.9.16, JMH 1.37, both runs in one boot. The
  `ForkJoinPool` probe printed `15 16` before and after both runs. Compressed swap held 880.2 MiB / 929.1 MiB at the
  start of run 1 / run 2, under the guard's 1 GiB, so we left it, and it changed by −15 / +9 MiB. Every guard passed on
  its first poll but one (the english re-run, second poll). load1 had a median of 1.32 / 1.29 in the A/B windows (max
  2.16 / 3.26); its maxima of 11.43–14.10 in three re-run invocations are their own 16-thread rows. The non-benchmark
  processes together passed one core in 2 of 160 run-1 and 7 of 200 run-2 A/B samples, and 0–6 per re-run invocation.
- **JSON checks.** 22 / 22 run-1 files and 25 / 25 run-2 files pass our validator (exact row set, Cnt = forks
  × iterations, `rawData` shape, score = mean of `rawData`, one JVM and JMH version), and the 5 re-run files hold
  exactly the 205 planned rows, none absent and none twice. Every row matches its JMH log to 3 decimals, and all 47 logs
  name the recorded jar.
- **Tests and harness.** `mvn -B test`: 465 tests and 0 failures for each of F1, F2 and F3 on `d8958bf`, 548 and 0 for
  each fix commit on `28005b7`. rev: 465 tests, 2 failures, both item-48 tests in `HuskyCoderFactoryTest`, as expected.
  Our harness passed 1432 / 1432 against each fix jar, after renaming `perfectForLength` to `couldBeExactAtLength` in 3
  places for `508351d`. The benchmark list (120 benchmarks) is identical across new, F1–F3 and the fix jars.
- **Equivalence.** The probe (8 coders × 29 cases, 232 result lines per jar) gives byte-identical codes and `perfect`
  flags for new against F1, F2 and F3, for our run-1 fix commit `549976a` against the three fix commits, and for new
  against `549976a`. As a control, rev differs from new in 50 lines, all in the `perfect` flag and none in a code.
- **Two-methods rule.** Met for every A/B figure (questions 1 to 3 and both gates; two methods per invocation). Not met
  for any re-run figure (questions 4 and 5, and question 2's cross-session figures): those invocations ran 18 (String
  english), 17 (String chinese), 9 (ParallelString) and 5 (Adversarial) methods, request 12's 21, 10 and 11.

## Run 1 results

Pooled ms/op and X/old (`req13-ab-*.json`, 6 forks per arm). Recovery fractions, the gate's numbers and the
`LoopUnrollLimit=0` table are in the appendix.

| row | n | old | new | rev | patch | new/old | rev/old | patch/old | new vs old | rev vs old | patch vs old |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|---|---|
| english `huskyEncodeOnlyEnglishMasking` | 32,000 | 1.583 | 1.575 | 1.551 | 1.601 | 0.995 | 0.980 | 1.011 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| english `huskyEncodeOnlyEnglishMasking` | 200,000 | 18.490 | 20.992 | 18.711 | 19.518 | 1.135 | 1.012 | 1.056 | > / > / > | > / ~ / ~ | > / > / > |
| english `huskyEncodeOnlyEnglishMasking` | 1,000,000 | 97.863 | 110.014 | 97.788 | 101.953 | 1.124 | 0.999 | 1.042 | > / > / > | ~ / ~ / ~ | > / > / > |
| english `huskyEncodeOnlyEnglishSaturating` | 32,000 | 2.816 | 2.948 | 2.695 | 3.052 | 1.047 | 0.957 | 1.084 | ~ / ~ / ~ | ~ / ~ / ~ | > / ~ / ~ |
| english `huskyEncodeOnlyEnglishSaturating` | 200,000 | 35.033 | 35.542 | 34.536 | 36.020 | 1.015 | 0.986 | 1.028 | ~ / ~ / ~ | ~ / ~ / ~ | > / > / > |
| english `huskyEncodeOnlyEnglishSaturating` | 1,000,000 | 179.679 | 181.112 | 179.103 | 183.038 | 1.008 | 0.997 | 1.019 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| chinese `huskyEncodeOnly` | 32,000 | 0.482 | 0.518 | 0.497 | 0.540 | 1.075 | 1.032 | 1.122 | ~ / > / ~ | ~ / ~ / ~ | > / > / > |
| chinese `huskyEncodeOnly` | 200,000 | 3.489 | 3.755 | 3.547 | 3.799 | 1.076 | 1.017 | 1.089 | > / > / > | ~ / ~ / ~ | > / > / > |
| chinese `huskyEncodeOnly` | 1,000,000 | 19.054 | 19.927 | 18.649 | 19.786 | 1.046 | 0.979 | 1.038 | ~ / > / ~ | ~ / ~ / ~ | > / ~ / ~ |
| chinese `radixHuskySortAuto` | 32,000 | 2.369 | 2.422 | 2.316 | 2.344 | 1.022 | 0.978 | 0.989 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| chinese `radixHuskySortAuto` | 200,000 | 14.829 | 14.733 | 14.795 | 15.120 | 0.994 | 0.998 | 1.020 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| chinese `radixHuskySortAuto` | 1,000,000 | 70.973 | 70.797 | 70.357 | 70.866 | 0.998 | 0.991 | 0.998 | < / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |

Recovery (new − X)/(new − old) where every fork establishes new − old: patch 0.589 / 0.663 and rev 0.912 / 1.006 on
masking at 200,000 / 1,000,000, patch −0.166 and rev 0.779 on chinese `huskyEncodeOnly` at 200,000. All 9 ‡ cells of run
1 are at 32,000.

## Run 2 results

Pooled X/old and fork tests (`req13b-ab-*.json`; K = gate key cell, ✗ = key cell above 1.025):

| row | n | K | old (ms) | rev/old | F1/old | F2/old | F3/old | rev vs old | F1 vs old | F2 vs old | F3 vs old |
|---|---:|---|---:|---:|---:|---:|---:|---|---|---|---|
| english `huskyEncodeOnlyEnglishMasking` | 32,000 |  | 1.554 | 1.018 | 0.989 | 1.009 | 0.997 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| english `huskyEncodeOnlyEnglishMasking` | 200,000 | K | 18.407 | 0.998 | 0.975 | 0.971 | 1.034 ✗ | ~ / ~ / ~ | < / ~ / ~ | ~ / < / ~ | > / > / ~ |
| english `huskyEncodeOnlyEnglishMasking` | 1,000,000 | K | 97.929 | 0.991 | 0.977 | 0.987 | 1.044 ✗ | ~ / ~ / ~ | ~ / < / ~ | ~ / ~ / ~ | > / > / > |
| english `huskyEncodeOnlyEnglishSaturating` | 32,000 |  | 2.933 | 1.057 | 0.959 | 1.004 | 0.976 | > / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ | > / ~ / ~ |
| english `huskyEncodeOnlyEnglishSaturating` | 200,000 | K | 34.846 | 1.001 | 0.984 | 0.981 | 0.984 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ | < / ~ / ~ |
| english `huskyEncodeOnlyEnglishSaturating` | 1,000,000 | K | 179.325 | 1.010 | 0.982 | 0.972 | 0.987 | ~ / ~ / ~ | ~ / < / ~ | ~ / < / ~ | ~ / ~ / ~ |
| chinese `huskyEncodeOnly` | 32,000 |  | 0.477 | 1.054 | 1.038 | 1.031 | 1.044 | ~ / > / ~ | ~ / > / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| chinese `huskyEncodeOnly` | 200,000 | K | 3.602 | 0.974 | 0.976 | 1.012 | 1.003 | ~ / < / ~ | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| chinese `huskyEncodeOnly` | 1,000,000 | K | 18.853 | 0.997 | 1.010 | 1.015 | 1.012 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| chinese `radixHuskySortAuto` | 32,000 |  | 2.320 | 1.017 | 1.010 | 1.025 | 1.017 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |
| chinese `radixHuskySortAuto` | 200,000 |  | 15.114 | 0.995 | 0.966 | 0.964 | 0.967 | ~ / ~ / ~ | ~ / < / ~ | ~ / < / ~ | ~ / ~ / ~ |
| chinese `radixHuskySortAuto` | 1,000,000 |  | 70.547 | 1.000 | 0.994 | 0.986 | 0.979 | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ | ~ / ~ / ~ |

Old and rev ran in both sessions: run 2 ÷ run 1 has a median of 1.002 over the 24 arm-cells (0.982–1.032 at n
≥ 200,000), and the fork ranges overlap in 24 / 24 (appendix, Table R2-c). Run 2's 8 ‡ cells are english at 32,000.

## Re-run vs request 12

**Baselines first: host drift**, fix ÷ req12 (`req13b-rerun-*.json` against `doc/req12-full-*.json`):

| class | corpus | rows | median | min–max | CIs disjoint: fix faster / slower |
|---|---|---:|---:|---|---|
| Adversarial | - | 8 | 1.020 | 1.010–1.040 | 0 / 3 |
| ParallelString | chinese | 3 | 0.997 | 0.993–1.018 | 0 / 0 |
| ParallelString | english | 3 | 0.995 | 0.943–1.029 | 1 / 1 |
| String | chinese | 9 | 1.010 | 0.998–1.022 | 0 / 3 |
| String | english | 9 | 1.013 | 0.943–1.047 | 0 / 4 |

All 32 read a median of 1.013 (0.943–1.047). Every husky ratio below carries this drift; "norm" divides a ratio by the
median baseline ratio of its class and corpus (chinesenames ran no baseline and is divided by String chinese, marked *).

**Husky rows by class and corpus:**

| class | corpus | rows | fix ÷ req12 median (min–max) | baseline drift | norm median (min–max) | CIs disjoint faster / slower |
|---|---|---:|---|---:|---|---|
| Adversarial | - | 32 | 1.013 (0.972–1.039) | 1.020 | 0.994 (0.953–1.019) | 0 / 11 |
| ParallelString | chinese | 24 | 1.007 (0.977–1.048) | 0.997 | 1.009 (0.980–1.050) | 0 / 4 |
| ParallelString | english | 24 | 1.007 (0.827–1.085) | 0.995 | 1.013 (0.831–1.091) | 4 / 1 |
| String | chinese | 42 | 0.995 (0.938–1.048) | 1.010 | 0.985 (0.929–1.038) | 11 / 5 |
| String | chinesenames | 6 | 1.009 (0.994–1.020) | 1.010* | 0.999 (0.984–1.010) | 0 / 0 |
| String | english | 45 | 0.957 (0.857–1.045) | 1.013 | 0.944 (0.845–1.031) | 20 / 0 |

- **english String.** 20 of 45 rows faster with disjoint CIs, none slower. The `radixHuskySort*` rows read 0.924–0.961
  normalised at 200,000 and 1,000,000, the masking encode 0.884–0.887. The saturating encode, which runs the same loop
  but paid no measurable penalty in run 1, did not move (0.988–1.016 normalised).
- **chinese.** String: 11 rows faster (5 of them encode-only rows, which time the changed loop) and 5 slower with
  disjoint CIs; ParallelString: 0 faster, 4 slower. 8 of the 9 slower rows are at 200,000, at 1.018–1.048 (String
  `radixHuskySort8`, `11`, `12`, `13`; ParallelString `parallelRadixHuskySort11_p8`, `parallelRadixHuskySortAuto_p4`,
  `_p8`, `_pAll`), where the chinese baselines read 0.993–1.018; we have not explained them.
- **Adversarial.** 1.013 raw, 0.994 normalised. Its 11 rows slower with disjoint CIs are each within the class's largest
  baseline ratio (1.040), and the in-invocation husky ÷ `sharedPrefixSystemSort` ratios moved by per-method medians of
  0.990–0.996.
- The appendix has the encode-only and MSD rows across 11cd, req12 and fix, and every re-run row.

**Husky ÷ baseline inside one invocation, the ratios the paper quotes** (below 1 = husky faster; ‡ = an unconverged
numerator or denominator; request 12's ratios from its whole-class invocations, the fix's from the re-run):

| class | husky | baseline | corpus | n (pL) | req12 ratio | fix ratio | speed-up req12 → fix (×) |
|---|---|---|---|---|---:|---:|---|
| String | `radixHuskySortAuto` | `systemSort` | english | n=200,000 | 0.325 | 0.302 | 3.08 → 3.31 |
| String | `radixHuskySortAuto` | `systemSortParallel` | english | n=200,000 | 4.378 | 4.076 | 0.23 → 0.25 |
| String | `radixHuskySortAuto` | `systemSort` | english | n=1,000,000 | 0.174 | 0.156 | 5.75 → 6.41 |
| String | `radixHuskySortAuto` | `systemSortParallel` | english | n=1,000,000 | 2.225 | 2.214 | 0.45 → 0.45 |
| String | `radixHuskySortAuto` | `systemSort` | chinese | n=200,000 | 0.175 | 0.173 | 5.73 → 5.78 |
| String | `radixHuskySortAuto` | `systemSortParallel` | chinese | n=200,000 | 2.172 | 2.179 | 0.46 → 0.46 |
| String | `radixHuskySortAuto` | `systemSort` | chinese | n=1,000,000 | 0.133 | 0.132 | 7.54 → 7.59 |
| String | `radixHuskySortAuto` | `systemSortParallel` | chinese | n=1,000,000 | 1.505 | 1.531 | 0.66 → 0.65 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | english | n=200,000 | 0.829 | 0.862 | 1.21 → 1.16 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | english | n=1,000,000 | 0.591‡ | 0.611‡ | 1.69 → 1.64 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | chinese | n=200,000 | 0.905 | 0.907 | 1.11 → 1.10 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | chinese | n=1,000,000 | 0.676 | 0.670 | 1.48 → 1.49 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=200,000, pL=0 | 0.301 | 0.300 | 3.33 → 3.34 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=0 | 0.179 | 0.167 | 5.59 → 5.98 |

## Convergence

29 of the 205 re-run rows are ‡ and 2 are §; 26 of the 29 were ‡ in request 12 too, and no row that was ‡ there lost the
flag. The ‡ rows are 14 english ParallelString rows (every multi-threaded arm at 200,000 and 1,000,000 but
`pAll_parCleanup` at 200,000, and three rows at 32,000) and 15 english String rows (eight radix rows at 32,000,
`radixHuskySort13` at 200,000, `msdStringSort` at every n, and three rows new to the list, all at 32,000). The 2 § rows
are ParallelString `_p2` and `_p4` on chinese at 32,000. Of the 42 rows a headline could draw on (String
`radixHuskySortAuto`, `huskyEncodeOnlyEnglishMasking`, `huskyEncodeOnly`, `systemSort` and `systemSortParallel`, and
ParallelString `pAll`, `pAll_parCleanup` and `systemSortParallel`, each on english and chinese, plus Adversarial's
pL = 0 husky rows and baseline, all at 200,000 and 1,000,000), only english `pAll` at both n and `pAll_parCleanup` at
1,000,000 are flagged, so this run supports no english claim of `pAll`, nor of `pAll_parCleanup` at 1,000,000, against
`Arrays.parallelSort`.
The A/B ‡ cells are all at 32,000, none a gate key cell. The appendix lists every flagged row with its iterations 6–10.

## What the fix changes in the code

All of `52b0894` against `28005b7` (this PR's commit makes the same change): one method,
`BaseHuskySequenceCoder.huskyEncode(X[])`, 8 lines added and 5 removed.

```diff
--- a/src/main/java/edu/neu/coe/huskySort/sort/huskySortUtils/BaseHuskySequenceCoder.java
+++ b/src/main/java/edu/neu/coe/huskySort/sort/huskySortUtils/BaseHuskySequenceCoder.java
@@ -45,13 +45,16 @@ public abstract class BaseHuskySequenceCoder<X extends CharSequence> implements
      */
     @Override
     public Coding huskyEncode(final X[] xs) {
+        // Test exactness before encoding, stopping at the first failure, so the encode loop carries no test.
         boolean isPerfect = true;
-        final long[] result = new long[xs.length];
-        for (int i = 0; i < xs.length; i++) {
-            final X x = xs[i];
-            if (isPerfect) isPerfect = exactlyEncodable(x);
-            result[i] = huskyEncode(x);
+        for (final X x : xs) {
+            if (!exactlyEncodable(x)) {
+                isPerfect = false;
+                break;
+            }
         }
+        final long[] result = new long[xs.length];
+        for (int i = 0; i < xs.length; i++) result[i] = huskyEncode(xs[i]);
         return new Coding(result, isPerfect);
     }
 
```

The exactness test now runs to the first failure before anything is encoded, so the encode loop holds no exactness test.
Item 48's answer holds: `perfect` is true exactly when every element is exactly encodable, as before (probe, tests and
harness above). The pinyin-rank loop, `HuskyCoderChinesePinyin.huskyEncode(String[])`, is unchanged.

## Files

All under `doc/`: byte-identical copies (`cmp`, sha256) of the JMH `-rff` output, gate reports and env snapshots.

| file | sha256 | what |
|---|---|---|
| `req13-ab-r1-old-E.json` | `974a18144f66de62eada11e1afdff6de9c39c89b0e327ebc010f2b82f5f3ba34` | run 1 A/B round 1, old (`d947e77`), english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r1-old-C.json` | `f2611cef5b67fc0d37faba833be187e32ac8a1a54d1a3b2117fdeabdaed821eb` | run 1 A/B round 1, old (`d947e77`), chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-ab-r1-new-E.json` | `c4ff9df6c0330f2f432ad2c6ed5318bb14e6832b24b79a99095eab5e1b48b494` | run 1 A/B round 1, new (`d8958bf`), english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r1-new-C.json` | `8b6bc3111463434155fdbfd130102032f2b2df7cfc548515c3539c23fffeaebe` | run 1 A/B round 1, new (`d8958bf`), chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-ab-r1-rev-E.json` | `718aeca127dc6656e78637f48d2c59c9189ce564036b40c9a9fe158a4afd4cf7` | run 1 A/B round 1, rev, english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r1-rev-C.json` | `b37baa49847501e65fa74d9c1c06454415378d00918357d480271df1d5f216cc` | run 1 A/B round 1, rev, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-ab-r1-patch-E.json` | `2678036d2d39b6cc31f40259a01c2374cf3891d022274728adad7741ad7db1ce` | run 1 A/B round 1, patch, english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r1-patch-C.json` | `6534d1a9c60c7006ed06f40ad152c81759280b9a2d01924c2d2dfecad8ef7b4a` | run 1 A/B round 1, patch, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-ab-r2-old-E.json` | `4be5fd00dd44f11af09738ff1a5d7360d7dea1172b1cfd17ef7c597259084f94` | run 1 A/B round 2, old (`d947e77`), english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r2-old-C.json` | `d5087ddfd70db7a68d90604a53fb6451df3e587a9fae80fb3121284c030c2961` | run 1 A/B round 2, old (`d947e77`), chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-ab-r2-new-E.json` | `8424ed6f70674b1bdff0f4df0c9cf8b9e97822766f0ebf67504eed34309fa47a` | run 1 A/B round 2, new (`d8958bf`), english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r2-new-C.json` | `c70e4e037b1582f47e6a00e5f111e29194714578f32c4322e2a5b752dfb68319` | run 1 A/B round 2, new (`d8958bf`), chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-ab-r2-rev-E.json` | `6f22b32817bd77dc2d5e6d211a758012db6ffc34d723c3ced8770b33d3d7791e` | run 1 A/B round 2, rev, english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r2-rev-C.json` | `d2a500b378354b520765d93f64b971c52931523836d41c4129be7861b861a7f9` | run 1 A/B round 2, rev, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-ab-r2-patch-E.json` | `fae39cfd3acff92467b30fd205c3d18c3188763a7e54d881d7e531905bb7eeb2` | run 1 A/B round 2, patch, english masking + saturating, 6 rows, 3 forks |
| `req13-ab-r2-patch-C.json` | `6d35ced86e35172db8575b453a84cd66c3aa049d43af900432e37b1859e17230` | run 1 A/B round 2, patch, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13-probe-diag-old-E.json` | `26c5f87be58496db54aca07c6d5f3df1382b0b4680af2a7cbe987e7db595cd39` | run 1 probe, PrintCompilation + PrintInlining, old (`d947e77`), english, n = 32,000, 2 rows, 1 fork × 1 iteration (score not used) |
| `req13-probe-diag-old-C.json` | `cc517286106157ef58a88c3fa3a565e097298ec4927e007f70a1e09a1266cf60` | run 1 probe, PrintCompilation + PrintInlining, old (`d947e77`), chinese, n = 32,000, 1 row, 1 fork × 1 iteration (score not used) |
| `req13-probe-diag-new-E.json` | `8e2c904975afdfc0a526c6a4e5cfc78e7e1076353af2949e24a024a769db03bd` | run 1 probe, PrintCompilation + PrintInlining, new (`d8958bf`), english, n = 32,000, 2 rows, 1 fork × 1 iteration (score not used) |
| `req13-probe-diag-new-C.json` | `5cfc0b57e86432f2805a647b3c806eafbadde389c9033af1c25341359c41b963` | run 1 probe, PrintCompilation + PrintInlining, new (`d8958bf`), chinese, n = 32,000, 1 row, 1 fork × 1 iteration (score not used) |
| `req13-probe-lu0-old-E.json` | `b6cebb471665cf5193ddea251e40c77a5503cf8e9a45bbd826aecb5c95acb705` | run 1 probe, `-XX:LoopUnrollLimit=0`, old (`d947e77`), english, 6 rows, 3 forks |
| `req13-probe-lu0-new-E.json` | `fbb0dbea04b7510b43aaa5b83ff93138d3a4a76ba065f7293a5581195837b883` | run 1 probe, `-XX:LoopUnrollLimit=0`, new (`d8958bf`), english, 6 rows, 3 forks |
| `req13-gate.txt` | `c11a325d2fb972c290fd8d60aae26b6c6d1cc69d8d91c90122d87a855d11d745` | run 1 gate report: validation, pooled numbers, the rule and `VERDICT HOLD` |
| `req13-env-before.txt` | `c1c83b93820d39282d985c9d70ce125131900b4ccdf7c3e3d39ffa7ba494017a` | run 1 unit: `date`, `uptime`, `lscpu`, `free -h`, swap, `uname -r`, `java -version`, `mvn -v`, cgroup, git HEAD, the jars' full sha256, plan and guard |
| `req13b-ab-r1-old-E.json` | `016192aa515750a7cf1ae79102379e498549c126a4a43ecf9772207fdc7786e4` | run 2 A/B round 1, old (`d947e77`), english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r1-old-C.json` | `916cb7a117f9cfac558d82abb975471a34f3262eb662c30b50a46a9893477168` | run 2 A/B round 1, old (`d947e77`), chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r1-rev-E.json` | `feb59bacb91f1658d3188334491f55ad4de5e9603216f586baa5d81ed61bd9d5` | run 2 A/B round 1, rev, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r1-rev-C.json` | `492828dea75d47105e4570af17ffd96370ed89b27dc974a7e5cc9d38cff1c9e0` | run 2 A/B round 1, rev, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r1-F1-E.json` | `5ec59651ad8b3f46ae8b02476306164de68cc5bf22254932fd4b0ac6b0628038` | run 2 A/B round 1, F1, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r1-F1-C.json` | `30235c6053888ea88cc4d9784d6e589409de322d5b4d6bb3218fe4c7a815f2aa` | run 2 A/B round 1, F1, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r1-F2-E.json` | `a50db0cfeb5c6d1a1178c67627fbe860ee7cab416e6fe04b2019c3c98caf1003` | run 2 A/B round 1, F2, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r1-F2-C.json` | `d868b0c8ce65c579b9c94fbc3f8b99f722f6f777173a9f22cab58ccf0a210539` | run 2 A/B round 1, F2, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r1-F3-E.json` | `172a9e5d72afacd85d77603ef2eae6bde6ac7d2138415b3565499b01d014e515` | run 2 A/B round 1, F3, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r1-F3-C.json` | `5d03c0156e21acd56435314c8ad41e201301ce5fc3047aab00787ab326936008` | run 2 A/B round 1, F3, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r2-old-E.json` | `ba9ca67576dca77ea4bc555f2abed44cce778e42446899b75440ec8186b9edd4` | run 2 A/B round 2, old (`d947e77`), english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r2-old-C.json` | `40bc5a830cef8c191b23a66427a6697c4ac6e06e2987566015cdff2dfcc6b5c2` | run 2 A/B round 2, old (`d947e77`), chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r2-rev-E.json` | `30f83b2d837e610db0d170fcc628f6a8f7f2bff97bcf181eaa18fed2c45bb867` | run 2 A/B round 2, rev, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r2-rev-C.json` | `24fae9ffa81aa57f882e551540a02985787b0948ebba9a0d1d3e8ea4436f9b01` | run 2 A/B round 2, rev, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r2-F1-E.json` | `0bada0e26bba4aaca6c63a06e417795c30ee09079e886447242f0543421a8b32` | run 2 A/B round 2, F1, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r2-F1-C.json` | `6fedfa7a280a49eaa3f2e9d4090a2621a279b180ea1b3101ab85f50a745c92a0` | run 2 A/B round 2, F1, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r2-F2-E.json` | `346895d23319951dc5014ab9649253ac5aa88bbb0bf67a98f05de3e53ef48b41` | run 2 A/B round 2, F2, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r2-F2-C.json` | `44015ccac3c1b3f5ee8bc80aead4266191d9114dda590ed5b9efbe735331ed8e` | run 2 A/B round 2, F2, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-ab-r2-F3-E.json` | `d641334ba7ed876fd730f187df8724bf463892b07f0f0ea0fcb71dd15dac5c27` | run 2 A/B round 2, F3, english masking + saturating, 6 rows, 3 forks |
| `req13b-ab-r2-F3-C.json` | `d292ae73c2ade9ae92d2c0948e46884498ed0b1b487cddbbdefc438d9fdbd947` | run 2 A/B round 2, F3, chinese `huskyEncodeOnly` + `radixHuskySortAuto`, 6 rows, 3 forks |
| `req13b-rerun-StringSortBenchmarks-english.json` | `52462a606e2251ff8fe092beaa4827795bb19b36fb7eeecddf4d036b24808d4b` | run 2 re-run, fix jar, `StringSortBenchmarks-english`, 54 rows, 18 methods, 5 forks |
| `req13b-rerun-StringSortBenchmarks-chinese.json` | `2f69d2032a842f1121f3393f7122040852667122403115145ddc616ea09a1d67` | run 2 re-run, fix jar, `StringSortBenchmarks-chinese`, 51 rows, 17 methods, 5 forks |
| `req13b-rerun-StringSortBenchmarks-chinesenames.json` | `a14e639c399e504308295d3c4105bf6fb7c489f279d5023774518e0f23fee737` | run 2 re-run, fix jar, `StringSortBenchmarks-chinesenames`, 6 rows, 2 methods, 5 forks |
| `req13b-rerun-ParallelStringSortBenchmarks.json` | `3abc2bd957e21a74839d503873bf56c1032e3263c7116b81dbfb8f3ee1472e19` | run 2 re-run, fix jar, `ParallelStringSortBenchmarks`, 54 rows, 9 methods, 5 forks |
| `req13b-rerun-AdversarialSortBenchmarks.json` | `3ab774031c1e3d04d7af4e341197debc65bb0553e3eefdb92a7fcfcd4a49f9d5` | run 2 re-run, fix jar, `AdversarialSortBenchmarks`, 40 rows, 5 methods, 5 forks |
| `req13b-gate.txt` | `b188f5b39c779bec3e8d11aa0d84ef59195447a5e3e9dbfbafff56d857165304` | run 2 gate report: validation, pooled numbers, the rule and `choice=F1`, `verdict=PASS` |
| `req13b-env-before.txt` | `59c25bac1a4bdc4415464e24151311a6e29c81d11e8bed654874fa00a39604ed` | run 2 unit: the same fields, the fix branches and commits, the jars' full sha256 and the chosen jar |

Plus this file, its appendix, the request 13 row in `doc/Run request for Yunlu.md` and the change above. The jars,
runner, JMH and PrintInlining logs, equivalence probe and harness are outside the repo, available on request.
