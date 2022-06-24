package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.util.Helper;

import java.lang.reflect.Array;
import java.util.function.Function;

/**
 * Interface to define a Helper which is suitable for a transforming sort (TransformingSort).
 *
 * @param <X> the underlying type of the input to be sorted. NOTE: do we need it to be Comparable here?
 * @param <T> the underlying type of the temporary representation which is what actually gets sorted.
 */
public interface TransformingHelper<X extends Comparable<X>, T> extends Helper<T> {
    /**
     * Method to do any required preProcessing.
     *
     * @param ts the array to be sorted.
     * @return the array after any pre-processing.
     */
    default T[] preProcess(final T[] ts) {
        // CONSIDER invoking init from here.
        // NOTE: not called by UnicodeMSDStringSort
        return ts;
    }

    /**
     * Method to transform an array (xs) of Xs into an array of Ts.
     * This is essentially the inverse of recoverXFromT.
     * <p>
     *
     * @param clazz     the class of T.
     * @param xs        the input array of Xs.
     * @param from      the starting index.
     * @param n         the number of elements to process and return.
     * @param transform a function which takes an X and returns a T.
     * @return an X[] of length n.
     */
    default T[] transformXToT(final Class<T> clazz, final X[] xs, final int from, final int n, final Function<X, T> transform) {
        @SuppressWarnings("unchecked") final T[] ts = (T[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) ts[i] = transform.apply(xs[i + from]);
        return ts;
    }

    /**
     * Method to recover the original X values from a (sorted) array of Ts.
     * This is essentially the inverse of transformXToT.
     * <p>
     *
     * @param ts   the (sorted) array of Ts.
     * @param xs   an array of Xs which will be over-written starting at index from.
     * @param from the starting index.
     * @param n    the number of elements to recover.
     */
    default void recoverXFromT(final T[] ts, final X[] xs, final int from, final int n, final Function<T, X> recover) {
        for (int i = 0; i < n; i++) xs[i + from] = recover.apply(ts[i]);
    }

}
