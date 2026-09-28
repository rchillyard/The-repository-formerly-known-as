package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.REGEX_LEIPZIG;
import static edu.neu.coe.huskySort.sort.huskySort.HuskySortBenchmarkHelper.REGEX_STRING_SPLITTER;
import static org.junit.Assert.*;

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
public class RadixHuskySortIntegrationTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @BeforeClass
    public static void before() throws IOException {
        config = Config.load(RadixHuskySortIntegrationTest.class);
    }

    private static Config config;

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
}
