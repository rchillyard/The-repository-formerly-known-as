package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.bqs.Bag;
import edu.neu.coe.huskySort.bqs.Bag_Array;
import edu.neu.coe.huskySort.sort.BaseComparisonSortHelper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyBucketHelper;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.lang.reflect.Array;

/**
 * @param <X> the underlying type which must
 */
public final class BucketSort<X extends Comparable<X>> implements Sort<X> {

    public static final String DESCRIPTION = "Bucket sort";

    public void sort(final X[] xs, final int from, final int to) {
        logger.info(helper.inversions(xs));
        // Determine the min, max and gap.
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        final Number[] ys = (Number[]) xs;
        for (int i = from; i < to; i++) {
            if (ys[i].doubleValue() < min) min = ys[i].doubleValue();
            if (max < ys[i].doubleValue()) max = ys[i].doubleValue();
        }
        final double gap = (max - min) / bucket.length;

        // Assign the elements to buckets
        for (int i = from; i < to; i++) {
            int index = (int) Math.floor((ys[i].doubleValue() - min) / gap);
            if (index == bucket.length) index--;
            bucket[index].add(xs[i]);
        }

        HuskyBucketHelper.unloadBuckets(bucket, xs, helper);

        logger.info(insertionSort.toString());
        logger.info(helper.inversions(xs));
    }

    @Override
    public String toString() {
        return helper.toString();
    }

    /**
     * Perform initializing step for this Sort.
     *
     * @param n the number of elements to be sorted.
     */
    public void init(final int n) {
        // NOTE: do nothing.
    }

    /**
     * Post-process the given array, i.e. after sorting has been completed.
     *
     * @param xs an array of Xs.
     * @return the result of calling helper.postProcess(xs).
     */
    public boolean postProcess(final X[] xs) {
        return helper.postProcess(xs);
    }

    public void close() {
        if (closeHelper) helper.close();
    }

    BucketSort(final int buckets, final BaseComparisonSortHelper<X> helper) {
        //noinspection unchecked
        bucket = (Bag<X>[]) Array.newInstance(Bag.class, buckets);
        for (int i = 0; i < buckets; i++) bucket[i] = new Bag_Array<>();
        this.helper = helper;
        insertionSort = new InsertionSort<>();
    }

    BucketSort(final int buckets) {
        this(buckets, new BaseComparisonSortHelper<>(DESCRIPTION));
        closeHelper = true;
    }

    private final static LazyLogger logger = new LazyLogger(BucketSort.class);

    private final BaseComparisonSortHelper<X> helper;
    private final Bag<X>[] bucket;
    private final InsertionSort<X> insertionSort;
    private boolean closeHelper = false;

}
