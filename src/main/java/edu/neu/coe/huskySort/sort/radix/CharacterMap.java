package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class CharacterMap {

    class UnicodeString {
        public UnicodeString(final String word) {
            this.word = word;
            this.unicodes = new UnicodeCharacter[word.length()];
            for (int i = 0; i < word.length(); i++) unicodes[i] = get(word.charAt(i));
        }

        public boolean valid(final int i) {
            assert (i >= 0) : "UnicodeString: negative index " + i;
            return i < unicodes.length;
        }

        public UnicodeCharacter charAt(final int i) {
            if (valid(i)) return unicodes[i];
            return UnicodeCharacter.NullChar;
        }

        public int compare(final UnicodeString other, final int d) {
            return charAt(d).compareTo(other.charAt(d));
        }

        final String word;
        private final UnicodeCharacter[] unicodes;
    }

    /**
     * A Comparator of String that can be used.
     *
     * NOTE: currently only used by test code.
     */
    public final Comparator<String> stringComparator = (o1, o2) -> {
        final CharacterMap.UnicodeString unicodeString1 = new UnicodeString(o1);
        final CharacterMap.UnicodeString unicodeString2 = new UnicodeString(o2);
        return unicodeString1.compare(unicodeString2, 0); // should test for the other positions, too.
    };

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

    public CharacterMap(final Function<Character, UnicodeCharacter> toUnicodeCharacter, final Character initialValue) {
        this.toUnicodeCharacter = toUnicodeCharacter;
        if (initialValue != null) get(initialValue);
    }

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

    final Map<Character, UnicodeCharacter> characters = new HashMap<>();

    private void put(final char x, final UnicodeCharacter value) {
        characters.put(x, value);
    }

    private final Function<Character, UnicodeCharacter> toUnicodeCharacter;
}
