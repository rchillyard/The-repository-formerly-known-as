/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

public class QuickHuskySort<X extends Comparable<X>> extends AbstractHuskySort<X> {
    @Override
    protected void preSort(Object[] objects, long[] longs, int from, int to) {
        quickSort(objects, longs, 0, objects.length - 1);
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "Duplicates"})
    private void quickSort(Object[] objects, long[] longs, int from, int to) {
        int lo = from, hi = to;
        if (hi <= lo) return;
        Partition partition = partition(objects, longs, lo, hi);
        quickSort(objects, longs, lo, partition.lt - 1);
        quickSort(objects, longs, partition.gt + 1, hi);
    }

    @SuppressWarnings("Duplicates")
    private Partition partition(Object[] objects, long[] longs, int lo, int hi) {
        int lt = lo, gt = hi;
        if (longs[lo] > longs[hi]) swap(objects, longs, lo, hi);
        long v = longs[lo];
        int i = lo + 1;
        while (i <= gt) {
            if (longs[i] < v) swap(objects, longs, lt++, i++);
            else if (longs[i] > v) swap(objects, longs, i, gt--);
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
}
