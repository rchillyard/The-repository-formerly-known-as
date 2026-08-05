/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

/**
 * Three-way radix quicksort, also called multikey quicksort, for String arrays, per Bentley and
 * Sedgewick, "Fast Algorithms for Sorting and Searching Strings," SODA 1997.
 * <p>
 * Partitions on one character at a time via a three-way (Dutch national flag) split around a
 * pivot character at the current depth: strings whose character at that depth is less than,
 * equal to, or greater than the pivot's. The less-than and greater-than partitions recurse at the
 * same depth; the equal partition recurses at depth + 1, since every string in it has already been
 * confirmed to share the same character at every depth up to and including this one. A string
 * that has been fully consumed (its length equals the current depth) is treated as having a
 * character below every real character code, so shorter strings sort before longer strings that
 * share the same prefix, matching ordinary lexicographic order.
 * <p>
 * Falls back to {@link InsertionSort} for small subarrays, per Bentley and Sedgewick's own
 * recommendation, since per-character partitioning has more overhead than directly comparing
 * whole strings once a subarray is small -- and since every string in a subarray at this point
 * already shares the same prefix up to the current depth, comparing whole strings (rather than
 * just their suffixes from the current depth on) still produces the correct order, just with a
 * small amount of redundant prefix comparison.
 */
public class MultikeyQuicksort {

    /**
     * Sort the given array of Strings in place.
     *
     * @param a the array to be sorted.
     */
    public static void sort(final String[] a) {
        sort(a, 0, a.length - 1, 0);
    }

    private static void sort(final String[] a, final int lo, final int hi, final int d) {
        if (hi - lo < CUTOFF) {
            if (hi > lo) new InsertionSort<String>().sort(a, lo, hi + 1);
            return;
        }
        final int pivot = charAt(a[lo], d);
        int lt = lo, gt = hi, i = lo + 1;
        while (i <= gt) {
            final int t = charAt(a[i], d);
            if (t < pivot) swap(a, lt++, i++);
            else if (t > pivot) swap(a, i, gt--);
            else i++;
        }
        sort(a, lo, lt - 1, d);
        if (pivot >= 0) sort(a, lt, gt, d + 1);
        sort(a, gt + 1, hi, d);
    }

    /**
     * The character of s at position d, or -1 if d is at or beyond the end of s. The sentinel -1
     * is below every actual character code (all of which are non-negative), so a string that has
     * ended sorts before any string that has a real character at this depth, matching ordinary
     * lexicographic order.
     */
    private static int charAt(final String s, final int d) {
        return d < s.length() ? s.charAt(d) : -1;
    }

    private static void swap(final String[] a, final int i, final int j) {
        final String temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private static final int CUTOFF = 16;
}
