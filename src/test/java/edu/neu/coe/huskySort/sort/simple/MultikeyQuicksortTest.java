/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

/**
 * Correctness checks for MultikeyQuicksort (three-way radix quicksort, Bentley and Sedgewick
 * 1997), added as a new baseline for the paper's classic string-sorting literature comparison.
 * Verified against Arrays.sort as ground truth throughout, per this repo's established practice
 * of not trusting a new sort implementation until it has been checked this way.
 */
public class MultikeyQuicksortTest {

    @Test
    public void sortsRandomAsciiStringsCorrectly() {
        final Random random = new Random(1);
        for (final int n : new int[]{0, 1, 2, 3, 10, 100, 1000, 10_000}) {
            for (int trial = 0; trial < 5; trial++) {
                final String[] xs = randomStrings(random, n, 0, 20, 'a', 'z');
                final String[] expected = Arrays.copyOf(xs, n);
                Arrays.sort(expected);
                MultikeyQuicksort.sort(xs);
                assertArrayEquals("n=" + n + " trial=" + trial, expected, xs);
            }
        }
    }

    @Test
    public void sortsHeavyDuplicatesCorrectly() {
        final Random random = new Random(2);
        // A tiny alphabet and short max length guarantees many exact-duplicate strings.
        for (final int n : new int[]{10, 100, 1000, 10_000}) {
            final String[] xs = randomStrings(random, n, 0, 3, 'a', 'c');
            final String[] expected = Arrays.copyOf(xs, n);
            Arrays.sort(expected);
            MultikeyQuicksort.sort(xs);
            assertArrayEquals("n=" + n, expected, xs);
        }
    }

    @Test
    public void sortsVaryingLengthStringsIncludingPrefixesOfEachOther() {
        final String[] xs = {"banana", "ban", "ba", "b", "", "bandana", "banana", "banan"};
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        MultikeyQuicksort.sort(xs);
        assertArrayEquals(expected, xs);
    }

    @Test
    public void sortsEmptyAndSingletonArrays() {
        final String[] empty = {};
        MultikeyQuicksort.sort(empty);
        assertArrayEquals(new String[]{}, empty);

        final String[] singleton = {"x"};
        MultikeyQuicksort.sort(singleton);
        assertArrayEquals(new String[]{"x"}, singleton);
    }

    @Test
    public void sortsAllIdenticalStringsCorrectly() {
        final String[] xs = new String[5000];
        Arrays.fill(xs, "same");
        final String[] expected = Arrays.copyOf(xs, xs.length);
        MultikeyQuicksort.sort(xs);
        assertArrayEquals(expected, xs);
    }

    /**
     * Adversarial case matching this document's earlier "shared long common prefix" scenario:
     * every string shares the same first 5,000 characters before differing. A naive recursive
     * implementation with no depth limit recurses once per shared character in the "equal"
     * partition, so this also checks that 5,000 shared characters does not overflow the stack.
     */
    @Test
    public void sortsLongSharedPrefixStringsCorrectly() {
        final Random random = new Random(3);
        final String prefix = "a".repeat(5000);
        final String[] xs = new String[500];
        for (int i = 0; i < xs.length; i++) xs[i] = prefix + random.nextInt(1000);
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        MultikeyQuicksort.sort(xs);
        assertArrayEquals(expected, xs);
    }

    private static String[] randomStrings(final Random random, final int n, final int minLen, final int maxLen, final char from, final char to) {
        final String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            final int len = minLen + random.nextInt(maxLen - minLen + 1);
            final StringBuilder sb = new StringBuilder(len);
            for (int j = 0; j < len; j++) sb.append((char) (from + random.nextInt(to - from + 1)));
            result[i] = sb.toString();
        }
        return result;
    }
}
