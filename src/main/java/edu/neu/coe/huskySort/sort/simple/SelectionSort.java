/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;

/**
 * Class to implement Selection Sort.
 * NOTE: this implementation does NOT use the insertion swap mechanism,
 *
 * @param <X> the underlying type to be sorted.
 */
public class SelectionSort<X extends Comparable<X>> extends SortWithHelper<X> {

    public SelectionSort(final Helper<X> helper) {
        super(helper);
    }

    /**
     * Constructor for SelectionSort
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public SelectionSort(final int N, final Config config) {
        super(DESCRIPTION, N, config);
    }

    public SelectionSort() {
        this(new BaseHelper<>(DESCRIPTION));
    }

    /**
     * Constructor for SelectionSort
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public SelectionSort(final BaseHelper<X> helper) {
        super(helper);
    }

    public void sort(final X[] xs, final int from, final int to) {
        final Helper<X> helper = getHelper();
        for (int i = from; i < to; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++)
                if (helper.less(xs[j], xs[min]))
                    min = j;
            helper.swap(xs, i, min);
        }
    }

    public static final String DESCRIPTION = "Selection sort";

}
