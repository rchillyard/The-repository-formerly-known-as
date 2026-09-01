package edu.neu.coe.huskySort.sort.huskySort;

/**
 * MultikeyQuicksort with its small-range fallback made comparable to MSDStringSort's, so that the
 * two can be compared on their partitioning rather than on their fallback overhead.
 * <p>
 * The three-way partitioning below is character-for-character the same as MultikeyQuicksort's,
 * including the loop over the equal partition. Only the fallback differs. The original allocates an
 * InsertionSort per small range -- which allocates a ComparableSortHelper, which calls
 * System.currentTimeMillis() and constructs a Random -- and then compares through
 * helper.swapIntoSorted, which binary-searches with a lambda and compares whole strings from
 * character zero, re-examining the prefix the recursion has already established.
 * <p>
 * This exists to answer one question: how much of the measured gap between multikey quicksort and
 * MSD is the algorithm and how much is that fallback.
 */
public class MultikeyQuicksortTuned {

    public static void sort(final String[] a) {
        sort(a, 0, a.length - 1, 0);
    }

    private static void sort(final String[] a, int lo, int hi, int d) {
        while (true) {
            if (hi - lo < CUTOFF) {
                if (hi > lo) insertionSort(a, lo, hi + 1, d);
                return;
            }
            final long pivot = keyAt(a[lo], d);
            int lt = lo, gt = hi, i = lo + 1;
            while (i <= gt) {
                final long t = keyAt(a[i], d);
                if (t < pivot) swap(a, lt++, i++);
                else if (t > pivot) swap(a, i, gt--);
                else i++;
            }
            sort(a, lo, lt - 1, d);
            sort(a, gt + 1, hi, d);
            if (pivot < 0) return;
            lo = lt;
            hi = gt;
            d = d + 1;
        }
    }

    /**
     * Every string in this range shares the first d characters, so the comparison starts there.
     * No allocation, no helper, no binary search -- the same shape as MSDStringSort's fallback.
     */
    private static void insertionSort(final String[] a, final int lo, final int hi, final int d) {
        for (int i = lo; i < hi; i++)
            for (int j = i; j > lo && less(a[j], a[j - 1], d); j--)
                swap(a, j, j - 1);
    }

    private static boolean less(final String v, final String w, final int d) {
        final int vLength = v.length(), wLength = w.length();
        final int limit = Math.min(vLength, wLength);
        for (int i = d; i < limit; i++) {
            final char cv = v.charAt(i), cw = w.charAt(i);
            if (cv != cw) return cv < cw;
        }
        return vLength < wLength;
    }

    private static long keyAt(final String s, final int d) {
        return d < s.length() ? s.charAt(d) : -1L;
    }

    private static void swap(final String[] a, final int i, final int j) {
        final String temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private static final int CUTOFF = 16;
}
