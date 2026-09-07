package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmark;
import edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Trainer for the corpus-derived polyphone override table (TODO.md item 11). NOT a JUnit test
 * (deliberately: it takes a couple of minutes over the full 1.14M-name corpus and writes
 * files) -- run its main() manually to (re)generate {@code polyphone_overrides.txt}.
 * <p>
 * Background (TODO.md items 9 and 11): {@code Chinese_Names_Corpus.txt} is pre-sorted by
 * pinyin by its curator, making its own adjacent-pair order an independent oracle. 93.43% of
 * all 1,145,008 adjacent pairs agree with {@link HuskyCoderChinesePinyin#NAME_ORDER}; of the
 * disagreement, 0.32% (of all pairs) is attributable to polyphone misreadings --
 * {@code ChineseCharacter.alt()} taking pinyin4j's first/default reading where the curator
 * evidently used another (e.g. the common surname reading). This trainer recovers the
 * curator's reading conventions from the corpus itself:
 * <ol>
 * <li>BASELINE GATE: re-measure the adjacent-pair agreement with an empty override table and
 * require it to reproduce item 9's re-checked figure (1,069,784 of 1,145,008, 93.43%) before
 * trusting anything else this program computes.</li>
 * <li>For each adjacent pair of names, find the first differing character position k
 * (identical shared prefixes cancel regardless of readings, so the curator's ordering
 * decision was made at k). Pairs where one name is a prefix of the other carry no
 * character-level signal and are skipped.</li>
 * <li>Round 1: where exactly one of the two characters at k is a polyphone (&ge;2 distinct
 * readings after de-duplication), each of its candidate readings is compared against the
 * opponent's (single) reading using the same syllable-then-tone character-level order
 * NAME_ORDER uses ({@link HuskyCoderChinesePinyin#compareAltReadings}) but WITHOUT the final
 * code-point tie-break: a reading that sorts strictly consistently with the observed corpus
 * order gets a FOR vote, strictly inconsistently AGAINST. If ANY candidate reading ties the
 * opponent (syllable+tone equal) the whole pair is discarded as ambiguous: that pair's order
 * may well have been decided by the curator's stroke-count convention, which readings cannot
 * reproduce (item 10 territory, not item 11), and counting it would credit the other readings
 * with spurious votes. Additionally, a pair only contributes votes at all if it is a
 * "discriminating pair": at least one candidate reading strictly FOR and at least one
 * strictly AGAINST -- the readings land on opposite sides of the opponent, so the observed
 * order genuinely distinguishes them; pairs where every reading gets the same verdict say
 * nothing about WHICH reading the curator used.</li>
 * <li>Decision: per character, the reading with the most FOR votes wins; an override is
 * emitted only if the winner differs from pinyin4j's default (element 0), has &ge;75% of the
 * character's total FOR votes, and that total is &ge;10 -- i.e. only decisive, well-supported
 * wins.</li>
 * <li>Round 2: pairs where BOTH characters at k were polyphones were skipped in round 1
 * (neither side's reading is known, so a vote cannot be attributed). With round-1 winners now
 * fixed (treated as monophones reading their winning override), those pairs are re-harvested
 * where exactly one side remains unresolved, and the same decision rule adds any new
 * decisive overrides.</li>
 * <li>Output: writes the override resource (committed) plus a full per-character vote-tally
 * stats file (target/, not committed), then re-measures agreement with the trained table
 * active -- installing it via the package-private hooks, since both the table and
 * HuskyCoderChinesePinyin's alt() memoization are static (a fresh coder instance would NOT
 * pick the new table up) -- and prints the BEFORE/AFTER comparison.</li>
 * </ol>
 */
public final class PolyphoneOverrideTrainer {

    /** Item 9's re-checked (2026-08-05) baseline: agreeing pairs of all adjacent pairs. */
    private static final long EXPECTED_BASELINE_AGREE = 1_069_784L;
    private static final long EXPECTED_BASELINE_PAIRS = 1_145_008L;
    /** Tolerance for the baseline gate, in percentage points. */
    private static final double BASELINE_TOLERANCE_PP = 0.05;

    /** Decision thresholds (see class javadoc / TODO.md item 11). */
    private static final int MIN_FOR_VOTES = 10;
    private static final double MIN_WINNER_SHARE = 0.75;

    public static void main(final String[] args) throws IOException {
        // 1. Load corpus names in file order (getWords preserves encounter order: BOM strip,
        // distinct, length >= 2 -- exactly what item 9's measurement used).
        final String[] names = HuskySortBenchmarkHelper.getWords(HuskySortBenchmark.CHINESE_NAMES_CORPUS, HuskySortBenchmark::lineAsList);
        System.out.println("Loaded " + names.length + " corpus names");

        // 2. BASELINE GATE -- explicitly install an EMPTY override table first, so the baseline
        // is honest even if a previously-trained polyphone_overrides.txt is already on the
        // classpath from an earlier run.
        ChineseCharacter.setPolyphoneOverrides(Collections.emptyMap());
        HuskyCoderChinesePinyin.clearAltCache();
        final long[] before = measureAgreement(names);
        final double beforePct = 100.0 * before[0] / before[1];
        System.out.printf("BASELINE: %d of %d adjacent pairs agree with NAME_ORDER (%.4f%%)%n", before[0], before[1], beforePct);
        final double expectedPct = 100.0 * EXPECTED_BASELINE_AGREE / EXPECTED_BASELINE_PAIRS;
        if (before[1] != EXPECTED_BASELINE_PAIRS || Math.abs(beforePct - expectedPct) > BASELINE_TOLERANCE_PP) {
            System.err.printf("BASELINE GATE FAILED: expected %d/%d (%.4f%%) +/- %.2fpp -- comparator usage is wrong; NOT proceeding.%n",
                    EXPECTED_BASELINE_AGREE, EXPECTED_BASELINE_PAIRS, expectedPct, BASELINE_TOLERANCE_PP);
            System.exit(1);
        }
        System.out.println("BASELINE GATE PASSED (expected ~" + String.format("%.2f%%", expectedPct) + ")");

        // 3. Polyphone candidates: every distinct corpus char with >= 2 readings after
        // de-duplicating identical raw strings (readings differing only in tone are kept --
        // tone affects ordering).
        final Map<Character, List<String>> candidates = findPolyphoneCandidates(names);
        System.out.println("Polyphone candidate characters in corpus: " + candidates.size());

        // 4-5. Round 1: harvest votes from single-polyphone pairs; both-polyphone pairs are
        // remembered for round 2.
        final Map<Character, Tally> tallies = new HashMap<>();
        final List<int[]> bothPolyphonePairs = new ArrayList<>();
        long prefixSkips = 0, noPolyphone = 0, round1Pairs = 0;
        for (int i = 1; i < names.length; i++) {
            final String a = names[i - 1], b = names[i];
            final int k = firstDifference(a, b);
            if (k < 0) { // one name is a prefix of the other: no character-level signal
                prefixSkips++;
                continue;
            }
            final char ca = a.charAt(k), cb = b.charAt(k);
            final boolean pa = candidates.containsKey(ca), pb = candidates.containsKey(cb);
            if (!pa && !pb) {
                noPolyphone++;
                continue;
            }
            if (pa && pb) { // both polyphones: cannot attribute the vote yet; round 2 territory
                bothPolyphonePairs.add(new int[]{i - 1, i});
                continue;
            }
            round1Pairs++;
            if (pa) harvestVotes(tallies, ca, candidates.get(ca), effectiveAlt(cb, null), true);
            else harvestVotes(tallies, cb, candidates.get(cb), effectiveAlt(ca, null), false);
        }
        System.out.printf("Pairs: %d prefix-skipped, %d with no polyphone at the deciding position, %d single-polyphone (round 1), %d both-polyphone (deferred to round 2)%n",
                prefixSkips, noPolyphone, round1Pairs, bothPolyphonePairs.size());

        final Map<Character, String> overrides = new LinkedHashMap<>();
        final List<String> statsLines = new ArrayList<>();
        decide(tallies, candidates, overrides, statsLines, 1);
        System.out.println("Round 1 overrides: " + overrides.size());

        // 6. Round 2: treat round-1 winners as fixed monophones (reading = winning override)
        // and re-harvest the deferred both-polyphone pairs where exactly one side is now fixed.
        final Map<Character, Tally> tallies2 = new HashMap<>();
        long round2Pairs = 0;
        for (final int[] pair : bothPolyphonePairs) {
            final String a = names[pair[0]], b = names[pair[1]];
            final int k = firstDifference(a, b);
            final char ca = a.charAt(k), cb = b.charAt(k);
            final boolean fixedA = overrides.containsKey(ca), fixedB = overrides.containsKey(cb);
            if (fixedA == fixedB) continue; // both fixed (no question left) or neither (still unattributable)
            round2Pairs++;
            if (fixedA) harvestVotes(tallies2, cb, candidates.get(cb), effectiveAlt(ca, overrides.get(ca)), false);
            else harvestVotes(tallies2, ca, candidates.get(ca), effectiveAlt(cb, overrides.get(cb)), true);
        }
        System.out.println("Round 2 usable pairs (exactly one side fixed by round 1): " + round2Pairs);
        final Map<Character, String> round2Overrides = new LinkedHashMap<>();
        decide(tallies2, candidates, round2Overrides, statsLines, 2);
        round2Overrides.forEach(overrides::putIfAbsent);
        System.out.println("Round 2 added overrides: " + round2Overrides.size() + "; total: " + overrides.size());

        // 7. Write the override resource (sorted by character for a stable, diffable file) and
        // the full stats file.
        final Map<Character, String> sorted = new TreeMap<>(overrides);
        final Path resourceDir = Paths.get("src", "main", "resources");
        final Path resourcePath = resourceDir.resolve(ChineseCharacter.POLYPHONE_OVERRIDES_RESOURCE);
        writeOverrideFile(resourcePath, sorted);
        // Also drop it onto the live test classpath so this JVM-external artifact is in sync
        // with what we are about to measure (and so tests can run without a rebuild).
        final Path targetClasses = Paths.get("target", "classes", ChineseCharacter.POLYPHONE_OVERRIDES_RESOURCE);
        if (Files.isDirectory(targetClasses.getParent())) Files.copy(resourcePath, targetClasses, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        final Path statsPath = Paths.get("target", "polyphone_override_stats.txt");
        Files.createDirectories(statsPath.getParent());
        try (final PrintWriter out = new PrintWriter(Files.newBufferedWriter(statsPath, StandardCharsets.UTF_8))) {
            statsLines.forEach(out::println);
        }
        System.out.println("Wrote " + sorted.size() + " overrides to " + resourcePath + " and full tallies to " + statsPath);

        // 8. AFTER measurement: install the trained table via the package-private hooks (both
        // the table and the alt() memoization are static, so this -- not a fresh coder
        // instance -- is what makes NAME_ORDER see the new readings).
        ChineseCharacter.setPolyphoneOverrides(sorted);
        HuskyCoderChinesePinyin.clearAltCache();
        final long[] after = measureAgreement(names);
        final double afterPct = 100.0 * after[0] / after[1];

        // 9. Summary.
        System.out.println("==== SUMMARY ====");
        System.out.printf("BEFORE: %d / %d agree (%.4f%%)%n", before[0], before[1], beforePct);
        System.out.printf("AFTER : %d / %d agree (%.4f%%)%n", after[0], after[1], afterPct);
        System.out.printf("Delta : %+d pairs (%+.4fpp)%n", after[0] - before[0], afterPct - beforePct);
        System.out.println("Override table size: " + sorted.size());
        System.out.println("---- override entries (char TAB reading) ----");
        sorted.forEach((c, r) -> System.out.println(c + "\t" + r));
    }

    /**
     * Adjacent-pair agreement measurement, exactly as TODO.md item 9's finding defines it: for
     * each of the names.length-1 adjacent pairs in corpus file order, does NAME_ORDER consider
     * them already in order?
     *
     * @param names the corpus names, in file order.
     * @return {agreeing pairs, total pairs}.
     */
    private static long[] measureAgreement(final String[] names) {
        long agree = 0;
        for (int i = 1; i < names.length; i++)
            if (HuskyCoderChinesePinyin.NAME_ORDER.compare(names[i - 1], names[i]) <= 0) agree++;
        return new long[]{agree, names.length - 1};
    }

    /**
     * @param a a name.
     * @param b the next name in corpus order.
     * @return the first index at which they differ, or -1 if one is a prefix of the other
     * (including the impossible equal case -- the corpus is distinct).
     */
    private static int firstDifference(final String a, final String b) {
        final int n = Math.min(a.length(), b.length());
        for (int k = 0; k < n; k++)
            if (a.charAt(k) != b.charAt(k)) return k;
        return -1;
    }

    /**
     * Scan every distinct character of the corpus and keep those with &ge;2 readings after
     * de-duplicating identical raw pinyin4j strings (pinyin4j frequently repeats a reading).
     * Element 0 of the de-duplicated list is pinyin4j's default -- the same element
     * ChineseCharacter.alt() uses when no override applies.
     */
    private static Map<Character, List<String>> findPolyphoneCandidates(final String[] names) {
        final Map<Character, List<String>> candidates = new HashMap<>();
        final Map<Character, Boolean> seen = new HashMap<>();
        for (final String name : names)
            for (int i = 0; i < name.length(); i++) {
                final char c = name.charAt(i);
                if (seen.putIfAbsent(c, Boolean.TRUE) != null) continue;
                final String[] readings = PinyinHelper.toHanyuPinyinStringArray(c);
                if (readings == null || readings.length == 0) continue; // no pinyin: skip
                final LinkedHashSet<String> deduped = new LinkedHashSet<>(java.util.Arrays.asList(readings));
                if (deduped.size() >= 2) candidates.put(c, new ArrayList<>(deduped));
            }
        return candidates;
    }

    /**
     * The character-level reading NAME_ORDER would effectively see for an opponent character:
     * its fixed override reading if one is supplied (round 2), else pinyin4j's default --
     * mirroring ChineseCharacter.alt() exactly (including the no-pinyin fallback to the
     * character itself).
     *
     * @param c        the opponent character.
     * @param fixedRaw the raw reading fixed by a round-1 override, or null for the default.
     * @return the opponent's reading in alt() format.
     */
    private static String effectiveAlt(final char c, final String fixedRaw) {
        if (fixedRaw != null) return ChineseCharacter.toAltFormat(fixedRaw);
        final String[] readings = PinyinHelper.toHanyuPinyinStringArray(c);
        if (readings == null || readings.length == 0) return String.valueOf(c);
        return ChineseCharacter.toAltFormat(readings[0]);
    }

    /**
     * Record one adjacent pair's votes for polyphone character {@code c}'s candidate readings
     * against the opponent's (known) reading. Corpus order says the polyphone character sorts
     * before the opponent if it is on the 'a' (earlier) side, after it otherwise.
     * <p>
     * Verdicts per candidate reading r (syllable-then-tone comparison, no code-point
     * tie-break): consistent with corpus order = FOR, inconsistent = AGAINST. If ANY reading
     * ties the opponent (syllable+tone equal) the whole pair is ambiguous and discarded --
     * see the inline comment (the curator broke such ties by stroke count, which no reading
     * choice can reproduce: item 10, not item 11). The pair only counts at all if it is
     * DISCRIMINATING -- at least one reading strictly FOR and at least one strictly AGAINST,
     * i.e. the readings land on opposite sides of the opponent; otherwise the pair carries no
     * signal about WHICH reading the curator used.
     *
     * @param tallies      accumulating per-character tallies.
     * @param c            the polyphone character being voted on.
     * @param readings     its de-duplicated candidate raw readings (element 0 = default).
     * @param opponentAlt  the opponent character's reading, alt() format.
     * @param polyphoneIsA true if c came from the earlier name of the pair (must sort &le;
     *                     opponent), false if from the later one (must sort &ge; opponent).
     */
    private static void harvestVotes(final Map<Character, Tally> tallies, final char c, final List<String> readings,
                                     final String opponentAlt, final boolean polyphoneIsA) {
        final int n = readings.size();
        final int[] verdicts = new int[n]; // +1 FOR, -1 AGAINST
        for (int i = 0; i < n; i++) {
            final int cmp = HuskyCoderChinesePinyin.compareAltReadings(ChineseCharacter.toAltFormat(readings.get(i)), opponentAlt);
            // Ambiguous-pair exclusion: if ANY candidate reading ties the opponent
            // (syllable+tone equal), the WHOLE pair is discarded, not just that reading's
            // vote. Rationale: the tying reading may well be the curator's actual reading, in
            // which case this pair's order was decided by the stroke-count convention (item 10
            // territory) that readings cannot reproduce -- while every OTHER reading would
            // receive a spurious, unanimously one-sided vote merely for sorting on whichever
            // side of the opponent the stroke-count decision happened to land. Measured
            // consequence of keeping such pairs: 华 (default hua2, the curator's evident
            // choice) drew 147 fake FOR votes for hua1 with hua2 abstaining in every one of
            // them, and the trained table made overall agreement WORSE than baseline.
            if (cmp == 0) return;
            if (polyphoneIsA) verdicts[i] = cmp < 0 ? 1 : -1;
            else verdicts[i] = cmp > 0 ? 1 : -1;
        }
        // Discriminating-pair check: the pair carries signal only if the candidate readings
        // land on OPPOSITE sides of the opponent -- at least one strictly consistent (FOR) and
        // at least one strictly inconsistent (AGAINST). If every reading falls on the same
        // side, the pair is equally compatible with all of them and identifies nothing.
        boolean anyFor = false, anyAgainst = false;
        for (final int v : verdicts) {
            if (v > 0) anyFor = true;
            else if (v < 0) anyAgainst = true;
        }
        if (!anyFor || !anyAgainst) return;
        final Tally tally = tallies.computeIfAbsent(c, x -> new Tally(readings));
        for (int i = 0; i < n; i++) {
            if (verdicts[i] > 0) tally.forVotes[i]++;
            else if (verdicts[i] < 0) tally.againstVotes[i]++;
        }
    }

    /**
     * Apply the decision rule to a round's tallies, appending any decisive overrides and
     * logging every tallied character's full counts and outcome to the stats lines.
     * An override is emitted only when the most-FOR-voted reading differs from the default,
     * strictly beats every other reading (no ties), holds &ge;{@link #MIN_WINNER_SHARE} of the
     * character's total FOR votes, and that total is &ge;{@link #MIN_FOR_VOTES}.
     */
    private static void decide(final Map<Character, Tally> tallies, final Map<Character, List<String>> candidates,
                               final Map<Character, String> overrides, final List<String> statsLines, final int round) {
        final List<Character> chars = new ArrayList<>(tallies.keySet());
        Collections.sort(chars);
        for (final char c : chars) {
            final Tally tally = tallies.get(c);
            final List<String> readings = candidates.get(c);
            long totalFor = 0;
            int winnerIdx = 0;
            boolean tie = false;
            for (int i = 0; i < readings.size(); i++) {
                totalFor += tally.forVotes[i];
                if (i > 0) {
                    if (tally.forVotes[i] > tally.forVotes[winnerIdx]) {
                        winnerIdx = i;
                        tie = false;
                    } else if (tally.forVotes[i] == tally.forVotes[winnerIdx]) tie = true;
                }
            }
            final String def = readings.get(0);
            final String winner = readings.get(winnerIdx);
            final double share = totalFor == 0 ? 0 : (double) tally.forVotes[winnerIdx] / totalFor;
            final String decision;
            if (winnerIdx == 0) decision = "KEEP-DEFAULT (default won)";
            else if (tie) decision = "NO-OVERRIDE (tied winners)";
            else if (totalFor < MIN_FOR_VOTES) decision = "NO-OVERRIDE (only " + totalFor + " FOR votes)";
            else if (share < MIN_WINNER_SHARE) decision = String.format("NO-OVERRIDE (winner share %.0f%% < 75%%)", share * 100);
            else {
                decision = "OVERRIDE -> " + winner;
                overrides.put(c, winner);
            }
            final StringBuilder sb = new StringBuilder();
            sb.append("round ").append(round).append(" char ").append(c).append(" default=").append(def);
            for (int i = 0; i < readings.size(); i++)
                sb.append(" | ").append(readings.get(i)).append(": FOR=").append(tally.forVotes[i]).append(" AGAINST=").append(tally.againstVotes[i]);
            sb.append(" | ").append(decision);
            statsLines.add(sb.toString());
        }
    }

    private static void writeOverrideFile(final Path path, final Map<Character, String> overrides) throws IOException {
        Files.createDirectories(path.getParent());
        try (final PrintWriter out = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            out.println("# polyphone_overrides.txt -- corpus-derived polyphone pinyin reading overrides (TODO.md item 11).");
            out.println("# Trained on Chinese_Names_Corpus.txt by PolyphoneOverrideTrainer: for each polyphone character,");
            out.println("# every candidate pinyin4j reading was voted on for ordering consistency against the corpus's own");
            out.println("# pre-sorted adjacent-pair order; an entry appears here only where a non-default reading won");
            out.println("# decisively (>=75% of FOR votes over >=10 discriminating pairs). The table therefore encodes THIS");
            out.println("# corpus curator's reading conventions (predominantly Chinese surname readings), not universal truth.");
            out.println("# Format: <character><TAB><raw pinyin4j reading, e.g. shan4 or lu:3>. Lines starting with '#' are comments.");
            overrides.forEach((c, r) -> out.println(c + "\t" + r));
        }
    }

    /** Per-character vote tally, indexed parallel to the candidate readings list. */
    private static final class Tally {
        final long[] forVotes;
        final long[] againstVotes;

        Tally(final List<String> readings) {
            forVotes = new long[readings.size()];
            againstVotes = new long[readings.size()];
        }
    }

    private PolyphoneOverrideTrainer() {
    }
}
