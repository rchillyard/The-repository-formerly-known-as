package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.SortException;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Husky coder for Chinese Strings, ordered by their pinyin romanization.
 * <p>
 * The Hanyu encoding packs each character's pinyin syllable <i>and</i> tone into 12 bits (9
 * for the syllable ordinal -- see {@link HanyuPinyinSyllables}, an alphabet of 413 standard
 * syllables -- plus 3 for tone) rather than spelling the syllable out as ASCII text (roughly 6
 * bits per <i>letter</i> of the romanization, the old approach) or dropping tone entirely (an
 * earlier version of this class, 2026-07-23 through 2026-07-24). That fits 5 characters per
 * 64-bit long (60 of 64 bits used) -- comfortably above the 2-3 characters actually seen in
 * `Chinese_Names_Corpus.txt`, and above the "4 is common, 5 is about the practical maximum"
 * real-world range for Chinese personal names.
 * <p>
 * This is still never claimed perfect (see {@link #perfect()}): two <i>different</i>
 * characters can be true homonyms -- identical syllable <i>and</i> tone, e.g. 郗/奚, both
 * "xi1" -- and no stroke-count data is available to distinguish them (see
 * {@link #NAME_ORDER}), so a residual, much narrower class of collisions remains possible
 * regardless of length. But encoding tone is still a real, deliberate improvement over
 * dropping it: it doesn't reduce how often the cleanup pass runs (it always runs, since
 * {@link #perfect()} is unconditionally false), it reduces how much work that pass actually
 * has to do. Dropping tone left every group of names sharing a syllable (which, for common
 * surnames, can be huge) in essentially arbitrary relative order after the first pass, forcing
 * a real O(k log k) sort within each such group during cleanup; encoding tone means the first
 * pass already gets almost everything right except the rare true-homonym pairs, leaving the
 * cleanup pass (`Arrays.sort`/TimSort, which is adaptive to already-sorted input) very little
 * real work to do.
 */
public class HuskyCoderChinesePinyin implements HuskyCoder<String> {
    /**
     * Encode x as a long.
     * As much as possible, if x > y, huskyEncode(x) > huskyEncode(y).
     * If this cannot be guaranteed, then the result of imperfect(z) will be true.
     *
     * @param s the X value to encode.
     * @return a long which is, as closely as possible, monotonically increasing with the domain of X values.
     */
    public long huskyEncode(final String s) {
        if (dialect.equalsIgnoreCase("Hanyu")) return encodeHanyuOrdinal(s);
        else if (dialect.equalsIgnoreCase("HanyuRank")) return encodeHanyuRank(s);
        else if (dialect.equalsIgnoreCase("BoPoMoFo")) return encodeBoPoMoFo(s);
        else throw new SortException("huskyEncode: unsupported dialect: " + dialect);
    }

    /**
     * For the HanyuRank dialect only, perfection is decided per element rather than declared once,
     * because this encoding really can be exact -- but only for strings that fit its assumptions.
     * A string qualifies when it is at most {@link #MAX_CHARACTERS_RANK} characters long and every
     * one of those characters lies in the CJK block the rank table covers. Both conditions matter:
     * a fifth character is simply not encoded, and a character outside the table takes rank 0,
     * which ties it with padding.
     * <p>
     * Every other dialect keeps the old behaviour of never claiming perfection, so no existing
     * measurement moves.
     *
     * @param xs the elements to encode.
     * @return their codes, with perfect set only if every element qualifies.
     */
    @Override
    public Coding huskyEncode(final String[] xs) {
        if (!rankDialect) return HuskyCoder.super.huskyEncode(xs);
        boolean isPerfect = true;
        final long[] result = new long[xs.length];
        for (int i = 0; i < xs.length; i++) {
            final String x = xs[i];
            if (isPerfect) isPerfect = exactlyEncodable(x);
            result[i] = encodeHanyuRank(x);
        }
        return new Coding(result, isPerfect);
    }

    /**
     * @param s a string.
     * @return true if {@link #encodeHanyuRank} orders s exactly as {@link #NAME_ORDER} would.
     */
    private static boolean exactlyEncodable(final String s) {
        if (s.length() > MAX_CHARACTERS_RANK) return false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c < RANK_LO || c > RANK_HI) return false;
        }
        return true;
    }

    /**
     * This encoding never claims to be perfect (see class javadoc: even with syllable and tone
     * both encoded, two different characters can be true homonyms -- identical syllable and
     * tone, e.g. 郗/奚, both "xi1" -- and no stroke-count data is available to distinguish them,
     * so this residual collision is always possible regardless of length). The cleanup pass,
     * using {@link #getCollator()}, is relied on for correctness.
     *
     * @return false.
     */
    @Override
    public boolean perfect() {
        return false;
    }

    /**
     * @return a Collator which orders Strings correctly by pinyin (character by character,
     * syllable spelling then tone as a per-character tie-break), for use by the cleanup pass.
     */
    @Override
    public Collator getCollator() {
        return PINYIN_COLLATOR;
    }

    public HuskyCoderChinesePinyin(final String dialect) {
        this.dialect = dialect;
        this.rankDialect = dialect.equalsIgnoreCase("HanyuRank");
    }

    /**
     * Comparator for full pinyin-romanized Chinese Strings (names or words), comparing
     * character by character: for each character, compare its pinyin syllable spelling (via
     * {@link HanyuPinyinSyllables#ORDER}), then tone, then (if the two characters are true
     * homonyms -- identical syllable and tone, e.g. 郗/奚, both "xi1") Unicode code point, as
     * successive per-character tie-breaks, before moving on to the next character. This is the
     * "character-by-character" convention (matching <i>Xiandai Hanyu Cidian</i>), as opposed
     * to "word-by-word" (matching the ABC Chinese-English Dictionary, which compares the whole
     * word's spelling before ever considering tone) -- see
     * <a href="https://en.wikipedia.org/wiki/Pinyin_alphabetical_order">Pinyin alphabetical
     * order</a> for the distinction between the two.
     * <p>
     * True homonyms should properly be broken by stroke count, per that same page, but no
     * stroke-count data is available here (nor in the code this replaces). Falling back to
     * Unicode code point rather than leaving the comparator's result as 0 makes the sort
     * result deterministic (not dependent on whatever arbitrary order the unstable first
     * sorting pass happens to leave true homonyms in) -- and it is not a meaningless
     * substitute: the CJK Unified Ideographs block's code point order is itself derived from
     * historical radical/stroke-ordered national encoding standards (GB, Big5, JIS, KSC), so
     * it approximates (without exactly reproducing) genuine stroke-count order.
     */
    public static final Comparator<String> NAME_ORDER = (a, b) -> {
        final int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            final int cf = compareCharacter(a.charAt(i), b.charAt(i));
            if (cf != 0) return cf;
        }
        return Integer.compare(a.length(), b.length());
    };

    /**
     * A single packed key for character c, ordered by pinyin syllable (per
     * {@link HanyuPinyinSyllables#ordinalOf}), then tone, then Unicode code point as a final
     * tie-break for true homonyms -- the same three-level order {@link #NAME_ORDER} uses,
     * packed into a single long so that plain numeric comparison of two characters' keys
     * reproduces exactly the order {@link #NAME_ORDER} would give for those two characters.
     * Exposed for reuse by other sorters -- e.g. a pinyin-aware multikey quicksort, partitioning
     * per character -- that want this same per-character ordering without duplicating the
     * syllable/tone parsing logic.
     * <p>
     * Relies on {@link HanyuPinyinSyllables#ordinalOf}'s numeric order matching
     * {@link HanyuPinyinSyllables#ORDER}'s comparator order, which is how {@link #encodeHanyuOrdinal}
     * already uses it (verified directly against the full 413-syllable table, not merely assumed).
     *
     * @param c the character.
     * @return a packed key; higher means later in pinyin order.
     */
    public static long pinyinCharacterKey(final char c) {
        final long syllableAndTone = syllableAndToneOf(c);
        final long syllableValue = syllableAndTone >>> BITS_PER_TONE;
        final long toneValue = syllableAndTone & ((1L << BITS_PER_TONE) - 1);
        return (syllableValue << (BITS_PER_TONE + Character.SIZE)) | (toneValue << Character.SIZE) | c;
    }

    private static int compareCharacter(final char x, final char y) {
        if (x == y) return 0;
        final int cf = compareAltReadings(altOf(x), altOf(y));
        if (cf != 0) return cf;
        return Character.compare(x, y);
    }

    /**
     * Compare two {@code ChineseCharacter.alt()}-format readings ("&lt;syllable&gt; &lt;tone&gt;")
     * by pinyin syllable spelling (via {@link HanyuPinyinSyllables#ORDER}), then tone -- i.e. the
     * first two levels of the three-level per-character order used by {@code compareCharacter}
     * and {@link #NAME_ORDER}, WITHOUT the final Unicode-code-point tie-break (which is a
     * property of the characters being compared, not of the readings themselves).
     * <p>
     * Package-private for reuse by {@code PolyphoneOverrideTrainer} (TODO.md item 11), which
     * needs to test *candidate* readings of a polyphone character -- not just the character's
     * currently-chosen reading -- for ordering consistency against corpus neighbors; a result
     * of 0 there means "this pair's relative order was decided by the corpus curator's
     * stroke-count tie-break, which readings alone cannot reproduce".
     *
     * @param altX one reading in alt() format, e.g. "lü 3".
     * @param altY another reading in alt() format.
     * @return negative/zero/positive per syllable-then-tone order.
     */
    static int compareAltReadings(final String altX, final String altY) {
        final int spaceX = altX.indexOf(' ');
        final int spaceY = altY.indexOf(' ');
        final String syllableX = spaceX >= 0 ? altX.substring(0, spaceX) : altX;
        final String syllableY = spaceY >= 0 ? altY.substring(0, spaceY) : altY;
        final int cf = HanyuPinyinSyllables.ORDER.compare(syllableX, syllableY);
        if (cf != 0) return cf;
        final String toneX = spaceX >= 0 ? altX.substring(spaceX + 1) : "";
        final String toneY = spaceY >= 0 ? altY.substring(spaceY + 1) : "";
        return toneX.compareTo(toneY);
    }

    /**
     * Pack up to the first 5 characters of s into a long, 12 bits per character (most
     * significant character first): 9 bits for the pinyin syllable ordinal (looked up via
     * {@link HanyuPinyinSyllables#ordinalOf(String)}, biased by 1 -- 0 is reserved for "no
     * recognized pinyin syllable", e.g. a non-Chinese character) followed by 3 bits for tone
     * (0 for "no/unrecognized tone", otherwise the tone digit 1-7 as reported by pinyin4j -- in
     * practice 1-5, the four tones plus neutral). Characters beyond the 5th are simply not
     * encoded (not dropped from the payload -- only from the long code); any resulting
     * collision (including the true-homonym case that persists regardless of length -- see
     * class javadoc) is corrected by the cleanup pass.
     *
     * @param s the String to encode.
     * @return a long encoding of (up to) the first 5 characters of s.
     */
    private static long encodeHanyuOrdinal(final String s) {
        long result = 0L;
        final int n = Math.min(s.length(), MAX_CHARACTERS);
        for (int i = 0; i < n; i++)
            result = (result << BITS_PER_CHARACTER) | syllableAndToneOf(s.charAt(i));
        result <<= (long) BITS_PER_CHARACTER * (MAX_CHARACTERS - n);
        return result;
    }

    /**
     * Pack the first {@value #MAX_CHARACTERS_RANK} characters of s into a long, {@value #BITS_RANK}
     * bits per character, where each character's value is its <i>rank in pinyin order</i> rather
     * than its syllable and tone.
     * <p>
     * <b>Why this exists.</b> {@link #encodeHanyuOrdinal} packs two of the three levels
     * {@link #NAME_ORDER} compares on -- syllable, then tone -- and omits the third, the Unicode
     * code point used to separate true homonyms. That omission, not any shortage of bits, is what
     * leaves the Chinese-names corpus so disordered after the radix phase. Measured over
     * `Chinese_Names_Corpus.txt` (1,145,009 names, 15.6% of two characters and 84.4% of three, so
     * length was never the constraint):
     * <pre>
     *     1,145,009 distinct names -&gt; 818,114 distinct codes, 1.40 names per code
     *     of 162,689 descents left in a shuffled million: 58.6% equal codes (homonyms the
     *     radix cannot order), 41.4% mis-orderings -- and those are the same cause one step
     *     removed, the code tying at character i and falling through to a padding zero at
     *     character i+1 where NAME_ORDER had already decided at i
     * </pre>
     * Ranking fixes both at once, because the rank is derived from {@link #pinyinCharacterKey},
     * which is the very key NAME_ORDER compares on. One number per character, ordered the way the
     * comparator orders characters, is therefore exactly order-preserving by construction -- and it
     * stays so under any future refinement of the comparator (stroke-count data, say, in place of
     * today's code-point tie-break), because re-ranking follows automatically.
     * <p>
     * <b>Why it fits.</b> CJK Unified Ideographs plus Extension A is U+3400..U+9FFF, 27,648 code
     * points, so a rank needs 15 bits and four characters need 60 of the available 64. That covers
     * every name in the corpus with a character to spare. Rank 0 is reserved for absent (padding)
     * and for out-of-block characters, so short names sort before longer ones that extend them.
     * <p>
     * <b>Why it is fast.</b> The table is <i>indexed</i>, not searched: {@code RANK[c - RANK_LO]} is
     * a single load from a 54 KB {@code char[]}, against the memoized pinyin4j lookup --- hash,
     * probe, dereference --- that {@link #syllableAndToneOf} performs per character. Measured on a
     * million names, this encodes in 41 ms against the ordinal encoding's 133, so it is 3.2x
     * <i>faster</i> as well as exact. The table's size costs nothing at steady state: real text
     * draws on a few thousand characters with a Zipfian frequency distribution, so the live working
     * set is a few kilobytes.
     *
     * @param s the String to encode.
     * @return a long encoding of (up to) the first {@value #MAX_CHARACTERS_RANK} characters of s.
     */
    private static long encodeHanyuRank(final String s) {
        final char[] ranks = RankTable.RANKS;
        long result = 0L;
        final int n = Math.min(s.length(), MAX_CHARACTERS_RANK);
        for (int i = 0; i < n; i++) {
            final char c = s.charAt(i);
            result = (result << BITS_RANK) | (c >= RANK_LO && c <= RANK_HI ? ranks[c - RANK_LO] : 0);
        }
        result <<= (long) BITS_RANK * (MAX_CHARACTERS_RANK - n);
        return result;
    }

    /**
     * The rank table, built on first use and never rebuilt. Initialization-on-demand holder, so the
     * cost is paid only by callers that actually use the HanyuRank dialect, and the JVM guarantees
     * the publication without any locking on the hot path.
     * <p>
     * Building it sorts the 27,648 characters of the block by {@link #pinyinCharacterKey} and
     * numbers them from 1, which costs about 340 ms once -- dominated by the pinyin4j lookups,
     * which the cache then holds for the encoder's own use. If that ever becomes awkward it can be
     * precomputed into a resource; at present it is paid once per JVM and is invisible beside a
     * benchmark's setup.
     */
    private static final class RankTable {
        static final char[] RANKS = build();

        private static char[] build() {
            final int span = RANK_HI - RANK_LO + 1;
            final Character[] block = new Character[span];
            for (int i = 0; i < span; i++) block[i] = (char) (RANK_LO + i);
            java.util.Arrays.sort(block, java.util.Comparator.comparingLong(HuskyCoderChinesePinyin::pinyinCharacterKey));
            final char[] ranks = new char[span];
            for (int i = 0; i < span; i++) ranks[block[i] - RANK_LO] = (char) (i + 1);
            return ranks;
        }
    }

    private static String syllableOf(final char c) {
        final String alt = altOf(c);
        final int space = alt.indexOf(' ');
        return space >= 0 ? alt.substring(0, space) : alt;
    }

    /**
     * @param toneText the tone portion of a ChineseCharacter.alt() result (everything after
     *                  the space), expected to be a single digit.
     * @return the tone as a value 0-7 (0 if toneText isn't recognized as a single digit 0-7),
     * fitting in BITS_PER_TONE bits.
     */
    private static long toneOf(final String toneText) {
        if (toneText.length() != 1) return 0L;
        final char c = toneText.charAt(0);
        return (c >= '0' && c <= '7') ? (c - '0') : 0L;
    }

    /**
     * Memoized ChineseCharacter.alt() lookup: computing this involves a pinyin4j lookup plus
     * regex-based parsing, and both the encoder and the (always-invoked, since perfect() is
     * always false -- see class javadoc) cleanup-pass comparator call it once per character
     * per comparison. Real Chinese text draws from a bounded vocabulary of a few thousand
     * characters at most, so a simple unbounded cache is appropriate here (unlike, say,
     * caching arbitrary Strings).
     */
    private static String altOf(final char c) {
        return ALT_CACHE.computeIfAbsent(c, ch -> new ChineseCharacter(ch).alt());
    }

    private static final Map<Character, String> ALT_CACHE = new ConcurrentHashMap<>();

    /**
     * Memoized syllable-and-tone value for a character: the expensive half of both
     * {@link #encodeHanyuOrdinal} and {@link #pinyinCharacterKey}.
     * <p>
     * Deriving it costs two substring allocations, a string-keyed table search and a string parse.
     * The encoder pays that for every character of every element, which is the one place this
     * mechanism cannot afford it: paying the key extraction once per element rather than once per
     * comparison is the whole premise, and an extraction an order of magnitude dearer than a
     * comparison inverts the economics. {@link #altOf} is cached by the same argument, and the
     * vocabulary bound that justifies it applies here identically.
     *
     * @param c a character.
     * @return its syllable ordinal in the high bits, its tone in the low BITS_PER_TONE bits.
     */
    private static long syllableAndToneOf(final char c) {
        return SYLLABLE_TONE_CACHE.computeIfAbsent(c, HuskyCoderChinesePinyin::computeSyllableAndTone);
    }

    private static long computeSyllableAndTone(final char c) {
        final String alt = altOf(c);
        final int space = alt.indexOf(' ');
        final String syllable = space >= 0 ? alt.substring(0, space) : alt;
        final String toneText = space >= 0 ? alt.substring(space + 1) : "";
        final int ordinal = HanyuPinyinSyllables.ordinalOf(syllable);
        final long syllableValue = ordinal >= 0 ? ordinal + 1L : 0L;
        return (syllableValue << BITS_PER_TONE) | toneOf(toneText);
    }

    private static final Map<Character, Long> SYLLABLE_TONE_CACHE = new ConcurrentHashMap<>();

    /**
     * Package-private hook for {@code PolyphoneOverrideTrainer} and tests (TODO.md item 11):
     * the alt() memoization above is static, so anything that changes what
     * {@code ChineseCharacter.alt()} returns for a character (namely, installing or clearing
     * the polyphone override table) must also invalidate this cache -- a "fresh coder
     * instance" is not enough. Never called on any production path.
     */
    static void clearAltCache() {
        ALT_CACHE.clear();
        // NOTE the syllable-and-tone cache is derived from alt(), so it is stale for exactly the
        // same reasons and must be dropped alongside it.
        SYLLABLE_TONE_CACHE.clear();
    }

    private static long encodeBoPoMoFo(final String s) {
        final Long[] codes = ChineseCharacter.parsePinyin(Long.class, ChineseCharacter.convertToPinyin(s), s.length(), xs -> {
            long result = 0L;
            for (int i = 0; i < xs.length; i++) {
                final int shift = ChineseCharacter.getShift(i);
                final long x = ChineseCharacter.lookupPinyin(i, xs[i]);
                assert x >= 0 : "chineseEncoderPinyin: logic error";
                result = (result << shift) | x;
            }
            return result;
        });
        long result = 0L;
        int bits = 0;
        int shift = 16;
        for (final long x : codes) {
            result = (result << shift) | x;
            bits += shift;
            if (bits == 48) shift = 15;
            if (bits >= 63) break;
        }
        // TODO Pad the remaining 47, 31, 15 bits as necessary.
        return result;
    }

    /**
     * Minimal Collator wrapping {@link #NAME_ORDER}, sufficient for use as the second-pass
     * comparator ({@code Arrays.sort(xs, collator)} in QuickHuskySort et al., which only calls
     * {@code compare}).
     */
    private static final class PinyinOrdinalCollator extends Collator {
        @Override
        public int compare(final String source, final String target) {
            return NAME_ORDER.compare(source, target);
        }

        @Override
        public CollationKey getCollationKey(final String source) {
            throw new UnsupportedOperationException("PinyinOrdinalCollator does not support getCollationKey");
        }

        @Override
        public int hashCode() {
            return PinyinOrdinalCollator.class.hashCode();
        }
    }

    private static final Collator PINYIN_COLLATOR = new PinyinOrdinalCollator();
    private static final int BITS_PER_SYLLABLE = 9;
    /**
     * The CJK block the rank table covers: Extension A (U+3400) through the end of Unified
     * Ideographs (U+9FFF), 27,648 code points, which is what makes a 15-bit rank sufficient.
     */
    private static final char RANK_LO = 0x3400;
    private static final char RANK_HI = 0x9FFF;
    private static final int BITS_RANK = 15;
    private static final int MAX_CHARACTERS_RANK = 4;

    private static final int BITS_PER_TONE = 3;
    private static final int BITS_PER_CHARACTER = BITS_PER_SYLLABLE + BITS_PER_TONE;
    private static final int MAX_CHARACTERS = 64 / BITS_PER_CHARACTER;

    private final String dialect;
    private final boolean rankDialect;
}
