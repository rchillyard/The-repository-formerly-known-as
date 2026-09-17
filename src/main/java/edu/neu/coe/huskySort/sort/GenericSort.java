package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Utilities;

import java.util.Arrays;
import java.util.Collection;

/**
 * Interface GenericSort which defines the various sort methods for sorting elements of type X.
 * NOTE this definition does not assume that X extends Comparable of X.
 * <p>
 * Extends {@link AutoCloseable} so that a sorter can be used in a try-with-resources statement,
 * which is what callers holding an instrumented helper actually need. The inherited {@code close()}
 * is narrowed to throw nothing, since no implementation here has anything to report, and sparing
 * callers a {@code catch (Exception)} is the whole point.
 *
 * @param <X> the type of the elements to be sorted.
 */
public interface GenericSort<X> extends AutoCloseable {

    /**
     * Close this sorter, releasing anything it holds.
     * <p>
     * NOTE: defaulted to a no-op rather than left abstract, so that implementations holding no
     * resources (the counting sorts reached through TransformingSort) need not declare one.
     * {@link Sort} re-declares it abstract, so every sorter with a helper to close is still obliged
     * to say what closing means.
     */
    @Override
    default void close() {
    }

    /**
     * Generic, non-mutating sort method which allows for explicit determination of the makeCopy option.
     *
     * @param xs       sort the array xs, returning the sorted result, leaving xs unchanged.
     * @param makeCopy if set to true, we make a copy first and sort that.
     */
    X[] sort(X[] xs, boolean makeCopy);

    /**
     * Generic, non-mutating sort method.
     *
     * @param xs sort the array xs, returning the sorted result, leaving xs unchanged.
     */
    default X[] sort(final X[] xs) {
        return sort(xs, true);
    }

    /**
     * Generic, mutating sort method.
     * Note that there is no return value.
     *
     * @param xs the array to be sorted.
     */
    default void mutatingSort(final X[] xs) {
        sort(xs, false);
    }

    /**
     * Generic, mutating sort method which operates on a sub-array.
     *
     * @param xs   sort the array xs from "from" until "to" (exclusive of to).
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    void sort(X[] xs, int from, int to);

    /**
     * Method to take a Collection of X and return an Iterable of X in order.
     *
     * @param xs the collection of X elements.
     * @return a sorted iterable of X.
     */
    default Iterable<X> sort(final Collection<X> xs) {
        if (xs.isEmpty()) return xs;
        final X[] array = Utilities.asArray(xs);
        mutatingSort(array);
        return Arrays.asList(array);
    }
}
