package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class CharacterMap {

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
