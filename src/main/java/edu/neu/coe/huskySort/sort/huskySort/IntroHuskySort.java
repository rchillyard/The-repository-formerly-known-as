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

import java.util.Arrays;
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
    public static <Y extends Comparable<Y>> IntroHuskySort<Y> createIntroHuskySortWithInversionCount(HuskyCoder<Y> huskyCoder, int N, Config config) {
        String value = isCountInterimInversions(config) + "";
        Config copy = config.copy(InstrumentedHelper.INSTRUMENTING, InstrumentedHelper.FIXES, value).copy(Config.HELPER, BaseHelper.INSTRUMENT, value);
        final MergeSortBasic<Y> finisher = new MergeSortBasic<>(N, copy);
        finisher.init(N);
        return new IntroHuskySort<>("IntroHuskySort/InversionCount", huskyCoder, finisher::mutatingSort, config.copy("huskyhelper", "countinteriminversions", ""), finisher);
    }

    // CONSIDER making this an instance method (carefully!)
    public static boolean isCountInterimInversions(Config config) {
        return config.getBoolean("huskyhelper", "countinteriminversions");
    }

    /**
     * Method to yield the expected number of inversions for a random array of length n.
     *
     * @param n the length of the array.
     * @return the expected number of inversions: n * (n-1) / 4.
     */
    public static double expectedInversions(int n) {
        return 0.25 * n * (n - 1);
    }

    /**
     * The primary sort method.
     *
     * @param xs   sort the array xs from "from" until "to" (i.e. exclusive of to).
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    // TEST
    @Override
    public void sort(X[] xs, int from, int to) {
        long[] longs = getHelper().getLongs();
        quickSort(xs, longs, 0, longs.length - 1, 2 * floor_lg(to - from));
    }

    /**
     * @param xs the array to be sorted.
     * @param makeCopy true if we should make a copy of xs.
     * @return the sorted array, either xs itself or a copy.
     */
    @Override
    public X[] sort(X[] xs, boolean makeCopy) {
        // CONSIDER merge this with super-method (which only lacks the adjunctSorter lines).
        huskyHelper.init(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        huskyHelper.initLongArray(result);
        sort(result, 0, result.length);
        if (adjunctSorter != null)
            adjunctSorter.preProcess(result);
        huskyHelper.getPostSorter().accept(result);
        return result;
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

    public boolean isClosed() {
        return closed;
    }

    private StatPack getStatPack() {
        final InstrumentedHelper<X> delegateHelper = InstrumentedHelper.getInstrumentedHelper(adjunctSorter.getHelper(), null);
        if (delegateHelper != null && delegateHelper.instrumented()) return delegateHelper.getStatPack();
        else return null;
    }

    /**
     * Method to post-process an array after sorting.
     * <p>
     * In this implementation, we delegate the post-processing to the helper.
     *
     * @param xs the array to be post-processed.
     */
    @Override
    public void postProcess(X[] xs) {
        super.postProcess(xs);
        if (adjunctSorter != null)
            adjunctSorter.postProcess(xs);
    }

    public SortWithHelper<X> getAdjunctSorter() {
        return adjunctSorter;
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
    public IntroHuskySort(String name, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config, SortWithHelper<X> adjunctSorter) {
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
    public IntroHuskySort(String name, HuskyCoder<X> huskyCoder, Consumer<X[]> postSorter, Config config) {
        this(name, huskyCoder, postSorter, config, null);
    }

    /**
     * Secondary constructor for IntroHuskySort.
     * Name will be IntroHuskySort/System.
     * Post-sorter will be the system sort.
     *
     * @param huskyCoder the Husky coder.
     * @param config     the configuration.
     */
    // TEST
    public IntroHuskySort(HuskyCoder<X> huskyCoder, Config config) {
        this("IntroHuskySort/System", huskyCoder, Arrays::sort, config);
    }

    // TEST
    @SuppressWarnings({"UnnecessaryLocalVariable"})
    private void quickSort(X[] objects, long[] longs, int from, int to, int depthThreshold) {
        int lo = from;
        int hi = to;
        if (hi <= lo) return;
        if (hi - lo <= sizeThreshold) {
            insertionSort(objects, longs, from, to);
            return;
        }
        if (depthThreshold == 0) {
            heapSort(objects, longs, from, to);
            return;
        }
        Partition partition = partition(objects, longs, lo, hi);
        quickSort(objects, longs, lo, partition.lt - 1, depthThreshold - 1);
        quickSort(objects, longs, partition.gt + 1, hi, depthThreshold - 1);
    }

    // TEST
    private Partition partition(X[] objects, long[] longs, int lo, int hi) {
        // CONSIDER merge with partition from QuickHuskySort
        int lt = lo, gt = hi;
        if (longs[lo] > longs[hi]) swap(objects, lo, hi);
        long v = longs[lo];
        int i = lo + 1;
        while (i <= gt) {
            if (longs[i] < v) swap(objects, lt++, i++);
            else if (longs[i] > v) swap(objects, i, gt--);
            else i++;
        }
        return new Partition(lt, gt);
    }

    // TEST
    private void heapSort(X[] objects, long[] longs, int from, int to) {
        int n = to - from + 1;
        for (int i = n / 2; i >= 1; i = i - 1) {
            downHeap(objects, longs, i, n, from);
        }
        for (int i = n; i > 1; i = i - 1) {
            swap(objects, from, from + i - 1);
            downHeap(objects, longs, 1, i - 1, from);
        }
    }

    // CONSIDER: use downHeap of PureHuskySort
    private void downHeap(X[] objects, long[] longs, int i, int n, int lo) {
        long d = longs[lo + i - 1];
        X od = objects[lo + i - 1];
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

    // TEST
    private void insertionSort(X[] objects, long[] longs, int from, int to) {
        for (int i = from + 1; i <= to; i++)
            for (int j = i; j > from && longs[j] < longs[j - 1]; j--)
                swap(objects, j, j - 1);
    }

    private static final int sizeThreshold = 16;

    // CONSIDER invoke method in IntroSort
    private static int floor_lg(int a) {
        return (int) (Math.floor(Math.log(a) / Math.log(2)));
    }

    private static class Partition {
        Partition(int lt, int gt) {
            this.lt = lt;
            this.gt = gt;
        }

        final int lt;
        final int gt;
    }

    public double getMeanInterimInversions() {
        if (adjunctSorter == null) {
            logger.warn("IntroHuskySort.getMeanInterimInversions: interim inversions is not enabled. Use createIntroHuskySortWithInversionCount() instead");
            return 0;
        } else {
            StatPack statPack = getStatPack();
            if (closed && statPack != null) {
                Statistics fixes = statPack.getStatistics(InstrumentedHelper.FIXES);
                if (fixes != null) return fixes.mean();
                else throw new RuntimeException("Cannot get fixes from StatPack");
            } else throw new RuntimeException("Cannot get statPack or not closed");
        }
    }

    private final SortWithHelper<X> adjunctSorter;

    private boolean closed;

}
