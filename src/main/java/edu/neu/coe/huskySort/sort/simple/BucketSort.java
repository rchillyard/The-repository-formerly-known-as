package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.bqs.Bag;
import edu.neu.coe.huskySort.bqs.Bag_Array;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.Sort;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.lang.reflect.Array;

public class BucketSort<X extends Number & Comparable<X>> implements Sort<X> {

    private final Helper<X> helper;
    private final Bag<X>[] bucket;
    private final InsertionSort<X> insertionSort;

    BucketSort(int buckets, Helper<X> helper) {
        //noinspection unchecked
        bucket = (Bag<X>[]) Array.newInstance(Bag.class, buckets);
        for (int i = 0; i < buckets; i++) bucket[i] = new Bag_Array<>();
        this.helper = helper;
        insertionSort = new InsertionSort<>();
    }

    BucketSort(int buckets) {
        this(buckets, new Helper<>("Bucket Sort"));
    }

    @Override
    public void sort(X[] xs, int from, int to) {
        logger.info(helper.inversions(xs, from, to));
        // Determine the min, max and gap.
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = from; i < to; i++) {
            if (xs[i].doubleValue() < min) min = xs[i].doubleValue();
            if (max < xs[i].doubleValue()) max = xs[i].doubleValue();
        }
        double gap = (max - min) / bucket.length;

        // Assign the elements to buckets
        for (int i = from; i < to; i++) {
            int index = (int) Math.floor((xs[i].doubleValue() - min) / gap);
            if (index == bucket.length) index--;
            bucket[index].add(xs[i]);
        }

        // Copy the buckets back into array
        int index = 0;
        // TODO consider replacing with foreach
        for (Bag<X> xes : bucket) {
            for (X x : xes) xs[index++] = x;
        }

        logger.info(helper.inversions(xs, from, to));

        insertionSort.sort(xs, from, to);
        logger.info(insertionSort.toString());

        logger.info(helper.inversions(xs, from, to));
    }

    @Override
    public String toString() {
        return helper.toString();
    }

    @Override
    public Helper<X> getHelper() {
        return helper;
    }

    final static LazyLogger logger = new LazyLogger(BucketSort.class);
}
