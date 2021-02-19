package edu.neu.coe.huskySort.sort.simple;


import edu.neu.coe.huskySort.sort.SortException;

/**
 * Class to implement Most significant digit string sort (a radix sort).
 */
public final class MSDStringSort {

    /**
     * Sort an array of Strings using MSDStringSort.
     *
     * @param a the array to be sorted.
     */
    public static void sort(final String[] a) {
        final int n = a.length;
        aux = new String[n];
        sort(a, 0, n, 0);
    }

    /**
     * Sort from a[lo] to a[hi] (exclusive), ignoring the first d characters of each String.
     * This method is recursive.
     *
     * @param a  the array to be sorted.
     * @param lo the low index.
     * @param hi the high index (one above the highest actually processed).
     * @param d  the number of characters in each String to be skipped.
     */
    private static void sort(final String[] a, final int lo, final int hi, final int d) {
        assert lo >= 0 : "lo " + lo + " is negative";
        assert hi <= a.length : "hi " + hi + " is out of bounds: " + a.length;
        if (hi < lo + cutoff) insertionSort(a, lo, hi, d);
        else {
            final int[] count = new int[radix + 2];        // Compute frequency counts.
            for (int i = lo; i < hi; i++) {
                final int x = charAt(a[i], d);
                if (x + 2 < 0 || x + 2 >= count.length)
                    throw new SortException("char " + x + " is out of bounds for count array: " + count.length);
                count[x + 2]++;
            }
            for (int r = 0; r < radix + 1; r++)      // Transform counts to indices.
                count[r + 1] += count[r];
            for (int i = lo; i < hi; i++)     // Distribute.
                aux[count[charAt(a[i], d) + 1]++] = a[i];
            // Copy back.
            if (hi - lo >= 0) System.arraycopy(aux, 0, a, lo, hi - lo);
            // Recursively sort for each character value.
            for (int r = 0; r < radix; r++)
                sort(a, lo + count[r], lo + count[r + 1], d + 1);
        }
    }

    private static int charAt(final String s, final int d) {
        if (d < s.length()) return s.charAt(d);
        else return -1;
    }

    private static void insertionSort(final String[] a, final int lo, final int hi, final int d) {
        for (int i = lo; i < hi; i++)
            for (int j = i; j > lo && less(a[j], a[j - 1], d); j--)
                swap(a, j, j - 1);
    }

    private static boolean less(final String v, final String w, final int d) {
        return v.substring(d).compareTo(w.substring(d)) < 0;
    }

    private static void swap(final Object[] a, final int j, final int i) {
        final Object temp = a[j];
        a[j] = a[i];
        a[i] = temp;
    }

    private static final int radix = 256;
    private static final int cutoff = 15;
    private static String[] aux;       // auxiliary array for distribution
}