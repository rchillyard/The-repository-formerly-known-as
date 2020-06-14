/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.util.Config;

import java.util.Arrays;
import java.util.function.Consumer;

public class QuickHuskySort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    public QuickHuskySort(String name, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config) {
        super(name, 0, huskyCoder, postSorter, config);
    }

    public QuickHuskySort(HuskyCoder<X> huskyCoder, Config config) {
        this("QuickHuskySort/System", huskyCoder, Arrays::sort, config);
    }

    public QuickHuskySort(String name, int n, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config) {
        super(name, n, huskyCoder, postSorter, config);
    }

    public void sort(X[] xs, int from, int to) {
        quickSort(xs, getHelper().getLongs(), from, to - 1);
    }

    // CONSIDER inlining this private method
    @SuppressWarnings({"UnnecessaryLocalVariable"})
    private void quickSort(X[] objects, long[] longs, int from, int to) {
        int lo = from, hi = to;
        if (hi <= lo) return;
        Partition partition = partition(objects, longs, lo, hi);
        quickSort(objects, longs, lo, partition.lt - 1);
        quickSort(objects, longs, partition.gt + 1, hi);
    }

    private Partition partition(X[] objects, long[] longs, int lo, int hi) {
        // CONSIDER creating a method less in order to avoid having direct access to the longs.
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

    private static class Partition {
        final int lt;
        final int gt;

        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }
}
