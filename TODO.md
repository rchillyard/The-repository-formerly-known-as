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

4. ~~**Wire RadixHuskySort into the Chinese-names/pinyin comparison path.**~~ **DONE
   2026-07-23** (Bopomofo dialect deliberately excepted, see below).
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
     code implements that. Robin suggested falling back to Unicode code point order in that
     case instead of leaving the comparator's result as a tie — implemented as stage 2b (see
     below) rather than left as a "correctly sorted, not necessarily one specific permutation"
     caveat.
   - **Stage 2b (done):** `NAME_ORDER` now falls back to Unicode code point order for true
     homonyms (Robin's suggestion — not stroke-count-accurate, but the CJK Unified Ideographs
     block's code point order is itself derived from historical radical/stroke-ordered
     national encoding standards, so it is a deterministic, non-arbitrary approximation, not a
     meaningless one). This makes `NAME_ORDER` a genuine strict total order over distinct
     strings, so `HuskyCoderChinesePinyinTest`'s corpus-stress tests were strengthened back to
     exact-permutation matches against the oracle (previously weakened to "correctly sorted"
     to tolerate the homonym ties) — including one new dedicated test for the fallback itself.
   - **Stage 3 (done):** JMH wiring. Added `"chinesenames"` as a `StringSortBenchmarks` corpus
     option (`Chinese_Names_Corpus.txt`, 1,145,009 unique names, using `chineseEncoderPinyin`),
     reusing the existing System/PureHuskySort/Radix comparison methods rather than adding
     MSDStringSort/UnicodeMSDStringSort (a different, non-`HuskyCoder`-based sorter family —
     could be added later but was not what this item's JMH-wiring ask was about). Found and
     fixed a real performance bug while first running this: the cleanup-pass pinyin lookup was
     uncached, making the first cut of numbers badly misleading (PureHuskySort ~150ms,
     RadixHuskySort ~37-38ms at N=20,000) until a simple per-character memoization cache
     dropped that to ~15ms and ~7.5ms respectively (10x and 5x).
   - **2026-07-24 correction — a real correctness bug, found while answering Robin's question
     about where the cleanup pass lives**: `RadixHuskySort`'s convenience constructor hardcoded
     `Arrays::sort` as the cleanup-pass post-sorter, never consulting `HuskyCoder.getCollator()`
     — so for a Collator-supplying coder (`HuskyCoderChinesePinyin`, which always needs the
     cleanup pass since it never claims `perfect()`), every `RadixHuskySort` result for Chinese
     names was silently sorted by natural Unicode-code-point order, not the intended pinyin
     order. Verified empirically (the 16-name canonical test case came out completely wrong).
     `PureHuskySort` already handled this correctly; `RadixHuskySort` did not, and no existing
     test caught it (`RadixHuskySortTest` never used a Collator-supplying coder;
     `HuskyCoderChinesePinyinTest` never exercised `RadixHuskySort`). Fixed by making the
     convenience constructor check `getCollator()`, matching `PureHuskySort`'s pattern; added a
     dedicated regression test (`testChineseNamesUseCollatorNotNaturalOrder`, digit widths
     8/11/16). Robin predicted correctly that the fix would be slower, not free: the
     collator-based comparator does real work (syllable+tone lookup, even cached) vs. a raw
     `char` comparison, and a quick check confirmed a real slowdown (Radix/11 at N=200,000:
     ~86ms buggy/wrong-order vs ~275ms correct — about 3.2x). All corrected numbers are in
     [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md).
   - **2026-07-24 encoding improvement — Robin's proposal**: since `perfect()` has to stay
     `false` regardless (true homonyms remain possible no matter how many characters are
     encoded), why not encode tone as well as syllable? It can't buy back skipping the cleanup
     pass, but it should reduce how much real work that pass does — dropping tone left every
     group of names sharing a syllable (huge for common surnames) in arbitrary relative order
     after the first pass, forcing real sorting work during cleanup; encoding tone means the
     first pass already gets almost everything right except rare true-homonym pairs, letting
     TimSort's adaptive behavior make the cleanup pass much cheaper. Implemented as 12
     bits/character (9 syllable + 3 tone), capacity dropping from 7 to 5 characters (still
     comfortable margin over "4 common, 5 is about the practical maximum" for real names, per
     Robin). Confirmed empirically, not just theoretically: both sorters got faster (e.g.
     PureHuskySort at N=1,000,000: 1439ms → 1145ms), radix's relative advantage over
     PureHuskySort widened (as expected, since its already-cheap first pass now leaves even
     less for the shared cleanup cost to dominate), and confidence intervals got meaningfully
     tighter for the best-behaved widths (Radix/11 at N=1,000,000: 1096±199ms → 724±71ms) —
     consistent with TimSort doing genuinely less work, not just running faster by chance.
     Headline margin at scale is now ~1.5-1.6x (Radix/11 vs PureHuskySort), up from the
     collator-fix-only ~1.3-1.5x. Full corrected tables in
     [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md).
   - **Still to do:** a second dialect (Bopomofo/Zhuyin, already stubbed as dead code in
     `HuskyCoderChinesePinyin.encodeBoPoMoFo`) — wanted eventually per Robin, not immediately;
     the design is parameterized by syllable table so this should be additive when it happens.
     Otherwise, item 4 is complete.

5. ~~**Wire RadixHuskySort into the date/`LocalDateTime` sorter benchmarks.**~~ **DONE
   2026-07-22** via `DateSortBenchmarks` (JMH) — see item 1. The *old* harness's
   `runDateTimeSortBenchmark` ternary is untouched (not worth it now that JMH covers this).

6. ~~**Add an explicit stability test for RadixHuskySort.**~~ **DONE 2026-07-24.** Added 4
   tests to `RadixHuskySortTest` using a `Tagged` payload (a coarse, heavily-duplicated `key`
   for ordering plus a `tag` field recording original index, unaffected by ordering): many
   duplicate keys, a sweep across digit widths (8/11/16), negative keys, and the strongest
   case (every element ties) which must reproduce the exact original order. Used a
   deliberately "perfect" test coder (`TaggedKeyCoder`) so the cleanup pass never runs,
   isolating the property to RadixHuskySort's own first pass rather than `Arrays.sort`'s
   already-known stability. All pass, confirming LSD counting sort's inherent stability holds
   in this implementation — the paper's Section 6.1 simplification is on solid footing.

7. ~~**Broader systematic adversarial/skewed-encoding testing (Reviewer 4's critique).**~~
   **DONE 2026-07-24.** New `AdversarialSortBenchmarks` (JMH), two scenarios: (A) synthetic
   `Long[]` with a swept number of high-order bits held fixed/identical, (B) real English words
   with a swept-length shared prefix prepended. Full write-up in the "Adversarial inputs"
   section of
   [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md). Headline:
   two different failure modes. (A) When keys collide in high-order bits but the encoding is
   still otherwise informative, RadixHuskySort is essentially immune (flat cost by
   construction) while the paper's own re-implemented dual-pivot-quicksort baseline degrades by
   orders of magnitude and then crashes outright with `StackOverflowError` at the extreme end
   (3 of 14 parameter combinations); PureHuskySort (the actual existing Introsort-based
   approach) avoids the crash but radix removes even the slowdown. (B) When the *source data*
   (a shared string prefix ≥ the coder's 9-character capture window) defeats the encoding
   entirely, no sort-algorithm choice helps — every Husky-based approach, radix included, ends
   up slower than plain System sort, since the wasted first-pass work is paid on top of a
   cleanup pass that has to do all the real sorting anyway. The first finding is a genuine
   radix-specific answer to Reviewer 4; the second is an honest limitation of the encoding
   scheme itself, already implicit in the original paper's own `p_crit` discussion.

8. ~~**Draft the short write-up the task brief's "Deliverable" section asks for.**~~ **DONE**
   (satisfied incrementally) — the three deliverable questions are all answered in
   [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md): radix beats
   the current approach at every real size/type tested (Headline conclusion section); digit
   width has no single crossover, a plateau from ~12-16 bits for Strings, no consistent winner
   for Numerics/Tuples (same section, and the "Finer digit-width sweep" section); and yes, it
   changes the adversarial-input story, in two different directions depending on where the
   adversarial structure lives (Adversarial-input headline, above). This groundwork now feeds
   directly into the paper resubmission itself, which is a separate, broader effort covering
   all reviewers' comments (not just Reviewers 3 and 4, which is all
   [Husky sort radix task brief.md](Husky%20sort%20radix%20task%20brief.md) covers).

9. **Track down and document the source of `Chinese_Names_Corpus.txt`.** No provenance
   exists anywhere in the repo: the commit that added it (`3d8576c`, July 2022) just says
   "Added Chinese_Names_Corpus", with no source URL, license, or attribution, and there's
   nothing in README.md, doc/, or the file itself either. Robin's best guess (2026-07-23) is
   that it may have come from his Chinese coauthor — worth confirming and then adding a
   one-line note (a comment near `HuskySortBenchmark.CHINESE_NAMES_CORPUS`, or a small
   `NOTICE`/attribution entry) so this doesn't come up again. Matters for the paper
   resubmission too, since a corpus without a documented source is a citation gap.

   **2026-07-23 finding**: `Chinese_Names_Corpus.txt` turns out to be genuinely pre-sorted by
   pinyin (93.4% of all 1,145,008 adjacent pairs agree with our `NAME_ORDER` comparator) — a
   real, independent oracle, not just raw data. The 6.6% disagreement splits cleanly into two
   causes, both already-known limitations now precisely quantified: 6.24% (of all pairs) are
   true-homonym tiebreak differences (the file appears to use stroke count; we fall back to
   Unicode code point), and 0.32% are genuine polyphone misreadings (`ChineseCharacter.alt()`
   always takes `pinyinStrings[0]`, pinyin4j's first/default reading, which sometimes differs
   from the reading the corpus's original curator intended). See items 10-11 below for
   proposed fixes to each. Note: using the file's own order as a *stronger correctness oracle*
   for sort-algorithm tests (shuffle a copy, check the exact original order is recovered) would
   actually be a worse test than the current self-consistency check — any reasonably-sized
   sample will likely hit a homonym/polyphone pair and fail spuriously, for reasons unrelated
   to whether the sort itself is correct.

10. **Replace the Unicode-code-point homonym tiebreak with genuine stroke-count order**, using
    the Unicode Unihan database's `kTotalStrokes` (or `kRSUnicode` for full radical+stroke)
    property as a lookup table. Confirmed via Unicode's own documentation
    (https://unicode-org.github.io/unicode-reports/tr38/tr38.html) that stroke count is *not*
    encoded in the code point's bits — a bitmask on the code point cannot recover it. The
    "radical-stroke sort key" Unicode documents is a constructed 64-bit key built by looking up
    Unihan properties per character (bits 23-30 = KangXi radical, bits 17-22 = residual stroke
    count, bits 0-19 = code point as a final tiebreak only) — not something extractable
    arithmetically. Fixing this needs embedding Unihan data (freely available as part of the
    standard Unicode Character Database), not a clever trick. Only affects the 6.24%
    homonym-tiebreak disagreement quantified in item 9's finding above; unrelated to item 11.

11. **Resolve polyphone pinyin readings using the corpus itself as training data**, rather than
    `ChineseCharacter.alt()`'s current arbitrary choice of `pinyinStrings[0]` (pinyin4j's
    first/default reading). `PinyinHelper.toHanyuPinyinStringArray()` returns *all* valid
    readings for a character; for each polyphone character, check every occurrence in the
    corpus against its immediate neighbors, testing which candidate reading keeps that
    occurrence's local ordering consistent, and take a majority vote across all occurrences.
    Where one reading wins decisively and differs from pinyin4j's default, build a small
    `Map<Character, String>` override table consulted before falling back to the default. Only
    fixes the 0.32% polyphone-driven disagreement quantified in item 9's finding above;
    unrelated to item 10.

## Paper resubmission (2026-07-24 onward)

The radix-sort backlog above (items 1-8) was groundwork for an actual SIAM ACDA21 resubmission.
The paper source is now in this repo at [paper/HuskySort.tex](paper/HuskySort.tex) (moved from
Robin's OneDrive so editing happens under git), with the four verbatim reviews plus PC decision
archived at [paper/SIAM_ACDA21_Reviews.md](paper/SIAM_ACDA21_Reviews.md). Full phased plan
(reviewer-to-content mapping, sequencing) is tracked as session tasks; see the plan file
referenced in that session, or re-derive from the reviews doc if picking this up cold. Document
format/venue (SIAM template vs. staying with `acmart`) is explicitly deferred until last —
Robin asked Claude Chat for a venue recommendation previously and didn't get one.

12. **Phase A — new algorithmic/experimental content** (answers Reviews 2, 3, 4 and the PC's
    "not enough algorithmic innovation" verdict): RadixHuskySort algorithm subsection; extend
    the array-access complexity analysis with a radix term; adversarial-inputs appendix section
    (collapsed high bits + shared-prefix strings, including the `StackOverflowError` finding);
    explicit rebuttal of Review 1's "advantage shrinks with N" critique using the new JMH data;
    generalization-beyond-64-bits paragraph; literature paragraph (external-memory/
    cache-oblivious sorting citations + parallelizability note) with two new hand-written
    `\bibitem` entries in `paper/HuskySort.bbl` (no `.bib` source exists in the tarball); new
    encoding-only JMH benchmark (isolating `huskyEncode` cost) to answer Review 1's "did you
    time the encoding phase" question, folded into
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md) before the
    corresponding paragraph is written.

13. **Phase B — structural reorganization** (Reviews 2, 3): prior-algorithms comparison passage
    in Background before introducing Huskysort; move Data Source subsection out of the start of
    Implementation; consolidate analysis currently split between §3.3 (`p_crit`) and §5 (Test
    Case and Analysis).

14. **Phase C — mechanical/presentation fixes** (Reviews 1, 3): convert Figures 4 and 6-9 from
    image screenshots to real LaTeX tables using the new JMH numbers; state units (ms) on every
    table and switch "% faster" framing to "Nx faster" ratio framing throughout; trim verbatim
    Java code listings in favor of the existing Algorithm pseudocode; grammar/wording pass per
    Review 3's itemized list; move the inline per-author-contribution sentence out of the body
    text (already covered by `\authornote`s); flag the "broken Bentley citation" issue to Robin
    — **no Bentley citation exists anywhere in this v1 source** (confirmed via `grep`), so this
    can't be "fixed" without guessing what was meant.
