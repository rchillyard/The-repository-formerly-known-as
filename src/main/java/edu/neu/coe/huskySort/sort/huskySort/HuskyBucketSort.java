/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.bqs.Bag;
import edu.neu.coe.huskySort.bqs.Bag_Array;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.simple.InsertionSort;

import java.lang.reflect.Array;

public class HuskyBucketSort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    private final int buckets;

    public HuskyBucketSort(int buckets, HuskyCoder<X> huskyCoder) {
        super("HuskyBucketSort", 0, huskyCoder, InsertionSort::mutatingInsertionSort);
        //noinspection unchecked
        this.buckets = buckets;
    }


    @Override
    public void sort(X[] xs, int from, int to) {
        final Bag<X>[] bucket = (Bag<X>[]) Array.newInstance(Bag.class, buckets);
        for (int i = 0; i < buckets; i++) bucket[i] = new Bag_Array<>();
        long[] longs = helper.getLongs();
//        System.out.println("inversions: " + helper.inversions(xs, from, to));
        // Determine the min, max and gap.
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int i = from; i < to; i++) {
            if (longs[i] < min) min = longs[i];
            if (max < longs[i]) max = longs[i];
        }
        long gap = (max - min + 1) / bucket.length;

        // Assign the elements to buckets
        for (int i = from; i < to; i++) {
            int index = (int) ((longs[i] - min) / gap);
            if (index == bucket.length) index--;
            bucket[index].add(xs[i]);
        }

        // Copy the buckets back into array
        int index = 0;
        // TODO consider replacing with foreach
        for (int i = 0; i < bucket.length; i++) {
            for (X x : bucket[i]) xs[index++] = x;
        }

//        System.out.println(helper.inversions(xs, from, to));
    }

    @Override
    public String toString() {
        return helper.toString();
    }
}
