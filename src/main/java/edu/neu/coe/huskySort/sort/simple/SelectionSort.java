/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.ComparisonSortHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;

/**
 * Class to implement Selection Sort.
 * NOTE: this implementation does NOT use the insertion swap mechanism,
 *
 * @param <X> the underlying type to be sorted.
 */
public class SelectionSort<X extends Comparable<X>> extends SortWithHelper<X> {

    public SelectionSort(final ComparisonSortHelper<X> helper) {
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
        this(new ComparableSortHelper<>(DESCRIPTION));
    }

    /**
     * Constructor for SelectionSort
     *
     * @param helper an explicit instance of ComparisonSortHelper to be used.
     */
    public SelectionSort(final ComparableSortHelper<X> helper) {
        super(helper);
    }

    public void sort(final X[] xs, final int from, final int to) {
        final ComparisonSortHelper<X> helper = getHelper();
        for (int i = from; i < to; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++)
                if (helper.inverted(xs[min], xs[j]))
                    min = j;
            helper.swap(xs, i, min);
        }
    }

    public static final String DESCRIPTION = "Selection sort";

}
