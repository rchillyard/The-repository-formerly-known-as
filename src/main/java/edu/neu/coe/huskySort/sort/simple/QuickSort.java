package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.Helper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.List;

/**
 * Base class for implementations of QuickSort.
 *
 * @param <X> the underlying type to be sorted.
 */
public abstract class QuickSort<X extends Comparable<X>> extends SortWithHelper<X> {

    /**
     * Method to create a Partitioner.
     *
     * @return a Partitioner of X which is suitable for the quicksort method being used.
     */
    public abstract Partitioner<X> createPartitioner();

    /**
     * Method to set the partitioner.
     * <p>
     * NOTE: it would be much nicer if we could do this immutably but this isn't Scala, it's Java.
     *
     * @param partitioner the partitioner to be used.
     */
    public void setPartitioner(final Partitioner<X> partitioner) {
        this.partitioner = partitioner;
    }

    /**
     * Sort the sub-array xs[from] .. xs[to-1]
     *
     * @param xs   the complete array from which this sub-array derives.
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(final X[] xs, final int from, final int to) {
        sort(xs, from, to, 0);
    }

    /**
     * Sort the sub-array xs[from] .. xs[to-1]
     *
     * @param xs    the complete array from which this sub-array derives.
     * @param from  the index of the first element to sort.
     * @param to    the index of the first element not to sort.
     * @param depth the depth of the recursion.
     */
    void sort(final X[] xs, final int from, final int to, final int depth) {
        if (terminator(xs, from, to, depth)) return;
        getHelper().registerDepth(depth);
        final Partition<X> partition = createPartition(xs, from, to);
        if (partitioner == null) throw new RuntimeException("partitioner not set");
        final List<Partition<X>> partitions = partitioner.partition(partition);
        partitions.forEach(p -> sort(p.xs, p.from, p.to, depth + 1));
    }

    /**
     * Protected method to determine to terminate the recursion of this quick sort.
     * NOTE that in this implementation, the depth is ignored.
     *
     * @param xs    the complete array from which this sub-array derives.
     * @param from  the index of the first element to sort.
     * @param to    the index of the first element not to sort.
     * @param depth the current depth of the recursion.
     * @return true if there is no further work to be done.
     */
    protected boolean terminator(final X[] xs, final int from, final int to, final int depth) {
        @SuppressWarnings("UnnecessaryLocalVariable") final int lo = from;
        if (to <= lo + getHelper().cutoff()) {
            insertionSort.sort(xs, from, to);
            return true;
        }
        return false;
    }

    /**
     * NOTE: this is called by privateMethodTester and needs to be visible.
     *
     * @return the instance of InsertionSort used by this sorter.
     */
    public InsertionSort<X> getInsertionSort() {
        return insertionSort;
    }

    /**
     * Create a partition on ys from "from" to "to".
     *
     * @param ys   the array to partition
     * @param from the index of the first element to partition.
     * @param to   the index of the first element NOT to partition.
     * @param <Y>  the underlying type of ys.
     * @return a Partition of Y.
     */
    public static <Y extends Comparable<Y>> Partition<Y> createPartition(final Y[] ys, final int from, final int to) {
        return new Partition<>(ys, from, to);
    }

    public static <Y extends Comparable<Y>> Partition<Y> createPartition(final Y[] ys) {
        return createPartition(ys, 0, ys.length);
    }

    public QuickSort(final String description, final int N, final Config config) {
        super(description, N, config);
        insertionSort = new InsertionSort<>(getHelper());
    }

    public QuickSort(final Helper<X> helper) {
        super(helper);
        insertionSort = new InsertionSort<>(helper);
    }

    private final InsertionSort<X> insertionSort;

    protected Partitioner<X> partitioner;

    final static LazyLogger logger = new LazyLogger(QuickSort.class);
}
