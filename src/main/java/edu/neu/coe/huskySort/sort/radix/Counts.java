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
    public int getWithIncrement(final UnicodeCharacter key) {
        int result = get(key);
        increment(key);
        return result;
    }

    /**
     * Increments the value with the specified key in this map.
     *
     * @param key key whose value is to be incremented.
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
    public void countCharacters(final UnicodeString[] xs, final int from, final int to, final int d) {
        assert from <= to : "from " + from + "is larger than to " + to;
        assert from >= 0 : "from is negative " + from;
        assert to <= xs.length : "to is too large: " + to;
        for (int i = from; i < to; i++) increment(xs[i].charAt(d));
    }

    /**
     * Method to accumulate the character counts.
     * Called after countCharacters has been completed.
     *
     * @param n the number of counts -- used only for checking.
     * @return an array of UnicodeCharacters in order.
     */
    public UnicodeCharacter[] accumulateCounts(int n) {
        final Set<UnicodeCharacter> keySet = keySet();
        int total = 0;
        for (final UnicodeCharacter key : keySet) {
            final int count = get(key);
            counts.put(key, total);
            total = count + total;
        }
        assert n == total : "total accumulated doesn't match n";
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
    void copyAndIncrementCount(final UnicodeString xs, final UnicodeString[] aux, final int d) {
        final UnicodeCharacter x = xs.charAt(d);
        aux[getWithIncrement(x)] = xs;
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
