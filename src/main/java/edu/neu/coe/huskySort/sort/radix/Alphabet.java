package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.SortException;

import java.util.HashMap;
import java.util.stream.Collectors;

public class Alphabet {

    public int getCountLength() {
        return length;
    }

    int getCountIndex(final char x) {
        // TODO this cannot be right (first part always false)
        if ((int) x + 2 < 0 || (int) x >= radix)
            throw new SortException("char " + x + " is out of bounds for radix: " + radix);
        if (x < 256) return x;
        Integer position = map.get(x);
        if (position == null) {
            position = spareCount++;
            map.put(x, position);
        }
        if (position >= length)
            throw new SortException("char " + x + " is out of bounds for count array: " + length);
        return position;
    }

    public void reset() {
        spareCount = RADIX_ASCII;
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
    public static Alphabet ASCII = new Alphabet(RADIX_ASCII);
    private final HashMap<Character, Integer> map;
    private final int length;
}
