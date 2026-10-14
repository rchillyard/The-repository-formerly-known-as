package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class MergeHuskySortTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @Test
    public void testSortString1() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.unicodeCoder);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString2() {
        final MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, MergeHuskySortTest::nextPositiveLongString);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString3() {
        final MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        final int N = 1000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> {
            final int x = r.nextInt(1000000000);
            final BigInteger b = BigInteger.valueOf(x).multiply(BigInteger.valueOf(1000000));
            return b.toString();
        });
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString4() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString5() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Welcome"};
        final MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    @Test
    public void testSortString6() {
        final MergeHuskySort<String> sorter = new MergeHuskySort<>(HuskyCoderFactory.asciiCoder);
        final int N = 32;
        helper.init(N);
        final String[] xs = helper.random(String.class, MergeHuskySortTest::nextPositiveLongString);
        sorter.sort(xs);
        assertTrue("sorted", helper.sorted(xs));
    }

    // ---------- the case no test above reaches: a PERFECT coder ----------

    /**
     * Every test above uses a String coder, and every String coder is imperfect, so
     * {@code sort} always follows the merge with {@code Arrays.sort} and repairs whatever the
     * merge produced. Only a <b>perfect</b> coder takes the early return, and then a defect in the
     * merge reaches the caller as a wrong answer. That is how the bug fixed on 2026-09-30 survived
     * for four years: the merge was corrupting between 4 and 134 slots of an array and no test
     * could see it.
     * <p>
     * {@code longCoder} is the cheapest perfect coder there is -- it returns its argument.
     */
    @Test
    public void sortsCorrectlyWithAPerfectCoder() {
        final Random random = new Random(20260930);
        // Sizes from below the cutoff up past 300: the old defect first appeared at n = 36, and
        // the sizes that break a merge sort are the ones where a split lands awkwardly, so this
        // sweeps every one rather than sampling.
        for (int n = 0; n <= 320; n++)
            for (int spread : new int[]{5, 50, Integer.MAX_VALUE}) {
                final Long[] xs = new Long[n];
                for (int i = 0; i < n; i++) xs[i] = (long) random.nextInt(spread) - spread / 2;
                assertSortsLike("n=" + n + " spread=" + spread, xs);
            }
    }

    @Test
    public void sortsOrderedReversedAndConstantInputWithAPerfectCoder() {
        for (final int n : new int[]{7, 9, 36, 64, 1000, 10_000}) {
            final Long[] up = new Long[n], down = new Long[n], same = new Long[n];
            for (int i = 0; i < n; i++) {
                up[i] = (long) i;
                down[i] = (long) (n - i);
                same[i] = 7L;
            }
            // Ordered input is the case the insurance check short-circuits, so it is the case that
            // caught the bug: the check skipped the copy as well as the comparisons.
            assertSortsLike("ascending n=" + n, up);
            assertSortsLike("descending n=" + n, down);
            assertSortsLike("all equal n=" + n, same);
        }
    }

    /**
     * Negative codes, which {@code longCoder} produces and the String coders never do.
     */
    @Test
    public void sortsNegativeCodesWithAPerfectCoder() {
        final Random random = new Random(11);
        final Long[] xs = new Long[50_000];
        for (int i = 0; i < xs.length; i++) xs[i] = random.nextLong();
        xs[0] = Long.MIN_VALUE;
        xs[1] = Long.MAX_VALUE;
        assertSortsLike("random long", xs);
    }

    /**
     * Asserts the full result, not merely that it ascends. The failure mode being guarded against
     * duplicates some elements and drops others, which leaves an array that is perfectly sorted
     * and simply not a permutation of its input -- so {@code helper.sorted(xs)}, which every test
     * above uses, would pass.
     */
    private static void assertSortsLike(final String label, final Long[] master) {
        final Long[] xs = Arrays.copyOf(master, master.length);
        final Long[] expected = Arrays.copyOf(master, master.length);
        Arrays.sort(expected);
        new MergeHuskySort<>(HuskyCoderFactory.longCoder).sort(xs);
        assertArrayEquals(label, expected, xs);
    }

    private static String nextPositiveLongString(final Random r) {
        final long l = r.nextLong();
        final long result = l >= 0L ? l : l == Long.MIN_VALUE ? 0L : -l;
        return String.format("%19d", result);
    }
}
