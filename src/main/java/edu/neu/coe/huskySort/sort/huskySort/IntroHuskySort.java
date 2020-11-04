/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.BaseHelper;
import edu.neu.coe.huskySort.sort.InstrumentedHelper;
import edu.neu.coe.huskySort.sort.SortWithHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.simple.MergeSortBasic;
import edu.neu.coe.huskySort.util.Config;
import edu.neu.coe.huskySort.util.StatPack;
import edu.neu.coe.huskySort.util.Statistics;
import edu.neu.coe.huskySort.util.Utilities;

import java.util.function.Consumer;

/**
 * This class defines the preferred form of HuskySort: based on IntroSort which tends to run slightly faster than pure QuickSort.
 *
 * @param <X> the underlying type of the elements to be sorted.
 */
public class IntroHuskySort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    /**
     * Factory method to create an IntroHuskySort instance which uses merge sort to finish up,
     * wherein the merge sort counts the number of fixes (which is the same thing as the interim number of inversions).
     *
     * @param huskyCoder the Husky coder.
     * @param N          the number of elements (may be 0).
     * @param config     the configuration.
     * @param <Y>        the underlying type.
     * @return a new instance of IntroHuskySort.
     */
    public static <Y extends Comparable<Y>> IntroHuskySort<Y> createIntroHuskySortWithInversionCount(final HuskyCoder<Y> huskyCoder, final int N, final Config config) {
        final String value = isCountInterimInversions(config) + "";
        final Config copy = config.copy(InstrumentedHelper.INSTRUMENTING, InstrumentedHelper.FIXES, value).copy(Config.HELPER, BaseHelper.INSTRUMENT, value);
        // CONSIDER using insertion sort instead of mergeSort.
        final MergeSortBasic<Y> finisher = new MergeSortBasic<>(N, copy);
        finisher.init(N);
        return new IntroHuskySort<>("IntroHuskySort/InversionCount", huskyCoder, finisher::mutatingSort, config.copy("huskyhelper", "countinteriminversions", ""), finisher);
    }

    // CONSIDER making this an instance method (carefully!)
    public static boolean isCountInterimInversions(final Config config) {
        return config.getBoolean("huskyhelper", "countinteriminversions");
    }

    /**
     * Method to yield the expected number of inversions for a random array of length n.
     *
     * @param n the length of the array.
     * @return the expected number of inversions: n * (n-1) / 4.
     */
    public static double expectedInversions(final int n) {
        return 0.25 * n * (n - 1);
    }

    /**
     * The primary sort method.
     *
     * @param xs   sort the array xs from "from" until "to" (i.e. exclusive of to).
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    public void sort(final X[] xs, final int from, final int to) {
        final long[] longs = getHelper().getLongs();
        quickSort(xs, longs, 0, longs.length - 1, 2 * floor_lg(to - from));
    }

    /**
     * The postSort method.
     * If adjunctSorter is not null. we invoke its pre-processor.
     * Then we apply the post-sorter to the array.
     * <p>
     * NOTE: this method does NOT invoke its super-method.
     *
     * @param xs the result of the sorting.
     * @return the array xs, which may have been changed by both the adjunctSort and the post-sorter.
     */
    @Override
    public X[] postSort(final X[] xs) {
        if (adjunctSorter != null)
            adjunctSorter.preProcess(xs);
        huskyHelper.getPostSorter().accept(xs);
        return xs;
    }

    /**
     * Close this sorter.
     * As a side-effect, we get the value of interim inversions, provided that it has been set up.
     */
    @Override
    public void close() {
        if (closeHelper) {
            huskyHelper.close();
            if (adjunctSorter != null) {
                adjunctSorter.close();
                closed = true;
            }
        }
    }

    /**
     * Method to determine if this sorter is closed.
     * <p>
     * TODO make this private (but note that it is unused by unit tests)
     *
     * @return the value of closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Method to post-process an array after sorting.
     * <p>
     * In this implementation, we delegate the post-processing to the helper.
     *
     * @param xs the array to be post-processed.
     */
    @Override
    public void postProcess(final X[] xs) {
        super.postProcess(xs);
        if (adjunctSorter != null)
            adjunctSorter.postProcess(xs);
    }

    /**
     * TODO make this private (but note that it is unused by unit tests)
     *
     * @return the value of adjunctSorter field.
     */
    public SortWithHelper<X> getAdjunctSorter() {
        return adjunctSorter;
    }

    /**
     * Get the mean number of interim inversions.
     * Such inversions are the ones left over after the first Husky sort pass.
     *
     * @return the mean as a double.
     */
    public double getMeanInterimInversions() {
        if (adjunctSorter == null) {
            logger.warn("IntroHuskySort.getMeanInterimInversions: interim inversions is not enabled. Use createIntroHuskySortWithInversionCount() instead");
            return Double.NaN;
        } else {
            final StatPack statPack = getStatPack();
            if (closed && statPack != null) {
                final Statistics fixes = statPack.getStatistics(InstrumentedHelper.FIXES);
                if (fixes != null) return fixes.mean();
                else throw new RuntimeException("Cannot get fixes from StatPack");
            } else throw new RuntimeException("Cannot get statPack or not closed");
        }
    }

    /**
     * Primary constructor for IntroHuskySort.
     *
     * @param name          the name of the sort which will be used by the Helper.
     * @param huskyCoder    the Husky coder.
     * @param postSorter    the post-sorter which will eliminate any remaining inversions.
     * @param config        the configuration.
     * @param adjunctSorter this sorter, if present, is the finisher and needs to be closed.
     */
    public IntroHuskySort(final String name, final HuskyCoder<X> huskyCoder, final Consumer<X[]> postSorter, final Config config, final SortWithHelper<X> adjunctSorter) {
        super(name, 0, huskyCoder, postSorter, config);
        this.adjunctSorter = adjunctSorter;
    }

    /**
     * Primary constructor for IntroHuskySort.
     *
     * @param name       the name of the sort which will be used by the Helper.
     * @param huskyCoder the Husky coder.
     * @param postSorter the post-sorter which will eliminate any remaining inversions.
     * @param config     the configuration.
     */
    public IntroHuskySort(final String name, final HuskyCoder<X> huskyCoder, final Consumer<X[]> postSorter, final Config config) {
        this(name, huskyCoder, postSorter, config, null);
    }

    private StatPack getStatPack() {
        final InstrumentedHelper<X> delegateHelper = InstrumentedHelper.getInstrumentedHelper(adjunctSorter.getHelper(), null);
        if (delegateHelper != null && delegateHelper.instrumented()) return delegateHelper.getStatPack();
        else return null;
    }

    @SuppressWarnings({"UnnecessaryLocalVariable"})
    private void quickSort(final X[] objects, final long[] longs, final int from, final int to, final int depthThreshold) {
        final int lo = from;
        final int hi = to;
        if (hi <= lo) return;
        if (hi - lo <= sizeThreshold) {
            insertionSort(objects, longs, from, to);
            return;
        }
        if (depthThreshold == 0) {
            heapSort(objects, longs, from, to);
            return;
        }
        final Partition partition = partition(objects, longs, lo, hi);
        quickSort(objects, longs, lo, partition.lt - 1, depthThreshold - 1);
        quickSort(objects, longs, partition.gt + 1, hi, depthThreshold - 1);
    }

    private Partition partition(final X[] objects, final long[] longs, final int lo, final int hi) {
        // CONSIDER merge with partition from QuickHuskySort
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

    private void heapSort(final X[] objects, final long[] longs, final int from, final int to) {
        if (to - from <= sizeThreshold + 1) {
            insertionSort(objects, longs, from, to);
            return;
        }
        final int n = to - from + 1;
        for (int i = n / 2; i >= 1; i = i - 1) {
            downHeap(objects, longs, i, n, from);
        }
        for (int i = n; i > 1; i = i - 1) {
            swap(objects, from, from + i - 1);
            downHeap(objects, longs, 1, i - 1, from);
        }
    }

    // CONSIDER: use downHeap of PureHuskySort
    private void downHeap(final X[] objects, final long[] longs, int i, final int n, final int lo) {
        final long d = longs[lo + i - 1];
        final X od = objects[lo + i - 1];
        int child;
        while (i <= n / 2) {
            child = 2 * i;
            if (child < n && longs[lo + child - 1] < longs[lo + child]) child++;
            if (d >= longs[lo + child - 1]) break;
            longs[lo + i - 1] = longs[lo + child - 1];
            objects[lo + i - 1] = objects[lo + child - 1];
            i = child;
        }
        longs[lo + i - 1] = d;
        objects[lo + i - 1] = od;
    }

    private void insertionSort(final X[] objects, final long[] longs, final int from, final int to) {
        for (int i = from + 1; i <= to; i++)
            for (int j = i; j > from && longs[j] < longs[j - 1]; j--)
                swap(objects, j, j - 1);
    }

    private static final int sizeThreshold = 16;

    private static int floor_lg(final int a) {
        return (int) Utilities.lg(a);
    }

    private static class Partition {
        Partition(final int lt, final int gt) {
            this.lt = lt;
            this.gt = gt;
        }

        final int lt;
        final int gt;
    }

    private final SortWithHelper<X> adjunctSorter;

    private boolean closed;

}
