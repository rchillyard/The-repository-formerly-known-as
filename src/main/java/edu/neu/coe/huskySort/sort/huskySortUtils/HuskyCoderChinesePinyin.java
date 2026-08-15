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
        else if (dialect.equalsIgnoreCase("BoPoMoFo")) return encodeBoPoMoFo(s);
        else throw new SortException("huskyEncode: unsupported dialect: " + dialect);
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
        final String alt = altOf(c);
        final int space = alt.indexOf(' ');
        final String syllable = space >= 0 ? alt.substring(0, space) : alt;
        final String toneText = space >= 0 ? alt.substring(space + 1) : "";
        final int ordinal = HanyuPinyinSyllables.ordinalOf(syllable);
        final long syllableValue = ordinal >= 0 ? ordinal + 1L : 0L;
        final long toneValue = toneOf(toneText);
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
        for (int i = 0; i < n; i++) {
            final String alt = altOf(s.charAt(i));
            final int space = alt.indexOf(' ');
            final String syllable = space >= 0 ? alt.substring(0, space) : alt;
            final String toneText = space >= 0 ? alt.substring(space + 1) : "";
            final int ordinal = HanyuPinyinSyllables.ordinalOf(syllable);
            final long syllableValue = ordinal >= 0 ? ordinal + 1L : 0L;
            final long toneValue = toneOf(toneText);
            final long charValue = (syllableValue << BITS_PER_TONE) | toneValue;
            result = (result << BITS_PER_CHARACTER) | charValue;
        }
        result <<= (long) BITS_PER_CHARACTER * (MAX_CHARACTERS - n);
        return result;
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
     * Package-private hook for {@code PolyphoneOverrideTrainer} and tests (TODO.md item 11):
     * the alt() memoization above is static, so anything that changes what
     * {@code ChineseCharacter.alt()} returns for a character (namely, installing or clearing
     * the polyphone override table) must also invalidate this cache -- a "fresh coder
     * instance" is not enough. Never called on any production path.
     */
    static void clearAltCache() {
        ALT_CACHE.clear();
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
    private static final int BITS_PER_TONE = 3;
    private static final int BITS_PER_CHARACTER = BITS_PER_SYLLABLE + BITS_PER_TONE;
    private static final int MAX_CHARACTERS = 64 / BITS_PER_CHARACTER;

    private final String dialect;
}
