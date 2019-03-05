/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;

public class IntroHuskySort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    public IntroHuskySort(HuskyCoder<X> huskyCoder) {
        super("IntroHuskySort", 0, huskyCoder, (xs2) -> new InsertionSort<X>().sort(xs2, false));
    }

    @Override
    public void sort(X[] xs, int from, int to) {
        long[] longs = getHelper().getLongs();
        quickSort(xs, longs, 0, longs.length - 1, 2 * floor_lg(to - from));
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "Duplicates"})
    private void quickSort(X[] objects, long[] longs, int from, int to, int depthThreshold) {
        int lo = from;
        int hi = to;
        if (hi <= lo) return;
        if (hi - lo <= sizeThreshold) {
            insertionSort(objects, longs, from, to);
            return;
        }
        if (depthThreshold == 0) {
            heapSort(objects, longs, from, to);
            return;
        }
        Partition partition = partition(objects, longs, lo, hi);
        quickSort(objects, longs, lo, partition.lt - 1, depthThreshold - 1);
        quickSort(objects, longs, partition.gt + 1, hi, depthThreshold - 1);
    }

    @SuppressWarnings("Duplicates")
    private Partition partition(X[] objects, long[] longs, int lo, int hi) {
        int lt = lo, gt = hi;
        if (longs[lo] > longs[hi]) swap(objects, lo, hi);
        long v = longs[lo];
        int i = lo + 1;
        while (i <= gt) {
            if (longs[i] < v) swap(objects, lt++, i++);
            else if (longs[i] > v) swap(objects, i, gt--);
            else i++;
        }
        return new Partition(lt, gt);
    }

    private class Partition {
        final int lt;
        final int gt;

        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }

    // HeapSort
    private void heapSort(X[] objects, long[] longs, int from, int to) {
        int n = to - from + 1;
        for (int i = n / 2; i >= 1; i = i - 1) {
            downHeap(objects, longs, i, n, from);
        }
        for (int i = n; i > 1; i = i - 1) {
            swap(objects, from, from + i - 1);
            downHeap(objects, longs, 1, i - 1, from);
        }
    }

    private void downHeap(X[] objects, long[] longs, int i, int n, int lo) {
        long d = longs[lo + i - 1];
        X od = objects[lo + i - 1];
        int child;
        while (i <= n / 2) {
            child = 2 * i;
            if (child < n && longs[lo + child - 1] < longs[lo + child]) child++;
            if (d >= longs[lo + child - 1]) break;
            longs[lo + i - 1] = longs[lo + child - 1];
            objects[lo + i - 1] = objects[lo + child - 1];
            i = child;
        }
        longs[lo + i - 1] = d;
        objects[lo + i - 1] = od;
    }

    // InsertionSort
    private void insertionSort(X[] objects, long[] longs, int from, int to) {
        for (int i = from + 1; i <= to; i++)
            for (int j = i; j > from && longs[j] < longs[j - 1]; j--)
                swap(objects, j, j - 1);
    }

    private static int floor_lg(int a) {
        return (int) (Math.floor(Math.log(a) / Math.log(2)));
    }

    private static final int sizeThreshold = 16;
}
