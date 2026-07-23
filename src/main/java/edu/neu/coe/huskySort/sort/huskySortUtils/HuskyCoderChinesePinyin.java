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
 * The Hanyu encoding packs each character's pinyin syllable as a compact ordinal (see
 * {@link HanyuPinyinSyllables}, an alphabet of 395 standard syllables) rather than spelling
 * the syllable out as ASCII text -- 9 bits per syllable (2^9 = 512 covers the alphabet, with
 * room to spare) instead of roughly 6 bits per *letter* of the spelled-out romanization. That
 * fits up to 7 characters per 64-bit long, versus 4 for the old spell-it-out approach.
 * <p>
 * Tone is deliberately not encoded: interleaving it would need 12 bits/character (9 for the
 * syllable, 3 for tone 1-5), dropping capacity to 5 characters with no margin. Since two
 * different names can share the same syllable spellings but differ only in tone, this
 * encoding can never be claimed perfect regardless of length -- see {@link #perfect()} -- and
 * the cleanup pass (via {@link #getCollator()}) is relied on to fix any such collisions
 * correctly, consistent with Husky Sort's general "mostly right first pass, corrective second
 * pass" design.
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
     * This encoding never claims to be perfect (see class javadoc: tone is deliberately
     * excluded, so same-spelling-different-tone collisions are always possible regardless of
     * length). The cleanup pass, using {@link #getCollator()}, is relied on for correctness.
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

    private static int compareCharacter(final char x, final char y) {
        if (x == y) return 0;
        final String altX = altOf(x);
        final String altY = altOf(y);
        final int spaceX = altX.indexOf(' ');
        final int spaceY = altY.indexOf(' ');
        final String syllableX = spaceX >= 0 ? altX.substring(0, spaceX) : altX;
        final String syllableY = spaceY >= 0 ? altY.substring(0, spaceY) : altY;
        final int cf = HanyuPinyinSyllables.ORDER.compare(syllableX, syllableY);
        if (cf != 0) return cf;
        final String toneX = spaceX >= 0 ? altX.substring(spaceX + 1) : "";
        final String toneY = spaceY >= 0 ? altY.substring(spaceY + 1) : "";
        final int tf = toneX.compareTo(toneY);
        if (tf != 0) return tf;
        return Character.compare(x, y);
    }

    /**
     * Pack up to the first 7 characters of s into a long, 9 bits per character (most
     * significant character first): each character's pinyin syllable is looked up via
     * {@link HanyuPinyinSyllables#ordinalOf(String)} and biased by 1 (0 is reserved for "no
     * recognized pinyin syllable", e.g. a non-Chinese character). Characters beyond the 7th
     * are simply not encoded (not dropped from the payload -- only from the long code); any
     * resulting collision is corrected by the cleanup pass.
     *
     * @param s the String to encode.
     * @return a long encoding of (up to) the first 7 characters of s.
     */
    private static long encodeHanyuOrdinal(final String s) {
        long result = 0L;
        final int n = Math.min(s.length(), MAX_SYLLABLES);
        for (int i = 0; i < n; i++) {
            final int ordinal = HanyuPinyinSyllables.ordinalOf(syllableOf(s.charAt(i)));
            final long value = ordinal >= 0 ? ordinal + 1L : 0L;
            result = (result << BITS_PER_SYLLABLE) | value;
        }
        result <<= (long) BITS_PER_SYLLABLE * (MAX_SYLLABLES - n);
        return result;
    }

    private static String syllableOf(final char c) {
        final String alt = altOf(c);
        final int space = alt.indexOf(' ');
        return space >= 0 ? alt.substring(0, space) : alt;
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
     * comparator ({@code Arrays.sort(xs, collator)} in PureHuskySort et al., which only calls
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
    private static final int MAX_SYLLABLES = 64 / BITS_PER_SYLLABLE;

    private final String dialect;
}
