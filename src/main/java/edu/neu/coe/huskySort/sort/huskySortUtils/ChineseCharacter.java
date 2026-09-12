package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.SortException;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Concrete implementation of UnicodeCharacter for Chinese characters.
 */
public class ChineseCharacter extends UnicodeCharacter {

    /**
     * Take the pinyin representation of a character and yield its husky code (614 bits).
     *
     * @return a long.
     */
    public long encode() {
        return pinyinCoder.huskyEncode(alt); // Note this assumes that alt is ready before encode() is called.
    }

    /**
     * Take the unicode representation of a character and yield its pinyin.
     * <p>
     * NOTE: if a character generates more than one pinyin string, the corpus-trained polyphone
     * override table (see {@code polyphone_overrides.txt} and TODO.md item 11) is consulted
     * first; if it selects one of the character's valid readings, that reading is used,
     * otherwise only the first will be used (pinyin4j's default), as before.
     *
     * @return the String from pinyin4j.
     */
    public String alt() {
        final String[] pinyinStrings = PinyinHelper.toHanyuPinyinStringArray(unicode);
        if (pinyinStrings == null)
            return unicode + "";
        else if (pinyinStrings.length > 0) {
            // Polyphone override lookup (TODO.md item 11): the override table was trained on
            // Chinese_Names_Corpus.txt by PolyphoneOverrideTrainer, which voted candidate
            // readings against the corpus's own (pre-sorted) adjacent-pair order. An override
            // is honored only if it exactly matches one of pinyin4j's valid raw readings for
            // this character; otherwise -- and whenever the table is absent or has no entry
            // for this character -- we fall back to pinyinStrings[0] exactly as before.
            String chosen = pinyinStrings[0];
            final String override = POLYPHONE_OVERRIDES.get(unicode);
            if (override != null)
                for (final String candidate : pinyinStrings)
                    if (override.equals(candidate)) {
                        chosen = candidate;
                        break;
                    }
            return toAltFormat(chosen);
        } else throw new RuntimeException("no pinyin available for: " + unicode);
    }

    /**
     * Post-process one raw pinyin4j reading (e.g. "lu:3") into alt() format ("lü 3"): colon
     * replaced, tone split off, u-umlaut spelled properly. Extracted from alt() (unchanged
     * logic) so that PolyphoneOverrideTrainer can convert *candidate* raw readings with
     * exactly the same rules alt() applies to the chosen one.
     *
     * @param rawPinyin a raw reading as returned by PinyinHelper.toHanyuPinyinStringArray.
     * @return the alt()-format rendering, "&lt;syllable&gt; &lt;tone&gt;".
     */
    static String toAltFormat(final String rawPinyin) {
        final String pinyin = ColonPattern.matcher(rawPinyin).replaceAll("~");
        // NOTE: not everything has a tone and I think we need to do this more carefully.
        // However, this is the code in a (private) method in the library.
        final String tone = pinyin.substring(pinyin.length() - 1);
        // Bug fix, 2026-08-05: pinyin4j represents u-umlaut as "u:" (colon), which the line
        // above turns into "u~" -- but this method used to leave it as "u~" rather than
        // converting on to the actual "ü" character, unlike parsePinyin's BoPoMoFo path
        // (see UTildePattern), which already does this conversion correctly. That meant
        // every lu:/nu:-syllable character (e.g. 吕, 律, 女) produced a syllable string that
        // could never match HanyuPinyinSyllables' correctly-spelled "lü"/"nü" entries.
        // Applied to just the syllable portion (after tone has already been split off, by
        // position, so this length-changing substitution can't disturb that).
        final String py = UTildePattern.matcher(pinyin.substring(0, pinyin.length() - 1)).replaceAll("ü");
        return py + " " + tone;
    }

    /**
     * Constructor which takes a (unicode) char as input.
     *
     * @param unicode the unicode character.
     */
    public ChineseCharacter(final char unicode) {
        super(unicode);
    }

    @Override
    public String toString() {
        return "ChineseCharacter{" + unicode + "(" + Long.toHexString(unicode) + "):" + alt + ":" + Long.toHexString(longCode) + "}";
    }

    public static String convertToPinyin(final String s) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) result.append(new ChineseCharacter(s.charAt(i)).alt);
        return result.toString();
    }

    /**
     * Determine the number of bits required to encode the property.
     *
     * @param property the sequence number of the property to be encoded.
     * @return the number of bits required for the property.
     */
    public static int getShift(int property) {
        return switch (property) {
            case 0 -> 5; // initials
            case 1 -> 3; // medials
            case 2 -> 5; // finals
            case 3 -> 3; // tone
            default -> -1;
        };
    }

    /**
     * Determine the (ordered) data value for the String s in the property.
     *
     * @param property the sequence number of the property to be encoded.
     * @param s        the value of the property.
     * @return the ordinal value of s for the property. Lower numbers appear first in the order.
     */
    public static long lookupPinyin(int property, String s) {
        if (s == null) return 0L;
        else return switch (property) {
            case 0 -> Arrays.binarySearch(InitialsOrdered, s) + 1;
            case 1 -> Arrays.binarySearch(MedialsOrdered, s) + 1;
            case 2 -> Arrays.binarySearch(FinalsOrdered, s) + 1;
            case 3 -> Integer.parseInt(s);
            default -> throw new SortException("lookupPinyin: property is invalid");
        };
    }

    /**
     * Method to parse a String of pinyin characters into a parsed string.
     *
     * @param s a String consisting of any number of pinyin character representations of the form initial final tone
     *          (with an optional space before the tone).
     * @param n the number of unicode characters in the original String from which the pinyin was derived.
     * @return an array of String, each element for the form "initial-final-tone"
     */
    public static String[] parsePinyin(final String s, final int n) {
        return parsePinyin(String.class, s, n, ChineseCharacter::toTokens);
    }

    /**
     * Generic method to parse a String of pinyin characters.
     *
     * @param s        a String consisting of any number of pinyin character representations of the form initial final tone
     *                 (with an optional space before the tone).
     * @param n        the number of unicode characters in the original String from which the pinyin was derived.
     * @param function a function which takes an array of four token strings and returns a result of type X.
     * @return an array of String, each element for the form "initial-final-tone"
     */
    public static <X> X[] parsePinyin(Class<X> clazz, final String s, final int n, Function<String[], X> function) {
        final ArrayList<X> results = new ArrayList<>();
        final Matcher matcher = PinyinPattern.matcher(UTildePattern.matcher(s).replaceAll("ü"));
        while (matcher.find()) {
            final int count = matcher.groupCount();
            assert count == 5 : "parsePinyin: Logic error: count=" + count;
            final String initial = matcher.group(2);
            final String medial = matcher.group(3);
            final String finall = matcher.group(4);
            final String tone = matcher.group(5);
            // XXX not sure why we need to check this, but we do.
            if (initial != null || medial != null || finall != null || !tone.isEmpty())
                results.add(function.apply(new String[]{initial, medial, finall, tone}));
        }
        if (results.size() != n)
            throw new SortException("parsePinyin: failed to parse " + n + " strings from '" + s + "'");
        @SuppressWarnings("unchecked") final X[] ts = (X[]) Array.newInstance(clazz, n);

        return results.toArray(ts);
    }

    public static String toTokens(String[] strings) {
        return (strings[0] != null ? strings[0] : "") + "-" + (strings[1] != null ? strings[1] : "") + "-" + (strings[2] != null ? strings[2] : "") + "-" + (strings[3] != null ? strings[3] : "");
    }

    private final static HuskyCoder<String> pinyinCoder = HuskyCoderFactory.englishCoder;

    /**
     * Name of the classpath resource holding the corpus-trained polyphone override table
     * (TODO.md item 11). Format: UTF-8 text; '#' lines are comments; data lines are
     * "&lt;character&gt;&lt;TAB&gt;&lt;raw pinyin4j reading&gt;" (e.g. "什\tshi2").
     */
    static final String POLYPHONE_OVERRIDES_RESOURCE = "polyphone_overrides.txt";

    /**
     * Corpus-trained polyphone override table: for a polyphone character, the raw pinyin4j
     * reading that PolyphoneOverrideTrainer found to best match Chinese_Names_Corpus.txt's
     * own ordering (see TODO.md item 11), consulted by alt() before falling back to
     * pinyinStrings[0]. Loaded once; a missing or unreadable resource yields an empty map,
     * in which case alt() behaves exactly as it did before overrides existed. ConcurrentHashMap
     * because alt() may be called from parallel sorters (same reason as
     * HuskyCoderChinesePinyin's ALT_CACHE); the only mutation after class-load is via the
     * single-threaded package-private training/test hook below.
     */
    private static final Map<Character, String> POLYPHONE_OVERRIDES = new ConcurrentHashMap<>(loadPolyphoneOverrides());

    /**
     * Load the polyphone override table from the classpath. Package-private so tests can
     * verify the loader against the shipped resource independently of the live map.
     * <p>
     * This must never throw: any problem (absent resource, bad encoding, malformed line)
     * degrades gracefully to "no override", i.e. alt()'s historical pinyinStrings[0] behavior.
     *
     * @return the parsed table; empty if the resource is missing or unreadable.
     */
    static Map<Character, String> loadPolyphoneOverrides() {
        final Map<Character, String> map = new HashMap<>();
        try (final InputStream is = ChineseCharacter.class.getClassLoader().getResourceAsStream(POLYPHONE_OVERRIDES_RESOURCE)) {
            if (is == null) return map;
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                    final int tab = trimmed.indexOf('\t');
                    // Malformed lines (no tab, multi-char key, empty reading) are skipped, not fatal.
                    if (tab != 1 || tab + 1 >= trimmed.length()) continue;
                    map.put(trimmed.charAt(0), trimmed.substring(tab + 1).trim());
                }
            }
        } catch (final Exception e) {
            // Deliberately swallowed (see javadoc): an override table must never be able to
            // break basic pinyin lookup.
        }
        return map;
    }

    /**
     * Package-private training/test hook (TODO.md item 11): replace the active override table.
     * PolyphoneOverrideTrainer needs an honest BEFORE measurement (empty table -- regardless of
     * whether a previously-trained resource is already on the classpath) and an honest AFTER
     * measurement (the freshly-trained table) within one JVM. Callers must also invalidate
     * HuskyCoderChinesePinyin's alt() memoization (clearAltCache), which is likewise static.
     * Never called on any production path.
     *
     * @param overrides the table to install (copied into the live map).
     */
    static void setPolyphoneOverrides(final Map<Character, String> overrides) {
        POLYPHONE_OVERRIDES.clear();
        POLYPHONE_OVERRIDES.putAll(overrides);
    }

    public static final String Initials = "(b|p|m|f|d|t|n|l|g|k|h|j|q|x|zh|ch|sh|r|z|c|s|y|w)";
    public static final String Medials = "(y|i|w|u|ü|yu|yü)";

    public static final String Finals = "(ong|eng|ang|ei|ai|ou|ao|en|an|in|un|ng|n|er|e|o|a|i|u)";
    public static final String[] InitialsOrdered = new String[]{"b", "c", "ch", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "sh", "t", "w", "x", "y", "z", "zh"};
    public static final String[] MedialsOrdered = new String[]{"i", "u", "ü", "w", "y", "yu", "yü"};
    public static final String[] FinalsOrdered = new String[]{"a", "ai", "an", "ang", "ao", "e", "ei", "en", "eng", "er", "i", "in", "n", "ng", "o", "ong", "ou", "u", "un"};
    private static final Pattern ColonPattern = Pattern.compile(":");
    public static final String PinyinRegex = "(" + Initials + "?" + Medials + "?" + Finals + "?" + "\\s?(\\d?))";
    public static final Pattern PinyinPattern = Pattern.compile(PinyinRegex);
    public static final Pattern UTildePattern = Pattern.compile("u~");
}
