package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.GenericSort;

import java.lang.reflect.Array;
import java.util.function.Function;

/**
 * Interface to define a sort method which sorts an array of Xs by temporarily transforming them into an array of Ys.
 *
 * @param <X> the element type of the input/output arrays (must implement Comparable of X.
 * @param <T> the element type of the temporary array.
 */
public interface TransformingSort<X extends Comparable<X>, T> extends GenericSort<X> {

    /**
     * Method to get a suitable Helper for this TransformingSort.
     *
     * @return the Helper.
     */
    TransformingHelper<T> getHelper();


    /**
     * Method to recover the original unicode Strings from an array of Xs.
     * This is essentially the inverse of getStringComparableStrings.
     * <p>
     *
     * @param ts   the array of StringComparable (i.e. X) objects.
     * @param xs   an array of Strings which will be over-written starting at index from.
     * @param from the starting index.
     * @param n    the number of strings to recover.
     */
    default void recoverXFromT(final T[] ts, final X[] xs, final int from, final int n, final Function<T, X> recover) {
        for (int i = 0; i < n; i++) xs[i + from] = recover.apply(ts[i]);
    }

    /**
     * Method to convert an array (ws) of unicode Strings into an array of Xs (which are StringComparable).
     *
     * @param clazz              the class of X.
     * @param ys                 the input array of unicode Strings.
     * @param from               the starting index.
     * @param n                  the number of elements to process and return.
     * @param toStringComparable a function which takes a String and returns an X.
     * @return an X[] of length n.
     */
    default T[] transformXToT(final Class<T> clazz, final String[] ys, final int from, final int n, final Function<String, T> toStringComparable) {
        @SuppressWarnings("unchecked") final T[] xs = (T[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) xs[i] = toStringComparable.apply(ys[i + from]);
        return xs;
    }
}
