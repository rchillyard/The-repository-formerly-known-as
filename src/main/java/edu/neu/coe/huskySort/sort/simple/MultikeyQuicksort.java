/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;

import java.util.Arrays;

/**
 * Three-way radix quicksort, also called multikey quicksort, for String arrays, per Bentley and
 * Sedgewick, "Fast Algorithms for Sorting and Searching Strings," SODA 1997.
 * <p>
 * Partitions on one character at a time via a three-way (Dutch national flag) split around a
 * pivot key at the current depth: strings whose key at that depth is less than, equal to, or
 * greater than the pivot's. The less-than and greater-than partitions recurse at the same depth;
 * the equal partition recurses at depth + 1, since every string in it has already been confirmed
 * to share the same key at every depth up to and including this one. A string that has been
 * fully consumed (its length equals the current depth) is treated as having a key below every
 * real key, so shorter strings sort before longer strings that share the same prefix.
 * <p>
 * The per-position key is pluggable, via {@link CharacterKey}: {@link #sort(String[])} keys on
 * the raw Unicode character (ordinary lexicographic order), while {@link #sortByPinyin(String[])}
 * keys on pinyin syllable/tone (see {@link HuskyCoderChinesePinyin#pinyinCharacterKey}) so that
 * "depth" still advances one Han character at a time, but the order at each depth is pinyin
 * order rather than raw code-point order. The underlying partitioning logic does not otherwise
 * change between the two; only the key and the small-subarray fallback (see {@link RangeSorter})
 * differ, since the fallback must agree with whichever order the key function establishes.
 * <p>
 * Falls back to a simpler sort for small subarrays, per Bentley and Sedgewick's own
 * recommendation that small subfiles be handled that way once per-character partitioning
 * overhead dominates. Every string in a subarray at this point already shares the same prefix up
 * to the current depth, so comparing whole strings (rather than just their suffixes from the
 * current depth on) still produces the correct order, just with a small amount of redundant
 * prefix comparison.
 */
public class MultikeyQuicksort {

    /**
     * Sort the given array of Strings in place, by ordinary lexicographic (Unicode code point)
     * order.
     *
     * @param a the array to be sorted.
     */
    public static void sort(final String[] a) {
        sort(a, MultikeyQuicksort::codePointKeyAt, MultikeyQuicksort::insertionSortRange);
    }

    /**
     * Sort the given array of Chinese-character Strings in place, by pinyin order (syllable,
     * then tone, then Unicode code point as a final tie-break for true homonyms -- see
     * {@link HuskyCoderChinesePinyin#pinyinCharacterKey}), one Han character per depth.
     *
     * @param a the array to be sorted.
     */
    public static void sortByPinyin(final String[] a) {
        sort(a, MultikeyQuicksort::pinyinKeyAt, (arr, lo, hiExclusive) -> Arrays.sort(arr, lo, hiExclusive, HuskyCoderChinesePinyin.NAME_ORDER));
    }

    private static void sort(final String[] a, final CharacterKey keyAt, final RangeSorter smallRangeSorter) {
        sort(a, 0, a.length - 1, 0, keyAt, smallRangeSorter);
    }

    /**
     * NOTE: the equal partition below is handled by looping back around (updating lo, hi, and d
     * and re-entering the method) rather than by a direct recursive call to sort(lo, gt, d + 1,
     * ...). A long run of strings sharing a common prefix recurses into that equal partition
     * once per shared character, and a direct recursive call would consume one stack frame per
     * character -- exactly the kind of unbounded-recursion-depth failure this document's own
     * "Adversarial inputs" discussion describes for a naively-implemented quicksort. Looping
     * instead keeps the equal partition at constant stack depth regardless of how long a shared
     * prefix is; only the less-than and greater-than partitions still recurse, and their depth
     * is bounded by how many times the array can be partitioned, not by prefix length.
     */
    private static void sort(final String[] a, int lo, int hi, int d, final CharacterKey keyAt, final RangeSorter smallRangeSorter) {
        while (true) {
            if (hi - lo < CUTOFF) {
                if (hi > lo) smallRangeSorter.sort(a, lo, hi + 1);
                return;
            }
            final long pivot = keyAt.at(a[lo], d);
            int lt = lo, gt = hi, i = lo + 1;
            while (i <= gt) {
                final long t = keyAt.at(a[i], d);
                if (t < pivot) swap(a, lt++, i++);
                else if (t > pivot) swap(a, i, gt--);
                else i++;
            }
            sort(a, lo, lt - 1, d, keyAt, smallRangeSorter);
            sort(a, gt + 1, hi, d, keyAt, smallRangeSorter);
            if (pivot < 0) return;
            lo = lt;
            hi = gt;
            d = d + 1;
        }
    }

    private static void insertionSortRange(final String[] a, final int lo, final int hiExclusive) {
        new InsertionSort<String>().sort(a, lo, hiExclusive);
    }

    /**
     * The per-position key used to partition strings during the sort: the key of s at depth d,
     * or a value below every real key if d is at or beyond the end of s (see {@link #sort(String[])}
     * and {@link #sortByPinyin(String[])} for the two keys currently provided).
     */
    @FunctionalInterface
    private interface CharacterKey {
        long at(String s, int d);
    }

    /**
     * The sort used to finish off a small subarray once per-character partitioning overhead
     * dominates. Must agree with whichever order the {@link CharacterKey} in use establishes --
     * natural order for {@link #codePointKeyAt}, pinyin order for {@link #pinyinKeyAt} -- since
     * every string in the subarray shares the same prefix and only the remaining, unpartitioned
     * suffix order matters.
     */
    @FunctionalInterface
    private interface RangeSorter {
        void sort(String[] a, int lo, int hiExclusive);
    }

    /**
     * The raw Unicode character of s at position d, or -1 if d is at or beyond the end of s. The
     * sentinel -1 is below every actual character code (all of which are non-negative), so a
     * string that has ended sorts before any string that has a real character at this depth,
     * matching ordinary lexicographic order.
     */
    private static long codePointKeyAt(final String s, final int d) {
        return d < s.length() ? s.charAt(d) : -1L;
    }

    /**
     * The pinyin key (see {@link HuskyCoderChinesePinyin#pinyinCharacterKey}) of the character
     * of s at position d, or -1 if d is at or beyond the end of s. Packed pinyin keys are always
     * non-negative, so -1 remains a safe sentinel below every real key, same as {@link #codePointKeyAt}.
     */
    private static long pinyinKeyAt(final String s, final int d) {
        return d < s.length() ? HuskyCoderChinesePinyin.pinyinCharacterKey(s.charAt(d)) : -1L;
    }

    private static void swap(final String[] a, final int i, final int j) {
        final String temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private static final int CUTOFF = 16;
}
