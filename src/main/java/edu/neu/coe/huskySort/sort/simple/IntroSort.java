/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.Utilities;

/**
 * Class to implement Intro Sort.
 *
 * @param <X> the underlying type to be sorted.
 */
public class IntroSort<X extends Comparable<X>> extends QuickSort_DualPivot<X> {

    /**
     * Method to do the preSort.
     * Before calling the super-method, we calculate the depthThreshold (i.e level of recursion to switch to heapSort).
     *
     * @param xs       the original array to be sorted.
     * @param makeCopy true if we need to work on a copy of the array.
     * @return the result of calling super.preSort(xs, makeCopy).
     */
    @Override
    public final X[] preSort(final X[] xs, final boolean makeCopy) {
        depthThreshold = 2 * floor_lg(xs.length);
        return super.preSort(xs, makeCopy);
    }

    /**
     * Protected method to determine to terminate the recursion of this quick sort.
     * If the current depth meets or exceeds the depthThreshold, the algorithm switches to heapSort.
     *
     * @param xs    the complete array from which this sub-array derives.
     * @param from  the index of the first element to sort.
     * @param to    the index of the first element not to sort.
     * @param depth the current depth of the recursion.
     * @return true if there is no further work to be done.
     */
    @Override
    protected final boolean terminator(final X[] xs, final int from, final int to, final int depth) {
        if (to - from <= sizeThreshold) {
            if (to > from + 1)
                getInsertionSort().sort(xs, from, to);
            return true;
        }

        if (depth >= depthThreshold) {
            heapSort(xs, from, to);
            return true;
        }

        return false;
    }

    public static final String DESCRIPTION = "Intro sort";

    /**
     * Constructor for QuickSort_3way
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public IntroSort(final Helper<X> helper) {
        super(helper);
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public IntroSort(final int N, final Config config) {
        super(DESCRIPTION, N, config);
    }

    /**
     * Constructor for QuickSort_3way which always uses an instrumented helper with a specific seed.
     * <p>
     * NOTE used by unit tests.
     *
     * @param N      the number of elements to be sorted.
     * @param seed   the seed for the random number generator.
     * @param config the configuration for this sorter.
     */
    public IntroSort(final int N, final long seed, final Config config) {
        super(DESCRIPTION, N, config);
    }

    public IntroSort() {
        this(new BaseHelper<>(DESCRIPTION));
    }

    /*
     * Heapsort algorithm
     */
    private void heapSort(final X[] a, final int from, final int to) {
        final Helper<X> helper = getHelper();
        final int n = to - from;
        for (int i = n / 2; i >= 1; i = i - 1) {
            downHeap(a, i, n, from, helper);
        }
        for (int i = n; i > 1; i = i - 1) {
            helper.swap(a, from, from + i - 1);
            downHeap(a, 1, i - 1, from, helper);
        }
    }

    private void downHeap(final X[] a, int i, final int n, final int lo, final Helper<X> helper) {
        final X d = a[lo + i - 1];
        int child;
        while (i <= n / 2) {
            child = 2 * i;
            if (helper.instrumented()) {
                if (child < n && helper.compare(a, lo + child - 1, lo + child) < 0) child++;
                if (helper.compare(d, a[lo + child - 1]) >= 0) break;
            } else {
                if (child < n && a[lo + child - 1].compareTo(a[lo + child]) < 0) child++;
                if (d.compareTo(a[lo + child - 1]) >= 0) break;
            }
            helper.incrementFixes(1);
            a[lo + i - 1] = a[lo + child - 1];
            i = child;
        }
        a[lo + i - 1] = d;
    }

    /**
     * exchange a[i] and a[j]
     *
     * @param a the array.
     * @param i one index.
     * @param j other index.
     */
    private static void swap(final Object[] a, final int i, final int j) {
        final Object temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private static int floor_lg(final int a) {
        return (int) Utilities.lg(a);
    }

    private int depthThreshold = Integer.MAX_VALUE;

    private static final int sizeThreshold = 16;
}
