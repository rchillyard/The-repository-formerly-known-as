package edu.neu.coe.huskySort.sort.simple;

import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests for AdaptiveInsertionSort, whose reason for existing is that its cost is (n-1) + X
 * comparisons rather than n log n -- so the tests check the adaptivity as well as the ordering.
 */
public class AdaptiveInsertionSortTest {

    @Test
    public void testSortsRandomArray() {
        final Random random = new Random(42);
        for (final int n : new int[]{0, 1, 2, 3, 17, 500, 2000}) {
            final Integer[] xs = new Integer[n];
            for (int i = 0; i < n; i++) xs[i] = random.nextInt(1000);
            final Integer[] expected = Arrays.copyOf(xs, n);
            Arrays.sort(expected);
            AdaptiveInsertionSort.mutatingAdaptiveInsertionSort(xs);
            assertArrayEquals("n=" + n, expected, xs);
        }
    }

    @Test
    public void testSortsAlreadySortedAndReversed() {
        final int n = 1000;
        final Integer[] ascending = new Integer[n];
        final Integer[] descending = new Integer[n];
        for (int i = 0; i < n; i++) {
            ascending[i] = i;
            descending[i] = n - i;
        }
        final Integer[] expectedAscending = Arrays.copyOf(ascending, n);
        final Integer[] expectedDescending = Arrays.copyOf(descending, n);
        Arrays.sort(expectedDescending);
        AdaptiveInsertionSort.mutatingAdaptiveInsertionSort(ascending);
        AdaptiveInsertionSort.mutatingAdaptiveInsertionSort(descending);
        assertArrayEquals(expectedAscending, ascending);
        assertArrayEquals(expectedDescending, descending);
    }

    /**
     * The comparator overload, which the cleanup pass needs wherever the husky coder's ordering is
     * not the natural one. A pinyin sort cleaned up by natural ordering is sorted by the wrong
     * thing, which is the bug this overload exists to prevent.
     */
    @Test
    public void testSortsByComparator() {
        final String[] xs = {"刘持平", "洪文胜", "樊辉辉", "苏会敏", "高民政", "曹玉德", "袁继鹏", "舒冬梅"};
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected, HuskyCoderChinesePinyin.NAME_ORDER);
        final String[] actual = Arrays.copyOf(xs, xs.length);
        AdaptiveInsertionSort.sort(actual, HuskyCoderChinesePinyin.NAME_ORDER);
        assertArrayEquals(expected, actual);
        assertFalse("the pinyin ordering should differ from the natural one here, or this proves nothing",
                Arrays.equals(expected, sortedNaturally(xs)));
    }

    private static String[] sortedNaturally(final String[] xs) {
        final String[] copy = Arrays.copyOf(xs, xs.length);
        Arrays.sort(copy);
        return copy;
    }

    /**
     * Stability: equal keys must keep their original relative order, since the cleanup pass runs
     * after a stable radix phase and must not undo it.
     */
    @Test
    public void testIsStable() {
        final int n = 2000;
        final Random random = new Random(42);
        final Tagged[] xs = new Tagged[n];
        for (int i = 0; i < n; i++) xs[i] = new Tagged(random.nextInt(20), i);
        AdaptiveInsertionSort.sort(xs, Comparator.comparingInt(t -> t.key));
        for (int i = 1; i < n; i++) {
            assertTrue("not sorted at " + i, xs[i - 1].key <= xs[i].key);
            if (xs[i - 1].key == xs[i].key)
                assertTrue("stability violated at " + i, xs[i - 1].tag < xs[i].tag);
        }
    }

    /**
     * The whole point: comparisons must scale with the number of inversions, not with n log n. A
     * fully ordered array costs exactly n-1 comparisons; the binary-search InsertionSort would cost
     * n log n on the same input.
     */
    @Test
    public void testComparisonsAreAdaptive() {
        final int n = 4096;
        final Integer[] ascending = new Integer[n];
        for (int i = 0; i < n; i++) ascending[i] = i;
        final int[] comparisons = {0};
        final Comparator<Integer> counting = (a, b) -> {
            comparisons[0]++;
            return Integer.compare(a, b);
        };
        AdaptiveInsertionSort.sort(ascending, counting);
        assertEquals("an ordered array of " + n + " costs exactly n-1 comparisons", n - 1, comparisons[0]);

        // One transposition adds one inversion, and so one comparison beyond the n-1 baseline.
        final Integer[] oneSwap = new Integer[n];
        for (int i = 0; i < n; i++) oneSwap[i] = i;
        final Integer t = oneSwap[100];
        oneSwap[100] = oneSwap[101];
        oneSwap[101] = t;
        comparisons[0] = 0;
        AdaptiveInsertionSort.sort(oneSwap, counting);
        assertEquals("one inversion costs one comparison more", n, comparisons[0]);
    }

    private static final class Tagged {
        final int key;
        final int tag;

        Tagged(final int key, final int tag) {
            this.key = key;
            this.tag = tag;
        }
    }
}
