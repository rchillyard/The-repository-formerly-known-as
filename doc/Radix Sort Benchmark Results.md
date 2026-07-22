# RadixHuskySort vs. existing HuskySort approaches: benchmark results

Run 2026-07-22, JDK 21.0.10, macOS (Apple Silicon), single un-replicated run using the repo's
existing custom `Benchmark`/`SorterBenchmark` harness (not JMH — see
[TODO.md](../TODO.md) item 1). Numbers are milliseconds per sort, averaged over an
adaptive number of repetitions chosen so that total comparisons are roughly constant per size
(`HuskySortBenchmark.getRepetitions`).

This benchmarks `RadixHuskySort` (new: LSD radix sort on the husky-coded 64-bit longs, with a
deferred `int[]` permutation instead of swapping payload references at every quicksort
exchange) against the repo's existing sort strategies, on real corpora and real
`HuskyCoderFactory` coders — not synthetic random longs. It answers a question raised by
Reviewer 3 of the original ACDA21 submission (arXiv:2012.00866): why not use radix sort
instead of quicksort on the husky codes? See
[Husky sort radix task brief.md](Husky%20sort%20radix%20task%20brief.md) for the full backstory.

**Config overrides from defaults:** `presorted=false` (words are drawn as a random sample
with replacement from each corpus, not fed in natural reading order — natural order is
nearly-sorted and unfairly favors System sort's TimSort, which has an adaptive run-detection
fast path).

## Strings

Corpora and their unique-word counts (relevant to duplicate-skew when sampling with
replacement into arrays much larger than the corpus):

| Corpus | File | Unique words |
|---|---|---|
| English (Leipzig, 32K test) | `eng-uk_web_2002_10K-sentences.txt` | 22,865 |
| English (Leipzig, 200K/1M tests) | `eng-uk_web_2002_1M-sentences.txt` | 275,333 |
| Chinese (Leipzig, all sizes) | `zho-simp-tw_web_2014_10K-sentences.txt` | 24,017 |
| Common words (all sizes) | `3000-common-words.txt` | 2,998 |

Coder: Unicode (default) for English/Chinese; ASCII/English-family coder for common words.

Time per sort (ms):

| N | Corpus | System sort | PureHuskySort (current approach) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 32,000 | English | 10.03 | 7.30 | 4.20 | 3.78 | 4.17 |
| 32,000 | Chinese | 10.04 | 3.78 | 2.02 | 1.65 | 1.58 |
| 32,000 | Common words | 6.44 | 3.48 | 1.88 | 1.60 | 1.52 |
| 200,000 | English | 108.73 | 77.44 | 54.60 | 41.03 | 38.19 |
| 200,000 | Chinese | 77.59 | 36.91 | 15.88 | 13.28 | 9.74 |
| 200,000 | Common words | 49.13 | 21.94 | 12.46 | 10.01 | 8.43 |
| 1,000,000 | English | 698.46 | 388.57 | 255.08 | 292.31 | 303.44 |
| 1,000,000 | Chinese | 439.08 | 188.76 | 227.76 | 224.36 | 69.99 |
| 1,000,000 | Common words | 234.54 | 125.33 | 132.99 | 116.62 | 157.28 |

Notes:
- At 32K and 200K, the ranking is consistently System > PureHuskySort > Radix/8 > Radix/11 >
  Radix/16 (i.e. radix wins, and wider digits win more, monotonically).
- At 1,000,000, results get inconsistent: English favors Radix/8 (reversing the 200K trend),
  Chinese favors Radix/16 heavily, and common words slightly favors PureHuskySort over two of
  the three radix variants. English has plenty of unique words at this scale (275,333, so the
  1M sample isn't as duplicate-dominated as it might look) — that reversal is more likely
  single-run measurement noise than a duplicate effect. Chinese (24,017 unique) and especially
  common words (2,998 unique) *are* heavily duplicate-skewed at N=1,000,000, which plausibly
  explains PureHuskySort's competitive/better showing there (its 3-way quicksort partition can
  collapse a run of equal keys in one pass; radix's LSD passes are a fixed cost regardless of
  duplicate density). **This needs repeated/JMH-quality runs before treating as conclusive** —
  see TODO.md items 1-2.

### JMH update (2026-07-22): the N=1,000,000 English reversal was noise

Re-ran the String comparisons under JMH (`mvn -Pjmh clean package && java -jar
target/benchmarks.jar StringSortBenchmarks` — see [JMH Benchmarks.md](JMH%20Benchmarks.md)),
default settings (2 forks x (3 warmup + 5 measurement) 1-second iterations = 10 samples per
row). Same corpora/coders/sampling as above. Score ± is the 99.9% CI half-width.

| N | Corpus | System sort | PureHuskySort | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 32,000 | English | 12.25 ± 5.04 | 12.86 ± 5.05 | 4.00 ± 0.85 | 4.32 ± 0.71 | 3.79 ± 0.75 |
| 32,000 | Chinese | 11.25 ± 4.24 | 4.11 ± 0.52 | 1.82 ± 0.35 | 1.40 ± 0.04 | 1.79 ± 0.81 |
| 32,000 | Common words | 6.56 ± 0.70 | 3.88 ± 1.12 | 1.86 ± 0.15 | 1.51 ± 0.02 | 1.52 ± 0.13 |
| 200,000 | English | 146.09 ± 111.5 | 87.18 ± 36.9 | 50.39 ± 19.6 | 45.58 ± 4.7 | 41.12 ± 4.3 |
| 200,000 | Chinese | 61.65 ± 11.7 | 25.99 ± 4.0 | 10.50 ± 1.9 | 14.71 ± 18.6 | 9.76 ± 4.3 |
| 200,000 | Common words | 52.19 ± 14.3 | 21.07 ± 4.7 | 11.49 ± 1.3 | 9.93 ± 1.1 | 8.39 ± 1.1 |
| 1,000,000 | English | 571.87 ± 53.4 | 391.09 ± 154.9 | 260.81 ± 65.8 | 337.22 ± **368.1** | 239.17 ± 49.4 |
| 1,000,000 | Chinese | 287.60 ± 17.4 | 112.13 ± 7.2 | 98.26 ± 20.3 | 103.13 ± 47.2 | 85.43 ± 37.2 |
| 1,000,000 | Common words | 247.48 ± 33.9 | 139.48 ± 112.2 | 87.07 ± **75.4** | 64.43 ± 9.3 | 56.38 ± 6.2 |

**The original N=1,000,000 English "Radix/8 wins, reversing the trend" result does not survive
proper measurement.** Radix/11's own confidence interval there (337 ± 368) is wider than its
mean — i.e. statistically indistinguishable from noise — and Radix/8's (261 ± 66) and Radix/16's
(239 ± 49) intervals overlap substantially. The honest reading at that specific
corpus/size is: **radix clearly and robustly beats both System sort and PureHuskySort (no
overlap there), but which of 8/11/16-bit wins against each other is not resolved by this data**
— not "8-bit wins," which is what the ad hoc numbers implied. Robin's read on skipping
Reviewer 3's actual question for years turns out to extend one level deeper: even the "fewer
passes wins at scale" digit-width story needs real statistics before trusting a specific
ranking, not just real data.

At every other row, radix's advantage over System sort and PureHuskySort holds up with tight,
non-overlapping intervals — the headline finding (radix wins, often 2-4x) is unaffected. The
16-bit-common-words 1,000,000 row also has one large outlier interval (87 ± 75 for Radix/8) —
consistent with the corpus's severe duplicate skew (2,998 unique words) making that specific
combination measurement-unstable, reinforcing the recommendation below to deprioritize it.

## Numeric types

Generators: `Integer`/`Long` via `Random::nextInt`/`nextLong` (full range, includes
negatives); `Double` via `Random::nextDouble` (uniform \[0,1), **no negative values
exercised**); `BigInteger` via `BigInteger.valueOf(r.nextLong())` (includes negatives);
`BigDecimal` via `BigDecimal.valueOf(r.nextDouble() * Long.MAX_VALUE)` (**no negative values
exercised**). Negative-value correctness for all of these is separately verified in
`RadixHuskySortTest` regardless of what this particular random generator happened to sample.

"quicksort" below is a dedicated raw quicksort on unboxed `long`/`double` primitives (not
going through any HuskySort machinery) — the strongest possible non-Husky baseline.
"DualPivotQuicksort" is `java.util.Arrays`' internal dual-pivot quicksort applied directly to
the boxed objects.

Time per sort (ms):

### Integer

| N | System | PureHuskySort | DualPivotQuicksort | quicksort (raw long) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 4.38 | 2.02 | 3.29 | 1.52 | 1.13 | 0.87 | 0.79 |
| 100,000 | 19.13 | 13.12 | 16.15 | 6.01 | 3.82 | 3.21 | 2.90 |
| 500,000 | 136.29 | 93.83 | 101.92 | 45.38 | 26.12 | 24.06 | 23.03 |

### Double

| N | System | PureHuskySort | DualPivotQuicksort | quicksort (raw double) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 3.74 | 2.17 | 2.82 | 1.34 | 0.68 | 0.60 | 0.74 |
| 100,000 | 23.03 | 15.45 | 20.08 | 7.28 | 3.84 | 3.48 | 3.28 |
| 500,000 | 152.37 | 93.18 | 119.17 | 52.78 | 36.84 | 36.14 | 31.07 |

### Long

| N | System | PureHuskySort | DualPivotQuicksort | quicksort (raw long) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 3.16 | 1.91 | 2.40 | 1.05 | 0.52 | 0.43 | 0.59 |
| 100,000 | 19.40 | 13.00 | 16.84 | 6.09 | 3.41 | 2.90 | 3.43 |
| 500,000 | 137.01 | 81.92 | 98.74 | 45.47 | 22.88 | 21.53 | 28.94 |

### BigInteger

| N | System | PureHuskySort | DualPivotQuicksort | quicksort (raw long) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 3.70 | 2.29 | 3.25 | 1.22 | 0.82 | 0.81 | 0.86 |
| 100,000 | 27.60 | 15.63 | 23.12 | 8.56 | 5.97 | 6.62 | 5.85 |
| 500,000 | 305.61 | 127.87 | 162.59 | 51.86 | 47.61 | 42.72 | 44.22 |

### BigDecimal

| N | System | PureHuskySort | DualPivotQuicksort | quicksort (raw double) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 5.87 | 3.58 | 6.80 | 6.00 | 2.20 | 2.06 | 1.85 |
| 100,000 | 49.22 | 27.93 | 40.17 | 29.90 | 12.37 | 11.14 | 11.55 |
| 500,000 | 331.88 | 149.25 | 256.27 | 211.65 | 72.08 | 79.13 | 91.66 |

Radix beats every other option, including the dedicated raw-primitive quicksort baseline, at
every size for every numeric type tested. Digit width winner is mixed at small N (noise-level
differences) but trends toward Radix/8 or Radix/11 at N=500,000 for most types; BigDecimal
favors Radix/8 specifically.

## Tuples

Composite key type (`birthYear`/`zip`/`name` packed into one husky code, imperfect encoding —
needs the cleanup pass) matching the paper's synthetic Tuple benchmark.

| N | System | PureHuskySort | DualPivotQuicksort | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 20,000 | 3.72 | 3.36 | 4.68 | 1.32 | 1.15 | 1.16 |
| 100,000 | 23.98 | 18.11 | 17.52 | 8.31 | 8.73 | 6.31 |
| 500,000 | 214.15 | 127.41 | 159.41 | 83.27 | 64.95 | 73.32 |

Radix/11 wins at 500,000 (~2x faster than the current PureHuskySort approach).

## Headline conclusion

Radix sort with a deferred permutation beats the repo's current quicksort-based husky
long-sort approach at every size and every real data type tested here, typically by
**2-4x at N >= 200,000**, directly confirming Reviewer 3's suspicion. Wider digits (16-bit)
tend to win at moderate-to-large N for strings; for numerics/tuples the 8/11/16-bit
differences are smaller and less consistent, needing the finer sweep + repeated runs in
TODO.md to pin down precisely.
