package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.InstrumentedHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;

import java.util.Arrays;

public class QuickSort_3way<X extends Comparable<X>> extends SortWithHelper<X> {
    /**
     * Constructor for QuickSort_3way
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public QuickSort_3way(BaseHelper<X> helper) {
        super(helper);
    }

    public QuickSort_3way() {
        this(new BaseHelper<>(DESCRIPTION));
    }

    /**
     * Constructor for QuickSort_3way
     *
     * @param N            the number elements we expect to sort.
     * @param instrumented whether or not we want an instrumented helper class.
     */
    public QuickSort_3way(int N, boolean instrumented) {
        super(DESCRIPTION, N, instrumented);
    }

    /**
     * Constructor for QuickSort_3way which always uses an instrumented helper with a specific seed.
     * <p>
     * NOTE used by unit tests.
     *
     * @param N    the number of elements to be sorted.
     * @param seed the seed for the random number generator.
     */
    public QuickSort_3way(int N, long seed) {
        this(new InstrumentedHelper<>(DESCRIPTION, N, seed));
    }

    /**
     * Constructor for QuickSort_3way which always uses an instrumented helper with a specific seed.
     * <p>
     * NOTE used by unit tests.
     *
     * @param N the number of elements to be sorted.
     */
    public QuickSort_3way(int N) {
        this(new InstrumentedHelper<>(DESCRIPTION, N, System.currentTimeMillis()));
    }

    /**
     * Class to represent a Partition for 3-way quick sort.
     * This partition divides the array into three parts:
     * 0..lt-1: elements smaller than the pivot element(s).
     * lt..gt: pivot elements: all of equal size (if lt == gt then there is only one pivot element).
     * gt+1..n-1: elements larger than the pivot element(s).
     */
    static class Partition {
        final int lt; // the index of the lowest pivot element
        final int gt; // the index of the highest pivot element

        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }

        @Override
        public String toString() {
            return "Partition{" +
                    "lt=" + lt +
                    ", gt=" + gt +
                    '}';
        }
    }

    public X[] sort(X[] xs, boolean makeCopy) {
        getHelper().setN(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        sort(result, 0, result.length);
        return result;
    }

    /**
     * Sort the sub-array a[from] .. a[to-1]
     *
     * @param a    the complete array from which this sub-array derives.
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(X[] a, int from, int to) {
        @SuppressWarnings("UnnecessaryLocalVariable") int lo = from;
        int hi = to - 1;
        if (hi <= lo) return;
        Partition partition = partition(a, lo, hi);
        sort(a, lo, partition.lt);
        sort(a, partition.gt + 1, hi + 1);
    }

    /**
     * Partition the array xs from lo to hi
     *
     * @param xs the entire array of Xs.
     * @param lo the first index of the desired sub-array.
     * @param hi the last index of the desired sub-array.
     * @return a Partition.
     */
    public Partition partition(X[] xs, int lo, int hi) {
        final Partitioner partitioner = new Partitioner(getHelper(), lo, hi);
        return partitioner.getPartition(xs, lo, hi);
    }

    class Partitioner {
        private final Helper<X> helper;
        private final int lo;
        private final int hi;

        Partitioner(Helper<X> helper, int lo, int hi) {
            this.helper = helper;
            this.lo = lo;
            this.hi = hi;
        }

        // CONSIDER inlining this
        void conditionalSwap(X[] a, int lo, int hi) {
            if (a[lo].compareTo(a[hi]) > 0) swap(a, lo, hi);
        }

        public Partition getPartition(X[] xs, int lo, int hi) {
						conditionalSwap(xs, lo, hi);
						X v = xs[lo];
						int i = lo + 1;
						int lt = lo, gt = hi;
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
            return new Partition(lt, gt);
        }

        private void swap(X[] a, int i, int j) {
            if (helper != null) helper.swap(a, i, j);
            else {
                X temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }
    }

    public static final String DESCRIPTION = "QuickSort 3 way";

    // This is for faster sorting (no instrumentation option)

}

