/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort;

import java.util.Arrays;

/**
 * Sort interface, which extends GenericSort.
 *
 * @param <X> the underlying type to be sorted.
 *            NOTE: currently, this is required to be a Comparable type.
 *            However, that is not strictly necessary because none of the methods defined here actually rely on that.
 */
public interface Sort<X extends Comparable<X>> extends GenericSort<X> {

    /**
     * Method to prepare for sorting, invoked by the default implementation of sort(X[], boolean).
     * The default method invokes init with the length of the array xs then makes a copy of the array if appropriate.
     *
     * @param xs       the original array to be sorted.
     * @param makeCopy true if we need to work on a copy of the array.
     * @return either the original or a copy of the array.
     */
    default X[] preSort(final X[] xs, final boolean makeCopy) {
        init(xs.length);
        return makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
    }

    /**
     * Generic, non-mutating sort method which allows for explicit determination of the makeCopy option.
     * The three steps are:
     * <ol>
     *     <li>invoke preSort(X[], boolean)</li>
     *     <li>invoke sort(X[], int, int)</li>
     *     <li>return value of postSort(X[])</li>
     * </ol>
     *
     * @param xs       sort the array xs, returning the sorted result, leaving xs unchanged.
     * @param makeCopy if set to true, we make a copy first and sort that.
     */
    default X[] sort(final X[] xs, final boolean makeCopy) {
        final X[] result = preSort(xs, makeCopy);
        sort(result, 0, result.length);
        return postSort(result);
    }

    /**
     * Method to clean up after sorting, invoked by the default implementation of sort(X[], boolean).
     * The default method simply returns the sorted array.
     *
     * @param xs the sorted array.
     * @return the sorted array.
     */
    default X[] postSort(final X[] xs) {
        return xs;
    }

    /**
     * Perform initializing step for this Sort.
     * Invoked by default implementation of preSort(X[], boolean)
     * <p>
     * CONSIDER merging this with preProcess logic.
     *
     * @param n the number of elements to be sorted.
     */
    void init(int n);

    /**
     * Perform pre-processing step for this Sort.
     * WHen benchmarking, this step is typically not timed.
     *
     * @param xs the elements to be pre-processed.
     */
    default X[] preProcess(final X[] xs) {
        init(xs.length);
        return xs;
    }

    /**
     * Post-process the given array, i.e. after sorting has been completed.
     * WHen benchmarking, this step is typically not timed.
     *
     * @param xs an array of Xs.
     */
    void postProcess(X[] xs);

    /**
     * Close this sorter, performing any appropriate cleanup.
     */
    void close();

}
