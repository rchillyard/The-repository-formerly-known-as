# Run results from Yunlu — all seven requests rerun, 2026-09-06

Requests 6 and 7 answered, and — since the commit moved and two of the fixes (HS-12, HS-13) change
code that earlier numbers were measured on — **requests 1 through 5 rerun at the same checkout**, so
every number below comes from one machine and one build. All seven raw JMH outputs are committed
alongside: the five familiar filenames are refreshed, `pinyin.json` and `adversarial.json` are new.

**Checkout**: branch tip `6dd4ef9`. One correction to the request doc: it names `5ed60a0` as the last
commit touching `src/`, but `e92610f` (HS-13, the recursion guard) touched `src/` after it —
**`e92610f` is the commit the paper should record**. Nothing under `src/` changed between `e92610f`
and the tip (verified with `git diff --stat`).

**Environment**: same instance and pins as before — c7g.4xlarge (Graviton3, Neoverse V1), Corretto
**21.0.12+8-LTS** (the OS auto-patched its JDK to 21.0.12.1 on 09-02; all runs use the pinned
original), Maven **3.9.16**, AL2023. New drift to record: the host rebooted on 09-03, so the kernel is
now **6.12.103** (09-02/03 runs: 6.12.100; August: 6.12.95), and swap is now 8 GiB zram + 32 GiB file
(0 B in use during runs). `mvn test` at the tip: **396 tests, 0 failures**. Runs were strictly
sequential in one detached unit; total wall clock ≈ 31.4 h.

## Correctness was checked before any timing was trusted

An external harness (reusing the repo's classes, replicating each benchmark's input generation with
its exact seeds) ran against the same jar — 37/37 checks passed:

- Every benchmarked sorter's output is **sorted under its intended comparator** and a **permutation of
  its input** (english, chinesenames, permits, adversarial, numeric Long, parallel).
- On chinesenames, `systemSort` (no comparator) output differs from pinyin order at
  **200,000 / 200,000 positions** (87,969 adjacent inversions under `NAME_ORDER`) — quantifying the
  request doc's point that it was solving a different, cheaper problem.
- `systemSortPinyin`, `quickHuskySort`, `radixHuskySort16` and `multikeyQuicksort.sortByPinyin`
  produce **byte-identical** output at n=200,000, confirming the "exactly comparable" claim.
- The guarded `PureDualPivotQuicksort` completes and sorts correctly at the previously-crashing
  fixedHighBits=60/63 shapes.

## Request 6 — chinesenames with the fair baseline (ms/op)

| n | systemSort (code-point) | systemSortPinyin (fair) | multikey (pinyin) | quickHusky | radix/16 | sysPinyin ÷ radix16 |
|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 10.5 ± 0.1 | 68.9 ± 0.9 | 35.7 ± 0.4 | 26.7 ± 0.3 | 22.7 ± 0.3 | **3.03×** |
| 200,000 | 103.7 ± 1.0 | 550.9 ± 3.0 | 283.9 ± 1.5 | 186.4 ± 1.3 | 152.2 ± 1.1 | **3.62×** |
| 1,000,000 | 799.8 ± 9.1 | 3119.9 ± 58.0 | 1596.9 ± 9.2 | 1266.8 ± 6.3 | 770.3 ± 17.9 | **4.05×** |

As you predicted: against the sort doing the same job, the corpus flips from the paper's weakest
result to its clearest demonstration — and the margin grows with n, which is the premise (pay the key
extraction once per element, not ~log n times per element in comparisons).

The HS-12 encoder cache is visible everywhere: `huskyEncodeOnly` @1M dropped **408.4 → 211.1** ms/op
(about half — please check that against your 5.7× per-character expectation; the pass includes array
traversal and allocation, so we would not expect the full 5.7× end-to-end), radix/16 @1M dropped
960 → 770, multikey-pinyin 3010 → 1597. With the cheaper encoding, radix/16 (770.3) now edges the
*unfair* code-point systemSort (799.8) at 1M — the mechanism pays for the pinyin work and still wins.

## Request 7 — adversarial with the guarded baseline

124/124 combinations, **zero crashes** (previously 3 combos died with StackOverflowError). All five
forks, every row Cnt=50. `collapsedBitsDualPivotQuicksort` @ n=1M:

| fixedHighBits | this run | previous (d3c359f) |
|---:|---:|---|
| 0 | 335.9 ± 6.8 | 335.7 |
| 48 | 333.3 ± 5.8 | 354.6 |
| 56 | **717.2 ± 2.4** | 7097.7 |
| 60 | 805.5 ± 28.0 | StackOverflowError |
| 63 | **186.9 ± 5.5** | StackOverflowError |

⚠ **This is your "if instead it is fast there, tell us" case — and it is worse than fast at 63; the
guard also rewrote 56.** The 21× degradation at fixedHighBits=56 is now **2.1×**, and at 63 the
baseline is *faster than its own fhb=0 case* (186.9 vs 335.9): the depth guard engages early on
duplicate-heavy partitions and heapsort handles them well, so the appendix's quadratic-degradation
story no longer appears in the data at 56–63. The partitioning pathology presumably still exists —
it is now masked by the fallback well before 64 levels of true pathological depth are reached.
The guard looks like it needs re-tuning (or the appendix rewording to "degrades, then the guard
caps it at heapsort cost"). Husky context at fhb=63 @1M: quickHusky 15.4, radix/16 34.9,
system 42.6 — unaffected either way.

## Requests 1–5 rerun at the same commit (deltas from the d3c359f run)

**1 — english baselines** (systemSort comfortably slowest at every size ✓):

| n | MSD | radix/16 | radix/11 | radix/8 | multikey | system |
|---:|---:|---:|---:|---:|---:|---:|
| 32,000 | 4.18 ± 0.05 | 4.22 ± 0.13 | 4.46 ± 0.18 | 4.63 ± 0.21 | 7.04 ± 0.03 | 13.27 ± 0.14 |
| 200,000 | 43.4 ± 1.2 | 50.6 ± 2.3 | 53.4 ± 2.3 | 58.4 ± 2.8 | 68.3 ± 0.8 | 140.7 ± 2.0 |
| 1,000,000 | 271.9 ± 8.4 | 271.5 ± 6.2 | 330.5 ± 23.1 | 351.4 ± 24.5 | 643.2 ± 7.4 | 1124.9 ± 15.9 |

Worth a sentence in the paper: at 1M, MSD vs radix/16 is a **1.00× dead heat** this run, after 1.09×
(09-01) and 1.05× (09-03 suite) — same source both times for the latter two. Across three runs the 1M
margin spans 1.00–1.09×; the robust MSD advantage is at 200,000 (here 1.17×).

**2 — distinct**: rx16÷MSD = 1.04× / 1.25× / 1.31× at 32k/200k/250k — direction unchanged; duplicates
still do not explain MSD's advantage.

**3 — permits**: radix/16 = **4.83×** over system, **2.32×** over quickHusky at the full corpus.
Cleanup-pass cost **9.5% / 33.6% / 18.6%** (was 9.8/25.3/19.0) — the non-monotonic, mid-peaking shape
reproduces; "a tenth to a third, peaking mid-range" is what two runs support.

**4 — full suite** (20 h 29 m; 415 rows; only by-design holes — msd × Chinese, systemSortPinyin ×
english/chinese; dual-pivot 14/14 with no crashes):
`HSComp` system÷radix/16: integer 4.80×, long 4.75×, double 4.58×, BigDecimal 5.00×, BigInteger
3.85×, Tuple 3.61×, String english 4.11×. `RadixImprovements` quickHusky÷radix/16: Tuple 2.46×,
english 2.56×, chinese 3.36×, **chinesenames 1.63× (a legitimate same-task row at last)**, permits
2.12×; Dates radix/11 5.17× over system. `ParallelRadix` @10M: serial 935 → p8 645 = 1.45×.
Rows shared with the dedicated runs agree within ~2% throughout.

**5 — crossover**: identical ladder and boundaries — system ≤ 20, insertion 50–200, quickHusky
500–2,000, radix/16 by 10,000; radix's flat ~167 µs setup floor unchanged.

## Files

`doc/pinyin.json`, `doc/adversarial.json` (new); `doc/english-baselines.json`,
`doc/english-distinct.json`, `doc/permits.json`, `doc/english-crossover.json`, `doc/full-suite.json`
(refreshed — they supersede the d3c359f versions merged via PR #63).
