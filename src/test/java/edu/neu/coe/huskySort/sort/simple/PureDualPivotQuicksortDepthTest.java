package edu.neu.coe.huskySort.sort.simple;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * The dual-pivot baseline used in the adversarial appendix used to fail outright, not merely degrade,
 * on a key space with heavily collapsed high bits.
 * <p>
 * Two-way partitioning around a heavily duplicated pivot goes maximally unbalanced, which costs
 * quadratic <em>time</em>; that much is expected and is the point the appendix makes. But the
 * implementation also recursed on both partitions with no bound, so the depth reached order N and the
 * stack was exhausted. Those are separate faults with separate remedies: three-way partitioning cures
 * the duplicates, and bounding the depth cures the crash. A sort with the second and not the first
 * goes quadratic and survives.
 * <p>
 * The class is a copy of {@code java.util.DualPivotQuicksort} as it stood in 2011, adapted to objects.
 * Later JDKs added exactly this guard to the primitive original, so without it the baseline failed
 * where the algorithm it copies would now merely slow down. That made it a straw man in the one place
 * the paper leans on it hardest.
 * <p>
 * NOTE the guard makes this introsort in the classic sense, as {@link IntroSort} in this package
 * already is. They are not interchangeable here: IntroSort is built on the Helper framework, so
 * substituting it would confound the depth guard with the framework's own overhead. Adding the guard
 * in place isolates the single variable.
 */
public class PureDualPivotQuicksortDepthTest {

    /**
     * The adversarial shape itself, at every setting the appendix reports. Before the guard, the last
     * two threw StackOverflowError.
     */
    @Test
    public void survivesCollapsedHighBits() {
        final Random random = new Random(11);
        for (final int fixedHighBits : new int[]{0, 16, 32, 48, 56, 60, 63}) {
            final long mask = fixedHighBits == 0 ? 0L : (-1L << (64 - fixedHighBits));
            final long fixed = 0x5A5A_5A5A_5A5A_5A5AL & mask;
            final Long[] xs = new Long[200_000];
            for (int i = 0; i < xs.length; i++) xs[i] = (random.nextLong() & ~mask) | fixed;
            assertSortsLikeTheJdk(xs, "fixedHighBits=" + fixedHighBits);
        }
    }

    /**
     * The guard must not disturb the ordinary path, which is what the rest of the paper measures.
     */
    @Test
    public void agreesWithTheJdkOnOrdinaryInput() {
        final Random random = new Random(7);
        for (int trial = 0; trial < 400; trial++) {
            final Integer[] xs = new Integer[1 + random.nextInt(3000)];
            final int spread = 1 + random.nextInt(60);      // deliberately duplicate-heavy
            for (int i = 0; i < xs.length; i++) xs[i] = random.nextInt(spread);
            assertSortsLikeTheJdk(xs, "trial " + trial);
        }
    }

    /**
     * The shapes that break sorts: empty, singleton, all-equal, sorted, reversed, and the sizes either
     * side of the insertion-sort and quicksort thresholds.
     */
    @Test
    public void handlesDegenerateShapes() {
        for (final int n : new int[]{0, 1, 2, 46, 47, 48, 285, 286, 287, 5000}) {
            final Integer[] same = new Integer[n];
            Arrays.fill(same, 7);
            assertSortsLikeTheJdk(same, "all equal, n=" + n);

            final Integer[] ascending = new Integer[n];
            for (int i = 0; i < n; i++) ascending[i] = i;
            assertSortsLikeTheJdk(ascending, "ascending, n=" + n);

            final Integer[] descending = new Integer[n];
            for (int i = 0; i < n; i++) descending[i] = n - i;
            assertSortsLikeTheJdk(descending, "descending, n=" + n);
        }
    }

    /**
     * The fallback is heapsort, which is not stable, but neither was the quicksort it replaces, so
     * this only pins that the multiset survives intact.
     */
    @Test
    public void losesNothingAndInventsNothing() {
        final Random random = new Random(3);
        final Integer[] xs = new Integer[100_000];
        for (int i = 0; i < xs.length; i++) xs[i] = random.nextInt(4);
        final Integer[] expected = xs.clone();
        Arrays.sort(expected);
        final Integer[] actual = xs.clone();
        PureDualPivotQuicksort.sort(actual);
        assertArrayEquals(expected, actual);
        assertEquals(xs.length, actual.length);
    }

    private static <X extends Comparable<X>> void assertSortsLikeTheJdk(final X[] xs, final String what) {
        final X[] expected = xs.clone();
        Arrays.sort(expected);
        final X[] actual = xs.clone();
        PureDualPivotQuicksort.sort(actual);
        assertArrayEquals(what, expected, actual);
    }
}
