package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Arrays;

/**
 * This class represents the purest form of Husky Sort based on IntroSort for pass 1 and the System sort for pass 2.
 * <p>
 * CONSIDER redefining all of the "to" parameters to be consistent with our other Sort utilities.
 *
 * @param <X> the type of the elements to be sorted.
 */
public class PureHuskySort<X extends Comparable<X>> {

    public static void main(String[] args) {

        int N = 50000;
        int m = 10000;
        logger.info("PureHuskySort: sorting " + N + " random alphabetic ASCII words " + m + " times");
        // Just for test purpose: this should take about 3 minutes
        PureHuskySort<String> sorter = new PureHuskySort<>(HuskySortHelper.asciiCoder);
        for (int i = 0; i < m; i++) {
            String[] alphaBetaArray = HuskySortHelper.generateRandomAlphaBetaArray(N, 4, 9);
            sorter.sort(Arrays.copyOf(alphaBetaArray, alphaBetaArray.length));
        }
        logger.info("Finished");
    }

    /**
     * The main sort method.
     *
     * @param xs the array to be sorted.
     */
    public void sort(X[] xs) {
        // NOTE: First pass where we code to longs and sort according to those.
        long[] longs = huskyCoder.huskyEncode(xs);
        introSort(xs, longs, 0, longs.length - 1, 2 * floor_lg(xs.length));

        // NOTE: Second pass (if required) to fix any remaining inversions.
        if (huskyCoder.isPerfectCallable() && huskyCoder.perfect())
            return;
        Arrays.sort(xs);
    }

    /**
     * Primary constructor.
     *
     * @param huskyCoder the Husky coder to be used for the encoding to longs.
     */
    public PureHuskySort(HuskyCoder<X> huskyCoder) {
        this.huskyCoder = huskyCoder;
    }

    // CONSIDER invoke method in IntroSort
    private static int floor_lg(int a) {
        return (int) (Math.floor(Math.log(a) / Math.log(2)));
    }

    private static final int sizeThreshold = 16;

    // TEST
    @SuppressWarnings({"UnnecessaryLocalVariable"})
    private void introSort(X[] objects, long[] longs, int from, int to, int depthThreshold) {
        // CONSIDER merge with IntroHuskySort
        if (to <= from) return;
        if (to - from <= sizeThreshold) {
            insertionSort(objects, longs, from, to);
            return;
        }
        if (depthThreshold == 0) {
            heapSort(objects, longs, from, to);
            return;
        }

        int lo = from;
        int hi = to;

        if (longs[hi] < longs[lo]) swap(objects, longs, lo, hi);

        int lt = lo + 1, gt = hi - 1;
        int i = lo + 1;
        while (i <= gt) {
            if (longs[i] < longs[lo]) swap(objects, longs, lt++, i++);
            else if (longs[hi] < longs[i]) swap(objects, longs, i, gt--);
            else i++;
        }
        swap(objects, longs, lo, --lt);
        swap(objects, longs, hi, ++gt);
        introSort(objects, longs, lo, lt - 1, depthThreshold - 1);
        if (longs[lt] < longs[gt]) introSort(objects, longs, lt + 1, gt - 1, depthThreshold - 1);
        introSort(objects, longs, gt + 1, hi, depthThreshold - 1);
    }

    // TEST
    private void heapSort(X[] objects, long[] longs, int from, int to) {
        int n = to - from + 1;
        for (int i = n / 2; i >= 1; i = i - 1) {
            downHeap(objects, longs, i, n, from);
        }
        for (int i = n; i > 1; i = i - 1) {
            swap(objects, longs, from, from + i - 1);
            downHeap(objects, longs, 1, i - 1, from);
        }
    }

    // TEST
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
                swap(objects, longs, j, j - 1);
    }

    private void swap(X[] xs, long[] longs, int i, int j) {
        // Swap longs
        long temp1 = longs[i];
        longs[i] = longs[j];
        longs[j] = temp1;
        // Swap xs
        X temp2 = xs[i];
        xs[i] = xs[j];
        xs[j] = temp2;
    }

    private final HuskyCoder<X> huskyCoder;

    private final static LazyLogger logger = new LazyLogger(PureHuskySort.class);
}
