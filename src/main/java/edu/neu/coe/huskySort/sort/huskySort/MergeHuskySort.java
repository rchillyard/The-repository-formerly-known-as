package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.Coding;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Arrays;

public class MergeHuskySort<X extends Comparable<X>> {

    public static void main(String[] args) {

        int N = 50000;
        int m = 10000;
        logger.info("MergeHuskySort: sorting " + N + " random alphabetic ASCII words " + m + " times");
        // Just for test purpose: this should take about 3 minutes
        MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        for (int i = 0; i < m; i++) {
            String[] alphaBetaArray = HuskySortHelper.generateRandomAlphaBetaArray(N, 4, 9);
            sorter.sort(alphaBetaArray);
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
        Coding coding = huskyCoder.huskyEncode(xs);
        long[] longs = coding.longs;
        auObject = Arrays.copyOf(xs, xs.length);
        auLong = Arrays.copyOf(longs, longs.length);
        mergeSort(xs, longs, 0, longs.length - 1);

        // NOTE: Second pass (if required) to fix any remaining inversions.
        if (coding.perfect)
            return;
        Arrays.sort(xs);
    }

    /**
     * Primary constructor.
     *
     * @param huskyCoder the Husky coder to be used for the encoding to longs.
     */
    public MergeHuskySort(HuskyCoder<X> huskyCoder) {
        this.huskyCoder = huskyCoder;
    }

    private static final int cutoff = 7;

    private void mergeSort(X[] objects, long[] longs, int from, int to) {
        @SuppressWarnings("UnnecessaryLocalVariable") int lo = from;
        if (to <= lo + cutoff) {
            insertionSort(objects, longs, from, to);
            return;
        }
        int mid = from + (to - from) / 2;
        mergeSort(objects, longs, lo, mid);
        mergeSort(objects, longs, mid, to);
        System.arraycopy(objects, from, auObject, from, to - from);
        System.arraycopy(longs, from, auLong, from, to - from);
        merge(auObject, objects, auLong, longs, lo, mid, to);
    }

    private void merge(X[] auObject, X[] objects, long[] auLong, long[] longs, int lo, int mid, int hi) {
        int i = lo;
        int j = mid;
        int k = lo;
        for (; k < hi; k++)
            if (i >= mid) copy(auObject, auLong, j++, objects, longs, k);
            else if (j >= hi) copy(auObject, auLong, i++, objects, longs, k);
            else if (auLong[j] < auLong[i]) {
                copy(auObject, auLong, j++, objects, longs, k);
            } else copy(auObject, auLong, i++, objects, longs, k);
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

    private void copy(X[] source, long[] sourceL, int i, X[] target, long[] targetL, int j) {
        target[j] = source[i];
        targetL[j] = sourceL[i];
    }

    private X[] auObject = null;

    private long[] auLong = null;

    private final HuskyCoder<X> huskyCoder;

    private final static LazyLogger logger = new LazyLogger(MergeHuskySort.class);
}
