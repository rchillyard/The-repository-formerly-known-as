/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public interface Sort<X extends Comparable<X>> {

    /**
     * Method to take an AbstractCollection of X and return an Iterable of X which is sorted.
     *
     * @param xs the collection of X elements.
     * @return a sorted iterable of X.
     */
    default Iterable<X> sort(Collection<X> xs) {
        if (xs.isEmpty()) return xs;
        final X[] array = asArray(xs);
        mutatingSort(array);
        return Arrays.asList(array);
    }

    /**
     * Generic, non-mutating sort method which allows for explicit determination of the makeCopy option.
     *
     * @param xs       sort the array xs, returning the sorted result, leaving xs unchanged.
     * @param makeCopy if set to true, we make a copy first and sort that.
     */
    default X[] sort(X[] xs, boolean makeCopy) {
        // CONSIDER doing this as part of init method.
        getHelper().setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        sort(result, 0, result.length);
        return result;
    }

    /**
     * Generic, non-mutating sort method.
     *
     * @param xs sort the array xs, returning the sorted result, leaving xs unchanged.
     */
    default X[] sort(X[] xs) {
        return sort(xs, true);
    }

    /**
     * Generic, mutating sort method.
     * Note that there is no return value.
     *
     * @param xs the array to be sorted.
     */
    default void mutatingSort(X[] xs) {
        sort(xs, false);
    }

    /**
     * Generic, mutating sort method which operates on a sub-array
     *
     * @param xs   sort the array xs from "from" to "to".
     * @param from the index of the first element to sort
     * @param to   the index of the first element not to sort
     */
    void sort(X[] xs, int from, int to);

    /**
     * Get the Helper associated with this Sort.
     *
     * @return the Helper
     */
    Helper<X> getHelper();

    /**
     * Perform initializing step for this Sort.
     *
     * @param n the number of elements to be sorted.
     */
    default void init(int n) {
        getHelper().setN(n);
    }

    /**
     * Perform pre-processing step for this Sort.
     *
     * @param xs the elements to be pre-processed.
     */
    default X[] preProcess(X[] xs) {
        init(xs.length);
        return xs;
    }

    void close();

    /**
     * There is really no good way that I could find to do this with library/language methods.
     *
     * @param xs  a collection of Xs.
     * @param <X> the underlying type of X.
     * @return an array X[].
     */
    static <X> X[] asArray(Collection<X> xs) {
        if (xs.isEmpty()) throw new RuntimeException("xs may not be empty");
        final Iterator<X> iterator = xs.iterator();
        @SuppressWarnings("unchecked") X[] result = (X[]) Array.newInstance(xs.iterator().next().getClass(), xs.size());
        int i = 0;
        while (iterator.hasNext())
            result[i++] = iterator.next();
        return result;
    }
}
