/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyBucketHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;
import edu.neu.coe.huskySort.util.Config;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Class to perform Husky Bucket Sort.
 *
 * @param <X> the type of elements to be sorted.
 */
public final class HuskyBucketSort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    /**
     * Perform the pre-processing before we do bucket sort.
     * Sets up the bucketHelper to be an instance of HuskyBucketHelper<X>.</X>
     *
     * @param xs the elements to be pre-processed.
     * @return the value of xs (unchanged in any way).
     */
    @Override
    public X[] preProcess(final X[] xs) {
        bucketHelper = new HuskyBucketHelper<>(name, bucketSize, xs.length, getHelper().getCoder(), getHelper().getPostSorter());
        return xs;
    }

    /**
     * This method behaves differently from its super-method mainly because we don't have a method to sort a sub-array.
     *
     * @param xs       sort the array xs, returning the sorted result, leaving xs unchanged.
     * @param makeCopy if set to true, we make a copy first and sort that.
     * @return the sorted array.
     */
    @Override
    public X[] sort(final X[] xs, final boolean makeCopy) {
        final int n = xs.length;
        final X[] result = makeCopy ? Arrays.copyOf(xs, n) : xs;
        assert (bucketHelper != null);
        final int t = bucketHelper.loadBuckets(result);
        assert (t == n);
        bucketHelper.unloadBuckets(result);
        return result;
    }

    /**
     * Sort a subarray for the given xs.
     *
     * @param xs   sort the array xs from "from" until "to" (exclusive of to).
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(final X[] xs, final int from, final int to) {
        throw new RuntimeException("logic error not implemented");
    }

    /**
     * Constructor for HuskyBucketSort.
     *
     * @param name       the name for this sorter.
     * @param bucketSize the bucket size.
     * @param huskyCoder the Husky coder.
     * @param sorter     the sorter.
     * @param config     the configuration.
     */
    public HuskyBucketSort(final String name, final int bucketSize, final HuskyCoder<X> huskyCoder, final Consumer<X[]> sorter, final Config config) {
        super(name, 0, huskyCoder, sorter, config);
        this.bucketSize = bucketSize;
    }

    /**
     * Constructor for HuskyBucketSort.
     *
     * @param bucketSize the bucket size.
     * @param huskyCoder the Husky coder.
     * @param config     the configuration.
     */
    public HuskyBucketSort(final int bucketSize, final HuskyCoder<X> huskyCoder, final Config config) {
        this("HuskyBucketSort", bucketSize, huskyCoder, InsertionSort::mutatingInsertionSort, config);
    }

    private final int bucketSize;
    private HuskyBucketHelper<X> bucketHelper;
}
