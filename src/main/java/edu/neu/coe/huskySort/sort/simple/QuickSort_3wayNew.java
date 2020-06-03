package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.InstrumentedHelper;
import edu.neu.coe.huskySort.util.Config;

import java.util.ArrayList;
import java.util.List;

public class QuickSort_3wayNew<X extends Comparable<X>> extends QuickSort<X> {

    public static final String DESCRIPTION = "QuickSort 3 way";

    /**
     * Constructor for QuickSort_3way
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public QuickSort_3wayNew(Helper<X> helper) {
        super(null, helper);
        setPartitioner(createPartitioner3Way(helper));
    }

    public QuickSort_3wayNew() {
        this(new BaseHelper<>(DESCRIPTION));
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param N      the number elements we expect to sort.
     * @param config the configuration.
     */
    public QuickSort_3wayNew(int N, Config config) {
        super(DESCRIPTION, N, config);
        setPartitioner(createPartitioner3Way(getHelper()));
    }

    /**
     * Constructor for QuickSort_3way which always uses an instrumented helper with a specific seed.
     * <p>
     * NOTE used by unit tests.
     *
     * @param N    the number of elements to be sorted.
     * @param seed the seed for the random number generator.
     */
    public QuickSort_3wayNew(int N, long seed, Config config) {
        this(new InstrumentedHelper<>(DESCRIPTION, N, config));
    }

    public Partitioner<X> createPartitioner3Way(Helper<X> helper) {
        return new Partitioner_3Way(helper);
    }

//    /**
//     * Class to represent a Partition for 3-way quick sort.
//     * This partition divides the array into three parts:
//     * 0..lt-1: elements smaller than the pivot element(s).
//     * lt..gt: pivot elements: all of equal size (if lt == gt then there is only one pivot element).
//     * gt+1..n-1: elements larger than the pivot element(s).
//     */
//    static class Partition {
//        final int lt; // the index of the lowest pivot element
//        final int gt; // the index of the highest pivot element
//
//        Partition(int lt, int gt) {
//            this.lt = lt;
//            this.gt = gt;
//        }
//
//        @Override
//        public String toString() {
//            return "Partition{" +
//                    "lt=" + lt +
//                    ", gt=" + gt +
//                    '}';
//        }
//    }

    //    /**
//     * Partition the array xs from lo to hi
//     *
//     * @param xs the entire array of Xs.
//     * @param lo the first index of the desired sub-array.
//     * @param hi the last index of the desired sub-array.
//     * @return a Partition.
//     */
//    public Partition partition(X[] xs, int lo, int hi) {
//        final Partitioner partitioner = new Partitioner(getHelper(), lo, hi);
//        return partitioner.getPartition(xs, lo, hi);
//    }

    class Partitioner_3Way implements Partitioner<X> {
        private final Helper<X> helper;

        public Partitioner_3Way(Helper<X> helper) {
            this.helper = helper;
        }

        /**
         * Method to partition the given partition into smaller partitions.
         *
         * @param partition the partition to divide up.
         * @return an array of partitions, whose length depends on the sorting method being used.
         */
        public List<Partition<X>> partition(Partition<X> partition) {
            logger.debug("partition on " + partition);
            X[] xs = partition.xs;
            int lt = partition.from;
            int gt = partition.to - 1;
            helper.swapConditional(xs, lt, gt);
            X v = xs[lt];
            int i = lt + 1;
            // NOTE: we are trying to avoid checking on instrumented for every time in the inner loop for performance reasons (probably a silly idea).
            // NOTE: if we were using Scala, it would be easy to set up a comparer function and a swapper function. With java, it's possible but much messier.
            if (helper.instrumented())
                while (i <= gt) {
                    int cmp = helper.compare(xs[i], v);
                    if (cmp < 0) helper.swap(xs, lt++, i++);
                    else if (cmp > 0) helper.swap(xs, i, gt--);
                    else i++;
                }
            else
                while (i <= gt) {
                    int cmp = xs[i].compareTo(v);
                    if (cmp < 0) swap(xs, lt++, i++);
                    else if (cmp > 0) swap(xs, i, gt--);
                    else i++;
                }

            List<Partition<X>> partitions = new ArrayList<>();
            partitions.add(new Partition<>(xs, partition.from, lt));
            partitions.add(new Partition<>(xs, gt + 1, partition.to));
            return partitions;
        }

        private void swap(X[] ys, int i, int j) {
            if (helper != null) helper.swap(ys, i, j);
            else {
                X temp = ys[i];
                ys[i] = ys[j];
                ys[j] = temp;
            }
        }
    }

//    class OldPartitioner {
//        private final Helper<X> helper;
//        private final int lo;
//        private final int hi;
//
//        OldPartitioner(Helper<X> helper, int lo, int hi) {
//            this.helper = helper;
//            this.lo = lo;
//            this.hi = hi;
//        }
//
//        public Partition doPartitioning(X[] xs, int lo, int hi) {
//            helper.swapConditional(xs, lo, hi);
//            X v = xs[lo];
//            int i = lo + 1;
//            int lt = lo, gt = hi;
//            // NOTE: we are trying to avoid checking on instrumented for every time in the inner loop for performance reasons (probably a silly idea).
//            // NOTE: if we were using Scala, it would be easy to set up a comparer function and a swapper function. With java, it's possible but much messier.
//            if (helper.instrumented())
//                while (i <= gt) {
//                    int cmp = helper.compare(xs[i], v);
//                    if (cmp < 0) helper.swap(xs, lt++, i++);
//                    else if (cmp > 0) helper.swap(xs, i, gt--);
//										else i++;
//								}
//						else
//								while (i <= gt) {
//										int cmp = xs[i].compareTo(v);
//                    if (cmp < 0) swap(xs, lt++, i++);
//                    else if (cmp > 0) swap(xs, i, gt--);
//                    else i++;
//                }
//            return new Partition(lt, gt);
//        }
//
//    }

    // This is for faster sorting (no instrumentation option)

}

