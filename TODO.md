# RadixHuskySort follow-up tasks

Backlog from the 2026-07-22 session that added `RadixHuskySort` (see
[doc/Husky sort radix task brief.md](doc/Husky%20sort%20radix%20task%20brief.md) for the
original motivation, and [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md)
for the benchmark numbers this backlog refers to).

1. ~~**Replace the custom `Benchmark`/`SorterBenchmark` harness with JMH.**~~ **DONE
   2026-07-22.** New `jmh` Maven profile (`mvn -Pjmh clean package`, off by default) plus four
   benchmark classes under `src/jmh/java` covering Strings/Numerics/Tuples/Dates — see
   [doc/JMH Benchmarks.md](doc/JMH%20Benchmarks.md) for how to run them. Old harness left
   in place, unchanged. Along the way this also incidentally completed item 5 below (Dates are
   now covered via `DateSortBenchmarks`), and fixed a latent bug in
   `HuskySortBenchmarkHelper.getWords` that only showed up once benchmarks ran from inside a
   shaded jar (it resolved resources to a filesystem path and used `FileReader`, which breaks
   inside a jar; now reads via `getResourceAsStream`). Ran the String suite under JMH and it
   resolved the original motivating question: the N=1,000,000 English "Radix/8 wins" reversal
   does not survive proper measurement (Radix/11's own CI there is wider than its mean) — see
   the "JMH update" section in
   [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md). Radix's
   win over System sort/PureHuskySort holds up everywhere; the 8-vs-11-vs-16 ordering at
   N=1,000,000 specifically is genuinely not resolved by this data, not "settled in favor of
   8-bit" as the ad hoc numbers implied. Numerics/Tuples/Dates haven't been run under JMH yet
   (only smoke-tested) — running them is what would let item 2 below use JMH-quality data too.

2. **Finer digit-width sweep (10/12/13/14-bit).**
   Locate the actual crossover point between "fewer passes" and "count-array fits in
   cache" rather than inferring it from only 8/11/16-bit data points. Now that item 1 is done,
   do this sweep under JMH from the start rather than the old harness.

3. **Decide the fate of the "common words" benchmark** (`3000-common-words.txt`).
   Recommendation: deprioritize as a headline case. Short strings are already cheap to
   compare (undercutting Husky Sort's whole value proposition), and the corpus's ~3,000
   unique words sampled with replacement into 200K-1M element arrays causes artificial
   duplicate-heavy skew — likely the main cause of the noisiest results we saw. Keep the
   config flag available, but treat it as a "known-weak-case" sanity check, not a
   Table/Figure-worthy number.

4. **Wire RadixHuskySort into the Chinese-names/pinyin comparison path.**
   `benchmarkUnicodeStringSorters*` in `HuskySortBenchmark.java` currently only compares
   PureHuskySort / MSDStringSort / UnicodeMSDStringSort.

5. ~~**Wire RadixHuskySort into the date/`LocalDateTime` sorter benchmarks.**~~ **DONE
   2026-07-22** via `DateSortBenchmarks` (JMH) — see item 1. The *old* harness's
   `runDateTimeSortBenchmark` ternary is untouched (not worth it now that JMH covers this).

6. **Add an explicit stability test for RadixHuskySort.**
   LSD counting sort is inherently stable, and the original task brief flagged this as a
   possible simplification to the paper's Section 6.1 (quicksort instability) discussion —
   not yet verified in a test.

7. **Broader systematic adversarial/skewed-encoding testing (Reviewer 4's critique).**
   We have one narrow "collapsed high bits" unit test in `RadixHuskySortTest`. The brief
   wants more: shared-prefix strings, poor-entropy high-order digits, and a real comparison
   of how radix vs. quicksort degrade differently under those conditions.

8. **Draft the short write-up the task brief's "Deliverable" section asks for**, as
   groundwork for the resubmission response to Reviewer 3: whether radix beats the current
   approach on real data, which digit width wins at what N, and whether it changes the
   adversarial-input story.
