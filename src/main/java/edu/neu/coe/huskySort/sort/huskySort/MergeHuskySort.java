package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.Coding;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskySortHelper;
import edu.neu.coe.huskySort.util.LazyLogger;

import java.util.Arrays;

/**
 * This version of (Pure) Husky Sort is based on merge sort (rather than quicksort) for the main sorting phase.
 * Mopping up (the last phase of HuskySort is performed by the System sort).
 *
 * @param <X> the underlying type to be sorted.
 */
public class MergeHuskySort<X extends Comparable<X>> {

    /**
     * The main sort method.
     * This version of merge sort has three improvements over the basic HuskySort/MergeSort scheme:
     * <ul>
     *     <li>Insertion sort cutoff</li>
     *     <li>Insurance check for all right-hand partition larger than all left-hand partition.</li>
     *     <li>Avoidance of copying between the arrays (other than the sort method itself).</li>
     * </ul>
     *
     * @param xs the array to be sorted.
     */
    public void sort(final X[] xs) {
        // NOTE: First pass where we code to longs and sort according to those.
        final Coding coding = huskyCoder.huskyEncode(xs);
        final long[] longs = coding.longs;
        final int n = xs.length;
        final X[] xsCopy = Arrays.copyOf(xs, n);
        final long[] longsCopy = Arrays.copyOf(longs, n);
        mergeSort(longsCopy, xsCopy, longs, xs, 0, n);

        // NOTE: Second pass (if required) to fix any remaining inversions.
        if (coding.perfect)
            return;
        Arrays.sort(xs);
    }

    /**
     * Primary constructor.
     *
     * @param huskyCoder the Husky coder to be used for the encoding into longs.
     */
    public MergeHuskySort(final HuskyCoder<X> huskyCoder) {
        this.huskyCoder = huskyCoder;
    }

    /**
     *
     * @param args a set of Strings corresponding to command-line arguments.
     */
    public static void main(final String[] args) {
        final int N = 50000;
        final int m = 10000;
        final boolean preSorted = args.length > 0 && Boolean.parseBoolean(args[0]);
        final String inputOrder = preSorted ? "ordered" : "random";
        logger.info("MergeHuskySort: sorting " + N + " " + inputOrder + " alphabetic ASCII words " + m + " times");
        final MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        for (int i = 0; i < m; i++)
            if (preSorted)
                // This should take about 20 seconds
                sorter.sort(getAlphaBetaArrayOrdered(N));
            else
                // This should take about 2 minutes
                sorter.sort(HuskySortHelper.generateRandomAlphaBetaArray(N, 4, 9));
        logger.info("Finished");
    }

    private static final int cutoff = 8;

    /**
     * Merge-sort the lsSortable/xsSortable arrays using the provided auxiliary arrays.
     *
     * @param lsSortable the longs which will actually be used for the comparisons.
     * @param xsSortable the Xs which will be moved collaterally along with their corresponding longs.
     * @param lsAux      the auxiliary long array (will be a copy of lsSortable) on entry.
     * @param xsAux      the auxiliary X array (will be a copy of xsSortable) on entry.
     * @param from       the index from which to begin sorting.
     * @param to         the index of the first element not to be sorted.
     */
    private void mergeSort(final long[] lsSortable, final X[] xsSortable, final long[] lsAux, final X[] xsAux, final int from, final int to) {
        if (to <= from + cutoff) {
            insertionSort(xsAux, lsAux, from, to);
            return;
        }
        final int mid = from + (to - from) / 2;
        mergeSort(lsAux, xsAux, lsSortable, xsSortable, from, mid);
        mergeSort(lsAux, xsAux, lsSortable, xsSortable, mid, to);
        merge(xsSortable, xsAux, lsSortable, lsAux, from, mid, to);
    }

    /**
     * Merge the sorted arrays xsOrdered and lsOrdered and place the result into xsDst and lsDst.
     *
     * @param xsOrdered the X array that is ordered in each of two partitions.
     * @param xsDst     the X array that will be fully ordered on return.
     * @param lsOrdered the long array that is ordered in each of two partitions.
     * @param lsDst     the long array that will be fully ordered on return.
     * @param from      the first index to merge.
     * @param mid       the mid-point index (first element to merge from the second partition).
     * @param to        the first index that should not be merged.
     */
    private void merge(final X[] xsOrdered, final X[] xsDst, final long[] lsOrdered, final long[] lsDst, final int from, final int mid, final int to) {
        // Insurance check: if everything in the high partition is larger than everything in the
        // low partition, no comparisons are needed -- but the elements must still be moved.
        // NOTE: this is where the "avoidance of copying between the arrays" optimization bites.
        // In a merge sort that merges back into the array it read from, an already-ordered pair of
        // partitions needs no work at all and the check can simply return. Here the two arrays
        // alternate roles at each level, so from the first merge onwards xsOrdered and xsDst hold
        // *different* permutations, and returning early leaves the caller reading a stale one.
        // Measured 2026-09-30: returning early instead of copying corrupted 280 of 8,020 random
        // arrays, from n=36 upwards. The bug was invisible for four years because every coder this
        // is used with is imperfect, so sort() always followed the merge with Arrays.sort.
        if (lsOrdered[mid] > lsOrdered[mid - 1]) {
            System.arraycopy(xsOrdered, from, xsDst, from, to - from);
            System.arraycopy(lsOrdered, from, lsDst, from, to - from);
            return;
        }
        int i = from;
        int j = mid;
        int k = from;
        for (; k < to; k++)
            if (i >= mid) copy(xsOrdered, lsOrdered, xsDst, lsDst, j++, k);
            else if (j >= to) copy(xsOrdered, lsOrdered, xsDst, lsDst, i++, k);
            else if (lsOrdered[j] < lsOrdered[i]) {
                copy(xsOrdered, lsOrdered, xsDst, lsDst, j++, k);
            } else copy(xsOrdered, lsOrdered, xsDst, lsDst, i++, k);
    }

    // TEST
    private void insertionSort(final X[] xs, final long[] ls, final int from, final int to) {
        for (int i = from + 1; i < to; i++)
            for (int j = i; j > from && ls[j] < ls[j - 1]; j--)
                swap(xs, ls, j, j - 1);
    }

    private void swap(final X[] xs, final long[] ls, final int i, final int j) {
        // Swap ls
        final long temp1 = ls[i];
        ls[i] = ls[j];
        ls[j] = temp1;
        // Swap xs
        final X temp2 = xs[i];
        xs[i] = xs[j];
        xs[j] = temp2;
    }

    private void copy(final X[] xsFrom, final long[] lsFrom, final X[] xsTo, final long[] lsTo, final int i, final int j) {
        xsTo[j] = xsFrom[i];
        lsTo[j] = lsFrom[i];
    }

    private static String[] getAlphaBetaArrayOrdered(final int n) {
        final String[] strings = new String[n];
        int m = 0;
        for (int i = 0; i < 26; i++)
            for (int j = 0; j < 26; j++)
                for (int k = 0; k < 26; k++)
                    for (int l = 0; l < 26; l++)
                        if (m < n)
                            strings[m++] = "" + (char) ('A' + i) + (char) ('A' + j) + (char) ('A' + k) + (char) ('A' + l);
        return strings;
    }

    private final HuskyCoder<X> huskyCoder;

    private final static LazyLogger logger = new LazyLogger(MergeHuskySort.class);
}
