/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.util.Config;

import java.text.Collator;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * HuskySort variant which sorts the husky-coded longs using LSD radix sort with a deferred
 * permutation, rather than swapping the payload objects at every key exchange during the
 * linearithmic phase (as IntroHuskySort/DutchHuskySort do).
 * <p>
 * This addresses a question raised by a reviewer of the original Huskysort paper
 * (arXiv:2012.00866): why not use radix sort -- which is O(N) rather than O(N log N) -- on the
 * fixed-width husky codes? The longs are sorted via counting sort, one digit (of
 * {@code digitBits} width) at a time, from least- to most-significant, carrying only a cheap
 * {@code int[]} index array through the passes rather than the (possibly heavy) payload. The
 * payload array is then permuted into its final order in a single O(N) pass at the end.
 *
 * @param <X> the underlying type of the elements to be sorted.
 */
public final class RadixHuskySort<X extends Comparable<X>> extends AbstractHuskySort<X> {

    /**
     * The default digit width, in bits, of each radix-sort pass.
     */
    public static final int DEFAULT_DIGIT_BITS = 8;

    /**
     * Primary constructor.
     *
     * @param name       the name of the sorter (used by the helper).
     * @param n          the number of elements to be sorted (may be 0 if unknown).
     * @param digitBits  the width, in bits, of each radix-sort digit/pass (e.g. 8, 11, 16).
     * @param huskyCoder the Husky coder.
     * @param postSorter the post-sorter which will fix any remaining inversions.
     * @param config     the configuration.
     */
    public RadixHuskySort(final String name, final int n, final int digitBits, final HuskyCoder<X> huskyCoder, final Consumer<X[]> postSorter, final Config config) {
        super(name, n, huskyCoder, postSorter, config);
        // NOTE: digitBits is capped well below 32 because the bucket count (1 << digitBits) grows
        // exponentially -- at 20 bits that's already a 4M-entry (16MB) count array per pass -- and
        // because "1 << 32" silently wraps around to 1 in Java (int shift amounts are taken mod 32).
        if (digitBits < 1 || digitBits > 20) throw new IllegalArgumentException("digitBits must be between 1 and 20: " + digitBits);
        this.digitBits = digitBits;
    }

    /**
     * Secondary constructor: the number of elements is unknown, and the post-sorter is the
     * System sort -- using huskyCoder's Collator if it supplies one (e.g.
     * HuskyCoderChinesePinyin), falling back to natural ordering otherwise. NOTE: this fixes a
     * real bug found 2026-07-24 -- this constructor previously hardcoded {@code Arrays::sort}
     * regardless of huskyCoder.getCollator(), silently producing natural-order (not
     * Collator-order) results whenever the cleanup pass actually ran for a Collator-supplying
     * coder. QuickHuskySort already got this right; RadixHuskySort did not.
     *
     * @param digitBits  the width, in bits, of each radix-sort digit/pass.
     * @param huskyCoder the Husky coder.
     * @param config     the configuration.
     */
    public RadixHuskySort(final int digitBits, final HuskyCoder<X> huskyCoder, final Config config) {
        this("RadixHuskySort/" + digitBits, 0, digitBits, huskyCoder, defaultPostSorter(huskyCoder), config);
    }

    private static <Y extends Comparable<Y>> Consumer<Y[]> defaultPostSorter(final HuskyCoder<Y> huskyCoder) {
        final Collator collator = huskyCoder.getCollator();
        return collator == null ? Arrays::sort : xs -> Arrays.sort(xs, collator);
    }

    /**
     * Secondary constructor which uses the default digit width ({@value #DEFAULT_DIGIT_BITS} bits).
     *
     * @param huskyCoder the Husky coder.
     * @param config     the configuration.
     */
    public RadixHuskySort(final HuskyCoder<X> huskyCoder, final Config config) {
        this(DEFAULT_DIGIT_BITS, huskyCoder, config);
    }

    /**
     * The primary sort method: LSD radix sort on the husky-coded longs, followed by a single
     * pass to permute the payload array into the same order.
     *
     * @param xs   sort the array xs from "from" until "to" (exclusive of to).
     * @param from the index of the first element to sort.
     * @param to   the index of the first element not to sort.
     */
    @Override
    public void sort(final X[] xs, final int from, final int to) {
        final int n = to - from;
        if (n < 2) return;
        final long[] longs = getHelper().getLongs();
        final int[] permutation = radixSortIndices(longs, from, n, digitBits);
        applyPermutation(xs, longs, from, n, permutation);
    }

    /**
     * Method to determine the permutation which puts longs[from..from+n) into ascending order,
     * via LSD radix sort with digitBits-wide digits, without moving any payload.
     * <p>
     * NOTE: longs are biased by flipping the sign bit so that unsigned digit-wise comparison of
     * the biased value matches signed comparison of the original value. This is a no-op on
     * relative order when all values happen to be non-negative (as for the String coders), and
     * is required to get correct results when values may be negative (as for the numeric and
     * Date/time coders) -- so it is applied unconditionally rather than relying on the coder to
     * declare its sign range.
     *
     * @param longs     the array of longs to consider (only the range [from, from+n) is read).
     * @param from      the index of the first long to consider.
     * @param n         the number of longs to consider.
     * @param digitBits the width, in bits, of each digit/pass.
     * @return an array of n indices (each relative to "from") giving the order in which the
     * original elements should appear so that the corresponding longs are sorted ascending.
     */
    private static int[] radixSortIndices(final long[] longs, final int from, final int n, final int digitBits) {
        final int buckets = 1 << digitBits;
        final int mask = buckets - 1;

        long[] biased = new long[n];
        for (int i = 0; i < n; i++) biased[i] = longs[from + i] ^ Long.MIN_VALUE;

        int[] index = new int[n];
        for (int i = 0; i < n; i++) index[i] = i;

        long[] biasedBuffer = new long[n];
        int[] indexBuffer = new int[n];
        final int[] count = new int[buckets + 1];

        for (int shift = 0; shift < Long.SIZE; shift += digitBits) {
            Arrays.fill(count, 0);
            for (int i = 0; i < n; i++) count[(int) ((biased[i] >>> shift) & mask) + 1]++;
            for (int b = 0; b < buckets; b++) count[b + 1] += count[b];
            for (int i = 0; i < n; i++) {
                final int b = (int) ((biased[i] >>> shift) & mask);
                final int pos = count[b]++;
                biasedBuffer[pos] = biased[i];
                indexBuffer[pos] = index[i];
            }
            final long[] tempBiased = biased;
            biased = biasedBuffer;
            biasedBuffer = tempBiased;
            final int[] tempIndex = index;
            index = indexBuffer;
            indexBuffer = tempIndex;
        }
        return index;
    }

    /**
     * Method to apply the given permutation to xs[from..from+n) (and, for consistency, to the
     * corresponding range of longs) in a single O(N) pass.
     *
     * @param xs          the payload array to be permuted in place.
     * @param longs       the array of longs corresponding to xs (kept in sync for consistency).
     * @param from        the index of the first element to permute.
     * @param n           the number of elements to permute.
     * @param permutation an array of n indices (each relative to "from") such that, for each i,
     *                    the element currently at from + permutation[i] should end up at from + i.
     */
    private static <Y> void applyPermutation(final Y[] xs, final long[] longs, final int from, final int n, final int[] permutation) {
        final Y[] sourceObjects = Arrays.copyOfRange(xs, from, from + n);
        final long[] sourceLongs = Arrays.copyOfRange(longs, from, from + n);
        for (int i = 0; i < n; i++) {
            final int sourceIndex = permutation[i];
            xs[from + i] = sourceObjects[sourceIndex];
            longs[from + i] = sourceLongs[sourceIndex];
        }
    }

    private final int digitBits;
}
