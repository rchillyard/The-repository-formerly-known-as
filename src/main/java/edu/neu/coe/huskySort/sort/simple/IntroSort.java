/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;

import java.util.Arrays;

public class IntroSort<X extends Comparable<X>> extends SortWithHelper<X> {

    /**
     * Constructor for IntroSort
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public IntroSort(int N, Config config) {
        super(DESCRIPTION, N, config);
    }

    public IntroSort() {
        this(new BaseHelper<>(DESCRIPTION));
    }

    /**
     * Constructor for InsertionSort
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public IntroSort(BaseHelper<X> helper) {
        super(helper);
    }

    static class Partition {
        final int lt;
        final int gt;

        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }

    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        getHelper().init(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        int from = 0, to = result.length;
        sort(result, from, to, 2 * floor_lg(to - from));
        return result;
    }

    /**
     * @param xs an array of Xs.
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    @Override
    public void sort(X[] xs, int from, int to) {
        sort(xs, from, to, 2 * floor_lg(to - from));
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void sort(X[] a, int from, int to, int depthThreshold) {
        if (to - from <= sizeThreshold) {
            if (to > from + 1)
                insertionSort(a, from, to);
            return;
        }
        // TEST
        if (depthThreshold == 0) {
            heapSort(a, from, to);
            return;
        }
        int lo = from;
        int hi = to - 1;
        Partition partition = partition(a, lo, hi);
        sort(a, lo, partition.lt, depthThreshold - 1);
        sort(a, partition.gt + 1, hi + 1, depthThreshold - 1);
    }

    public Partition partition(X[] a, int lo, int hi) {
        // CONSIDER using code from QuickSort_3way
        int lt = lo, gt = hi;
        if (a[lo].compareTo(a[hi]) > 0) swap(a, lo, hi);
        X v = a[lo];
        int i = lo + 1;
        while (i <= gt) {
            int cmp = a[i].compareTo(v);
            if (cmp < 0) swap(a, lt++, i++);
            else if (cmp > 0) swap(a, i, gt--);
            else i++;
        }
        return new Partition(lt, gt);
    }

    public static final String DESCRIPTION = "Intro sort";

    /*
     * Heapsort algorithm
     */
    private void heapSort(X[] a, int from, int to) {
        int n = to - from;
        for (int i = n / 2; i >= 1; i = i - 1) {
            downHeap(a, i, n, from);
        }
        for (int i = n; i > 1; i = i - 1) {
            swap(a, from, from + i - 1);
            downHeap(a, 1, i - 1, from);
        }
    }

    private void downHeap(X[] a, int i, int n, int lo) {
        X d = a[lo + i - 1];
        int child;
        while (i <= n / 2) {
            child = 2 * i;
            if (child < n && a[lo + child - 1].compareTo(a[lo + child]) < 0) child++;
            if (d.compareTo(a[lo + child - 1]) >= 0) break;
            a[lo + i - 1] = a[lo + child - 1];
            i = child;
        }
        a[lo + i - 1] = d;
    }

    /*
     * Insertion sort algorithm
     */
    private void insertionSort(X[] xs, int from, int to) {
        final Helper<X> helper = getHelper();
        for (int i = from + 1; i < to; i++) {
            for (int j = i; j > from && helper.less(xs[j], xs[j - 1]); j--)
                helper.swap(xs, j, j - 1);
        }
    }

    /**
     * exchange a[i] and a[j]
     *
     * @param a the array.
     * @param i one index.
     * @param j other index.
     */
    private static void swap(Object[] a, int i, int j) {
        Object temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private static int floor_lg(int a) {
        return (int) (Math.floor(Math.log(a) / Math.log(2)));
    }

    private static final int sizeThreshold = 16;
}
