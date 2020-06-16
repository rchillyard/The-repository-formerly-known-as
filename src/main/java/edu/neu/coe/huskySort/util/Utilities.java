package edu.neu.coe.huskySort.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

public class Utilities {
    /**
     * There is really no better way that I could find to do this with library/language methods.
     * Don't try to inline this if the generic type extends something like Comparable, or you will get a ClassCastException.
     *
     * @param ts  a collection of Ts.
     * @param <T> the underlying type of ts.
     * @return an array T[].
     */
    public static <T> T[] asArray(Collection<T> ts) {
        if (ts.isEmpty()) throw new RuntimeException("ts may not be empty");
        @SuppressWarnings("unchecked") T[] result = (T[]) Array.newInstance(ts.iterator().next().getClass(), 0);
        return ts.toArray(result);
    }

    /**
     * Create a string representing an integer, with commas to separate thousands.
     *
     * @param x the integer.
     * @return a String representing the number with commas.
     */
    public static String formatWhole(int x) {
        return String.format("%,d", x);
    }

    static String asInt(double x) {
        final int i = round(x);
        return formatWhole(i);
    }

    public static int round(double x) {
        return (int) (Math.round(x));
    }

    public static <T> T[] fillRandomArray(Class<T> clazz, Random random, int n, Function<Random, T> f) {
        @SuppressWarnings("unchecked") T[] result = (T[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) result[i] = f.apply(random);
        return result;
    }
}
