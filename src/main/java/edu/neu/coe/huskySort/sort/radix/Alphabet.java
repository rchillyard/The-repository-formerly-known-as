package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.SortException;

import java.util.HashMap;
import java.util.stream.Collectors;

public class Alphabet {

    public int getCountLength() {
        return length;
    }

    /**
     * The bucket index of x.
     * <p>
     * A radix sort emits buckets in index order, so this mapping must be monotonic in the character:
     * if v &lt; w then the index of v must be less than the index of w. Characters below
     * {@link #RADIX_ASCII} are their own index and so are monotonic for free. The rest are assigned
     * positions in a spare region by {@link #prepare}, in code-point order, which is what makes them
     * monotonic too.
     *
     * @param x a character occurring in the strings being sorted.
     * @return the index of x's bucket.
     */
    int getCountIndex(final char x) {
        if ((int) x >= radix)
            throw new SortException("char " + x + " is out of bounds for radix: " + radix);
        if (x < RADIX_ASCII) return x;
        final Integer position = map.get(x);
        if (position == null)
            throw new SortException("char " + x + " (" + (int) x + ") has no position in this alphabet."
                    + " prepare() must be called with the whole input before sorting, so that characters"
                    + " beyond ASCII can be assigned positions in code-point order.");
        return position;
    }

    /**
     * Assign a bucket position to every character of xs at or above {@link #RADIX_ASCII},
     * in code-point order.
     * <p>
     * This must run over the whole input before any of it is sorted. Assigning positions lazily as
     * characters are encountered — which is what this class used to do — orders the spare region by
     * first encounter rather than by code point, so two strings differing first at a non-ASCII
     * character come out in whichever order the input happened to present those characters in.
     *
     * @param xs the entire array about to be sorted.
     * @throws SortException if xs holds more distinct characters beyond ASCII than there is room for.
     */
    public void prepare(final String[] xs) {
        // NOTE a presence array rather than a sorted set: this runs over every character of every
        // string, and boxing each one into a TreeSet would cost an appreciable fraction of the sort.
        final boolean[] present = new boolean[Character.MAX_VALUE + 1];
        for (final String x : xs)
            for (int i = 0; i < x.length(); i++) {
                final char c = x.charAt(i);
                if (c >= RADIX_ASCII) present[c] = true;
            }
        reset();
        final int capacity = length - RADIX_ASCII - 2;
        for (int c = RADIX_ASCII; c < present.length; c++)
            if (present[c]) {
                if (spareCount - RADIX_ASCII >= capacity)
                    throw new SortException("this alphabet has room for " + capacity
                            + " distinct characters beyond ASCII, and the input holds more."
                            + " It is an extended-ASCII alphabet, not a Unicode one.");
                map.put((char) c, spareCount++);
            }
    }

    /**
     * Discard any assignment made by {@link #prepare}, so that this Alphabet can be used again.
     */
    public void reset() {
        spareCount = RADIX_ASCII;
        map.clear();
    }

    public int counts() {
        return spareCount;
    }

    public Alphabet(final int radix) {
        this.radix = radix;
        this.spare = radix > RADIX_ASCII ? 256 : 0;
        this.length = RADIX_ASCII + spare + 2;
        this.map = new HashMap<>();
    }

    public Alphabet() {
        this(RADIX_ASCII);
    }

    @Override
    public String toString() {
        final String mapAsString = map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        return "Alphabet{" +
                "radix=" + radix +
                ", spare=" + spare +
                ", spareCount=" + spareCount +
                ", map=" + mapAsString +
                ", length=" + length +
                '}';
    }

    final int radix;
    private final int spare;
    int spareCount = RADIX_ASCII;

    public static final int RADIX_ASCII = 256;
    public static final int RADIX_UNICODE = 256 * 256;
    public static final Alphabet ASCII = new Alphabet(RADIX_ASCII);
    private final HashMap<Character, Integer> map;
    private final int length;
}
