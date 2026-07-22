package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Correctness tests for RadixHuskySort.
 * <p>
 * NOTE: the task brief that motivated this class specifically calls out a bug found in an early
 * prototype quicksort (an off-by-one in the partition recursion bounds) that silently dropped
 * elements near the pivot, and which only showed up under stress-testing many small random
 * arrays. These tests therefore check exact ordering against a trusted reference sort (not just
 * that the result "looks sorted"), across a range of small sizes, and across a sweep of digit
 * widths, including one (11) that does not evenly divide 64.
 */
public class RadixHuskySortTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @BeforeClass
    public static void before() throws IOException {
        config = Config.load(RadixHuskySortTest.class);
    }

    private static Config config;

    @Test
    public void testSortStringSmall() {
        final String[] xs = {"Hello", "Goodbye", "Ciao", "Willkommen"};
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.unicodeCoder, config);
        final String[] ys = sorter.sort(xs);
        assertTrue("sorted", helper.sorted(ys));
    }

    @Test
    public void testSortStringRandom() {
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        final int N = 2000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> r.nextLong() + "");
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        final String[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testSortStringPaddedBigInteger() {
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        final int N = 2000;
        helper.init(N);
        final String[] xs = helper.random(String.class, r -> {
            final int x = r.nextInt(1000000000);
            final BigInteger b = BigInteger.valueOf(x).multiply(BigInteger.valueOf(1000000));
            return b.toString();
        });
        final String[] expected = Arrays.copyOf(xs, xs.length);
        Arrays.sort(expected);
        final String[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    /**
     * Stress-test on many small random arrays, across a sweep of digit widths (including one,
     * 11, that does not evenly divide 64), comparing against Arrays.sort as the trusted reference.
     * This is the JUnit formalization of the ad hoc stress-test loop in the original prototype
     * (RadixVsQuickBenchmark.java), which is what caught the original quicksort bug.
     */
    @Test
    public void testStressSmallLongArrays() {
        final Random r = new Random(7);
        for (final int digitBits : new int[]{1, 3, 7, 8, 11, 16, 20}) {
            for (int trial = 0; trial < 500; trial++) {
                final int n = 1 + r.nextInt(200);
                final Long[] xs = new Long[n];
                for (int i = 0; i < n; i++) xs[i] = r.nextLong();
                final Long[] expected = Arrays.copyOf(xs, n);
                Arrays.sort(expected);

                final RadixHuskySort<Long> sorter = new RadixHuskySort<>(digitBits, HuskyCoderFactory.longCoder, config);
                final Long[] ys = sorter.sort(xs);
                assertArrayEquals("digitBits=" + digitBits + ", n=" + n, expected, ys);
            }
        }
    }

    @Test
    public void testNegativeAndPositiveLongs() {
        final Random r = new Random(42);
        final int N = 5000;
        final Long[] xs = new Long[N];
        for (int i = 0; i < N; i++) xs[i] = r.nextLong();
        final Long[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Long> sorter = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        final Long[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testNegativeAndPositiveIntegers() {
        final Random r = new Random(43);
        final int N = 5000;
        final Integer[] xs = new Integer[N];
        for (int i = 0; i < N; i++) xs[i] = r.nextInt();
        final Integer[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Integer> sorter = new RadixHuskySort<>(11, HuskyCoderFactory.integerCoder, config);
        final Integer[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testNegativeAndPositiveDoubles() {
        final Random r = new Random(44);
        final int N = 5000;
        final Double[] xs = new Double[N];
        for (int i = 0; i < N; i++) xs[i] = (r.nextDouble() - 0.5) * 2 * Long.MAX_VALUE;
        final Double[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Double> sorter = new RadixHuskySort<>(16, HuskyCoderFactory.doubleCoder, config);
        final Double[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testAdversarialCollapsedHighBits() {
        // NOTE: all keys share the same top 56 bits (differ only in the low 8 bits), so if the
        // radix sort had a bug specific to skewed/collapsed high-order digits (per Reviewer 4's
        // concern about adversarial inputs), it would show up here.
        final Random r = new Random(99);
        final int N = 3000;
        final Long[] xs = new Long[N];
        for (int i = 0; i < N; i++) xs[i] = 0x1234_5678_9ABC_DE00L | (r.nextInt(256) & 0xFFL);
        final Long[] expected = Arrays.copyOf(xs, N);
        Arrays.sort(expected);

        final RadixHuskySort<Long> sorter = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        final Long[] ys = sorter.sort(xs);
        assertArrayEquals(expected, ys);
    }

    @Test
    public void testAlreadySortedAndReverseSorted() {
        final int N = 1000;
        final Long[] ascending = new Long[N];
        for (int i = 0; i < N; i++) ascending[i] = (long) i;
        final Long[] descending = new Long[N];
        for (int i = 0; i < N; i++) descending[i] = (long) (N - i);

        final RadixHuskySort<Long> sorter1 = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        assertArrayEquals(ascending, sorter1.sort(Arrays.copyOf(ascending, N)));

        final RadixHuskySort<Long> sorter2 = new RadixHuskySort<>(HuskyCoderFactory.longCoder, config);
        final Long[] expectedFromDescending = Arrays.copyOf(descending, N);
        Arrays.sort(expectedFromDescending);
        assertArrayEquals(expectedFromDescending, sorter2.sort(descending));
    }

    @Test
    public void testEmptyAndSingleton() {
        final RadixHuskySort<String> sorter = new RadixHuskySort<>(HuskyCoderFactory.asciiCoder, config);
        assertArrayEquals(new String[0], sorter.sort(new String[0]));
        assertArrayEquals(new String[]{"one"}, sorter.sort(new String[]{"one"}));
    }
}
