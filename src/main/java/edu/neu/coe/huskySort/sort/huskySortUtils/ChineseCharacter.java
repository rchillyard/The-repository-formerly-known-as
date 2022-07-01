package edu.neu.coe.huskySort.sort.huskySortUtils;

import edu.neu.coe.huskySort.sort.SortException;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
     * NOTE: if a character generates more than one pinyin string, only the first will be used.
     *
     * @return the String from pinyin4j.
     */
    public String alt() {
        final String[] pinyinStrings = PinyinHelper.toHanyuPinyinStringArray(unicode);
        if (pinyinStrings == null)
            return unicode + "";
        else if (pinyinStrings.length > 0) {
            final String pinyin = ColonPattern.matcher(pinyinStrings[0]).replaceAll("~");
            // NOTE: not everything has a tone and I think we need to do this more carefully.
            // However, this is the code in a (private) method in the library.
            final String tone = pinyin.substring(pinyin.length() - 1);
            final String py = pinyin.substring(0, pinyin.length() - 1);
            return py + " " + tone;
        } else throw new RuntimeException("no pinyin available for: " + unicode);
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
