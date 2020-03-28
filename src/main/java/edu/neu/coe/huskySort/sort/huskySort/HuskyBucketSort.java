/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyBucketHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;

import java.util.Arrays;
import java.util.function.Consumer;

public class HuskyBucketSort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    private final int bucketSize;
    private HuskyBucketHelper<X> bucketHelper;

    public HuskyBucketSort(String name, int bucketSize, HuskyCoder<X> huskyCoder, Consumer<X[]> sorter, boolean instrumentation) {
        super(name, 0, huskyCoder, sorter, instrumentation);
        this.bucketSize = bucketSize;
    }

    public HuskyBucketSort(int bucketSize, HuskyCoder<X> huskyCoder, boolean instrumentation) {
        this("HuskyBucketSort", bucketSize, huskyCoder, InsertionSort::mutatingInsertionSort, instrumentation);
    }

    public X[] preProcess(X[] xs) {
        bucketHelper = new HuskyBucketHelper<>(name, bucketSize, xs.length, getHelper().getCoder(), getHelper().getPostSorter());
        return xs;
    }

    public X[] sort(X[] xs, boolean makeCopy) {
        int n = xs.length;
        X[] result = makeCopy ? Arrays.copyOf(xs, n) : xs;
        assert (bucketHelper != null);
        int t = bucketHelper.loadBuckets(result);
        assert (t == n);
        bucketHelper.unloadBuckets(result);
        getHelper().getPostSorter().accept(result);
        return result;
    }

    public void sort(X[] xs, int from, int to) {
        throw new RuntimeException("logic error not implemented");
    }
}
