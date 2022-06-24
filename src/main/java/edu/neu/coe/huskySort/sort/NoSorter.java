/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort;

import edu.neu.coe.huskySort.util.Config;

/**
 * Class to implement No Sort (does not sort).
 *
 * @param <X> the underlying type to be sorted.
 */
public class NoSorter<X extends Comparable<X>> extends SortWithHelper<X> {

    /**
     * Sort the sub-array xs:from:to using insertion sort.
     *
     * @param xs   sort the array xs from "from" to "to".
     * @param from the index of the first element to sort
     * @param to   the index of the first element not to sort
     */
    public void sort(final X[] xs, final int from, final int to) {
        // XXX do absolutely nothing.
    }

    public static final String DESCRIPTION = "No sort";

    /**
     * Constructor for NoSorter
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public NoSorter(final int N, final Config config) {
        super(DESCRIPTION, N, config);
    }

    public NoSorter() {
        this(new BaseComparisonSortHelper<>(DESCRIPTION));
    }

    /**
     * Constructor for InsertionSort
     *
     * @param helper an explicit instance of ComparisonSortHelper to be used.
     */
    public NoSorter(final ComparisonSortHelper<X> helper) {
        super(helper);
    }

}
