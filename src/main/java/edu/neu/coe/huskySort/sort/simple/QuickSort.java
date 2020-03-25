/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.SortWithHelper;

import java.util.Arrays;

/**
 * Currently unused.
 *
 * @param <X>
 */
public class QuickSort<X extends Comparable<X>> extends SortWithHelper<X> {

    public QuickSort(Helper<X> helper) {
        super(helper);
    }

    /**
     * Constructor for QuickSort
     *
     * @param N            the number elements we expect to sort.
     * @param instrumented whether or not we want an instrumented helper class.
     */
    public QuickSort(int N, boolean instrumented) {
        super(DESCRIPTION, N, instrumented);
    }

    public QuickSort() {
        this(new BaseHelper<>(DESCRIPTION));
    }

    @Override
    // TODO this needs to be unit-tested
    public void sort(X[] xs, int from, int to) {
        Arrays.sort(xs, from, to);
//        for (int i = from; i < to; i++)
//            for (int j = i; j > 0; j--)
//                if (less(xs[j], xs[j - 1]))
//                    swap(xs, j, j - 1);
//                else break;
//
    }

    public static final String DESCRIPTION = "Quick sort";

}

