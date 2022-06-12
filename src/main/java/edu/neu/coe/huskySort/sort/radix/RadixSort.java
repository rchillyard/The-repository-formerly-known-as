package edu.neu.coe.huskySort.sort.radix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

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
     * @param incrementP    ?
     * @param bucketFunction a function which takes a T, an int and yields a bucket index.
     */
    public RadixSort(final int r, final Function<Integer, Integer> incrementP, final BiFunction<T, Integer, Integer> bucketFunction) {
        this.r = r;
        this.incrementP = incrementP;
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
        final double x = Math.PI;
        boolean maxIndex = false;
        int digitIndex = 0;
        while (!maxIndex) {
            maxIndex = true;
            for (final T t : result) {
                final int b = bucketFunction.apply(t, digitIndex);
                bucket[b % r].add(t);
                if (maxIndex && b >= 0) maxIndex = false;
            }
            int a = 0;
            for (int b = 0; b < r; b++) {
                for (final T t : bucket[b]) result[a++] = t;
                bucket[b].clear();
            }
            digitIndex = incrementP.apply(digitIndex);
        }
        return result;
    }

    public static int getHexBucket(final String x, final int p) {
        return Integer.parseInt(x, 16) / p;
    }

    public static int getEnglishBucket(final String x, final int p) {
        return x.toCharArray()[p] >> 6;
    }

    private final int r;
    private final Function<Integer, Integer> incrementP;
    private final BiFunction<T, Integer, Integer> bucketFunction;
}

