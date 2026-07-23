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
   8-bit" as the ad hoc numbers implied. **2026-07-23:** also ran Numerics/Tuples/Dates under
   JMH (previously only smoke-tested) — same "radix wins everywhere" conclusion holds, with
   tighter/more trustworthy numbers than the ad hoc run (which likely under-warmed the JIT:
   e.g. Integer System sort dropped from 136ms ad hoc to 94ms under JMH). See "JMH update"
   sections throughout
   [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md).

2. ~~**Finer digit-width sweep (10/12/13/14-bit).**~~ **DONE 2026-07-23** for Strings (added
   `radixHuskySort10/12/13/14` to `StringSortBenchmarks`, ran under JMH). Finding: **no single
   crossover point — a plateau from roughly 12 through 16 bits**, with 8-bit and (surprisingly)
   10-bit consistently worse despite 10-bit needing fewer passes. See the "Finer digit-width
   sweep" section in
   [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md) for the
   full table and caveats (including a tentative, not-yet-confirmed observation that 11-bit
   showed the widest confidence interval in 3 of 6 rows — more than any other width, but a
   small enough sample that it could still be coincidence). **Still open:** the same sweep for
   Numerics/Tuples, and independent replication (different day/session) of the String sweep
   before trusting the 11-bit observation either way.

3. ~~**Decide the fate of the "common words" benchmark**~~ **DONE 2026-07-23.** Removed
   `commonwords` from `StringSortBenchmarks`' default `@Param` corpus list — a plain
   `java -jar target/benchmarks.jar StringSortBenchmarks` run now only covers english/chinese.
   Still runnable explicitly as a known-weak-case sanity check
   (`-p corpus=commonwords`, documented in
   [doc/JMH Benchmarks.md](doc/JMH%20Benchmarks.md)). The old harness's `config.ini` already
   defaulted this off (`english = false`) so no change needed there. Rationale unchanged: short
   strings are already cheap to compare (undercutting Husky Sort's whole value proposition),
   and the corpus's ~3,000 unique words sampled with replacement into 200K-1M element arrays
   causes artificial duplicate-heavy skew — likely the main cause of the noisiest results seen
   in [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md).

4. **Wire RadixHuskySort into the Chinese-names/pinyin comparison path.** In progress,
   2026-07-23.
   - **Stage 1 (done):** built the syllable *order* first (Robin's insight — pinyin syllables
     form a compact alphabet of about 400 symbols ordered by English spelling, not the
     wasteful "spell it out as ASCII text" approach the encoder used). New
     `HanyuPinyinSyllables` class (`sort/huskySortUtils`) holds all 395 standard Hanyu Pinyin
     syllables (sourced from Wikipedia's Pinyin table, parsed 2026-07-23; Robin recalled "~449"
     from memory, approximately right per his own account, discrepancy not resolved but does
     not affect the bit-width math either way) in correct pinyin alphabetical order, plus an
     O(1) ordinal lookup. 10 tests in `HanyuPinyinSyllablesTest`.
   - **Stage 2 (done):** rewrote `HuskyCoderChinesePinyin`'s Hanyu encoding to pack each
     character's syllable ordinal into 9 bits (up to 7 characters/64-bit long), dropping tone
     entirely (never claims `perfect()`, always relies on the cleanup pass). Also fixed a
     second, more serious latent bug found along the way: `perfect()` previously returned
     `true` unconditionally, meaning the cleanup pass never ran at all for pinyin sorting,
     regardless of name length or real collisions. Added `getCollator()` returning a proper
     pinyin-aware `Collator` (character-by-character: syllable spelling, then tone as a
     per-character tie-break — the "Xiandai Hanyu Cidian" convention, not the "compare whole
     word's spelling first" ABC-dictionary convention; see
     https://en.wikipedia.org/wiki/Pinyin_alphabetical_order for the distinction). This fixed
     the existing `// FIXME` in `PureHuskySortTest.testSortString7` (removed the FIXME; the
     test now passes) and required updating one magic-number test
     (`HuskyCoderFactoryTest.testChineseEncoderPinyin1`) to the new encoding's value. New
     `HuskyCoderChinesePinyinTest`: real-corpus stress test (3000 names) plus 200 small random
     trials cross-validated against the trusted comparator, a long-name test exercising the
     7-character capacity limit, and a tone-only-collision test (妈/麻/马/骂, all "ma").
     Found along the way: true homonyms (identical syllable AND tone, e.g. 郗/奚, both "xi1")
     need stroke-count as a final tiebreak per the Wikipedia page; neither the old nor new
     code implements that, so such pairs are only guaranteed "correctly sorted", not a specific
     stable permutation — reflected in the test design, not a bug.
   - **Still to do:** the actual JMH wiring (RadixHuskySort alongside PureHuskySort /
     MSDStringSort / UnicodeMSDStringSort on `Chinese_Names_Corpus.txt`), and a second dialect
     (Bopomofo/Zhuyin, already stubbed as dead code in `HuskyCoderChinesePinyin.encodeBoPoMoFo`
     — wanted eventually per Robin, not immediately; the design is parameterized by syllable
     table so this should be additive when it happens).

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
