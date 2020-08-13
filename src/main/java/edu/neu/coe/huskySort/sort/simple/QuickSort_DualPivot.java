package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.util.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to implement Dual-pivot Quick Sort.
 *
 * @param <X> the underlying type to be sorted.
 */
public class QuickSort_DualPivot<X extends Comparable<X>> extends QuickSort<X> {

    public static final String DESCRIPTION = "QuickSort dual pivot";

    /**
     * Method to create a partitioner.
     *
     * @return a Partitioner
     */
    public Partitioner<X> createPartitioner() {
        return new Partitioner_DualPivot(getHelper());
    }

    public QuickSort_DualPivot(final String description, final int N, final Config config) {
        super(description, N, config);
        setPartitioner(createPartitioner());
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public QuickSort_DualPivot(final Helper<X> helper) {
        super(helper);
        setPartitioner(createPartitioner());
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public QuickSort_DualPivot(final int N, final Config config) {
        this(DESCRIPTION, N, config);
    }

    public final class Partitioner_DualPivot implements Partitioner<X> {

        public Partitioner_DualPivot(final Helper<X> helper) {
            this.helper = helper;
        }

        /**
         * Method to partition the given partition into smaller partitions.
         *
         * @param partition the partition to divide up.
         * @return an array of partitions, whose length depends on the sorting method being used.
         */
        public List<Partition<X>> partition(final Partition<X> partition) {
            final X[] xs = partition.xs;
            final int lo = partition.from;
            final int hi = partition.to - 1;
            helper.swapConditional(xs, lo, hi);
            int lt = lo + 1;
            int gt = hi - 1;
            int i = lt;
            // NOTE: we are trying to avoid checking on instrumented for every time in the inner loop for performance reasons (probably a silly idea).
            // NOTE: if we were using Scala, it would be easy to set up a comparer function and a swapper function. With java, it's possible but much messier.
            if (helper.instrumented()) {
                while (i <= gt) {
                    if (helper.compare(xs, i, lo) < 0) helper.swap(xs, lt++, i++);
                    else if (helper.compare(xs, i, hi) > 0) helper.swap(xs, i, gt--);
                    else i++;
                }
                helper.swap(xs, lo, --lt);
                helper.swap(xs, hi, ++gt);
            } else {
                while (i <= gt) {
                    final X x = xs[i];
                    if (x.compareTo(xs[lo]) < 0) swap(xs, lt++, i++);
                    else if (x.compareTo(xs[hi]) > 0) swap(xs, i, gt--);
                    else i++;
                }
                swap(xs, lo, --lt);
                swap(xs, hi, ++gt);
            }

            final List<Partition<X>> partitions = new ArrayList<>();
            partitions.add(new Partition<>(xs, lo, lt));
            partitions.add(new Partition<>(xs, lt + 1, gt));
            partitions.add(new Partition<>(xs, gt + 1, hi + 1));
            return partitions;
        }

        // CONSIDER invoke swap in BaseHelper.
        private void swap(final X[] ys, final int i, final int j) {
            final X temp = ys[i];
            ys[i] = ys[j];
            ys[j] = temp;
        }

        private final Helper<X> helper;
    }
}

