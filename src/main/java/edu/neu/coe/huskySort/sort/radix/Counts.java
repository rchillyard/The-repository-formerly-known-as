package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class to maintain a set of character counts for MSD radix sort.
 * <p>
 * NOTE: At present this is not generic: and is specific to Unicode Characters.
 */
public class Counts {

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the UnicodeCharacter whose value we want.
     * @throws ClassCastException   if the specified key cannot be compared
     *                              with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *                              and this map uses natural ordering, or its comparator
     *                              does not permit null keys
     */
    public int get(final UnicodeCharacter key) {
        return counts.getOrDefault(key, 0);
    }

    /**
     * Increments the value with the specified key in this map.
     *
     * @param key   key whose value is to be incremented.
     * @throws ClassCastException   if the specified key cannot be compared
     *                              with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *                              and this map uses natural ordering, or its comparator
     *                              does not permit null keys
     */
    public void increment(final UnicodeCharacter key) {
        final Integer i = counts.computeIfAbsent(key, k -> 0);
        counts.put(key, i + 1);
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     */
    public Set<UnicodeCharacter> keySet() {
        return counts.keySet();
    }

    /**
     * Method to count characters.
     *
     * @param xs   an array of UnicodeString objects.
     * @param from the starting index of xs.
     * @param to   the ending index of xs (first element NOT to be counted).
     * @param d    the offset into the UnicodeStrings specifying which character position is to be counted.
     */
    public void countCharacters(final CharacterMap.UnicodeString[] xs, final int from, final int to, final int d) {
        for (int i = from; i < to; i++) increment(xs[i].charAt(d));
    }

    /**
     * Method to accumulate the character counts.
     * Called after countCharacters has been completed.
     *
     * @return an array of UnicodeCharacters in order.
     */
    public UnicodeCharacter[] accumulateCounts() {
        final Set<UnicodeCharacter> keySet = keySet();
        int total = 0;
        for (final UnicodeCharacter key : keySet) {
            final int count = get(key);
            counts.put(key, total);
            total = count + total;
        }
        return keySet.toArray(new UnicodeCharacter[0]);
    }

    /**
     * Method to copy characters from xs to aux.
     * NOTE: both aux and this will be mutated by this method.
     *
     * @param xs  the UnicodeString to be copied into aux.
     * @param aux the auxiliary storage of UnicodeString elements.
     * @param d   the offset into xs.
     */
    void copyAndIncrementCount(final CharacterMap.UnicodeString xs, final CharacterMap.UnicodeString[] aux, final int d) {
        final UnicodeCharacter x = xs.charAt(d);
        aux[get(x)] = xs;
        increment(x);
    }

    @Override
    public String toString() {
        return "Counts{" +
                "counts=" + counts +
                '}';
    }

    public Counts() {
    }

    final Map<UnicodeCharacter, Integer> counts = new TreeMap<>();
}
