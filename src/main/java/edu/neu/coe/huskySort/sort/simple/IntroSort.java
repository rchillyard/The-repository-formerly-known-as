/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;

import java.util.Arrays;

public class IntroSort<X extends Comparable<X>> implements Sort<X> {
    /**
     * Constructor for InsertionSort
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public IntroSort(Helper<X> helper) {
        this.helper = helper;
    }

    public IntroSort() {
        this(new Helper<>("IntroSort"));
    }

    class Partition {
        final int lt;
        final int gt;

        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }

    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        getHelper().setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        // TODO make this consistent with other uses of sort where the upper limit of the range is result.length
        int from = 0, to = result.length - 1;
        sort(result, 0, result.length - 1, 2 * floor_lg(to - from));
        return result;
    }

    @Override
    public void sort(X[] a, int from, int to) {
        sort(a, from, to, 2 * floor_lg(to - from));
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void sort(X[] a, int from, int to, int depthThreshold) {
        int lo = from;
        int hi = to;
        if (hi <= lo) return;
        if (hi - lo <= sizeThreshold) {
            insertionSort(a, from, to);
            return;
        }
        // TODO this needs to be unit-tested
        if (depthThreshold == 0) {
            heapSort(a, from, to);
            return;
        }
        Partition partition = partition(a, lo, hi);
        sort(a, lo, partition.lt - 1, depthThreshold - 1);
        sort(a, partition.gt + 1, hi, depthThreshold - 1);
    }

    public Partition partition(X[] a, int lo, int hi) {
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

    /*
     * Heapsort algorithm
     */
    private void heapSort(X[] a, int from, int to) {
        int n = to - from + 1;
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
        for (int i = from + 1; i <= to; i++)
            for (int j = i; j > from && helper.less(xs[j], xs[j - 1]); j--)
                helper.swap(xs, from, to, j, j - 1);
    }

    @Override
    public Helper<X> getHelper() {
        return helper;
    }

    // exchange a[i] and a[j]
    private static void swap(Object[] a, int i, int j) {
        Object temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private static int floor_lg(int a) {
        return (int) (Math.floor(Math.log(a) / Math.log(2)));
    }

    private final Helper<X> helper;

    private static final int sizeThreshold = 16;
}
