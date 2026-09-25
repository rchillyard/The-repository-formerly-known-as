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

9. ~~**Track down and document the source of `Chinese_Names_Corpus.txt`.**~~ **DONE 2026-08-18.**
   Originally: no provenance
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

   **2026-08-18: source found.** Yunlu tracked it down (with help from his own Claude session) to
   [wainshine/Chinese-Names-Corpus](https://github.com/wainshine/Chinese-Names-Corpus)
   (Apache License 2.0, 4,325 stars), specifically
   `Chinese_Names_Corpus/Chinese_Names_Corpus（120W）.txt` at commit
   `95b1a185ae8b180d8030ba41ce2aea9214ca733e`. Verified independently rather than just trusting
   the claim: the upstream file is 1,145,012 lines — 3 header/attribution lines ("By@萌名NameMoe",
   a date, a blank line) followed by exactly 1,145,009 names, matching our resource file line for
   line once those header lines are stripped. Attribution added as a doc comment on
   `HuskySortBenchmark.CHINESE_NAMES_CORPUS`; the paper's Data Source section (§5.1) should cite
   it too, closing the citation gap noted below.

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
The paper source is now in this repo at [paper/RadixHuskySort.tex](paper/RadixHuskySort.tex) (moved from
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
    now in the paper itself (see item 24's 2026-08-14 update). Full table and discussion in the
    "Crossover points" section of
    [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md).

    **2026-08-14: `QuickHuskySort`'s dormant `OPTIMIZED` flag, revisited and resolved.** This
    flag guards `QuickHuskySort`'s own separate binary-search-based `swapIntoSorted`, used only
    for the small-subarray fallback inside its Introsort recursion (`sizeThreshold=16`, so this
    path never runs on more than 17 elements at a time). Turned out to still carry the third of
    the three bug classes commit `408011c` fixed elsewhere: that commit fixed the subarray-corruption
    bug (`from` not threaded through the binary search) in *this exact copy* of `swapIntoSorted`
    too, but the tie-handling/stability fix only ever reached `ComparisonSortHelper`'s copy (used
    by the standalone `InsertionSort`), never this one — so flipping `OPTIMIZED` on as-is would
    have shipped a real, silent stability bug for duplicate keys. Fixed by porting the same
    scan-past-ties logic (comparing `longs[]` values, matching this class's own convention of
    comparing via the pre-encoded long codes rather than the objects themselves). Added a
    dedicated stability regression test (`QuickHuskySortTest.testInsertionSortStableForDuplicateKeys`,
    a `Tagged`-payload/duplicate-`longs[]` array, mirroring `InsertionSortCorrectnessTest`'s
    pattern) — the existing tests all use distinct random Strings, so none of them would have
    caught this. Full suite (355 tests) passes with `OPTIMIZED=true`.

    With the bug fixed, benchmarked whether turning it on is actually worth it (JMH,
    `StringSortBenchmarks.quickHuskySort`, English corpus, $N=1{,}000{,}000$): no measurable
    improvement (371.6±158.1ms optimized vs. 367.0±24.2ms baseline on the first pair of runs,
    heavily overlapping either way). Checked `uptime`/`ps` before trusting that, given how wide
    the optimized run's CI was, and found the same contention pattern this document has hit
    before: load average 6.9-8.7, with Microsoft Teams (~48% CPU), `WindowServer` (~44%), and
    `LabStatsGoClient` (~26%, see item 26's IT update — unfixable until a re-imaging appointment,
    not available before October) all running. So this comparison is not clean enough to trust
    precisely, but the finding is consistent with the best available clean data: the standalone
    `InsertionSort` benchmark above already found binary-search insertion statistically
    indistinguishable from a plain scan at exactly this size regime ($N=4$-$20$), which is exactly
    the range `QuickHuskySort`'s own fallback operates in ($\le 17$ elements). Decision: keep the
    bug fix (real correctness issue in previously-dormant code, worth having regardless), leave
    `OPTIMIZED=false` (no evidence it helps, some evidence it might not be worth the added binary-search
    overhead at this scale), and leave a note in the flag's own comment to revisit only if a
    clean machine becomes available.

26. ~~**Cloud (AWS) run for larger-than-local-capacity benchmarks.**~~ **DONE 2026-08-18.**
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

    **2026-08-18: Yunlu ran the full JMH suite on an AWS `c7g.4xlarge` (Graviton3, 16 vCPU, 30
    GiB, Amazon Linux 2023, Corretto JDK 21), confirmed idle beforehand (load average
    0.36/0.36/0.27) — PR #62, merged. All 81 benchmark methods, full parameter sweep, 385 result
    rows, 2h37m wall time. Report: [doc/JMH Benchmark Results 2026-08-17.md](doc/JMH%20Benchmark%20Results%202026-08-17.md);
    raw CSV committed alongside it since `target/` is gitignored. Reviewed by spot-checking every
    headline number directly against the CSV (not just trusting the write-up) — all matched
    exactly, including the 6 wide-CI rows (all `DualPivotQuicksort` on boxed types) and the 3
    absent `StackOverflowError` rows.

    Answers both open questions this item was created for:
    - **Item 1's N=1,000,000 String question, settled**: the old ad hoc harness's noisy
      N=1,000,000 English reversal was harness noise, not real. Under proper JMH fork
      isolation/warmup, RadixHuskySort/16 beats QuickHuskySort by 2.51x (English) / 2.85x
      (Chinese) and System sort by 4.21x / 5.85x at N=1,000,000, with median relative CI width
      still under 2% at that size (1.3% at 32K, 1.8% at 200K, 2.0% at 1M) — the algorithm gap is
      two orders of magnitude larger than the noise floor.
    - **Item 16's parallel-scaling question, answered with real many-core hardware**: with 16
      real vCPUs available (vs. 4 performance cores on the local Apple M1), scaling *still*
      flattens past 2-4 threads — p=1 to p=8 is only 1.46x at N=10,000,000 (980.9 to 672.5
      ms/op), consistent with the local finding rather than an artifact of core scarcity. The
      whole parallel family still beats QuickHuskySort by roughly 6-9x regardless of thread
      count, since most of that margin is radix's own serial advantage.

    Other findings: radix wins 2-6x across every category at largest N tested (Numerics, Tuples,
    Dates — Dates the largest single margin at ~4.9x); RadixHuskySort stays structurally flat
    (30-75ms) across the full collapsed-high-bits adversarial sweep while the paper's own
    from-scratch dual-pivot quicksort baseline degrades over 20x and then crashes on 3 of 14
    combinations; and the chinesenames exception (System sort "wins" at N=1M) is explicitly
    called out as an artifact of System sort computing the wrong, cheaper natural-Unicode order
    rather than a real loss for Husky. One genuine caveat carried forward: this is an ARM/aarch64
    host, so absolute ms/op numbers here are not comparable to the x86/Apple-Silicon numbers
    elsewhere in [doc/Radix Sort Benchmark Results.md](doc/Radix%20Sort%20Benchmark%20Results.md)
    — only relative comparisons within this run should be trusted across documents.

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

## Post-submission (deferred until after 2026-09-15)

Items below were found during the ACDA27 preparation and deliberately not acted on, because they
touch code whose output appears in the paper's tables and the submission deadline is close. None
is a defect; all are hardening or generalisation.

29. **Give `Tuple.huskyCode` a field-width budget check, and name its widths.** Found 2026-09-07
    while answering Robin's question about packing order. `HuskySortBenchmark.Tuple.huskyCode()`
    packs `birthYear - 1850` in 8 bits, `zip` in 17, and a masked English code of `name` in 38.

    **It is correct, and was verified rather than assumed.** As generated, `birthYear - 1850`
    ranges 0–170 against 255 available, `zip` maxes at 99,999 against 131,071, and 8 + 17 + 38 =
    63, so the sign bit stays clear and the worst-case code is 6.15e18 against a `Long.MAX_VALUE`
    of 9.22e18. Nothing to fix.

    **What is missing is anything that would catch a change.** The total is 63 of 63 bits, so
    widening *any* field by one bit overflows into the sign bit, the codes go negative, and the
    ordering inverts rather than degrading — the failure mode `PermitCoder.codeOf` was written
    specifically to avoid. The widths are literals (`17`, `38`) inside a shift expression rather
    than named constants, so there is nothing to assert against.

    Worth noting the contrast, because it is the point that generalises. The class *does*
    document the property that is hard to get right — "the fields must be coded in the same order
    of priority as the comparison", written 2020-08-16 — and leaves undocumented the one that is
    merely arithmetic. That is the right way round for a human writing it once and the wrong way
    round for a library: the ordering obligation is what a derivation can discharge automatically,
    and the budget is what a check can catch. See appendix A.4 of the paper.

    Deferred rather than done because `Tuple` supplies the Tuples row of Tables `HS_BM_T` and
    `RadixImprovements`; changing the class would mean re-running to prove the numbers had not
    moved.

30. **Promote `PermitCoder.encodeString` and `codeOf` into a reusable field encoder.** Both are
    `private static`, hold no permit-specific state, and are the only order-preserving
    ordered-alphabet string packer in the codebase — `HuskyCoderFactory`'s `stringToLong` masks
    raw char values instead, which does not give the padding and out-of-range properties a
    composite key field needs. A visibility change and a home, plausibly `HuskyCoderFactory`.
    Prerequisite for item 31.

31. **A composite-key coder: combinator first, derivation second.** The paper's appendix A.4
    argues this is what adoption would require, and Robin's framing on 2026-09-07 was that it is
    not essential for the paper but would be for anyone taking the method up. Two stages, and the
    second is the one that matters: a builder that folds N field encodings most-significant-first
    while accumulating shifts and failing fast on the 64-bit budget; and, where a type's ordering
    is already derived from its field declarations (Java records, Python dataclasses with an
    ordering), deriving the encoding from that same declaration — which removes by construction
    the possibility that packing order and comparison order disagree, and lets `perfect()` be
    computed rather than asserted and separately verified by a corpus test.

32. **Complete the two thin bibliography entries' provenance note.** Both were completed on
    2026-09-07 and neither blocks anything; recorded only so the reconstruction of
    `paper/sample-base.bib` from `HuskySort.bbl` is not mistaken later for the original file. The
    original `.bib` was never committed and BibTeX had been failing silently for some time.

33. ~~**No parallel-versus-parallel comparison on strings.**~~ **WIRED 2026-09-16**, measurement
    requested as 10b. `ParallelStringSortBenchmarks` is a new class holding a `p1/p2/p4/p8` sweep on
    the automatic digit width, fixed-11 at p8, a serial reference and a parallel baseline, over the
    existing `StringSortBenchmarks.StringState` (reused, not duplicated, so corpora, seed and
    sampling semantics match the serial numbers exactly). Kept separate from `StringSortBenchmarks`
    so it can run without re-measuring that class's dozen serial benchmarks. Smoke-tested on
    `english` and `chinesenames`; the numbers themselves are request 10b, since this machine cannot
    produce usable parallel figures (see item 34).

    **Found while wiring it, and FIXED 2026-09-16: `StringSortBenchmarks.systemSortParallel` called
    the no-Comparator `Arrays.parallelSort` for every corpus, including `chinesenames`.** The husky
    sorts order that corpus by pinyin, so that row compared two different orderings --- the same
    apples-to-oranges problem `multikeyQuicksort` avoids there by calling `sortByPinyin`, and the
    same one request 6 existed to fix for the *serial* system sort. It now uses
    `HuskyCoderChinesePinyin.NAME_ORDER` for that corpus, as does the new class's baseline. Robin's
    reason for fixing rather than documenting it (2026-09-16): we should never compare a pinyin sort
    against a system sort that does not use the pinyin comparator, and per Yunlu the code-point
    ordering is in any case almost never used for Chinese --- where the ordering is not pinyin it is
    **stroke order**, which is item 10's subject, not code point. So a code-point row is not a
    baseline a reader would recognise as realistic.

    Consequence to watch: **chinesenames figures collected under the name `systemSortParallel`
    before 2026-09-16 --- request 9's --- measured code-point order and must not be tabulated
    alongside figures collected after it.** The english and chinese corpora are unaffected (their
    coder supplies no Collator, and natural order is the correct order for them).

    One asymmetry left deliberately: the *serial* `systemSort` still sorts chinesenames by code
    point, with `systemSortPinyin` beside it as the correct-ordering variant, which is how request 6
    chose to solve it and is the figure actually quoted. The parallel case had no such variant to
    quote, which is why it was corrected in place instead. If that inconsistency grates, the tidy
    resolution is to make `systemSort` corpus-aware too and retire `systemSortPinyin` --- but that
    touches a number already in the abstract and Table `HS_BM`, so it was not done unasked.

    Original text follows.

    Request 9 (2026-09-12) asks Yunlu to put
    `Arrays.parallelSort` beside `ParallelRadixHuskySort` in Table `ParallelRadix`, which settles
    the question on `Long[]` — the type that table measures. It does not settle it on strings,
    because `ParallelRadixHuskySort` is wired into the benchmarks for `Long[]` only:
    `StringSortBenchmarks` gained a `systemSortParallel` on 2026-09-12 but has no parallel husky
    sort to set against it.

    That leaves the gap on the data type where it matters most. Comparing two `Long`s is cheap, so
    husky coding has least to offer there and the `Long[]` comparison is the unfavourable case for
    us; the English and Chinese corpora are where an expensive ordering makes the encoding pay, and
    where a reviewer asking "how does the parallel variant do against `Arrays.parallelSort` on real
    strings?" currently has no answer.

    What it needs: a `ParallelRadixHuskySort` benchmark over `StringState`, with the same thread
    counts as the `Long[]` one, at the sizes Table `ParallelRadix` uses. The sorter itself is
    generic over the coder, so this is benchmark wiring rather than new algorithm work. The reason
    it is deferred rather than done is the 2026-09-15 deadline, not difficulty.

    Raised by Robin on 2026-09-12, out of the same question that produced request 9: whether a user
    at $n > 1{,}000{,}000$ would realistically reach for the system sort at all.

34. **`ParallelRadixHuskySort`: the per-pass bucket bookkeeping outweighs the data, and the
    sequential step grows with thread count.** Found 2026-09-14, reviewing the implementation after
    request 9 showed `Arrays.parallelSort` beating the parallel husky sort on the permits at every
    size and at $n = 2{,}000{,}000$ on `Long[]`. This is the explanation, and it is arithmetic
    rather than hypothesis.

    **The motivating measurement compared a 15-thread sort against an 8-thread one (found
    2026-09-16).** Yunlu's 09-13 environment note records that the common `ForkJoinPool` behind
    `Arrays.parallelSort` had **15 workers on his 16 processors**, and that "no p15/p16 husky row was
    requested or run" --- every husky row in request 9 was fixed at p4 or p8. So the permits
    headline, `Arrays.parallelSort` 2.77x faster than p8 (13.1 ± 0.2 against 36.4 ± 0.5), gave the
    baseline nearly twice the threads. That is very likely most of why his 16-core result and the
    local 8-core one disagree in *direction*: on eight cores, where p8 and the system sort get
    comparable resources, the husky sort came out 10--24% ahead on the same corpus.

    A `parallelRadixHuskySortAuto_pAll` row now sizes itself from `availableProcessors()`, as
    `Arrays.parallelSort` does, and is the row to compare against the system sort; request 10a asks
    for it. The fixed p4/p8 rows stay for the scaling sweep, where a fixed count is the point. Open
    question for Yunlu: whether the husky row should match the pool's 15 rather than the machine's
    16, since `availableProcessors()` reports 16 where the pool runs 15 (and `13 14` under his
    `kiro.slice` quota).

    **Separately, the local attempt could not measure the effect at all**, because run-to-run drift
    exceeded it: `systemSortParallel`, a benchmark no edit had
    touched, scored 5.112 ms and then 3.514 ms at $n = 32{,}000$ across two consecutive runs of the
    same jar — a 31% swing — and 29.44 then 31.43 at 198,900. The cause was machine contention:
    `uptime` reported a load average of 46, with a lab-monitoring agent pegging one core and
    OneDrive, GitKraken and IntelliJ between them taking another core and a half. So the honest
    statement is that `Arrays.parallelSort` may or may not beat the parallel husky sort on permits;
    the request-9 run's machine conditions were not recorded, and no local run has yet been clean
    enough to settle it. Whatever the answer, the `buckets × chunks` costs below are real and worth
    removing on their own.

    Note also that any future attempt must keep an untouched control benchmark in the *same* JMH
    invocation as the candidate, and discard the run if the control moves. Comparing two runs
    against each other on a desktop is not sound at these effect sizes.

    Three costs, all per pass, all sized by `buckets × chunks` rather than by `n`:

    - `Arrays.fill(localCount, 0)` (line 200) clears `chunks × buckets` ints every pass.
    - `chunkBucketOffset[chunkIndex].clone()` (line 207) **allocates and copies another
      `buckets`-sized `int[]` per chunk per pass**, inside the timed region — 16.8 MB of garbage
      for one permits sort.
    - The `afterHistogram` barrier action (line 173) loops `for b in buckets { for c in chunks }`,
      single-threaded.

    Measured against the real configurations:

    | | bookkeeping touched | key traffic | sequential combine |
    | --- | ---: | ---: | ---: |
    | permits 198,900 @16-bit, 8 threads | **25.2 MB** | 12.7 MB | **2,097,152 iters = 10.5 × n** |
    | `Long[]` 2,000,000 @11-bit, 8 threads | 1.2 MB | 192 MB | 98,304 iters = 0.05 × n |
    | `Long[]` 10,000,000 @11-bit, 8 threads | 1.2 MB | 960 MB | 98,304 iters = 0.05 × n |

    So the design is sound while `buckets × chunks ≪ n` and collapses when it is not. At 16 bits
    with 8 threads that product is 524,288, more than twice the permits corpus. That is the whole
    of the permits result, and it is why `Long[]` at ten million — 2,048 buckets against ten million
    elements — is the one configuration we win.

    It also explains the scaling ceiling, and **§6.4 was corrected on 2026-09-14 (`750e29c`)
    accordingly**: the barrier action is O(`buckets × chunks`), so doubling the threads doubles it
    (2,048 steps per pass at one chunk, 32,768 at sixteen). That is Amdahl with a serial fraction
    growing in P, not a fixed cost being unmasked.

    **The fix is a different decomposition, not a tuning pass.** Split on the *most significant*
    digit first — one parallel histogram, one prefix sum, partition into 2^d buckets — then let each
    thread LSD-sort whole buckets independently. No per-pass barrier, no shared prefix sum per pass,
    no merge (the top digit already orders the buckets), and each bucket's count array is small
    enough to stay in cache. One synchronization point for the whole sort instead of two per pass.

    Two things to know before starting. Bucket sizes are data-dependent, so threads need
    work-stealing or a second-level split of oversized buckets rather than the static ranges the
    current code hands out. And the adversarial input of Table `AdversarialBits` — collapsed high
    bits — is precisely the worst case for an MSD split, since every element lands in one bucket and
    parallelism goes to zero; that test is already written.

    Smaller, independent of the above: `MIN_CHUNK_SIZE = 1 << 14` means eight threads are not
    reached below $n = 131{,}072$, which is far too high for a design that starts its threads once;
    and `Executors.newFixedThreadPool` is called inside `sort()` (line 118), so pool construction is
    charged to every measurement — `Arrays.parallelSort` uses the common `ForkJoinPool`.

    ### What was done, 2026-09-16

    Four changes landed, each of which strictly removes work, so none needed a measurement to
    justify it; all are covered by the existing stability and negative-key sweeps.

    - **The `clone()` is gone.** The scatter advances `chunkBucketOffset[chunkIndex]` in place: the
      `afterHistogram` action rewrites every element of every row before the next scatter reads it,
      so a chunk destroys nothing that is read again.
    - **The thread pool is created once and shared** — a cached pool of daemon threads, held
      statically. Deliberately *not* `ForkJoinPool.commonPool()`: these workers block on a
      `CyclicBarrier` until every chunk arrives, and the common pool runs only
      `availableProcessors - 1` threads, so a sort asking for more chunks than that would deadlock
      with the unscheduled chunks never reaching the barrier. Unbounded also keeps the
      deliberately-oversized chunk counts in the test sweep working.
    - **The two setup passes over `n` are folded into pass 0**, which now biases each key as it
      reads it from `longs` and writes the identity index as it scatters.
    - **The digit width can be derived from `n` and the chunk count** — `AUTO_DIGIT_BITS` plus
      `chooseDigitBits`, budgeting `buckets × chunks` at `n/4` and taking the widest digit inside
      it, clamped to [8, 16]. An explicitly-given width is still honoured exactly, so `/16` really
      runs at 16 bits and the paper's digit-width sweep stays reproducible; there is a test for that.

    **The automatic width is a measured win** (5 forks, 50 samples, control passed): 11.9% / 11.7% /
    6.2% faster than fixed-16 at the three permits sizes, non-overlapping at 32,000 and 198,900. So
    item 35's second bullet was right that capping buckets against `n` would carry the permits case.

    **`MIN_CHUNK_SIZE` is a measured negative, and the suspicion recorded above does not hold.**
    `minChunkSize` became a constructor parameter so both policies could be measured in one JMH
    invocation; lowering it from 16,384 to 4,096 gave $n = 100{,}000$ the two chunks it was short of
    (6 of the 8 requested) and changed nothing, and gave $n = 32{,}000$ seven chunks where it had
    been running serially, making it **8.9% slower**. The control ($n = 198{,}900$, which reaches 8
    chunks under either policy) agreed to 1.1%, so the run was sound. The parameter was kept --- it
    is how the negative was established --- but the default is unchanged. Note that $n = 32{,}000$
    beats `Arrays.parallelSort` by around 20% *while running entirely serially*, which is worth
    remembering before attributing any of this sort's advantage to parallelism.

    **Against `Arrays.parallelSort` on permits, on an unsuitable machine:** the automatic width won
    at 32,000 (23.7%, 20% across two clean runs) and at 198,900 (10.5%, 12.3%), both non-overlapping
    both times. $n = 100{,}000$ is **unsettled, because the baseline is what is unstable there** ---
    `Arrays.parallelSort`'s error at that size was ±1.857 and then ±6.996 (48% of its own score)
    across runs, where at 32,000 and 198,900 it was ±0.070 and ±0.716, and where our own error stayed
    between ±0.727 and ±1.159 throughout. `Arrays.parallelSort` derives its split granularity from
    `n` and the common pool's parallelism, so a size dividing awkwardly against 7 workers is the
    obvious suspect --- and a 16-core machine will behave differently there regardless. Not worth
    pursuing locally.

    **The MSD redecomposition below was not attempted**, and on this evidence is less clearly
    necessary than this item assumed: the bookkeeping that motivated it was dominated by the
    `clone()`, which was one line.

    ### Three measurement traps, all hit on 2026-09-16

    Recorded because two of them are biases rather than noise, and the first may affect figures
    already in the paper.

    1. **JMH runs a class's methods in lexicographic order.** When load drifts upward across a run,
       whichever method sorts last is systematically penalised --- and `systemSortParallel` sorts
       last in `PermitSortBenchmarks`, `StringSortBenchmarks` and `ParallelStringSortBenchmarks`
       alike. Observed directly: 30.065 ± 3.580 in one run against 22.078 ± 0.605 in another, with
       the noisy reading being the one where it ran last as load climbed. **Run a baseline and a
       candidate as a two-method invocation** so they sit adjacent in time.
    2. **Antivirus scans the freshly-built jar during the next run.** The benchmarks jar is 76 MB and
       Microsoft Defender was reading it throughout the first benchmark of the following run. Leave
       several minutes between `mvn package` and measuring. Worse, and self-inflicted: rebuilding
       the jar *while* a run is in progress swaps it under the live JVM and kills the run with
       `NoClassDefFoundError` at the results-formatting stage.
    3. **`-f 1` cannot see a 5--10% effect.** One fork gave ±11 ms on a 29 ms score. Five forks gave
       ±0.2--1.0 ms. Every figure quoted above is `-f 5 -wi 5 -i 10`.

    And the standing lesson: **keep an untouched control in the same invocation and discard the run
    if it moves.** A 31% swing in `systemSortParallel` across two runs of an identical jar is what
    revealed that this machine --- 8 cores, one permanently held by a lab-monitoring agent, much of
    another by the desktop app --- cannot measure an 8-thread sort at all.

35. ~~**`RadixHuskySort` serial: four small things left on the table.**~~ **ALL FOUR DONE
    2026-09-16.** Same review, 2026-09-14. Measurement deferred to Yunlu along with request 10 --
    every one of the four strictly removes work, so none needed a measurement to justify it, but the
    serial figures are the ones in the paper and the size of the effect is worth knowing. New
    `radixHuskySortAuto` benchmarks in `PermitSortBenchmarks` and `StringSortBenchmarks` measure the
    width rule; the other three are not separately observable.

    What was done, bullet by bullet:

    - **The long array is no longer permuted.** Confirmed by inspection that nothing reads it after a
      radix sort: `IntroHuskySort` and `DutchHuskySort` keep their longs in step through
      `HuskyHelper.swap`, which neither radix sorter calls; `HuskyBucketHelper.loadBuckets`
      recomputes the coding itself before reading; no test touches `getLongs()`. Robin's call was to
      drop the sync outright rather than keep it as the cheap sequential write from `biased`. **So
      after a radix husky sort, `getLongs()` holds the codes in input order, not sorted order, and
      is documented as not meaningful** -- a deliberate narrowing of a public method on this path.
    - **The width now adapts**, via a shared `RadixHuskySort.chooseDigitBits(n, chunks)` -- the
      serial sorter being the one-chunk case. It reproduces the preference this bullet recorded:
      /11 ahead of /16 at n = 32,000 and behind it at 198,900, against which the rule picks 12 and
      15. Explicit widths are still honoured exactly, so the published sweep stays reproducible.
      The rule lives in `RadixHuskySort` and `ParallelRadixHuskySort` delegates to it, so the
      dependency runs serial <- parallel and there is one definition rather than two to drift.
    - **The final pass no longer writes keys**, once the longs are not being permuted: only the
      index is returned, so those 8 bytes per element had no reader. `digitBits` is capped at 20, so
      there are always at least four passes, which is what lets the last-pass branch assume it is
      not also the first.
    - **`biased` no longer costs a setup pass**: the sign bias and the identity index are folded
      into the first digit pass, which was already reading and writing every element.

    Note that bullets 1 and 3 described code `ParallelRadixHuskySort` held in identical form, so
    both were fixed in both places -- this item's heading says "serial", but the defects were not.

    Original text follows.

    - **`applyPermutation` permutes `longs` as well as `xs`** — an `n`-long array copy plus `n`
      random reads. No test asserts `getLongs()` is sorted afterwards and no consumer was found on
      the `RadixHuskySort` path. If it must stay, the sorted keys are already in `biased` after the
      final pass, so it should be a sequential write rather than a permuted copy.
    - **`digitBits` never adapts to `n`.** The permits table has /11 ahead of /16 at $n = 32{,}000$
      (2.61 vs 2.72 ms) and behind it at 198,900 (33.47 vs 30.89). The paper documents the plateau;
      the code ignores it. Capping buckets at roughly `n/4` would choose correctly, and would fix
      most of item 34's permits case on its own.
    - **The final pass writes keys nothing reads** — 8 bytes per element.
    - **`biased` costs a full extra pass over `n` and an allocation**, when the sign-bias XOR only
      affects bit 63 and so matters to one pass out of four.

    **One candidate optimization that was measured and mostly does not pay**, recorded so it is not
    re-proposed: skipping passes above the highest *differing* bit. Measured on the real corpora —
    English words 60–63 significant bits, Chinese names 62, `Long`/`Double` 64. Only dates benefit
    (41 bits: 3 passes instead of 4 at 16-bit digits, 4 instead of 6 at 11-bit). `Integer` looks
    like a 32-bit win but `integerCoder` returns `x.longValue()`, which sign-extends, so its codes
    differ across all 64 bits; an unsigned-offset coder would halve its passes. Note also that the
    unconditional `^ Long.MIN_VALUE` makes the naive "highest set bit" test useless — every
    non-negative code has bit 63 set — so the test must be on the bits that *differ*, which a
    constant XOR leaves unchanged.

36. **The english corpus is encoded with the wrong coder, and the cleanup-cost model is
    mis-specified.** Found 2026-09-17, following request 10. Both findings come out of
    `CleanupPassProbe` (`src/test/.../CleanupPassProbe.java`), a `main` rather than a test, which
    reports exact structural counts plus soft timings; the counts are deterministic and
    machine-independent, the timings were taken at load 13--19 and are worth about ±30%.

    ### The coder

    `StringSortBenchmarks` gives the Leipzig english corpus `UNICODE_CODER`. That packs four 16-bit
    characters (`unicodeToLong` fills all 64 bits and then `>>> 1` to clear the sign bit, so it is
    four characters less one bit --- the `MAX_LENGTH_UNICODE - 1` in the coder's constructor is the
    declared `perfect()` threshold, not the packing width). `asciiCoder` packs nine characters at 7
    bits and `englishCoder` ten at 6, and the same class already uses `englishCoder` for the
    commonwords corpus.

    | coder | distinct codes for 275,333 words | words/code | natural runs at n=1M | mean run | cleanup ÷ full sort |
    | --- | ---: | ---: | ---: | ---: | ---: |
    | `unicode` (4x16) | 68,512 | **4.02** | **365,958** | 2.7 | 0.21 |
    | `ascii` (9x7) | 256,993 | 1.07 | 30,921 | 32.3 | 0.07 |
    | `english` (10x6) | 265,207 | 1.04 | **17,506** | 57.1 | 0.06--0.09 |

    So the Unicode coder collapses the vocabulary four-to-one, and the resulting 366k runs of mean
    length 2.7 are what the cleanup pass has to merge. **The serial floor --- encode plus cleanup,
    the two phases no chunk count touches --- moves from 1.06--1.10x of `Arrays.parallelSort`'s
    entire runtime to 0.62--0.64x with `asciiCoder`.** That is the difference between "cannot win
    however well the digit passes parallelise" and "has room to win".

    **The switch is safe and carries no correctness risk.** Only **0.446%** of the english
    vocabulary contains a character outside ASCII, and the count outside 64..127 is *identical*,
    which means no word holds a digit, space or sub-64 punctuation --- the Leipzig extraction regex
    already strips them, so `englishCoder`'s narrower window is as safe here as `asciiCoder`'s. All
    three coders are imperfect, so the cleanup pass runs and guarantees the result either way; this
    is purely a performance choice. `asciiCoder` is the more conservative default (order-preserving
    across all of ASCII, so it survives a corpus containing digits or punctuation); `englishCoder`
    buys a tenth character and 1.8x fewer runs but assumes the 64..127 window.

    Note also that the 0.446% is **mojibake, not text**, and the corpus file itself is at fault
    rather than the reader. The affected words are `cafÃ©`, `crÃ¨che`, `ChÃ¢teau`, `SÃ£o`,
    `BahÃ¡'Ã­` and the like --- `café`, `crèche`, `Château`, `São`, `Bahá'í`. The bytes on disk for
    the `é` of `AmÃ©lie` are `c3 83 c2 a9`, which is UTF-8 for `Ã` followed by `©`: the original
    UTF-8 `c3 a9` was decoded as Latin-1 and re-encoded, i.e. **the file is double-encoded**.
    `HuskySortBenchmarkHelper` reads it as UTF-8, which is correct, and faithfully reproduces the
    damage. It is scattered through the file, once per accented character, so it is not a
    byte-order-mark effect.

    **The damage is upstream, in the Leipzig distribution: settled 2026-09-17 and not worth
    revisiting.** All five files of the `eng-uk_web_2002` package in `src/main/resources` carry the
    signature (3,675 occurrences of the double-encoded marker in `1M-sentences`, 38 in
    `10K-sentences`) and **not one clean UTF-8 accented character between them** --- zero `c3 a9` in
    121 MB of English web text, where a clean corpus would hold thousands. The `10K-sentences` file
    was then downloaded fresh from
    `downloads.wortschatz-leipzig.de/corpora/eng-uk_web_2002_10K.tar.gz` and is **byte-identical to
    ours**, same length and sha256 `475aa01b4c4d97a03d23d7bbe5730a39acae5bdd18ee720f3a6c807304a9d5af`,
    carrying the same 38 markers. So our copy is pristine; there is nothing to replace it with, and
    nobody here caused it. Repairable in principle by a Latin-1 encode / UTF-8 decode round trip,
    which would make a narrow coder near-exact --- but that would depart from the published corpus
    and change every english figure measured from it, so it is left alone.

    The choice must stay **per corpus**: `asciiCoder` would be catastrophic on the Leipzig chinese
    corpus (97.6% of its words are non-ASCII), where `UNICODE_CODER` is already excellent --- 1.03
    words per code, 12,265 runs, cleanup 2.2% of a full sort, serial floor 0.24x.

    This bears on the paper's **serial** english figures as well as the parallel ones, since the
    cleanup pass is common to both.

    ### The model, and what wants clarifying rather than correcting

    `T_3 = k_3 (N + pX)` **was written for insertion sort** (Robin, 2026-09-17), whose cost genuinely
    is N plus the number of inversions, so it is not wrong --- but step 3 uses Timsort, whose cost
    follows the number of *runs*, and the paper does not say which sort the term describes. On a
    revision that is worth one sentence, because the two measures can disagree in **direction**, not
    merely in magnitude: switching english from `unicode` to `english` cut runs by 21x while
    inversions **rose** from 1,151,133 to 3,378,167, and the cleanup nonetheless got ~3x cheaper. So
    a reader who took the term as describing step 3 as implemented would predict the wrong sign. The
    mechanism is that masking to 6 or 7 bits mis-encodes a few characters, so a handful of elements
    land far from home; each contributes many inversions but only one or two run breaks.

    A related consequence for §A.5, which reports the insertion-sort/Timsort crossover at around
    N = 50,000 without explaining it: insertion sort wins while X < N, and X ≈ N²/(4D) for a corpus
    with D effective code-groups, so the crossover sits at N ≈ 4D. The measured X for english gives
    D ≈ 8,700 and hence a predicted crossover near 35,000, against the ~50,000 observed. Close
    enough to be the mechanism, and worth a sentence.

    Measured X/N at n = 200,000, which also answers directly whether the remaining inversions are
    "few compared with N" as the model assumes: english **5.8**, chinese **0.14**, chinesenames
    **35**. The assumption holds for chinese alone --- which is the one corpus where the parallel
    sorter is competitive (1.28x behind) and the only one where threads help.

    ### chinesenames, and items 10 and 11

    1,145,009 distinct names yield 818,114 codes, so 1.40 names per code --- tie groups far too
    short to explain 162,690 runs of mean length 6.1. The codes must therefore be **mis-ordering**,
    not merely tying, which points at polyphone characters resolved to the wrong reading: **item 11**.
    The 1.40 ties themselves are **item 10**'s stroke-count tiebreak. Both open items now have a
    measured motivation, and item 11 looks like the larger lever. Names are at most 3 characters
    against the pinyin code's 5-character capacity, so there are spare bits for item 10 to use.

37. **The husky coders are not order-preserving, and every string benchmark will need re-running.**
    Found 2026-09-17 out of item 36. Robin's reading of it: "our husky coders that we've been
    tacitly assuming were ideal turn out not to be ideal, and for reasons that I should have thought
    of at the time. I think I was seduced by the idea of making the encoding as fast as possible,
    without realizing that it had such a negative effect on the cleanup phase."

    ### The defect

    `stringToLong` narrows each character with `& mask`, which is **not monotonic**. A character
    above the masked width wraps to an arbitrary position inside it, so the code mis-orders the
    element rather than merely losing resolution on it:

    - `asciiCoder` (7 bits): 'é' is 233 and masks to 105, which is 'i', so "café" encodes as though
      it were "cafi" and sorts *before* "cafz" when it belongs after. The mojibake form is worse:
      'Ã' is 195, masking to 67, which is uppercase 'C', so it sorts before every lowercase word.
    - `englishCoder` (6 bits): worse still, because the wrap is *ambiguous* rather than just wrong.
      An apostrophe is 39 and masks to 39; 'g' is 103 and also masks to 39. So **`"don't"` and
      `"dongt"` receive identical codes** --- a coder that cannot distinguish two different words.
      `HuskyCoderFactoryTest.testApostropheIsNotConfusedWithG` asserts this.

    A husky code's only job is to increase with its argument, so this is a defect in the coders
    rather than a tuning choice. It matters through the cleanup pass: a mis-ordering puts an element
    far from home and breaks a run, where a tie leaves the run intact, and Timsort's cost follows
    runs (item 36).

    ### What was done

    `asciiSaturatingCoder` and `englishSaturatingCoder` are added **alongside** the masking coders,
    not replacing them --- the originals are used from a dozen tests, `HuskySortHelper`'s
    `sequenceCoderMap`, `ChineseCharacter` and `HuskySortBenchmark`, and the paper's figures were
    measured with them. They map a character to `clamp(c - offset, 0, 2^bits - 1)`, which is
    non-decreasing over every char value: below the window everything ties at the bottom, the window
    passes through, above it everything ties at the top.
    `HuskyCoderFactoryTest.testSaturatingCodersAreMonotonic` checks that exhaustively over all
    65,536 characters, and `testMaskingCodersAreNotMonotonic` asserts the contrast so that it cannot
    quietly stop being true. `StringSortBenchmarks` now uses `englishSaturatingCoder` for the english
    and commonwords corpora.

    ### What is NOT yet known, and matters

    **Whether saturating pays on the english corpus is undecided, and Robin's instinct that masking
    was chosen for encode speed may well be right.** The accounting so far, at n = 1,000,000:

    | | masking (10x6) | saturating (10x6) |
    | --- | ---: | ---: |
    | natural runs after the radix phase (exact) | 17,506 | 16,641 (-4.9%) |
    | cleanup / full sort | 0.064 | 0.066 --- no gain, within noise |
    | encode | ? | ? |

    So the cleanup gain does not clearly show up on *this* corpus, and any encode cost would make
    saturating a net loss on it. The encode side could not be measured honestly by hand: two
    harnesses put the *same* masking arithmetic at 41 ms and 69 ms per million, and english masking
    at 43 ms and 130 ms, because a call site with four coder implementations measures JIT inlining
    rather than `&` against `min`. `huskyEncodeOnlyEnglishMasking` and
    `huskyEncodeOnlyEnglishSaturating` were added to `StringSortBenchmarks` to settle it under JMH.

    Note *why* the gain is invisible here: the Leipzig extraction regex strips digits and
    punctuation, so this corpus contains no apostrophes and only 0.446% non-ASCII words --- the
    conditions under which masking is nearly harmless. The defect should cost much more on ordinary
    English text. A corpus containing punctuation would be the test that shows it, and we do not
    currently have one wired.

    ### Consequences to plan for

    - **The paper may need revising**, and Robin considers this likely already for other reasons. Any
      figure measured with a masking coder describes a coder that mis-orders; if a saturating coder
      is adopted, the english and commonwords figures move. §A.4 on coding accuracy is the natural
      home for the monotonicity point, and the cleanup term wants the clarification recorded in
      item 36.
    - **The string benchmarks will need re-running** whichever way this lands, since the coder for
      english changed twice today (UNICODE_CODER to asciiCoder to englishSaturatingCoder) and the
      run counts differ by 22x across those choices.
    - **The anonymised artifact** would need regenerating with the paper.
    - Nothing here reaches the submitted paper, which corresponds to `master`.

38. **§A.5's cleanup-sort choice was measured against the wrong insertion sort, and the right answer
    depends on the coder.** Found 2026-09-17, following items 36 and 37.

    ### The wrong algorithm

    The paper writes the cleanup as `k(N + pX)`, which is the cost of **adaptive** insertion sort:
    scan left from each element, shift the larger ones up, so `(n-1) + X` comparisons and `X` moves.
    `InsertionSort` in this repository is not that algorithm. Its `sort` calls
    `ComparisonSortHelper.swapIntoSorted`, which locates each element by **binary search over the
    whole sorted prefix** --- `n log n` comparisons however nearly ordered the input is. Its own
    class comment says it "does NOT use the insertion swap mechanism"; what had not been drawn is
    the consequence, that it cannot exploit the very property the cleanup pass exists to exploit.

    Measured on the english corpus after the radix phase at n = 200,000: binary insertion 44,947 us
    against Timsort's 9,830, a factor of **4.57**. §A.5 reports Timsort winning "by a factor of four
    and a half at N = 4,000,000". That correspondence is close enough to suspect strongly that §A.5
    measured binary insertion, in which case its conclusion is about the wrong algorithm and the
    design decision resting on it ("that is why step 3 uses the system sort") is unsupported as
    stated --- though see below, because it may well be right for other reasons.

    `AdaptiveInsertionSort` is added alongside `InsertionSort`, not replacing it: the existing class
    is used from the benchmarks and several tests, and it is a legitimate algorithm, merely not the
    one the model describes. It carries a comparator overload as well as the natural-ordering form,
    because a cleanup pass in the wrong ordering silently produces an array sorted by the wrong
    thing --- the trap the pinyin coder has sprung twice already.
    `AdaptiveInsertionSortTest.testComparisonsAreAdaptive` pins the property that matters: exactly
    n-1 comparisons on an ordered array, exactly n with one inversion.

    ### The right answer depends on the coder, and not monotonically

    `CleanupPassBenchmarks` measures all three cleanups on the array the radix phase actually hands
    over, parameterised by coder, since p varies by 3 orders of magnitude across them (item 37).
    A smoke run at n = 200,000 (1 fork, 2 iterations --- indicative only):

    | coder | p | Timsort | adaptive | |
    | --- | ---: | ---: | ---: | --- |
    | `englishSaturatingCoder` | 4.7e-7 | **10.8 ms** | 11.9 ms | Timsort by 1.10x |
    | `UNICODE_CODER` | 1.15e-4 | 29.5 ms | **25.1 ms** | adaptive by 1.18x |
    | pinyin | 7.0e-4 | **67.8 ms** | 1006.9 ms | **Timsort by 14.8x** |

    Not monotonic in p, which refutes the obvious model-based expectation that a lower p should
    favour the algorithm whose cost is linear in X. The reason is Timsort's galloping: at p = 4.7e-7
    the 2,157 runs average 464 elements and are nearly disjoint in key space, so each merge costs
    about log(464) comparisons rather than 464, leaving Timsort at roughly n comparisons --- the same
    as adaptive insertion sort, and slightly ahead of it because it shifts no memory. At the other
    end, pinyin's X is 7 million at this n and adaptive insertion sort pays every one of them as an
    expensive NAME_ORDER comparison.

    So: **Timsort is the right default and §A.5's conclusion survives**, but not for the reason it
    gives, and the margin is a coin-flip rather than 4.5x wherever p is small. An adaptive cleanup is
    worth having available for the middle of the range.

    ### The threshold: use adaptive insertion sort when 0.2 < pn < 25

    Answered 2026-09-17 by sweeping X directly rather than inferring it from three coders. A husky
    code leaves groups of equal-coded words in arbitrary internal order, so shuffling within blocks
    of size b reproduces that structure faithfully and gives X/n about b/4. Timings best-of-5 on a
    loaded machine; the effects at the peak are large enough to be directional.

    | configuration | adaptive wins up to | loses from | peak advantage |
    | --- | ---: | ---: | --- |
    | english, n = 200,000 | X/n = 3.60 | 7.60 | **2.00x** at X/n = 0.18 |
    | english, n = 1,000,000 | 6.91 | 14.88 | **2.03x** at 0.32 |
    | chinese names in pinyin order, n = 200,000 | 3.72 | 7.73 | **2.57x** at 0.23 |

    Since X = p n^2 / 4, the deciding quantity is **X/n = pn/4**, so the rule is: choose adaptive
    insertion sort when roughly **0.2 < pn < 25**, and Timsort otherwise. Below the band both sorts
    cost about n comparisons and Timsort's scan has the better constant; above it, adaptive's O(X)
    overruns Timsort's O(n log r) ceiling. The win inside the band reaches 2.6x, and the band is
    wide --- X/n from about 0.07 to about 7.

    **Scope, and why pn works at all.** Robin's observation (2026-09-17) that pn resembles the
    expected degree in the Erdos-Renyi-Gilbert model is exact rather than analogical: the *permutation
    graph* of a permutation has an edge per inverted pair, so pn/4 is literally the average degree of
    the residual inversion graph --- intensive where p alone is not. The two sorts then read different
    features of that graph: **adaptive insertion sort pays for every edge, Timsort pays only for the
    descents**, which are the edges between positionally adjacent vertices. Descents saturate at n/2
    however disordered the array becomes, which is why across the whole sweep above Timsort's time
    grew 5.7x (9.0 to 51.3 ms) while adaptive's grew 109x (9.5 to 1,038.7 ms).

    That also bounds the rule. **pn is not a sufficient statistic in general.** Two arrays with the
    same X but different structure, at n = 200,000:

    | structure | X | descents | Timsort | adaptive | winner |
    | --- | ---: | ---: | ---: | ---: | --- |
    | union of cliques, block 32 | 1,519,383 | 94,624 | 37,086 us | **33,044 us** | adaptive 1.12x |
    | 15 long displacements | 1,559,905 | **15** | **8,404 us** | 39,164 us | Timsort 4.66x |
    | union of cliques, block 8 | 319,684 | 79,821 | 25,603 us | **19,370 us** | adaptive 1.32x |
    | 4 long displacements | 319,991 | **4** | **8,424 us** | 14,708 us | Timsort 1.75x |

    At equal edge counts Timsort's time varies 4.4x with structure while adaptive's varies 1.19x. So
    the rule holds **only for the family of arrays husky coding produces** --- a disjoint union of
    cliques, one per equal-code group --- for which the group-size distribution fixes the edge count
    and the descent count together, so a single parameter determines both. It is not a general
    result about sorting nearly-ordered arrays, and should not be written as one.

    The crossover is **the same for pinyin as for english at the same n** (3.7--7.7 against
    3.60--7.60) even though a pinyin comparison costs about three times as much. That is expected,
    since both algorithms pay the same per-comparison cost and it cancels, and it means the threshold
    is a property of counts and so portable across element types.

    **This also resolves what looked like a contradiction between two measurements of the saturating
    coder.** They were taken at different n, and the crossover moves with n: pn/4 is 0.024 at
    n = 200,000, below the band, and 0.118 at n = 1,000,000, inside it. Both measurements were right.
    The rule predicts every case measured so far:

    | coder | p | n | pn/4 | predicted | measured |
    | --- | ---: | ---: | ---: | --- | --- |
    | `englishSaturatingCoder` | 4.7e-7 | 1,000,000 | 0.12 | adaptive | adaptive 1.24x |
    | `englishSaturatingCoder` | 4.7e-7 | 200,000 | 0.024 | Timsort | Timsort 1.10x |
    | `UNICODE_CODER` | 1.15e-4 | 1,000,000 | 28.8 | Timsort | Timsort 1.72x |
    | pinyin | 7.0e-4 | 200,000 | 35 | Timsort | Timsort 14.8x |

    **What this suggests for the design**, not yet implemented and Robin's call: p is a property of a
    coder and a corpus, measurable once (`CleanupPassProbe` measures it), so a sorter given an
    estimate of p could choose its own cleanup at runtime from pn. That is a bigger claim than the
    paper currently makes and would want the JMH figures below before anyone builds it.

    ### What is not yet known

    The margins near the band edges are from a 1-fork smoke run on a loaded machine; only the 14.8x
    pinyin result and the 2x peaks are large enough to trust as they stand. A proper run:

    ```
    java -jar target/benchmarks.jar "CleanupPassBenchmarks" -f 5 -wi 5 -i 10 -rf json -rff cleanup.json
    ```

    Exclude `binaryInsertionCleanup` for the pinyin coder, which it refuses rather than sorting by
    the wrong ordering.

    On revision, §A.5 wants: which insertion sort Table `TimvsInsertion` measured, and the fact that
    the choice turns on p and therefore on the coder.

39. **Consider backing off parallel sorting as a feature, and say the benefits are serial.** Raised
    by Robin 2026-09-17: "What benefits we have are strictly serial, so maybe we should simply
    acknowledge that and deemphasize the parallel versions." Items 34, 37 and 38 hold the
    measurements; none of them draws this conclusion, which is the gap this item fills.

    ### The case for it

    The mechanism's advantage and its parallel ceiling are **the same property**. Husky coding wins
    by moving work out of the linearithmic phase into two linear phases, encode and cleanup, both of
    which are serial. A sort that wins by doing less total work has less work left to spread across
    cores. So "the benefits are strictly serial" is not a limitation to confess but the mechanism
    stated correctly, and it explains both halves of the results at once --- why the serial sort beats
    `Arrays.sort` and why the parallel one loses to `Arrays.parallelSort`.

    The numbers, all from requests 9 and 10 and item 36:

    - The parallelizable fraction is **0.06** on english and **0.07** on chinesenames; the p1-to-p8
      sweep buys nothing at all on either. Only chinese, at 0.25, gets anything (1.26x).
    - The **serial floor** --- encode plus cleanup, untouched by any chunk count --- is **1.11x** of
      `Arrays.parallelSort`'s entire runtime on english and **1.12x** on chinesenames, on eight
      cores. Make the digit passes free and infinitely parallel and the sort still loses. On sixteen
      cores the floor ratio roughly doubles.
    - The parallel variant wins in exactly **one** measured configuration: `Long[]` at ten million,
      by 1.19x --- the cheapest comparison in the suite, which is where husky coding has least to
      offer in the first place. On the permits it never beats its own serial form.

    ### What in the paper would have to change

    - **The conclusion's claim is wrong as written** (line ~1337): "The causes named there are our
      implementation's, not the approach's, so a parallel proxy-key sort should still win wherever
      the serial one does". The serial floor is the approach, not the implementation, and no amount
      of repairing the histogram-combine touches it. This sentence is the one that has to go or be
      reversed.
    - **The intro needs qualifying** (lines ~397-403): "Radix sort's independent per-digit
      counting-sort passes are well suited to parallelization" is true of the passes but they are not
      where the time goes.
    - **§6.4 and the abstract are already honest.** §6.4 even predicts its own failure mode ---
      "the design should fail wherever buckets times threads approaches N" --- which request 10 then
      confirmed on the permits. The abstract already concedes "where cores are free, the JDK's own
      parallel sort" overtakes us.

    ### Reframe rather than remove --- recommended, but Robin's call

    Robin's own standing prediction is that the likeliest referee ask is a better
    parallel-versus-parallel benchmark. Going silent leaves that question unanswered; reporting the
    negative **with the mechanism** pre-empts it, and a measured negative with an explanation is a
    stronger section than a hopeful positive. A referee cannot ask "but what about threads?" of a
    paper that has already shown the serial fraction to be 0.94 and said why.

    There is also a page-budget argument for it: moving §6.4 out of the body and into the appendix
    beside A.7 frees body space, and the body is at exactly 12.00 pages with no margin, while the
    appendix is outside the count. Do **not** estimate how much it frees --- the length metric is
    non-monotonic under float reflow, so it has to be rebuilt and measured.

    Interacts with the four options Robin is weighing for the paper (withdraw / revise now / revise
    in the author-response phase / leave it): this is a revision of emphasis and one incorrect
    sentence, not of results, which makes it cheap under options 2 and 3 and impossible under 4.

    ### DONE in the paper, 2026-09-17, on the `parallel-redesign` branch

    Robin asked for the revision to be made now so that a camera-ready copy can simply be built if
    and when the time comes. Four changes to `paper/RadixHuskySort.tex`; **no measured figure was
    altered**, only what is claimed about the figures and where the material sits.

    1. **The conclusion's incorrect sentence is reversed.** It claimed the parallel shortfall was
       "our implementation's, not the approach's, so a parallel proxy-key sort should still win
       wherever the serial one does". It now opens with the positive statement --- the advantage this
       paper reports is a serial one, and that is a property of the mechanism --- and gives the
       argument: husky coding earns its advantage by moving work into two linear phases, both
       sequential, so a sort that wins by performing less total work has less work left to
       distribute. It cites the measured parallelizable fractions (0.06 English, 0.07 Chinese names)
       and the serial floor exceeding `Arrays.parallelSort`'s whole runtime, and says plainly that
       repairing the histogram-combine would not change the conclusion. It also concedes the case
       where a parallel comparison sort is the right tool.
    2. **The introduction is qualified.** "Well suited to parallelization" now reads "taken alone,
       well suited", followed by the point that those passes are not where a proxy-key sort spends
       its time, and that the parallel variant is offered as a measured negative result rather than
       as a feature.
    3. **§6.4 moved out of the body into the appendix**, landing immediately after A.7, the parallel
       baseline it loses to, so all the parallel material sits together. It is now A.8. A `%%`
       comment at the move site records why. The contributions list no longer says "we add a parallel
       variant of it" but that we parallelize the digit passes, "which turns out not to pay and is
       reported as such".
    4. A `\label{sec:conclusion}` was added, the conclusion having had none, so the introduction can
       point forward to the argument.

    **Measured, not estimated** (the length metric being non-monotonic under float reflow): the body
    was exactly 12.00 pages, with `References` at the very top of page 13 and no margin whatever. It
    now ends part-way down page 12's **right** column --- `References` begins at yMin 182 of that
    column --- so roughly four tenths of a page of body space has been freed. All cross-references
    resolve; the log reports no undefined reference, and the only hardcoded section number anywhere
    in the source is inside the new explanatory comment.

    Note for whoever builds next: **`paper/build.sh` fails out of the box**, because `pdflatex` is
    not on the default `PATH` on this machine --- it lives in `/Library/TeX/texbin`. Prefix the
    invocation with `PATH="/Library/TeX/texbin:$PATH"` or fix the script. Also note that Table
    `ParallelRadix` is now numbered A.7 while the parallel-baseline *section* is also A.7, separate
    LaTeX counters both reaching the same number; standard, but a reader could trip on it.

40. **The cleanup pass IS well parallelizable, which undercuts item 39's central argument and the
    conclusion revised on 2026-09-17.** Found 2026-09-17, late, after Robin proposed that the
    cleanup resists parallelism because "the data elements are NOT independent ... there is a
    direction of processing such that we cannot treat whole chunks independently."

    ### What is true

    That holds for **insertion sort**: element i's destination depends on 0..i-1 already being
    ordered, a loop-carried dependence with no chunking available. It does **not** hold for
    **Timsort**, which is a merge sort, and merge sort is the canonical parallelizable comparison
    sort --- `Arrays.parallelSort` chunks it exactly that way, Timsorting leaf blocks independently
    and merging them.

    The counter-argument offered in reply was also wrong, and more instructively. It ran: chunking
    destroys the global run detection that makes an adaptive sort cheap, since serial Timsort finds
    one run spanning the whole array and stops, so parallelizing is counterproductive. **The work
    analysis is right and the time conclusion does not follow, because it omits the division by
    core count.** Measured, n = 1,000,000 english strings, 8 cores and 7 pool workers:

    | disorder (block shuffle) | natural runs | serial `Arrays.sort` | `Arrays.parallelSort` | ratio |
    | --- | ---: | ---: | ---: | ---: |
    | 1 (fully sorted) | 1 | 26,147 us | **14,931 us** | 0.57x |
    | 2 | 66,784 | 59,027 | **21,215** | 0.36x |
    | 32 | 431,767 | 114,519 | **35,241** | 0.31x |
    | 65,536 | 499,852 | 316,435 | **83,809** | 0.26x |

    Parallel wins at every level, **including on an already-sorted array**. The mechanism is exactly
    the work analysis with the missing division: JDK `Merger` binary-searches a split point for
    parallelism and then merges element-wise at the base case, without galloping, so on sorted input
    parallelSort does about 4x the work of serial Timsort (n at the leaves plus ~3n across merge
    levels, against n comparisons and no moves) and finishes in 4/7 of the time. 4/7 = 0.571 against
    a measured 0.571.

    ### What it costs us

    All four phases are parallelizable: the digit passes (measured, 1.19x on `Long[]` at 10M), the
    encoding (a pure function per element), the permutation (an independent scatter under a
    bijection), and now the cleanup (measured above). **We parallelized one of the four, and not the
    one with the time in it.** So:

    - **Item 39's central argument is weakened.** "A sort that wins by performing less total work has
      less work left to distribute across cores" is true as arithmetic but does not bound anything,
      because our reduced work parallelizes about as well as the baseline's --- our cleanup *is* a
      parallel comparison sort over the same array.
    - **The conclusion as revised in `36fa361` asserts more than we know.** It says the serial
      advantage is "a property of the mechanism rather than a shortcoming of our implementation".
      That is not established. The sentence it replaced --- "the causes named there are our
      implementation's, not the approach's, so a parallel proxy-key sort should still win wherever
      the serial one does" --- was closer to right, though it too asserted more than was measured.

    ### The arithmetic that decides it, and what is still missing

    A fully parallel proxy-key sort beats `Arrays.parallelSort` iff encoding plus digit passes plus
    permutation costs less than what pre-ordering saves the baseline. From the table above that
    saving is 83.8 - 14.9 = **68.9 ms** at n = 1,000,000 on this machine. The measured encode is
    24.3 ms, leaving **44.6 ms** for the passes and the permutation. Six passes over a million
    elements at 12 bytes is 72 MB of traffic spread over seven workers, which is plausibly inside
    that budget --- but it is **not measured**, so neither outcome may be claimed.

    **DONE in the paper, same day.** Robin's call was to rework rather than revert. The conclusion
    now opens "The advantage demonstrated here is a serial one", names the four phases, says we
    parallelized one of them and not the one with the time in it, keeps the f = 0.06 figure and the
    serial-floor comparison --- and then, in a second paragraph, says explicitly that this does *not*
    establish a limit on the approach: the other three phases are parallelizable, the cleanup
    measurably so against our own expectation, so the ceiling bounds this implementation rather than
    the mechanism. It states the open question with its budget (about 69 ms saved by pre-ordering at
    a million English strings, 24 of which a measured encoding spends, leaving some 45 ms unmeasured)
    and claims neither outcome. The introduction's forward pointer was corrected to match, since it
    had promised an argument that no longer exists. Body still inside 12 pages, `References` at
    yMin 349 of page 12's right column against 182 before the rework and the very top of page 13
    before any of today's edits; no undefined references.

    **Do not rewrite it a fourth time without measuring.** The claim was stated three mutually
    inconsistent ways in one day. What settles it is one benchmark: a variant that parallelizes the
    encoding and uses `Arrays.parallelSort` as the post-sorter, set against `Arrays.parallelSort`
    alone. That is the "clearest future work" the conclusion now names.

    ### Future work: a run-aware parallel cleanup, and why it probably does not help us

    Robin's suggestion (2026-09-17) was that Timsort might parallelize on its **runs** rather than on
    fixed-size blocks. That is a genuinely different scheme from `Arrays.parallelSort`, which splits
    into blocks of `n/(4p)` floored at 8192, Timsorts each and merges --- chopping a run that spans
    the whole array into some sixty pieces and then merging them back. Splitting at natural run
    boundaries instead preserves the adaptivity that fixed blocks destroy: detect runs in parallel
    (each worker scans its own segment, stitching at the boundaries), then merge the runs.

    The two cost models, with r runs over p workers:

    | input | run-aware | fixed blocks (`parallelSort`) |
    | --- | --- | --- |
    | already sorted, r = 1 | `n/p` --- detect, find one run, stop | ~`4n/p` --- every block sorts, then ~3 merge levels |
    | r runs generally | `(n/p)(1 + log2 r)` | ~`4n/p`, independent of r |

    So run-aware wins while `log2 r < 3`, which is to say roughly **r < 4p** --- fewer natural runs
    than `parallelSort` would have made blocks. On a sorted array that is about 4x better than
    `parallelSort` and 7x better than serial Timsort, well beyond the 1.75x measured above.

    **But the regime where it wins barely overlaps with the regime we care about.** With
    `englishSaturatingCoder`, r is 16,641 at n = 1,000,000, so `log2 r` is about 14 and a run-aware
    scheme would be roughly 3.75x *worse* than fixed blocks --- merging sixteen thousand small pieces
    instead of twenty-eight large ones. And where r is small enough for it to win, the cleanup is
    already cheap enough that nothing is at stake; the permits are the limiting case, r = 1 and no
    cleanup pass at all, the coding being perfect.

    Which is a compact statement of the whole finding: **the cleanup is either expensive and
    fragmented, in which case fixed blocking parallelizes it better, or cheap and coherent, in which
    case there is nothing to parallelize.** Fixed-size blocking is the right choice for the one
    regime that matters here.

    Consequence for the paper's open question, recorded rather than acted on: the ~45 ms budget the
    conclusion quotes for passes and permutation assumes the cleanup is parallelized the way
    `Arrays.parallelSort` does it. If some scheme beat that in some regime, the budget would be a
    floor rather than a fixed figure. The paper already claims neither outcome, and a scheme that has
    not been built does not strengthen that, so the text was left alone.

41. ~~**Parallelize step 1, the encoding, in the parallel sorter.**~~ **DONE 2026-09-17.** Robin:
    "Step 1 is eminently parallelizable, yet I believe we have relegated it to future work because it
    just wouldn't make a sufficient difference. However, I think we should do it in our parallel
    version(s). I can't see a good reason not to." He was right, and the earlier estimate that it
    "wouldn't make a sufficient difference" was based on the wrong coder's encode cost.

    ### What was done

    `AbstractHuskySort.preSort` is `final`, deliberately, because the three-phase structure must hold
    for every husky sort; so the coding step became its own `protected doCoding(X[])`, which
    `preSort` calls and which `ParallelRadixHuskySort` overrides. Every other sorter keeps the
    sequential form, which is what the published serial figures were measured with and must remain.

    `HuskyHelper.doCoding(array, chunks, executor)` divides the work by **slicing the array and
    invoking the coder's own array-level encode on each slice**, rather than encoding element by
    element. That is a correctness point rather than a convenience: a coder decides for itself
    whether its coding is perfect and may decide per element ---
    `BaseHuskySequenceCoder` reports perfection only if *every* sequence fits --- so any
    implementation that bypassed the coder's array method would have to duplicate that reasoning and
    would silently diverge from it. The slices' verdicts combine with a logical and. Three tests
    cover it, including one for all-perfect input, which is the only case that distinguishes an "and"
    from an "or".

    ### Measured, n = 1,000,000 on eight cores

    | corpus / coder | sequential | 2 chunks | 4 chunks | 8 chunks |
    | --- | ---: | ---: | ---: | ---: |
    | english / `englishSaturatingCoder` | 72,953 us | 38,303 (1.90x) | 25,852 (2.82x) | **21,002 (3.47x)** |
    | english / `UNICODE_CODER` | 24,308 us | 15,577 (1.56x) | 12,091 (2.01x) | **9,850 (2.47x)** |
    | chinesenames / pinyin | 124,506 us | 70,701 (1.76x) | 43,175 (2.88x) | **33,626 (3.70x)** |

    ### Why this matters more than the earlier estimate suggested

    **The saturating coder's encode costs three times the Unicode coder's** --- 72.9 ms against
    24.3 ms at a million elements, since it processes ten characters rather than four. The "encode is
    only about a quarter of our own total, so parallelizing it buys little" reasoning in item 40 used
    the 24.3 ms figure, which belongs to the coder we have just stopped using for english.

    Against the budget item 40 derives --- pre-ordering saves the baseline about 68.9 ms at a million
    English strings --- a **sequential** saturating encode at 72.9 ms does not fit *at all*.
    Parallelized to 21.0 ms it fits with some 48 ms left for the digit passes and the permutation. So
    parallelizing step 1 is not a marginal gain; it is what makes the saturating coder of item 37
    affordable, and the two changes support each other.

    It also partly answers item 37's open question, though not the way the JMH benchmarks there will:
    saturating **does** cost real encode time, about 3x, which is a genuine argument against it while
    the encode is sequential and much less of one once it is not.

    ### Still not parallelized

    Two of four phases now: encoding and digit passes. The permutation remains sequential --- an
    independent scatter under a bijection, so it is safe in principle, and the existing comment
    explains it was left alone as one O(N) pass over arbitrary payload objects. The cleanup remains
    sequential, and item 40 records both that it parallelizes well and why a run-aware scheme
    probably would not help us.

42. **There is no parallel QuickHuskySort, and it should stay that way --- but for strategic reasons,
    not because it would not work.** Robin asked on 2026-09-17 whether such a thing exists. It does
    not: `ParallelRadixHuskySort` is the only parallel variant in the repository, and the paper
    declines to attempt another.

    ### It would work, and it has *more* parallel headroom than the radix variant

    Parallelizing QuickHuskySort's step 2 is textbook: partition sequentially, fork the two halves,
    and after log P levels there are P independent subarrays. The payload-drag swaps that make
    QuickHuskySort slower than RHSort are safe under that scheme, forked subranges being disjoint.
    With the `doCoding` hook added in item 41, its encoding would parallelize for free.

    The headroom is the counterintuitive part. From the paper's own figures at n = 1,000,000 on
    English words:

    | | total | encoding | step 2 + cleanup |
    | --- | ---: | ---: | ---: |
    | QuickHuskySort | 697.9 ms | 67.7 (9.7%) | **~630 ms** |
    | RHSort | 272.7 ms | 67.7 (24.8%) | ~205 ms |

    **QuickHuskySort has about three times the parallelizable work in absolute terms**, its
    O(n log n) step 2 being a far larger share of a far larger total. Its Amdahl fraction is
    therefore much more favourable than RHSort's: the variant with the *worse* serial performance has
    the *better* parallel prospect, because parallelism rewards having work to divide. That is the
    mirror image of item 39's argument --- "a sort that wins by doing less total work has less left to
    spread across cores" --- read backwards, and it is worth keeping the pair in view, because
    together they say the serial win and the parallel prospect trade against each other rather than
    reinforcing.

    ### Why not to build it

    - It runs against the decision of item 39 to back off parallel claims generally.
    - The paper deliberately de-emphasises QuickHuskySort as the original idea, RHSort being the
      headline (see item 21, the renaming).
    - It is real algorithm work --- parallel quicksort with payload drag, plus the stability and
      correctness sweep that `ParallelRadixHuskySortTest` needed --- for a variant that is not the
      contribution.

    ### The paper's existing sentence is fine, with one caution

    The introduction says of QuickHuskySort's comparison-based steps that "parallelizing them well is
    a separate, harder problem that this paper does not address". That is a **scope** statement and
    stands. The caution is only that "harder" must not be read as "unsolved": the JDK demonstrably
    parallelizes Timsort --- `Arrays.parallelSort` *is* that --- and it beats serial Timsort by
    1.75--3.8x in the measurements of item 40. If a referee presses, the honest answer is "harder,
    and out of scope", not "not known how".

43. **Consolidated paper-revision checklist (opened 2026-09-22, after Yunlu's request-11 run,
    PR #67).** Items 36--42 each recorded their own paper consequences as they were found, over
    about a week, and two of them have since been partly overtaken by measurement. Robin asked for
    one list. This is it: everything the paper needs, in one place, so that whichever of item 39's
    four options is chosen the work is already scoped. Nothing here is started.

    ### A. The cleanup cost model is the wrong algorithm's, and the paper already knows it

    \S~`sec:pcrit` says "for Timsort **or insertion sort**, the time to re-sort the array will be
    $t = k (N + pX)$", then defines $T_3 = k_3 (N + pX)$ as "the time to **Timsort** the element
    array". $N + pX$ is the *adaptive insertion sort* cost --- N to scan plus one move per remaining
    inversion. It is not Timsort's. Timsort's cost is $\textbf{O}(N + N \log r)$ for $r$ runs, which
    **the paper already states correctly** in the algorithm-comparison table, citing Auger et al.:
    "more precisely $\textbf{O}(N + N \log p)$ for $p$ runs". So the paper contradicts itself
    between the body and the table, and the body's version is the wrong one. Worse, that table's
    bound overloads $p$: $p$ is the residual inversion *probability* everywhere else in the paper
    and the *run count* in that one cell.

    The measurement settling it (2026-09-22, hand timing, english corpus, n = 1,000,000, 15
    interleaved reps, eight-core Mac --- JMH cells now committed as `c5f47d3` and awaiting a run):

    ```
                           runs    inversions   timsort(ms)   adaptive(ms)
    unicode      4x16   366,865    27,535,704         133.3          223.7
    asciiMasking 9x7     30,520   185,161,666          54.2          888.7
    asciiSat     9x7     29,327       216,488          43.6           45.0
    englishMask 10x6     17,305   180,352,275          43.5          830.9
    englishSat  10x6     16,061       110,206          38.0           39.1
    ```

    Timsort's cleanup is monotone in run count and **blind to inversions**: 185.2M inversions and
    216,488 inversions both cost about 46 ms. Two independent coder pairs differing by 855x and
    1,637x in inversions, and by 4% and 8% in runs, differ by about 1.1x in Timsort time --- against
    which `AdaptiveInsertionSort`, on the very same arrays, is 16x to 19x slower on the masking
    member of each pair and level on the saturating one. Same data, same machine, same minute: the
    two algorithms' costs track different statistics, and only one of them is the statistic the
    paper's formula uses.

    Consequences, in increasing order of how much they cost to write:

    1. Fix $T_3$ to $k_3 N (1 + \log_2 r)$, or keep both forms and say which algorithm each belongs
       to. Rename the run count in the comparison table so it is not $p$.
    2. \S~`sec:pcrit` is then built on the wrong control variable. $p_{crit}$ is defined as a
       critical *inversion probability*; if the shipped cleanup is Timsort, the quantity that
       decides whether the method wins is the **run count the coder leaves**, not $p$. The honest
       rework states both: $p$ governs the cleanup if you use insertion sort, $r$ governs it if you
       use Timsort, and the paper uses Timsort. This is the largest single piece of writing on this
       list and it touches the abstract's framing.
    3. The $T_3$-at-$p=0$ argument (the permits, one full composite comparison per element) is
       **unaffected and stays** --- at $p = 0$ there is one run and both models agree on $N-1$.

    ### B. Masking versus saturation, and "quasi-order-preserving" as a stated concept

    Robin's call (2026-09-22): this is a genuine discussion point and the paper should have it
    rather than quietly shipping one coder. The shape of the argument:

    - A husky code does **not** have to be order-preserving. It has to be *quasi* order-preserving:
      wrong often enough to matter is fine, because step 3 repairs whatever step 2 leaves. The paper
      currently treats order preservation as the requirement and imperfection as a matter of ties
      only. Widening that to admit genuinely mis-ordering codes is a small conceptual contribution,
      and it is what licenses the cheapest coders.
    - **Masking is the simplest thing that works.** `c & mask` is one instruction and it is exactly
      order-preserving *inside* the window --- for `englishCoder` every character in 64..127, which
      is all of A--Z and a--z. Outside the window it wraps, so an apostrophe (39) collides with `g`
      and `e`-acute (233) sorts as `i`. Quasi, not order-preserving.
    - **Saturation** (`clamp(c - offset, 0, width-1)`) is monotonic over all 65,536 chars, verified
      exhaustively, and costs a compare-and-select per character.
    - The measured trade (above, plus TODO 37): saturation costs roughly 25 ms per million to encode
      on an eight-core Mac and 1.7--3.0x the masking encode on Yunlu's Graviton, and buys nothing
      measurable in cleanup. **Masking wins on speed**, and the encode-plus-cleanup figures are
      111.1 ms masking against 136.9 saturating.
    - A point that sharpens the discussion and was not obvious: on the english corpus the two coders
      fail on *exactly the same words*. The word splitter emits letter-only tokens, so no token
      holds an apostrophe, hyphen or digit, and the only characters reaching outside either window
      are the letters at or above 128. The "don't" / "dongt" collision is real arithmetic but the
      corpus never exercises it. Whatever the paper says about masking should therefore be said in
      terms of the coder's window against the corpus's character repertoire, not in terms of
      punctuation.
    - The counter-argument, which is not a speed one: the paper *defines* a husky code as
      order-preserving, so a headline figure produced by a coder that violates the definition is
      awkward. Either the definition widens to "quasi", or the slower coder ships. **This is Robin's
      decision and the paper should show the reader the trade either way.**
    - The conditional that must be stated: all of this holds **because the cleanup is Timsort**. If
      the cleanup were adaptive insertion sort, masking's 86M inversions would cost of the order of
      a second and saturation would be mandatory. Coder choice and cleanup choice are coupled, which
      is itself worth saying.

    ### C. A caution about which characters actually cause the damage

    Found 2026-09-22 while checking Robin's hypothesis that `asciiCoder` (9x7) would be much safer
    than `englishCoder` (10x6) because a 7-bit mask preserves all printable ASCII. It does preserve
    it --- apostrophes, hyphens and digits all survive `& 0x7F` intact --- **and it does not help**:
    `asciiCoder` carries 87.8M inversions against `englishCoder`'s 85.8M, slightly *more*. What
    dominates is not punctuation but the characters at or above 128, which neither width preserves.
    Punctuation is the visible hazard; the corpus's character repertoire is the invisible one. Worth
    a sentence, because the intuition is natural and wrong.

    **Chasing this found a real defect in our own tokenizer, now fixed --- see the separate note
    below.** What follows was re-measured afterwards, against the repaired tokenizer.

    **Repairing the mojibake does not help, and costs a little (measured 2026-09-22).** Robin asked
    whether repairing the corpus would help, on the reasonable view that the mojibake is an error
    and testing against a corrected corpus is defensible if declared. It is repairable, at line
    level: repairing each line *before* tokenizing takes words still carrying a mojibake marker from
    1,843 to 238 and recovers `Amelie`, `Nurnberger`, `Cliches`, `Souffles`, `Cafe`, `fur` properly
    accented. (Word-by-word repair does not work, because the splitter has by then already dropped
    the trailing non-letter: `ClichA-tilde-copyright` is left as `ClichA-tilde`, and the
    continuation byte is gone.)

    But the repair *restores genuine accented characters*, which is exactly what a fixed-width coder
    cannot represent, so it moves our numbers the wrong way. At n = 1,000,000:

    ```
                        words>127    runs    inversions   timsort(ms)  adaptive(ms)
    englishMask  before      1,843  17,305  180,352,275         36.9          841.6
    englishMask  after       2,536  17,815  187,739,187         37.2          916.9
    asciiMask    before      1,843  30,520  185,161,666         42.3          921.0
    asciiMask    after       2,536  31,078  259,115,324         45.4        1,199.0
    ```

    Runs rise about 3%, so the Timsort cleanup is unchanged within the noise of an eight-core Mac;
    the masking coders' inversions rise 4% and 40%, and the adaptive cleanup degrades accordingly.

    So the corruption was, accidentally, flattering our coders --- it was replacing accented
    characters with truncated ASCII-ish ones. **Recommendation: do not repair, and say in the paper
    that we checked.** One or two sentences forecloses a referee's question and is more interesting
    than silence: the mojibake is upstream, our copy is byte-identical to a fresh download (sha256
    `475aa01b...`, item 36), a correct repair exists, and applying it moves the Timsort cleanup by
    less than the measurement noise, in the unfavourable direction. That supersedes the earlier
    "deleting every affected word is worth 2.6%" ceiling, which measured deletion rather than repair.

    ### C2. The word splitter was discarding 15% of the english corpus and 51% of the chinese one

    Found 2026-09-22 while establishing why word-level mojibake repair failed, and **fixed the same
    day** at Robin's instruction ("our tokenizer is wrong. We need to fix that, even if it costs us
    time"). `HuskySortBenchmark.REGEX_LEIPZIG` read

    ```
    [~\t]*\t(([\s\p{Punct}\uFF0C]*\p{L}+)*)
    ```

    which requires the captured sentence to be an alternation of ASCII punctuation and Unicode
    letters. Java's `\p{Punct}` is POSIX, hence **ASCII-only**, so the group stopped at the first
    character that was neither a Unicode letter nor ASCII punctuation, and `String.split` was handed
    only that prefix. Digits qualified; so did the pound sign, the copyright sign, and every
    non-ASCII dash or quotation mark. The failure was silent and total:

    ```
    eng-uk_web_2002_1M    15.2% of sentence characters discarded
                          275,387 -> 304,959 distinct words, 16.39M -> 18.86M tokens
    zho-simp-tw_web_2014  51.5% discarded (U+3002, the ideographic full stop, is not ASCII punct)
                          24,215 -> 50,009 distinct, 25,745 -> 56,134 tokens
    ```

    "With Amelie (Cert 15) Jeunet combines the best of his two previous films..." yielded "With
    Amelie (Cert". "The figure size must not exceed A4 or 8.5 x 11in..." yielded "The figure size
    must not exceed A". Every line with a digit in it lost everything from the digit onwards.

    The fix is `[~\t]*\t(.*)` for the line --- a Leipzig line is `<id>\t<sentence>`, so the
    sentence is simply everything after the first tab, and any attempt to validate it inside the
    pattern can only truncate it --- together with `[^\p{L}]+` for the splitter, replacing an
    enumeration of separators that could never be complete. The splitter change is behaviour-neutral
    on the text the old line pattern captured; the line pattern is the repair. It is **purely
    additive**: no word either corpus produced before is lost.

    **Consequences.**

    1. Every english and chinese string figure ever measured, by us or by Yunlu, is superseded.
       chinesenames is not affected (it loads via `lineAsList`). This is a second full-suite re-run,
       and Robin accepted that cost explicitly.
    2. It makes the masking coders look *worse*, because the recovered vocabulary is more accented:
       `englishCoder`'s inversions at n = 1,000,000 go 85,818,889 -> **180,352,275**, `asciiCoder`'s
       87,797,452 -> **185,161,666**, while every run count moves by less than 4%. The
       masking-versus-saturation verdict does not change --- Timsort still cannot see it --- but the
       discrimination in section A is now 1,637x and 855x rather than 731x and 373x, which makes the
       experiment stronger, not weaker.
    3. The paper's data-source section must say how the corpus is tokenized, which it currently does
       not. Given that the bug hid 15% of one corpus and half of the other, "words extracted from
       the Leipzig sentences" is not a sufficient description.
    4. It supersedes the claim in section C above that punctuation is not what damages the masking
       coders "because the mojibake dominates". The truer statement is stronger: the splitter emits
       letter-only tokens, so **no token contains punctuation or a digit at all**, and the only
       characters reaching outside a 6- or 7-bit window are letters at or above 128. Both masking
       coders therefore fail on exactly the same words.

    ### D. The parallel claims are now too pessimistic --- item 40's precondition is met

    Item 40 says "do not rewrite the conclusion a fourth time without measuring" and names the
    deciding benchmark. Half of it has now been run. With the encode parallel (`15cc2ff`) but the
    cleanup still serial, `parallelRadixHuskySortAuto_pAll` beats `Arrays.parallelSort` on **8 of 15
    cells**: english@1M 1.24--1.34x, chinese@1M 1.30x, all three permit sizes 1.13--1.57x,
    `Long[]`@10M 1.31x, english@32k 1.14x, chinese@32k 1.08x. The conclusion's opening --- "The
    advantage demonstrated here is a serial one" --- is false for those cells. The replacement is
    not a reversal but a size-and-corpus-dependent claim; the losses are english@200k, chinese@200k,
    chinesenames at all three sizes, `Long[]`@2M.

    The other half of item 40's benchmark --- a `parallelSort` post-sorter --- is **built as of
    2026-09-22 and awaiting a run**, opt-in rather than default. The cleanup is the only serial
    phase left, and it is 50% of the parallel sort at english@200k, 74% at english@1M and **91% at
    chinesenames@1M**, where the pinyin cleanup alone is 506 ms of a 558 ms sort.

    **The speed-up it buys is smaller and more variable than this item first claimed, and the
    correction matters.** Item 40 records 2.8--3.8x. That figure came from *randomly shuffled*
    arrays, where `parallelSort`'s leaves do real work. Measured on the hand-over arrays the cleanup
    actually receives, eight cores, one corpus per JVM:

    ```
    corpus         n          runs   mean run   serial   parallel   speed-up
    english      200,000     2,000      100.0    10.82      10.36      1.04x
    english    1,000,000    16,061       62.3    42.38      22.72      1.87x
    chinese      200,000     3,107       64.4     4.99       9.38      0.53x  <-- loses
    chinese    1,000,000    16,790       59.6    12.47       8.34      1.50x
    chinesenames 200,000    30,462        6.6    70.71      20.93      3.38x
    chinesenames 1,000,000 162,690        6.1   290.82     114.64      2.54x
    ```

    So it pays in proportion to the work available and **can go negative**: Robin's original
    objection --- the elements are not independent, so chunks cannot be processed separately --- is
    right about the mechanism, and what it costs is a factor, not the whole gain. The dependence
    lives in the merge, and merging parallelizes; but on a nearly ordered array serial Timsort finds
    long runs and stops while `parallelSort` still pays about `log(4p)` merge levels over `4p`
    blocks, so where the cleanup is already cheap the division by p does not cover it.

    Projected onto the request-11 figures --- **projections, not measurements**: chinesenames@200k
    0.43x -> ~1.2--1.4x and chinesenames@1M 0.42x -> ~0.9--1.2x (the two worst cells in the table,
    flipping or reaching parity), english@1M 1.34x -> ~1.9x, chinese@1M 1.30x -> ~1.5x,
    english@200k unchanged at 0.53x, and chinese@200k **regressing** from 0.95x to ~0.6x.

    Hence opt-in. No guard rule is encoded, because n alone cannot express one: chinese and
    chinesenames at n = 200,000 are the same size and want opposite answers. What separates them is
    how much disorder the coder left, which is what `CleanupPassBenchmarks` parameterises --- so the
    rule should come from its new `parallelTimsortCleanup` arm on the machine of record.

    **A deeper reading of the same numbers.** chinesenames gains most because its coder is worst:
    the radix phase cuts its run count only **3.1x** (500,269 -> 162,690) against english's **62x**,
    leaving runs of mean length 6.1 where english has 62.3. Parallelizing that cleanup treats a
    symptom; the cause is the pinyin coder, which is items 10 and 11 --- now quantitatively
    motivated rather than speculative. Worth a sentence in the paper, because a referee who sees the
    chinesenames row will ask why it is the weak one.

    ### E. The encode, not the cleanup, is the dominant serial term

    Direct phase measurement of the real sorter (2026-09-22, english@1M, saturating coder, eight
    cores): encode 65 ms (46%), digit passes plus permutation 38 ms (27%), cleanup 38 ms (27%), full
    serial sort 141 ms. The paper treats $T_1 = k_1 N$ as the cheap term and spends its analysis on
    $T_2$ and $T_3$. For a ten-character coder $T_1$ is the **largest** of the three. That is worth
    stating plainly, and it is the motivation for parallelizing step 1 (item 41) rather than an
    afterthought.

    Caution for anyone reading an earlier draft of this: a "74% cleanup" figure was quoted on
    2026-09-21 from dividing a *serial* cleanup measurement by a *parallel* total. It is arithmetic
    about the parallel implementation, not about the algorithm, and it is not a statement that the
    coding is poor. The serial share is 27%.

    ### F. Tables and figures: replace, do not splice

    - Every english and commonwords string row of requests 4, 9 and 10 is superseded (the corpus
      changed coder), as is every `RadixHuskySort` / `ParallelRadixHuskySort` row of every earlier
      request (internals changed), and request 9's chinesenames `systemSortParallel` row (it sorted
      by code point, not pinyin --- a cheaper and different job from the husky sorts beside it).
    - **Warm-up**: 35 of 81 english String-class rows in request 11 are unconverged at 2 s
      iterations. `systemSort / radixHuskySortAuto` reads 3.98x / 2.90x at english 32k / 200k but
      steady state is about 4.5x / 3.2x --- the paper's ratios would be *understated*. No chinese or
      chinesenames row is affected. Any english figure in the paper needs the long-warm-up form.
    - **Label the n = 32,000 parallel rows "1 chunk (serial)"** (Yunlu's Q4, agreed in the reply to
      request 10, not yet done). At that size `chunks = max(1, min(p, n/16384)) = 1`, so a row
      labelled `p8` or `pAll` invites exactly the wrong inference. Print chunks, bits and passes per
      row, since the automatic width varies with size.
    - \S~A.5's Table `tab:TimvsInsertion` must say **which** insertion sort it measured: it is the
      binary-search form, confirmed by Yunlu at 5.18x / 4.67x against Timsort on english, which
      brackets the paper's reported 4.5x. Timsort survives as the default, but the margin against
      the *adaptive* form is a coin flip at small $p$, not 4.5x, and the crossover is near
      $pn \approx 12$--$23$ (adaptive wins unicode at n = 20k / 50k / 100k by 0.63x / 0.68x / 0.82x,
      ties at 200k, loses at 1M). It fails at the window's *lower* edge for a reason worth one
      sentence: below about one move per element both algorithms are at a scan floor of roughly
      50--60 ns per string and there is no work left for adaptivity to save.

    ### G. Two smaller things already noted elsewhere, repeated here so the list is complete

    - **Item 36's character-count lever.** The english corpus was encoded with a coder capturing
      four characters where ten were available, collapsing a 275,333-word vocabulary into 68,512
      codes. Runs fall 365,958 -> 16,641. The design guidance --- capture as many characters as the
      width allows, it dominates everything else about the coder --- is worth stating as guidance.
    - **Item 38's `AdaptiveInsertionSort`** now exists in the repo, so \S~A.5 can name and cite the
      $N + X$ algorithm rather than gesture at it.

    ### What is NOT on this list

    No new claim that the cleanup parallelizes *in our implementation* (it does not yet); no
    rewrite of the conclusion beyond what D describes; no parallel QuickHuskySort (item 42); and no
    verdict on masking versus saturation until the cells committed in `c5f47d3` have been run --- the
    figures in A and B are hand timings on a loaded eight-core Mac, consistent and directionally
    clear, but not JMH on the machine of record.

44. **An exactly order-preserving pinyin coder, and the general principle behind it (2026-09-22).**
    Robin asked why the Chinese-names coder is the worst in the project, given that names are two or
    three characters and the coder packs five. The answer is that length was never the constraint,
    and the diagnosis generalises further than the fix.

    ### The defect: the code implements two levels of an ordering that has three

    `NAME_ORDER` compares each character on **(1)** pinyin syllable ordinal, **(2)** tone, and
    **(3)** Unicode code point --- the last a tie-break between true homonyms, standing in for the
    stroke-count data the class comment notes is unavailable. `encodeHanyuOrdinal` packs only (1) at
    9 bits and (2) at 3. There is no third level, so true homonyms receive byte-identical codes.

    Measured on `Chinese_Names_Corpus.txt` (1,145,009 names; 15.6% of two characters, 84.4% of
    three; 12 bits each, so 64 bits holds five --- capacity was never near the limit):

    - 1,145,009 distinct names collapse to **818,114 codes**, 1.40 names per code.
    - Of 162,689 descents left in a shuffled million, **58.6% are ties** (阿滨/阿斌, both *ā bīn*;
      阿辰/阿晨/阿臣, all *ā chén*) and **41.4% are mis-orderings** --- and those are the same cause
      one step removed: 阿鹭 against 阿露露, where 鹭 and 露 are both *lù*, so the code ties at
      character 2 and falls through to a padding zero at character 3, while NAME_ORDER had already
      decided at character 2 on code point (鹭 U+9E6D against 露 U+9732).

    So 100% of the residual disorder traces to the missing third level. Not to length, not to
    polyphones (item 11), not to stroke order (item 10).

    ### The fix: rank the character, do not decompose it

    Pack one **rank in pinyin order** per character instead of a syllable and a tone, the rank taken
    from `pinyinCharacterKey` --- which is the comparator's own key function. CJK Unified Ideographs
    plus Extension A is U+3400..U+9FFF, 27,648 code points, so a rank needs 15 bits and four
    characters need 60 of 64.

    | | distinct codes | names/code | NAME_ORDER descents, 300,000 names sorted by code alone | encode, 1M names |
    | --- | ---: | ---: | ---: | ---: |
    | `chineseEncoderPinyin` | 818,114 | 1.40 | 59,332 | 133.25 ms |
    | `chineseEncoderPinyinRank` | 1,145,009 | 1.00 | **0** | **41.20 ms** |

    Zero descents over the entire corpus, so `perfect` is reported and **the cleanup pass does not
    run**. It is also **3.2x faster to encode**, because `RANK[c - 0x3400]` is one load from a 54 KB
    `char[]` where `syllableAndToneOf` is a memoized pinyin4j lookup --- a hash, a probe and two
    dereferences. The table is *indexed, not searched*, so its size costs nothing; and the live
    working set is smaller still, a few thousand characters with a Zipfian frequency distribution.

    Implemented as a third dialect, `HanyuRank`, alongside the ordinal one rather than in place of
    it, so the two can be measured against each other before anything switches. Perfection is
    claimed per element and only when it holds --- at most four characters, every one inside the
    block --- because claiming it wrongly skips a cleanup that was needed and yields a silently
    wrong answer. `HuskyCoderChinesePinyinRankTest` asserts the whole-corpus claim directly.

    Rough value: chinesenames@1M is 558 ms of which about 506 is cleanup. Removing it leaves ~52 ms
    against `Arrays.parallelSort`'s 235 --- some **4.5x**, from 0.42x, the worst cell in the table
    becoming one of the best. That dwarfs the parallel cleanup of item 40, which got the same cell
    only to parity; the two are alternative answers to one problem and should not both be needed.

    ### The general principle, which is what the paper should take

    The husky code is a proxy key for a *string*. The rank table is a proxy key for a *character* ---
    the same trick, nested one level down. And at that level it is **exact**, because the domain is
    small enough to enumerate.

    That is worth stating as a general result rather than a Chinese special case:

    > Where the alphabet has at most 2^b symbols and their collation order can be precomputed, a
    > rank table gives an exactly order-preserving b-bit code per symbol, and hence a **perfect**
    > husky code for any string of at most 64/b symbols.

    It supplies the limiting case of the whole mechanism. A husky coder is normally approximate
    because it must compress an unbounded domain into 64 bits; when the *per-symbol* domain is
    enumerable, the compression is lossless and the cleanup pass disappears. Chinese is the case
    where this pays most --- 27,584 symbols, an expensive collation, and short strings --- but the
    statement is not about Chinese. It also explains, retrospectively, why the ASCII and english
    coders are imperfect for a reason that is *not* this: their alphabet is enumerable too, but at
    one character per 6 or 7 bits they can hold only 9 or 10 characters of an unbounded-length word.
    The constraint there is string length, which is real; for names it never was.

    ### Not yet done

    - The paper: this is a new subsection, and it interacts with item 43 A (the cleanup cost model)
      and 43 B (quasi-order-preserving) --- with a perfect coder there is no cleanup term at all,
      which is the cleanest possible illustration of what `p_crit` is about.
    - The rank table is built on first use, about 340 ms. It could be precomputed into a resource.
    - Nothing switches over until Yunlu's numbers arrive: `radixHuskySortAutoPinyinRank` and
      `parallelRadixHuskySortAuto_pAll_pinyinRank` are the A/B rows, and `pinyinRank` is the
      CleanupPassBenchmarks cell (where it should show the p = 0 floor --- N-1 comparisons and no
      moves, the same quantity the permits measure in the paper's \S~`sec:pcrit`).

45. **Two latent failures in `src/it`, found 2026-09-23 when Robin enabled it to check a refactor.**
    Neither is caused by anything recent: running `-Pintegration-test` on `parallel-redesign-Robin`
    and on `parallel-redesign` gives byte-identical results, 9 tests and 2 errors on both. They have
    simply been invisible, for the reason in the third bullet.

    ### 45a. ~~`AlphabetTest.getCountIndexUnicode` is written against a contract that no longer exists~~ **DONE 2026-09-23**

    ```
    SortException: char Ĭ (300) has no position in this alphabet. prepare() must be called with
    the whole input before sorting, so that characters beyond ASCII can be assigned positions in
    code-point order.
    ```

    The test is stale, not the code. `Alphabet` used to assign a bucket position to each non-ASCII
    character *on first encounter*; that was changed --- correctly, and the reasoning is in
    `Alphabet.prepare`'s javadoc --- because first-encounter order is not code-point order, so two
    strings differing first at a non-ASCII character came out in whatever order the input happened
    to present those characters. `getCountIndex` now refuses rather than guessing. The test still
    calls it on a fresh `Alphabet` with no `prepare`.

    **The fix was one line, and the test's expectations were already right.** After
    `prepare(new String[]{"Ĭ", "Ɛ", "￿"})` the same three characters map to
    **256 / 257 / 258**, exactly what the test asserts, so the `prepare` call was added and nothing
    else changed. `AlphabetTest` is 4/4. Worth repairing rather than deleting: it is the only direct test of the
    spare-region mapping, which is the part of `Alphabet` that the monotonicity of
    `UnicodeMSDStringSort` rests on.

    ### 45b. ~~`BenchmarkIntegrationTest.testStrings10K` and `testStrings100K` cannot fit their timeout~~ **DONE 2026-09-24**

    Both run benchmark-sized workloads under a wall-clock `ProcessorDependentTimeout`, which scales
    a nominal 10 s down to **7,353 ms** on Robin's machine. Measured by running the same calls with
    a reduced run count and scaling, 2026-09-23:

    | test | n | runs | needs | budget | over by |
    | --- | ---: | ---: | ---: | ---: | ---: |
    | `testStrings10K` | 10,000 | 3,800 | ~24 s | 7.4 s | 3x |
    | `testStrings100K` | 100,000 | 255 | ~17 s | 7.4 s | 2x |

    This is not the corpus getting bigger: the word-splitter repair of item 43 C2 added 15% of
    vocabulary, and the two branches time out at 14.0 s and 14.6 s respectively, which is noise.
    The likelier cause is accumulation --- `benchmarkStringSorters` has gained sorters steadily
    (the radix family, MSD, the parallel rows) while the run counts and the timeout stayed where
    they were. The class comment already shows the strain: "you cannot include insertionSort among
    the sort methods to be used: it WILL time out here."

    Three options, in the order I would consider them:

    1. **Cut the run counts** to what the budget allows --- 3,800 to about 1,000, and 255 to about
       100. These are correctness-and-smoke tests that happen to be built on the benchmark harness;
       they do not need statistical power, and nothing reads their timings.
    2. **Raise the timeout** to 30 s nominal. Honest, but it makes a slow suite slower and only
       defers the next accumulation.
    3. **`@Ignore` them**, which is what has effectively happened already, but explicitly and with a
       reason.

    **Done 2026-09-24, by option 1.** 3,800 runs -> 500 and 255 -> 50. Measured after the change:
    `testStrings10K` 1.21 s (16.5% of the 7,353 ms budget) and `testStrings100K` 2.04 s (27.8%),
    the whole class 3.565 s, 5/5 green. The reduction is deliberately more than arithmetic requires,
    because the budget on an unlisted machine is a flat 10 s with no guarantee that machine is fast,
    and because the next sorter added will eat into it. `testStrings10KInstrumented` was left alone
    at 950 runs: it measures 0.106 s, 1.4% of budget, so it was never near the limit.

    The reasoning for option 1 over the other two: nothing reads the timings these tests print, so
    the run count was buying statistical power that no one spends. A note to that effect is now on
    `testStrings10K`, together with the instruction to re-check the budget whenever a sorter is
    added to `benchmarkStringSorters` --- one sorter at a time is how a passing test became a
    failing one.

    ### 45d. ~~`InstrumentationIsCompleteTest` passes in the suite and fails on its own~~ **DONE 2026-09-24**

    **The first diagnosis recorded here was wrong and is corrected below.** It said order
    dependence --- that the test relied on state a neighbour left behind. It does not. The real
    cause is the same mechanism as 45c, one layer over: a contaminated *resource*, not a stale
    class, and it is a good deal nastier than order dependence because a clean build hides it.

    `src/it/resources/config.ini` and `src/test/resources/config.ini` are different files, and the
    integration-test profile adds the former as a test resource, so it is copied **over** the latter
    into `target/test-classes/config.ini` --- where it then stays. Among the differences:
    `src/test` has `fixes = false`, `src/it` has `fixes = true`.

    That matters because with `fixes` on the instrumented Helper counts inversions fixed, and doing
    so *compares*. Those comparisons go through `Counted.compareTo` like every other, so they land
    in the test's `actual` while never reaching the StatPack's `COMPARES`. The test then reports
    comparisons "made but not counted" that the sort never made, and the three sorts that swap most
    --- `QuickSort_3way`, `QuickSort_DualPivot`, `IntroSort` --- fail. The `@BeforeClass` pinned
    `compares`, `swaps` and `hits` but inherited `fixes`.

    Demonstrated end to end, 2026-09-24:

    ```
    mvn clean test -Dtest=InstrumentationIsCompleteTest   6/6 pass, fixes = false
    mvn -Pintegration-test test                           target/test-classes/config.ini <- src/it
    mvn test -Dtest=InstrumentationIsCompleteTest         3 of 6 fail, fixes = true
    ```

    **Fixed** by pinning `fixes = false` alongside the three settings already pinned --- one line,
    and the right one, because a test that declares the instrumentation it depends on cannot be
    contaminated by whatever else has written to the classpath. Verified by contaminating
    deliberately and re-running: 6/6. With this and 45a and 45b done, `-Pintegration-test` is
    **450/0** and the normal build is 432/0; both green together for the first time.

    The lesson is 45c's, sharpened. A stale class is at least visible in a diff of `target`; a
    stale *config* silently changes what the code under test does. And the failure mode is
    perfectly calibrated to waste time: it appears only after someone runs the integration profile,
    disappears on `mvn clean test`, and therefore reads as flakiness.

    ### 45c. Why nobody noticed, which is the part worth fixing

    `src/it/java` is added as a test source by `build-helper`, but only inside the
    `integration-test` profile, so a normal `mvn test` never compiles it and the six classes there
    never run --- 432 tests rather than 450.

    **An IDE does compile it**, because it honours the same source root, and writes the classes into
    `target/test-classes`. A subsequent `mvn test` then picks them up and runs them, since they
    match surefire's default includes. That is how this surfaced: 450 tests and 3 errors from a
    tree whose `mvn clean test` is 432 and green. It is a trap worth knowing about, because the
    same mechanism can make a build look broken that is not, and --- worse --- can make a stale
    class pass a test that the current source would fail.

    Options: run `-Pintegration-test` in CI so these cannot rot unnoticed; or fold `src/it` into the
    default build with the slow tests trimmed per 45b. The present arrangement, where the tests
    exist but run only by accident, is the one arrangement with no upside.

46. ~~**`HuskyCoder.huskyEncode(byte[])` does not pad, so its codes are not order-preserving across
    lengths**~~ **DONE 2026-09-24**, together with `GenericCollator.English`.

    ```java
    default long huskyEncode(final byte[] bs) {
        long result = 0L;
        for (int i = 0; i < bs.length && i < 7; i++) result = (result << 8) | bs[i] & 0xFF;
        return result;                       // <-- no trailing shift
    }
    ```

    A two-byte array lands in the low sixteen bits while a seven-byte array fills the long, so a
    short key always codes below a longer one whatever the bytes say: `"b"` is `0x62` and `"ab"` is
    `0x6162`, so `"b"` codes first although `"ab"` sorts first. Among equal-length keys the codes
    are exactly order-preserving, which is what makes the omission easy to miss.

    Compare `HuskyCoderFactory.stringToLong`, which computes `padding = maxLength - length` and ends
    with `result <<= bitWidth * padding` for precisely this reason. The byte-array path never got
    the same treatment, and its javadoc --- "a long which is based on the first seven of the given
    bytes" --- does not mention the bias.

    **The fix is one line**, `result <<= 8 * (7 - Math.min(bs.length, 7));`, and it would strictly
    improve the encoding: fewer inversions for the cleanup pass, no cost at encode time.

    **Done**, on Robin's instruction --- "I'd like things to work the way they sound". Safe to do
    now for the reason that made it safe to defer: the only production user of the path is
    `HuskyCoderFactory.chineseEncoderCollator`, via `SequenceEncoder_Collator`, which appears in
    tests and in `HuskySortHelper`'s name map and in **no benchmark at all**, so no figure in the
    paper moves and Yunlu's frozen jar is unaffected. Verified by grep before changing anything.

    ### And `GenericCollator.English`, which was the same complaint one level up

    The class defined `English` as a collator whose key compared by `String.compareTo`, so it put
    "Banana" before "apple" where `Collator.getInstance(ENGLISH)` does the reverse. The name said
    one thing and the code did another. Now:

    - **`English`** is `new GenericCollator<>(Collator.getInstance(Locale.ENGLISH))` --- genuinely
      English, and its key bytes compare in collation order, so a husky code taken from them does
      too. Collation keys are longer than their source, so expect `perfect` to be false for all but
      the shortest strings; that is honest, and the cleanup pass is what makes the order exact.
    - **`CodePointOrder`** is the old behaviour under a name that describes it, with
      `CollationKeyEnglish` renamed to `CollationKeyCodePoint`. Worth keeping rather than deleting:
      UTF-8 preserves code-point order, so its bytes compare as its keys do, and code-point order is
      what the rest of this project's string sorts use as their natural order.

    One thing found while making that change, and **an initial claim about it that was wrong**.
    `Collator` is mutable internally --- `RuleBasedCollator` reuses its iterators and buffers rather
    than reallocating them --- and the first note here said that made it unsafe to share. It does
    not. Both `compare` and `getCollationKey` are declared `synchronized`, deliberately, and the
    JDK source says why in as many words: "the objects persist anyway to avoid wasting extra
    creation time. compare() and getCollationKey() are synchronized to ensure thread safety with
    this scheme."

    So it is correct under concurrency, and the real cost is **contention**: every caller of a
    shared instance serializes on one monitor, which is a poor default for a `public static` field
    in a project that has a parallel sorter. The `Collator` constructor therefore gives each thread
    its own clone via a `ThreadLocal`, which is the documented way out ---
    `RuleBasedCollator.clone` exists for it and uses a private copy constructor because, as it
    notes, "This is faster."

    The accompanying test is named for what it actually checks: that a cloned collator yields keys
    identical to the original's. It would have passed before the change as well, which is worth
    stating rather than hiding, since a test that cannot fail for the reason you claim is not
    evidence for that reason.

    `GenericCollatorTest` now has 14 tests including
    `huskyCodesAreOrderPreservingAcrossKeyLengths`, the regression test for the padding. Unit suite
    453/0, integration 475/0.

47. ~~**`Config.getString` throws on a null default where `get` does not**~~ **DONE 2026-09-24.** It was
    one of three public methods with no references anywhere, turned up while checking the TESTME
    sweep, and writing its test exposed the asymmetry.

    ```java
    public String getString(final String sectionName, final String optionName, final String defaultValue) {
        final String s = get(sectionName, optionName, defaultValue);
        if (s.isEmpty()) return defaultValue;        // <-- NPE when s is null
        return s;
    }
    ```

    `get(section, option, null)` returns null quite happily --- `ConfigTest` asserts exactly that ---
    so `getString(section, option, null)` throws a NullPointerException. Its siblings `getInt`,
    `getLong` and `getDouble` all write `if (s == null || s.isEmpty())`, so this is an inconsistency
    within one family rather than a considered choice.

    **Fixed** by adding `s == null ||`, which is what the siblings do, so `getString` now agrees
    with `get`: a null default comes back as null rather than throwing.
    `ConfigTest.testGetString` asserts the new behaviour at three points --- a null default with an
    absent key, with an empty value, and `get` itself for comparison. Note that the last of those
    needs `(String) null`: an unadorned `null` matches both `get(Object, Object, String)` and
    `get(Object, Object, Class<T>)`, which is why `Config` casts the same way internally.

    No caller is affected, the method having none. It is worth having correct anyway: the next
    person to reach for the typed getter should not have to discover that one member of the family
    handles an absent option differently from the other three.
