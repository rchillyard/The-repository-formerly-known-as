/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;

public class SelectionSort<X extends Comparable<X>> implements Sort<X> {

    /**
     * Constructor for SelectionSort
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public SelectionSort(Helper<X> helper) {
        this.helper = helper;
    }

    public SelectionSort() {
        this(new Helper<>("SelectionSort"));
    }

    @Override
    public void sort(X[] xs, int from, int to) {
        for (int i = from; i < to; i++) {
            int min = i;
            for (int j = i + 1; j < to; j++)
                if (helper.less(xs[j], xs[min]))
                    min = j;
            helper.swap(xs, from, to, i, min);
        }
    }

    @Override
    public String toString() {
        return helper.toString();
    }

    public Helper<X> getHelper() {
        return helper;
    }

    private final Helper<X> helper;
}
