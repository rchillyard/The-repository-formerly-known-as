package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Class to model the mapping of Unicode characters to a (long) proxy value which can be used for sorting.
 * There is also an intermediate mapping possible to a form such as Pinyin for Chinese characters.
 */
public class CharacterMap {

    /**
     * A Comparator of String that can be used.
     * <p>
     * NOTE: this method works for Hanyu but not necessarily for other dialects such as bopomofo.
     * <p>
     * NOTE: currently only used by test code and for checking sorts.
     */
    public final Comparator<String> stringComparatorPinyin = (o1, o2) -> {
        final UnicodeString unicodeString1 = getUnicodeString(o1);
        final UnicodeString unicodeString2 = getUnicodeString(o2);
        int d = 0;
        while (unicodeString1.valid(d) && unicodeString2.valid(d)) {
            int cf = unicodeString1.unicodes[d].alt().compareTo(unicodeString2.unicodes[d].alt());
            d++;
            if (cf != 0) return cf;
        }
        if (unicodeString1.valid(d)) return 1;
        else if (unicodeString2.valid(d)) return -1;
        else return 0;
    };

    /**
     * A Comparator of String that can be used.
     * <p>
     * NOTE: currently only used by test code and for checking sorts.
     */
    public final Comparator<String> stringComparator = (o1, o2) -> compareUnicodeStrings(getUnicodeString(o1), getUnicodeString(o2));

    public static int compareUnicodeStrings(final UnicodeString s1, final UnicodeString s2) {
        return compareUnicodeStringsFromD(s1, s2, 0);
    }

    public static int compareUnicodeStringsFromD(final UnicodeString s1, final UnicodeString s2, int d) {
        while (s1.valid(d) || s2.valid(d)) {
            final int cf = s1.compareTo(s2, d++); // NOTE: comparison according to long code.
            if (cf != 0) return cf;
        }
        return 0;
    }

    /**
     * Construct a UnicodeString for the given String s.
     *
     * @param s a String made up of unicode characters.
     * @return an instance of UnicodeString.
     */
    public UnicodeString getUnicodeString(final String s) {
        return new UnicodeString(this, s);
    }

    /**
     * Returns the value to which the specified char is mapped,
     * or {@code null} if this map contains no mapping for x.
     *
     * @param x a character.
     */
    public UnicodeCharacter get(final char x) {
        UnicodeCharacter unicodeCharacter = characters.get(x);
        if (unicodeCharacter == null) {
            unicodeCharacter = toUnicodeCharacter.apply(x);
            put(x, unicodeCharacter);
        }
        return unicodeCharacter;
    }

    /**
     * Method to get the dialect for this CharacterMap.
     * For example, for Chinese, we might specify "Hanyu" or "bopomofo."
     *
     * @return a String representing the dialect.
     */
    public String getDialect() {
        return dialect;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     */
    public Set<Character> keySet() {
        return characters.keySet();
    }

    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return characters.size();
    }

    /**
     * Constructor with specified initialValue.
     *
     * @param toUnicodeCharacter a function which turns a Character into a UnicodeCharacter.
     * @param dialect            the dialect of Pinyin to be used, for example Hanyu.
     * @param initialValue       a value which, if present, will be added to the characters immediately.
     */
    public CharacterMap(final Function<Character, UnicodeCharacter> toUnicodeCharacter, final String dialect, final Character initialValue) {
        this.toUnicodeCharacter = toUnicodeCharacter;
        this.dialect = dialect;
        if (initialValue != null) get(initialValue);
    }

    /**
     * Constructor without a specified initialValue.
     *
     * @param toUnicodeCharacter a function which turns a Character into a UnicodeCharacter.
     */
    public CharacterMap(final Function<Character, UnicodeCharacter> toUnicodeCharacter) {
        this(toUnicodeCharacter, "Hanyu", null);
    }

    /**
     * Removes all the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *                                       is not supported by this map
     */
    public void clear() {
        characters.clear();
    }

    private final Map<Character, UnicodeCharacter> characters = new HashMap<>();

    private void put(final char x, final UnicodeCharacter value) {
        characters.put(x, value);
    }

    private final Function<Character, UnicodeCharacter> toUnicodeCharacter;

    private final String dialect;
}
