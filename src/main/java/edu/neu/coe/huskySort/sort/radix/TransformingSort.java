package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.GenericSort;

import java.util.function.Function;

/**
 * Interface to define a sort method which sorts an array of Xs by temporarily transforming them into an array of Ys.
 *
 * @param <X> the element type of the input/output arrays (must implement Comparable of X).
 * @param <T> the element type of the temporary array.
 */
public interface TransformingSort<X extends Comparable<X>, T> extends GenericSort<X> {

    /**
     * Method to get a suitable Helper for this TransformingSort.
     *
     * @return the Helper.
     */
    TransformingHelper<X, T> getHelper();

    /**
     * Method to convert an array (ws) of unicode Strings into an array of Xs (which are StringComparable).
     * This is essentially the inverse of recoverXFromT.
     * <p>
     *
     * @param clazz     the class of T.
     * @param xs        the input array of Xs.
     * @param from      the starting index.
     * @param n         the number of elements to process and return.
     * @param transform a function which takes an X and returns an T.
     * @return a T[] of length n.
     */
    default T[] transformXToT(final Class<T> clazz, final X[] xs, final int from, final int n, final Function<X, T> transform) {
        return getHelper().transformXToT(clazz, xs, from, n, transform);
    }

    /**
     * Method to recover an array of Xs from an array of Ts.
     * This is essentially the inverse of transformXToT.
     * <p>
     *
     * @param ts      the (sorted) array of temporary objects.
     * @param xs      an array of Xs which will be over-written starting at index from.
     * @param from    the starting index.
     * @param n       the number of strings to recover.
     * @param recover a function which takes an T and returns an X.
     */
    default void recoverXFromT(final T[] ts, final X[] xs, final int from, final int n, final Function<T, X> recover) {
        getHelper().recoverXFromT(ts, xs, from, n, recover);
    }
}
