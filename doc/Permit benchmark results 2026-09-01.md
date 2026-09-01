# San Francisco building permits — first measurement, 2026-09-01

Taken on the machine of Table `SysEnvCurrent` (Apple M1, OpenJDK 21), 4 forks, 3 warm-up and 8
measurement iterations at 2s, starting at a one-minute load average of 2.41. Relative confidence
intervals run from 0.5% to 7%, median about 3% — usable, though Yunlu's run is still the one to quote.

Corpus: 198,900 permits from DataSF, sorted by Assessor's block, then lot, then filing date. Smaller
sizes are subsets drawn without replacement. Every sorter's output is verified against `Arrays.sort`
by `PermitSortCorrectnessTest`, including over the whole corpus.

## The measurements, ms/op

| n | radix/16 | radix/11 | radix/8 | QuickHuskySort | + cleanup | System sort | DualPivot |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 32,000 | **2.113 ± 0.095** | 2.212 ± 0.150 | 2.386 ± 0.138 | 4.335 ± 0.046 | 4.590 ± 0.053 | 8.492 ± 0.079 | 8.772 ± 0.322 |
| 100,000 | **9.167 ± 0.655** | 9.650 ± 0.360 | 10.328 ± 0.280 | 18.553 ± 0.545 | 20.763 ± 0.474 | 35.464 ± 1.280 | 34.265 ± 1.142 |
| 198,900 | **19.695 ± 0.721** | 21.371 ± 0.848 | 22.521 ± 0.576 | 43.123 ± 0.794 | 50.634 ± 1.320 | 86.601 ± 2.007 | 87.250 ± 2.739 |

Digit width 16 is best at all three sizes, consistent with the English string result.

## What it says

| n | radix/16 over System sort | radix/16 over QuickHuskySort |
| ---: | ---: | ---: |
| 32,000 | 4.02x | 2.05x |
| 100,000 | 3.87x | 2.02x |
| 198,900 | **4.40x** | **2.19x** |

**Read the second column against Table `RadixImprovements`, not the first.** That table reports the
advantage over QuickHuskySort, so permits enter it at **2.19x** — comparable to Tuples (2.6x) and
below Dates (4.5x). Against the system sort permits give 4.40x, where Dates give about 4.9x.

So permits do **not** beat the synthetic dates row on either measure. What they offer instead is the
same kind of favourable case on **real data**, which is what every other favourable case in the paper
lacks. Dates remain the best number; permits are the credible one.

## The cleanup pass, measured directly

`quickHuskySort` and `quickHuskySortWithCleanup` compute identical codes and differ only in whether
the coder declares itself perfect. So the gap between them is the cost of the cleanup pass on input
where it provably has nothing to do:

| n | without | with | cost |
| ---: | ---: | ---: | ---: |
| 32,000 | 4.335 ± 0.046 | 4.590 ± 0.053 | 5.9% |
| 100,000 | 18.553 ± 0.545 | 20.763 ± 0.474 | 11.9% |
| 198,900 | 43.123 ± 0.794 | 50.634 ± 1.320 | **17.4%** |

Non-overlapping intervals at all three sizes, and the cost **grows with n**.

This is the measurement worth having. The discussion of $p_{crit}$ turns on what an imperfect encoding
costs, and until now that cost has been argued rather than isolated: every other benchmark varies the
encoding and the sort together. Here the encoding is held identical and only the perfection flag
moves. A pass that finds nothing to correct still costs a sixth of the running time at 198,900, and
more as n grows — which sharpens the case for exact encodings well beyond the dates example, and gives
the $p_{crit}$ analysis an empirical anchor it did not have.

## Caveats

One machine, one afternoon, and the machine had spent the day under heavy and varying load — see
[MSD baseline preview 2026-09-01.md](MSD%20baseline%20preview%202026-09-01.md) for how badly that went
earlier. The intervals here are tight and the ordering is unambiguous, but Yunlu's run is the one to
publish.

The corpus supports one natural size. Subsets are drawn without replacement so they stay all-distinct;
resampling to reach a larger n would raise duplicate density and confound the comparison, which is a
trap the string benchmarks in this suite had to be rescued from.
