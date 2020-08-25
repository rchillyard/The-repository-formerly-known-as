/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.util.Config;

import java.util.Arrays;
import java.util.function.Consumer;

public final class QuickHuskySort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    /**
     * Primary constructor to create an implementation of HuskySort which primarily uses Quicksort.
     *
     * @param name       the name of the sorter (used for the helper).
     * @param n          the number of elements to be sorted (may be 0 if unknown).
     * @param huskyCoder the Husky coder.
     * @param postSorter the post-sorter (i.e. the sort method which will fix any remaining inversions).
     * @param config     the configuration.
     */
    public QuickHuskySort(String name, int n, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config) {
        super(name, n, huskyCoder, postSorter, config);
    }

    /**
     * Secondary constructor to create an implementation of HuskySort which primarily uses Quicksort.
     * The number of elements to be sorted is unknown.
     *
     * @param name       the name of the sorter (used for the helper).
     * @param huskyCoder the Husky coder.
     * @param postSorter the post-sorter (i.e. the sort method which will fix any remaining inversions).
     * @param config     the configuration.
     */
    public QuickHuskySort(String name, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config) {
        this(name, 0, huskyCoder, postSorter, config);
    }

    /**
     * Secondary constructor to create an implementation of HuskySort which primarily uses Quicksort.
     * The name will be QuickHuskySort/System.
     * The post-sorter will be the System sort.
     *
     * @param huskyCoder the Husky coder.
     * @param config     the configuration.
     */
    public QuickHuskySort(HuskyCoder<X> huskyCoder, Config config) {
        this("QuickHuskySort/System", huskyCoder, Arrays::sort, config);
    }

    /**
     * Primary sort method, defined in Sort.
     *
     * @param xs   sort the array xs from "from" to "to".
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(X[] xs, int from, int to) {
        quickSort(xs, getHelper().getLongs(), from, to - 1);
    }

    // CONSIDER inlining this private method
    // CONSIDER redefining to to be one higher.
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
