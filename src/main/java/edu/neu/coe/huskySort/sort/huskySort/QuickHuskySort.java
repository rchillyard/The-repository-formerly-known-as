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
    public QuickHuskySort(final String name, final int n, final HuskyCoder<X> huskyCoder, final Consumer<X[]> postSorter, final Config config) {
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
    public QuickHuskySort(final String name, final HuskyCoder<X> huskyCoder, final Consumer<X[]> postSorter, final Config config) {
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
    public QuickHuskySort(final HuskyCoder<X> huskyCoder, final Config config) {
        this("QuickHuskySort/System", huskyCoder, Arrays::sort, config);
    }

    /**
     * Primary sort method, defined in Sort.
     *
     * @param xs   sort the array xs from "from" to "to".
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(final X[] xs, final int from, final int to) {
        quickSort(xs, getHelper().getLongs(), from, to - 1);
    }

    // CONSIDER inlining this private method
    // CONSIDER redefining to to be one higher.
    @SuppressWarnings({"UnnecessaryLocalVariable"})
    private void quickSort(final X[] objects, final long[] longs, final int from, final int to) {
        final int lo = from;
        final int hi = to;
        if (hi <= lo) return;
        final Partition partition = partition(objects, longs, lo, hi);
        quickSort(objects, longs, lo, partition.lt - 1);
        quickSort(objects, longs, partition.gt + 1, hi);
    }

    private Partition partition(final X[] objects, final long[] longs, final int lo, final int hi) {
        // CONSIDER creating a method less in order to avoid having direct access to the longs.
        int lt = lo, gt = hi;
        if (longs[lo] > longs[hi]) swap(objects, lo, hi);
        final long v = longs[lo];
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

        Partition(final int lt, final int gt) {
            this.lt = lt;
            this.gt = gt;
        }
    }
}
