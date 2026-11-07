# Run results from Yunlu 2026-10-18, appendix: request 13 tables

Every table here was cut from our analysis of the JSONs listed under Files in `Run results from Yunlu 2026-10-18.md`,
with each value rounded HALF_UP to 3 decimals and each ratio taken from unrounded values. The names (old, new, rev,
patch, F1, F2, F3, fix, 11cd, req12) and the marks († ‡ § and the fork tests `>` `<` `~`, as round 1 / round 2 / pooled)
are defined at the top of that document.

## Run 1: the A/B and the probes (`req13-ab-*.json`, `req13-probe-*.json`)

The pooled run-1 table (ms/op, X/old and fork tests) is in the main document, under Run 1 results.

### Table R1-b: recovery fractions (pooled)

Recovery of X = (new − X)/(new − old): 1 = X back at old, 0 = X no better than new. Only meaningful where new is slower
than old on every fork (column 4).

| row | n | new − old (ms) | new vs old (pooled forks) | patch: (new − patch)/(new − old) | rev: (new − rev)/(new − old) |
|---|---:|---:|---|---|---|
| english `huskyEncodeOnlyEnglishMasking` | 32,000 | -0.008 | ~ | n/a (new ≤ old) | n/a (new ≤ old) |
| english `huskyEncodeOnlyEnglishMasking` | 200,000 | 2.502 | > | 0.589 | 0.912 |
| english `huskyEncodeOnlyEnglishMasking` | 1,000,000 | 12.152 | > | 0.663 | 1.006 |
| english `huskyEncodeOnlyEnglishSaturating` | 32,000 | 0.132 | ~ | -0.793 (new − old not established) | 1.923 (new − old not established) |
| english `huskyEncodeOnlyEnglishSaturating` | 200,000 | 0.509 | ~ | -0.940 (new − old not established) | 1.976 (new − old not established) |
| english `huskyEncodeOnlyEnglishSaturating` | 1,000,000 | 1.434 | ~ | -1.343 (new − old not established) | 1.401 (new − old not established) |
| chinese `huskyEncodeOnly` | 32,000 | 0.036 | ~ | -0.611 (new − old not established) | 0.571 (new − old not established) |
| chinese `huskyEncodeOnly` | 200,000 | 0.266 | > | -0.166 | 0.779 |
| chinese `huskyEncodeOnly` | 1,000,000 | 0.874 | ~ | 0.162 (new − old not established) | 1.463 (new − old not established) |
| chinese `radixHuskySortAuto` | 32,000 | 0.053 | ~ | 1.471 (new − old not established) | 1.999 (new − old not established) |
| chinese `radixHuskySortAuto` | 200,000 | -0.095 | ~ | n/a (new ≤ old) | n/a (new ≤ old) |
| chinese `radixHuskySortAuto` | 1,000,000 | -0.176 | ~ | n/a (new ≤ old) | n/a (new ≤ old) |

### The run-1 gate, recomputed (`req13-gate.txt`)

HOLD if, at 1,000,000 or 200,000, chinese `huskyEncodeOnly` has pooled new/old of at least 1.02 and a share (new −
rev)/(new − old) above 0.5.

| n | pooled old | pooled new | pooled rev | new/old | share (new − rev)/(new − old) | condition |
|---:|---:|---:|---:|---:|---:|---|
| 1,000,000 | 19.054 | 19.927 | 18.649 | 1.046 | 1.463 | true |
| 200,000 | 3.489 | 3.755 | 3.547 | 1.076 | 0.779 | true |

The rule reads pooled means. On the forks, new − old is established at 200,000 in both rounds and at 1,000,000 in round 2
only, which is why Table R1-b marks the 1,000,000 share as not established; by round the share is 0.739 / 0.829 at
200,000 and 1.497 / 1.430 at 1,000,000.

### Table R1-c: `-XX:LoopUnrollLimit=0` (one round, old then new, 3 forks each)

"no flag" = the pooled A/B (6 forks); † = the two LU0 CIs overlap; ‡ = a fork with t > 1.10.

| row | n | old LU0 | new LU0 | new/old LU0 | new − old LU0 (ms) | new vs old forks LU0 | new/old no flag | new − old no flag (ms) | old LU0 ÷ old no flag | new LU0 ÷ new no flag |
|---|---:|---|---|---:|---:|---|---:|---:|---:|---:|
| english `huskyEncodeOnlyEnglishMasking` | 32,000 | 2.123 ± 0.089 | 2.270 ± 0.178‡ | 1.069† | 0.147 | ~ | 0.995 | -0.008 | 1.341 | 1.442 |
| english `huskyEncodeOnlyEnglishMasking` | 200,000 | 30.715 ± 0.816 | 31.557 ± 0.865 | 1.027† | 0.842 | ~ | 1.135 | 2.502 | 1.661 | 1.503 |
| english `huskyEncodeOnlyEnglishMasking` | 1,000,000 | 157.694 ± 4.241 | 164.337 ± 3.318 | 1.042† | 6.644 | > | 1.124 | 12.152 | 1.611 | 1.494 |
| english `huskyEncodeOnlyEnglishSaturating` | 32,000 | 2.713 ± 0.155‡ | 2.671 ± 0.175‡ | 0.985† | -0.042 | ~ | 1.047 | 0.132 | 0.963 | 0.906 |
| english `huskyEncodeOnlyEnglishSaturating` | 200,000 | 34.142 ± 0.631 | 35.084 ± 0.683 | 1.028† | 0.942 | ~ | 1.015 | 0.509 | 0.975 | 0.987 |
| english `huskyEncodeOnlyEnglishSaturating` | 1,000,000 | 178.742 ± 3.440 | 176.894 ± 3.906 | 0.990† | -1.849 | ~ | 1.008 | 1.434 | 0.995 | 0.977 |

### Table I-1: PrintInlining for the exactness test inside `huskyEncode(X[])`, n = 32,000 (run 1) against 1,000,000 (request 12)

Counts of lines in the exactness subtree of the `huskyEncode(X[])` body, C2 compiles only, from the one-fork logs of the
`req13-probe-diag-*` runs (32,000) and of request 12's diagnostics (1,000,000); the logs are outside the repo. The old
jar has no `exactlyEncodable` call: its test is `perfectForLength(x.length())`.

| n | jar | E/C | override exactlyEncodable (25 B) low call site frequency | override already compiled into a medium method | perfectForLength low call site frequency | length call low call site frequency / virtual call | inline (hot) | C2 lines in subtree |
|---|---|---|---:|---:|---:|---:|---:|---:|
| 32k | old | E | 0 | 0 | 0 | 0 | 15 | 15 |
| 32k | new | E | 4 | 1 | 0 | 0 | 5 | 10 |
| 32k | old | C | 0 | 0 | 0 | 0 | 12 | 12 |
| 32k | new | C | 0 | 0 | 4 | 4 | 4 | 12 |
| 1M | old | E | 0 | 0 | 0 | 0 | 12 | 12 |
| 1M | new | E | 4 | 0 | 0 | 0 | 4 | 8 |
| 1M | old | C | 0 | 0 | 0 | 0 | 6 | 6 |
| 1M | new | C | 0 | 0 | 2 | 2 | 2 | 6 |

- 32,000, new, english: both C2 compiles of `huskyEncode` (OSR and normal) keep the override out of line for each coder,
  and the C2 compile of the benchmark method that inlines `huskyEncode` reports the masking override
  `already compiled into a medium method`, still a call. The old jar inlines `String::length` and `perfectForLength`
  (`inline (hot)`) in every C2 compile at both n.
- 32,000, new, chinese (`UNICODE_CODER`, no override): `CharSequence::length` is a `virtual call` and `perfectForLength`
  gets `low call site frequency` in each C2 compile, as request 12 found at 1,000,000.

Run-1 ‡ cells (fork t > 1.10), all at n = 32,000: patch `huskyEncodeOnlyEnglishSaturating` (rounds 1 and 2), rev
`huskyEncodeOnlyEnglishMasking` (rounds 1 and 2), rev `huskyEncodeOnlyEnglishSaturating` (rounds 1 and 2), LU0 old
`huskyEncodeOnlyEnglishSaturating`, LU0 new `huskyEncodeOnlyEnglishMasking` and `huskyEncodeOnlyEnglishSaturating`.

## Run 2: the A/B of the three shapes (`req13b-ab-*.json`)

### The run-2 gate, recomputed (`req13b-gate.txt`)

A shape passes if pooled X/old ≤ 1.025 in all 6 key cells (K in the run-2 table of the main document); among passing
shapes the lowest geometric mean of X/old wins, and if the best two are within 1 % the order is F1, F3, F2. The control,
rev, must pass or the gate holds.

| shape | key cells passing | geomean of X/old over the 6 K cells | verdict |
|---|---|---:|---|
| rev (control) | 6/6 | 0.995 | pass |
| F1 | 6/6 | 0.984 | pass |
| F2 | 6/6 | 0.990 | pass |
| F3 | 4/6 | 1.010 | fail |

Best two: F1 0.984 and F2 0.990, F2 ÷ F1 = 1.006, within 1 %, so F1.

### Table R2-c: old and rev, run 1 against run 2 (same jars, a day apart)

Pooled means over 6 forks each; fork-mean range min–max; "overlap" = the two 6-fork ranges overlap.

| row | n | arm | run 1 | run 2 | run 2 / run 1 | run 1 forks | run 2 forks | ranges |
|---|---:|---|---:|---:|---:|---|---|---|
| english `huskyEncodeOnlyEnglishMasking` | 32,000 | old | 1.583 | 1.554 | 0.982 | 1.495–1.697 | 1.443–1.640 | overlap |
| english `huskyEncodeOnlyEnglishMasking` | 32,000 | rev | 1.551 | 1.581 | 1.019 | 1.451–1.677 | 1.546–1.608 | overlap |
| english `huskyEncodeOnlyEnglishMasking` | 200,000 | old | 18.490 | 18.407 | 0.996 | 18.104–18.720 | 18.018–18.812 | overlap |
| english `huskyEncodeOnlyEnglishMasking` | 200,000 | rev | 18.711 | 18.377 | 0.982 | 17.432–19.921 | 17.869–19.174 | overlap |
| english `huskyEncodeOnlyEnglishMasking` | 1,000,000 | old | 97.863 | 97.929 | 1.001 | 97.017–98.973 | 95.978–100.315 | overlap |
| english `huskyEncodeOnlyEnglishMasking` | 1,000,000 | rev | 97.788 | 97.041 | 0.992 | 95.993–99.361 | 95.034–101.594 | overlap |
| english `huskyEncodeOnlyEnglishSaturating` | 32,000 | old | 2.816 | 2.933 | 1.042 | 2.615–2.927 | 2.583–3.702 | overlap |
| english `huskyEncodeOnlyEnglishSaturating` | 32,000 | rev | 2.695 | 3.099 | 1.150 | 2.578–2.892 | 2.892–3.557 | overlap |
| english `huskyEncodeOnlyEnglishSaturating` | 200,000 | old | 35.033 | 34.846 | 0.995 | 33.827–35.578 | 34.255–35.434 | overlap |
| english `huskyEncodeOnlyEnglishSaturating` | 200,000 | rev | 34.536 | 34.866 | 1.010 | 33.895–35.903 | 34.035–35.762 | overlap |
| english `huskyEncodeOnlyEnglishSaturating` | 1,000,000 | old | 179.679 | 179.325 | 0.998 | 175.149–183.662 | 173.717–182.782 | overlap |
| english `huskyEncodeOnlyEnglishSaturating` | 1,000,000 | rev | 179.103 | 181.069 | 1.011 | 176.910–181.348 | 179.237–183.712 | overlap |
| chinese `huskyEncodeOnly` | 32,000 | old | 0.482 | 0.477 | 0.990 | 0.473–0.497 | 0.452–0.516 | overlap |
| chinese `huskyEncodeOnly` | 32,000 | rev | 0.497 | 0.503 | 1.010 | 0.485–0.512 | 0.495–0.512 | overlap |
| chinese `huskyEncodeOnly` | 200,000 | old | 3.489 | 3.602 | 1.032 | 3.337–3.580 | 3.407–3.727 | overlap |
| chinese `huskyEncodeOnly` | 200,000 | rev | 3.547 | 3.509 | 0.989 | 3.454–3.654 | 3.424–3.574 | overlap |
| chinese `huskyEncodeOnly` | 1,000,000 | old | 19.054 | 18.853 | 0.989 | 18.517–19.723 | 18.131–19.852 | overlap |
| chinese `huskyEncodeOnly` | 1,000,000 | rev | 18.649 | 18.798 | 1.008 | 18.114–18.980 | 17.951–19.917 | overlap |
| chinese `radixHuskySortAuto` | 32,000 | old | 2.369 | 2.320 | 0.979 | 2.283–2.552 | 2.245–2.503 | overlap |
| chinese `radixHuskySortAuto` | 32,000 | rev | 2.316 | 2.360 | 1.019 | 2.279–2.373 | 2.293–2.516 | overlap |
| chinese `radixHuskySortAuto` | 200,000 | old | 14.829 | 15.114 | 1.019 | 14.041–16.303 | 14.252–16.657 | overlap |
| chinese `radixHuskySortAuto` | 200,000 | rev | 14.795 | 15.040 | 1.017 | 14.461–15.113 | 14.381–16.232 | overlap |
| chinese `radixHuskySortAuto` | 1,000,000 | old | 70.973 | 70.547 | 0.994 | 69.159–72.635 | 68.758–72.463 | overlap |
| chinese `radixHuskySortAuto` | 1,000,000 | rev | 70.357 | 70.521 | 1.002 | 67.771–71.434 | 69.106–71.427 | overlap |

Run-2 ‡ cells, all english at n = 32,000: F1 `huskyEncodeOnlyEnglishMasking` and `huskyEncodeOnlyEnglishSaturating`
(rounds 1 and 2), F2 `huskyEncodeOnlyEnglishMasking` (round 1) and `huskyEncodeOnlyEnglishSaturating` (round 2), rev
`huskyEncodeOnlyEnglishSaturating` (round 1), old `huskyEncodeOnlyEnglishSaturating` (round 2).

## The re-run against request 12 (`req13b-rerun-*.json` against `doc/req12-full-*.json`)

205 rows, 5 forks × 10 iterations × 2 s, on the fix jar; request 12's rows from the `d8958bf` jar with the same
settings. Rows are keyed by (class, method, params). "norm" = fix ÷ req12 divided by the median baseline ratio of the
same class and corpus; chinesenames, which ran no baseline, is divided by String chinese and marked *.

The 32 baseline rows are in the last table (reason `baseline`); their summary by class and corpus is in the main
document, under Re-run vs request 12.

### Table 4-c: encode-only rows and `msdStringSort`, 11cd → req12 → fix (ms/op)

11cd = `doc/req11cd-full-StringSortBenchmarks.json` (`d947e77`). `huskyEncodeOnly` on english changed coder between 11cd
and req12 (saturating → masking), so its 11cd column is not comparable (marked c).

| method | corpus | n | 11cd | req12 | fix | req12 ÷ 11cd | fix ÷ 11cd | fix ÷ req12 | fix ÷ req12 norm |
|---|---|---:|---:|---|---|---:|---:|---:|---:|
| `huskyEncodeOnly` | chinese | 32,000 | 0.478 | 0.509 ± 0.006 | 0.510 ± 0.004 | 1.065 | 1.068 | 1.003† | 0.993 |
| `huskyEncodeOnly` | chinese | 200,000 | 3.488 | 3.727 ± 0.046 | 3.587 ± 0.054 | 1.069 | 1.028 | 0.962 | 0.953 |
| `huskyEncodeOnly` | chinese | 1,000,000 | 18.267 | 19.597 ± 0.343 | 19.164 ± 0.118 | 1.073 | 1.049 | 0.978† | 0.968 |
| `huskyEncodeOnly` | english | 32,000 | 2.749c | 1.750 ± 0.047 | 1.499 ± 0.043‡ | 0.636c | 0.545c | 0.857 | 0.845 |
| `huskyEncodeOnly` | english | 200,000 | 35.249c | 21.217 ± 0.333 | 18.442 ± 0.235 | 0.602c | 0.523c | 0.869 | 0.858 |
| `huskyEncodeOnly` | english | 1,000,000 | 177.937c | 109.132 ± 1.471 | 97.994 ± 1.094 | 0.613c | 0.551c | 0.898 | 0.886 |
| `huskyEncodeOnlyEnglishMasking` | chinese | 32,000 | 0.874 | 0.908 ± 0.006 | 0.868 ± 0.004 | 1.039 | 0.993 | 0.956 | 0.946 |
| `huskyEncodeOnlyEnglishMasking` | chinese | 200,000 | 6.089 | 6.322 ± 0.068 | 5.992 ± 0.057 | 1.038 | 0.984 | 0.948 | 0.939 |
| `huskyEncodeOnlyEnglishMasking` | chinese | 1,000,000 | 30.954 | 31.912 ± 0.171 | 31.192 ± 0.394 | 1.031 | 1.008 | 0.977 | 0.968 |
| `huskyEncodeOnlyEnglishMasking` | chinesenames | 32,000 | 0.857 | 0.858 ± 0.011 | 0.866 ± 0.011 | 1.002 | 1.012 | 1.010† | 1.000* |
| `huskyEncodeOnlyEnglishMasking` | chinesenames | 200,000 | 13.126 | 12.681 ± 0.199 | 12.914 ± 0.161 | 0.966 | 0.984 | 1.018† | 1.008* |
| `huskyEncodeOnlyEnglishMasking` | chinesenames | 1,000,000 | 68.941 | 68.539 ± 0.377 | 69.145 ± 0.354 | 0.994 | 1.003 | 1.009† | 0.999* |
| `huskyEncodeOnlyEnglishMasking` | english | 32,000 | 1.540 | 1.551 ± 0.022 | 1.584 ± 0.038 | 1.007 | 1.029 | 1.021† | 1.008 |
| `huskyEncodeOnlyEnglishMasking` | english | 200,000 | 18.498 | 20.547 ± 0.367 | 18.468 ± 0.261 | 1.111 | 0.998 | 0.899 | 0.887 |
| `huskyEncodeOnlyEnglishMasking` | english | 1,000,000 | 98.077 | 107.987 ± 1.202 | 96.758 ± 1.041 | 1.101 | 0.987 | 0.896 | 0.884 |
| `huskyEncodeOnlyEnglishSaturating` | chinese | 32,000 | 1.107 | 1.232 ± 0.009 | 1.208 ± 0.019 | 1.112 | 1.091 | 0.981† | 0.971 |
| `huskyEncodeOnlyEnglishSaturating` | chinese | 200,000 | 8.679 | 9.121 ± 0.091 | 8.735 ± 0.056 | 1.051 | 1.006 | 0.958 | 0.948 |
| `huskyEncodeOnlyEnglishSaturating` | chinese | 1,000,000 | 45.773 | 46.798 ± 0.502 | 46.540 ± 0.591 | 1.022 | 1.017 | 0.994† | 0.985 |
| `huskyEncodeOnlyEnglishSaturating` | chinesenames | 32,000 | 0.903 | 0.921 ± 0.008 | 0.915 ± 0.018 | 1.020 | 1.014 | 0.994† | 0.984* |
| `huskyEncodeOnlyEnglishSaturating` | chinesenames | 200,000 | 13.207 | 13.025 ± 0.200 | 13.289 ± 0.117 | 0.986 | 1.006 | 1.020† | 1.010* |
| `huskyEncodeOnlyEnglishSaturating` | chinesenames | 1,000,000 | 70.148 | 70.334 ± 0.478 | 70.264 ± 0.354 | 1.003 | 1.002 | 0.999† | 0.989* |
| `huskyEncodeOnlyEnglishSaturating` | english | 32,000 | 2.742 | 2.798 ± 0.145 | 2.880 ± 0.210‡ | 1.020 | 1.050 | 1.029† | 1.016 |
| `huskyEncodeOnlyEnglishSaturating` | english | 200,000 | 35.218 | 34.622 ± 0.528 | 35.495 ± 0.367 | 0.983 | 1.008 | 1.025† | 1.012 |
| `huskyEncodeOnlyEnglishSaturating` | english | 1,000,000 | 175.475 | 180.004 ± 2.555 | 180.268 ± 1.659 | 1.026 | 1.027 | 1.001† | 0.988 |
| `msdStringSort` | english | 32,000 | 4.218 | 4.218 ± 0.144‡ | 4.120 ± 0.101‡ | 1.000 | 0.977 | 0.977† | 0.964 |
| `msdStringSort` | english | 200,000 | 58.782 | 60.084 ± 1.819‡ | 61.642 ± 2.621‡ | 1.022 | 1.049 | 1.026† | 1.012 |
| `msdStringSort` | english | 1,000,000 | 319.260 | 321.415 ± 19.633‡ | 328.986 ± 18.445‡ | 1.007 | 1.030 | 1.024† | 1.010 |

### The String baselines across the three sessions (ms/op)

| method | corpus | n | 11cd | req12 | fix | req12 ÷ 11cd | fix ÷ 11cd |
|---|---|---:|---:|---:|---:|---:|---:|
| `multikeyQuicksort` | chinese | 32,000 | 6.122 | 6.005 | 6.103 | 0.981 | 0.997 |
| `multikeyQuicksort` | chinese | 200,000 | 42.733 | 42.553 | 43.315 | 0.996 | 1.014 |
| `multikeyQuicksort` | chinese | 1,000,000 | 308.164 | 307.586 | 310.619 | 0.998 | 1.008 |
| `systemSort` | chinese | 32,000 | 10.743 | 10.800 | 10.858 | 1.005 | 1.011 |
| `systemSort` | chinese | 200,000 | 82.426 | 82.802 | 83.645 | 1.005 | 1.015 |
| `systemSort` | chinese | 1,000,000 | 522.223 | 529.009 | 540.413 | 1.013 | 1.035 |
| `systemSortParallel` | chinese | 32,000 | 3.289 | 3.282 | 3.296 | 0.998 | 1.002 |
| `systemSortParallel` | chinese | 200,000 | 6.689 | 6.655 | 6.641 | 0.995 | 0.993 |
| `systemSortParallel` | chinese | 1,000,000 | 46.619 | 46.611 | 46.522 | 1.000 | 0.998 |
| `multikeyQuicksort` | english | 32,000 | 7.983 | 7.991 | 8.085 | 1.001 | 1.013 |
| `multikeyQuicksort` | english | 200,000 | 81.839 | 79.973 | 83.701 | 0.977 | 1.023 |
| `multikeyQuicksort` | english | 1,000,000 | 747.621 | 739.699 | 769.159 | 0.989 | 1.029 |
| `systemSort` | english | 32,000 | 15.096 | 14.839 | 15.525 | 0.983 | 1.028 |
| `systemSort` | english | 200,000 | 156.327 | 157.808 | 158.809 | 1.009 | 1.016 |
| `systemSort` | english | 1,000,000 | 1293.911 | 1269.099 | 1327.939 | 0.981 | 1.026 |
| `systemSortParallel` | english | 32,000 | 4.864 | 4.882 | 4.948 | 1.004 | 1.017 |
| `systemSortParallel` | english | 200,000 | 11.225 | 11.700 | 11.765 | 1.042 | 1.048 |
| `systemSortParallel` | english | 1,000,000 | 91.832 | 99.273 | 93.652 | 1.081 | 1.020 |

### Table 4-d: husky ÷ baseline inside the same invocation, req12 and fix

Ratio < 1 = husky faster; ‡ = numerator or denominator unconverged in that run. req12: one invocation per class (String
21 methods, ParallelString 10, Adversarial 11); fix: String split by corpus (english 18 methods, chinese 17),
ParallelString 9, Adversarial 5. The two-methods rule is met in neither.

| class | husky | baseline | corpus | n (pL) | req12 ratio | fix ratio | fix ÷ req12 | speed-up req12 → fix (×) |
|---|---|---|---|---|---:|---:|---:|---|
| String | `radixHuskySortAuto` | `systemSort` | english | n=32,000 | 0.274‡ | 0.264‡ | 0.962 | 3.65 → 3.79 |
| String | `radixHuskySortAuto` | `systemSortParallel` | english | n=32,000 | 0.833‡ | 0.827‡ | 0.993 | 1.20 → 1.21 |
| String | `radixHuskySortAuto` | `systemSort` | english | n=200,000 | 0.325 | 0.302 | 0.930 | 3.08 → 3.31 |
| String | `radixHuskySortAuto` | `systemSortParallel` | english | n=200,000 | 4.378 | 4.076 | 0.931 | 0.23 → 0.25 |
| String | `radixHuskySortAuto` | `systemSort` | english | n=1,000,000 | 0.174 | 0.156 | 0.897 | 5.75 → 6.41 |
| String | `radixHuskySortAuto` | `systemSortParallel` | english | n=1,000,000 | 2.225 | 2.214 | 0.995 | 0.45 → 0.45 |
| String | `radixHuskySortAuto` | `systemSort` | chinese | n=32,000 | 0.219 | 0.211 | 0.963 | 4.57 → 4.75 |
| String | `radixHuskySortAuto` | `systemSortParallel` | chinese | n=32,000 | 0.720 | 0.694 | 0.964 | 1.39 → 1.44 |
| String | `radixHuskySortAuto` | `systemSort` | chinese | n=200,000 | 0.175 | 0.173 | 0.991 | 5.73 → 5.78 |
| String | `radixHuskySortAuto` | `systemSortParallel` | chinese | n=200,000 | 2.172 | 2.179 | 1.003 | 0.46 → 0.46 |
| String | `radixHuskySortAuto` | `systemSort` | chinese | n=1,000,000 | 0.133 | 0.132 | 0.994 | 7.54 → 7.59 |
| String | `radixHuskySortAuto` | `systemSortParallel` | chinese | n=1,000,000 | 1.505 | 1.531 | 1.017 | 0.66 → 0.65 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `systemSortParallel` | english | n=32,000 | 1.021 | 0.943 | 0.923 | 0.98 → 1.06 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | english | n=32,000 | 0.882‡ | 0.870‡ | 0.986 | 1.13 → 1.15 |
| ParallelString | `serialRadixHuskySortAuto` | `systemSortParallel` | english | n=32,000 | 0.867‡ | 0.697‡ | 0.804 | 1.15 → 1.43 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `systemSortParallel` | english | n=200,000 | 2.051‡ | 2.105‡ | 1.026 | 0.49 → 0.48 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | english | n=200,000 | 0.829 | 0.862 | 1.039 | 1.21 → 1.16 |
| ParallelString | `serialRadixHuskySortAuto` | `systemSortParallel` | english | n=200,000 | 4.495 | 4.226 | 0.940 | 0.22 → 0.24 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `systemSortParallel` | english | n=1,000,000 | 1.026‡ | 1.128‡ | 1.100 | 0.97 → 0.89 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | english | n=1,000,000 | 0.591‡ | 0.611‡ | 1.035 | 1.69 → 1.64 |
| ParallelString | `serialRadixHuskySortAuto` | `systemSortParallel` | english | n=1,000,000 | 2.266 | 2.316 | 1.022 | 0.44 → 0.43 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `systemSortParallel` | chinese | n=32,000 | 0.986 | 1.000 | 1.015 | 1.01 → 1.00 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | chinese | n=32,000 | 0.689 | 0.681 | 0.988 | 1.45 → 1.47 |
| ParallelString | `serialRadixHuskySortAuto` | `systemSortParallel` | chinese | n=32,000 | 0.713 | 0.737 | 1.034 | 1.40 → 1.36 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `systemSortParallel` | chinese | n=200,000 | 1.504 | 1.551 | 1.031 | 0.66 → 0.64 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | chinese | n=200,000 | 0.905 | 0.907 | 1.002 | 1.11 → 1.10 |
| ParallelString | `serialRadixHuskySortAuto` | `systemSortParallel` | chinese | n=200,000 | 2.192 | 2.243 | 1.023 | 0.46 → 0.45 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | `systemSortParallel` | chinese | n=1,000,000 | 0.907 | 0.870 | 0.959 | 1.10 → 1.15 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | `systemSortParallel` | chinese | n=1,000,000 | 0.676 | 0.670 | 0.991 | 1.48 → 1.49 |
| ParallelString | `serialRadixHuskySortAuto` | `systemSortParallel` | chinese | n=1,000,000 | 1.548 | 1.513 | 0.978 | 0.65 → 0.66 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=200,000, pL=0 | 0.456 | 0.451 | 0.990 | 2.19 → 2.22 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=200,000, pL=0 | 0.320 | 0.311 | 0.972 | 3.12 → 3.21 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=200,000, pL=0 | 0.310 | 0.299 | 0.964 | 3.22 → 3.35 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=200,000, pL=0 | 0.301 | 0.300 | 0.997 | 3.33 → 3.34 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=200,000, pL=10 | 1.036 | 1.039 | 1.003 | 0.97 → 0.96 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=200,000, pL=10 | 1.153 | 1.140 | 0.989 | 0.87 → 0.88 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=200,000, pL=10 | 1.129 | 1.124 | 0.996 | 0.89 → 0.89 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=200,000, pL=10 | 1.110 | 1.135 | 1.022 | 0.90 → 0.88 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=200,000, pL=20 | 1.032 | 1.039 | 1.007 | 0.97 → 0.96 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=200,000, pL=20 | 1.125 | 1.130 | 1.005 | 0.89 → 0.89 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=200,000, pL=20 | 1.104 | 1.117 | 1.012 | 0.91 → 0.90 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=200,000, pL=20 | 1.084 | 1.098 | 1.013 | 0.92 → 0.91 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=200,000, pL=40 | 1.007 | 1.008 | 1.002 | 0.99 → 0.99 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=200,000, pL=40 | 1.107 | 1.095 | 0.990 | 0.90 → 0.91 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=200,000, pL=40 | 1.089 | 1.080 | 0.992 | 0.92 → 0.93 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=200,000, pL=40 | 1.070 | 1.069 | 0.999 | 0.93 → 0.94 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=0 | 0.446 | 0.433 | 0.970 | 2.24 → 2.31 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=0 | 0.193 | 0.181 | 0.937 | 5.19 → 5.54 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=0 | 0.179 | 0.173 | 0.964 | 5.57 → 5.78 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=0 | 0.179 | 0.167 | 0.935 | 5.59 → 5.98 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=10 | 1.033 | 1.014 | 0.982 | 0.97 → 0.99 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=10 | 1.075 | 1.072 | 0.997 | 0.93 → 0.93 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=10 | 1.055 | 1.053 | 0.998 | 0.95 → 0.95 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=10 | 1.051 | 1.036 | 0.985 | 0.95 → 0.97 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=20 | 1.028 | 1.010 | 0.982 | 0.97 → 0.99 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=20 | 1.081 | 1.073 | 0.993 | 0.93 → 0.93 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=20 | 1.057 | 1.053 | 0.996 | 0.95 → 0.95 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=20 | 1.063 | 1.052 | 0.990 | 0.94 → 0.95 |
| Adversarial | `sharedPrefixQuickHuskySort` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=40 | 1.005 | 1.007 | 1.002 | 1.00 → 0.99 |
| Adversarial | `sharedPrefixRadixHuskySort8` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=40 | 1.073 | 1.063 | 0.991 | 0.93 → 0.94 |
| Adversarial | `sharedPrefixRadixHuskySort11` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=40 | 1.042 | 1.054 | 1.012 | 0.96 → 0.95 |
| Adversarial | `sharedPrefixRadixHuskySort16` | `sharedPrefixSystemSort` | - | n=1,000,000, pL=40 | 1.056 | 1.047 | 0.992 | 0.95 → 0.95 |

### Question 5: the masking encode-only row, and where the remaining shortfall sits

Table Q5-c: english `huskyEncodeOnlyEnglishMasking`, whose coder and benchmark body are the same in all three jars (†
= CIs overlap).

| n | 11cd | req12 | fix | req12 ÷ 11cd | fix ÷ 11cd | fix ÷ req12 | req12 − 11cd (ms) | fix − 11cd (ms) | share of the req12 slowdown removed (req12 − fix)/(req12 − 11cd) |
|---:|---|---|---|---:|---:|---:|---:|---:|---:|
| 32,000 | 1.540 ± 0.027 | 1.551 ± 0.022 | 1.584 ± 0.038 | 1.007† | 1.029† | 1.021† | 0.011 | 0.044 | -3.062 |
| 200,000 | 18.498 ± 0.331 | 20.547 ± 0.367 | 18.468 ± 0.261 | 1.111 | 0.998† | 0.899 | 2.049 | -0.031 | 1.015 |
| 1,000,000 | 98.077 ± 1.204 | 107.987 ± 1.202 | 96.758 ± 1.041 | 1.101 | 0.987† | 0.896 | 9.910 | -1.319 | 1.133 |

Table Q5-d: encode saving = `huskyEncodeOnlyEnglishSaturating` − `huskyEncodeOnlyEnglishMasking` in the same run; sort
saving = 11cd − fix `radixHuskySortAuto`.

| n | 11cd encode saving (your rule) | fix encode saving | sort saving 11cd − fix | sort saving ÷ 11cd encode saving | sort saving ÷ fix encode saving |
|---:|---:|---:|---:|---:|---:|
| 32,000 | 1.202 | 1.296 | 0.462 | 0.385 | 0.357 |
| 200,000 | 16.720 | 17.027 | 14.197 | 0.849 | 0.834 |
| 1,000,000 | 77.398 | 83.510 | 72.517 | 0.937 | 0.868 |

### Convergence of the re-run rows

t = mean(iterations 1–5) ÷ mean(6–10) per fork; ‡ = some fork above 1.10, § = every fork below 0.90; "iter 6–10 mean" is
the mean over forks of iterations 6–10.

| class | method | params | score (fix) | iter 6–10 mean | max t (min t) | req12 flag | req12 max t |
|---|---|---|---|---:|---|---|---:|
| ParallelString | `parallelRadixHuskySort11_p8` | corpus=english, n=32,000 | 4.091 ± 0.198‡ | 3.972 | 1.14 (0.92) | ‡ | 1.13 |
| ParallelString | `parallelRadixHuskySort11_p8` | corpus=english, n=200,000 | 24.967 ± 1.359‡ | 23.040 | 1.19 (1.13) | ‡ | 1.23 |
| ParallelString | `parallelRadixHuskySort11_p8` | corpus=english, n=1,000,000 | 104.237 ± 18.160‡ | 77.728 | 1.90 (1.44) | ‡ | 1.74 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | corpus=english, n=200,000 | 31.693 ± 1.944‡ | 29.553 | 1.22 (1.04) | ‡ | 1.21 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | corpus=english, n=1,000,000 | 216.863 ± 26.754‡ | 227.203 | 1.21 (0.83) | ‡ | 1.29 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | corpus=english, n=200,000 | 27.507 ± 1.713‡ | 25.823 | 1.23 (1.05) | ‡ | 1.24 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | corpus=english, n=1,000,000 | 108.782 ± 10.125‡ | 94.624 | 1.48 (1.13) | ‡ | 1.53 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | corpus=english, n=200,000 | 23.266 ± 1.454‡ | 21.269 | 1.22 (1.11) | ‡ | 1.22 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | corpus=english, n=1,000,000 | 109.521 ± 20.816‡ | 79.966 | 2.43 (1.36) | ‡ | 1.91 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | corpus=english, n=200,000 | 24.051 ± 1.277‡ | 22.038 | 1.19 (1.17) | ‡ | 1.22 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | corpus=english, n=1,000,000 | 101.161 ± 18.240‡ | 76.868 | 2.01 (1.16) | ‡ | 1.87 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | corpus=english, n=32,000 | 4.348 ± 0.353‡ | 4.130 | 1.33 (0.95) | ‡ | 1.28 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | corpus=english, n=1,000,000 | 54.809 ± 6.498‡ | 48.001 | 1.39 (1.12) | ‡ | 1.65 |
| ParallelString | `serialRadixHuskySortAuto` | corpus=english, n=32,000 | 3.483 ± 0.187‡ | 3.188 | 1.20 (1.14) | ‡ | 1.17 |
| String | `huskyEncodeOnly` | corpus=english, n=32,000 | 1.499 ± 0.043‡ | 1.458 | 1.10 (1.00) | — | 1.09 |
| String | `huskyEncodeOnlyEnglishSaturating` | corpus=english, n=32,000 | 2.880 ± 0.210‡ | 2.874 | 1.17 (0.89) | — | 1.09 |
| String | `msdStringSort` | corpus=english, n=32,000 | 4.120 ± 0.101‡ | 3.986 | 1.11 (1.04) | ‡ | 1.16 |
| String | `msdStringSort` | corpus=english, n=200,000 | 61.642 ± 2.621‡ | 59.617 | 1.21 (0.99) | ‡ | 1.17 |
| String | `msdStringSort` | corpus=english, n=1,000,000 | 328.986 ± 18.445‡ | 301.376 | 1.24 (1.11) | ‡ | 1.30 |
| String | `quickHuskySortPhase2CodesOnly` | corpus=english, n=32,000 | 4.115 ± 0.130‡ | 4.074 | 1.10 (0.94) | — | 1.07 |
| String | `radixHuskySort10` | corpus=english, n=32,000 | 4.171 ± 0.167‡ | 4.005 | 1.20 (0.96) | ‡ | 1.19 |
| String | `radixHuskySort11` | corpus=english, n=32,000 | 3.690 ± 0.208‡ | 3.362 | 1.22 (1.17) | ‡ | 1.24 |
| String | `radixHuskySort12` | corpus=english, n=32,000 | 3.845 ± 0.216‡ | 3.597 | 1.22 (1.03) | ‡ | 1.23 |
| String | `radixHuskySort13` | corpus=english, n=32,000 | 3.775 ± 0.240‡ | 3.552 | 1.22 (1.02) | ‡ | 1.26 |
| String | `radixHuskySort13` | corpus=english, n=200,000 | 45.361 ± 1.295‡ | 44.274 | 1.15 (1.02) | ‡ | 1.10 |
| String | `radixHuskySort14` | corpus=english, n=32,000 | 3.546 ± 0.195‡ | 3.248 | 1.24 (1.07) | ‡ | 1.22 |
| String | `radixHuskySort16` | corpus=english, n=32,000 | 3.521 ± 0.215‡ | 3.239 | 1.23 (1.09) | ‡ | 1.28 |
| String | `radixHuskySort8` | corpus=english, n=32,000 | 4.099 ± 0.218‡ | 3.748 | 1.20 (1.17) | ‡ | 1.19 |
| String | `radixHuskySortAuto` | corpus=english, n=32,000 | 4.094 ± 0.140‡ | 3.987 | 1.20 (0.99) | ‡ | 1.22 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | corpus=chinese, n=32,000 | 3.212 ± 0.189§ | 3.396 | 0.90 (0.88) | § | 0.89 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | corpus=chinese, n=32,000 | 3.201 ± 0.186§ | 3.396 | 0.89 (0.88) | — | 0.91 |

### Every re-run row, fix ÷ req12

reason: `encode` = the row runs `BaseHuskySequenceCoder.huskyEncode(X[])`; `sortcode:8b4639b` = the MSD cutoff change;
`baseline` = re-run for same-invocation ratios and drift.

| class | method | params | reason | req12 | fix | fix ÷ req12 | norm |
|---|---|---|---|---|---|---:|---:|
| Adversarial | `sharedPrefixSystemSort` | n=200,000, pL=0 | baseline | 172.214 ± 2.663 | 175.202 ± 2.281 | 1.017† | — |
| Adversarial | `sharedPrefixSystemSort` | n=200,000, pL=10 | baseline | 89.623 ± 1.175 | 91.124 ± 1.042 | 1.017† | — |
| Adversarial | `sharedPrefixSystemSort` | n=200,000, pL=20 | baseline | 95.360 ± 1.433 | 96.295 ± 1.339 | 1.010† | — |
| Adversarial | `sharedPrefixSystemSort` | n=200,000, pL=40 | baseline | 108.708 ± 1.553 | 111.075 ± 1.437 | 1.022† | — |
| Adversarial | `sharedPrefixSystemSort` | n=1,000,000, pL=0 | baseline | 1494.926 ± 22.258 | 1553.998 ± 20.799 | 1.040 | — |
| Adversarial | `sharedPrefixSystemSort` | n=1,000,000, pL=10 | baseline | 916.675 ± 11.126 | 939.688 ± 10.694 | 1.025 | — |
| Adversarial | `sharedPrefixSystemSort` | n=1,000,000, pL=20 | baseline | 928.168 ± 10.761 | 952.041 ± 11.429 | 1.026 | — |
| Adversarial | `sharedPrefixSystemSort` | n=1,000,000, pL=40 | baseline | 985.791 ± 13.030 | 997.102 ± 9.099 | 1.011† | — |
| Adversarial | `sharedPrefixQuickHuskySort` | n=200,000, pL=0 | encode | 78.485 ± 1.000 | 79.060 ± 1.021 | 1.007† | 0.988 |
| Adversarial | `sharedPrefixQuickHuskySort` | n=200,000, pL=10 | encode | 92.851 ± 0.796 | 94.696 ± 0.857 | 1.020 | 1.000 |
| Adversarial | `sharedPrefixQuickHuskySort` | n=200,000, pL=20 | encode | 98.385 ± 0.817 | 100.020 ± 0.816 | 1.017 | 0.997 |
| Adversarial | `sharedPrefixQuickHuskySort` | n=200,000, pL=40 | encode | 109.421 ± 1.245 | 111.976 ± 1.166 | 1.023 | 1.004 |
| Adversarial | `sharedPrefixQuickHuskySort` | n=1,000,000, pL=0 | encode | 666.911 ± 5.790 | 672.176 ± 5.418 | 1.008† | 0.989 |
| Adversarial | `sharedPrefixQuickHuskySort` | n=1,000,000, pL=10 | encode | 946.589 ± 7.692 | 953.052 ± 8.583 | 1.007† | 0.988 |
| Adversarial | `sharedPrefixQuickHuskySort` | n=1,000,000, pL=20 | encode | 954.521 ± 12.281 | 961.493 ± 12.442 | 1.007† | 0.988 |
| Adversarial | `sharedPrefixQuickHuskySort` | n=1,000,000, pL=40 | encode | 990.507 ± 9.841 | 1003.728 ± 8.441 | 1.013† | 0.994 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=200,000, pL=0 | encode | 53.411 ± 0.874 | 52.372 ± 1.027 | 0.981† | 0.962 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=200,000, pL=10 | encode | 101.179 ± 0.788 | 102.450 ± 0.864 | 1.013† | 0.993 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=200,000, pL=20 | encode | 105.233 ± 0.944 | 107.558 ± 0.833 | 1.022 | 1.002 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=200,000, pL=40 | encode | 118.337 ± 1.200 | 119.932 ± 0.890 | 1.013† | 0.994 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=1,000,000, pL=0 | encode | 268.205 ± 2.773 | 268.742 ± 2.667 | 1.002† | 0.983 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=1,000,000, pL=10 | encode | 966.935 ± 9.719 | 989.626 ± 9.366 | 1.023 | 1.004 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=1,000,000, pL=20 | encode | 981.353 ± 9.320 | 1002.722 ± 10.836 | 1.022 | 1.002 |
| Adversarial | `sharedPrefixRadixHuskySort11` | n=1,000,000, pL=40 | encode | 1027.176 ± 9.599 | 1050.968 ± 10.955 | 1.023 | 1.004 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=200,000, pL=0 | encode | 51.761 ± 1.022 | 52.512 ± 0.913 | 1.015† | 0.995 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=200,000, pL=10 | encode | 99.523 ± 0.897 | 103.390 ± 0.759 | 1.039 | 1.019 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=200,000, pL=20 | encode | 103.401 ± 1.044 | 105.749 ± 0.858 | 1.023 | 1.003 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=200,000, pL=40 | encode | 116.343 ± 1.118 | 118.766 ± 1.099 | 1.021 | 1.001 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=1,000,000, pL=0 | encode | 267.352 ± 7.161 | 259.748 ± 7.938 | 0.972† | 0.953 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=1,000,000, pL=10 | encode | 963.853 ± 13.644 | 973.506 ± 9.800 | 1.010† | 0.991 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=1,000,000, pL=20 | encode | 986.185 ± 12.497 | 1001.641 ± 9.937 | 1.016† | 0.996 |
| Adversarial | `sharedPrefixRadixHuskySort16` | n=1,000,000, pL=40 | encode | 1041.380 ± 12.214 | 1044.456 ± 9.231 | 1.003† | 0.984 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=200,000, pL=0 | encode | 55.180 ± 0.781 | 54.563 ± 0.777 | 0.989† | 0.970 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=200,000, pL=10 | encode | 103.305 ± 0.969 | 103.847 ± 0.762 | 1.005† | 0.986 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=200,000, pL=20 | encode | 107.233 ± 0.856 | 108.801 ± 0.848 | 1.015† | 0.995 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=200,000, pL=40 | encode | 120.328 ± 1.231 | 121.665 ± 1.160 | 1.011† | 0.992 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=1,000,000, pL=0 | encode | 288.116 ± 7.206 | 280.538 ± 6.277 | 0.974† | 0.955 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=1,000,000, pL=10 | encode | 985.306 ± 10.754 | 1007.216 ± 10.904 | 1.022 | 1.003 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=1,000,000, pL=20 | encode | 1003.163 ± 11.921 | 1021.405 ± 10.458 | 1.018† | 0.999 |
| Adversarial | `sharedPrefixRadixHuskySort8` | n=1,000,000, pL=40 | encode | 1058.204 ± 10.404 | 1060.196 ± 9.944 | 1.002† | 0.983 |
| ParallelString | `systemSortParallel` | chinese, n=32,000 | baseline | 3.279 ± 0.018 | 3.271 ± 0.016 | 0.997† | — |
| ParallelString | `systemSortParallel` | chinese, n=200,000 | baseline | 6.698 ± 0.038 | 6.653 ± 0.027 | 0.993† | — |
| ParallelString | `systemSortParallel` | chinese, n=1,000,000 | baseline | 45.878 ± 2.202 | 46.718 ± 2.270 | 1.018† | — |
| ParallelString | `systemSortParallel` | english, n=32,000 | baseline | 4.858 ± 0.048 | 4.998 ± 0.050 | 1.029 | — |
| ParallelString | `systemSortParallel` | english, n=200,000 | baseline | 11.486 ± 0.184 | 11.426 ± 0.214 | 0.995† | — |
| ParallelString | `systemSortParallel` | english, n=1,000,000 | baseline | 95.021 ± 2.108 | 89.649 ± 1.611 | 0.943 | — |
| ParallelString | `parallelRadixHuskySort11_p8` | chinese, n=32,000 | encode | 3.116 ± 0.165 | 3.115 ± 0.181 | 1.000† | 1.002 |
| ParallelString | `parallelRadixHuskySort11_p8` | chinese, n=200,000 | encode | 9.584 ± 0.163 | 10.040 ± 0.077 | 1.048 | 1.050 |
| ParallelString | `parallelRadixHuskySort11_p8` | chinese, n=1,000,000 | encode | 40.152 ± 0.596 | 40.649 ± 0.363 | 1.012† | 1.015 |
| ParallelString | `parallelRadixHuskySort11_p8` | english, n=32,000 | encode | 4.508 ± 0.158‡ | 4.091 ± 0.198‡ | 0.907 | 0.912 |
| ParallelString | `parallelRadixHuskySort11_p8` | english, n=200,000 | encode | 24.278 ± 1.371‡ | 24.967 ± 1.359‡ | 1.028† | 1.034 |
| ParallelString | `parallelRadixHuskySort11_p8` | english, n=1,000,000 | encode | 100.962 ± 15.833‡ | 104.237 ± 18.160‡ | 1.032† | 1.038 |
| ParallelString | `parallelRadixHuskySortAuto_p1` | chinese, n=32,000 | encode | 3.158 ± 0.172 | 3.141 ± 0.174 | 0.995† | 0.997 |
| ParallelString | `parallelRadixHuskySortAuto_p1` | chinese, n=200,000 | encode | 15.208 ± 0.200 | 15.561 ± 0.180 | 1.023† | 1.026 |
| ParallelString | `parallelRadixHuskySortAuto_p1` | chinese, n=1,000,000 | encode | 73.252 ± 1.352 | 73.376 ± 1.340 | 1.002† | 1.004 |
| ParallelString | `parallelRadixHuskySortAuto_p1` | english, n=32,000 | encode | 4.606 ± 0.289 | 4.846 ± 0.435 | 1.052† | 1.058 |
| ParallelString | `parallelRadixHuskySortAuto_p1` | english, n=200,000 | encode | 48.828 ± 1.143 | 48.224 ± 0.595 | 0.988† | 0.993 |
| ParallelString | `parallelRadixHuskySortAuto_p1` | english, n=1,000,000 | encode | 220.129 ± 5.992 | 211.428 ± 2.968 | 0.960† | 0.965 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | chinese, n=32,000 | encode | 3.243 ± 0.190§ | 3.212 ± 0.189§ | 0.990† | 0.993 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | chinese, n=200,000 | encode | 12.908 ± 0.130 | 13.141 ± 0.131 | 1.018† | 1.021 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | chinese, n=1,000,000 | encode | 54.775 ± 0.997 | 53.865 ± 0.774 | 0.983† | 0.986 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | english, n=32,000 | encode | 4.518 ± 0.258 | 4.653 ± 0.372 | 1.030† | 1.035 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | english, n=200,000 | encode | 32.536 ± 1.921‡ | 31.693 ± 1.944‡ | 0.974† | 0.979 |
| ParallelString | `parallelRadixHuskySortAuto_p2` | english, n=1,000,000 | encode | 210.929 ± 28.214‡ | 216.863 ± 26.754‡ | 1.028† | 1.033 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | chinese, n=32,000 | encode | 3.188 ± 0.180 | 3.201 ± 0.186§ | 1.004† | 1.007 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | chinese, n=200,000 | encode | 10.729 ± 0.094 | 10.921 ± 0.069 | 1.018 | 1.021 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | chinese, n=1,000,000 | encode | 48.061 ± 0.894 | 47.481 ± 1.109 | 0.988† | 0.991 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | english, n=32,000 | encode | 4.409 ± 0.216 | 4.410 ± 0.292 | 1.000† | 1.006 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | english, n=200,000 | encode | 26.543 ± 1.786‡ | 27.507 ± 1.713‡ | 1.036† | 1.042 |
| ParallelString | `parallelRadixHuskySortAuto_p4` | english, n=1,000,000 | encode | 111.223 ± 12.456‡ | 108.782 ± 10.125‡ | 0.978† | 0.983 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | chinese, n=32,000 | encode | 3.237 ± 0.187§ | 3.240 ± 0.189 | 1.001† | 1.003 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | chinese, n=200,000 | encode | 10.069 ± 0.105 | 10.385 ± 0.098 | 1.031 | 1.034 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | chinese, n=1,000,000 | encode | 41.443 ± 0.787 | 42.375 ± 1.031 | 1.022† | 1.025 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | english, n=32,000 | encode | 4.588 ± 0.362 | 4.906 ± 0.522 | 1.069† | 1.075 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | english, n=200,000 | encode | 23.622 ± 1.227‡ | 23.266 ± 1.454‡ | 0.985† | 0.990 |
| ParallelString | `parallelRadixHuskySortAuto_p8` | english, n=1,000,000 | encode | 100.949 ± 16.264‡ | 109.521 ± 20.816‡ | 1.085† | 1.091 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | chinese, n=32,000 | encode | 3.233 ± 0.174 | 3.272 ± 0.179 | 1.012† | 1.015 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | chinese, n=200,000 | encode | 10.073 ± 0.083 | 10.320 ± 0.091 | 1.024 | 1.027 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | chinese, n=1,000,000 | encode | 41.619 ± 1.089 | 40.658 ± 0.969 | 0.977† | 0.980 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | english, n=32,000 | encode | 4.962 ± 0.423 | 4.712 ± 0.416 | 0.950† | 0.955 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | english, n=200,000 | encode | 23.554 ± 1.217‡ | 24.051 ± 1.277‡ | 1.021† | 1.026 |
| ParallelString | `parallelRadixHuskySortAuto_pAll` | english, n=1,000,000 | encode | 97.517 ± 18.127‡ | 101.161 ± 18.240‡ | 1.037† | 1.043 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | chinese, n=32,000 | encode | 2.259 ± 0.026 | 2.226 ± 0.021 | 0.985† | 0.988 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | chinese, n=200,000 | encode | 6.059 ± 0.122 | 6.033 ± 0.038 | 0.996† | 0.998 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | chinese, n=1,000,000 | encode | 31.025 ± 0.527 | 31.323 ± 0.583 | 1.010† | 1.012 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | english, n=32,000 | encode | 4.285 ± 0.296‡ | 4.348 ± 0.353‡ | 1.015† | 1.020 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | english, n=200,000 | encode | 9.527 ± 0.174 | 9.844 ± 0.131 | 1.033 | 1.039 |
| ParallelString | `parallelRadixHuskySortAuto_pAll_parCleanup` | english, n=1,000,000 | encode | 56.135 ± 8.227‡ | 54.809 ± 6.498‡ | 0.976† | 0.981 |
| ParallelString | `serialRadixHuskySortAuto` | chinese, n=32,000 | encode | 2.339 ± 0.019 | 2.412 ± 0.055 | 1.031† | 1.034 |
| ParallelString | `serialRadixHuskySortAuto` | chinese, n=200,000 | encode | 14.682 ± 0.261 | 14.925 ± 0.158 | 1.017† | 1.019 |
| ParallelString | `serialRadixHuskySortAuto` | chinese, n=1,000,000 | encode | 71.018 ± 1.472 | 70.695 ± 1.233 | 0.995† | 0.998 |
| ParallelString | `serialRadixHuskySortAuto` | english, n=32,000 | encode | 4.211 ± 0.141‡ | 3.483 ± 0.187‡ | 0.827 | 0.831 |
| ParallelString | `serialRadixHuskySortAuto` | english, n=200,000 | encode | 51.627 ± 1.409 | 48.290 ± 0.803 | 0.935 | 0.940 |
| ParallelString | `serialRadixHuskySortAuto` | english, n=1,000,000 | encode | 215.345 ± 2.981 | 207.628 ± 2.045 | 0.964 | 0.969 |
| String | `multikeyQuicksort` | chinese, n=32,000 | baseline | 6.005 ± 0.054 | 6.103 ± 0.040 | 1.016 | — |
| String | `multikeyQuicksort` | chinese, n=200,000 | baseline | 42.553 ± 0.287 | 43.315 ± 0.260 | 1.018 | — |
| String | `multikeyQuicksort` | chinese, n=1,000,000 | baseline | 307.586 ± 3.881 | 310.619 ± 4.166 | 1.010† | — |
| String | `multikeyQuicksort` | english, n=32,000 | baseline | 7.991 ± 0.071 | 8.085 ± 0.064 | 1.012† | — |
| String | `multikeyQuicksort` | english, n=200,000 | baseline | 79.973 ± 1.115 | 83.701 ± 0.759 | 1.047 | — |
| String | `multikeyQuicksort` | english, n=1,000,000 | baseline | 739.699 ± 11.354 | 769.159 ± 8.705 | 1.040 | — |
| String | `systemSort` | chinese, n=32,000 | baseline | 10.800 ± 0.058 | 10.858 ± 0.071 | 1.005† | — |
| String | `systemSort` | chinese, n=200,000 | baseline | 82.802 ± 0.687 | 83.645 ± 0.940 | 1.010† | — |
| String | `systemSort` | chinese, n=1,000,000 | baseline | 529.009 ± 3.612 | 540.413 ± 4.312 | 1.022 | — |
| String | `systemSort` | english, n=32,000 | baseline | 14.839 ± 0.304 | 15.525 ± 0.297 | 1.046 | — |
| String | `systemSort` | english, n=200,000 | baseline | 157.808 ± 2.349 | 158.809 ± 1.499 | 1.006† | — |
| String | `systemSort` | english, n=1,000,000 | baseline | 1269.099 ± 31.475 | 1327.939 ± 19.309 | 1.046 | — |
| String | `systemSortParallel` | chinese, n=32,000 | baseline | 3.282 ± 0.013 | 3.296 ± 0.015 | 1.004† | — |
| String | `systemSortParallel` | chinese, n=200,000 | baseline | 6.655 ± 0.033 | 6.641 ± 0.036 | 0.998† | — |
| String | `systemSortParallel` | chinese, n=1,000,000 | baseline | 46.611 ± 2.228 | 46.522 ± 2.343 | 0.998† | — |
| String | `systemSortParallel` | english, n=32,000 | baseline | 4.882 ± 0.054 | 4.948 ± 0.046 | 1.013† | — |
| String | `systemSortParallel` | english, n=200,000 | baseline | 11.700 ± 0.148 | 11.765 ± 0.146 | 1.005† | — |
| String | `systemSortParallel` | english, n=1,000,000 | baseline | 99.273 ± 9.874 | 93.652 ± 3.748 | 0.943† | — |
| String | `huskyEncodeOnly` | chinese, n=32,000 | encode | 0.509 ± 0.006 | 0.510 ± 0.004 | 1.003† | 0.993 |
| String | `huskyEncodeOnly` | chinese, n=200,000 | encode | 3.727 ± 0.046 | 3.587 ± 0.054 | 0.962 | 0.953 |
| String | `huskyEncodeOnly` | chinese, n=1,000,000 | encode | 19.597 ± 0.343 | 19.164 ± 0.118 | 0.978† | 0.968 |
| String | `huskyEncodeOnly` | english, n=32,000 | encode | 1.750 ± 0.047 | 1.499 ± 0.043‡ | 0.857 | 0.845 |
| String | `huskyEncodeOnly` | english, n=200,000 | encode | 21.217 ± 0.333 | 18.442 ± 0.235 | 0.869 | 0.858 |
| String | `huskyEncodeOnly` | english, n=1,000,000 | encode | 109.132 ± 1.471 | 97.994 ± 1.094 | 0.898 | 0.886 |
| String | `huskyEncodeOnlyEnglishMasking` | chinese, n=32,000 | encode | 0.908 ± 0.006 | 0.868 ± 0.004 | 0.956 | 0.946 |
| String | `huskyEncodeOnlyEnglishMasking` | chinese, n=200,000 | encode | 6.322 ± 0.068 | 5.992 ± 0.057 | 0.948 | 0.939 |
| String | `huskyEncodeOnlyEnglishMasking` | chinese, n=1,000,000 | encode | 31.912 ± 0.171 | 31.192 ± 0.394 | 0.977 | 0.968 |
| String | `huskyEncodeOnlyEnglishMasking` | chinesenames, n=32,000 | encode | 0.858 ± 0.011 | 0.866 ± 0.011 | 1.010† | 1.000* |
| String | `huskyEncodeOnlyEnglishMasking` | chinesenames, n=200,000 | encode | 12.681 ± 0.199 | 12.914 ± 0.161 | 1.018† | 1.008* |
| String | `huskyEncodeOnlyEnglishMasking` | chinesenames, n=1,000,000 | encode | 68.539 ± 0.377 | 69.145 ± 0.354 | 1.009† | 0.999* |
| String | `huskyEncodeOnlyEnglishMasking` | english, n=32,000 | encode | 1.551 ± 0.022 | 1.584 ± 0.038 | 1.021† | 1.008 |
| String | `huskyEncodeOnlyEnglishMasking` | english, n=200,000 | encode | 20.547 ± 0.367 | 18.468 ± 0.261 | 0.899 | 0.887 |
| String | `huskyEncodeOnlyEnglishMasking` | english, n=1,000,000 | encode | 107.987 ± 1.202 | 96.758 ± 1.041 | 0.896 | 0.884 |
| String | `huskyEncodeOnlyEnglishSaturating` | chinese, n=32,000 | encode | 1.232 ± 0.009 | 1.208 ± 0.019 | 0.981† | 0.971 |
| String | `huskyEncodeOnlyEnglishSaturating` | chinese, n=200,000 | encode | 9.121 ± 0.091 | 8.735 ± 0.056 | 0.958 | 0.948 |
| String | `huskyEncodeOnlyEnglishSaturating` | chinese, n=1,000,000 | encode | 46.798 ± 0.502 | 46.540 ± 0.591 | 0.994† | 0.985 |
| String | `huskyEncodeOnlyEnglishSaturating` | chinesenames, n=32,000 | encode | 0.921 ± 0.008 | 0.915 ± 0.018 | 0.994† | 0.984* |
| String | `huskyEncodeOnlyEnglishSaturating` | chinesenames, n=200,000 | encode | 13.025 ± 0.200 | 13.289 ± 0.117 | 1.020† | 1.010* |
| String | `huskyEncodeOnlyEnglishSaturating` | chinesenames, n=1,000,000 | encode | 70.334 ± 0.478 | 70.264 ± 0.354 | 0.999† | 0.989* |
| String | `huskyEncodeOnlyEnglishSaturating` | english, n=32,000 | encode | 2.798 ± 0.145 | 2.880 ± 0.210‡ | 1.029† | 1.016 |
| String | `huskyEncodeOnlyEnglishSaturating` | english, n=200,000 | encode | 34.622 ± 0.528 | 35.495 ± 0.367 | 1.025† | 1.012 |
| String | `huskyEncodeOnlyEnglishSaturating` | english, n=1,000,000 | encode | 180.004 ± 2.555 | 180.268 ± 1.659 | 1.001† | 0.988 |
| String | `msdStringSort` | english, n=32,000 | sortcode:8b4639b | 4.218 ± 0.144‡ | 4.120 ± 0.101‡ | 0.977† | 0.964 |
| String | `msdStringSort` | english, n=200,000 | sortcode:8b4639b | 60.084 ± 1.819‡ | 61.642 ± 2.621‡ | 1.026† | 1.012 |
| String | `msdStringSort` | english, n=1,000,000 | sortcode:8b4639b | 321.415 ± 19.633‡ | 328.986 ± 18.445‡ | 1.024† | 1.010 |
| String | `quickHuskySort` | chinese, n=32,000 | encode | 5.386 ± 0.037 | 5.289 ± 0.043 | 0.982 | 0.972 |
| String | `quickHuskySort` | chinese, n=200,000 | encode | 38.811 ± 0.326 | 39.201 ± 0.447 | 1.010† | 1.000 |
| String | `quickHuskySort` | chinese, n=1,000,000 | encode | 283.821 ± 3.355 | 277.108 ± 3.145 | 0.976 | 0.967 |
| String | `quickHuskySort` | english, n=32,000 | encode | 8.266 ± 0.129 | 8.327 ± 0.140 | 1.007† | 0.994 |
| String | `quickHuskySort` | english, n=200,000 | encode | 104.170 ± 1.599 | 104.403 ± 1.112 | 1.002† | 0.989 |
| String | `quickHuskySort` | english, n=1,000,000 | encode | 662.778 ± 9.263 | 666.936 ± 9.874 | 1.006† | 0.993 |
| String | `quickHuskySortPhase2` | chinese, n=32,000 | encode | 4.454 ± 0.019 | 4.404 ± 0.031 | 0.989† | 0.979 |
| String | `quickHuskySortPhase2` | chinese, n=200,000 | encode | 34.133 ± 0.218 | 34.289 ± 0.334 | 1.005† | 0.995 |
| String | `quickHuskySortPhase2` | chinese, n=1,000,000 | encode | 267.528 ± 3.389 | 268.411 ± 3.346 | 1.003† | 0.994 |
| String | `quickHuskySortPhase2` | english, n=32,000 | encode | 6.933 ± 0.121 | 6.983 ± 0.098 | 1.007† | 0.994 |
| String | `quickHuskySortPhase2` | english, n=200,000 | encode | 85.097 ± 1.197 | 85.940 ± 0.947 | 1.010† | 0.997 |
| String | `quickHuskySortPhase2` | english, n=1,000,000 | encode | 603.628 ± 7.450 | 611.270 ± 7.128 | 1.013† | 0.999 |
| String | `quickHuskySortPhase2CodesOnly` | chinese, n=32,000 | encode | 2.430 ± 0.009 | 2.408 ± 0.007 | 0.991 | 0.981 |
| String | `quickHuskySortPhase2CodesOnly` | chinese, n=200,000 | encode | 17.590 ± 0.117 | 17.516 ± 0.091 | 0.996† | 0.986 |
| String | `quickHuskySortPhase2CodesOnly` | chinese, n=1,000,000 | encode | 92.082 ± 2.854 | 90.156 ± 2.618 | 0.979† | 0.970 |
| String | `quickHuskySortPhase2CodesOnly` | english, n=32,000 | encode | 3.939 ± 0.099 | 4.115 ± 0.130‡ | 1.045† | 1.031 |
| String | `quickHuskySortPhase2CodesOnly` | english, n=200,000 | encode | 36.816 ± 0.359 | 35.110 ± 0.207 | 0.954 | 0.941 |
| String | `quickHuskySortPhase2CodesOnly` | english, n=1,000,000 | encode | 194.844 ± 2.701 | 185.649 ± 1.680 | 0.953 | 0.940 |
| String | `radixHuskySort10` | chinese, n=32,000 | encode | 2.537 ± 0.070 | 2.380 ± 0.018 | 0.938 | 0.929 |
| String | `radixHuskySort10` | chinese, n=200,000 | encode | 16.079 ± 0.192 | 16.318 ± 0.168 | 1.015† | 1.005 |
| String | `radixHuskySort10` | chinese, n=1,000,000 | encode | 91.911 ± 2.639 | 92.667 ± 2.811 | 1.008† | 0.998 |
| String | `radixHuskySort10` | english, n=32,000 | encode | 4.182 ± 0.220‡ | 4.171 ± 0.167‡ | 0.997† | 0.984 |
| String | `radixHuskySort10` | english, n=200,000 | encode | 49.326 ± 0.922 | 47.880 ± 0.788 | 0.971† | 0.958 |
| String | `radixHuskySort10` | english, n=1,000,000 | encode | 241.924 ± 1.855 | 235.070 ± 1.626 | 0.972 | 0.959 |
| String | `radixHuskySort11` | chinese, n=32,000 | encode | 2.542 ± 0.012 | 2.477 ± 0.017 | 0.975 | 0.965 |
| String | `radixHuskySort11` | chinese, n=200,000 | encode | 15.062 ± 0.232 | 15.544 ± 0.213 | 1.032 | 1.022 |
| String | `radixHuskySort11` | chinese, n=1,000,000 | encode | 80.796 ± 1.918 | 80.395 ± 1.920 | 0.995† | 0.985 |
| String | `radixHuskySort11` | english, n=32,000 | encode | 3.993 ± 0.200‡ | 3.690 ± 0.208‡ | 0.924† | 0.912 |
| String | `radixHuskySort11` | english, n=200,000 | encode | 48.622 ± 0.700 | 47.375 ± 0.717 | 0.974† | 0.961 |
| String | `radixHuskySort11` | english, n=1,000,000 | encode | 233.518 ± 2.272 | 221.647 ± 2.280 | 0.949 | 0.937 |
| String | `radixHuskySort12` | chinese, n=32,000 | encode | 2.332 ± 0.007 | 2.325 ± 0.012 | 0.997† | 0.987 |
| String | `radixHuskySort12` | chinese, n=200,000 | encode | 15.185 ± 0.181 | 15.921 ± 0.219 | 1.048 | 1.038 |
| String | `radixHuskySort12` | chinese, n=1,000,000 | encode | 80.062 ± 2.059 | 79.818 ± 2.000 | 0.997† | 0.987 |
| String | `radixHuskySort12` | english, n=32,000 | encode | 3.911 ± 0.224‡ | 3.845 ± 0.216‡ | 0.983† | 0.970 |
| String | `radixHuskySort12` | english, n=200,000 | encode | 48.892 ± 0.793 | 46.785 ± 0.778 | 0.957 | 0.944 |
| String | `radixHuskySort12` | english, n=1,000,000 | encode | 235.570 ± 2.196 | 223.633 ± 1.965 | 0.949 | 0.937 |
| String | `radixHuskySort13` | chinese, n=32,000 | encode | 2.254 ± 0.034 | 2.263 ± 0.037 | 1.004† | 0.995 |
| String | `radixHuskySort13` | chinese, n=200,000 | encode | 13.985 ± 0.198 | 14.597 ± 0.181 | 1.044 | 1.034 |
| String | `radixHuskySort13` | chinese, n=1,000,000 | encode | 75.115 ± 2.066 | 74.470 ± 1.957 | 0.991† | 0.982 |
| String | `radixHuskySort13` | english, n=32,000 | encode | 3.730 ± 0.238‡ | 3.775 ± 0.240‡ | 1.012† | 0.999 |
| String | `radixHuskySort13` | english, n=200,000 | encode | 47.216 ± 1.187‡ | 45.361 ± 1.295‡ | 0.961† | 0.948 |
| String | `radixHuskySort13` | english, n=1,000,000 | encode | 225.618 ± 3.469 | 212.447 ± 5.575 | 0.942 | 0.929 |
| String | `radixHuskySort14` | chinese, n=32,000 | encode | 2.384 ± 0.053 | 2.334 ± 0.054 | 0.979† | 0.969 |
| String | `radixHuskySort14` | chinese, n=200,000 | encode | 14.572 ± 0.172 | 14.878 ± 0.170 | 1.021† | 1.011 |
| String | `radixHuskySort14` | chinese, n=1,000,000 | encode | 75.041 ± 1.769 | 75.521 ± 1.829 | 1.006† | 0.997 |
| String | `radixHuskySort14` | english, n=32,000 | encode | 3.803 ± 0.221‡ | 3.546 ± 0.195‡ | 0.932† | 0.920 |
| String | `radixHuskySort14` | english, n=200,000 | encode | 48.436 ± 1.065 | 45.795 ± 0.645 | 0.945 | 0.933 |
| String | `radixHuskySort14` | english, n=1,000,000 | encode | 226.707 ± 3.751 | 212.448 ± 4.605 | 0.937 | 0.925 |
| String | `radixHuskySort16` | chinese, n=32,000 | encode | 2.257 ± 0.020 | 2.296 ± 0.012 | 1.017 | 1.007 |
| String | `radixHuskySort16` | chinese, n=200,000 | encode | 13.670 ± 0.265 | 13.561 ± 0.112 | 0.992† | 0.982 |
| String | `radixHuskySort16` | chinese, n=1,000,000 | encode | 70.415 ± 1.165 | 70.809 ± 1.367 | 1.006† | 0.996 |
| String | `radixHuskySort16` | english, n=32,000 | encode | 3.698 ± 0.241‡ | 3.521 ± 0.215‡ | 0.952† | 0.940 |
| String | `radixHuskySort16` | english, n=200,000 | encode | 47.462 ± 0.892 | 44.565 ± 1.018 | 0.939 | 0.927 |
| String | `radixHuskySort16` | english, n=1,000,000 | encode | 217.009 ± 2.923 | 206.067 ± 2.476 | 0.950 | 0.937 |
| String | `radixHuskySort8` | chinese, n=32,000 | encode | 2.657 ± 0.101 | 2.583 ± 0.091 | 0.972† | 0.963 |
| String | `radixHuskySort8` | chinese, n=200,000 | encode | 16.876 ± 0.216 | 17.382 ± 0.252 | 1.030 | 1.020 |
| String | `radixHuskySort8` | chinese, n=1,000,000 | encode | 96.921 ± 2.484 | 95.938 ± 2.661 | 0.990† | 0.980 |
| String | `radixHuskySort8` | english, n=32,000 | encode | 4.343 ± 0.159‡ | 4.099 ± 0.218‡ | 0.944† | 0.931 |
| String | `radixHuskySort8` | english, n=200,000 | encode | 50.817 ± 0.763 | 48.178 ± 0.851 | 0.948 | 0.936 |
| String | `radixHuskySort8` | english, n=1,000,000 | encode | 251.476 ± 2.076 | 238.780 ± 2.191 | 0.950 | 0.937 |
| String | `radixHuskySortAuto` | chinese, n=32,000 | encode | 2.362 ± 0.013 | 2.287 ± 0.020 | 0.968 | 0.959 |
| String | `radixHuskySortAuto` | chinese, n=200,000 | encode | 14.456 ± 0.180 | 14.472 ± 0.120 | 1.001† | 0.991 |
| String | `radixHuskySortAuto` | chinese, n=1,000,000 | encode | 70.167 ± 0.985 | 71.224 ± 1.373 | 1.015† | 1.005 |
| String | `radixHuskySortAuto` | english, n=32,000 | encode | 4.069 ± 0.174‡ | 4.094 ± 0.140‡ | 1.006† | 0.993 |
| String | `radixHuskySortAuto` | english, n=200,000 | encode | 51.224 ± 0.968 | 47.949 ± 0.996 | 0.936 | 0.924 |
| String | `radixHuskySortAuto` | english, n=1,000,000 | encode | 220.857 ± 2.819 | 207.322 ± 2.580 | 0.939 | 0.926 |

## Load during the timed invocations

From the 60 s `top` samples in each unit's load monitor, restricted to the START–END window of each JMH invocation.
"non-bench %CPU" = the sum of the listed non-benchmark processes in one sample (top 11 rows only); swap from `top`, MiB.
The load1 maxima above 10 fall in the invocations that run 16-thread rows. The largest non-benchmark processes were ones
named `MainThread` and `yum`, and the host's own monitoring services.

| run | group | invocations | samples | load1 median / max | swap first → last (max) | non-bench %CPU median / max | samples with non-bench > 100 % |
|---|---|---:|---:|---|---|---|---:|
| run 1 | A/B | 16 | 160 | 1.32 / 2.16 | 880 → 866 (880) | 6.2 / 118.7 | 2 |
| run 1 | probes | 6 | 23 | 1.38 / 2.37 | 866 → 865 (866) | 6.2 / 74.7 | 0 |
| run 2 | A/B | 20 | 200 | 1.29 / 3.26 | 929 → 926 (929) | 6.2 / 256.0 | 7 |
| run 2 | rerun-StringSortBenchmarks-chinesenames | 1 | 15 | 1.26 / 1.48 | 926 → 926 (926) | 6.2 / 100.0 | 0 |
| run 2 | rerun-StringSortBenchmarks-chinese | 1 | 131 | 1.39 / 14.10 | 926 → 926 (926) | 6.2 / 156.2 | 2 |
| run 2 | rerun-AdversarialSortBenchmarks | 1 | 133 | 1.47 / 2.64 | 926 → 926 (926) | 6.2 / 118.7 | 2 |
| run 2 | rerun-ParallelStringSortBenchmarks | 1 | 150 | 2.00 / 13.31 | 926 → 926 (926) | 6.2 / 256.1 | 6 |
| run 2 | rerun-StringSortBenchmarks-english | 1 | 165 | 1.45 / 11.43 | 926 → 938 (939) | 6.2 / 249.9 | 4 |
