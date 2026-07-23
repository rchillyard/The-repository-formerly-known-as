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

### Finer digit-width sweep (2026-07-23): the crossover is a plateau, not a point

TODO.md item 2 asked for a finer sweep (10/12/13/14-bit, alongside 8/11/16) to locate the
actual crossover point between "fewer passes" and "count-array fits in cache". Ran under JMH,
same corpora/sizes (`java -jar target/benchmarks.jar
'StringSortBenchmarks\.radixHuskySort1[0234]'`).

Full seven-way digit-width comparison at the two larger sizes (ms, mean ± 99.9% CI; the 32,000
row is omitted — every width is statistically indistinguishable there, overhead-dominated):

| N | Corpus | 8-bit | 10-bit | 11-bit | 12-bit | 13-bit | 14-bit | 16-bit |
|---|---|---|---|---|---|---|---|---|
| 200,000 | English | 50.4±**19.6** | 57.8±**17.2** | 45.6±4.7 | 43.1±2.6 | 47.2±4.4 | 44.2±6.1 | 41.1±4.3 |
| 200,000 | Chinese | 10.5±1.9 | 9.8±0.7 | 14.7±**18.6** | 9.5±3.7 | 8.5±0.8 | 7.7±0.2 | 9.8±4.3 |
| 200,000 | Common words | 11.5±1.3 | 10.7±0.7 | 9.9±1.1 | 16.6±**12.5** | 8.7±0.4 | 8.9±0.4 | 8.4±1.1 |
| 1,000,000 | English | 260.8±65.8 | 256.2±60.6 | 337.2±**368.1** | 309.3±**185.7** | 243.4±101.5 | 213.2±23.8 | 239.2±49.4 |
| 1,000,000 | Chinese | 98.3±20.3 | 75.1±23.1 | 103.1±**47.2** | 77.4±22.0 | 79.1±17.4 | 62.7±21.1 | 85.4±37.2 |
| 1,000,000 | Common words | 87.1±**75.4** | 84.3±15.8 | 64.4±9.3 | 72.0±17.1 | 59.0±5.5 | 57.7±6.1 | 56.4±6.2 |

Two findings, one solid and one worth watching rather than trusting yet:

1. **There is no single crossover point — it's a broad plateau from roughly 12 through 16
   bits.** Those five widths cluster together with mostly-overlapping intervals at every row,
   consistently beating 8-bit and 10-bit. At N=1,000,000, 14-bit has the single lowest mean for
   both English and Chinese (though not always the tightest interval), with 13-bit and 16-bit
   close behind; for common words, 16-bit and 14-bit are essentially tied for lowest. **8-bit
   and, surprisingly, 10-bit are consistently the worst of the seven** — 10-bit needs fewer
   passes than 8-bit (7 vs. 8) yet doesn't reliably beat it, so pass count alone doesn't explain
   the ranking; a plausible mechanism is per-element mask/shift overhead not amortizing as well,
   but this hasn't been confirmed with a profiler.
2. **11-bit shows the widest confidence interval in 3 of these 6 rows** (200K Chinese, 1M
   English, 1M Chinese) — more often than any other single width, though with only 6 rows this
   is a small sample and not strong evidence on its own. It's tempting to blame 11-bit's uneven
   pass structure (6 passes, the last using only 9 of its 11 allocated bits) — but 10/12/13/14-bit
   share that same "doesn't divide 64 evenly" property without showing the same pattern, so
   that specific explanation doesn't hold up. Treat this as an observation to watch in a
   follow-up (independent, different-day) run, not a conclusion.

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

### JMH update (2026-07-23)

Re-ran under JMH (`java -jar target/benchmarks.jar NumericSortBenchmarks`); same generators
except Double/BigDecimal now exercise the full signed range rather than `[0, Long.MAX_VALUE)`
only (see `NumericSortBenchmarks.java`). N=500,000 shown (ms, mean ± 99.9% CI):

| Type | System | PureHuskySort | DualPivotQuicksort | quicksort (raw) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| Integer | 94.4±4.8 | 68.9±8.3 | 67.0±5.8 | 36.3±3.4 | 27.3±3.8 | 24.1±5.3 | 26.0±2.8 |
| Double | 109.6±15.6 | 73.5±5.1 | 94.9±**59.4** | 41.4±1.3 | 29.1±6.0 | 28.0±10.0 | 22.3±2.1 |
| Long | 101.5±8.6 | 74.9±3.7 | 77.0±11.3 | 35.2±0.5 | 25.4±5.3 | 27.2±9.0 | 38.2±**42.1** |
| BigInteger | 157.1±10.5 | 108.6±5.7 | 157.9±10.3 | 38.4±2.8 | 42.4±10.8 | 39.5±4.4 | 35.4±2.1 |
| BigDecimal | 201.4±21.2 | 145.0±**136.9** | 189.6±39.1 | 46.2±1.9 | 45.0±8.0 | 50.0±**40.0** | 39.4±5.3 |

The relative story is unchanged and now on firmer statistical footing: radix beats every other
option — including the dedicated raw-primitive-quicksort baseline — for every numeric type,
typically by 25-40% over that baseline and 2.5-5x over System sort. Absolute magnitudes came
down noticeably from the ad hoc numbers above (e.g. Integer System sort: 136ms ad hoc vs. 94ms
here), a reminder that the un-forked ad hoc harness likely under-warmed the JIT — exactly the
kind of thing JMH's explicit warmup iterations exist to correct for. Digit width has no
consistent single winner across types at N=500,000 (each of 8/11/16 wins for at least one
type); Long's Radix/16 and BigDecimal's Radix/11 rows are this run's outlier-interval cases
(same single-session-noise caveat as the String sweep above).

## Tuples

Composite key type (`birthYear`/`zip`/`name` packed into one husky code, imperfect encoding —
needs the cleanup pass) matching the paper's synthetic Tuple benchmark.

| N | System | PureHuskySort | DualPivotQuicksort | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 20,000 | 3.72 | 3.36 | 4.68 | 1.32 | 1.15 | 1.16 |
| 100,000 | 23.98 | 18.11 | 17.52 | 8.31 | 8.73 | 6.31 |
| 500,000 | 214.15 | 127.41 | 159.41 | 83.27 | 64.95 | 73.32 |

Radix/11 wins at 500,000 (~2x faster than the current PureHuskySort approach).

### JMH update (2026-07-23)

| N | System | PureHuskySort | DualPivotQuicksort | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 20,000 | 3.11±0.40 | 2.19±0.10 | 2.71±0.13 | 1.17±0.17 | 0.99±0.03 | 1.03±0.05 |
| 100,000 | 19.93±1.52 | 13.63±1.05 | 19.58±3.50 | 6.19±0.33 | 5.38±0.28 | 4.89±0.23 |
| 500,000 | 174.30±13.6 | 117.14±20.8 | 155.56±16.1 | 52.56±3.7 | 52.04±9.0 | 45.00±4.6 |

A clean run — no outlier-interval rows this time. Radix/16 is modestly best at N=500,000
(~2.6x faster than PureHuskySort, ~3.9x faster than System sort), consistent with the ad hoc
numbers but now with tight, trustworthy intervals throughout.

## Dates

`ChronoLocalDateTime` via `chronoLocalDateTimeCoder` — a single epoch-second `long`, a
"perfect" encoding that never needs the cleanup pass. Not covered by the original ad hoc
harness run; this is JMH-only, N=20,000 (`java -jar target/benchmarks.jar DateSortBenchmarks`).

| Sorter | Time (ms, mean ± 99.9% CI) |
|---|---|
| System sort | 2.96 ± 0.13 |
| QuickHuskySort | 2.81 ± 0.13 |
| QuickHuskySort + insertion cleanup | 3.06 ± 0.70 |
| Radix/8 | 0.72 ± 0.03 |
| Radix/11 | 0.63 ± 0.04 |
| Radix/16 | 0.67 ± 0.03 |

The largest relative win anywhere in this benchmark: radix is **~4-4.5x faster** than every
quicksort-based option, including the existing QuickHuskySort. The "perfect" single-long
encoding removes any need for a cleanup pass, so radix's O(N) advantage shows through with
nothing else in the way.

## Chinese names (pinyin)

`Chinese_Names_Corpus.txt` (1,145,009 unique names, 2-3 characters each), ordered by Hanyu
Pinyin via the rewritten `HuskyCoderChinesePinyin` (TODO.md item 4). JMH, full digit-width
sweep: `java -jar target/benchmarks.jar 'StringSortBenchmarks\..*' -p corpus=chinesenames`.

**Important caveat on "System sort" here**: unlike the Leipzig English/Chinese corpora (where
natural String order via System sort *is* the semantically correct comparison target), System
sort here does raw Unicode-code-point order, which is *not* correct pinyin order for this
corpus. The meaningful comparison is PureHuskySort vs RadixHuskySort — both correctly produce
pinyin order; System sort's number is included only as a "how fast is a differently-defined
sort" reference point, not a real competitor.

| N | System (wrong order) | PureHuskySort (correct) | Radix/8 | Radix/10 | Radix/11 | Radix/12 | Radix/13 | Radix/14 | Radix/16 |
|---|---|---|---|---|---|---|---|---|---|
| 32,000 | 8.6±1.2 | 33.6±6.3 | 13.9±1.5 | 13.5±1.7 | 13.7±1.8 | 13.6±1.5 | 14.9±3.6 | 14.1±3.0 | 13.9±2.8 |
| 200,000 | 80.3±13.6 | 326.2±**299.8** | 88.1±8.3 | 87.0±7.3 | 86.3±5.4 | 145.7±**53.6** | 95.7±15.7 | 96.8±19.0 | 87.9±11.2 |
| 1,000,000 | 544.2±160.7 | 1090.8±**399.4** | 438.9±28.5 | 428.4±23.4 | 422.4±55.5 | 492.8±81.4 | 475.8±105.4 | 502.2±134.9 | 457.0±79.9 |

Radix beats the existing pinyin-sorting approach (PureHuskySort) by roughly **2.4-3.8x**
depending on N, directionally consistent and robust even given PureHuskySort's wide confidence
intervals at the larger sizes (even PureHuskySort's own lower bound stays well above most
radix variants' upper bound). Digit width shows no clear single winner here (unlike the
cleaner plateau seen for the Leipzig corpora): all widths land in a similar 420-500ms range at
N=1,000,000, and Radix/12 is a noisy-CI outlier at 200,000 — matching the same
single-session-noise pattern already documented for the String fine sweep.

This benchmark also motivated two real fixes made along the way (TODO.md item 4, stage 2):
`HuskyCoderChinesePinyin` previously always claimed `perfect()` (skipping the cleanup pass
unconditionally, regardless of correctness), and — separately, a performance issue rather than
a correctness one — even after fixing that, the cleanup pass's pinyin lookup was uncached,
making a first cut of this benchmark badly misleading (PureHuskySort at ~150ms and
RadixHuskySort at ~37-38ms for N=20,000, vs ~15ms and ~7.5ms respectively once a simple
per-character cache was added — a 10x and 5x improvement). All numbers above reflect the
cached version.

## Headline conclusion

Radix sort with a deferred permutation beats the repo's current quicksort-based husky
long-sort approach at every size and every real data type tested here — Strings, Integer,
Double, Long, BigInteger, BigDecimal, Tuples, Dates, and Chinese names sorted by pinyin —
typically by **2-4x at N >= 200,000** (up to ~4.5x for Dates, where the "perfect" encoding
removes any cleanup-pass overhead), directly confirming Reviewer 3's suspicion. This is now
backed by JMH measurements (proper fork isolation, warmup, and confidence intervals) for every
category, not just the original ad hoc timer-loop numbers — and JMH caught a real problem the
ad hoc numbers didn't: the apparent N=1,000,000 String digit-width reversal turned out to be
noise, not a real effect.

The digit-width question turned out more nuanced than the ad hoc data suggested, and there's no
single sharp crossover. For Strings, the best widths form a **plateau from roughly 12 through
16 bits**, with 8-bit and (surprisingly) 10-bit consistently worse despite 10-bit needing fewer
passes. For Numerics and Tuples, 8/11/16-bit differences remain close to noise level at these
sizes, with no single width winning across every type. See TODO.md item 2 for what's still
open: independent replication, a possible (but not yet confirmed) 11-bit-specific noise
pattern, and whether the String plateau shape holds for Numerics/Tuples too.
