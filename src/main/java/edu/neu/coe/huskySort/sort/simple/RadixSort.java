package edu.neu.coe.huskySort.sort.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Class to implement Radix Sort.
 *
 * @param <T> the underlying type to be sorted.
 */
public class RadixSort<T> {
    /**
     * Construct a RadixSort instance.
     *
     * @param r              the number of distinct classes of T (this is also known as the radix).
     * @param bucketFunction a function which takes a T, an int and yields a bucket index.
     */
    public RadixSort(final int r, final BiFunction<T, Integer, Integer> bucketFunction) {
        this.r = r;
        this.bucketFunction = bucketFunction;
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
        int placement = 1;
        while (!maxLength) {
            maxLength = true;
            for (final T t : result) {
                final int b = bucketFunction.apply(t, placement);
                bucket[b % r].add(t);
                if (maxLength && b > 0) maxLength = false;
            }
            int a = 0;
            for (int b = 0; b < r; b++) {
                for (final T t : bucket[b]) result[a++] = t;
                bucket[b].clear();
            }
            placement *= r;
        }
        return result;
    }

    private final int r;
    private final BiFunction<T, Integer, Integer> bucketFunction;
}

