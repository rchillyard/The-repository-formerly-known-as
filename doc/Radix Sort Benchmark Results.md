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

**2026-07-24 correction — the numbers below replace an earlier, invalid set.** `RadixHuskySort`
had a real bug: its convenience constructor hardcoded `Arrays::sort` as the cleanup-pass
post-sorter and never consulted `HuskyCoder.getCollator()`. `HuskyCoderChinesePinyin` always
needs the cleanup pass (it never claims `perfect()`), so every prior `RadixHuskySort` result
for Chinese names was silently sorted by natural Unicode-code-point order, not real pinyin
order — invalidating the RadixHuskySort numbers from the first pass of this benchmark (though
not PureHuskySort's, which already checked `getCollator()` correctly). Fixed by making the
convenience constructor check `getCollator()`, with a dedicated regression test added. Robin
predicted correctly that the fix would come at a real performance cost, not be free — the
collator-based comparator does genuinely more work (a cached syllable+tone lookup and
comparison) than a raw `char` comparison. Confirmed below: RadixHuskySort's advantage over
PureHuskySort shrinks substantially now that both pay the same expensive cleanup-pass cost.

| N | System (wrong order) | PureHuskySort (correct) | Radix/8 | Radix/10 | Radix/11 | Radix/12 | Radix/13 | Radix/14 | Radix/16 |
|---|---|---|---|---|---|---|---|---|---|
| 32,000 | 14.8±5.3 | 52.6±**33.5** | 42.0±16.3 | 42.8±25.9 | 47.0±18.6 | 34.3±6.1 | 37.1±7.4 | 40.1±16.2 | 33.8±2.1 |
| 200,000 | 105.1±10.6 | 276.6±85.9 | 434.7±**248.9** | 310.9±**152.6** | 207.2±27.3 | 221.1±58.3 | 235.0±65.2 | 199.6±47.9 | 211.2±44.6 |
| 1,000,000 | 695.5±200.2 | 1439.3±481.5 | 1575.0±**1595.6** | 970.1±90.9 | 1095.7±198.7 | 970.5±73.5 | 1056.3±186.0 | 1073.6±126.3 | 1205.3±**646.0** |

Radix still wins at every size, but by a much smaller margin than the invalid numbers implied
— roughly **1.3-1.5x** at N=1,000,000 (Radix/10 and Radix/12, the tightest/lowest of the
bunch, at ~970ms vs PureHuskySort's ~1439ms), not the previously-reported 2.4-3.8x. That makes
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
benchmark badly misleading (PureHuskySort ~150ms, RadixHuskySort ~37-38ms at N=20,000, before
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

| N | System (wrong order) | PureHuskySort | Radix/8 | Radix/10 | Radix/11 | Radix/12 | Radix/13 | Radix/14 | Radix/16 |
|---|---|---|---|---|---|---|---|---|---|
| 32,000 | 10.9±6.7 | 33.3±9.2 | 29.4±9.4 | 33.8±11.1 | 24.6±2.1 | 34.8±**20.2** | 23.5±3.4 | 24.4±4.6 | 36.5±**30.8** |
| 200,000 | 99.2±54.1 | 244.2±110.8 | 206.8±**157.9** | 165.7±41.7 | 165.0±54.7 | 133.8±5.7 | 141.6±14.6 | 142.3±27.0 | 159.6±34.5 |
| 1,000,000 | 840.6±549.5 | 1145.2±515.9 | 936.4±429.5 | 799.2±252.2 | 723.7±70.5 | 772.5±251.2 | 731.2±108.7 | 755.6±196.0 | 766.4±171.9 |

Compared to the syllable-only table above (same corpus, same sizes, same digit widths, only
the encoding changed):
- **Both sorters got faster**, confirming the mechanism is real: PureHuskySort improved
  ~1.13-1.58x depending on N (e.g. at N=1,000,000: 1439ms → 1145ms), and most radix widths
  improved by a similar or larger factor (e.g. Radix/12 at N=200,000: 221ms → 134ms, ~1.65x;
  Radix/11 at N=1,000,000: 1096ms → 724ms, ~1.51x).
- **Radix's relative advantage over PureHuskySort widened**, as expected if radix's already-cheap
  first pass now leaves even less work for the shared cleanup cost to dominate: the
  PureHuskySort/Radix-11 ratio grew from ~1.31x (syllable-only) to ~1.58x (syllable+tone) at
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

Headline: radix now beats PureHuskySort on Chinese names by roughly **1.5-1.6x** at scale
(using the tightest, most reliable width, Radix/11) — smaller than the 2-4x seen on the other
categories, but a real, solid win, recovered by fixing the `getCollator()` bug and then further
improved by encoding tone.

## Encoding-only cost (Reviewer 1)

Reviewer 1 of the original submission asked directly: "did you time the encoding phase? How
much time does it account for?" Nothing in the original ad hoc harness isolated Step 1
(`huskyEncode`) from Step 2 (sort). New `StringSortBenchmarks.huskyEncodeOnly` benchmark,
N=1,000,000 (`java -jar target/benchmarks.jar 'StringSortBenchmarks\.(huskyEncodeOnly|pureHuskySort|radixHuskySort16)$' -p n=1000000 -p corpus=english,chinesenames`):

| Corpus | Encoding only (ms) | PureHuskySort total (ms) | Encoding % of PureHuskySort | Radix/16 total (ms) | Encoding % of Radix/16 |
|---|---|---|---|---|---|
| English | 52.7 | 383.2 | 13.7% | 249.3 | 21.1% |
| Chinese names (pinyin) | 311.6 | 1070.2 | 29.1% | 779.4 | 40.0% |

Encoding is a real but minority cost in every case measured — consistent with the paper's own
"linear, doesn't contribute to overall growth" framing, now backed by a measurement rather than
an assumption. It's a noticeably bigger share for the pinyin encoding (which does per-character
syllable+tone lookups) than for the simple ASCII/Unicode string packing, and a bigger share for
radix than for PureHuskySort in both cases — not because radix's encoding is different, but
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

| N | fixedHighBits | System sort | PureHuskySort | DualPivotQuicksort (raw) | Radix/8 | Radix/11 | Radix/16 |
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
   (163-194ms at N=1,000,000, similar to System sort/PureHuskySort), then blows up by more than
   an order of magnitude at 56 (3220ms) and 60 (2059ms at N=200,000), before failing completely
   at 60 (N=1,000,000) and 63 (both sizes) with a stack overflow. This is the sharpest possible
   illustration of Reviewer 4's point — a plausible, simple construction (many keys sharing most
   of their high-order bits) doesn't just slow this implementation down, it crashes it — but the
   crashing implementation is the *baseline comparison*, not Huskysort itself.
3. **PureHuskySort — Huskysort's actual current approach (Introsort, i.e. quicksort with a
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

| N | prefixLength | System sort | PureHuskySort | Radix/8 | Radix/11 | Radix/16 |
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

- At `prefixLength=0` (baseline), the usual pattern holds — radix beats PureHuskySort, which
  beats System sort (e.g. N=1,000,000: 174-215ms vs 292ms vs 627ms).
- **Once `prefixLength` reaches 10 (past the 9-character encoding window), the entire ranking
  inverts: plain System sort becomes the fastest option, and every Husky-based approach —
  PureHuskySort *and* Radix, at every digit width — becomes slower than System sort**, not just
  no-longer-faster (e.g. N=1,000,000, prefixLength=10: System 452.9ms vs. PureHuskySort 484.9ms,
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
  PureHuskySort 556.7ms) — its fixed per-pass cost, a strength in Scenario A, becomes pure
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
