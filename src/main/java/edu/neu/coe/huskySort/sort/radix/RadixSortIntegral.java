package edu.neu.coe.huskySort.sort.radix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to implement Radix Sort.
 *
 * @param <T> the underlying type to be sorted.
 */
public class RadixSortIntegral<T extends Number> {
    /**
     * Construct a RadixSort instance.
     *
     * @param r the number of distinct classes of T (this is also known as the radix).
     */
    public RadixSortIntegral(final int r) {
        this.r = r;
    }

    /**
     * Java method to sort a given array ot Ts using a radix sort algorithm.
     *
     * @param ts an array to Ts to be sorted.
     */
    public T[] sort(final T[] ts) {
        final T[] result = Arrays.copyOf(ts, ts.length);
        @SuppressWarnings("unchecked") final List<T>[] bucket = new ArrayList[r];
        for (int i = 0; i < bucket.length; i++) bucket[i] = new ArrayList<>();
        boolean maxLength = false;
        long divisor = 1L;
        while (!maxLength) {
            maxLength = true;
            for (final T t : result) {
                final long b = t.longValue() / divisor;
                bucket[(int) (b % r)].add(t);
                if (maxLength && b > 0) maxLength = false;
            }
            int a = 0;
            for (int b = 0; b < r; b++) {
                for (final T t : bucket[b]) result[a++] = t;
                bucket[b].clear();
            }
            divisor *= r;
        }
        return result;
    }

    private final int r;
}

