package edu.neu.coe.huskySort.sort.huskySort;

import edu.neu.coe.huskySort.sort.ComparableSortHelper;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoder;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderChinesePinyin;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyCoderFactory;
import edu.neu.coe.huskySort.sort.huskySortUtils.HuskyHelper;
import edu.neu.coe.huskySort.util.Config;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Correctness tests for ParallelRadixHuskySort.
 * <p>
 * Parallelizing the digit passes introduces a new class of bug that RadixHuskySortTest's
 * sequential tests cannot catch: an off-by-one in the per-chunk histogram-to-offset combine
 * step, which would show up only when the chunk boundaries actually split runs of equal or
 * adjacent keys in a way that a single-threaded implementation never exercises. So, on top of
 * mirroring RadixHuskySortTest's correctness sweep, every test here also sweeps a range of
 * chunk/thread counts -- including 1 (degenerate, single chunk), a count that does not evenly
 * divide n, and a count far larger than what MIN_CHUNK_SIZE would ever pick on its own -- against
 * the same trusted reference (Arrays.sort), not just "looks sorted".
 */
public class ParallelRadixHuskySortIntegrationTest {

    private final ComparableSortHelper<String> helper = new ComparableSortHelper<>("dummy helper");

    @BeforeClass
    public static void before() throws IOException {
        config = Config.load(ParallelRadixHuskySortIntegrationTest.class);
    }

    private static Config config;

    private static <X extends Comparable<X>> ParallelRadixHuskySort<X> newSorter(final int digitBits, final int parallelism, final HuskyCoder<X> coder) {
        return new ParallelRadixHuskySort<>("test", 0, digitBits, coder, Arrays::sort, config, parallelism);
    }

    /**
     * Stress-test on many small random arrays, sweeping both digit width and chunk/thread count,
     * comparing against Arrays.sort as the trusted reference -- the parallel analogue of
     * RadixHuskySortTest's testStressSmallLongArrays, which is what caught a real bug (an
     * off-by-one in a from-scratch quicksort partition) in this project's own history.
     */
    @Test
    public void testStressSmallLongArrays() {
        final Random r = new Random(7);
        for (final int digitBits : new int[]{1, 3, 7, 8, 11, 16, 20}) {
            for (final int parallelism : new int[]{1, 2, 5, 16}) {
                for (int trial = 0; trial < 50; trial++) {
                    final int n = 1 + r.nextInt(200);
                    final Long[] xs = new Long[n];
                    for (int i = 0; i < n; i++) xs[i] = r.nextLong();
                    final Long[] expected = Arrays.copyOf(xs, n);
                    Arrays.sort(expected);

                    final ParallelRadixHuskySort<Long> sorter = newSorter(digitBits, parallelism, HuskyCoderFactory.longCoder);
                    final Long[] ys = sorter.sort(xs);
                    assertArrayEquals("digitBits=" + digitBits + ", parallelism=" + parallelism + ", n=" + n, expected, ys);
                }
            }
        }
    }
}
