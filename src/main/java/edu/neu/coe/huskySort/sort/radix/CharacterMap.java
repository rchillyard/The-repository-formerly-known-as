package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

import java.util.*;
import java.util.function.Function;

/**
 * Class to model the mapping of Unicode characters to a (long) proxy value which can be used for sorting.
 * There is also an intermediate mapping possible to a form such as Pinyin for Chinese characters.
 */
public class CharacterMap {

    /**
     * Inner instance class which represents a string of UnicodeCharacter instances.
     */
    class UnicodeString {
        /**
         * Constructor which takes a String representing a "word" or name.
         * We expect each of the characters of word to be a unicode representation.
         *
         * @param word a sequence of unicode characters.
         */
        public UnicodeString(final String word) {
            this.word = word;
            this.unicodes = new UnicodeCharacter[word.length()];
            for (int i = 0; i < word.length(); i++) unicodes[i] = get(word.charAt(i));
        }

        /**
         * Method to determine if the ith unicode character is valid, i.e. is i < the length of the string.
         *
         * @param i the index of the desired unicode character (equivalent to "d" in UnicodeMSDStringSort).
         * @return true or false.
         */
        public boolean valid(final int i) {
            assert (i >= 0) : "UnicodeString: negative index " + i;
            return i < unicodes.length;
        }

        /**
         * Method to get the character at position i.
         *
         * @param i the index of the desired unicode character (equivalent to "d" in UnicodeMSDStringSort).
         * @return a UnicodeCharacter or (if not a valid character) the Null character.
         */
        public UnicodeCharacter charAt(final int i) {
            if (valid(i)) return unicodes[i];
            return UnicodeCharacter.NullChar;
        }

        /**
         * Method to compare this UnicodeString with other -- at the dth character.
         *
         * @param other another UnicodeString.
         * @param d     the offset of the character in each of the strings.
         * @return negative, zero, or positive according to this less than, = or greater than other.
         */
        public int compare(final UnicodeString other, final int d) {
            return charAt(d).compareTo(other.charAt(d));
        }

        @Override
        public String toString() {
            return "UnicodeString{" +
                    "word='" + word + '\'' +
                    ", unicodes=" + Arrays.toString(unicodes) +
                    '}';
        }

        /**
         * The original representation of the String, before being converted to UnicodeCharacter form.
         */
        final String word;
        private final UnicodeCharacter[] unicodes;
    }

    /**
     * A Comparator of String that can be used.
     * <p>
     * NOTE: currently only used by test code and for checking sorts.
     */
    public final Comparator<String> stringComparator = (o1, o2) -> {
        final CharacterMap.UnicodeString unicodeString1 = getUnicodeString(o1);
        final CharacterMap.UnicodeString unicodeString2 = getUnicodeString(o2);
        int d = 0;
        while (unicodeString1.valid(d) || unicodeString2.valid(d)) {
            int cf = unicodeString1.compare(unicodeString2, d++);
            if (cf != 0) return cf;
        }
        return 0;
    };

    /**
     * Construct a UnicodeString for the given String s.
     *
     * @param s a String made up of unicode characters.
     * @return an instance of UnicodeString.
     */
    public UnicodeString getUnicodeString(final String s) {
        return new UnicodeString(s);
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
     * @param initialValue       a value which, if present, will be added to the characters immediately.
     */
    public CharacterMap(final Function<Character, UnicodeCharacter> toUnicodeCharacter, final Character initialValue) {
        this.toUnicodeCharacter = toUnicodeCharacter;
        if (initialValue != null) get(initialValue);
    }

    /**
     * Constructor without a specified initialValue.
     *
     * @param toUnicodeCharacter a function which turns a Character into a UnicodeCharacter.
     */
    public CharacterMap(final Function<Character, UnicodeCharacter> toUnicodeCharacter) {
        this(toUnicodeCharacter, null);
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
}
