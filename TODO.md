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
   win over System sort/QuickHuskySort holds up everywhere; the 8-vs-11-vs-16 ordering at
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
     the existing `// FIXME` in `QuickHuskySortTest.testSortString7` (removed the FIXME; the
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
     reusing the existing System/QuickHuskySort/Radix comparison methods rather than adding
     MSDStringSort/UnicodeMSDStringSort (a different, non-`HuskyCoder`-based sorter family —
     could be added later but was not what this item's JMH-wiring ask was about). Found and
     fixed a real performance bug while first running this: the cleanup-pass pinyin lookup was
     uncached, making the first cut of numbers badly misleading (QuickHuskySort ~150ms,
     RadixHuskySort ~37-38ms at N=20,000) until a simple per-character memoization cache
     dropped that to ~15ms and ~7.5ms respectively (10x and 5x).
   - **2026-07-24 correction — a real correctness bug, found while answering Robin's question
     about where the cleanup pass lives**: `RadixHuskySort`'s convenience constructor hardcoded
     `Arrays::sort` as the cleanup-pass post-sorter, never consulting `HuskyCoder.getCollator()`
     — so for a Collator-supplying coder (`HuskyCoderChinesePinyin`, which always needs the
     cleanup pass since it never claims `perfect()`), every `RadixHuskySort` result for Chinese
     names was silently sorted by natural Unicode-code-point order, not the intended pinyin
     order. Verified empirically (the 16-name canonical test case came out completely wrong).
     `QuickHuskySort` already handled this correctly; `RadixHuskySort` did not, and no existing
     test caught it (`RadixHuskySortTest` never used a Collator-supplying coder;
     `HuskyCoderChinesePinyinTest` never exercised `RadixHuskySort`). Fixed by making the
     convenience constructor check `getCollator()`, matching `QuickHuskySort`'s pattern; added a
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
     QuickHuskySort at N=1,000,000: 1439ms → 1145ms), radix's relative advantage over
     QuickHuskySort widened (as expected, since its already-cheap first pass now leaves even
     less for the shared cleanup cost to dominate), and confidence intervals got meaningfully
     tighter for the best-behaved widths (Radix/11 at N=1,000,000: 1096±199ms → 724±71ms) —
     consistent with TimSort doing genuinely less work, not just running faster by chance.
     Headline margin at scale is now ~1.5-1.6x (Radix/11 vs QuickHuskySort), up from the
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
   (3 of 14 parameter combinations); QuickHuskySort (the actual existing Introsort-based
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

   **Re-checked 2026-08-05**, after item 28's two real `NAME_ORDER`/`HanyuPinyinSyllables` bug
   fixes: 93.43% of all 1,145,008 adjacent pairs agree (1,069,784 agree, 75,224 disagree) —
   essentially unchanged from the original 93.4% finding, despite `NAME_ORDER` itself genuinely
   changing behavior for lü/nü-syllable characters (the second bug). Makes sense on reflection:
   most adjacent name pairs in the corpus are decided by an earlier character before the
   comparison ever reaches an lü/nü-syllable one, so the fix barely moves this particular
   statistic even though 吕 is a common surname. The "-a" syllable fix (the first bug)
   shouldn't have touched this number at all — `NAME_ORDER` never consulted the ordinal table
   in the first place, only the raw `HanyuPinyinSyllables.ORDER` string comparator, which
   doesn't care whether a syllable is in the table.

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

11. **[Yunlu]** **Resolve polyphone pinyin readings using the corpus itself as training data**, rather than
    `ChineseCharacter.alt()`'s current arbitrary choice of `pinyinStrings[0]` (pinyin4j's
    first/default reading). `PinyinHelper.toHanyuPinyinStringArray()` returns *all* valid
    readings for a character; for each polyphone character, check every occurrence in the
    corpus against its immediate neighbors, testing which candidate reading keeps that
    occurrence's local ordering consistent, and take a majority vote across all occurrences.
    Where one reading wins decisively and differs from pinyin4j's default, build a small
    `Map<Character, String>` override table consulted before falling back to the default. Only
    fixes the 0.32% polyphone-driven disagreement quantified in item 9's finding above;
    unrelated to item 10.

    **2026-08-11 finding**: implemented as proposed (`PolyphoneOverrideTrainer`, a main()
    program under src/test/java, generating `src/main/resources/polyphone_overrides.txt`,
    which `ChineseCharacter.alt()` now consults — an override is honored only if it matches
    one of pinyin4j's valid readings for that character, and a missing/empty table reproduces
    the old behavior exactly). Votes are harvested from each adjacent pair's first differing
    character position (shared prefixes cancel; prefix pairs and pairs whose deciding
    characters are both polyphones carry no attributable signal — the latter are retried in a
    second round after round-1 winners are fixed). Two subtleties proved essential: (1) a pair
    is only counted if it *discriminates*, i.e. at least one candidate reading sorts strictly
    consistently with the observed order and at least one strictly inconsistently; and (2) if
    *any* candidate reading ties the opponent on syllable+tone, the whole pair must be
    discarded, because its order may be a stroke-count tie-break (item 10 territory) that
    would otherwise credit the non-tying readings with spurious unanimous votes — without
    exclusion (2), the trained table actually made agreement *worse* than baseline (93.34%,
    −0.09pp). With both rules plus decisiveness thresholds (winner ≠ default, ≥75% of FOR
    votes, ≥10 FOR votes), training learns a 20-entry override table (all of them
    real-name-convention readings, e.g. 肖 xiao4→xiao1, 柏 bo2→bai3, 贲 bi4→ben1, 蔚
    yu4→wei4), and adjacent-pair agreement goes from 1,069,784 / 1,145,008 (93.4303%) before
    to 1,072,555 / 1,145,008 (93.6723%) after — +2,771 pairs (+0.24pp) of the ~0.32%
    (≈3,664 pairs) polyphone-driven disagreement. The residual polyphone disagreement is
    largely context-dependent characters (the same character legitimately read differently in
    different names — surname vs. given-name position, or neighboring-character context),
    which a single global per-character map cannot express by construction. Note that the
    override table encodes *this corpus curator's* reading conventions, learned from the
    corpus's own ordering — a genuinely better fit for sorting this (and similar
    surname-heavy) data than pinyin4j's dictionary-default readings, but not a claim of
    universal correctness. One existing test expectation updated accordingly
    (`ChineseCharacterTest.testConvertToPinyin`: 蔚 in 何欣蔚 now wei4, no longer pinyin4j's
    yu4).

## Paper resubmission (2026-07-24 onward)

The radix-sort backlog above (items 1-8) was groundwork for an actual SIAM ACDA21 resubmission.
The paper source is now in this repo at [paper/HuskySort.tex](paper/HuskySort.tex) (moved from
Robin's OneDrive so editing happens under git), with the four verbatim reviews plus PC decision
archived at [paper/SIAM_ACDA21_Reviews.md](paper/SIAM_ACDA21_Reviews.md). Full phased plan
(reviewer-to-content mapping, sequencing) is tracked as session tasks; see the plan file
referenced in that session, or re-derive from the reviews doc if picking this up cold. Document
format/venue (SIAM template vs. staying with `acmart`) is explicitly deferred until last —
Robin asked Claude Chat for a venue recommendation previously and didn't get one.

12. ~~**Phase A — new algorithmic/experimental content**~~ **DONE.** (answers Reviews 2, 3, 4
    and the PC's "not enough algorithmic innovation" verdict): RadixHuskySort algorithm
    subsection; extend the array-access complexity analysis with a radix term; adversarial-inputs
    appendix section (collapsed high bits + shared-prefix strings, including the
    `StackOverflowError` finding); explicit rebuttal of Review 1's "advantage shrinks with N"
    critique using the new JMH data; generalization-beyond-64-bits paragraph; literature
    paragraph (external-memory/cache-oblivious sorting citations + parallelizability note) with
    two new hand-written `\bibitem` entries in `paper/HuskySort.bbl` (no `.bib` source exists in
    the tarball); new encoding-only JMH benchmark (isolating `huskyEncode` cost) to answer
    Review 1's "did you time the encoding phase" question, folded into
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md) before the
    corresponding paragraph was written.

13. ~~**Phase B — structural reorganization**~~ **DONE.** (Reviews 2, 3): added a new
    "Prior comparison-based sorting algorithms" table to Background, contrasting insertion
    sort/merge sort/quicksort/Timsort before introducing Huskysort, plus a paragraph tying the
    contrast directly to Huskysort's own design choice (quicksort/radix for step 2's
    unordered data, Timsort for step 3's already-close-to-sorted cleanup). Moved the
    `Data Source` subsection out of the start of `Implementation` (which now opens with
    `System Environment`/`Implementation of Algorithm`) into a new first subsection of
    `Test Case and Analysis` (renamed its old content to a sibling `Analysis` subsection), so
    data provenance sits next to the experiments that use it rather than opening the
    implementation discussion. Tied together the previously-disconnected analyses in §3.3
    (`Discussion of $p_{crit}$`, the abstract $T_1$/$T_2$/$T_3$ time model) and §5 (`Analysis`,
    the concrete array-access derivation) with explicit forward/back cross-references, rather
    than merging or rewriting either — they're at different levels of detail (abstract time
    constants vs. concrete array-access counts) and both are worth keeping, they just weren't
    previously acknowledged as the same three-step decomposition.

14. ~~**Phase C — mechanical/presentation fixes**~~ **DONE** (Reviews 1, 3). Read the actual
    screenshot images (`SystemEnivornment.png`, `HS_BM_N.png`, `HS_BM_SE.png`, `HS_BM_SC.png`,
    `HS_BM_T.png`) to confirm they're genuine spreadsheet/console screenshots before replacing:
    Figure 4 (System Environment) became two real tables — the original 2020 machine (Intel
    Core i7 MacBook Pro, JDK 1.8.0\_152) and the current one (Apple M1, JDK 21.0.10) used for the
    radix-sort/JMH work, directly answering the portability question about other machines/JVM
    versions. Figures 6-9 became three real tables (Numeric, Strings, Tuples) using the new,
    statistically rigorous JMH numbers rather than the old ad hoc screenshots. Every table now
    states its units explicitly; all "% faster" framing (including the "Improvements Summary"
    table, the exact one the 42%-vs-1.7x critique targeted) converted to "Nx faster" ratio
    framing. Trimmed the verbatim Java code listings in `Implementation of Algorithm` down to
    prose plus one small constants table — found and removed a genuine copy-paste bug along the
    way (the "stringToLong" listing was an exact duplicate of the "unicodeToLong" listing above
    it, not the actual `stringToLong` method). Full grammar/wording pass per Review 3's itemized
    list: retitled "Explanation of Working" to "Why Huskysort Works"; fixed "long is primitive";
    "code 1" → "Listing 1" throughout; Table 1 (`tab:Comparison`) now states what's counted and
    is right-aligned; added a methodology sentence for Table 2 (`tab:HSComp`, corpus/sampling/
    environment); fixed a premature forward-reference to the "Improvements Summary" table that
    appeared one section before that table was actually introduced. Moved the inline
    per-author-contribution sentence out of the body text (redundant with the existing
    `\authornote`, which already credited the same innovation to the same author). Flagged the
    "broken Bentley citation" issue to Robin directly — **no Bentley citation exists anywhere in
    this v1 source** (confirmed via `grep`), so this couldn't be "fixed" without guessing what
    was meant. **Resolved 2026-07-31**: Robin supplied the actual reference — Bentley and
    McIlroy, "Engineering a sort function," Software: Practice and Experience, Vol. 23, Issue 11,
    pp. 1249-1265, 1993 (DOI `10.1002/spe.4380231105`), confirmed via web search. Added as a new
    hand-written `\bibitem` in `HuskySort.bbl` and cited in two places: the Introduction's
    existing cluster of "sorting still sees new improvements" citations, and — more
    substantively — the Adversarial Inputs section, where it's the classic reference for the
    Dutch-National-Flag three-way partitioning that the crashing `PureDualPivotQuicksort`
    baseline lacks, directly explaining the mechanism behind that crash. The "why Husky?" naming
    gap is now
    resolved too — Robin confirmed it's named for Northeastern's mascot (having wanted "Hash
    Sort" first, but that name was already taken by a different algorithm, Gilreath 2004,
    already cited right at that spot) — added to the paper directly.

15. ~~**Implement a parallel RadixHuskySort variant.**~~ **DONE 2026-07-31.** The paper's
    literature paragraph (§Introduction) and `\S~\ref{sec:radix}` said a parallel radix-sort
    variant was left as future work — Robin wanted this actually built rather than left as a
    claim. New `ParallelRadixHuskySort` (same package): splits each digit pass into contiguous
    chunks, one per thread. Each chunk computes its own local per-bucket histogram independently
    (no synchronization), a short sequential step combines histograms into an exact
    per-(chunk, bucket) starting offset (preserving LSD stability — a chunk's elements always
    land after all lower-numbered chunks' same-bucket elements), then chunks scatter
    independently using only their own precomputed offsets. New `ParallelRadixHuskySortTest` (13
    tests, mirroring `RadixHuskySortTest`'s coverage — small/random strings, a digit-width
    stress sweep, negative numbers, already/reverse-sorted, empty/singleton, and the same
    `Tagged`/`TaggedKeyCoder` stability tests) with one addition specific to parallelism: every
    test also sweeps chunk/thread counts (1, and several others including counts that do not
    evenly divide N and one exceeding N), since a broken histogram-to-offset combine step would
    only show up when chunk boundaries actually split a run of equal/adjacent keys. All pass
    (run four times to check for intermittent concurrency bugs); full existing suite (329 tests)
    still passes too.

    **First implementation and its real overhead**: dispatched fresh tasks to an
    `ExecutorService` twice per digit pass (twelve executor round-trips per sort call at
    11-bit digits). JMH showed this overhead eating a real share of the theoretical parallel
    benefit — isolating parallelism itself (1 thread vs. 8, same framework overhead) gave a
    resolved 2.16x at N=10,000,000, but against the existing zero-overhead serial
    `RadixHuskySort` the net win shrank to ~1.2-1.3x, not statistically resolved at either size.
    Robin asked for ideas to reduce this; recommended (over Java parallel streams, a simpler but
    less targeted fix) a redesign spawning worker threads once per sort call rather than once
    per phase, synchronizing via two reused `CyclicBarrier`s whose barrier actions do the
    sequential combine/swap steps — collapsing twelve executor round-trips into one `invokeAll`
    for the whole sort. Robin agreed to go straight to this redesign.

    **Redesign, and a real detour through a confounded benchmark run**: the first
    re-measurement after the redesign looked worse, not better (noisier CIs, a reversed
    chunk-count trend at N=10,000,000). Checking `uptime`/`ps` before concluding anything found
    the actual cause: unrelated background processes (an enterprise antivirus daemon, macOS's
    media-analysis indexer, a lab-monitoring client) each consuming 80-130% CPU, load average
    8.58-11.10 on an 8-core machine. Robin rebooted and closed other applications; a re-run on a
    verified-clean machine (load average ~3) gave a clean result.

    **Final JMH results** (`ParallelRadixSortBenchmarks`, `Long[]`, 11-bit digits,
    N=2,000,000/10,000,000) — full table and discussion in the "Parallel radix sort" section of
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md). The
    redesign worked: 1-vs-4-threads (same framework overhead) is now statistically resolved at
    both sizes (1.41x, 1.61x), versus only being resolved at the larger size before. Against the
    existing serial `RadixHuskySort`, the win is now resolved at N=10,000,000 (574.3ms →
    426.4ms, 1.35x) and suggestive-but-not-fully-resolved at N=2,000,000 (1.32x). Four threads
    is the practical sweet spot on this machine (Apple M1, 4 performance + 4 efficiency cores) —
    p=4 and p=8 are statistically indistinguishable at both sizes.

    **Paper text DONE 2026-07-31.** New `\subsection{Parallel Radix Sort}` (label
    `sec:parallel-radix`) after the existing Radix Sort Results subsection, with the design
    description, the barrier-based redesign rationale, the results table above, and the same
    honest statistical framing (which comparisons are resolved vs. suggestive). The
    Introduction's literature paragraph now points to this subsection instead of saying "we
    leave as future work," and no longer claims GPU parallelization (only CPU threads were
    actually implemented and measured). Also fixed a real, pre-existing, unrelated bug found
    while editing nearby: a broken cross-reference (`\ref{sec:radix-adversarial-appendix-note}`,
    never actually defined anywhere) in the Radix Sort Results subsection's Chinese-names
    sentence — oddly did not trigger LaTeX's usual undefined-reference warning, but was
    confirmed broken via the `.aux` file; removed the dangling pointer rather than guessing what
    it should have pointed to, since the paper never actually built out a fuller discussion for
    it to reference.

16. ~~**Audit the "3 15/16ths characters" Unicode encoding claim**~~ **DONE 2026-07-31.** The
    claim is accurate and still current — nothing stale about it. Verified empirically (a small
    Python bit-trace, not just reasoning by hand): the final `>>> 1` in `unicodeToLong` exists to
    guarantee a non-negative packed value — without it, any string starting with a character
    whose code point is `>= 0x8000` (common for CJK, e.g. `0x9EC4`) would set the sign bit and
    encode as a *negative* `long`, corrupting simple numeric comparison. The shift sacrifices
    exactly the lowest bit of a true 4th character (confirmed: two 4-character strings differing
    only in that bit collide to the identical code after the shift); for strings of length <= 3
    that bit is always an unused padding zero, so nothing is lost, which is exactly why
    `unicodeCoder` declares `perfect()` only up to 3 characters, not 4. Unrelated to both the
    pinyin coder rewrite (a separate coder entirely) and RadixHuskySort's sign-bit XOR bias
    (harmless but redundant here, since this coder already guarantees non-negative output on its
    own). The only real gap was that the paper stated the claim with no explanation, same pattern
    as the cache-friendliness issue — added a short explanatory clause to
    `paper/HuskySort.tex` covering the mechanism and tying it to the `perfect()` cutoff.

18. **Cite the "quicksort is cache-friendly" claim** (§Why Huskysort Works, near where it used
    to be line 598). **DONE 2026-07-31.** Robin asked whether this bare assertion needed
    justification; recommended against reusing the Bentley & McIlroy citation for it (that paper
    is about partitioning robustness, not cache behavior — would have been a citation mismatch).
    Added the actual standard reference instead: LaMarca and Ladner, "The Influence of Caches on
    the Performance of Sorting," Journal of Algorithms, Vol. 31, Issue 1, pp. 66-104, 1999
    (new `\bibitem` in `HuskySort.bbl`, key `LAMARCA199966`). Added a parenthetical caveat per
    Robin's request: that same paper found *raw* radix sort (sorting keys directly, no deferred
    permutation) has relatively poor cache behavior on 1990s hardware — noted explicitly as a
    different radix-sort design on much older hardware, so it doesn't undercut this paper's own
    radix-sort results. Robin also pointed out Yaroslavskiy's dual-pivot quicksort paper
    (`\cite{Dual-pivot}`, already in the bibliography) discusses caching's role across several
    quicksort variants specifically. Couldn't independently verify the exact passage (the
    bibliography's own URL for it, codeblab.com, is now dead, and the web.archive.org mirror
    isn't fetchable from here) — trusted Robin's direct recollection and added it as a second,
    complementary citation alongside LaMarca & Ladner (general cross-algorithm cache study vs.
    Yaroslavskiy's quicksort-variant-specific one), rather than replacing either.

## Claude Chat assessment and venue decision (2026-08-04)

Robin shared `Huskysort_Revision_Assessment_and_Venue_Recommendations.pdf` (prepared by a
separate Claude Chat session, dated 2026-07-31) — an independent assessment of the revision
against all four original reviews, plus venue recommendations. It confirmed every
reviewer-mapping item above is addressed, flagged three small items, and recommended among SIAM
ACDA27, SEA 2027, ALENEX (unavailable near-term), and JEA.

19. **Venue decided: SIAM ACDA27.** Submission deadline confirmed mid-September 2026 via SIAM's
    own EasyChair CFP page (`easychair.org/cfp/ACDA27`) — the SIAM.org page itself blocks
    automated fetches (403). The detailed CFP (page limits, required LaTeX template/style) is
    **not yet published** as of 2026-08-04 — both the assessment (2026-07-31) and this direct
    check found the same "will be published in Spring 2026" placeholder with no specifics yet.
    This is a real external blocker, not a research gap: the Phase D document-class/template
    swap (item to be added once real requirements exist) cannot start until SIAM actually
    publishes them — expected before the deadline, but not yet available. Worth checking back
    periodically.

20. ~~**Add classic string-sorting literature** (MSD radix sort, three-way/multikey radix
    quicksort, burstsort).~~ **DONE 2026-08-04.** The assessment flagged this as a real gap:
    RadixHuskySort's own contribution invites the comparison more directly than the original
    paper did. New paragraph in `\S~\ref{sec:radix}` (right after the 64-bit-generalization
    paragraph) situating RadixHuskySort relative to this literature — different in kind, not
    degree: a general mechanism for any `Comparable` type via a 64-bit encoding, not a
    string-specialized sorting algorithm — and explicitly scoping a direct empirical comparison
    against these as future work, not something this paper claims to have done. Two new
    hand-written `\bibitem` entries in `HuskySort.bbl` (bibliographic details verified via web
    search, not guessed, consistent with how every other citation this session was added):
    Bentley and Sedgewick, "Fast Algorithms for Sorting and Searching Strings," SODA '97,
    pp. 360-369 (covers MSD radix sort and three-way/multikey quicksort for strings in one
    canonical paper); Sinha and Zobel, "Cache-Conscious Sorting of Large Sets of Strings with
    Dynamic Tries," ACM J. Exp. Algorithmics, Vol. 9, Article 1.5, 2004 (the burstsort paper).

    Two other small items from the assessment, not acted on: the leftover ACM Trans. Graph.
    template boilerplate is already covered by the deferred Phase D template swap above, not a
    separate gap; the Zhang et al. 2016 "quicksort is fastest" claim resting on an arXiv-only
    preprint predates this revision entirely (already in the original 2020 paper) and was noted
    by the assessment as minor — no action taken, flagged here in case it comes up later.

    **Update 2026-08-05:** the direct empirical comparison scoped as future work above has now
    been done — see item 27 below. RadixHuskySort beats the actual Bentley and Sedgewick
    three-way radix quicksort outright, not just in theory.

21. ~~**Rename QuickHuskySort to DutchHuskySort, and PureHuskySort to QuickHuskySort.**~~ **DONE
    2026-08-04.** Robin proposed naming the Introsort-based approach "QuickHuskySort" in the
    paper, but that name already belonged to a different, existing class (plain quicksort, 3-way
    Dutch-national-flag partitioning, no depth-limit/heapsort fallback — unlike the actual
    Introsort-based class this session's benchmarks all used, `PureHuskySort`). Rather than
    create a paper-vs-repo naming mismatch, Robin asked for a two-stage rename instead: the old
    `QuickHuskySort` class became `DutchHuskySort` (its docstring updated to explain the Dutch
    National Flag partitioning the new name refers to), then `PureHuskySort` became
    `QuickHuskySort`. Mechanical rename across the whole codebase (`git mv` for the four affected
    files — the class itself plus its unit and integration tests — content substitution
    everywhere else), done in that specific order to avoid double-substitution. One real gotcha
    hit along the way: the first `sed` pass used `\b` word-boundary syntax, which BSD/macOS sed
    (the default on this machine) does not support the way GNU sed does — it silently matched
    nothing at all rather than erroring, so the "stage 1 done" report the first time through was
    wrong. Caught by verifying with a grep afterward rather than trusting the sed's silent
    success, redone with plain substitution instead. Verified via full recompile and the full
    329-test suite (unchanged) after every stage, not just at the end. Also updated the paper's
    "Introsort approach"/"Introsort-based approach" phrasing throughout to use "QuickHuskySort"
    directly, per Robin's request to stop using vague relative naming.

    A second, easy-to-miss layer surfaced only because Robin explicitly asked "don't forget
    config.ini too": three copies of `config.ini` (main/test/it resources) plus
    `HuskySortBenchmark.java` reference the same rename concept through all-lowercase config-key
    strings (`purehuskysort`, `purehuskysortwithinsertionsort`, `quickhuskysort`,
    `quickhuskyinsertionsort`, and a pre-existing typo'd `quickuskyinsertionsort`, kept as-is
    since fixing typos wasn't part of this request) that the original capitalized-only
    `sed` passes never touched. Applied the same two-stage rename to every one of these, in the
    same order, across all three `config.ini` files and the Java code that reads them, plus the
    JMH benchmark method names that had the same problem (`DateSortBenchmarks`'s
    `quickHuskySort`/`quickHuskySortWithInsertion` methods were silently testing the *new*
    `DutchHuskySort` after the class rename; three other files had methods still named
    `pureHuskySort`). Verified with a direct side-by-side diff of every Java string-literal config
    lookup against every actual `config.ini` key afterward, not just a recompile+retest, since a
    silent key mismatch would not have failed any test — it would have just silently disabled a
    benchmark path. Also fixed one stale, literally-broken command example in
    `doc/Radix Sort Benchmark Results.md` that referenced the old `pureHuskySort` JMH method name.

22. ~~**Raw primitive radix sort baseline for numeric types.**~~ **DONE 2026-08-04.** Robin asked
    whether RadixHuskySort had ever been compared against a "plain old radix sort" the way the
    existing "raw quicksort" baseline compares against comparison-based sorting — it hadn't. New
    `rawLongRadixSort`/`rawDoubleRadixSort` in `NumericSortBenchmarks`: a plain LSD radix sort
    directly on primitive `long[]`/`double[]`, no `HuskyCoder` indirection, no boxed payload
    array, no cleanup-pass consideration — isolating how much RadixHuskySort's generality costs.
    Verified correctness first (400 random trials plus explicit edge cases against
    `Arrays.sort`) and caught a real bug doing it: the first double-to-long encoding
    (sign-plus-magnitude-negation) collapsed `+0.0`/`-0.0` to the identical value, silently
    losing a distinction `Arrays.sort` preserves — replaced with the standard bijective XOR-mask
    bit-trick. Full results and discussion in the "Raw radix sort baseline: quantifying overhead
    where Husky was never meant to be used" section of
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md). Robin's
    framing point after seeing the first draft: Husky isn't designed for sorting primitives, so
    losing to a specialized raw radix sort here is expected, not a weakness the data exposes —
    Husky's whole premise is amortizing an O(N) encoding pass against savings from avoiding
    expensive comparisons, and comparing two primitives directly is already cheap, so there's no
    such saving to amortize against in the first place. Headline numbers: RadixHuskySort is
    consistently 1.1-2.6x slower than the raw baseline, and the gap widens with both N and
    "type weight" (1.1-1.4x for Integer/Long at N=20,000, up to 1.9-2.6x for
    Double/BigInteger/BigDecimal at N=500,000), most plausibly from boxed-object-array
    permutation and `HuskyCoder` interface indirection rather than the
    `.longValue()`/`.doubleValue()` conversion itself (both baselines pay that identically). Not
    yet written into the paper — feeds into the use-case-guidance section (item 24 below), since
    it directly answers "when would you NOT want RadixHuskySort": when the data is already a
    primitive numeric type worth hand-specializing for — which was never RadixHuskySort's target
    case to begin with.

    A process note worth recording too: an earlier attempt at the companion crossover-N sweep
    (item 23 below) got corrupted mid-run because `mvn -Pjmh clean package` was run for this
    item's code changes while that sweep was still executing in the background — `clean` deletes
    `target/benchmarks.jar`, which JMH's forked subprocesses depend on for every fork, not just
    the first one, so everything after the point of the rebuild failed with
    `ClassNotFoundException`. Lesson: never rebuild the benchmarks jar while any JMH run using it
    is still in flight.

23. ~~**Crossover-N sweep: QuickHuskySort vs RadixHuskySort.**~~ **DONE 2026-08-04**, updated
    2026-08-04. Robin asked whether there is a value of N below which QuickHuskySort beats
    RadixHuskySort. First attempt corrupted (see item 22's process note); clean rerun
    (`StringSortBenchmarks`, English corpus, N=4 through 10,000) found a real crossover:
    QuickHuskySort wins everywhere from N=4 through N=2,000, RadixHuskySort takes over somewhere
    between N=2,000 and N=5,000 (not sampled finely enough to pin down more precisely, and
    probably not worth chasing further given how flat RadixHuskySort's cost curve is through
    that whole range). RadixHuskySort's per-call cost is nearly flat (0.17-0.20ms) from N=4 to
    N=1,000 — its digit-pass setup cost dominates completely below a few thousand elements, the
    same fixed-vs-variable-cost shape as the parallel-radix design's thread/barrier setup
    (item 16 above), just serial rather than threaded.

    A second crossover in the same table, easy to miss looking only at the
    QuickHuskySort-vs-RadixHuskySort comparison: Robin pointed out that plain System sort beats
    QuickHuskySort too, for N below roughly 2^8 (256). Confirmed against the same data: System
    sort is clearly faster (non-overlapping 99.9% CIs) at N=20 and N=50, the two are
    statistically indistinguishable by N=100-200, and QuickHuskySort has pulled clearly ahead by
    N=500 — so the real crossover sits somewhere between 200 and 500, consistent with the "N <
    2^8" approximation. Same underlying shape one level down: QuickHuskySort pays its own
    husky-encoding pass before it sorts, and for a handful of elements that fixed cost isn't
    recovered by anything Quicksort saves over `Arrays.sort`'s own tuned (insertion-sort-based,
    for tiny arrays) implementation. Full tables and discussion in the "Crossover points" section
    of [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md). Feeds
    directly into the use-case-guidance section (item 24 below): System sort below ~256,
    QuickHuskySort from ~256 to ~2,000, RadixHuskySort above.

24. ~~**Write use-case guidance section synthesizing findings.**~~ **DONE 2026-08-04**, updated
    2026-08-04. Pulled together every comparison in this document's tracking file into a single
    decision guide, new "Use-case guidance" section at the end of
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md), in the same
    spirit as the original paper's own use-case eliminations (Timsort for partially-ordered
    input, dual-pivot quicksort for primitive arrays). Six-point guide: (1) already-cheap
    primitive types → hand-specialized raw radix sort, not RadixHuskySort (item 22 above) — but
    that was never RadixHuskySort's target case; (2) very small N (below ~256 for Strings) →
    plain System sort, since even QuickHuskySort's encoding pass isn't recovered at that size;
    (3) small-but-not-tiny N (~256 to ~2,000 for Strings) → QuickHuskySort (item 23 above);
    (4) source data defeats the encoding's fixed capture window entirely → neither Husky-based
    sorter helps, both lose to plain System sort (see "Adversarial inputs" section); (5)
    otherwise (the majority case this paper targets) → RadixHuskySort, typically 2-4x faster
    than QuickHuskySort; (6) large workload with spare cores → `ParallelRadixHuskySort` widens
    the advantage further (item 16 above), but only once there's enough work to amortize its own
    thread/barrier setup cost.

    **2026-08-14 update — folded into the paper, and reconciled with item 25**: new
    `\subsection{Use-Case Guidance}` (`sec:usecase`) in `paper/HuskySort.tex`, after
    `sec:parallel-radix` and before the Conclusion. The doc's own six-point guide above was
    reconciled first (this section had explicitly flagged the insertion-sort tier as not yet
    folded in — see item 25) into a seven-point guide with `InsertionSort` as its own tier
    (String keys, roughly N=100-200), then that reconciled version was written into the paper as
    prose organized by crossover point rather than as a numbered list, to match the paper's own
    style. The already-cheap-primitive point (item 22) was left out of the paper's version
    deliberately — that benchmark itself was never written into the paper, so citing its
    conclusion without the supporting data would be citing something the paper doesn't actually
    show; still available in the doc's own guide for anyone who reads that far.

25. ~~**Insertion sort small-N benchmark.**~~ **DONE 2026-08-04.** Robin expected plain insertion
    sort to beat System sort below roughly N=16, while separately noting System sort likely
    already defers to something insertion-sort-like internally for small arrays — asked to check
    since it's simple to benchmark (the repo already has a working `InsertionSort`). Added
    `insertionSort` as a new `@Benchmark` in `StringSortBenchmarks` alongside `systemSort` and
    `quickHuskySort`; JMH, English corpus, N=4 through 1,000 (capped there — insertion sort is
    $O(N^2)$, not worth measuring further out). At the smallest sizes (N=4-20), insertion sort
    and System sort are statistically indistinguishable (overlapping 99.9% CIs throughout),
    consistent with Robin's *other* hypothesis — that System sort already defers to something
    insertion-sort-like below its own internal merge threshold — rather than his first
    hypothesis (insertion sort should win outright below ~16).

    **Update 2026-08-05:** `InsertionSort` was rewritten mid-session, from a plain linear
    adjacent-swap scan to binary-search-based insertion (`swapIntoSorted`), while fixing three
    real bugs that change surfaced (subarray corruption when used as a fallback elsewhere, a
    swap miscount, and a tie-handling issue breaking stability for duplicate keys) — see item 21
    below and commit `408011c`. That's a genuine algorithm change, not just a bug fix, so the
    benchmark was rerun. Result: the new implementation is roughly 4-10x faster than the old one
    at N>=100 (block-copy shifts via `System.arraycopy` beat one-at-a-time swaps), and it now
    **beats both QuickHuskySort and System sort outright from roughly N=100 through N=200**
    (non-overlapping CIs), not just tying at the smallest sizes as before. QuickHuskySort only
    pulls back ahead at N=500; insertion sort's $O(N^2)$ shift cost finally dominates by
    N=1,000, where it falls behind both again. `QuickHuskySort`/`RadixHuskySort` numbers
    elsewhere in this document are unaffected (`QuickHuskySort` has its own separate, unchanged,
    still-linear-scan insertion-sort fallback, `OPTIMIZED=false`).

    **2026-08-14: reconciled into the "Use-case guidance" section (item 24)** — `InsertionSort`
    now has its own tier there (String keys, roughly N=100-200), and that reconciled guidance is
    now in the paper itself (see item 24's 2026-08-14 update). Still open: `QuickHuskySort`'s own
    dormant `OPTIMIZED` flag guards an equivalent binary-search-based fallback that was never
    turned on — given how much faster it measures here, that may be worth revisiting (with the
    same care around the same three bug classes). Full table and discussion in the "Crossover
    points" section of
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md).

26. **Cloud (AWS) run for larger-than-local-capacity benchmarks.** Not started, low priority,
    may or may not happen before the deadline. Motivation is a genuine capability gap, not just
    avoiding the local machine's CPU contention (see item 25's LabStatsGoClient saga): Robin's
    machine has 16GB RAM and 8 cores (4 performance + 4 efficiency), which caps how far N can go
    for the String/numeric/tuple comparisons and how many threads `ParallelRadixHuskySort`'s
    scaling question can actually be tested against locally (already found 4 threads is the
    practical ceiling on this machine, item 16 above — an open question whether that holds with
    real many-core hardware). If it happens, a one-off large-memory/many-core instance (e.g.
    AWS's Graviton `r7g`/`c7g` family) for a few hours would answer both: does RadixHuskySort's
    advantage over QuickHuskySort hold or grow past N=1-10M, and does parallel radix scale past 4
    threads with more cores actually available. Robin's co-author Yunlu works at AWS and would
    likely be the one to actually set this up and run it.

    **2026-08-14 update — the LabStatsGoClient contention is not fixable locally.** Robin's IT
    session confirmed the offending software is deployed via a machine image, not an individual
    install, so it cannot be updated or removed without a full re-image; that can't happen until
    Robin is back in Boston, and even then not likely before October given IT's typical
    scheduling. This closes off the "just get IT to fix the local machine" option entirely for the
    foreseeable future, strengthening the case for this item (or at least a rerun of the
    still-provisional, contention-affected numbers flagged in items 15, 27, and 28) once an AWS
    session is actually feasible.

27. ~~**Real empirical comparison against three-way radix quicksort.**~~ **DONE 2026-08-05.**
    Item 20's classic-string-sorting-literature addition scoped a direct empirical comparison as
    future work rather than something the paper had actually done. Robin asked for the real
    comparison instead of just the reasoned argument already in `\S~\ref{sec:radix}`, and picked
    the specific algorithm: three-way radix quicksort (multikey quicksort), Bentley and
    Sedgewick 1997 — already cited in the bibliography.

    New `MultikeyQuicksort` in `sort.simple`: per-character three-way (Dutch-flag) partition
    around a pivot character at the current depth, `<`/`>` partitions recurse at the same depth,
    the `=` partition recurses at depth+1, a string shorter than the current depth is treated as
    having a character below every real one (so shorter strings sort first, matching ordinary
    lexicographic order). Falls back to the newly-fixed `InsertionSort` for small subarrays, per
    Bentley and Sedgewick's own recommendation. Verified correctness first (6 new tests: random
    strings, heavy duplicates, varying-length/prefix relationships, empty/singleton,
    all-identical, and an adversarial 5,000-character shared-prefix case checking recursion
    depth doesn't overflow the stack) before trusting any benchmark — same discipline as every
    other new sort implementation this session.

    JMH (`StringSortBenchmarks.multikeyQuicksort`, English and Chinese Leipzig corpora, natural
    Unicode order so all four sorters are doing the same task): **RadixHuskySort beats the real
    Bentley-Sedgewick algorithm by 1.3-1.75x on English and 2-3.1x on Chinese**, non-overlapping
    99.9% CIs at every N tested (32K/200K/1M). MultikeyQuicksort itself beats plain System sort
    consistently, so this is a real result against real competition, not a strawman. Chinese
    names results were also collected but are excluded from this comparison — `MultikeyQuicksort`
    and `systemSort` sort that corpus by natural Unicode order while `QuickHuskySort`/
    `RadixHuskySort` sort by pinyin (via a Collator), so it is not the same task for that corpus.
    One honesty note: `RadixHuskySort`'s CIs were noticeably wider than System sort's in this
    run (up to ±34% at N=1M on Chinese) — plausibly residual LabStatsGoClient contention (see
    item 25's saga, not yet resolved by IT as of this writing), plausibly RadixHuskySort's own
    GC/allocation variability, not fully distinguished. The headline finding survives regardless
    (gaps are non-overlapping even at the widest CIs), but exact ratios should be treated as
    provisional pending a rerun once the machine is confirmed clean.

    **2026-08-14 update — fully folded into the paper now.** The English/Chinese natural-order
    comparison above was already in `\S~\ref{sec:radix}` by the time the conclusion rewrite (that
    this item's note used to be blocked on) was finished; this update adds the remaining piece —
    item 28's pinyin-aware chinesenames comparison — as a new paragraph right after it, including
    the CI-width honesty caveat (up to $\pm$29\%, finer RadixHuskySort-vs-QuickHuskySort
    distinction not resolvable from this data) rather than quoting the ~1.6-2.7x margin as a
    clean number. Robin's own observation about why natural-order System sort stays out of that
    comparison (sorting Chinese names by natural Unicode order is a much easier, and not even
    correct, task compared to pinyin order) is preserved in the new paragraph's framing too.

28. ~~**Pinyin-aware MultikeyQuicksort, and two real pre-existing pinyin bugs found doing it.**~~
    **DONE 2026-08-05.** Robin asked for item 27's followup: implement pinyin ordering inside
    `MultikeyQuicksort` itself, reusing the same utilities `HuskyCoderChinesePinyin` already
    uses rather than duplicating pinyin lookup logic, so the excluded chinesenames comparison
    from item 27 becomes fair. Generalized `MultikeyQuicksort` to take a pluggable per-character
    key plus a matching small-subarray fallback (natural order's existing `InsertionSort`-based
    fallback untouched, so item 27's already-committed benchmark numbers stay valid); added
    `sortByPinyin`, keyed on a new `HuskyCoderChinesePinyin.pinyinCharacterKey` that packs
    syllable/tone/code-point into one long, same three-level priority as `NAME_ORDER`. Verified
    the packing scheme directly (`ordinalOf`'s numeric order matches `ORDER`'s comparator order
    across the full syllable table) before relying on it.

    Testing against the real names corpus (not just synthetic cases) surfaced two real,
    independent, pre-existing bugs in code this whole effort had trusted for correctness —
    neither caused by the new sorter, both just newly *visible* because of it:

    - **`HanyuPinyinSyllables` was missing the entire bare "-a" final column** — 18 syllables
      (a, ba, ca, cha, da, fa, ga, ha, ka, la, ma, na, pa, sa, sha, ta, za, zha), one per
      compatible initial, apparently dropped when the table was originally parsed from
      Wikipedia's Pinyin table. Verified the replacement list independently (18 confirmed
      standard, plus confirmed "ja"/"qa"/"xa"/"ra" correctly do not exist) before touching the
      data. Very likely the actual explanation for the "~449 vs. 395" discrepancy that class's
      own comment already flagged as unresolved — one missing column accounts for roughly a
      third of that gap. Table grown from 395 to 413 entries.
    - **`ChineseCharacter.alt()` never finished converting pinyin4j's "u:" (ü) marker** — it
      converted the colon to a literal tilde placeholder ("lu~") and left it there, instead of
      finishing the conversion to the actual "ü" character the way the BoPoMoFo encoding path
      already did with its own `UTildePattern`. This reaches `NAME_ORDER` itself, the comparator
      this whole effort has treated as ground truth: every lü/lüe/nü/nüe-syllable character
      (e.g. 吕, 律, common surname characters) produced a syllable string that could never match
      `HanyuPinyinSyllables`' correctly-spelled entries. Fixed by reusing the existing
      `UTildePattern`, applied only to the syllable portion (after tone is split off by
      position, so the length-changing substitution cannot disturb that).

    Neither bug ever produced a wrong *final* sort order through the existing pipeline — both
    were silently paid for as unnecessary cleanup-pass work, not visibly wrong results, since
    `RadixHuskySort`/`QuickHuskySort` always run a Timsort cleanup pass for Chinese names
    regardless. They only became visible now because the new pinyin-aware sorter has no such
    safety net.

    Confirmed neither fix disturbs any actual source of truth before making them: the names
    corpus itself is raw input data, never a presorted reference, and every existing test in
    this area computes its own oracle from `NAME_ORDER` fresh at test time — so correcting
    `NAME_ORDER` only makes that oracle more accurate, it does not invalidate anything. Updated
    a couple of tests that had the old buggy output baked into a hardcoded expected value
    (`CharacterMapTest`, `HuskyCoderFactoryTest`), recomputing each new value directly from the
    corrected code. Added a dedicated regression test enumerating all 18 previously-missing
    syllables and the 4 confirmed non-syllables, plus corpus-scale coverage for the new sorter
    (all 1,145,009 real names, not just synthetic samples). Full suite: 350 tests passing.
    Committed as `d9be40f`.

    `StringSortBenchmarks.multikeyQuicksort` now dispatches to `sortByPinyin` for the
    chinesenames corpus specifically (natural order still used for english/chinese), making the
    comparison item 27 excluded finally fair.

    **Update 2026-08-07:** collected two attempts, `LabStatsGoClient` (item 25) killed
    immediately before each, both ramping back to 90%+ CPU within ~30 seconds regardless — so
    neither run is clean, but the headline held independently both times: RadixHuskySort and
    QuickHuskySort (both pinyin-aware) clearly beat MultikeyQuicksort (also pinyin-aware) by
    roughly 1.6-2.7x, same range as the English/Chinese results. The finer RadixHuskySort-vs-
    QuickHuskySort distinction flips direction between the two runs and isn't resolvable from
    this data — not a new gap, the dedicated crossover-N sweep already answers that precisely on
    a clean run. Full tables and discussion in the "Multikey quicksort baseline" section of
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md).

    One framing note from Robin, applied to that write-up: natural-Unicode-order System sort is
    deliberately *not* included in the chinesenames comparison table, unlike the English/Chinese
    tables. For Chinese personal names, natural Unicode order isn't just a different, less
    comparable ordering — it's the wrong order outright, not a real option anyone would choose,
    so presenting a System-sort number there would wrongly suggest it as a viable competitor.
    Waiting for a confirmed-clean machine before trusting any chinesenames-pinyin timing
    comparison.
