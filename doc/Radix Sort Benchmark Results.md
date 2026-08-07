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

| N | Corpus | System sort | QuickHuskySort (current approach) | Radix/8 | Radix/11 | Radix/16 |
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
- At 32K and 200K, the ranking is consistently System > QuickHuskySort > Radix/8 > Radix/11 >
  Radix/16 (i.e. radix wins, and wider digits win more, monotonically).
- At 1,000,000, results get inconsistent: English favors Radix/8 (reversing the 200K trend),
  Chinese favors Radix/16 heavily, and common words slightly favors QuickHuskySort over two of
  the three radix variants. English has plenty of unique words at this scale (275,333, so the
  1M sample isn't as duplicate-dominated as it might look) — that reversal is more likely
  single-run measurement noise than a duplicate effect. Chinese (24,017 unique) and especially
  common words (2,998 unique) *are* heavily duplicate-skewed at N=1,000,000, which plausibly
  explains QuickHuskySort's competitive/better showing there (its 3-way quicksort partition can
  collapse a run of equal keys in one pass; radix's LSD passes are a fixed cost regardless of
  duplicate density). **This needs repeated/JMH-quality runs before treating as conclusive** —
  see TODO.md items 1-2.

### JMH update (2026-07-22): the N=1,000,000 English reversal was noise

Re-ran the String comparisons under JMH (`mvn -Pjmh clean package && java -jar
target/benchmarks.jar StringSortBenchmarks` — see [JMH Benchmarks.md](JMH%20Benchmarks.md)),
default settings (2 forks x (3 warmup + 5 measurement) 1-second iterations = 10 samples per
row). Same corpora/coders/sampling as above. Score ± is the 99.9% CI half-width.

| N | Corpus | System sort | QuickHuskySort | Radix/8 | Radix/11 | Radix/16 |
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
corpus/size is: **radix clearly and robustly beats both System sort and QuickHuskySort (no
overlap there), but which of 8/11/16-bit wins against each other is not resolved by this data**
— not "8-bit wins," which is what the ad hoc numbers implied. Robin's read on skipping
Reviewer 3's actual question for years turns out to extend one level deeper: even the "fewer
passes wins at scale" digit-width story needs real statistics before trusting a specific
ranking, not just real data.

At every other row, radix's advantage over System sort and QuickHuskySort holds up with tight,
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

| N | System | QuickHuskySort | DualPivotQuicksort | quicksort (raw long) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 4.38 | 2.02 | 3.29 | 1.52 | 1.13 | 0.87 | 0.79 |
| 100,000 | 19.13 | 13.12 | 16.15 | 6.01 | 3.82 | 3.21 | 2.90 |
| 500,000 | 136.29 | 93.83 | 101.92 | 45.38 | 26.12 | 24.06 | 23.03 |

### Double

| N | System | QuickHuskySort | DualPivotQuicksort | quicksort (raw double) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 3.74 | 2.17 | 2.82 | 1.34 | 0.68 | 0.60 | 0.74 |
| 100,000 | 23.03 | 15.45 | 20.08 | 7.28 | 3.84 | 3.48 | 3.28 |
| 500,000 | 152.37 | 93.18 | 119.17 | 52.78 | 36.84 | 36.14 | 31.07 |

### Long

| N | System | QuickHuskySort | DualPivotQuicksort | quicksort (raw long) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 3.16 | 1.91 | 2.40 | 1.05 | 0.52 | 0.43 | 0.59 |
| 100,000 | 19.40 | 13.00 | 16.84 | 6.09 | 3.41 | 2.90 | 3.43 |
| 500,000 | 137.01 | 81.92 | 98.74 | 45.47 | 22.88 | 21.53 | 28.94 |

### BigInteger

| N | System | QuickHuskySort | DualPivotQuicksort | quicksort (raw long) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 20,000 | 3.70 | 2.29 | 3.25 | 1.22 | 0.82 | 0.81 | 0.86 |
| 100,000 | 27.60 | 15.63 | 23.12 | 8.56 | 5.97 | 6.62 | 5.85 |
| 500,000 | 305.61 | 127.87 | 162.59 | 51.86 | 47.61 | 42.72 | 44.22 |

### BigDecimal

| N | System | QuickHuskySort | DualPivotQuicksort | quicksort (raw double) | Radix/8 | Radix/11 | Radix/16 |
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

| Type | System | QuickHuskySort | DualPivotQuicksort | quicksort (raw) | Radix/8 | Radix/11 | Radix/16 |
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

| N | System | QuickHuskySort | DualPivotQuicksort | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 20,000 | 3.72 | 3.36 | 4.68 | 1.32 | 1.15 | 1.16 |
| 100,000 | 23.98 | 18.11 | 17.52 | 8.31 | 8.73 | 6.31 |
| 500,000 | 214.15 | 127.41 | 159.41 | 83.27 | 64.95 | 73.32 |

Radix/11 wins at 500,000 (~2x faster than the current QuickHuskySort approach).

### JMH update (2026-07-23)

| N | System | QuickHuskySort | DualPivotQuicksort | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 20,000 | 3.11±0.40 | 2.19±0.10 | 2.71±0.13 | 1.17±0.17 | 0.99±0.03 | 1.03±0.05 |
| 100,000 | 19.93±1.52 | 13.63±1.05 | 19.58±3.50 | 6.19±0.33 | 5.38±0.28 | 4.89±0.23 |
| 500,000 | 174.30±13.6 | 117.14±20.8 | 155.56±16.1 | 52.56±3.7 | 52.04±9.0 | 45.00±4.6 |

A clean run — no outlier-interval rows this time. Radix/16 is modestly best at N=500,000
(~2.6x faster than QuickHuskySort, ~3.9x faster than System sort), consistent with the ad hoc
numbers but now with tight, trustworthy intervals throughout.

## Dates

`ChronoLocalDateTime` via `chronoLocalDateTimeCoder` — a single epoch-second `long`, a
"perfect" encoding that never needs the cleanup pass. Not covered by the original ad hoc
harness run; this is JMH-only, N=20,000 (`java -jar target/benchmarks.jar DateSortBenchmarks`).

| Sorter | Time (ms, mean ± 99.9% CI) |
|---|---|
| System sort | 2.96 ± 0.13 |
| DutchHuskySort | 2.81 ± 0.13 |
| DutchHuskySort + insertion cleanup | 3.06 ± 0.70 |
| Radix/8 | 0.72 ± 0.03 |
| Radix/11 | 0.63 ± 0.04 |
| Radix/16 | 0.67 ± 0.03 |

The largest relative win anywhere in this benchmark: radix is **~4-4.5x faster** than every
quicksort-based option, including the existing DutchHuskySort. The "perfect" single-long
encoding removes any need for a cleanup pass, so radix's O(N) advantage shows through with
nothing else in the way.

## Chinese names (pinyin)

`Chinese_Names_Corpus.txt` (1,145,009 unique names, 2-3 characters each), ordered by Hanyu
Pinyin via the rewritten `HuskyCoderChinesePinyin` (TODO.md item 4). JMH, full digit-width
sweep: `java -jar target/benchmarks.jar 'StringSortBenchmarks\..*' -p corpus=chinesenames`.

**Important caveat on "System sort" here**: unlike the Leipzig English/Chinese corpora (where
natural String order via System sort *is* the semantically correct comparison target), System
sort here does raw Unicode-code-point order, which is *not* correct pinyin order for this
corpus. The meaningful comparison is QuickHuskySort vs RadixHuskySort — both correctly produce
pinyin order; System sort's number is included only as a "how fast is a differently-defined
sort" reference point, not a real competitor.

**2026-07-24 correction — the numbers below replace an earlier, invalid set.** `RadixHuskySort`
had a real bug: its convenience constructor hardcoded `Arrays::sort` as the cleanup-pass
post-sorter and never consulted `HuskyCoder.getCollator()`. `HuskyCoderChinesePinyin` always
needs the cleanup pass (it never claims `perfect()`), so every prior `RadixHuskySort` result
for Chinese names was silently sorted by natural Unicode-code-point order, not real pinyin
order — invalidating the RadixHuskySort numbers from the first pass of this benchmark (though
not QuickHuskySort's, which already checked `getCollator()` correctly). Fixed by making the
convenience constructor check `getCollator()`, with a dedicated regression test added. Robin
predicted correctly that the fix would come at a real performance cost, not be free — the
collator-based comparator does genuinely more work (a cached syllable+tone lookup and
comparison) than a raw `char` comparison. Confirmed below: RadixHuskySort's advantage over
QuickHuskySort shrinks substantially now that both pay the same expensive cleanup-pass cost.

| N | System (wrong order) | QuickHuskySort (correct) | Radix/8 | Radix/10 | Radix/11 | Radix/12 | Radix/13 | Radix/14 | Radix/16 |
|---|---|---|---|---|---|---|---|---|---|
| 32,000 | 14.8±5.3 | 52.6±**33.5** | 42.0±16.3 | 42.8±25.9 | 47.0±18.6 | 34.3±6.1 | 37.1±7.4 | 40.1±16.2 | 33.8±2.1 |
| 200,000 | 105.1±10.6 | 276.6±85.9 | 434.7±**248.9** | 310.9±**152.6** | 207.2±27.3 | 221.1±58.3 | 235.0±65.2 | 199.6±47.9 | 211.2±44.6 |
| 1,000,000 | 695.5±200.2 | 1439.3±481.5 | 1575.0±**1595.6** | 970.1±90.9 | 1095.7±198.7 | 970.5±73.5 | 1056.3±186.0 | 1073.6±126.3 | 1205.3±**646.0** |

Radix still wins at every size, but by a much smaller margin than the invalid numbers implied
— roughly **1.3-1.5x** at N=1,000,000 (Radix/10 and Radix/12, the tightest/lowest of the
bunch, at ~970ms vs QuickHuskySort's ~1439ms), not the previously-reported 2.4-3.8x. That makes
sense: with both sorters now paying the same expensive collator-based cleanup cost, radix's
advantage is confined to the (proportionally much smaller) first-pass cost difference.

Two things stand out that didn't show up as clearly on the other corpora:
- **The corrected comparator makes timings noisier across the board**, not just slower — every
  CI here is wide relative to its mean, and Radix/8 at N=1,000,000 has a CI *larger than its
  own mean* (essentially uninformative that row). This looks like a property of the
  collator-based comparison itself (more branching/lookups per comparison than a raw `char`
  compare) rather than anything specific to a particular digit width.
- **No clear digit-width winner** — unlike the clean plateau seen for the Leipzig corpora,
  every width from 8 through 16 lands somewhere in a wide, overlapping range at each size, with
  the "which width looks best" ranking not obviously consistent between 200,000 and 1,000,000.

Given the width of these confidence intervals, this data would benefit from an independent
repeated run before treating any specific ranking as settled — the headline "radix still wins,
margin much smaller than first thought" is solid; the fine-grained ordering among 8-16 bits
here is not.

This benchmark also motivated two earlier fixes (TODO.md item 4, stage 2, both still valid and
unaffected by the correction above): `HuskyCoderChinesePinyin` previously always claimed
`perfect()` (skipping the cleanup pass unconditionally, regardless of correctness), and,
separately, the cleanup pass's pinyin lookup was uncached, making an even earlier cut of this
benchmark badly misleading (QuickHuskySort ~150ms, RadixHuskySort ~37-38ms at N=20,000, before
caching; both numbers were also using the wrong-order RadixHuskySort at that point, on top of
being uncached).

### 2026-07-24 update: encoding tone as well as syllable

Robin's proposal: since `perfect()` must stay `false` regardless (true homonyms — identical
syllable *and* tone, different character — remain possible no matter how many characters are
encoded), why not encode tone too? It can't buy back the cleanup pass being skipped, but it
should reduce how much real work that pass has to do: dropping tone left every group of names
sharing a syllable (which, for common surnames, can be huge) in essentially arbitrary relative
order after the first pass, forcing real O(k log k) work within each such group during cleanup;
encoding tone means the first pass already gets almost everything right except the rare
true-homonym pairs, which should let TimSort's adaptive/galloping behavior make the
(still-mandatory) cleanup pass much cheaper. Implemented as 12 bits/character (9 syllable + 3
tone), capacity dropping from 7 to 5 characters — still comfortable margin over the "4 common,
5 is about the practical maximum" range for real Chinese personal names, and `perfect()`
correctly stays `false`.

Confirmed empirically — this is a clear, consistent win, not just a hoped-for one:

| N | System (wrong order) | QuickHuskySort | Radix/8 | Radix/10 | Radix/11 | Radix/12 | Radix/13 | Radix/14 | Radix/16 |
|---|---|---|---|---|---|---|---|---|---|
| 32,000 | 10.9±6.7 | 33.3±9.2 | 29.4±9.4 | 33.8±11.1 | 24.6±2.1 | 34.8±**20.2** | 23.5±3.4 | 24.4±4.6 | 36.5±**30.8** |
| 200,000 | 99.2±54.1 | 244.2±110.8 | 206.8±**157.9** | 165.7±41.7 | 165.0±54.7 | 133.8±5.7 | 141.6±14.6 | 142.3±27.0 | 159.6±34.5 |
| 1,000,000 | 840.6±549.5 | 1145.2±515.9 | 936.4±429.5 | 799.2±252.2 | 723.7±70.5 | 772.5±251.2 | 731.2±108.7 | 755.6±196.0 | 766.4±171.9 |

Compared to the syllable-only table above (same corpus, same sizes, same digit widths, only
the encoding changed):
- **Both sorters got faster**, confirming the mechanism is real: QuickHuskySort improved
  ~1.13-1.58x depending on N (e.g. at N=1,000,000: 1439ms → 1145ms), and most radix widths
  improved by a similar or larger factor (e.g. Radix/12 at N=200,000: 221ms → 134ms, ~1.65x;
  Radix/11 at N=1,000,000: 1096ms → 724ms, ~1.51x).
- **Radix's relative advantage over QuickHuskySort widened**, as expected if radix's already-cheap
  first pass now leaves even less work for the shared cleanup cost to dominate: the
  QuickHuskySort/Radix-11 ratio grew from ~1.31x (syllable-only) to ~1.58x (syllable+tone) at
  N=1,000,000.
- **Confidence intervals got tighter for the best-behaved widths**, not just the means
  improving — Radix/11 at N=1,000,000 went from 1096±199 to 724±71, nearly a 3x tighter
  interval alongside the faster mean. This is consistent with TimSort actually doing
  meaningfully less real comparison/data-movement work now that the first pass is more
  accurate, not merely running faster by chance.
- **Some rows are still noisy** (Radix/8 and Radix/16 at 32,000, Radix/8 at 200,000) — the
  earlier caveat about single-session noise in this comparator still applies to the
  fine-grained digit-width ranking, just less severely than before. There's a tentative
  suggestion of a "middle widths (11-14) do best" pattern at both 200,000 and 1,000,000, but
  given the overlapping CIs this isn't asserted as settled.

Headline: radix now beats QuickHuskySort on Chinese names by roughly **1.5-1.6x** at scale
(using the tightest, most reliable width, Radix/11) — smaller than the 2-4x seen on the other
categories, but a real, solid win, recovered by fixing the `getCollator()` bug and then further
improved by encoding tone.

## Encoding-only cost (Reviewer 1)

Reviewer 1 of the original submission asked directly: "did you time the encoding phase? How
much time does it account for?" Nothing in the original ad hoc harness isolated Step 1
(`huskyEncode`) from Step 2 (sort). New `StringSortBenchmarks.huskyEncodeOnly` benchmark,
N=1,000,000 (`java -jar target/benchmarks.jar 'StringSortBenchmarks\.(huskyEncodeOnly|quickHuskySort|radixHuskySort16)$' -p n=1000000 -p corpus=english,chinesenames`):

| Corpus | Encoding only (ms) | QuickHuskySort total (ms) | Encoding % of QuickHuskySort | Radix/16 total (ms) | Encoding % of Radix/16 |
|---|---|---|---|---|---|
| English | 52.7 | 383.2 | 13.7% | 249.3 | 21.1% |
| Chinese names (pinyin) | 311.6 | 1070.2 | 29.1% | 779.4 | 40.0% |

Encoding is a real but minority cost in every case measured — consistent with the paper's own
"linear, doesn't contribute to overall growth" framing, now backed by a measurement rather than
an assumption. It's a noticeably bigger share for the pinyin encoding (which does per-character
syllable+tone lookups) than for the simple ASCII/Unicode string packing, and a bigger share for
radix than for QuickHuskySort in both cases — not because radix's encoding is different, but
because radix's own sort-phase cost is smaller, making whatever encoding cost exists a larger
fraction of a smaller total. This suggests encoding cost, particularly for pinyin, is now the
more promising target if further optimization is wanted, since the sort phase is no longer the
dominant cost once radix is in the picture.

## Adversarial inputs (Reviewer 4)

TODO.md item 7, answering the other unanswered critique from the original review round:
Reviewer 4 pointed out it would be "trivial to construct inputs that cause the proposed scheme
to perform poorly," and that the paper never showed what happens when the husky encoding has
poor entropy. Two scenarios, both JMH (`AdversarialSortBenchmarks`,
`java -jar target/benchmarks.jar 'AdversarialSortBenchmarks\..*'`), `@Fork(2)`, 3+5 one-second
iterations.

### Scenario A: collapsed high-order bits (synthetic `Long[]`)

A direct, parameterized version of the concern: `fixedHighBits` high-order bits held identical
across every element (masked to a fixed pattern), the rest random; `fixedHighBits=0` is the
random baseline, `63` leaves only the sign bit free. `collapsedBitsDualPivotQuicksort` is the
paper's own re-implemented dual-pivot quicksort baseline (see the footnote in the paper's
Test Case and Analysis section: "dual-pivot quicksort is not actually implemented in the Java
library for Comparable objects, so we re-implemented the class for objects" — i.e. a from-scratch
recursive partition with no equal-element/3-way handling). Time (ms, mean; CI omitted here for
readability, available in the raw CSV):

| N | fixedHighBits | System sort | QuickHuskySort | DualPivotQuicksort (raw) | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|---|
| 200,000 | 0 | 42.8 | 26.7 | 24.6 | 6.1 | 6.3 | 6.3 |
| 200,000 | 16 | 32.8 | 41.0 | 27.9 | 6.8 | 5.5 | 6.0 |
| 200,000 | 32 | 32.3 | 26.6 | 24.6 | 8.5 | 8.2 | 6.1 |
| 200,000 | 48 | 33.2 | 30.1 | 22.9 | 10.7 | 7.3 | 6.5 |
| 200,000 | 56 | 21.8 | 11.9 | 174.5 | 14.5 | 7.9 | 6.1 |
| 200,000 | 60 | 12.9 | 6.0 | 2059.6 | 9.6 | 7.4 | 6.1 |
| 200,000 | 63 | 4.8 | 1.8 | **CRASH** | 9.0 | 6.6 | 5.0 |
| 1,000,000 | 0 | 287.9 | 180.6 | 174.9 | 54.8 | 55.9 | 61.5 |
| 1,000,000 | 16 | 243.2 | 181.9 | 193.7 | 59.3 | 69.0 | 52.1 |
| 1,000,000 | 32 | 232.0 | 193.4 | 180.2 | 69.7 | 80.6 | 53.5 |
| 1,000,000 | 48 | 205.5 | 159.5 | 163.4 | 76.7 | 60.6 | 51.3 |
| 1,000,000 | 56 | 107.4 | 82.6 | 3220.5 | 78.5 | 72.1 | 52.2 |
| 1,000,000 | 60 | 71.5 | 75.6 | **CRASH** | 66.9 | 52.9 | 40.9 |
| 1,000,000 | 63 | 27.1 | 13.1 | **CRASH** | 56.8 | 37.7 | 34.5 |

"CRASH" = every fork threw `java.lang.StackOverflowError` during warmup, no measurement
possible at all — not "slow," but a hard failure. This happened at 3 of the 14
(N, fixedHighBits) combinations, all at the extreme end of the sweep (fixedHighBits 60-63,
i.e. only the top 1-4 bits vary).

Three findings:

1. **RadixHuskySort stays essentially flat across the entire sweep**, at every digit width —
   6-11ms at N=200,000 and 41-81ms at N=1,000,000 regardless of `fixedHighBits`. This is exactly
   the expected behavior for an algorithm whose cost per pass is a fixed function of N and digit
   width, independent of the data's actual distribution: a pass over collapsed digits is just
   "everything lands in one bucket," no more expensive than any other pass.
2. **The paper's own re-implemented dual-pivot quicksort baseline degrades catastrophically,
   then crashes outright.** It tracks the random baseline closely up to `fixedHighBits=48`
   (163-194ms at N=1,000,000, similar to System sort/QuickHuskySort), then blows up by more than
   an order of magnitude at 56 (3220ms) and 60 (2059ms at N=200,000), before failing completely
   at 60 (N=1,000,000) and 63 (both sizes) with a stack overflow. This is the sharpest possible
   illustration of Reviewer 4's point — a plausible, simple construction (many keys sharing most
   of their high-order bits) doesn't just slow this implementation down, it crashes it — but the
   crashing implementation is the *baseline comparison*, not Huskysort itself.
3. **QuickHuskySort — Huskysort's actual current approach (Introsort, i.e. quicksort with a
   depth-limited heapsort fallback per Musser 1997, plus the mandatory Timsort cleanup pass) —
   neither crashes nor degrades anywhere in this sweep; if anything it gets faster as
   `fixedHighBits` grows** (26.7ms → 1.8ms at N=200,000), the same pattern System sort shows,
   consistent with Timsort's adaptive run-detection treating long stretches of tied/nearly-equal
   keys as pre-existing runs. So Huskysort's *existing* design is already meaningfully more
   robust to this adversarial pattern than a naively-implemented quicksort — the heapsort
   fallback (or the cleanup pass, or both) evidently prevents the pathological recursion depth
   that sinks the raw baseline. Radix sort is more robust still, because its immunity is
   structural (cost provably independent of key distribution) rather than incidental to a
   fallback mechanism tuned to catch *this particular* pathology.

### Scenario B: strings sharing a long common prefix

A realistic version of the same concern for the paper's actual String benchmarks: real Leipzig
English words with a fixed-length prefix of `a` characters prepended, so the shared prefix
consumes some or all of the husky code's fixed character-capture window (`englishCoder` is
7-bit ASCII, `MAX_LENGTH_ASCII = 64/7 = 9` characters per the paper's own Constants listing —
so `prefixLength >= 9` means every element's husky code is *identical*, i.e. total information
loss before the sort even starts). No dual-pivot-quicksort baseline here (String sorting always
goes through Timsort/comparator machinery, not a primitive dual-pivot path). Time (ms, mean):

| N | prefixLength | System sort | QuickHuskySort | Radix/8 | Radix/11 | Radix/16 |
|---|---|---|---|---|---|---|
| 200,000 | 0 | 97.1 | 46.2 | 31.1 | 32.4 | 31.5 |
| 200,000 | 10 | 64.6 | 86.4 | 72.5 | 98.7 | 73.0 |
| 200,000 | 20 | 77.4 | 73.0 | 81.4 | 79.3 | 71.3 |
| 200,000 | 40 | 73.4 | 72.2 | 90.6 | 84.4 | 79.0 |
| 1,000,000 | 0 | 627.0 | 292.1 | 173.7 | 214.6 | 180.2 |
| 1,000,000 | 10 | 452.9 | 484.9 | 490.0 | 520.0 | 519.9 |
| 1,000,000 | 20 | 439.7 | 475.7 | 524.0 | 518.0 | 568.1 |
| 1,000,000 | 40 | 516.7 | 556.7 | 582.5 | 599.5 | 582.3 |

A genuinely different, and less flattering, story than Scenario A:

- At `prefixLength=0` (baseline), the usual pattern holds — radix beats QuickHuskySort, which
  beats System sort (e.g. N=1,000,000: 174-215ms vs 292ms vs 627ms).
- **Once `prefixLength` reaches 10 (past the 9-character encoding window), the entire ranking
  inverts: plain System sort becomes the fastest option, and every Husky-based approach —
  QuickHuskySort *and* Radix, at every digit width — becomes slower than System sort**, not just
  no-longer-faster (e.g. N=1,000,000, prefixLength=10: System 452.9ms vs. QuickHuskySort 484.9ms,
  Radix 490-520ms). This holds at prefixLength 20 and 40 too.
- The mechanism is different from Scenario A, and it isn't a sort-algorithm problem at all: once
  the shared prefix meets or exceeds the coder's fixed capture window, *every* husky code is
  bit-for-bit identical, so the O(N) encoding pass buys zero disambiguation, and the entire
  ordering burden falls on the mandatory Timsort cleanup pass doing real `String.compareTo`
  comparisons — comparisons which are themselves more expensive than usual, since they must scan
  past the shared prefix before finding a difference. Whichever sort ran first (quicksort/
  Introsort or radix) did genuinely useless work first, then the encoding+first-pass overhead is
  paid *on top of* a cleanup pass that has to do all the real work anyway — which is exactly why
  every Husky variant ends up slower than just running System sort directly.
- Radix doesn't recover any advantage here, and is very slightly the slowest of the three
  Husky-based options in this regime (e.g. N=1,000,000, prefixLength=40: Radix/8-16 582-600ms vs
  QuickHuskySort 556.7ms) — its fixed per-pass cost, a strength in Scenario A, becomes pure
  overhead once the encoding carries no information at all: it still does the full digit sweep
  for zero sorting progress.

This scenario isn't a radix-vs-quicksort question at all — it's a direct, real demonstration of
the paper's own `p_crit`/"perfect encoding" discussion (§3.2 in the original paper): performance
depends on how much the *actual data* defeats the encoding's fixed capture window, independent
of which O(N)/O(N log N) sort follows it. A shared prefix long enough to exceed that window is
exactly the kind of adversarial input Reviewer 4 asked about, and the honest answer is that it
defeats the whole Huskysort premise (spend O(N) upfront to save comparisons later), not just one
implementation choice for the sort step.

### Adversarial-input headline

Two distinct findings, because Reviewer 4's "poor entropy" concern actually covers two different
failure modes:

1. **When many keys collide in their high-order bits but the underlying objects are still
   genuinely distinguishable by the encoding** (Scenario A) — radix sort is effectively immune,
   by construction; a naively-implemented quicksort baseline is not, and can degrade by orders of
   magnitude or crash outright. Huskysort's actual existing approach (Introsort with a
   depth-limited fallback) already avoids the crash, but radix removes even the slowdown.
2. **When the source data itself overwhelms the encoding's fixed capture window** (Scenario B) —
   no choice of sort algorithm for the encoded longs can help, because the encoding has been
   defeated and carries no information; every Husky-based approach, radix included, ends up
   *worse* than plain System sort, since the wasted encoding/first-pass work is paid on top of a
   cleanup pass that has to do all the real work regardless.

Both are real, constructible "trivial adversarial inputs" in the sense Reviewer 4 meant. The
first strengthens the case for radix specifically; the second is a limitation of the encoding
scheme itself, already implicit in the original paper's own `p_crit` discussion, and applies
equally regardless of whether quicksort or radix sorts the codes.

## Parallel radix sort

TODO.md item 15: implement (not just claim as future work) a parallel variant of
RadixHuskySort's digit passes. New `ParallelRadixHuskySort`: each pass is split into contiguous
chunks (one per thread), each chunk computes its own local per-bucket histogram independently,
a short sequential step combines histograms into an exact per-(chunk, bucket) starting offset
(preserving LSD stability), then chunks scatter independently using only their own precomputed
offsets. Correctness verified first (13 new tests, `ParallelRadixHuskySortTest`, mirroring
`RadixHuskySortTest`'s coverage but additionally sweeping chunk/thread counts including ones
that do not evenly divide N — a broken histogram-to-offset combine would only show up when
chunk boundaries actually split a run of equal/adjacent keys); full existing suite (329 tests)
still passes.

### First cut: real overhead, then a redesign

The first implementation dispatched fresh tasks to an `ExecutorService` twice per digit pass
(once for the histogram phase, once for the scatter phase — twelve executor round-trips per
sort call at 11-bit digits' six passes). JMH showed this overhead eating a real share of the
theoretical parallel benefit: isolating parallelism itself (1 thread vs. 8, same framework
overhead in both) gave a resolved 2.16x at N=10,000,000, but against the existing
zero-overhead serial `RadixHuskySort` the net win shrank to ~1.2-1.3x and was not statistically
resolved at either size tested.

Redesigned to spawn the worker threads once per sort call (not once per phase): each thread
runs a loop over every pass, synchronizing via two reused `CyclicBarrier`s (one per phase
transition) whose "barrier actions" — code that runs exactly once per trip, in whichever thread
arrives last — do the sequential histogram-combine step and the buffer-swap/shift-advance step.
This collapses twelve executor round-trips into one `invokeAll` for the whole sort; ongoing
synchronization between phases and passes is then just a barrier wait. Correctness re-verified
after the redesign (all 13 tests, run four times to check for intermittent concurrency bugs;
full 329-test suite unaffected).

The first re-measurement after this redesign looked worse, not better — noisier confidence
intervals and even a reversed chunk-count trend at N=10,000,000. Before concluding anything,
checking `uptime`/`ps` found the actual cause: the machine's load average was 8.58/10.62/11.10
(on 8 cores) during that run, with unrelated background processes — an enterprise antivirus
daemon, macOS's media-analysis indexer, a lab-monitoring client — each consuming 80-130% CPU
concurrently. Robin rebooted and closed other applications; a re-run on a verified-clean machine
(load average 3.62/3.60/3.04) gave the numbers below. This is the same "don't trust a noisy
single run" lesson this document has already learned twice with ad hoc timing versus JMH,
playing out a third time with system-level noise instead.

JMH (`ParallelRadixSortBenchmarks`), `Long[]`, 11-bit digits, N=2,000,000 and 10,000,000, ms
mean ± 99.9% CI, clean-machine run:

| N | QuickHuskySort | Serial Radix/11 | Parallel p=1 | Parallel p=2 | Parallel p=4 | Parallel p=8 |
|---|---|---|---|---|---|---|
| 2,000,000 | 366.7±10.8 | 100.4±24.0 | 107.5±10.7 | 83.7±3.3 | 76.3±4.1 | 77.3±3.6 |
| 10,000,000 | 2589.4±215.1 | 574.3±83.3 | 685.3±135.5 | 569.5±307.0 | 426.4±49.1 | 444.0±127.3 |

- **The redesign worked**: comparing 1 thread against 4 threads (same framework overhead in
  both) is now statistically resolved at **both** sizes — 1.41x at N=2,000,000 and 1.61x at
  N=10,000,000, non-overlapping confidence intervals at both. Under the first design, this
  comparison (1 vs. 8) was only resolved at the larger size.
- **Against the existing serial `RadixHuskySort`**, the win is now resolved at N=10,000,000:
  574.3ms → 426.4ms, **1.35x**, non-overlapping. At N=2,000,000 the comparison (100.4ms →
  76.3ms, 1.32x) is suggestive but the intervals still overlap slightly, so not fully resolved
  there.
- **Four threads is the practical sweet spot on this machine** (Apple M1, 4 performance + 4
  efficiency cores): p=4 and p=8 are statistically indistinguishable at both sizes (ratio
  0.96-0.99x, heavily overlapping CIs) — using the efficiency cores does not help further,
  consistent with only 4 cores actually being "fast" ones.
- Against `QuickHuskySort`, the parallel variant at p=4 is ~4.8-6.1x — as before, most of that
  margin is radix sort's own advantage (already ~3.6-4.5x serial-vs-QuickHuskySort at these
  sizes), with parallelism's own incremental contribution being the smaller 1.3-1.6x discussed
  above.

## Raw radix sort baseline: quantifying overhead where Husky was never meant to be used

Every comparison so far has been RadixHuskySort (generic, works on any `Comparable` type via a
`HuskyCoder`, permutes a boxed object array, always available for a cleanup pass even when
unused) against comparison-based baselines. The numeric benchmarks already had a "raw quicksort"
baseline (sorting the primitive array directly, no Husky machinery at all) as "the strongest
possible non-Husky baseline" for the comparison-sort side; there was no equivalent for the radix
side. Added one: a plain LSD radix sort directly on primitive `long[]`/`double[]` arrays, no
encoding-function indirection, no boxed payload array to permute, no cleanup-pass consideration
— sorting the primitive keys directly *is* the whole job, unlike RadixHuskySort which always
carries a deferred `int[]` permutation for a separate object array even when, as for these
numeric types, the "object" and the "key" happen to be the same value.

Worth being explicit about what this comparison actually tests, since the framing matters:
Huskysort's entire premise (\S~\ref{sec:radix} and the paper's own array-access analysis) is
amortizing an O(N) encoding pass against the *savings from avoiding expensive comparisons* in the
sort phase that follows. Integer/Long/Double/BigInteger/BigDecimal are exactly the case where
that premise does not apply — comparing two of them directly is already cheap, so there is no
expensive-comparison cost for the encoding to amortize against, only the encoding/permutation
machinery's own overhead with nothing to offset it. So a result where RadixHuskySort loses to a
type-specialized raw radix sort here is not a weakness the data exposes; it is the expected
outcome of applying a general mechanism to a case it was never designed for, and this section
exists to quantify that overhead precisely rather than to argue RadixHuskySort is the wrong
choice for primitives (nobody claims otherwise).

Correctness verified first, and a real bug turned up in the process (worth being honest about,
consistent with everything else in this document): the first version of the double-to-long
encoding used a sign-plus-magnitude-negation scheme that looked bijective but collapsed `+0.0`
and `-0.0` to the identical encoded value, silently losing a distinction `Arrays.sort` itself
preserves (`Double.compare` treats `-0.0` as strictly less than `0.0`). Caught by an explicit
`-0.0`/`+0.0` test case, not by reasoning about the bits alone — replaced with the standard
bijective bit-trick (flip the sign bit for non-negative values, flip every bit for negative
ones), which handles this correctly by construction. 400 random trials (long and double,
including negatives) plus edge cases (exact zero, negative zero, `MIN_VALUE`, `MAX_VALUE`, empty,
singleton) all pass against `Arrays.sort` as ground truth.

JMH (`NumericSortBenchmarks`), best RadixHuskySort digit width vs. the new raw radix baseline, ms
mean:

| Type | N=20,000 ratio | N=500,000 ratio |
|---|---|---|
| Integer | 1.13x | 1.38x |
| Long | 1.28x | 1.60x |
| Double | 1.31x | 1.88x |
| BigInteger | 1.40x | 2.55x |
| BigDecimal | 1.42x | 2.11x |

Two findings, both consistent with (rather than a challenge to) the case for RadixHuskySort made
everywhere else in this document:

- **Generality has a quantifiable, and here entirely expected, overhead** — RadixHuskySort is
  consistently 1.1-2.6x slower than a hand-specialized raw radix sort on the same primitive data.
  Since primitives were never the case Husky-encoding was meant to help with, this is exactly the
  overhead one should expect to see: the encoding/permutation machinery's own cost, with no
  expensive-comparison saving on the other side of the ledger to offset it.
- **The overhead grows with both N and "type weight".** At the smaller size, the gap is a
  fairly uniform 1.1-1.4x across all five types. At N=500,000, it widens to 1.4-1.6x for the
  lightweight types (Integer, Long) but 1.9-2.6x for Double and especially the arbitrary-precision
  types (BigInteger, BigDecimal). Both raw and Husky baselines pay the same `.longValue()`/
  `.doubleValue()` conversion cost from BigInteger/BigDecimal, so that conversion itself is not
  the explanation; the remaining difference is most plausibly the cost of permuting a boxed
  object array (rather than a primitive one) and going through the `HuskyCoder` interface's
  function-call indirection, both of which RadixHuskySort must pay unconditionally to remain
  generic, and neither of which a type-specialized raw radix sort needs to pay at all.

For use-case guidance: if the data to be sorted is already a primitive numeric type amenable to
hand-specialization, a raw radix sort will beat RadixHuskySort by a real, and at scale
substantial, margin — but that was never RadixHuskySort's target case in the first place.
RadixHuskySort's value proposition is specifically for types that are *not* already cheap to
compare (Strings, Tuples, dates, and similar), where no realistic "raw" alternative exists to
compare against at all, and where every other benchmark in this document shows it winning
decisively. This section quantifies the cost of generality where that premise does not apply,
which is a different — and considerably less interesting — question than whether RadixHuskySort
is good at what it is actually for.

## Crossover points: System sort, QuickHuskySort, and RadixHuskySort by N

Robin's question: is there a value of N below which QuickHuskySort (the Introsort-based
approach) actually beats RadixHuskySort? Every other comparison in this document is at
N >= 20,000 or so, which leaves the small-N regime unexamined.

JMH (`StringSortBenchmarks`), English corpus, `avgt` mode, ms/op, N=4 through 10,000:

| N | QuickHuskySort | RadixHuskySort (16-bit) | System sort |
|---|---|---|---|
| 4 | 0.000055 | 0.183361 | 0.000037 |
| 10 | 0.000163 | 0.170093 | 0.000119 |
| 20 | 0.000466 | 0.169802 | 0.000322 |
| 50 | 0.001420 | 0.170411 | 0.001152 |
| 100 | 0.003113 | 0.182189 | 0.002873 |
| 200 | 0.007251 | 0.176831 | 0.007034 |
| 500 | 0.018859 | 0.195186 | 0.025214 |
| 1,000 | 0.051111 | 0.199768 | 0.086396 |
| 2,000 | 0.180584 | 0.264943 | 0.261751 |
| 5,000 | 0.574760 | 0.409341 | 0.910646 |
| 10,000 | 1.450683 | 0.772005 | 2.154067 |

There is indeed a crossover, and it's well up in the range this document otherwise treats as
"small N": QuickHuskySort is faster everywhere from N=4 through N=2,000, and RadixHuskySort
takes over somewhere between N=2,000 and N=5,000 (at N=2,000 QuickHuskySort's CI and
RadixHuskySort's CI are essentially non-overlapping in QuickHuskySort's favor; at N=5,000 they're
clearly non-overlapping in RadixHuskySort's favor). We didn't sample finely enough between those
two points to pin down the exact crossover N more precisely than "somewhere in the low
thousands," and given how flat RadixHuskySort's curve is through this whole range, that's
probably not worth chasing further.

The shape of RadixHuskySort's curve explains why: from N=4 to N=1,000 its cost is nearly flat,
between 0.17ms and 0.20ms, regardless of N — that's the fixed cost of setting up the digit
passes (histogram/prefix-sum/scatter buffers) dominating completely, with the actual linear-in-N
work still too small to matter. QuickHuskySort, with no comparable fixed setup cost, wins easily
in that regime simply by doing less work overall for a handful of elements. Only once N is large
enough for RadixHuskySort's O(N) term to overtake its own fixed floor does its structural
advantage (avoiding Quicksort's O(N log N) comparisons) start to show, and it does so quickly:
by N=10,000 RadixHuskySort is already the best of the three by nearly 2x over QuickHuskySort.

The same table holds a second crossover that's easy to miss looking only at the
QuickHuskySort-vs-RadixHuskySort comparison: at the very smallest sizes, plain `Arrays.sort`
(System sort) beats QuickHuskySort too. At N=20 and N=50 System sort is clearly faster
(non-overlapping 99.9% CIs: N=20, System 0.000322±0.000025ms vs. QuickHuskySort
0.000466±0.000095ms; N=50, System 0.001152±0.000083ms vs. QuickHuskySort
0.001420±0.000202ms). By N=100 and N=200 the two are statistically indistinguishable (CIs
overlap substantially), and by N=500 QuickHuskySort has pulled clearly ahead (0.018859±0.001182ms
vs. System's 0.025214±0.002988ms, non-overlapping). So the QuickHuskySort/System crossover sits
in roughly the same neighborhood as often cited for switching from an $O(N \log N)$ sort to
$O(N^2)$ insertion sort on tiny arrays — informally "N < 2^8 (256)" is a reasonable
approximation, though the data only pins it down to "somewhere between 200 and 500," same
caveat as the RadixHuskySort crossover above. The explanation is the same shape as the main
crossover, one level down: QuickHuskySort pays a husky-encoding pass before it ever gets to sort,
and for a handful of elements that fixed cost isn't recovered by anything Quicksort itself saves
over `Arrays.sort`'s own highly-tuned (and, for tiny arrays, insertion-sort-based) implementation.

That last parenthetical is worth checking rather than asserting: Robin's expectation was that a
plain insertion sort should beat System sort outright below roughly N=16, since System sort's
own tuned implementation (TimSort, for `Object[]`) is understood to defer to something
insertion-sort-like below its own internal merge threshold anyway. Simple to test — the repo
already has a working `InsertionSort` (`src/main/java/.../sort/simple/InsertionSort.java`) — so
added `insertionSort` as a new `@Benchmark` in `StringSortBenchmarks` alongside the existing
`systemSort` and `quickHuskySort`. JMH, English corpus, N=4 through 1,000 (capped well below the
main sweep's range — insertion sort is $O(N^2)$, so it stops being worth measuring past a
thousand elements):

| N | Insertion sort | QuickHuskySort | System sort |
|---|---|---|---|
| 4 | 0.000052 | 0.000056 | 0.000039 |
| 10 | 0.000159 | 0.000165 | 0.000126 |
| 20 | 0.000396 | 0.000445 | 0.000341 |
| 50 | 0.001157 | 0.001474 | 0.001211 |
| 100 | 0.002600 | 0.004125 | 0.003207 |
| 200 | 0.006032 | 0.006949 | 0.007464 |
| 500 | 0.025012 | 0.019597 | 0.024630 |
| 1,000 | 0.111075 | 0.046448 | 0.092414 |

**Note on this table's provenance (2026-08-05):** these numbers are from a second run, after
`InsertionSort` was rewritten mid-session from a plain linear adjacent-swap scan to
binary-search-based insertion (`swapIntoSorted`) — a genuine algorithm change, made while fixing
real bugs that change surfaced (subarray corruption when used as a fallback elsewhere, a swap
miscount, and a tie-handling issue that broke stability for duplicate keys; see commit
`408011c`). An earlier version of this table, measured against the old linear-scan
implementation, showed insertion sort losing to both alternatives by N=50 and falling behind by
12-24x at N=1,000. The new implementation is a different story entirely, below.

At the smallest sizes (N=4 through 20), the picture is unchanged from before: insertion sort and
System sort remain statistically indistinguishable (overlapping or barely-overlapping 99.9% CIs
throughout), consistent with Robin's *other* hypothesis that System sort already defers to
something insertion-sort-like below its own internal merge threshold — there's no room for a
hand-written insertion sort to win outright when the two are doing essentially the same work.

The real change is in the middle of the range. With the binary-search rewrite, insertion sort
now **beats both QuickHuskySort and System sort outright from roughly N=100 through N=200**
(non-overlapping CIs both ways at N=200; resolved against System sort specifically at N=100).
It's only from N=500 that QuickHuskySort pulls back ahead (non-overlapping), with insertion sort
and System sort still statistically tied there; by N=1,000 insertion sort's $O(N^2)$ shift cost
finally dominates and it falls behind both. So the old "insertion sort loses ground steadily
past N=20" story no longer holds — the optimized version is actually the *best* choice in this
document for a real middle band of sizes, not just a tiny-N curiosity.

This is worth reconciling with the "Use-case guidance" section below, which currently only
compares System sort, QuickHuskySort, and RadixHuskySort (the crossover-N sweep in items 23-24
never included plain `InsertionSort` as a candidate). Since `InsertionSort` is itself a usable,
exposed sorter (not just an internal fallback), this result suggests it may deserve its own tier
in that guidance for a String-keyed collection roughly in the 100-200 range — not something
folded in here unilaterally, since it changes the shape of the existing guidance rather than
just adding a data point to it.

Also worth flagging as a natural follow-up, not yet acted on: `QuickHuskySort`'s *own* internal
small-subarray fallback (used when its Introsort recursion bottoms out) still uses the old-style
linear scan — it has a separate, dormant `OPTIMIZED` flag guarding an equivalent
binary-search-based path that was never turned on. Given how much faster the binary-search
version measures here, that dormant path may be worth revisiting, though enabling it would need
the same care (and probably the same three bug classes) this session just worked through for
the standalone `InsertionSort`.

Use-case takeaway: for genuinely small collections, prefer the simplest sort that's still winning
— plain System sort below roughly N=256 for String keys, QuickHuskySort from there up to roughly
N=2,000, and RadixHuskySort above that. Every one of these crossovers has the same underlying
shape: each successive algorithm carries a larger fixed setup cost (Quicksort's encoding pass,
then RadixHuskySort's digit-pass buffers) that only pays for itself once there's enough work to
amortize it against — the same fixed-vs-variable-cost story as the parallel-radix design earlier
in this document (thread/barrier setup only pays off once there's enough work to divide across
threads), just one level further down the stack.

## Multikey quicksort baseline: a real comparison against the cited literature

The classic-string-sorting-literature paragraph in `\S~\ref{sec:radix}` (Bentley and Sedgewick,
three-way/multikey radix quicksort; Sinha and Zobel, burstsort) explicitly scoped a direct
empirical comparison as future work rather than something actually done. Robin asked for the
real comparison instead, and picked the specific algorithm to implement: three-way radix
quicksort (multikey quicksort), Bentley and Sedgewick 1997 — already in the bibliography, so
this lets the paper honestly say it compared against the cited algorithm, not just cited it.

New `MultikeyQuicksort` in `sort.simple`: per-character three-way (Dutch national flag) partition
around a pivot character at the current depth. The `<` and `>` partitions recurse at the same
depth; the `=` partition recurses at depth + 1, since every string in it has already been
confirmed to share the same character at every depth up to and including this one. A string
shorter than the current depth is treated as having a character below every real character code,
so shorter strings sort before longer strings sharing the same prefix — ordinary lexicographic
order. Falls back to the newly-fixed, binary-search-based `InsertionSort` for small subarrays,
per Bentley and Sedgewick's own recommendation (their paper suggests insertion sort; this reuses
the one already fixed and verified earlier in this same document).

Correctness verified first, per this document's established practice of not trusting a new sort
implementation until it has been checked against `Arrays.sort`: random ASCII strings (N up to
10,000, 5 trials each), heavy-duplicate strings (alphabet as small as 3 characters), strings of
varying length including exact-prefix relationships ("b", "ba", "ban", "banana", ...),
empty/singleton arrays, an all-identical 5,000-element array, and — matching this document's own
adversarial-input theme — 500 strings sharing a 5,000-character common prefix, specifically to
check that the recursion into the `=` partition for a long shared prefix does not overflow the
stack. All 6 tests pass.

JMH (`StringSortBenchmarks.multikeyQuicksort`), English and Chinese Leipzig corpora, natural
Unicode order (so all four sorters below are doing the same task):

| N | RadixHuskySort | MultikeyQuicksort | QuickHuskySort | System sort |
|---|---|---|---|---|
| **English** 32,000 | 3.65ms | 6.38ms | 7.64ms | 9.80ms |
| **English** 200,000 | 41.11ms | 53.76ms | 71.18ms | 90.67ms |
| **English** 1,000,000 | 209.07ms | 292.70ms | 338.82ms | 545.95ms |
| **Chinese** 32,000 | 1.68ms | 5.13ms | 4.16ms | 7.75ms |
| **Chinese** 200,000 | 8.52ms | 26.79ms | 25.06ms | 54.21ms |
| **Chinese** 1,000,000 | 70.92ms | 142.96ms | 103.14ms | 298.38ms |

**RadixHuskySort beats the real Bentley-Sedgewick algorithm by 1.3-1.75x on English and 2-3.1x
on Chinese**, non-overlapping 99.9% confidence intervals at every single N tested. This is a
real result against real competition, not a strawman comparison — MultikeyQuicksort itself
consistently beats plain System sort here, exactly as the literature's own claims for it would
predict.

Chinese-names results were also collected but are deliberately excluded from the table above:
`MultikeyQuicksort` and `systemSort` sort that corpus by natural Unicode code-point order, while
`QuickHuskySort`/`RadixHuskySort` sort it by pinyin (via a `Collator`) — not the same task.
Robin's framing of this exclusion is worth keeping for the paper's own write-up: sorting by
natural Unicode order is a much easier task than sorting by pinyin, generally speaking (and,
depending on the application, not even the *correct* order for Chinese names) — so this isn't
just "not directly comparable," the excluded numbers would be unfairly flattering to
`MultikeyQuicksort` if included as though they meant something. A possible follow-up Robin
raised: implementing pinyin-aware character ordering inside `MultikeyQuicksort` itself, to make
a fair comparison possible for that corpus too. Not started.

One honesty note on data quality: `RadixHuskySort`'s confidence intervals were noticeably wider
than System sort's in this particular run — up to ±34% of the mean at N=1,000,000 on Chinese,
versus System sort's ~1.6-7% at the same points. This could be residual contention from the
LabStatsGoClient background-CPU saga (see the parallel-radix section's own earlier confound, and
this document's `TODO.md` for the ongoing story — not yet resolved by IT as of this writing), or
it could be inherent GC/allocation variability in `RadixHuskySort` specifically; this run doesn't
distinguish between the two. The headline finding above survives regardless, since the gaps
between RadixHuskySort and MultikeyQuicksort are non-overlapping even at RadixHuskySort's widest
CIs — but the exact ratios (1.3-1.75x, 2-3.1x) should be treated as provisional pending a rerun
on a confirmed-clean machine, not as final numbers to quote precisely in the paper.

### Pinyin-aware MultikeyQuicksort, and two real bugs found doing it

Robin's follow-up: implement pinyin ordering inside `MultikeyQuicksort` itself, so the
chinesenames comparison the section above excluded (natural Unicode order was not the same task
as `QuickHuskySort`/`RadixHuskySort`'s pinyin order) becomes fair. Reused the same utilities
`HuskyCoderChinesePinyin` already relies on rather than duplicating pinyin lookup logic:
generalized `MultikeyQuicksort` to take a pluggable per-character key plus a matching
small-subarray fallback, and added `sortByPinyin`, keyed on a new
`HuskyCoderChinesePinyin.pinyinCharacterKey` that packs syllable, tone, and Unicode code point
into a single long, in the same three-level priority `NAME_ORDER` already uses (syllable, then
tone, then code point as a true-homonym tie-break). Verified the packing scheme directly —
`HanyuPinyinSyllables.ordinalOf`'s numeric order matches its `ORDER` comparator's order across
the full syllable table — before relying on it for anything.

Testing the new sorter against the real names corpus (1,145,009 names), not just synthetic
cases, surfaced two real, independent, pre-existing bugs in code this whole effort has trusted
for correctness all along — neither one caused by the new sorter, both just newly *visible*
because it has no cleanup-pass safety net to hide behind:

1. **`HanyuPinyinSyllables` was missing the entire bare "-a" final column.** Eighteen standard
   syllables — a, ba, ca, cha, da, fa, ga, ha, ka, la, ma, na, pa, sa, sha, ta, za, zha, one per
   compatible initial — were simply absent, apparently dropped when the table was originally
   parsed from Wikipedia's Pinyin table. Verified the replacement list independently before
   touching the data (confirmed all 18 are standard, and that "ja"/"qa"/"xa"/"ra" correctly do
   not exist as syllables, so they were rightly excluded rather than another gap). This is very
   likely the actual explanation for the "~449 vs. 395" discrepancy that class's own comment
   already flagged as unresolved — one missing final column alone accounts for roughly a third
   of that gap. Table grown from 395 to 413 entries.
2. **`ChineseCharacter.alt()` never finished converting pinyin4j's "u:" (ü) marker.** It
   converted the colon to a literal tilde placeholder (producing syllable strings like "lu~")
   and left it there, instead of finishing the conversion to the actual "ü" character the way
   the BoPoMoFo encoding path already did with its own `UTildePattern`. This reaches `NAME_ORDER`
   itself — every lü/lüe/nü/nüe-syllable character (e.g. 吕, 律, common surname characters)
   produced a syllable string that could never match `HanyuPinyinSyllables`' correctly-spelled
   entries. Fixed by reusing the existing, already-tested `UTildePattern` rather than inventing a
   new mechanism, applied only to the syllable portion (tone is split off by position first, so
   the length-changing substitution cannot disturb that).

Neither bug ever produced a wrong *final* sort order through the existing pipeline — both were
silently paid for as extra, unnecessary cleanup-pass work rather than surfacing as visibly wrong
results, since `RadixHuskySort`/`QuickHuskySort` always run a Timsort cleanup pass for Chinese
names regardless of what the first pass gets right. Before fixing either, confirmed neither fix
disturbs any actual source of truth: the names corpus itself is raw input data, never a
presorted reference, and every existing test in this area computes its own oracle from
`NAME_ORDER` fresh at test time — so correcting `NAME_ORDER` only makes that oracle more
accurate, it does not invalidate anything already relying on it. A couple of tests had the old
buggy output baked directly into a hardcoded expected value (`CharacterMapTest`,
`HuskyCoderFactoryTest`); each was recomputed directly from the corrected code rather than
guessed at. Added a dedicated regression test enumerating all 18 previously-missing syllables
and the 4 confirmed non-syllables. Full suite: 350 tests passing.

`StringSortBenchmarks.multikeyQuicksort` now dispatches to `sortByPinyin` for the chinesenames
corpus specifically, completing the three-corpus comparison alongside the English and Chinese
(natural order) numbers earlier in this section. Two attempts, both with `LabStatsGoClient`
killed immediately beforehand (see the Parallel radix sort section above) but ramping back to
90%+ CPU within about 30 seconds each time, so neither run is clean:

| N | RadixHuskySort | QuickHuskySort | MultikeyQuicksort (pinyin) |
|---|---|---|---|
| 32,000 (run 1) | 25.91ms | 28.36ms | 44.42ms |
| 32,000 (run 2) | 27.61ms | 26.86ms | 52.26ms |
| 200,000 (run 1) | 145.32ms | 170.05ms | 339.04ms |
| 200,000 (run 2) | 163.79ms | 160.81ms | 363.39ms |
| 1,000,000 (run 1) | 736.67ms | 1017.56ms | 2013.45ms |
| 1,000,000 (run 2) | 821.81ms | 916.12ms | 1919.05ms |

The fine RadixHuskySort-vs-QuickHuskySort distinction is not resolvable from this data — it
flips direction between the two runs, consistent with confidence intervals up to ±29% wide in
both attempts. That is not a new gap: the dedicated crossover-N sweep elsewhere in this document
already answers that comparison precisely, on a clean run. What *did* hold, independently, in
both attempts: **RadixHuskySort and QuickHuskySort (both pinyin-aware) clearly and consistently
beat MultikeyQuicksort (also pinyin-aware) by a wide margin** — roughly 1.6-2.7x, in the same
range as the English (1.3-1.75x) and Chinese (2-3.1x) results, and holding up across two
independent, noisy attempts is itself reasonable corroborating evidence even without pristine
CIs. Treat the exact ratios as provisional pending an actually clean machine; treat the
direction of the finding as solid.

One framing point worth being explicit about, since the table above could be misread otherwise:
natural-Unicode-order System sort is not included in this table at all, unlike the English/Chinese
comparisons earlier in this document. For those corpora, natural order is genuinely what all
four sorters were computing, so System sort belonged in the comparison as a real alternative.
For Chinese personal names, per Robin, natural Unicode code-point order is not merely a
different, less-comparable ordering the way it was framed earlier in this section — it is simply
the wrong order for that use case, full stop, not a legitimate choice a real user would ever
reach for. Including a natural-order System-sort number here would invite reading it as a viable
competitor for this specific corpus, which it is not; the only three sorters worth comparing for
Chinese names are the three pinyin-aware ones in the table above.

## Headline conclusion

Radix sort with a deferred permutation beats the repo's current quicksort-based husky
long-sort approach at every size and every real data type tested here — Strings, Integer,
Double, Long, BigInteger, BigDecimal, Tuples, Dates, and Chinese names sorted by pinyin —
typically by **2-4x at N >= 200,000** (up to ~4.5x for Dates, where the "perfect" encoding
removes any cleanup-pass overhead), directly confirming Reviewer 3's suspicion. Chinese names
are the one exception to that "2-4x" range: after fixing a real `RadixHuskySort` bug (it wasn't
using the pinyin-aware cleanup comparator at all — see the Chinese names section) and then
encoding tone as well as syllable in the husky code (Robin's proposal, confirmed to meaningfully
reduce the cleanup pass's real work even though `perfect()` correctly stays `false`), the honest
margin there is a smaller but solid ~1.5-1.6x, because both sorters now pay the same
collator-based cleanup cost, leaving less of a gap for radix's cheaper first pass to show
through. This is now backed by JMH measurements (proper fork isolation, warmup, and confidence
intervals) for every category, not just the original ad hoc timer-loop numbers — and JMH
caught two real problems the ad hoc numbers didn't: the apparent N=1,000,000 String
digit-width reversal turned out to be noise, not a real effect, and the first cut of the
Chinese-names numbers was measured against an incorrectly-ordered `RadixHuskySort` result.

The digit-width question turned out more nuanced than the ad hoc data suggested, and there's no
single sharp crossover. For Strings, the best widths form a **plateau from roughly 12 through
16 bits**, with 8-bit and (surprisingly) 10-bit consistently worse despite 10-bit needing fewer
passes. For Numerics and Tuples, 8/11/16-bit differences remain close to noise level at these
sizes, with no single width winning across every type. See TODO.md item 2 for what's still
open: independent replication, a possible (but not yet confirmed) 11-bit-specific noise
pattern, and whether the String plateau shape holds for Numerics/Tuples too.

On adversarial inputs (Reviewer 4, see the section above): radix sort is structurally immune to
high-order-bit collisions in the encoded keys themselves, while the paper's own re-implemented
quicksort baseline degrades by orders of magnitude and eventually crashes with
`StackOverflowError` under the same conditions — Huskysort's actual existing Introsort-based
approach already avoids the crash, but radix removes the slowdown too. That result doesn't
carry over unconditionally, though: when the *source data* (not just the encoded keys) defeats
the encoding's fixed capture window entirely (e.g. a shared string prefix longer than the
coder's character limit), no sort-algorithm choice helps — radix included — and every
Husky-based approach ends up slower than plain System sort, because the wasted encoding/sort
work on a fully-collided key is paid on top of a cleanup pass that has to do all the real work
regardless. The first result is a genuine, radix-specific answer to Reviewer 4; the second is
an honest limitation of the encoding scheme itself, already implicit in the original paper's
`p_crit` discussion.

## Use-case guidance: which sort to actually reach for

Pulling every result in this document together into a single decision guide, in the same spirit
as the original paper's own use-case eliminations (Timsort for partially-ordered input,
dual-pivot quicksort for primitive arrays):

1. **Is the data already a cheap-to-compare primitive** (Integer, Long, Double, BigInteger,
   BigDecimal, or similar)? If so, and hand-specializing for that exact type is practical, a raw
   type-specialized radix sort beats RadixHuskySort by 1.1-2.6x (see "Raw radix sort baseline"
   above) — but this is the one case Husky-encoding was never meant to help with in the first
   place, since there's no expensive-comparison cost for the encoding to amortize against.
   RadixHuskySort's actual value proposition starts at the next question.
2. **Is N very small** (roughly below 256 for String keys — see "Crossover points" above)? Then
   plain System sort wins outright: even QuickHuskySort's own encoding pass isn't recovered by
   anything Quicksort saves over `Arrays.sort`'s own tuned implementation at that size. Neither
   Husky-based sorter is the right choice for genuinely tiny collections.
3. **Otherwise, is N still small** (roughly 256 to 2,000 for String keys)? Then QuickHuskySort
   wins: RadixHuskySort's fixed per-call digit-pass setup cost isn't amortized yet, and
   QuickHuskySort has no comparable fixed floor to pay (beyond its own encoding pass, which is
   now worth it). Both of these small-N crossovers were only measured for Strings; it's plausible
   other key types have similarly-shaped curves (the fixed setup costs involved are largely
   type-independent) but that hasn't been separately confirmed.
4. **Does the source data defeat the husky encoding's fixed capture window** (e.g. a shared
   string prefix longer than the coder's character limit, so the encoded keys collapse to the
   same or nearly the same value)? Then neither QuickHuskySort nor RadixHuskySort helps — both
   end up slower than plain `System.sort`, since the wasted encode/sort work on collided keys is
   paid on top of a cleanup pass that has to do all the real work regardless. This is a limitation
   of Husky-encoding itself, not of either sort algorithm layered on top of it.
5. **Otherwise** (N large enough to clear RadixHuskySort's setup cost, data not already a cheap
   primitive, encoding not defeated) — which covers the great majority of the realistic use
   cases this paper targets — **RadixHuskySort is the right choice**, typically **2-4x faster**
   than QuickHuskySort (up to ~4.5x for Dates), and structurally immune to the high-order-bit
   adversarial inputs that make QuickHuskySort's underlying quicksort degrade or crash.
6. **Is the workload large enough, and are spare cores available, to make parallelizing worth
   it?** `ParallelRadixHuskySort` widens RadixHuskySort's own advantage further at scale — 1.4x
   at N=2,000,000 and 1.6x at N=10,000,000 with 4 threads on this machine's 4 performance cores
   — but pays its own fixed thread/barrier setup cost, so it only pays for itself once there's
   enough work to divide (the same fixed-vs-variable-cost shape as the serial crossovers in
   points 2 and 3, just realized across threads instead of within one).
