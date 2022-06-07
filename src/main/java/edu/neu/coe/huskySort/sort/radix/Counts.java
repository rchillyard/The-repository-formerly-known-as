package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

    public void countCharacters(final CharacterMap.UnicodeString[] xs, final int d) {
        for (final CharacterMap.UnicodeString x : xs) increment(x.charAt(d));
    }

    public UnicodeCharacter[] accumulateCounts() {
        final Set<UnicodeCharacter> keySet = keySet();
        int total = 0;
        for (final UnicodeCharacter key : keySet) {
            final int cumulativeCount = get(key) + total;
            counts.put(key, cumulativeCount);
            total = cumulativeCount;
        }
        return keySet.toArray(new UnicodeCharacter[0]);
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
