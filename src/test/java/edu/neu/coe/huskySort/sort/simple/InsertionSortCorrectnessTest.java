/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.simple;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Rigorous correctness checks for the binary-search-based InsertionSort.sort (swapIntoSorted),
 * added after Robin was suspicious of a 4-10x JMH speedup measured for it relative to the old
 * linear-scan implementation (2026-08-05). The existing InsertionSortTest only ever sorts 4 or
 * 128 elements and never checks stability directly, so none of it would have caught a
 * correctness regression large enough to produce a spurious speedup.
 */
public class InsertionSortCorrectnessTest {

    @Test
    public void sortsLargeRandomArraysCorrectly() {
        final Random random = new Random(1);
        for (final int n : new int[]{0, 1, 2, 3, 10, 100, 1000, 10_000, 50_000}) {
            for (int trial = 0; trial < 5; trial++) {
                final Integer[] xs = new Integer[n];
                for (int i = 0; i < n; i++) xs[i] = random.nextInt();
                final Integer[] expected = Arrays.copyOf(xs, n);
                Arrays.sort(expected);
                InsertionSort.mutatingInsertionSort(xs);
                assertArrayEquals("n=" + n + " trial=" + trial, expected, xs);
            }
        }
    }

    @Test
    public void sortsHeavyDuplicatesCorrectly() {
        final Random random = new Random(2);
        for (final int n : new int[]{10, 100, 1000, 10_000}) {
            // A range much narrower than n guarantees many duplicate keys.
            for (final int range : new int[]{1, 2, 5, 20}) {
                final Integer[] xs = new Integer[n];
                for (int i = 0; i < n; i++) xs[i] = random.nextInt(range);
                final Integer[] expected = Arrays.copyOf(xs, n);
                Arrays.sort(expected);
                InsertionSort.mutatingInsertionSort(xs);
                assertArrayEquals("n=" + n + " range=" + range, expected, xs);
            }
        }
    }

    @Test
    public void sortsAlreadySortedAndReverseSortedArrays() {
        for (final int n : new int[]{0, 1, 2, 10, 1000, 10_000}) {
            final Integer[] ascending = new Integer[n];
            final Integer[] descending = new Integer[n];
            for (int i = 0; i < n; i++) {
                ascending[i] = i;
                descending[i] = n - i;
            }
            final Integer[] expectedAscending = Arrays.copyOf(ascending, n);
            InsertionSort.mutatingInsertionSort(ascending);
            assertArrayEquals("ascending n=" + n, expectedAscending, ascending);

            final Integer[] expectedDescending = Arrays.copyOf(descending, n);
            Arrays.sort(expectedDescending);
            InsertionSort.mutatingInsertionSort(descending);
            assertArrayEquals("descending n=" + n, expectedDescending, descending);
        }
    }

    @Test
    public void sortsAllEqualArrayCorrectly() {
        for (final int n : new int[]{0, 1, 2, 10, 1000, 10_000}) {
            final Integer[] xs = new Integer[n];
            Arrays.fill(xs, 42);
            final Integer[] expected = Arrays.copyOf(xs, n);
            InsertionSort.mutatingInsertionSort(xs);
            assertArrayEquals("n=" + n, expected, xs);
        }
    }

    /**
     * Explicit stability check. swapIntoSorted's binary search can find an exact match anywhere
     * within a run of equal keys; the fix scans past ties before shifting, specifically so equal
     * elements are never moved past each other. Verify that directly, rather than only checking
     * that the final key order is correct, since a stability regression would not otherwise show
     * up in any of the other tests here (Integer has no notion of identity beyond its value).
     */
    @Test
    public void isStableForDuplicateKeys() {
        final Random random = new Random(3);
        final int n = 5000;
        final Tagged[] xs = new Tagged[n];
        for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(20), i);
        InsertionSort.mutatingInsertionSort(xs);
        for (int i = 1; i < n; i++) {
            assertTrue("key order violated at index " + i, xs[i - 1].key <= xs[i].key);
            if (xs[i - 1].key == xs[i].key)
                assertTrue("stability violated at index " + i + ": originalIndex " +
                                xs[i - 1].originalIndex + " should precede " + xs[i].originalIndex,
                        xs[i - 1].originalIndex < xs[i].originalIndex);
        }
    }

    private static final class Tagged implements Comparable<Tagged> {
        final int key;
        final int originalIndex;

        Tagged(final int key, final int originalIndex) {
            this.key = key;
            this.originalIndex = originalIndex;
        }

        public int compareTo(final Tagged other) {
            return Integer.compare(key, other.key);
        }
    }
}
