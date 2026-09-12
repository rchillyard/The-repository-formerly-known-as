# Run results from Yunlu — the full suite, 2026-09-03

This completes **request 4** of `doc/Run request for Yunlu.md`, and with it all five requests. Raw
output committed alongside: **`doc/full-suite.json`** — 409 result rows, every one at Cnt = 50
(5 forks × 10 iterations), no null scores.

```
java -jar target/benchmarks.jar -f 5 -wi 5 -i 10 -r 2s -w 2s -rf json -rff full-suite.json
```

Commit `d3c359f`, same pinned environment as requests 3 and 5 (Corretto 21.0.12+8-LTS, Maven 3.9.16,
kernel 6.12.100 — verbatim outputs in the 2026-09-02 doc's appendix). Wall clock **20 h 30 m**
(2026-09-02 21:03 UTC → 2026-09-03 17:33 UTC), 22 h 29 m CPU. Ran unattended as a systemd unit; no
other workloads scheduled alongside it.

## Coverage

| class | rows | feeds |
|---|---:|---|
| NumericSortBenchmarks | 120 | `HSComp` (integer/long/double/BigInteger/BigDecimal × 8 sorts × 3 sizes) |
| StringSortBenchmarks | 111 | `HSComp`, `RadixImprovements` (english + chinese + chinesenames) |
| AdversarialSortBenchmarks | 121 | the adversarial appendix |
| TupleSortBenchmarks | 18 | `HSComp`, `RadixImprovements` |
| PermitSortBenchmarks | 21 | (same as request 3, re-measured within the suite) |
| ParallelRadixSortBenchmarks | 12 | `ParallelRadix` |
| DateSortBenchmarks | 6 | `RadixImprovements` dates row |

**Two kinds of hole, both explainable, nothing else missing:**

1. `msdStringSort` × Chinese corpora — refused by design (`IllegalStateException`, as the request
   predicted); those combinations are simply absent from the JSON.
2. `collapsedBitsDualPivotQuicksort` at (fixedHighBits=60, n=1M), (63, 200k), (63, 1M) — **all five
   forks died with `StackOverflowError`**. The adversarial input does exactly what it was built to do:
   collapsed high bits drive dual-pivot into pathological recursion depth. Arguably this is the
   appendix's strongest datapoint — on the paper's machine of record, the baseline does not merely
   slow down, it fails. (It survives fhb=60 at 200k: 267.9 ms/op, 6.5× its fhb=0 time.)

## Headline ratios for the tables being replaced (largest size each, ms/op)

**`HSComp` — vs the system sort** (which the 2017 MacBook currently backs):

| domain (n) | system | quickHusky | radix/16 | system ÷ radix/16 |
|---|---:|---:|---:|---:|
| integer (500k) | 123.678 | 87.923 | 25.688 | **4.81×** |
| long (500k) | 138.707 | 99.263 | 28.665 | **4.84×** |
| double (500k) | 144.540 | 93.500 | 31.471 | **4.59×** |
| BigDecimal (500k) | 264.082 | 149.109 | 53.264 | **4.96×** |
| Tuple (500k) | 212.833 | 149.632 | 59.988 | **3.55×** |
| String english (1M) | 1193.776 | 732.319 | 276.661 | **4.31×** |

**`RadixImprovements` — radix/16 over QuickHuskySort:**

| row (n) | ratio |
|---|---:|
| Tuple (500k) | **2.49×** |
| String english (1M) | **2.65×** |
| chinese (1M) | **3.26×** |
| chinesenames (1M) | 1.52× |
| permits (198.9k) | 2.19× (2.21× in the standalone request-3 run — consistent) |
| Dates (20k) | radix/11 0.731 vs system 3.826 = **5.23×**; vs dutchHuskySort 4.348 = 5.95× |

The Dates row: whatever mix the 4.5× claim was computed from, both candidate ratios comfortably
exceed it on this machine.

**`ParallelRadix`** (n = 10M): serial 943.4 → p1 887.6 → p2 745.3 → p4 685.4 → p8 634.1.
Parallel speedup tops out at **1.49× with 8 threads** — the scaling story is modest on 16 physical
cores, and worth wording carefully. Against QuickHuskySort (5994.0) the p8 figure is 9.45×.

**Adversarial appendix** (n = 1M): at fixedHighBits=0, radix sorts hold 73–77 against quickHusky 406
and system 480. At fixedHighBits=63 the ordering inverts: quickHusky 15.5 wins, radix/16 35.0,
system 43.3, dual-pivot dead by stack overflow. sharedPrefix at prefixLength=0: radix/16 235.7 vs
system 1328.2.

## One finding that deserves its own paragraph

**`chinesenames` is an adverse corpus for the Husky mechanism on this machine.** The system sort beats
every Husky variant at every size: at 1M, system 823.8 vs radix/16 960.2 (1.17× against us) and vs
QuickHuskySort 1458.5 (1.77× against us); the pattern holds at 32k and 200k. The encoding itself is
the likely culprit — `huskyEncodeOnly` costs 408.4 ms/op on chinesenames at 1M against 14.3 on the
`chinese` corpus, a ~29× difference for the coding pass alone. If Table `RadixImprovements` keeps a
chinesenames row sourced from this run, it will report a loss; the `chinese` corpus row, by contrast,
is the suite's best string result (3.26× over quickHusky, 6.58× over system).

## Cross-checks against requests 1–3 (same machine, different runs)

English @1M, this run vs the 2026-09-01 dedicated run: msd 264.4 vs 258.5, radix/16 276.7 vs 282.1,
multikey 616.5 vs 637.6, system 1193.8 vs 1166.2 — all within ~3%. MSD still ahead of radix/16 at 1M
in both runs, margin 1.05× here vs 1.09× there (intervals barely separate in this run). Direction is
stable; the thin 1M margin is genuinely thin. Permits within the suite reproduced the standalone run
to within interval noise (2.19× vs 2.21× over quickHusky).
